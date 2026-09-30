import type { Config } from "@netlify/functions";
import { and, asc, desc, eq, sql } from "drizzle-orm";
import { db } from "../../db/index.js";
import { inventoryItems, restaurants, stockMovements } from "../../db/schema.js";
import { ensureSeeded } from "../../db/seed.js";
import { handle, HttpError, json, readBody } from "./_shared/http.js";

const REASONS = ["RESTOCK", "USAGE", "WASTE", "CORRECTION"];

type ItemBody = Partial<{
  restaurantId: string;
  sku: string;
  name: string;
  category: string;
  unit: string;
  quantity: number;
  reorderLevel: number;
  unitCost: number;
  supplier: string;
}>;

function cleanItem(body: ItemBody, partial: boolean) {
  const out: Partial<typeof inventoryItems.$inferInsert> = {};
  for (const key of ["sku", "name", "category", "unit", "supplier"] as const) {
    if (body[key] !== undefined) out[key] = String(body[key]).trim().slice(0, 120);
  }
  for (const key of ["quantity", "reorderLevel", "unitCost"] as const) {
    if (body[key] !== undefined) {
      const n = Number(body[key]);
      if (!Number.isFinite(n) || n < 0) throw new HttpError(400, `${key} must be 0 or more`);
      out[key] = n;
    }
  }
  if (!partial) {
    for (const key of ["sku", "name", "category", "unit"] as const) {
      if (!out[key]) throw new HttpError(400, `${key} is required`);
    }
  }
  return out;
}

export default handle(async (req, { id, action }) => {
  await ensureSeeded();
  const url = new URL(req.url);

  if (!id) {
    if (req.method === "GET") {
      const restaurantId = url.searchParams.get("restaurantId");
      const rows = await db
        .select({ item: inventoryItems, restaurantName: restaurants.name })
        .from(inventoryItems)
        .innerJoin(restaurants, eq(inventoryItems.restaurantId, restaurants.id))
        .where(restaurantId ? eq(inventoryItems.restaurantId, restaurantId) : undefined)
        .orderBy(asc(restaurants.name), asc(inventoryItems.category), asc(inventoryItems.name));
      return json(rows.map((r) => ({ ...r.item, restaurantName: r.restaurantName })));
    }
    if (req.method === "POST") {
      const body = await readBody<ItemBody>(req);
      if (!body.restaurantId) throw new HttpError(400, "restaurantId is required");
      const values = cleanItem(body, false);
      const [row] = await db
        .insert(inventoryItems)
        .values({ ...values, restaurantId: body.restaurantId } as typeof inventoryItems.$inferInsert)
        .returning();
      if (row.quantity > 0) {
        await db.insert(stockMovements).values({ inventoryItemId: row.id, change: row.quantity, reason: "RESTOCK", note: "Opening stock" });
      }
      return json(row, 201);
    }
    throw new HttpError(405, "Method not allowed");
  }

  if (id === "movements" && req.method === "GET") {
    const restaurantId = url.searchParams.get("restaurantId");
    const rows = await db
      .select({ movement: stockMovements, itemName: inventoryItems.name, unit: inventoryItems.unit, restaurantName: restaurants.name })
      .from(stockMovements)
      .innerJoin(inventoryItems, eq(stockMovements.inventoryItemId, inventoryItems.id))
      .innerJoin(restaurants, eq(inventoryItems.restaurantId, restaurants.id))
      .where(restaurantId ? eq(inventoryItems.restaurantId, restaurantId) : undefined)
      .orderBy(desc(stockMovements.createdAt))
      .limit(50);
    return json(rows.map((r) => ({ ...r.movement, itemName: r.itemName, unit: r.unit, restaurantName: r.restaurantName })));
  }

  const itemId = Number(id);
  if (!Number.isInteger(itemId)) throw new HttpError(404, "Not found");

  if (action === "adjust") {
    if (req.method !== "POST") throw new HttpError(405, "Method not allowed");
    const body = await readBody<{ change?: number; reason?: string; note?: string }>(req);
    const change = Number(body.change);
    const reason = String(body.reason ?? "");
    if (!Number.isFinite(change) || change === 0) throw new HttpError(400, "Enter a non-zero quantity");
    if (!REASONS.includes(reason)) throw new HttpError(400, "Invalid reason");
    const row = await db.transaction(async (tx) => {
      const [updated] = await tx
        .update(inventoryItems)
        .set({ quantity: sql`${inventoryItems.quantity} + ${change}`, updatedAt: new Date() })
        .where(and(eq(inventoryItems.id, itemId), sql`${inventoryItems.quantity} + ${change} >= 0`))
        .returning();
      if (!updated) throw new HttpError(409, "Not enough stock for that adjustment");
      await tx.insert(stockMovements).values({ inventoryItemId: itemId, change, reason, note: String(body.note ?? "").slice(0, 200) });
      return updated;
    });
    return json(row);
  }

  if (req.method === "PATCH") {
    const values = cleanItem(await readBody<ItemBody>(req), true);
    // Quantity changes go through /adjust so every change is logged
    delete values.quantity;
    if (Object.keys(values).length === 0) throw new HttpError(400, "Nothing to update");
    const [row] = await db.update(inventoryItems).set({ ...values, updatedAt: new Date() }).where(eq(inventoryItems.id, itemId)).returning();
    if (!row) throw new HttpError(404, "Item not found");
    return json(row);
  }

  if (req.method === "DELETE") {
    const [row] = await db.delete(inventoryItems).where(eq(inventoryItems.id, itemId)).returning();
    if (!row) throw new HttpError(404, "Item not found");
    return new Response(null, { status: 204 });
  }

  throw new HttpError(405, "Method not allowed");
});

export const config: Config = { path: ["/api/inventory", "/api/inventory/:id", "/api/inventory/:id/:action"] };
