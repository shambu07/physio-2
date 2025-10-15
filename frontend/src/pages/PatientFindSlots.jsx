import React, { useEffect, useState } from "react";
import { Availability, Appointments, Doctors } from "../api.js";

export default function PatientFindSlots() {
    const [doctors, setDoctors] = useState([]);
    const [doctorId, setDoctorId] = useState(1);
    const [date, setDate] = useState(new Date().toISOString().slice(0,10));
    const [slots, setSlots] = useState([]);
    const [loading, setLoading] = useState(false);
    const [msg, setMsg] = useState("");

    useEffect(() => { Doctors.list().then(setDoctors).catch(e=>setMsg(e.message)); }, []);

    const search = async () => {
        setMsg(""); setLoading(true);
        try {
            const res = await Availability.day(doctorId, date);
            setSlots(res.freeSlots);
        } catch (e) { setMsg(e.message); }
        finally { setLoading(false); }
    };

    const book = async (slot) => {
        setMsg("");
        try {
            await Appointments.book({
                patientId: 1, doctorId,
                start: slot.start, end: slot.end,
                type: "CONSULTATION", notes: ""
            });
            setMsg("Booked!");
            await search();
        } catch (e) { setMsg(e.message); }
    };

    return (
        <div>
            <h2>Find slots</h2>
            <div style={{display:"flex", gap:8, alignItems:"center"}}>
                <select value={doctorId} onChange={e=>setDoctorId(Number(e.target.value))}>
                    {doctors.map(d=><option key={d.id} value={d.id}>{d.name}</option>)}
                </select>
                <input type="date" value={date} onChange={e=>setDate(e.target.value)} />
                <button onClick={search} disabled={loading}>{loading?"Loading...":"Search"}</button>
            </div>
            {msg && <p style={{color:"crimson"}}>{msg}</p>}
            <ul>
                {slots.map((s,i)=>(
                    <li key={i} style={{margin:"8px 0"}}>
                        {new Date(s.start).toLocaleTimeString([], {hour:"2-digit", minute:"2-digit"})}
                        {" - "}
                        {new Date(s.end).toLocaleTimeString([], {hour:"2-digit", minute:"2-digit"})}
                        {" "}
                        <button onClick={()=>book(s)}>Book</button>
                    </li>
                ))}
                {slots.length===0 && <p>No free slots.</p>}
            </ul>
        </div>
    );
}
