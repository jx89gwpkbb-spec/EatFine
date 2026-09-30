export type Restaurant = {
  id: string;
  name: string;
  tagline: string;
  cuisines: string;
  rating: number;
  reviewCount: number;
  deliveryTimeMin: number;
  deliveryFee: number;
  minOrder: number;
  priceTier: number;
  distanceKm: number;
  address: string;
  isPureVeg: boolean;
  dietary: string[];
  isPromoted: boolean;
  isOpen: boolean;
  offerText: string;
  image: string;
  heroCategory: string;
  certificationNote: string;
};

export type MenuItem = {
  id: string;
  restaurantId: string;
  name: string;
  description: string;
  price: number;
  category: string;
  isVeg: boolean;
  dietary: string[];
  calories: number;
  spiceLevel: number;
  isAvailable: boolean;
  isBestseller: boolean;
  rating: number;
  stock: number;
};

export type Review = {
  id: number;
  restaurantId: string;
  userName: string;
  rating: number;
  comment: string;
  dietaryTags: string;
  createdAt: string;
};

export type RestaurantDetail = Restaurant & { menu: MenuItem[]; reviews: Review[] };

export type OrderLine = { menuItemId: string; name: string; price: number; quantity: number };

export type Order = {
  id: string;
  restaurantId: string;
  restaurantName: string;
  items: OrderLine[];
  subtotal: number;
  deliveryFee: number;
  taxAndFees: number;
  discount: number;
  total: number;
  status: OrderStatus;
  deliveryType: "DELIVERY" | "PICKUP";
  deliveryAddress: string;
  customerName: string;
  chefNotes: string;
  paymentMethod: string;
  estimatedMinutes: number;
  driverName: string;
  createdAt: string;
  updatedAt: string;
};

export type OrderStatus = "PLACED" | "ACCEPTED" | "PREPARING" | "READY_FOR_PICKUP" | "ON_THE_WAY" | "DELIVERED" | "CANCELLED";

export type InventoryItem = {
  id: number;
  restaurantId: string;
  restaurantName: string;
  sku: string;
  name: string;
  category: string;
  unit: string;
  quantity: number;
  reorderLevel: number;
  unitCost: number;
  supplier: string;
  updatedAt: string;
};

export type StockMovement = {
  id: number;
  inventoryItemId: number;
  change: number;
  reason: string;
  note: string;
  createdAt: string;
  itemName: string;
  unit: string;
  restaurantName: string;
};

export type Stats = {
  totalOrders: number;
  activeOrders: number;
  revenue: number;
  inventory: { items: number; lowStock: number; value: number };
  menu: { soldOut: number; lowPortions: number };
  restaurants: { open: number; total: number };
  byRestaurant: { restaurantName: string; orders: number; revenue: number }[];
};
