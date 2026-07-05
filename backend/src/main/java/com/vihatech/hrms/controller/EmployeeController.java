package com.vihatech.hrms.controller;

import com.vihatech.hrms.dto.EmployeeDto;
import com.vihatech.hrms.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public List<EmployeeDto> getAll() {
        return employeeService.getAll();
    }

    @GetMapping("/{id}")
    public EmployeeDto getById(@PathVariable Long id) {
        return employeeService.getById(id);
    }

    @PostMapping
    public ResponseEntity<EmployeeDto> create(@RequestBody EmployeeDto dto) {
        return ResponseEntity.ok(employeeService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDto> update(@PathVariable Long id, @RequestBody EmployeeDto dto) {
        return ResponseEntity.ok(employeeService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return ResponseEntity.ok(Map.of("deleted", true));
    }

    @PostMapping("/{id}/photo")
    public ResponseEntity<Map<String, String>> uploadPhoto(@PathVariable Long id,
                                                             @RequestParam("file") MultipartFile file) throws IOException {
        String path = employeeService.uploadProfilePhoto(id, file);
        return ResponseEntity.ok(Map.of("path", path));
    }

    @PostMapping("/{id}/documents")
    public ResponseEntity<Map<String, String>> uploadDocument(@PathVariable Long id,
                                                                @RequestParam("file") MultipartFile file) throws IOException {
        String path = employeeService.uploadDocument(id, file);
        return ResponseEntity.ok(Map.of("path", path));
    }
}
