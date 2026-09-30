import type { Config } from "@netlify/functions";
import { asc, desc, eq } from "drizzle-orm";
import { db } from "../../db/index.js";
import { menuItems, restaurants, reviews } from "../../db/schema.js";
import { ensureSeeded } from "../../db/seed.js";
import { handle, HttpError, json, readBody } from "./_shared/http.js";

export default handle(async (req, params) => {
  await ensureSeeded();
  const { id } = params;

  if (!id) {
    if (req.method !== "GET") throw new HttpError(405, "Method not allowed");
    const rows = await db.select().from(restaurants).orderBy(desc(restaurants.isPromoted), desc(restaurants.rating));
    return json(rows);
  }

  if (req.method === "GET") {
    const [restaurant] = await db.select().from(restaurants).where(eq(restaurants.id, id));
    if (!restaurant) throw new HttpError(404, "Restaurant not found");
    const [menu, reviewRows] = await Promise.all([
      db.select().from(menuItems).where(eq(menuItems.restaurantId, id)).orderBy(asc(menuItems.category), asc(menuItems.name)),
      db.select().from(reviews).where(eq(reviews.restaurantId, id)).orderBy(desc(reviews.createdAt)),
    ]);
    return json({ ...restaurant, menu, reviews: reviewRows });
  }

  if (req.method === "PATCH") {
    const body = await readBody<{ isOpen?: boolean }>(req);
    if (typeof body.isOpen !== "boolean") throw new HttpError(400, "isOpen must be a boolean");
    const [row] = await db.update(restaurants).set({ isOpen: body.isOpen }).where(eq(restaurants.id, id)).returning();
    if (!row) throw new HttpError(404, "Restaurant not found");
    return json(row);
  }

  throw new HttpError(405, "Method not allowed");
});

export const config: Config = { path: ["/api/restaurants", "/api/restaurants/:id"] };
