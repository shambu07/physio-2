import React, { useState } from "react";
import PatientFindSlots from "./pages/PatientFindSlots.jsx";
import PatientMyAppointments from "./pages/PatientMyAppointments.jsx";
import DoctorSchedule from "./pages/DoctorSchedule.jsx";
import ManageAvailability from "./pages/ManageAvailability.jsx";

const Tab = ({label, active, onClick}) => (
    <button style={{marginRight:8, padding:"6px 10px", fontWeight:active?"700":"400"}} onClick={onClick}>
        {label}
    </button>
);

export default function App() {
    const [tab, setTab] = useState("patient-find");
    return (
        <div style={{maxWidth: 900, margin:"20px auto", fontFamily:"system-ui, sans-serif"}}>
            <h1>Physio Clinic</h1>
            <div style={{marginBottom:12}}>
                <Tab label="Patient: Find Slots" active={tab==="patient-find"} onClick={()=>setTab("patient-find")} />
                <Tab label="Patient: My Appointments" active={tab==="patient-my"} onClick={()=>setTab("patient-my")} />
                <Tab label="Doctor: Schedule" active={tab==="doctor-schedule"} onClick={()=>setTab("doctor-schedule")} />
                <Tab label="Doctor: Manage Availability" active={tab==="manage-availability"} onClick={()=>setTab("manage-availability")} />
            </div>
            {tab==="patient-find" && <PatientFindSlots/>}
            {tab==="patient-my" && <PatientMyAppointments/>}
            {tab==="doctor-schedule" && <DoctorSchedule/>}
            {tab==="manage-availability" && <ManageAvailability/>}
        </div>
    );
}
