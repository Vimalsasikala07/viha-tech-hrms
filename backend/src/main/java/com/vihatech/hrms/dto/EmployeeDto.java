package com.vihatech.hrms.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EmployeeDto {
    private Long id;
    private String employeeCode;
    private String name;
    private String email;
    private String password;      // only used on create
    private String phone;
    private String address;
    private LocalDate dob;
    private LocalDate doj;
    private Long departmentId;
    private String departmentName;
    private Long designationId;
    private String designationName;
    private BigDecimal salary;
    private Long reportingManagerId;
    private String reportingManagerName;
    private String role;
    private String status;
    private String profilePhotoPath;
}
