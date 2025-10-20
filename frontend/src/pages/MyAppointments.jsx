import React, { useEffect, useState } from "react";
import { apiClient } from "../api/apiClient";

export default function MyAppointments() {
    const [patientId, setPatientId] = useState(1);
    const [items, setItems] = useState([]);
    const [err, setErr] = useState("");

    async function load() {
        setErr("");
        try {
            const data = await apiClient.listByPatientId(patientId);
            setItems(data || []);
        } catch (ex) {
            setErr(ex.message);
        }
    }

    useEffect(() => {
        load();
    }, []);

    return (
        <div style={{ maxWidth: 800, margin: "64px auto" }}>
            <h2>My Appointments</h2>
            <div style={{ marginBottom: 16 }}>
                <label>Patient ID</label>
                <input
                    type="number"
                    value={patientId}
                    onChange={(e) => setPatientId(parseInt(e.target.value || "0"))}
                />
                <button onClick={load}>Load</button>
            </div>
            {err && <p style={{ color: "red" }}>{err}</p>}
            <table border="1" cellPadding="8">
                <thead>
                <tr>
                    <th>ID</th><th>Start</th><th>End</th><th>Status</th>
                </tr>
                </thead>
                <tbody>
                {(items || []).map((a) => (
                    <tr key={a.id}>
                        <td>{a.id}</td>
                        <td>{a.startTime || a.start_time}</td>
                        <td>{a.endTime || a.end_time}</td>
                        <td>{a.status}</td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}
