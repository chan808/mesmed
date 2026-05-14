package com.chan.medmes.production.entity;

import com.chan.medmes.production.enums.AlarmSeverity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "alarm_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AlarmLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Column(name = "alarm_code", length = 50)
    private String alarmCode;

    @Column(length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AlarmSeverity severity;

    @Column(name = "occurred_at", updatable = false)
    private LocalDateTime occurredAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Builder
    public AlarmLog(Equipment equipment, String alarmCode, String message, AlarmSeverity severity) {
        this.equipment = equipment;
        this.alarmCode = alarmCode;
        this.message = message;
        this.severity = severity;
        this.occurredAt = LocalDateTime.now();
    }

    public void resolve() {
        this.resolvedAt = LocalDateTime.now();
    }

    public boolean isResolved() {
        return this.resolvedAt != null;
    }
}