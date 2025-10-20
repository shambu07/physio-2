import React from "react";
import { Navigate } from "react-router-dom";
import { isAuthed, role as currentRole } from "../utils/auth";

export default function ProtectedRoute({ children, requireRole }) {
    if (!isAuthed()) return <Navigate to="/login" replace />;
    if (requireRole && currentRole() !== requireRole) return <Navigate to="/" replace />;
    return children;
}
