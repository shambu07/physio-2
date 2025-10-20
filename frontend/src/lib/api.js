const API_BASE = import.meta.env.VITE_API_BASE || "http://localhost:9092";

export function setToken(t){ localStorage.setItem("token", t); }
export function getToken(){ return localStorage.getItem("token"); }
export async function api(path, opts = {}) {
    const res = await fetch(`${API_BASE}${path}`, {
        headers: {
            "Content-Type": "application/json",
            ...(getToken() ? { Authorization: `Bearer ${getToken()}` } : {}),
            ...(opts.headers||{})
        },
        ...opts,
    });
    if (!res.ok) throw new Error(await res.text());
    return res.status === 204 ? null : res.json();
}
