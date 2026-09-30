export async function api<T>(path: string, init?: RequestInit & { body?: any }): Promise<T> {
  const opts: RequestInit = { ...init };
  if (init?.body !== undefined && typeof init.body !== "string") {
    opts.body = JSON.stringify(init.body);
    opts.headers = { "Content-Type": "application/json", ...init.headers };
  }
  const res = await fetch(path, opts);
  if (res.status === 204) return undefined as T;
  const data = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(data.error || `Request failed (${res.status})`);
  return data as T;
}

export const money = (n: number) => `$${n.toFixed(2)}`;

export const timeAgo = (iso: string) => {
  const mins = Math.round((Date.now() - new Date(iso).getTime()) / 60000);
  if (mins < 1) return "just now";
  if (mins < 60) return `${mins} min ago`;
  const hrs = Math.round(mins / 60);
  if (hrs < 24) return `${hrs} h ago`;
  return `${Math.round(hrs / 24)} d ago`;
};
