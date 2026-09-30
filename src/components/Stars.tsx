export function Stars({ value }: { value: number }) {
  return (
    <span className="stars" aria-label={`${value.toFixed(1)} out of 5`}>
      <svg width="13" height="13" viewBox="0 0 24 24" fill="currentColor" aria-hidden>
        <path d="m12 2 3.1 6.3 6.9 1-5 4.9 1.2 6.8L12 17.8 5.8 21l1.2-6.8-5-4.9 6.9-1z" />
      </svg>
      {value.toFixed(1)}
    </span>
  );
}

export function VegMark({ veg }: { veg: boolean }) {
  return <span className={`vegmark ${veg ? "veg" : "nonveg"}`} title={veg ? "Vegetarian" : "Non-vegetarian"} />;
}

export function Skeleton({ rows = 3, className = "" }: { rows?: number; className?: string }) {
  return (
    <div className={`skeleton-wrap ${className}`}>
      {Array.from({ length: rows }).map((_, i) => (
        <div key={i} className="skeleton" style={{ animationDelay: `${i * 90}ms` }} />
      ))}
    </div>
  );
}
