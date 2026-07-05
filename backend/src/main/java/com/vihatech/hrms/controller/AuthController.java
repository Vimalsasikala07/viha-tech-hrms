package com.vihatech.hrms.controller;

import com.vihatech.hrms.dto.*;
import com.vihatech.hrms.entity.Employee;
import com.vihatech.hrms.repository.EmployeeRepository;
import com.vihatech.hrms.security.EmployeeUserDetailsService;
import com.vihatech.hrms.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final EmployeeUserDetailsService userDetailsService;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401).body(new ApiResponse(false, "Invalid email or password"));
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        Employee emp = employeeRepository.findByEmail(request.getEmail()).orElseThrow();

        if (emp.getStatus() == Employee.Status.INACTIVE) {
            return ResponseEntity.status(403).body(new ApiResponse(false, "Account is inactive. Contact HR."));
        }

        String token = jwtUtil.generateToken(userDetails);
        String role = emp.getRole() != null ? emp.getRole().getName() : "EMPLOYEE";

        return ResponseEntity.ok(new LoginResponse(token, emp.getName(), emp.getEmail(), role, emp.getId()));
    }

    // Logout is stateless with JWT - the frontend simply discards the token.
    // This endpoint exists so the UI has a clear action to call.
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(new ApiResponse(true, "Logged out successfully"));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request,
                                             @RequestParam String email) {
        Employee emp = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), emp.getPassword())) {
            return ResponseEntity.status(400).body(new ApiResponse(false, "Current password is incorrect"));
        }
        emp.setPassword(passwordEncoder.encode(request.getNewPassword()));
        employeeRepository.save(emp);
        return ResponseEntity.ok(new ApiResponse(true, "Password changed successfully"));
    }

    // Simplified forgot-password: resets to a temporary password and returns it.
    // In production, wire this to an email service instead of returning the password directly.
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        Employee emp = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No account with that email"));
        String tempPassword = "Temp@" + (int) (Math.random() * 9000 + 1000);
        emp.setPassword(passwordEncoder.encode(tempPassword));
        employeeRepository.save(emp);
        return ResponseEntity.ok(new ApiResponse(true, "Temporary password: " + tempPassword +
                " (in production this would be emailed, not returned here)"));
    }
}
