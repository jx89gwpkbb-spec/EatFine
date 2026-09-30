import { createContext, useContext, useEffect, useState, type ReactNode } from "react";
import type { MenuItem, Restaurant } from "./types";

export type CartLine = { item: MenuItem; quantity: number };
type Cart = { restaurant: Pick<Restaurant, "id" | "name" | "deliveryFee" | "minOrder" | "address"> | null; lines: CartLine[] };

type CartApi = Cart & {
  count: number;
  subtotal: number;
  add: (restaurant: Restaurant, item: MenuItem) => boolean;
  setQty: (itemId: string, qty: number) => void;
  clear: () => void;
};

const CartContext = createContext<CartApi | null>(null);
const KEY = "eatfine.cart";

export function CartProvider({ children }: { children: ReactNode }) {
  const [cart, setCart] = useState<Cart>(() => {
    try {
      return JSON.parse(localStorage.getItem(KEY) || "") as Cart;
    } catch {
      return { restaurant: null, lines: [] };
    }
  });

  useEffect(() => localStorage.setItem(KEY, JSON.stringify(cart)), [cart]);

  const api: CartApi = {
    ...cart,
    count: cart.lines.reduce((s, l) => s + l.quantity, 0),
    subtotal: cart.lines.reduce((s, l) => s + l.quantity * l.item.price, 0),
    add(restaurant, item) {
      // A cart holds items from one kitchen at a time, as in the mobile app
      if (cart.restaurant && cart.restaurant.id !== restaurant.id && cart.lines.length) {
        if (!confirm(`Your cart has items from ${cart.restaurant.name}. Start a new cart with ${restaurant.name}?`)) return false;
        setCart({ restaurant, lines: [{ item, quantity: 1 }] });
        return true;
      }
      setCart((c) => {
        const existing = c.lines.find((l) => l.item.id === item.id);
        const lines = existing
          ? c.lines.map((l) => (l.item.id === item.id ? { ...l, quantity: Math.min(l.quantity + 1, item.stock) } : l))
          : [...c.lines, { item, quantity: 1 }];
        const { id, name, deliveryFee, minOrder, address } = restaurant;
        return { restaurant: { id, name, deliveryFee, minOrder, address }, lines };
      });
      return true;
    },
    setQty(itemId, qty) {
      setCart((c) => {
        const lines = c.lines
          .map((l) => (l.item.id === itemId ? { ...l, quantity: Math.min(qty, l.item.stock) } : l))
          .filter((l) => l.quantity > 0);
        return { restaurant: lines.length ? c.restaurant : null, lines };
      });
    },
    clear: () => setCart({ restaurant: null, lines: [] }),
  };

  return <CartContext.Provider value={api}>{children}</CartContext.Provider>;
}

export function useCart() {
  const ctx = useContext(CartContext);
  if (!ctx) throw new Error("useCart outside CartProvider");
  return ctx;
}

// Orders placed from this browser, so customers can find them again without an account
const ORDERS_KEY = "eatfine.orders";
export const myOrderIds = (): string[] => {
  try {
    return JSON.parse(localStorage.getItem(ORDERS_KEY) || "[]");
  } catch {
    return [];
  }
};
export const rememberOrder = (id: string) => localStorage.setItem(ORDERS_KEY, JSON.stringify([id, ...myOrderIds()].slice(0, 50)));
