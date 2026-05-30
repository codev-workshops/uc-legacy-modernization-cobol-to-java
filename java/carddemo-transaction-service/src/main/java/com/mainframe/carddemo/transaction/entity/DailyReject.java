package com.mainframe.carddemo.transaction.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "daily_reject")
public class DailyReject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reject_id")
    private Integer rejectId;

    @Column(name = "tran_id", length = 16)
    private String tranId;

    @Column(name = "reject_reason")
    private Integer rejectReason;

    @Column(name = "reject_desc", length = 100)
    private String rejectDesc;

    @Column(name = "reject_ts")
    private LocalDateTime rejectTs;

    public DailyReject() {
    }

    public Integer getRejectId() { return rejectId; }
    public void setRejectId(Integer rejectId) { this.rejectId = rejectId; }

    public String getTranId() { return tranId; }
    public void setTranId(String tranId) { this.tranId = tranId; }

    public Integer getRejectReason() { return rejectReason; }
    public void setRejectReason(Integer rejectReason) { this.rejectReason = rejectReason; }

    public String getRejectDesc() { return rejectDesc; }
    public void setRejectDesc(String rejectDesc) { this.rejectDesc = rejectDesc; }

    public LocalDateTime getRejectTs() { return rejectTs; }
    public void setRejectTs(LocalDateTime rejectTs) { this.rejectTs = rejectTs; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DailyReject that = (DailyReject) o;
        return Objects.equals(rejectId, that.rejectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rejectId);
    }
}
