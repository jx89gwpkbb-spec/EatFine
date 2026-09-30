import type { Config } from "@netlify/functions";
import { eq } from "drizzle-orm";
import { db } from "../../db/index.js";
import { menuItems } from "../../db/schema.js";
import { handle, HttpError, json, readBody } from "./_shared/http.js";

export default handle(async (req, { id }) => {
  if (req.method !== "PATCH") throw new HttpError(405, "Method not allowed");
  const body = await readBody<{ isAvailable?: boolean; stock?: number; price?: number }>(req);
  const updates: Partial<typeof menuItems.$inferInsert> = {};
  if (typeof body.isAvailable === "boolean") updates.isAvailable = body.isAvailable;
  if (body.stock !== undefined) {
    const stock = Math.floor(Number(body.stock));
    if (!Number.isFinite(stock) || stock < 0) throw new HttpError(400, "Stock must be 0 or more");
    updates.stock = stock;
  }
  if (body.price !== undefined) {
    const price = Number(body.price);
    if (!Number.isFinite(price) || price <= 0) throw new HttpError(400, "Price must be positive");
    updates.price = price;
  }
  if (Object.keys(updates).length === 0) throw new HttpError(400, "Nothing to update");
  const [row] = await db.update(menuItems).set(updates).where(eq(menuItems.id, id)).returning();
  if (!row) throw new HttpError(404, "Menu item not found");
  return json(row);
});

export const config: Config = { path: "/api/menu-items/:id" };
