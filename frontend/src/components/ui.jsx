// src/components/ui.jsx
import React from "react";   // <- add this line

export function Field({ label, children, className = "" }) {
    return (
        <div className={className}>
            {label && <div className="label">{label}</div>}
            {children}
        </div>
    );
}

export const Input = (props) => <input className="input" {...props} />;
export const Select = (props) => <select className="select" {...props} />;
export const Button = ({ children, ...rest }) => (
    <button className="btn" {...rest}>{children}</button>
);

export function StatusBadge({ status }) {
    const s = (status || "").toUpperCase();
    return <span className={`badge ${s}`}>{s || "—"}</span>;
}
