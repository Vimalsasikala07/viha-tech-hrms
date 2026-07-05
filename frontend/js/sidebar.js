// Renders the left sidebar (logo + full module navigation) into #sidebar-container.
// activePage should match the data-page attribute of the current page's link.
function renderSidebar(activePage) {
    const user = getUser() || {};
    const nav = [
        { section: "Overview", links: [
            { page: "dashboard", href: "dashboard.html", icon: "bi-speedometer2", label: "Dashboard" },
        ]},
        { section: "Core HR", links: [
            { page: "employees", href: "employees.html", icon: "bi-people", label: "Employees" },
            { page: "departments", href: "departments.html", icon: "bi-diagram-3", label: "Departments" },
            { page: "designations", href: "designations.html", icon: "bi-briefcase", label: "Designations" },
            { page: "directory", href: "directory.html", icon: "bi-person-lines-fill", label: "Employee Directory" },
        ]},
        { section: "Time & Leave", links: [
            { page: "attendance", href: "attendance.html", icon: "bi-clock-history", label: "Attendance" },
            { page: "leave", href: "leave.html", icon: "bi-calendar-check", label: "Leave Management" },
            { page: "holidays", href: "holidays.html", icon: "bi-calendar-event", label: "Holiday Calendar" },
            { page: "shifts", href: "shifts.html", icon: "bi-arrow-repeat", label: "Shift Management" },
            { page: "timesheet", href: "timesheet.html", icon: "bi-stopwatch", label: "Timesheet" },
        ]},
        { section: "Money", links: [
            { page: "payroll", href: "payroll.html", icon: "bi-cash-coin", label: "Payroll" },
            { page: "expenses", href: "expenses.html", icon: "bi-receipt", label: "Expenses" },
            { page: "assets", href: "assets.html", icon: "bi-laptop", label: "Assets" },
        ]},
        { section: "People Lifecycle", links: [
            { page: "recruitment", href: "recruitment.html", icon: "bi-person-plus", label: "Recruitment" },
            { page: "onboarding", href: "onboarding.html", icon: "bi-clipboard-check", label: "Onboarding" },
            { page: "performance", href: "performance.html", icon: "bi-graph-up", label: "Performance" },
            { page: "exit", href: "exit.html", icon: "bi-box-arrow-right", label: "Exit Management" },
        ]},
        { section: "Admin", links: [
            { page: "roles", href: "roles.html", icon: "bi-shield-lock", label: "Admin Panel" },
            { page: "announcements", href: "announcements.html", icon: "bi-megaphone", label: "Announcements" },
            { page: "notifications", href: "notifications.html", icon: "bi-bell", label: "Notifications" },
            { page: "reports", href: "reports.html", icon: "bi-bar-chart", label: "Reports & Dashboard" },
        ]},
    ];

    let html = `
      <div class="sidebar-brand">
        <img src="assets/logo.png" alt="Viha Tech">
      </div>
      <div class="sidebar-nav">`;

    nav.forEach(group => {
        html += `<div class="nav-section-label">${group.section}</div>`;
        group.links.forEach(link => {
            const activeClass = link.page === activePage ? "active" : "";
            html += `<a class="nav-link ${activeClass}" href="${link.href}"><i class="bi ${link.icon}"></i> ${link.label}</a>`;
        });
    });

    html += `</div>
      <div class="sidebar-footer">
        <div class="d-flex align-items-center gap-2 mb-2">
          <span class="avatar-sm">${initials(user.name)}</span>
          <div style="line-height:1.1;">
            <div style="font-weight:600; font-size:0.85rem;">${user.name || "User"}</div>
            <div class="text-muted" style="font-size:0.75rem;">${user.role || ""}</div>
          </div>
        </div>
        <button class="btn btn-outline-danger btn-sm w-100" onclick="handleLogout()">
          <i class="bi bi-box-arrow-left"></i> Log Out
        </button>
      </div>`;

    document.getElementById("sidebar-container").innerHTML = html;
}

async function handleLogout() {
    try { await api("/auth/logout", { method: "POST" }); } catch (e) { /* ignore */ }
    logout();
}
