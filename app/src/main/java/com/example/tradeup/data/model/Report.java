package com.example.tradeup.data.model;

import androidx.annotation.Nullable;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

public class Report {
    @DocumentId
    private String reportId;
    private String reportingUserId;
    private String reportedContentType; // "listing", "profile", "chatMessage"
    private String reportedContentId;
    @Nullable
    private String reportedUserId; // UID of the owner of the reported content
    private String reason; // ID from appConfig
    @Nullable
    private String details;
    private String status; // "pending_review", "under_review", "resolved_action_taken", "resolved_no_action"
    @ServerTimestamp
    @Nullable
    private Timestamp createdAt;
    @Nullable
    private String adminNotes; // Client should not write
    @Nullable
    private Timestamp resolvedAt; // Client should not write

    // Constructor rỗng cần thiết cho Firestore
    public Report() {
        this.reportId = "";
        this.reportingUserId = "";
        this.reportedContentType = "";
        this.reportedContentId = "";
        this.reportedUserId = null;
        this.reason = "";
        this.details = null;
        this.status = "pending_review";
        this.createdAt = null;
        this.adminNotes = null;
        this.resolvedAt = null;
    }

    // Getters and Setters
    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public String getReportingUserId() { return reportingUserId; }
    public void setReportingUserId(String reportingUserId) { this.reportingUserId = reportingUserId; }

    public String getReportedContentType() { return reportedContentType; }
    public void setReportedContentType(String reportedContentType) { this.reportedContentType = reportedContentType; }

    public String getReportedContentId() { return reportedContentId; }
    public void setReportedContentId(String reportedContentId) { this.reportedContentId = reportedContentId; }

    @Nullable
    public String getReportedUserId() { return reportedUserId; }
    public void setReportedUserId(@Nullable String reportedUserId) { this.reportedUserId = reportedUserId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    @Nullable
    public String getDetails() { return details; }
    public void setDetails(@Nullable String details) { this.details = details; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Nullable
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(@Nullable Timestamp createdAt) { this.createdAt = createdAt; }

    @Nullable
    public String getAdminNotes() { return adminNotes; }
    public void setAdminNotes(@Nullable String adminNotes) { this.adminNotes = adminNotes; }

    @Nullable
    public Timestamp getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(@Nullable Timestamp resolvedAt) { this.resolvedAt = resolvedAt; }
}