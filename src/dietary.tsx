export type DietKey = "VEGETARIAN" | "VEGAN" | "GLUTEN_FREE" | "HALAL" | "JAIN" | "DAIRY_FREE" | "NUT_FREE" | "KETO" | "ORGANIC";

export const DIETARY: Record<DietKey, { label: string; short: string; description: string; tone: string }> = {
  VEGETARIAN: { label: "Vegetarian", short: "Veg", description: "No meat, fish or poultry", tone: "green" },
  VEGAN: { label: "Vegan", short: "Vegan", description: "100% plant-based, no animal byproducts", tone: "green" },
  GLUTEN_FREE: { label: "Gluten-Free", short: "GF", description: "Safe for celiac or gluten intolerance", tone: "blue" },
  HALAL: { label: "Halal", short: "Halal", description: "Certified Halal preparation", tone: "amber" },
  JAIN: { label: "Jain Friendly", short: "Jain", description: "No root vegetables (onion, garlic, potato)", tone: "purple" },
  DAIRY_FREE: { label: "Dairy-Free", short: "DF", description: "No milk, cheese, lactose or butter", tone: "blue" },
  NUT_FREE: { label: "Nut-Free", short: "Nut-Safe", description: "Prepared in peanut & tree nut safe kitchen", tone: "amber" },
  KETO: { label: "Keto / Low-Carb", short: "Keto", description: "High protein & healthy fats, under 10g net carbs", tone: "orange" },
  ORGANIC: { label: "Organic", short: "Organic", description: "Certified pesticide-free natural ingredients", tone: "green" },
};

export const DIET_KEYS = Object.keys(DIETARY) as DietKey[];

export function DietTag({ k }: { k: string }) {
  const d = DIETARY[k as DietKey];
  if (!d) return null;
  return (
    <span className={`diet-tag tone-${d.tone}`} title={d.description}>
      {d.short}
    </span>
  );
}
