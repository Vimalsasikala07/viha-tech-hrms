-- ============================================================
-- VIHA TECH HRMS - Full Database Schema
-- Run this once in MySQL Workbench / CLI to create everything
-- ============================================================

CREATE DATABASE IF NOT EXISTS hrms_db;
USE hrms_db;

-- ---------- 6 & 7. Department / Designation ----------
CREATE TABLE department (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    department_head VARCHAR(100),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE designation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    salary_grade VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ---------- 8. Roles ----------
CREATE TABLE role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE   -- ADMIN, HR, MANAGER, EMPLOYEE
);

INSERT INTO role (name) VALUES ('ADMIN'), ('HR'), ('MANAGER'), ('EMPLOYEE');

-- ---------- 1. Employee ----------
CREATE TABLE employee (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_code VARCHAR(20) UNIQUE NOT NULL,   -- e.g. VT-2026-0001
    name VARCHAR(120) NOT NULL,
    email VARCHAR(120) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,               -- BCrypt hash
    phone VARCHAR(20),
    address VARCHAR(255),
    dob DATE,
    doj DATE,
    department_id BIGINT,
    designation_id BIGINT,
    salary DECIMAL(12,2),
    reporting_manager_id BIGINT,
    role_id BIGINT DEFAULT 4,
    status ENUM('ACTIVE','INACTIVE') DEFAULT 'ACTIVE',
    profile_photo_path VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (department_id) REFERENCES department(id),
    FOREIGN KEY (designation_id) REFERENCES designation(id),
    FOREIGN KEY (reporting_manager_id) REFERENCES employee(id),
    FOREIGN KEY (role_id) REFERENCES role(id)
);

CREATE TABLE employee_document (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    doc_type VARCHAR(50),        -- AADHAAR, PAN, CERTIFICATE
    file_path VARCHAR(255),
    uploaded_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE CASCADE
);

-- ---------- 2. Attendance ----------
CREATE TABLE attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    attendance_date DATE NOT NULL,
    check_in TIME,
    check_out TIME,
    break_minutes INT DEFAULT 0,
    late_login BOOLEAN DEFAULT FALSE,
    early_logout BOOLEAN DEFAULT FALSE,
    ip_address VARCHAR(50),
    gps_location VARCHAR(100),
    status ENUM('PRESENT','ABSENT','HALF_DAY','ON_LEAVE') DEFAULT 'PRESENT',
    correction_requested BOOLEAN DEFAULT FALSE,
    correction_reason VARCHAR(255),
    UNIQUE KEY uniq_emp_date (employee_id, attendance_date),
    FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE CASCADE
);

-- ---------- 3. Leave ----------
CREATE TABLE leave_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL   -- SICK, CASUAL, EARNED, HALF_DAY
);
INSERT INTO leave_type (name) VALUES ('SICK'),('CASUAL'),('EARNED'),('HALF_DAY');

CREATE TABLE leave_balance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    leave_type_id BIGINT NOT NULL,
    year INT NOT NULL,
    total_days DECIMAL(5,1) DEFAULT 12,
    used_days DECIMAL(5,1) DEFAULT 0,
    FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE CASCADE,
    FOREIGN KEY (leave_type_id) REFERENCES leave_type(id)
);

CREATE TABLE leave_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    leave_type_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason VARCHAR(255),
    status ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING',
    approved_by BIGINT,
    applied_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE CASCADE,
    FOREIGN KEY (leave_type_id) REFERENCES leave_type(id),
    FOREIGN KEY (approved_by) REFERENCES employee(id)
);

-- ---------- 4. Holiday Calendar ----------
CREATE TABLE holiday (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    holiday_date DATE NOT NULL,
    type ENUM('COMPANY','NATIONAL','FESTIVAL') DEFAULT 'COMPANY'
);

-- ---------- 5. Payroll ----------
CREATE TABLE payroll (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    pay_month INT NOT NULL,
    pay_year INT NOT NULL,
    basic DECIMAL(12,2),
    bonus DECIMAL(12,2) DEFAULT 0,
    incentives DECIMAL(12,2) DEFAULT 0,
    deductions DECIMAL(12,2) DEFAULT 0,
    pf DECIMAL(12,2) DEFAULT 0,
    esi DECIMAL(12,2) DEFAULT 0,
    professional_tax DECIMAL(12,2) DEFAULT 0,
    net_pay DECIMAL(12,2),
    generated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uniq_emp_month (employee_id, pay_month, pay_year),
    FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE CASCADE
);

-- ---------- 9. Recruitment ----------
CREATE TABLE job_opening (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(120),
    department_id BIGINT,
    description TEXT,
    status ENUM('OPEN','CLOSED') DEFAULT 'OPEN',
    FOREIGN KEY (department_id) REFERENCES department(id)
);

