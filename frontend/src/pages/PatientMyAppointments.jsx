import React, { useEffect, useState } from "react";
import { Appointments } from "../api.js";

export default function PatientMyAppointments() {
    const [data, setData] = useState([]);
    const [msg, setMsg] = useState("");

    useEffect(()=>{ Appointments.me().then(setData).catch(e=>setMsg(e.message)); }, []);

    return (
        <div>
            <h2>My Appointments</h2>
            {msg && <p style={{color:"crimson"}}>{msg}</p>}
            <ul>
                {data.map(a=>(
                    <li key={a.id}>
                        #{a.id} — {new Date(a.start).toLocaleString()} → {a.status}
                    </li>
                ))}
                {data.length===0 && <p>No appointments yet.</p>}
            </ul>
        </div>
    );
}
