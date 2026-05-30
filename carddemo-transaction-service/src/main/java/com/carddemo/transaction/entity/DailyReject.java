package com.carddemo.transaction.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "daily_rejects")
public class DailyReject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reject_id")
    private Integer rejectId;

    @Column(name = "dalytran_id", length = 16)
    private String dalytranId;

    @Column(name = "reject_reason", length = 100)
    private String rejectReason;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;
}
