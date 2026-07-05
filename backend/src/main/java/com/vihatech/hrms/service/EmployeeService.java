package com.vihatech.hrms.service;

import com.vihatech.hrms.dto.EmployeeDto;
import com.vihatech.hrms.entity.*;
import com.vihatech.hrms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.upload.dir}")
    private String uploadDir;

    // ---------- ID generation: VT-<year>-0001 ----------
    public String generateEmployeeCode() {
        int year = Year.now().getValue();
        long count = employeeRepository.count() + 1;
        String code;
        do {
            code = String.format("VT-%d-%04d", year, count);
            count++;
        } while (employeeRepository.existsByEmployeeCode(code));
        return code;
    }

    public List<EmployeeDto> getAll() {
        return employeeRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public EmployeeDto getById(Long id) {
        return toDto(findEntity(id));
    }

    public EmployeeDto create(EmployeeDto dto) {
        if (employeeRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("An employee with this email already exists");
        }
        Employee emp = new Employee();
        applyDto(emp, dto, true);
        emp.setEmployeeCode(generateEmployeeCode());
        emp.setCreatedAt(LocalDateTime.now());
        emp.setUpdatedAt(LocalDateTime.now());
        return toDto(employeeRepository.save(emp));
    }

    public EmployeeDto update(Long id, EmployeeDto dto) {
        Employee emp = findEntity(id);
        applyDto(emp, dto, false);
        emp.setUpdatedAt(LocalDateTime.now());
        return toDto(employeeRepository.save(emp));
    }

    public void delete(Long id) {
        employeeRepository.delete(findEntity(id));
    }

    public String uploadProfilePhoto(Long id, MultipartFile file) throws IOException {
        Employee emp = findEntity(id);
        String path = storeFile(file, "photos");
        emp.setProfilePhotoPath(path);
        employeeRepository.save(emp);
        return path;
    }

    public String uploadDocument(Long id, MultipartFile file) throws IOException {
        findEntity(id); // ensures employee exists
        return storeFile(file, "documents/" + id);
    }

    private String storeFile(MultipartFile file, String subFolder) throws IOException {
        Path dir = Path.of(uploadDir, subFolder);
        Files.createDirectories(dir);
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path target = dir.resolve(filename);
        file.transferTo(target);
        return "/uploads/" + subFolder + "/" + filename;
    }

    private Employee findEntity(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
    }

    private void applyDto(Employee emp, EmployeeDto dto, boolean isCreate) {
        emp.setName(dto.getName());
        emp.setEmail(dto.getEmail());
        if (isCreate && dto.getPassword() != null && !dto.getPassword().isBlank()) {
            emp.setPassword(passwordEncoder.encode(dto.getPassword()));
        } else if (isCreate) {
            emp.setPassword(passwordEncoder.encode("Welcome@123")); // default temp password
        }
        emp.setPhone(dto.getPhone());
        emp.setAddress(dto.getAddress());
        emp.setDob(dto.getDob());
        emp.setDoj(dto.getDoj());
        emp.setSalary(dto.getSalary());

        if (dto.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            emp.setDepartment(dept);
        }
        if (dto.getDesignationId() != null) {
            Designation des = designationRepository.findById(dto.getDesignationId())
                    .orElseThrow(() -> new RuntimeException("Designation not found"));
            emp.setDesignation(des);
        }
        if (dto.getReportingManagerId() != null) {
            Employee manager = employeeRepository.findById(dto.getReportingManagerId())
                    .orElseThrow(() -> new RuntimeException("Reporting manager not found"));
            emp.setReportingManager(manager);
        } else {
            emp.setReportingManager(null);
        }
        if (dto.getRole() != null) {
            Role role = roleRepository.findByName(dto.getRole())
                    .orElseThrow(() -> new RuntimeException("Role not found: " + dto.getRole()));
            emp.setRole(role);
        } else if (isCreate) {
            emp.setRole(roleRepository.findByName("EMPLOYEE").orElseThrow());
        }
        if (dto.getStatus() != null) {
            emp.setStatus(Employee.Status.valueOf(dto.getStatus()));
        }
    }

    private EmployeeDto toDto(Employee emp) {
        EmployeeDto dto = new EmployeeDto();
        dto.setId(emp.getId());
        dto.setEmployeeCode(emp.getEmployeeCode());
        dto.setName(emp.getName());
        dto.setEmail(emp.getEmail());
        dto.setPhone(emp.getPhone());
        dto.setAddress(emp.getAddress());
        dto.setDob(emp.getDob());
        dto.setDoj(emp.getDoj());
        if (emp.getDepartment() != null) {
            dto.setDepartmentId(emp.getDepartment().getId());
            dto.setDepartmentName(emp.getDepartment().getName());
        }
        if (emp.getDesignation() != null) {
            dto.setDesignationId(emp.getDesignation().getId());
            dto.setDesignationName(emp.getDesignation().getTitle());
        }
        dto.setSalary(emp.getSalary());
        if (emp.getReportingManager() != null) {
            dto.setReportingManagerId(emp.getReportingManager().getId());
            dto.setReportingManagerName(emp.getReportingManager().getName());
        }
        dto.setRole(emp.getRole() != null ? emp.getRole().getName() : null);
        dto.setStatus(emp.getStatus().name());
        dto.setProfilePhotoPath(emp.getProfilePhotoPath());
        return dto;
        // Note: password hash is intentionally never included in the response DTO
    }
}
