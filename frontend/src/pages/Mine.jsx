// src/pages/Mine.jsx
import { useState } from "react";
import { Field, Input, Button, StatusBadge } from "../components/ui";

const API_BASE = import.meta.env.VITE_API_BASE || "http://localhost:9092";

// --- Helpers ---------------------------------------------------------------
function toIsoSeconds(v) {
    if (!v) return v;                 // "YYYY-MM-DDTHH:MM"
    return v.length === 16 ? v + ":00" : v; // -> "YYYY-MM-DDTHH:MM:SS"
}
function fromIsoToLocalMinutes(s) {
    // "2025-10-19T10:00:00" -> "2025-10-19T10:00" (for <input type="datetime-local">)
    if (!s) return "";
    return s.slice(0, 16);
}
async function fetchByPatient(id) {
    const r = await fetch(`${API_BASE}/api/appointments/patient/${id}`);
    if (!r.ok) throw new Error(`HTTP ${r.status}`);
    return r.json();
}
async function patchTime(id, startTime, endTime) {
    const r = await fetch(`${API_BASE}/api/appointments/${id}/time`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ startTime, endTime }),
    });
    if (!r.ok) throw new Error(`HTTP ${r.status}`);
    return r.json();
}
async function patchStatus(id, status) {
    const r = await fetch(`${API_BASE}/api/appointments/${id}/status`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ status }),
    });
    if (!r.ok) throw new Error(`HTTP ${r.status}`);
    return r.json();
}
// --------------------------------------------------------------------------

export default function Mine() {
    const [pid, setPid] = useState("1");
    const [rows, setRows] = useState([]);
    const [loading, setLoading] = useState(false);
    const [listErr, setListErr] = useState("");

    // Inline edit state
    const [editingId, setEditingId] = useState(null);
    const [editStart, setEditStart] = useState("");
    const [editEnd, setEditEnd] = useState("");
    const [rowBusy, setRowBusy] = useState(null); // id that is saving/cancelling
    const [rowErr, setRowErr] = useState("");     // inline error

    async function load() {
        setListErr(""); setLoading(true);
        try {
            const data = await fetchByPatient(Number(pid));
            setRows(Array.isArray(data) ? data : []);
        } catch (ex) {
            setListErr(ex.message || "Failed to load appointments.");
        } finally {
            setLoading(false);
        }
    }

    function startEdit(row) {
        setRowErr("");
        setEditingId(row.id);
        setEditStart(fromIsoToLocalMinutes(row.startTime));
        setEditEnd(fromIsoToLocalMinutes(row.endTime));
    }

    function cancelEdit() {
        setEditingId(null);
        setEditStart("");
        setEditEnd("");
        setRowErr("");
    }

    async function saveEdit(id) {
        setRowErr("");
        // Validate
        const s = new Date(editStart);
        const e = new Date(editEnd);
        if (isNaN(s.getTime()) || isNaN(e.getTime())) {
            setRowErr("Please provide valid date/times.");
            return;
        }
        if (s >= e) {
            setRowErr("End time must be after start time.");
            return;
        }

        setRowBusy(id);
        try {
            await patchTime(id, toIsoSeconds(editStart), toIsoSeconds(editEnd));
            await load();
            cancelEdit();
        } catch (ex) {
            setRowErr(ex.message || "Failed to reschedule.");
        } finally {
            setRowBusy(null);
        }
    }

    async function cancelAppointment(id) {
        if (!confirm("Cancel this appointment?")) return;
        setRowBusy(id);
        setRowErr("");
        try {
            await patchStatus(id, "CANCELLED");
            await load();
        } catch (ex) {
            setRowErr(ex.message || "Failed to cancel appointment.");
        } finally {
            setRowBusy(null);
        }
    }

    function actionsDisabled(status) {
        const s = (status || "").toUpperCase();
        return s === "COMPLETED" || s === "CANCELLED";
    }

    return (
        <div className="container">
            <h1 className="h1">My Appointments</h1>

            <div className="card" style={{ marginBottom: 14 }}>
                <div style={{ display: "flex", gap: 12, alignItems: "end" }}>
                    <Field label="Patient ID" style={{ flex: "0 0 160px" }}>
                        <Input
                            type="number"
                            min="1"
                            value={pid}
                            onChange={(e) => setPid(e.target.value)}
                            inputMode="numeric"
                        />
                    </Field>
                    <Button onClick={load} disabled={loading}>
                        {loading ? "Loading..." : "Load"}
                    </Button>
                </div>
                {listErr && (
                    <div style={{ marginTop: 10, color: "var(--danger)" }}>❌ {listErr}</div>
                )}
            </div>

            <div className="table-wrap card">
                <table className="table">
                    <thead>
                    <tr>
                        <th style={{ width: 80 }}>ID</th>
                        <th>Start</th>
                        <th>End</th>
                        <th style={{ width: 160 }}>Status</th>
                        <th style={{ width: 280 }}>Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    {rows.length === 0 ? (
                        <tr>
                            <td colSpan="5" style={{ textAlign: "center", color: "var(--muted)" }}>
                                No appointments to show.
                            </td>
                        </tr>
                    ) : (
                        rows.map((r) => {
                            const disabled = actionsDisabled(r.status);
                            const isEditing = editingId === r.id;

                            return (
                                <tr key={r.id}>
                                    <td>{r.id}</td>

                                    {/* Start */}
                                    <td>
                                        {isEditing ? (
                                            <Input
                                                type="datetime-local"
                                                value={editStart}
                                                onChange={(e) => setEditStart(e.target.value)}
                                            />
                                        ) : (
                                            r.startTime?.replace("T", " ")
                                        )}
                                    </td>

                                    {/* End */}
                                    <td>
                                        {isEditing ? (
                                            <Input
                                                type="datetime-local"
                                                value={editEnd}
                                                onChange={(e) => setEditEnd(e.target.value)}
                                                min={editStart || fromIsoToLocalMinutes(r.startTime)}
                                            />
                                        ) : (
                                            r.endTime?.replace("T", " ")
                                        )}
                                    </td>

                                    <td>
                                        <StatusBadge status={r.status} />
                                    </td>

                                    <td>
                                        {isEditing ? (
                                            <div style={{ display: "flex", gap: 8 }}>
                                                <Button
                                                    onClick={() => saveEdit(r.id)}
                                                    disabled={rowBusy === r.id}
                                                >
                                                    {rowBusy === r.id ? "Saving..." : "Save"}
                                                </Button>
                                                <Button onClick={cancelEdit} disabled={rowBusy === r.id}>
                                                    Cancel
                                                </Button>
                                            </div>
                                        ) : (
                                            <div style={{ display: "flex", gap: 8 }}>
                                                <Button
                                                    onClick={() => startEdit(r)}
                                                    disabled={disabled || rowBusy === r.id}
                                                >
                                                    Reschedule
                                                </Button>
                                                <Button
                                                    onClick={() => cancelAppointment(r.id)}
                                                    disabled={disabled || rowBusy === r.id}
                                                >
                                                    Cancel
                                                </Button>
                                            </div>
                                        )}
                                    </td>
                                </tr>
                            );
                        })
                    )}
                    </tbody>
                </table>
            </div>

            {rowErr && (
                <div style={{ marginTop: 10, color: "var(--danger)" }}>❌ {rowErr}</div>
            )}

            <div style={{ marginTop: 18 }}>
                <a href="/book">← Book another appointment</a>
            </div>
        </div>
    );
}
