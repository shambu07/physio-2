export function saveLogin(data) {
    // expected: { token, fullName, email, roles: [...] }
    if (data?.token) localStorage.setItem("token", data.token);
    if (data?.email) localStorage.setItem("email", data.email);
    if (Array.isArray(data?.roles) && data.roles.length) {
        localStorage.setItem("role", data.roles[0]); // PATIENT / DOCTOR
    }
}
export function isAuthed() {
    return !!localStorage.getItem("token");
}
export function role() {
    return localStorage.getItem("role") || "";
}
