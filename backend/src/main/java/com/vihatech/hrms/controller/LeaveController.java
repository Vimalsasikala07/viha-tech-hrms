package com.vihatech.hrms.controller;

import com.vihatech.hrms.entity.*;
import com.vihatech.hrms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/leave")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeRepository employeeRepository;

    @GetMapping("/types")
    public List<LeaveType> types() {
        return leaveTypeRepository.findAll();
    }

    @PostMapping("/apply")
    public ResponseEntity<?> apply(@RequestParam Long employeeId,
                                    @RequestParam Long leaveTypeId,
                                    @RequestParam String startDate,
                                    @RequestParam String endDate,
                                    @RequestParam(required = false) String reason) {
        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        LeaveType type = leaveTypeRepository.findById(leaveTypeId)
                .orElseThrow(() -> new RuntimeException("Leave type not found"));

        LeaveRequest req = new LeaveRequest();
        req.setEmployee(emp);
        req.setLeaveType(type);
        req.setStartDate(LocalDate.parse(startDate));
        req.setEndDate(LocalDate.parse(endDate));
        req.setReason(reason);
        req.setStatus(LeaveRequest.LeaveStatus.PENDING);
        return ResponseEntity.ok(leaveRequestRepository.save(req));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id, @RequestParam Long approverId) {
        LeaveRequest req = getRequest(id);
        req.setStatus(LeaveRequest.LeaveStatus.APPROVED);
        req.setApprovedBy(employeeRepository.findById(approverId).orElseThrow());
        deductBalance(req);
        return ResponseEntity.ok(leaveRequestRepository.save(req));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id, @RequestParam Long approverId) {
        LeaveRequest req = getRequest(id);
        req.setStatus(LeaveRequest.LeaveStatus.REJECTED);
        req.setApprovedBy(employeeRepository.findById(approverId).orElseThrow());
        return ResponseEntity.ok(leaveRequestRepository.save(req));
    }

    @GetMapping("/employee/{employeeId}")
    public List<LeaveRequest> forEmployee(@PathVariable Long employeeId) {
        return leaveRequestRepository.findByEmployeeIdOrderByAppliedAtDesc(employeeId);
    }

    @GetMapping("/all")
    public List<LeaveRequest> all() {
        return leaveRequestRepository.findAllByOrderByAppliedAtDesc();
    }

    @GetMapping("/balance/{employeeId}")
    public List<LeaveBalance> balance(@PathVariable Long employeeId,
                                       @RequestParam(required = false) Integer year) {
        int y = year != null ? year : Year.now().getValue();
        return leaveBalanceRepository.findByEmployeeIdAndYear(employeeId, y);
    }

    private LeaveRequest getRequest(Long id) {
        return leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));
    }

    private void deductBalance(LeaveRequest req) {
        long days = ChronoUnit.DAYS.between(req.getStartDate(), req.getEndDate()) + 1;
        int year = req.getStartDate().getYear();
        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndYear(req.getEmployee().getId(), req.getLeaveType().getId(), year)
                .orElseGet(() -> {
                    LeaveBalance b = new LeaveBalance();
                    b.setEmployee(req.getEmployee());
                    b.setLeaveType(req.getLeaveType());
                    b.setYear(year);
                    b.setTotalDays(BigDecimal.valueOf(12));
                    b.setUsedDays(BigDecimal.ZERO);
                    return b;
                });
        balance.setUsedDays(balance.getUsedDays().add(BigDecimal.valueOf(days)));
        leaveBalanceRepository.save(balance);
    }
}
