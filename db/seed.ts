import { sql } from "drizzle-orm";
import { db } from "./index.js";
import { restaurants, menuItems, reviews, inventoryItems } from "./schema.js";
import { seedRestaurants, seedMenuItems, seedReviews, seedInventory } from "./seed-data.js";

let seeded = false;

// Populates the fresh database with the EatFine catalogue the first time any API is hit
export async function ensureSeeded() {
  if (seeded) return;
  const [{ count }] = await db.select({ count: sql<number>`count(*)::int` }).from(restaurants);
  if (count === 0) {
    await db.insert(restaurants).values(seedRestaurants).onConflictDoNothing();
    await db.insert(menuItems).values(seedMenuItems).onConflictDoNothing();
    const [{ reviewCount }] = await db.select({ reviewCount: sql<number>`count(*)::int` }).from(reviews);
    if (reviewCount === 0) await db.insert(reviews).values(seedReviews);
    const [{ invCount }] = await db.select({ invCount: sql<number>`count(*)::int` }).from(inventoryItems);
    if (invCount === 0) await db.insert(inventoryItems).values(seedInventory);
  }
  seeded = true;
}
