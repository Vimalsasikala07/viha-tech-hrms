package com.vihatech.hrms.controller;

import com.vihatech.hrms.entity.Designation;
import com.vihatech.hrms.repository.DesignationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/designations")
@RequiredArgsConstructor
public class DesignationController {

    private final DesignationRepository designationRepository;

    @GetMapping
    public List<Designation> getAll() {
        return designationRepository.findAll();
    }

    @GetMapping("/{id}")
    public Designation getById(@PathVariable Long id) {
        return designationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Designation not found"));
    }

    @PostMapping
    public Designation create(@RequestBody Designation designation) {
        return designationRepository.save(designation);
    }

    @PutMapping("/{id}")
    public Designation update(@PathVariable Long id, @RequestBody Designation updated) {
        Designation des = getById(id);
        des.setTitle(updated.getTitle());
        des.setSalaryGrade(updated.getSalaryGrade());
        return designationRepository.save(des);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> delete(@PathVariable Long id) {
        designationRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("deleted", true));
    }
}
