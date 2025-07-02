package com.venue.mgmt.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Entity
@Table(
        name = "rule_master",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"channel_code", "rule_name"})})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RuleMasterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_name", nullable = false)
    private String ruleName;

    @Column(nullable = false)
    private String channelCode;

    @Column(name = "rule_value", nullable = false, columnDefinition = "TEXT")
    private String ruleValue;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
