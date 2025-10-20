// src/pages/Book.jsx
import React, { useState } from "react";
import { apiClient } from "../api/apiClient";
import { Field, Input, Select, Button } from "../components/ui";

// Ensure "YYYY-MM-DDTHH:MM:SS"
function toIsoSeconds(v) {
    if (!v) return v;
    return v.length === 16 ? v + ":00" : v;
}

// Round up to next N-minute slot (default 30)
function nextSlot(minutes = 30) {
    const d = new Date();
    d.setSeconds(0, 0);
    const m = d.getMinutes();
    const bump = minutes - (m % minutes || minutes);
    d.setMinutes(m + bump);
    return d;
}
function toLocalInputValue(d) {
    const pad = (n) => String(n).padStart(2, "0");
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

export default function Book() {
    // Default to a future 30-min slot
    const start0 = nextSlot(30);
    const end0 = new Date(start0.getTime() + 30 * 60 * 1000);

    const [patientId, setPatientId] = useState(1);
    const [doctorId, setDoctorId] = useState(1);
    const [startTime, setStartTime] = useState(toLocalInputValue(start0)); // "YYYY-MM-DDTHH:MM"
    const [endTime, setEndTime] = useState(toLocalInputValue(end0));
    const [type, setType] = useState("CONSULTATION");
    const [notes, setNotes] = useState("");

    const [message, setMessage] = useState("");
    const [err, setErr] = useState("");
    const [saving, setSaving] = useState(false);

    async function submit(e) {
        e.preventDefault();
        setMessage("");
        setErr("");

        // Basic validation
        const s = new Date(startTime);
        const eTime = new Date(endTime);
        const now = new Date();

        if (isNaN(s.getTime()) || isNaN(eTime.getTime())) {
            setErr("Please provide valid start and end times.");
            return;
        }
        if (s >= eTime) {
            setErr("End time must be after start time.");
            return;
        }
        if (s < now) {
            setErr("Start time must be in the future.");
            return;
        }

        setSaving(true);
        try {
            await apiClient.createAppointment({
                patientId: Number(patientId),
                doctorId: Number(doctorId),
                startTime: toIsoSeconds(startTime), // "YYYY-MM-DDTHH:MM:SS"
                endTime: toIsoSeconds(endTime),
                type,                               // must match backend (enum or string)
                notes: notes || undefined,
                // status is optional; backend defaults to SCHEDULED
            });
            setMessage("✅ Appointment created successfully!");
            // setNotes(""); // optional
        } catch (ex) {
            setErr(ex.message || "Failed to create appointment.");
        } finally {
            setSaving(false);
        }
    }

    // When start changes, keep end at least start+15m (or keep user’s choice if later)
    function onStartChange(v) {
        setStartTime(v);
        const s = new Date(v);
        const e = new Date(endTime);
        if (isNaN(e.getTime()) || e <= s) {
            const minEnd = new Date(s.getTime() + 15 * 60 * 1000);
            setEndTime(toLocalInputValue(minEnd));
        }
    }

    return (
        <div className="container">
            <h1 className="h1">Book Appointment</h1>

            <div className="card">
                <form onSubmit={submit} className="form-grid" noValidate>
                    <Field label="Patient ID">
                        <Input
                            type="number"
                            min="1"
                            value={patientId}
                            onChange={(e) => setPatientId(parseInt(e.target.value || "0", 10))}
                            required
                            inputMode="numeric"
                        />
                    </Field>

                    <Field label="Doctor ID">
                        <Input
                            type="number"
                            min="1"
                            value={doctorId}
                            onChange={(e) => setDoctorId(parseInt(e.target.value || "0", 10))}
                            required
                            inputMode="numeric"
                        />
                    </Field>

                    <Field label="Start Time" className="full">
                        <Input
                            type="datetime-local"
                            value={startTime}
                            min={toLocalInputValue(new Date())} // prevent past
                            onChange={(e) => onStartChange(e.target.value)}
                            required
                        />
                    </Field>

                    <Field label="End Time" className="full">
                        <Input
                            type="datetime-local"
                            value={endTime}
                            min={startTime} // cannot be before start
                            onChange={(e) => setEndTime(e.target.value)}
                            required
                        />
                    </Field>

                    <Field label="Type">
                        <Select value={type} onChange={(e) => setType(e.target.value)}>
                            <option value="CONSULTATION">CONSULTATION</option>
                            <option value="FOLLOW_UP">FOLLOW_UP</option>
                            <option value="PHYSIOTHERAPY">PHYSIOTHERAPY</option>
                        </Select>
                    </Field>

                    <Field label="Notes (optional)" className="full">
                        <Input
                            placeholder="e.g., neck pain"
                            value={notes}
                            onChange={(e) => setNotes(e.target.value)}
                        />
                    </Field>

                    <div className="full" style={{ display: "flex", justifyContent: "flex-end", gap: 12 }}>
                        <Button disabled={saving}>{saving ? "Creating..." : "Create"}</Button>
                    </div>

                    {message && <div className="full" style={{ color: "var(--success)" }}>{message}</div>}
                    {err && <div className="full" style={{ color: "var(--danger)" }}>❌ {err}</div>}
                </form>
            </div>

            <div style={{ marginTop: 18 }}>
                <a href="/mine">See My Appointments →</a>
            </div>
        </div>
    );
}
