package com.vihatech.hrms.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "leave_type")
@Data
public class LeaveType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // SICK, CASUAL, EARNED, HALF_DAY
}
