import { NavLink, Route, Routes, Link } from "react-router-dom";
import { useCart } from "./cart";
import Home from "./pages/Home";

export default function App() {
  const { count } = useCart();
  return (
    <div className="shell">
      <header className="topbar">
        <Link to="/" className="brand">
          <img src="/images/icon.jpg" alt="" />
          <span>
            Eat<em>Fine</em>
          </span>
        </Link>
        <nav className="mainnav">
          <NavLink to="/" end>Discover</NavLink>
          <NavLink to="/orders">My orders</NavLink>
          <span className="nav-sep" aria-hidden />
          <NavLink to="/inventory">Inventory</NavLink>
          <NavLink to="/kitchen">Kitchen</NavLink>
          <NavLink to="/admin">Admin</NavLink>
        </nav>
        <Link to="/cart" className="cart-btn" aria-label={`Cart, ${count} items`}>
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4Z" />
            <path d="M3 6h18M16 10a4 4 0 0 1-8 0" />
          </svg>
          Cart
          {count > 0 && <b>{count}</b>}
        </Link>
      </header>
      <main>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="*" element={<div className="empty"><h2>Page not found</h2><Link to="/">Back to restaurants</Link></div>} />
        </Routes>
      </main>
      <footer className="footer">
        <span>EatFine · Good Food. Great Moments.</span>
        <span>Dietary-first ordering with kitchen inventory built in</span>
      </footer>
    </div>
  );
}