CREATE TABLE candidate (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_opening_id BIGINT,
    name VARCHAR(120),
    email VARCHAR(120),
    phone VARCHAR(20),
    resume_path VARCHAR(255),
    interview_date DATETIME,
    feedback TEXT,
    status ENUM('APPLIED','INTERVIEW','OFFERED','REJECTED','HIRED') DEFAULT 'APPLIED',
    FOREIGN KEY (job_opening_id) REFERENCES job_opening(id)
);

-- ---------- 10. Onboarding ----------
CREATE TABLE onboarding_checklist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    item VARCHAR(150),
    is_completed BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE CASCADE
);

-- ---------- 11. Performance ----------
CREATE TABLE performance_review (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    review_period VARCHAR(50),
    goals TEXT,
    kpi_score DECIMAL(5,2),
    manager_feedback TEXT,
    self_assessment TEXT,
    rating DECIMAL(3,1),
    FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE CASCADE
);

-- ---------- 12. Timesheet ----------
CREATE TABLE timesheet (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    work_date DATE NOT NULL,
    project_name VARCHAR(120),
    hours_worked DECIMAL(4,1),
    overtime_hours DECIMAL(4,1) DEFAULT 0,
    approval_status ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING',
    FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE CASCADE
);

-- ---------- 13. Shift Management ----------
CREATE TABLE shift (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50),         -- MORNING, EVENING, NIGHT, ROTATIONAL
    start_time TIME,
    end_time TIME
);

CREATE TABLE employee_shift (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    shift_id BIGINT NOT NULL,
    effective_date DATE,
    FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE CASCADE,
    FOREIGN KEY (shift_id) REFERENCES shift(id)
);

-- ---------- 14. Asset Management ----------
CREATE TABLE asset (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_type VARCHAR(50),   -- LAPTOP, DESKTOP, MOUSE, KEYBOARD, ID_CARD
    asset_tag VARCHAR(50) UNIQUE,
    allocated_to BIGINT,
    allocated_date DATE,
    returned_date DATE,
    status ENUM('ALLOCATED','RETURNED','DAMAGED') DEFAULT 'ALLOCATED',
    FOREIGN KEY (allocated_to) REFERENCES employee(id)
);

-- ---------- 15. Expense Management ----------
CREATE TABLE expense (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    category VARCHAR(80),
    amount DECIMAL(12,2),
    bill_path VARCHAR(255),
    status ENUM('PENDING','APPROVED','REJECTED','REIMBURSED') DEFAULT 'PENDING',
    submitted_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE CASCADE
);

-- ---------- 16. Announcements ----------
CREATE TABLE announcement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150),
    content TEXT,
    type ENUM('NEWS','HOLIDAY','POLICY','BIRTHDAY') DEFAULT 'NEWS',
    posted_by BIGINT,
    posted_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (posted_by) REFERENCES employee(id)
);

-- ---------- 19. Notifications ----------
CREATE TABLE notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    message VARCHAR(255),
    type VARCHAR(50),
    is_read BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE CASCADE
);

-- ---------- 20. Exit Management ----------
CREATE TABLE exit_process (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    resignation_date DATE,
    notice_period_days INT,
    last_working_day DATE,
    exit_interview_notes TEXT,
    clearance_status ENUM('PENDING','CLEARED') DEFAULT 'PENDING',
    fnf_settled BOOLEAN DEFAULT FALSE,
    experience_letter_issued BOOLEAN DEFAULT FALSE,
    relieving_letter_issued BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE CASCADE
);

-- ---------- Seed sample data ----------
INSERT INTO department (name, department_head) VALUES
 ('Engineering', 'Ravi Kumar'),
 ('Human Resources', 'Anjali Sharma'),
 ('Sales', 'Vikram Singh');

INSERT INTO designation (title, salary_grade) VALUES
 ('Software Engineer', 'G2'),
 ('HR Executive', 'G2'),
 ('Sales Manager', 'G3'),
 ('System Administrator', 'G3');

-- Default admin login -> email: admin@vihatech.com / password: Admin@123
-- (password hash below is BCrypt for "Admin@123" - generated by the backend on first run instead if you prefer)
INSERT INTO employee (employee_code, name, email, password, phone, doj, department_id, designation_id, salary, role_id, status)
VALUES ('VT-2026-0001', 'System Admin', 'admin@vihatech.com',
 '$2a$10$7EqJtq98hPqEX7fNZaFWoOe6zhpZ3RwXe2rNlRzMd3lYb1J0kzEeK',
 '9999999999', CURDATE(), 1, 1, 0, 1, 'ACTIVE');
