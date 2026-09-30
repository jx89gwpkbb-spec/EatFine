CREATE TABLE "inventory_items" (
	"id" serial PRIMARY KEY,
	"restaurant_id" text NOT NULL,
	"sku" text NOT NULL,
	"name" text NOT NULL,
	"category" text NOT NULL,
	"unit" text NOT NULL,
	"quantity" double precision DEFAULT 0 NOT NULL,
	"reorder_level" double precision DEFAULT 0 NOT NULL,
	"unit_cost" double precision DEFAULT 0 NOT NULL,
	"supplier" text DEFAULT '' NOT NULL,
	"updated_at" timestamp with time zone DEFAULT now() NOT NULL
);
--> statement-breakpoint
CREATE TABLE "menu_items" (
	"id" text PRIMARY KEY,
	"restaurant_id" text NOT NULL,
	"name" text NOT NULL,
	"description" text NOT NULL,
	"price" double precision NOT NULL,
	"category" text NOT NULL,
	"is_veg" boolean NOT NULL,
	"dietary" text[] NOT NULL,
	"calories" integer NOT NULL,
	"spice_level" integer DEFAULT 0 NOT NULL,
	"is_available" boolean DEFAULT true NOT NULL,
	"is_bestseller" boolean DEFAULT false NOT NULL,
	"rating" real DEFAULT 4.8 NOT NULL,
	"stock" integer DEFAULT 50 NOT NULL
);
--> statement-breakpoint
CREATE TABLE "orders" (
	"id" text PRIMARY KEY,
	"restaurant_id" text NOT NULL,
	"restaurant_name" text NOT NULL,
	"items" jsonb NOT NULL,
	"subtotal" double precision NOT NULL,
	"delivery_fee" double precision NOT NULL,
	"tax_and_fees" double precision NOT NULL,
	"discount" double precision DEFAULT 0 NOT NULL,
	"total" double precision NOT NULL,
	"status" text DEFAULT 'PLACED' NOT NULL,
	"delivery_type" text DEFAULT 'DELIVERY' NOT NULL,
	"delivery_address" text NOT NULL,
	"customer_name" text NOT NULL,
	"chef_notes" text DEFAULT '' NOT NULL,
	"payment_method" text NOT NULL,
	"estimated_minutes" integer NOT NULL,
	"driver_name" text NOT NULL,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL,
	"updated_at" timestamp with time zone DEFAULT now() NOT NULL
);
--> statement-breakpoint
CREATE TABLE "restaurants" (
	"id" text PRIMARY KEY,
	"name" text NOT NULL,
	"tagline" text NOT NULL,
	"cuisines" text NOT NULL,
	"rating" real NOT NULL,
	"review_count" integer DEFAULT 0 NOT NULL,
	"delivery_time_min" integer NOT NULL,
	"delivery_fee" double precision NOT NULL,
	"min_order" double precision NOT NULL,
	"price_tier" integer NOT NULL,
	"distance_km" double precision NOT NULL,
	"address" text NOT NULL,
	"is_pure_veg" boolean DEFAULT false NOT NULL,
	"dietary" text[] NOT NULL,
	"is_promoted" boolean DEFAULT false NOT NULL,
	"is_open" boolean DEFAULT true NOT NULL,
	"offer_text" text DEFAULT '' NOT NULL,
	"image" text NOT NULL,
	"hero_category" text NOT NULL,
	"certification_note" text DEFAULT '' NOT NULL
);
--> statement-breakpoint
CREATE TABLE "reviews" (
	"id" serial PRIMARY KEY,
	"restaurant_id" text NOT NULL,
	"user_name" text NOT NULL,
	"rating" real NOT NULL,
	"comment" text NOT NULL,
	"dietary_tags" text DEFAULT '' NOT NULL,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL
);
--> statement-breakpoint
CREATE TABLE "stock_movements" (
	"id" serial PRIMARY KEY,
	"inventory_item_id" integer NOT NULL,
	"change" double precision NOT NULL,
	"reason" text NOT NULL,
	"note" text DEFAULT '' NOT NULL,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL
);
--> statement-breakpoint
ALTER TABLE "inventory_items" ADD CONSTRAINT "inventory_items_restaurant_id_restaurants_id_fkey" FOREIGN KEY ("restaurant_id") REFERENCES "restaurants"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "menu_items" ADD CONSTRAINT "menu_items_restaurant_id_restaurants_id_fkey" FOREIGN KEY ("restaurant_id") REFERENCES "restaurants"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "orders" ADD CONSTRAINT "orders_restaurant_id_restaurants_id_fkey" FOREIGN KEY ("restaurant_id") REFERENCES "restaurants"("id");--> statement-breakpoint
ALTER TABLE "reviews" ADD CONSTRAINT "reviews_restaurant_id_restaurants_id_fkey" FOREIGN KEY ("restaurant_id") REFERENCES "restaurants"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "stock_movements" ADD CONSTRAINT "stock_movements_inventory_item_id_inventory_items_id_fkey" FOREIGN KEY ("inventory_item_id") REFERENCES "inventory_items"("id") ON DELETE CASCADE;