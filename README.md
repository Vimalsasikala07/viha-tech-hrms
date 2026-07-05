# Viha Tech HRMS

A full-stack HR Management System skeleton built with:
- **Backend:** Java 21, Spring Boot 3, Spring Security (JWT), Spring Data JPA, MySQL, Maven
- **Frontend:** HTML5, CSS3, JavaScript (ES6+), Bootstrap 5

## What's fully working out of the box

| Module | Status |
|---|---|
| Authentication (login, logout, change/forgot password, JWT) | ✅ Working |
| Employee Management (CRUD, photo & document upload, ID generation) | ✅ Working |
| Department Management (CRUD) | ✅ Working |
| Designation Management (CRUD) | ✅ Working |
| Attendance (check-in/out, late/early flags, history) | ✅ Working |
| Leave Management (apply, approve/reject, balance tracking) | ✅ Working |
| Employee Directory (searchable) | ✅ Working |

## What's scaffolded (database ready, UI placeholder)

Holiday Calendar, Payroll, Roles & Permissions UI, Recruitment, Onboarding,
Performance, Timesheet, Shift Management, Asset Management, Expense Management,
Announcements, Reports & Dashboard charts, Notifications, Exit Management.

Every one of these already has its **database table** in `database/schema.sql`.
Their sidebar pages currently show a "coming soon" placeholder. To wire one up,
copy the pattern used for Department/Designation (simplest) or Employee/Leave
(more advanced, with file upload and business logic) — a repository interface,
a controller with the CRUD endpoints, and a frontend page calling `api()`.

This was a genuinely large ask (20 modules is enterprise-scale software), so
this build gives you a working, correctly-architected core you can extend
piece by piece rather than a rushed, half-broken version of everything.

---

## 1. Database setup

1. Open MySQL Workbench (or the `mysql` CLI).
2. Run the whole `database/schema.sql` file. It will:
   - Create the `hrms_db` database
   - Create every table for all 20 modules
   - Seed 3 departments, 4 designations, and 4 roles
   - Create a default admin login:
     **Email:** `admin@vihatech.com`  **Password:** `Admin@123`

```bash
mysql -u root -p < database/schema.sql
```

## 2. Backend setup (Spring Boot)

1. Open `backend/` in IntelliJ IDEA, Eclipse, or VS Code (with the Java Extension Pack).
2. Edit `backend/src/main/resources/application.properties`:
   ```
   spring.datasource.password=YOUR_MYSQL_PASSWORD
   ```
3. Run it:
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   The API starts on **http://localhost:8080**.

4. Test it's alive:
   ```bash
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email":"admin@vihatech.com","password":"Admin@123"}'
   ```
   You should get back a JWT token.

## 3. Frontend setup

The frontend is plain HTML/CSS/JS — no build step needed.

1. Open `frontend/` in VS Code.
2. Install the **Live Server** extension (or any static file server).
3. Right-click `login.html` → "Open with Live Server".
4. Log in with `admin@vihatech.com` / `Admin@123`.

If your Live Server runs on a different port than `5500`, add it to
`app.cors.allowed-origins` in `application.properties` and restart the backend.

> The frontend calls the API at `http://localhost:8080/api` — this is set
> in `frontend/js/api.js` (`API_BASE`). Change it if you deploy the backend elsewhere.

## 4. Your logo

Your Viha Tech logo is already placed at `frontend/assets/logo.png` and is
shown on the login page and at the top of the sidebar on every screen.

## 5. Security notes before going live

- Change `app.jwt.secret` in `application.properties` to a long random value.
- The `forgot-password` endpoint currently returns the temporary password
  directly in the response for demo purposes — wire it to a real email
  service (e.g. Spring Mail) before production use.
- Add role-based `@PreAuthorize` checks on controllers (e.g. only
  ADMIN/HR can delete employees) — currently any authenticated user can
  call any endpoint, which is fine for development but not for production.

## Project structure

```
hrms/
├── database/
│   └── schema.sql            <- run this first
├── backend/                  <- Spring Boot 3 / Java 21 project
│   └── src/main/java/com/vihatech/hrms/
│       ├── entity/            JPA entities
│       ├── repository/        Spring Data repositories
│       ├── service/            business logic
│       ├── controller/        REST endpoints
│       ├── security/           JWT filter, util, user details
│       ├── config/             Spring Security + CORS + static file config
│       └── dto/                 request/response objects
└── frontend/
    ├── login.html
    ├── dashboard.html
    ├── employees.html          full CRUD + photo/doc upload
    ├── departments.html        full CRUD
    ├── designations.html       full CRUD
    ├── attendance.html         check-in/out + history
    ├── leave.html              apply/approve/reject
    ├── directory.html          searchable directory
    ├── [14 more module pages]  placeholders, ready to wire up
    ├── css/style.css
    ├── js/api.js               fetch wrapper + auth helpers
    ├── js/sidebar.js            shared nav w/ your logo
    └── assets/logo.png
```
