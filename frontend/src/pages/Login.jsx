import React, { useState } from "react";
import { apiClient } from "../api/apiClient";
import { saveLogin } from "../utils/auth";

export default function Login() {
    const [email, setEmail] = useState("alice@x125.com");
    const [password, setPassword] = useState("pass1234");
    const [loading, setLoading] = useState(false);
    const [err, setErr] = useState("");

    async function onSubmit(e) {
        e.preventDefault();
        setLoading(true);
        setErr("");
        try {
            const data = await apiClient.login(email, password);
            saveLogin(data);
            const firstRole = (data.roles && data.roles[0]) || "PATIENT";
            window.location.href = firstRole === "DOCTOR" ? "/doctor" : "/book";
        } catch (ex) {
            setErr(ex.message);
        } finally {
            setLoading(false);
        }
    }

    return (
        <div style={{ maxWidth: 420, margin: "64px auto" }}>
            <h2>Login</h2>
            <form onSubmit={onSubmit}>
                <label>Email</label>
                <input value={email} onChange={(e) => setEmail(e.target.value)} required />
                <label>Password</label>
                <input
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                />
                <button disabled={loading}>{loading ? "Signing in..." : "Login"}</button>
                {err && <p style={{ color: "red" }}>{err}</p>}
            </form>
            <p>New user? <a href="/register">Register</a></p>
        </div>
    );
}
