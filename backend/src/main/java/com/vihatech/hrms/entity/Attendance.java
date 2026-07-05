package com.vihatech.hrms.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "attendance", uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "attendance_date"}))
@Data
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    private LocalTime checkIn;
    private LocalTime checkOut;
    private Integer breakMinutes = 0;
    private Boolean lateLogin = false;
    private Boolean earlyLogout = false;
    private String ipAddress;
    private String gpsLocation;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus status = AttendanceStatus.PRESENT;

    private Boolean correctionRequested = false;
    private String correctionReason;

    public enum AttendanceStatus { PRESENT, ABSENT, HALF_DAY, ON_LEAVE }
}
