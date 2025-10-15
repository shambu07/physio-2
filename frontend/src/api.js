const API = import.meta.env.VITE_API_BASE || "http://localhost:9092";

export async function api(path, opts = {}) {
    const res = await fetch(API + path, {
        headers: { "Content-Type": "application/json", ...(opts.headers || {}) },
        ...opts,
    });
    if (!res.ok) {
        const text = await res.text();
        throw new Error(text || res.statusText);
    }
    return res.status === 204 ? null : res.json();
}

export const Doctors = {
    list: () => api("/api/doctors"),
};

export const Patients = {
    list: () => api("/api/patients"),
};

export const Availability = {
    day: (doctorId, date) => api(`/api/availability/${doctorId}?date=${date}`),
    upsert: (body) => api("/api/availability", { method: "POST", body: JSON.stringify(body) }),
};

export const Appointments = {
    me: () => api("/api/appointments/me"),
    doctorDay: (id, date) => api(`/api/appointments/doctor/${id}?date=${date}`),
    doctorWeek: (id, start) => api(`/api/appointments/doctor/${id}/week?start=${start}`),
    book: (body) => api("/api/appointments", { method: "POST", body: JSON.stringify(body) }),
    setStatus: (id, status) => api(`/api/appointments/${id}/status`, { method: "PATCH", body: JSON.stringify({ status }) }),
};
