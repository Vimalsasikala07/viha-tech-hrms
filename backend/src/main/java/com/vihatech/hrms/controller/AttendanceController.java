package com.vihatech.hrms.controller;

import com.vihatech.hrms.entity.Attendance;
import com.vihatech.hrms.entity.Employee;
import com.vihatech.hrms.repository.AttendanceRepository;
import com.vihatech.hrms.repository.EmployeeRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    private static final LocalTime SHIFT_START = LocalTime.of(9, 30);
    private static final LocalTime SHIFT_END = LocalTime.of(18, 30);

    @PostMapping("/checkin/{employeeId}")
    public ResponseEntity<?> checkIn(@PathVariable Long employeeId, HttpServletRequest request) {
        LocalDate today = LocalDate.now();
        if (attendanceRepository.findByEmployeeIdAndAttendanceDate(employeeId, today).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Already checked in today"));
        }
        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        Attendance att = new Attendance();
        att.setEmployee(emp);
        att.setAttendanceDate(today);
        LocalTime now = LocalTime.now();
        att.setCheckIn(now);
        att.setLateLogin(now.isAfter(SHIFT_START));
        att.setIpAddress(request.getRemoteAddr());
        return ResponseEntity.ok(attendanceRepository.save(att));
    }

    @PostMapping("/checkout/{employeeId}")
    public ResponseEntity<?> checkOut(@PathVariable Long employeeId) {
        LocalDate today = LocalDate.now();
        Attendance att = attendanceRepository.findByEmployeeIdAndAttendanceDate(employeeId, today)
                .orElseThrow(() -> new RuntimeException("No check-in found for today"));
        LocalTime now = LocalTime.now();
        att.setCheckOut(now);
        att.setEarlyLogout(now.isBefore(SHIFT_END));
        return ResponseEntity.ok(attendanceRepository.save(att));
    }

    @GetMapping("/employee/{employeeId}")
    public List<Attendance> history(@PathVariable Long employeeId) {
        return attendanceRepository.findByEmployeeIdOrderByAttendanceDateDesc(employeeId);
    }

    @GetMapping("/today")
    public List<Attendance> todayAll() {
        return attendanceRepository.findByAttendanceDateOrderByEmployeeId(LocalDate.now());
    }

    @PostMapping("/correction/{attendanceId}")
    public ResponseEntity<?> requestCorrection(@PathVariable Long attendanceId, @RequestParam String reason) {
        Attendance att = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new RuntimeException("Attendance record not found"));
        att.setCorrectionRequested(true);
        att.setCorrectionReason(reason);
        return ResponseEntity.ok(attendanceRepository.save(att));
    }
}
