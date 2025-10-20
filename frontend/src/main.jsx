// src/main.jsx
import React from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Login from "./pages/Login.jsx";
import Register from "./pages/Register.jsx";
import BookAppointment from "./pages/BookAppointment.jsx";
import MyAppointments from "./pages/MyAppointments.jsx";
import DoctorDashboard from "./pages/DoctorDashboard.jsx";
import ProtectedRoute from "./components/ProtectedRoute.jsx";
import "./styles.css"; // global styles

function App() {
    return (
        <Routes>
            <Route path="/" element={<Navigate to="/login" replace />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route
                path="/book"
                element={
                    <ProtectedRoute requireRole="PATIENT">
                        <BookAppointment />
                    </ProtectedRoute>
                }
            />
            <Route
                path="/mine"
                element={
                    <ProtectedRoute requireRole="PATIENT">
                        <MyAppointments />
                    </ProtectedRoute>
                }
            />
            <Route
                path="/doctor"
                element={
                    <ProtectedRoute requireRole="DOCTOR">
                        <DoctorDashboard />
                    </ProtectedRoute>
                }
            />
        </Routes>
    );
}

ReactDOM.createRoot(document.getElementById("root")).render(
    <React.StrictMode>
        <BrowserRouter>
            <App />
        </BrowserRouter>
    </React.StrictMode>
);
