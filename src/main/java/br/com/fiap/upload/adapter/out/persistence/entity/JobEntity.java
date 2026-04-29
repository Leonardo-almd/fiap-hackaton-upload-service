package br.com.fiap.upload.adapter.out.persistence.entity;

import br.com.fiap.upload.domain.model.Job;
import br.com.fiap.upload.domain.model.JobStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "jobs")
public class JobEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JobStatus status;

    @Column(name = "s3_key", nullable = false, length = 512)
    private String s3Key;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "file_type", nullable = false, length = 10)
    private String fileType;

    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;

    @Column(length = 500)
    private String description;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    @Column(name = "report_id", columnDefinition = "uuid")
    private UUID reportId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected JobEntity() {}

    public static JobEntity from(Job job) {
        JobEntity entity = new JobEntity();
        entity.id = job.getId();
        entity.status = job.getStatus();
        entity.s3Key = job.getS3Key();
        entity.originalFilename = job.getOriginalFilename();
        entity.fileType = job.getFileType();
        entity.fileSizeBytes = job.getFileSizeBytes();
        entity.description = job.getDescription();
        entity.errorMessage = job.getErrorMessage();
        entity.reportId = job.getReportId();
        entity.createdAt = job.getCreatedAt();
        entity.updatedAt = job.getUpdatedAt();
        return entity;
    }

    public Job toDomain() {
        return new Job(id, status, s3Key, originalFilename, fileType, fileSizeBytes,
                description, errorMessage, reportId, createdAt, updatedAt);
    }

    public UUID getId() { return id; }
    public JobStatus getStatus() { return status; }
}
