import { useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { money } from "../api";
import { DIETARY, DIET_KEYS, DietTag, type DietKey } from "../dietary";
import { Skeleton, Stars } from "../components/Stars";
import { useFetch } from "../useFetch";
import type { Restaurant } from "../types";

const SORTS = {
  RECOMMENDED: "Recommended",
  RATING: "Highest rated",
  DELIVERY_TIME: "Fastest delivery",
  PRICE_LOW: "Price: low to high",
  DISTANCE: "Nearest",
} as const;

export default function Home() {
  const { data, loading, error } = useFetch<Restaurant[]>("/api/restaurants");
  const [query, setQuery] = useState("");
  const [selected, setSelected] = useState<DietKey[]>([]);
  const [matchAll, setMatchAll] = useState(false);
  const [pureVeg, setPureVeg] = useState(false);
  const [sort, setSort] = useState<keyof typeof SORTS>("RECOMMENDED");

  const toggle = (k: DietKey) => setSelected((s) => (s.includes(k) ? s.filter((x) => x !== k) : [...s, k]));

  const list = useMemo(() => {
    let rows = data ?? [];
    const q = query.trim().toLowerCase();
    if (q) rows = rows.filter((r) => `${r.name} ${r.cuisines} ${r.tagline} ${r.heroCategory}`.toLowerCase().includes(q));
    if (pureVeg) rows = rows.filter((r) => r.isPureVeg);
    if (selected.length) {
      rows = rows.filter((r) => (matchAll ? selected.every((k) => r.dietary.includes(k)) : selected.some((k) => r.dietary.includes(k))));
    }
    const sorted = [...rows];
    if (sort === "RATING") sorted.sort((a, b) => b.rating - a.rating);
    if (sort === "DELIVERY_TIME") sorted.sort((a, b) => a.deliveryTimeMin - b.deliveryTimeMin);
    if (sort === "PRICE_LOW") sorted.sort((a, b) => a.priceTier - b.priceTier);
    if (sort === "DISTANCE") sorted.sort((a, b) => a.distanceKm - b.distanceKm);
    return sorted;
  }, [data, query, selected, matchAll, pureVeg, sort]);

  return (
    <>
      <section className="hero">
        <div className="hero-copy">
          <p className="eyebrow">Dietary-first food delivery</p>
          <h1>
            Good food.
            <br />
            <span>Great moments.</span>
          </h1>
          <p className="lede">
            Celiac-safe bakes, certified Halal biryani, Jain thalis with zero root vegetables. Filter by what you can eat, then order from kitchens that take it seriously.
          </p>
          <label className="search">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden>
              <circle cx="11" cy="11" r="7" />
              <path d="m20 20-3.5-3.5" />
            </svg>
            <input value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Search biryani, keto, gluten-free pizza…" />
          </label>
        </div>
        <div className="hero-art">
          <img src="/images/hero.jpg" alt="A spread of dishes from EatFine partner kitchens" />
          <div className="hero-badge">
            <strong>{data?.length ?? 7}</strong> kitchens verified for dietary safety
          </div>
        </div>
      </section>

      <section className="filters">
        <div className="chip-row">
          {DIET_KEYS.map((k) => (
            <button key={k} className={`chip tone-${DIETARY[k].tone} ${selected.includes(k) ? "on" : ""}`} onClick={() => toggle(k)} aria-pressed={selected.includes(k)} title={DIETARY[k].description}>
              {DIETARY[k].label}
            </button>
          ))}
        </div>
        <div className="filter-controls">
          <label className="switch">
            <input type="checkbox" checked={pureVeg} onChange={(e) => setPureVeg(e.target.checked)} />
            <span />
            Pure veg only
          </label>
          <label className="switch">
            <input type="checkbox" checked={matchAll} onChange={(e) => setMatchAll(e.target.checked)} />
            <span />
            Match all selected
          </label>
          <select value={sort} onChange={(e) => setSort(e.target.value as keyof typeof SORTS)} aria-label="Sort restaurants">
            {Object.entries(SORTS).map(([k, v]) => (
              <option key={k} value={k}>{v}</option>
            ))}
          </select>
        </div>
      </section>

      {error && <p className="error-banner">{error}</p>}
      {loading && !data ? (
        <Skeleton rows={4} className="grid-skel" />
      ) : list.length === 0 ? (
        <div className="empty">
          <h2>No kitchens match those filters</h2>
          <p>Try matching any restriction instead of all, or clear a filter.</p>
          <button className="btn ghost" onClick={() => { setSelected([]); setPureVeg(false); setQuery(""); }}>Clear filters</button>
        </div>
      ) : (
        <section className="restaurant-grid">
          {list.map((r, i) => (
            <Link to={`/restaurant/${r.id}`} key={r.id} className={`rcard ${i === 0 ? "feature" : ""} ${r.isOpen ? "" : "closed"}`} style={{ animationDelay: `${i * 60}ms` }}>
              <div className="rcard-img">
                <img src={r.image} alt="" loading="lazy" />
                {r.offerText && <span className="offer">{r.offerText}</span>}
                {!r.isOpen && <span className="closed-flag">Closed now</span>}
              </div>
              <div className="rcard-body">
                <div className="rcard-head">
                  <h3>{r.name}</h3>
                  <Stars value={r.rating} />
                </div>
                <p className="muted">{r.tagline}</p>
                <div className="tags">{r.dietary.map((k) => <DietTag key={k} k={k} />)}</div>
                <div className="meta">
                  <span>{r.deliveryTimeMin} min</span>
                  <span>{r.distanceKm} km</span>
                  <span>{r.deliveryFee === 0 ? "Free delivery" : `${money(r.deliveryFee)} delivery`}</span>
                  <span>{"$".repeat(r.priceTier)}</span>
                </div>
              </div>
            </Link>
          ))}
        </section>
      )}
    </>
  );
}
