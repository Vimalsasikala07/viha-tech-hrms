// ---- Central config & helpers used by every page ----
const API_BASE = "http://localhost:8080/api";

function getToken() { return localStorage.getItem("vt_token"); }
function getUser() {
    const raw = localStorage.getItem("vt_user");
    return raw ? JSON.parse(raw) : null;
}
function requireAuth() {
    if (!getToken()) window.location.href = "login.html";
}
function logout() {
    localStorage.removeItem("vt_token");
    localStorage.removeItem("vt_user");
    window.location.href = "login.html";
}

// Wrapper around fetch that adds the JWT header and handles JSON automatically.
async function api(path, { method = "GET", body, isForm = false } = {}) {
    const headers = {};
    const token = getToken();
    if (token) headers["Authorization"] = "Bearer " + token;
    if (!isForm && body) headers["Content-Type"] = "application/json";

    const res = await fetch(API_BASE + path, {
        method,
        headers,
        body: isForm ? body : (body ? JSON.stringify(body) : undefined)
    });

    if (res.status === 401) {
        logout();
        throw new Error("Session expired. Please log in again.");
    }

    let data = null;
    try { data = await res.json(); } catch (e) { /* no JSON body */ }

    if (!res.ok) {
        throw new Error((data && data.message) || "Request failed");
    }
    return data;
}

function initials(name) {
    if (!name) return "?";
    return name.split(" ").map(p => p[0]).slice(0, 2).join("").toUpperCase();
}
