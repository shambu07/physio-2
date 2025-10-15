import React, { useState } from "react";
import { Availability } from "../api.js";

export default function ManageAvailability() {
    const [dayOfWeek, setDay] = useState(1);
    const [startTime, setStart] = useState("09:00");
    const [endTime, setEnd] = useState("17:00");
    const [slotMinutes, setSlot] = useState(30);
    const [breakStart, setBreakStart] = useState("12:00");
    const [breakEnd, setBreakEnd] = useState("13:00");
    const [msg, setMsg] = useState("");

    const save = async () => {
        setMsg("");
        try {
            await Availability.upsert({
                doctorId: 1, dayOfWeek: Number(dayOfWeek),
                startTime, endTime, slotMinutes: Number(slotMinutes),
                breaks: [{ startTime: breakStart, endTime: breakEnd }]
            });
            setMsg("Saved!");
        } catch (e) { setMsg(e.message); }
    };

    return (
        <div>
            <h2>Manage Availability</h2>
            <div style={{display:"grid", gridTemplateColumns:"140px 1fr", gap:8, maxWidth:480}}>
                <label>Day of week</label>
                <select value={dayOfWeek} onChange={e=>setDay(e.target.value)}>
                    <option value={1}>Mon</option><option value={2}>Tue</option><option value={3}>Wed</option>
                    <option value={4}>Thu</option><option value={5}>Fri</option><option value={6}>Sat</option><option value={7}>Sun</option>
                </select>

                <label>Start</label><input type="time" value={startTime} onChange={e=>setStart(e.target.value)} />
                <label>End</label><input type="time" value={endTime} onChange={e=>setEnd(e.target.value)} />
                <label>Slot (min)</label><input type="number" value={slotMinutes} onChange={e=>setSlot(e.target.value)} />
                <label>Break start</label><input type="time" value={breakStart} onChange={e=>setBreakStart(e.target.value)} />
                <label>Break end</label><input type="time" value={breakEnd} onChange={e=>setBreakEnd(e.target.value)} />
            </div>
            <button style={{marginTop:10}} onClick={save}>Save</button>
            {msg && <p style={{color: msg==="Saved!" ? "green":"crimson"}}>{msg}</p>}
        </div>
    );
}
