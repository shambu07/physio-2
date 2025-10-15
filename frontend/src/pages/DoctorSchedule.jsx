import React, { useEffect, useState } from "react";
import { Appointments } from "../api.js";

export default function DoctorSchedule() {
    const [doctorId, setDoctorId] = useState(1);
    const [date, setDate] = useState(new Date().toISOString().slice(0,10));
    const [view, setView] = useState("day");
    const [items, setItems] = useState([]);
    const [msg, setMsg] = useState("");

    const load = async () => {
        setMsg("");
        try {
            if (view==="day") {
                const r = await Appointments.doctorDay(doctorId, date);
                setItems(r.appointments);
            } else {
                const r = await Appointments.doctorWeek(doctorId, date);
                setItems(r.appointments);
            }
        } catch (e) { setMsg(e.message); }
    };

    useEffect(()=>{ load(); }, [view, doctorId, date]);

    const setStatus = async (id, status) => {
        setMsg("");
        try {
            await Appointments.setStatus(id, status);
            await load();
        } catch (e) { setMsg(e.message); }
    };

    return (
        <div>
            <h2>Doctor Schedule</h2>
            <div style={{display:"flex", gap:8}}>
                <select value={doctorId} onChange={e=>setDoctorId(Number(e.target.value))}>
                    <option value={1}>Dr. Jane Physio</option>
                </select>
                <input type="date" value={date} onChange={e=>setDate(e.target.value)} />
                <select value={view} onChange={e=>setView(e.target.value)}>
                    <option value="day">Day</option>
                    <option value="week">Week</option>
                </select>
            </div>
            {msg && <p style={{color:"crimson"}}>{msg}</p>}
            <ul>
                {items.map(a=>(
                    <li key={a.id} style={{margin:"8px 0"}}>
                        #{a.id} — {new Date(a.start).toLocaleString()} — {a.type} — {a.status}
                        {" "}
                        <button onClick={()=>setStatus(a.id, "COMPLETED")}>Complete</button>
                        <button onClick={()=>setStatus(a.id, "CANCELLED")}>Cancel</button>
                    </li>
                ))}
                {items.length===0 && <p>No items.</p>}
            </ul>
        </div>
    );
}
