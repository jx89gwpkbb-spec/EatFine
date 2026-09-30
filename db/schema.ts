import {
  pgTable,
  text,
  integer,
  boolean,
  doublePrecision,
  real,
  serial,
  jsonb,
  timestamp,
} from "drizzle-orm/pg-core";

export const restaurants = pgTable("restaurants", {
  id: text().primaryKey(),
  name: text().notNull(),
  tagline: text().notNull(),
  cuisines: text().notNull(),
  rating: real().notNull(),
  reviewCount: integer("review_count").notNull().default(0),
  deliveryTimeMin: integer("delivery_time_min").notNull(),
  deliveryFee: doublePrecision("delivery_fee").notNull(),
  minOrder: doublePrecision("min_order").notNull(),
  priceTier: integer("price_tier").notNull(),
  distanceKm: doublePrecision("distance_km").notNull(),
  address: text().notNull(),
  isPureVeg: boolean("is_pure_veg").notNull().default(false),
  dietary: text().array().notNull(),
  isPromoted: boolean("is_promoted").notNull().default(false),
  isOpen: boolean("is_open").notNull().default(true),
  offerText: text("offer_text").notNull().default(""),
  image: text().notNull(),
  heroCategory: text("hero_category").notNull(),
  certificationNote: text("certification_note").notNull().default(""),
});

export const menuItems = pgTable("menu_items", {
  id: text().primaryKey(),
  restaurantId: text("restaurant_id")
    .notNull()
    .references(() => restaurants.id, { onDelete: "cascade" }),
  name: text().notNull(),
  description: text().notNull(),
  price: doublePrecision().notNull(),
  category: text().notNull(),
  isVeg: boolean("is_veg").notNull(),
  dietary: text().array().notNull(),
  calories: integer().notNull(),
  spiceLevel: integer("spice_level").notNull().default(0),
  isAvailable: boolean("is_available").notNull().default(true),
  isBestseller: boolean("is_bestseller").notNull().default(false),
  rating: real().notNull().default(4.8),
  // Portions ready to sell; decremented automatically when orders are placed
  stock: integer().notNull().default(50),
});

export type OrderLine = {
  menuItemId: string;
  name: string;
  price: number;
  quantity: number;
};

export const orders = pgTable("orders", {
  id: text().primaryKey(),
  restaurantId: text("restaurant_id")
    .notNull()
    .references(() => restaurants.id),
  restaurantName: text("restaurant_name").notNull(),
  items: jsonb().$type<OrderLine[]>().notNull(),
  subtotal: doublePrecision().notNull(),
  deliveryFee: doublePrecision("delivery_fee").notNull(),
  taxAndFees: doublePrecision("tax_and_fees").notNull(),
  discount: doublePrecision().notNull().default(0),
  total: doublePrecision().notNull(),
  status: text().notNull().default("PLACED"),
  deliveryType: text("delivery_type").notNull().default("DELIVERY"),
  deliveryAddress: text("delivery_address").notNull(),
  customerName: text("customer_name").notNull(),
  chefNotes: text("chef_notes").notNull().default(""),
  paymentMethod: text("payment_method").notNull(),
  estimatedMinutes: integer("estimated_minutes").notNull(),
  driverName: text("driver_name").notNull(),
  createdAt: timestamp("created_at", { withTimezone: true }).notNull().defaultNow(),
  updatedAt: timestamp("updated_at", { withTimezone: true }).notNull().defaultNow(),
});

export const reviews = pgTable("reviews", {
  id: serial().primaryKey(),
  restaurantId: text("restaurant_id")
    .notNull()
    .references(() => restaurants.id, { onDelete: "cascade" }),
  userName: text("user_name").notNull(),
  rating: real().notNull(),
  comment: text().notNull(),
  dietaryTags: text("dietary_tags").notNull().default(""),
  createdAt: timestamp("created_at", { withTimezone: true }).notNull().defaultNow(),
});

// Inventory system: ingredients and supplies stocked by each restaurant kitchen
export const inventoryItems = pgTable("inventory_items", {
  id: serial().primaryKey(),
  restaurantId: text("restaurant_id")
    .notNull()
    .references(() => restaurants.id, { onDelete: "cascade" }),
  sku: text().notNull(),
  name: text().notNull(),
  category: text().notNull(),
  unit: text().notNull(),
  quantity: doublePrecision().notNull().default(0),
  reorderLevel: doublePrecision("reorder_level").notNull().default(0),
  unitCost: doublePrecision("unit_cost").notNull().default(0),
  supplier: text().notNull().default(""),
  updatedAt: timestamp("updated_at", { withTimezone: true }).notNull().defaultNow(),
});

export const stockMovements = pgTable("stock_movements", {
  id: serial().primaryKey(),
  inventoryItemId: integer("inventory_item_id")
    .notNull()
    .references(() => inventoryItems.id, { onDelete: "cascade" }),
  change: doublePrecision().notNull(),
  reason: text().notNull(),
  note: text().notNull().default(""),
  createdAt: timestamp("created_at", { withTimezone: true }).notNull().defaultNow(),
});
