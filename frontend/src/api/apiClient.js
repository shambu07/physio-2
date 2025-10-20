const API_BASE = import.meta.env.VITE_API_BASE || "http://localhost:9092";
const DEFAULT_TIMEOUT_MS = 12000;

export function getToken() {
    return localStorage.getItem("token");
}
export function setToken(t) {
    localStorage.setItem("token", t);
}
export function clearToken() {
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    localStorage.removeItem("email");
}

async function api(path, opts = {}) {
    const headers = {
        "Content-Type": "application/json",
        "Accept": "application/json, text/plain;q=0.9,*/*;q=0.8",
        ...(opts.headers || {}),
    };

    const token = getToken();
    if (token) headers.Authorization = `Bearer ${token}`;

    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort("timeout"), opts.timeout ?? DEFAULT_TIMEOUT_MS);

    let res;
    try {
        res = await fetch(`${API_BASE}${path}`, { ...opts, headers, signal: controller.signal });
    } catch (e) {
        // Network/timeout
        throw new Error(e?.message === "timeout" ? "Request timed out" : (e?.message || "Network error"));
    } finally {
        clearTimeout(timeout);
    }

    // 401 → clear and send to login, then stop
    if (res.status === 401) {
        clearToken();
        // localStorage.setItem("postLoginRedirect", location.pathname + location.search);
        window.location.href = "/login";
        return null;
    }

    // read body safely
    const contentType = res.headers.get("content-type") || "";
    const isJson = contentType.includes("application/json");

    if (!res.ok) {
        let msg = `HTTP ${res.status}`;
        try {
            const data = isJson ? await res.json() : await res.text();
            if (typeof data === "string") msg = data || msg;
            else if (data?.message) msg = data.message;
            else if (data?.error) msg = data.error;
        } catch { /* ignore parse errors */ }
        throw new Error(msg);
    }

    if (res.status === 204) return null;
    return isJson ? res.json() : res.text();
}

export const apiClient = {
    // Auth
    login: (email, password) =>
        api("/api/auth/login", { method: "POST", body: JSON.stringify({ email, password }) }),
    register: (payload) =>
        api("/api/auth/register", { method: "POST", body: JSON.stringify(payload) }),

    // Appointments
    createAppointment: (payload) =>
        api("/api/appointments", { method: "POST", body: JSON.stringify(payload) }),

    // 🔽 NEW: doctor-scoped list (backend derives doctor via JWT email)
    listMyDoctorAppointments: () => api("/api/appointments/doctor/me"),

    // existing endpoints
    listAppointments: () => api("/api/appointments"),
    listByPatientId: (id) => api(`/api/appointments/patient/${id}`),
    reschedule: (id, startTime, endTime) =>
        api(`/api/appointments/${id}/time`, {
            method: "PATCH",
            body: JSON.stringify({ startTime, endTime }),
        }),
    changeStatus: (id, status) =>
        api(`/api/appointments/${id}/status`, {
            method: "PATCH",
            body: JSON.stringify({ status }),
        }),

    // Ops
    health: () => api("/actuator/health", { method: "GET" }),
};
