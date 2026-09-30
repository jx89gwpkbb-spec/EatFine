import type { Config } from "@netlify/functions";
import { desc, sql } from "drizzle-orm";
import { db } from "../../db/index.js";
import { inventoryItems, menuItems, orders, restaurants } from "../../db/schema.js";
import { ensureSeeded } from "../../db/seed.js";
import { handle, json } from "./_shared/http.js";

export default handle(async () => {
  await ensureSeeded();
  const [[orderStats], [inv], [menu], [rest], byRestaurant] = await Promise.all([
    db
      .select({
        totalOrders: sql<number>`count(*)::int`,
        activeOrders: sql<number>`count(*) filter (where ${orders.status} not in ('DELIVERED','CANCELLED'))::int`,
        revenue: sql<number>`coalesce(sum(${orders.total}) filter (where ${orders.status} <> 'CANCELLED'), 0)::float`,
      })
      .from(orders),
    db
      .select({
        items: sql<number>`count(*)::int`,
        lowStock: sql<number>`count(*) filter (where ${inventoryItems.quantity} <= ${inventoryItems.reorderLevel})::int`,
        value: sql<number>`coalesce(sum(${inventoryItems.quantity} * ${inventoryItems.unitCost}), 0)::float`,
      })
      .from(inventoryItems),
    db
      .select({
        soldOut: sql<number>`count(*) filter (where ${menuItems.stock} = 0 or not ${menuItems.isAvailable})::int`,
        lowPortions: sql<number>`count(*) filter (where ${menuItems.stock} > 0 and ${menuItems.stock} <= 10)::int`,
      })
      .from(menuItems),
    db.select({ open: sql<number>`count(*) filter (where ${restaurants.isOpen})::int`, total: sql<number>`count(*)::int` }).from(restaurants),
    db
      .select({
        restaurantName: orders.restaurantName,
        orders: sql<number>`count(*)::int`,
        revenue: sql<number>`coalesce(sum(${orders.total}), 0)::float`,
      })
      .from(orders)
      .where(sql`${orders.status} <> 'CANCELLED'`)
      .groupBy(orders.restaurantName)
      .orderBy(desc(sql`sum(${orders.total})`)),
  ]);
  return json({ ...orderStats, inventory: inv, menu, restaurants: rest, byRestaurant });
});

export const config: Config = { path: "/api/stats" };
