// src/pages/DoctorDashboard.jsx
import React, { useEffect, useMemo, useState } from "react";
import { apiClient } from "../api/apiClient";
import { Button, StatusBadge, Input, Select } from "../components/ui";

// Parse "YYYY-MM-DDTHH:MM:SS" as local time reliably
function toLocalDate(dtStr) {
    if (!dtStr) return null;
    try {
        const [d, t = "00:00:00"] = dtStr.split("T");
        const [y, m, day] = d.split("-").map(Number);
        const [hh, mm, ss] = t.split(":").map(Number);
        return new Date(y, (m || 1) - 1, day, hh || 0, mm || 0, ss || 0);
    } catch {
        return null;
    }
}
const toLocalMinutes = (s) => (s ? s.slice(0, 16) : "");
const toIsoSeconds = (v) => (!v ? v : v.length === 16 ? v + ":00" : v);

export default function DoctorDashboard() {
    const [rows, setRows] = useState([]);
    const [err, setErr] = useState("");
    const [busy, setBusy] = useState(false);

    // inline edit
    const [editId, setEditId] = useState(null);
    const [editStart, setEditStart] = useState("");
    const [editEnd, setEditEnd] = useState("");

    // 🔍 filters
    const [statusFilter, setStatusFilter] = useState("ALL"); // ALL | SCHEDULED | COMPLETED | CANCELLED
    const [fromDate, setFromDate] = useState("");            // "YYYY-MM-DD"
    const [toDate, setToDate] = useState("");

    async function load() {
        setErr("");
        try {
            // ⬇️ doctor-scoped endpoint (requires apiClient.listMyDoctorAppointments)
            const data = await apiClient.listMyDoctorAppointments();
            setRows(Array.isArray(data) ? data : []);
        } catch (ex) {
            setErr(ex.message || "Failed to load appointments");
        }
    }
    useEffect(() => { load(); }, []);

    const filteredSorted = useMemo(() => {
        const from = fromDate ? new Date(`${fromDate}T00:00:00`) : null;
        const to = toDate ? new Date(`${toDate}T23:59:59`) : null;

        return [...rows]
            .filter((r) => {
                if (statusFilter !== "ALL" && (r.status || "").toUpperCase() !== statusFilter)
                    return false;

                const start = toLocalDate(r.startTime || r.start_time);
                if (from && start && start < from) return false;
                if (to && start && start > to) return false;

                return true;
            })
            .sort((a, b) => {
                const ax = a.startTime || a.start_time || "";
                const bx = b.startTime || b.start_time || "";
                return ax.localeCompare(bx);
            });
    }, [rows, statusFilter, fromDate, toDate]);

    const counts = useMemo(() => {
        const c = { SCHEDULED: 0, COMPLETED: 0, CANCELLED: 0 };
        rows.forEach((r) => { const s = (r.status || "").toUpperCase(); if (c[s] !== undefined) c[s]++; });
        return c;
    }, [rows]);

    function actionsDisabled(status) {
        const s = (status || "").toUpperCase();
        return s === "COMPLETED" || s === "CANCELLED";
    }

    async function changeStatus(id, status) {
        setBusy(true); setErr("");
        try {
            await apiClient.changeStatus(id, status);
            await load();
        } catch (ex) {
            setErr(ex.message || "Failed to update status");
        } finally { setBusy(false); }
    }

    function startEdit(row) {
        setEditId(row.id);
        setEditStart(toLocalMinutes(row.startTime || row.start_time || ""));
        setEditEnd(toLocalMinutes(row.endTime || row.end_time || ""));
    }
    function cancelEdit() { setEditId(null); setEditStart(""); setEditEnd(""); }

    async function commitReschedule() {
        if (!editId) return;
        if (!editStart || !editEnd) return setErr("Provide both start and end times.");

        const s = new Date(editStart), e = new Date(editEnd);
        if (isNaN(s) || isNaN(e) || s >= e) return setErr("End time must be after start time.");

        setBusy(true); setErr("");
        try {
            await apiClient.reschedule(Number(editId), toIsoSeconds(editStart), toIsoSeconds(editEnd));
            cancelEdit();
            await load();
        } catch (ex) {
            setErr(ex.message || "Failed to reschedule");
        } finally { setBusy(false); }
    }

    // overdue if scheduled and end < now
    function isOverdue(r) {
        const end = toLocalDate(r.endTime || r.end_time);
        return (r.status || "").toUpperCase() === "SCHEDULED" && end && end < new Date();
    }

    return (
        <div className="container">
            <h1 className="h1">Doctor Dashboard (My Appointments)</h1>

            {/* Controls */}
            <div className="card" style={{ marginBottom: 14, display: "grid", gap: 12, gridTemplateColumns: "1fr 1fr 1fr auto" }}>
                <div>
                    <div className="label">Status</div>
                    <Select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
                        <option value="ALL">ALL ({rows.length})</option>
                        <option value="SCHEDULED">SCHEDULED ({counts.SCHEDULED})</option>
                        <option value="COMPLETED">COMPLETED ({counts.COMPLETED})</option>
                        <option value="CANCELLED">CANCELLED ({counts.CANCELLED})</option>
                    </Select>
                </div>
                <div>
                    <div className="label">From</div>
                    <Input type="date" value={fromDate} onChange={(e) => setFromDate(e.target.value)} />
                </div>
                <div>
                    <div className="label">To</div>
                    <Input type="date" value={toDate} min={fromDate || undefined} onChange={(e) => setToDate(e.target.value)} />
                </div>
                <div style={{ alignSelf: "end", display: "flex", gap: 8, justifyContent: "flex-end" }}>
                    <Button onClick={() => { setStatusFilter("ALL"); setFromDate(""); setToDate(""); }}>Clear</Button>
                    <Button onClick={load} disabled={busy}>{busy ? "Refreshing..." : "Refresh"}</Button>
                </div>
                {err && <div style={{ gridColumn: "1 / -1", color: "var(--danger)" }}>❌ {err}</div>}
            </div>

            {/* Table */}
            <div className="table-wrap card">
                <table className="table">
                    <thead>
                    <tr>
                        <th>ID</th><th>Patient</th><th>Doctor</th>
                        <th>Start</th><th>End</th><th>Type</th><th>Status</th><th>Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    {filteredSorted.length === 0 ? (
                        <tr><td colSpan="8" style={{ textAlign: "center", color: "var(--muted)" }}>No appointments found.</td></tr>
                    ) : filteredSorted.map((a) => {
                        const overdue = isOverdue(a);
                        const isEditing = editId === a.id;
                        const done = actionsDisabled(a.status);

                        const start = toLocalDate(a.startTime || a.start_time);
                        const end = toLocalDate(a.endTime || a.end_time);

                        return (
                            <React.Fragment key={a.id}>
                                <tr style={overdue ? { background: "rgba(255,176,32,.08)" } : undefined}>
                                    <td>{a.id}</td>
                                    <td>{a.patientId}</td>
                                    <td>{a.doctorId}</td>
                                    <td>{start ? start.toLocaleString() : ""}</td>
                                    <td>{end ? end.toLocaleString() : ""}</td>
                                    <td>{a.type}</td>
                                    <td><StatusBadge status={overdue ? "SCHEDULED (OVERDUE)" : a.status} /></td>
                                    <td>
                                        <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
                                            <Button disabled={busy || done} onClick={() => changeStatus(a.id, "COMPLETED")}>Complete</Button>
                                            <Button disabled={busy || done} onClick={() => changeStatus(a.id, "CANCELLED")}>Cancel</Button>
                                            <Button disabled={busy || done} onClick={() => startEdit(a)}>Reschedule</Button>
                                        </div>
                                    </td>
                                </tr>

                                {isEditing && (
                                    <tr>
                                        <td colSpan={8} style={{ background: "rgba(255,255,255,.02)" }}>
                                            <div style={{ display: "grid", gap: 10, gridTemplateColumns: "1fr 1fr auto auto", alignItems: "end" }}>
                                                <div>
                                                    <div className="label">New start</div>
                                                    <Input type="datetime-local" value={editStart} onChange={(e) => setEditStart(e.target.value)} />
                                                </div>
                                                <div>
                                                    <div className="label">New end</div>
                                                    <Input
                                                        type="datetime-local"
                                                        value={editEnd}
                                                        min={editStart || toLocalMinutes(a.startTime || a.start_time)}
                                                        onChange={(e) => setEditEnd(e.target.value)}
                                                    />
                                                </div>
                                                <Button disabled={busy} onClick={commitReschedule}>Save</Button>
                                                <Button disabled={busy} onClick={cancelEdit}>Close</Button>
                                            </div>
                                        </td>
                                    </tr>
                                )}
                            </React.Fragment>
                        );
                    })}
                    </tbody>
                </table>
            </div>
        </div>
    );
}
