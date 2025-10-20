import React, { useState } from "react";
import { apiClient } from "../api/apiClient";

export default function Register() {
    const [fullName, setFullName] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [phone, setPhone] = useState("");
    const [role, setRole] = useState("PATIENT");
    const [msg, setMsg] = useState("");
    const [err, setErr] = useState("");

    async function onSubmit(e) {
        e.preventDefault();
        setMsg("");
        setErr("");
        try {
            await apiClient.register({ fullName, email, password, role, phone });
            setMsg("Registered! Now you can login.");
        } catch (ex) {
            setErr(ex.message);
        }
    }

    return (
        <div style={{ maxWidth: 520, margin: "64px auto" }}>
            <h2>Register</h2>
            <form onSubmit={onSubmit}>
                <label>Full Name</label>
                <input value={fullName} onChange={(e) => setFullName(e.target.value)} required />
                <label>Email</label>
                <input value={email} onChange={(e) => setEmail(e.target.value)} required />
                <label>Password</label>
                <input
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                />
                <label>Phone</label>
                <input value={phone} onChange={(e) => setPhone(e.target.value)} />
                <label>Role</label>
                <select value={role} onChange={(e) => setRole(e.target.value)}>
                    <option value="PATIENT">PATIENT</option>
                    <option value="DOCTOR">DOCTOR</option>
                </select>
                <button>Register</button>
            </form>
            {msg && <p style={{ color: "green" }}>{msg}</p>}
            {err && <p style={{ color: "red" }}>{err}</p>}
            <p>Have an account? <a href="/login">Login</a></p>
        </div>
    );
}
