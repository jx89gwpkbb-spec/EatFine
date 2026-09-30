import type { Config } from "@netlify/functions";
import { eq, sql } from "drizzle-orm";
import { db } from "../../db/index.js";
import { restaurants, reviews } from "../../db/schema.js";
import { handle, HttpError, json, readBody } from "./_shared/http.js";

export default handle(async (req) => {
  if (req.method !== "POST") throw new HttpError(405, "Method not allowed");
  const body = await readBody<{ restaurantId?: string; userName?: string; rating?: number; comment?: string; dietaryTags?: string }>(req);
  const rating = Number(body.rating);
  const userName = String(body.userName ?? "").trim().slice(0, 60);
  const comment = String(body.comment ?? "").trim().slice(0, 1000);
  if (!body.restaurantId) throw new HttpError(400, "restaurantId is required");
  if (!userName || !comment) throw new HttpError(400, "Name and comment are required");
  if (!(rating >= 1 && rating <= 5)) throw new HttpError(400, "Rating must be between 1 and 5");

  const [restaurant] = await db.select().from(restaurants).where(eq(restaurants.id, body.restaurantId));
  if (!restaurant) throw new HttpError(404, "Restaurant not found");

  const [review] = await db
    .insert(reviews)
    .values({ restaurantId: restaurant.id, userName, rating, comment, dietaryTags: String(body.dietaryTags ?? "").slice(0, 200) })
    .returning();
  // Roll the new rating into the restaurant's running average
  await db
    .update(restaurants)
    .set({
      rating: sql`round(((${restaurants.rating} * ${restaurants.reviewCount} + ${rating}) / (${restaurants.reviewCount} + 1))::numeric, 1)`,
      reviewCount: sql`${restaurants.reviewCount} + 1`,
    })
    .where(eq(restaurants.id, restaurant.id));
  return json(review, 201);
});

export const config: Config = { path: "/api/reviews" };
