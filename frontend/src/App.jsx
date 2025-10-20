// src/App.jsx
import React, { useEffect, useMemo, useState } from "react";
import PatientFindSlots from "./pages/PatientFindSlots.jsx";
import PatientMyAppointments from "./pages/PatientMyAppointments.jsx";
import DoctorSchedule from "./pages/DoctorSchedule.jsx";
import ManageAvailability from "./pages/ManageAvailability.jsx";

// If you already have these helpers in utils/auth, import them instead.
function getToken() { return localStorage.getItem("token"); }
function getRoles() {
    try { return JSON.parse(localStorage.getItem("roles") || "[]"); }
    catch { return []; }
}
function getEmail() { return localStorage.getItem("email") || ""; }
function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("roles");
    localStorage.removeItem("email");
    localStorage.removeItem("activeTab");
    window.location.href = "/login";
}

const TabBtn = ({ label, active, onClick }) => (
    <button
        onClick={onClick}
        style={{
            marginRight: 8,
            padding: "8px 12px",
            fontWeight: active ? 700 : 500,
            borderRadius: 8,
            border: active ? "1px solid #2b6cb0" : "1px solid #ddd",
            background: active ? "#e8f1fd" : "#fff",
            cursor: "pointer",
        }}
    >
        {label}
    </button>
);

export default function App() {
    const token = getToken();
    const roles = getRoles(); // e.g., ["PATIENT"] or ["DOCTOR"]
    const primaryRole = roles[0] || "PATIENT";
    const email = getEmail();

    // Guard: not logged in → send to /login
    useEffect(() => {
        if (!token) window.location.href = "/login";
    }, [token]);

    // Available tabs by role
    const tabs = useMemo(() => {
        const patientTabs = [
            { key: "patient-find", label: "Patient: Find Slots", node: <PatientFindSlots /> },
            { key: "patient-my", label: "Patient: My Appointments", node: <PatientMyAppointments /> },
        ];
        const doctorTabs = [
            { key: "doctor-schedule", label: "Doctor: Schedule", node: <DoctorSchedule /> },
            { key: "manage-availability", label: "Doctor: Manage Availability", node: <ManageAvailability /> },
        ];
        return primaryRole === "DOCTOR" ? doctorTabs.concat(patientTabs) : patientTabs;
    }, [primaryRole]);

    // Restore last tab (and ensure it still exists for this role)
    const defaultKey = primaryRole === "DOCTOR" ? "doctor-schedule" : "patient-find";
    const [tab, setTab] = useState(() => {
        const saved = localStorage.getItem("activeTab");
        return tabs.find(t => t.key === saved)?.key || defaultKey;
    });

    useEffect(() => {
        // If role changed, ensure the tab is valid
        if (!tabs.find(t => t.key === tab)) setTab(defaultKey);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [tabs.length, primaryRole]);

    useEffect(() => {
        localStorage.setItem("activeTab", tab);
    }, [tab]);

    const active = tabs.find(t => t.key === tab);

    if (!token) return null; // brief blank before redirect

    return (
        <div style={{ maxWidth: 1000, margin: "20px auto", fontFamily: "system-ui, sans-serif" }}>
            {/* Header */}
            <header style={{
                display: "flex",
                alignItems: "center",
                justifyContent: "space-between",
                marginBottom: 16
            }}>
                <h1 style={{ margin: 0 }}>Physio Clinic</h1>
                <div style={{ display: "flex", gap: 12, alignItems: "center" }}>
          <span style={{ fontSize: 14, color: "#444" }}>
            {primaryRole} {email ? `· ${email}` : ""}
          </span>
                    <button onClick={logout} style={{
                        padding: "6px 10px",
                        borderRadius: 8,
                        border: "1px solid #ddd",
                        background: "#fff",
                        cursor: "pointer"
                    }}>
                        Logout
                    </button>
                </div>
            </header>

            {/* Tabs */}
            <nav style={{ marginBottom: 14 }}>
                {tabs.map(t => (
                    <TabBtn key={t.key} label={t.label} active={t.key === tab} onClick={() => setTab(t.key)} />
                ))}
            </nav>

            {/* Active tab content */}
            <main style={{ padding: 16, border: "1px solid #eee", borderRadius: 12, background: "#fff" }}>
                {active?.node ?? <div>Tab not found</div>}
            </main>
        </div>
    );
}
