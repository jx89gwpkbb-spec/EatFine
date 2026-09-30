import type { Config } from "@netlify/functions";
import { and, desc, eq, gte, inArray, sql } from "drizzle-orm";
import { db } from "../../db/index.js";
import { menuItems, orders, restaurants, type OrderLine } from "../../db/schema.js";
import { ensureSeeded } from "../../db/seed.js";
import { handle, HttpError, json, readBody, round2 } from "./_shared/http.js";

const STATUSES = ["PLACED", "ACCEPTED", "PREPARING", "READY_FOR_PICKUP", "ON_THE_WAY", "DELIVERED", "CANCELLED"];
const DRIVERS = ["Alex Rivera", "Marcus Vance", "Samantha Wu", "Liam Patel", "Priya Nair"];
const TAX_RATE = 0.08;

type PlaceOrderBody = {
  restaurantId?: string;
  items?: { menuItemId: string; quantity: number }[];
  customerName?: string;
  deliveryAddress?: string;
  deliveryType?: "DELIVERY" | "PICKUP";
  chefNotes?: string;
  paymentMethod?: string;
  promoCode?: string;
};

async function placeOrder(body: PlaceOrderBody) {
  const lines = (body.items ?? []).filter((l) => l && l.menuItemId && Number(l.quantity) > 0);
  const customerName = String(body.customerName ?? "").trim().slice(0, 80);
  const deliveryType = body.deliveryType === "PICKUP" ? "PICKUP" : "DELIVERY";
  const deliveryAddress = String(body.deliveryAddress ?? "").trim().slice(0, 200);
  if (!body.restaurantId) throw new HttpError(400, "restaurantId is required");
  if (lines.length === 0) throw new HttpError(400, "Your cart is empty");
  if (!customerName) throw new HttpError(400, "Please enter your name");
  if (deliveryType === "DELIVERY" && !deliveryAddress) throw new HttpError(400, "Please enter a delivery address");

  const [restaurant] = await db.select().from(restaurants).where(eq(restaurants.id, body.restaurantId));
  if (!restaurant) throw new HttpError(404, "Restaurant not found");
  if (!restaurant.isOpen) throw new HttpError(409, `${restaurant.name} is currently closed`);

  const ids = lines.map((l) => l.menuItemId);
  const menu = await db
    .select()
    .from(menuItems)
    .where(and(eq(menuItems.restaurantId, restaurant.id), inArray(menuItems.id, ids)));
  const byId = new Map(menu.map((m) => [m.id, m]));

  const orderLines: OrderLine[] = lines.map((l) => {
    const item = byId.get(l.menuItemId);
    if (!item) throw new HttpError(400, "An item in your cart is no longer on the menu");
    if (!item.isAvailable) throw new HttpError(409, `${item.name} is unavailable right now`);
    const quantity = Math.min(Math.floor(Number(l.quantity)), 20);
    if (item.stock < quantity) throw new HttpError(409, `Only ${item.stock} × ${item.name} left in stock`);
    return { menuItemId: item.id, name: item.name, price: item.price, quantity };
  });

  const subtotal = round2(orderLines.reduce((s, l) => s + l.price * l.quantity, 0));
  if (subtotal < restaurant.minOrder) throw new HttpError(400, `Minimum order is $${restaurant.minOrder.toFixed(2)}`);
  const deliveryFee = deliveryType === "DELIVERY" ? restaurant.deliveryFee : 0;
  const discount = String(body.promoCode ?? "").trim().toUpperCase() === "EATFINE10" ? round2(subtotal * 0.1) : 0;
  const taxAndFees = round2((subtotal - discount) * TAX_RATE);
  const total = round2(subtotal - discount + deliveryFee + taxAndFees);
  const id = `EF-${Math.floor(100000 + Math.random() * 900000)}`;

  return db.transaction(async (tx) => {
    for (const line of orderLines) {
      // Conditional decrement guards against two orders racing for the last portions
      const updated = await tx
        .update(menuItems)
        .set({ stock: sql`${menuItems.stock} - ${line.quantity}` })
        .where(and(eq(menuItems.id, line.menuItemId), gte(menuItems.stock, line.quantity)))
        .returning({ id: menuItems.id });
      if (updated.length === 0) throw new HttpError(409, `${line.name} just sold out`);
    }
    const [order] = await tx
      .insert(orders)
      .values({
        id,
        restaurantId: restaurant.id,
        restaurantName: restaurant.name,
        items: orderLines,
        subtotal,
        deliveryFee,
        taxAndFees,
        discount,
        total,
        deliveryType,
        deliveryAddress: deliveryType === "PICKUP" ? restaurant.address : deliveryAddress,
        customerName,
        chefNotes: String(body.chefNotes ?? "").slice(0, 500),
        paymentMethod: String(body.paymentMethod ?? "EatFine Pay (Card)").slice(0, 40),
        estimatedMinutes: restaurant.deliveryTimeMin + (deliveryType === "DELIVERY" ? 10 : 0),
        driverName: DRIVERS[Math.floor(Math.random() * DRIVERS.length)],
      })
      .returning();
    return order;
  });
}

export default handle(async (req, { id }) => {
  await ensureSeeded();

  if (!id) {
    if (req.method === "GET") {
      const url = new URL(req.url);
      const restaurantId = url.searchParams.get("restaurantId");
      const idsParam = url.searchParams.get("ids");
      const filters = [];
      if (restaurantId) filters.push(eq(orders.restaurantId, restaurantId));
      if (idsParam !== null) {
        const ids = idsParam.split(",").filter(Boolean).slice(0, 100);
        if (ids.length === 0) return json([]);
        filters.push(inArray(orders.id, ids));
      }
      const rows = await db
        .select()
        .from(orders)
        .where(filters.length ? and(...filters) : undefined)
        .orderBy(desc(orders.createdAt))
        .limit(200);
      return json(rows);
    }
    if (req.method === "POST") return json(await placeOrder(await readBody<PlaceOrderBody>(req)), 201);
    throw new HttpError(405, "Method not allowed");
  }

  if (req.method === "GET") {
    const [order] = await db.select().from(orders).where(eq(orders.id, id));
    if (!order) throw new HttpError(404, "Order not found");
    return json(order);
  }

  if (req.method === "PATCH") {
    const { status } = await readBody<{ status?: string }>(req);
    if (!status || !STATUSES.includes(status)) throw new HttpError(400, "Invalid status");
    const [existing] = await db.select().from(orders).where(eq(orders.id, id));
    if (!existing) throw new HttpError(404, "Order not found");
    if (existing.status === "DELIVERED" || existing.status === "CANCELLED") {
      throw new HttpError(409, "This order is already closed");
    }
    const order = await db.transaction(async (tx) => {
      // Cancelled orders return their portions to menu stock
      if (status === "CANCELLED") {
        for (const line of existing.items) {
          await tx
            .update(menuItems)
            .set({ stock: sql`${menuItems.stock} + ${line.quantity}` })
            .where(eq(menuItems.id, line.menuItemId));
        }
      }
      const [row] = await tx.update(orders).set({ status, updatedAt: new Date() }).where(eq(orders.id, id)).returning();
      return row;
    });
    return json(order);
  }

  throw new HttpError(405, "Method not allowed");
});

export const config: Config = { path: ["/api/orders", "/api/orders/:id"] };
