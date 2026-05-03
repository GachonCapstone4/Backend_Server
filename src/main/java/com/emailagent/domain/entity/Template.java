package com.emailagent.domain.entity;

import com.emailagent.domain.enums.TemplateIndexStatus;
import com.emailagent.domain.enums.TemplateOrigin;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "templates",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_templates_user_template_no",
                        columnNames = {"user_id", "user_template_no"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "user_template_no")
    private Long userTemplateNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "variant_label", length = 100)
    private String variantLabel;

    @Column(name = "subject_template", nullable = false, length = 500)
    private String subjectTemplate;

    @Column(name = "body_template", nullable = false, columnDefinition = "TEXT")
    private String bodyTemplate;

    @Enumerated(EnumType.STRING)
    @Column(name = "origin", length = 30)
    @Builder.Default
    private TemplateOrigin origin = TemplateOrigin.USER_CREATED;

    @Column(name = "user_modified")
    @Builder.Default
    private Boolean userModified = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "index_status", length = 30)
    @Builder.Default
    private TemplateIndexStatus indexStatus = TemplateIndexStatus.NOT_INDEXED;

    @Column(name = "canonical_text", columnDefinition = "TEXT")
    private String canonicalText;

    @Column(name = "indexed_at")
    private LocalDateTime indexedAt;

    @Column(name = "accuracy_score", precision = 5, scale = 2)
    private BigDecimal accuracyScore;

    @Column(name = "use_count")
    @Builder.Default
    private Integer useCount = 0;

    @Column(name = "user_count")
    @Builder.Default
    private Integer userCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void update(String title, String variantLabel, String subjectTemplate, String bodyTemplate) {
        this.title = title;
        this.variantLabel = variantLabel;
        this.subjectTemplate = subjectTemplate;
        this.bodyTemplate = bodyTemplate;
    }

    public void markAiGenerated() {
        this.origin = TemplateOrigin.AI_GENERATED;
        this.userModified = false;
    }

    public void markUserCreated() {
        this.origin = TemplateOrigin.USER_CREATED;
        this.userModified = false;
    }

    public void markUserModified() {
        this.userModified = true;
    }

    public boolean isUserModified() {
        return Boolean.TRUE.equals(userModified);
    }

    public void prepareIndexing(String canonicalText) {
        this.canonicalText = canonicalText;
        this.indexStatus = TemplateIndexStatus.INDEXING;
        this.indexedAt = null;
    }

    public void markIndexed() {
        this.indexStatus = TemplateIndexStatus.INDEXED;
        this.indexedAt = LocalDateTime.now();
    }

    public void markIndexFailed() {
        this.indexStatus = TemplateIndexStatus.FAILED;
    }

    public void incrementUseCount() {
        this.useCount = (this.useCount == null ? 0 : this.useCount) + 1;
    }

    public void updateUserCount(int count) {
        this.userCount = count;
    }

    public void updateAccuracyScore(BigDecimal score) {
        this.accuracyScore = score;
    }

    public void assignUserTemplateNo(Long userTemplateNo) {
        if (this.userTemplateNo != null) {
            return;
        }
        this.userTemplateNo = userTemplateNo;
    }

    public String getQuality() {
        if (accuracyScore == null) return "보통";
        if (accuracyScore.compareTo(new BigDecimal("0.8")) >= 0) return "높음";
        if (accuracyScore.compareTo(new BigDecimal("0.5")) >= 0) return "보통";
        return "낮음";
    }
}
