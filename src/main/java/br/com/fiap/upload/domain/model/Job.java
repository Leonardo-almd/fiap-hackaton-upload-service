package br.com.fiap.upload.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Job {

    private final UUID id;
    private JobStatus status;
    private final String s3Key;
    private final String originalFilename;
    private final String fileType;
    private final Long fileSizeBytes;
    private final String description;
    private String errorMessage;
    private UUID reportId;
    private final Instant createdAt;
    private Instant updatedAt;

    public Job(UUID id, JobStatus status, String s3Key, String originalFilename,
               String fileType, Long fileSizeBytes, String description,
               String errorMessage, UUID reportId, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.status = status;
        this.s3Key = s3Key;
        this.originalFilename = originalFilename;
        this.fileType = fileType;
        this.fileSizeBytes = fileSizeBytes;
        this.description = description;
        this.errorMessage = errorMessage;
        this.reportId = reportId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Job create(String s3Key, String originalFilename, String fileType,
                             Long fileSizeBytes, String description) {
        Instant now = Instant.now();
        return new Job(UUID.randomUUID(), JobStatus.RECEBIDO, s3Key, originalFilename,
                fileType, fileSizeBytes, description, null, null, now, now);
    }

    public void markAsProcessing() {
        requireNonTerminal("EM_PROCESSAMENTO");
        this.status = JobStatus.EM_PROCESSAMENTO;
        this.updatedAt = Instant.now();
    }

    public void markAsAnalyzed(UUID reportId) {
        requireNonTerminal("ANALISADO");
        this.status = JobStatus.ANALISADO;
        this.reportId = reportId;
        this.updatedAt = Instant.now();
    }

    public void markAsError(String errorMessage) {
        requireNonTerminal("ERRO");
        this.status = JobStatus.ERRO;
        this.errorMessage = errorMessage;
        this.updatedAt = Instant.now();
    }

    private void requireNonTerminal(String targetStatus) {
        if (this.status == JobStatus.ANALISADO || this.status == JobStatus.ERRO) {
            throw new IllegalArgumentException(
                    "Transição inválida: job já está em estado terminal '" + this.status +
                    "' e não pode ser movido para '" + targetStatus + "'.");
        }
    }

    public UUID getId() { return id; }
    public JobStatus getStatus() { return status; }
    public String getS3Key() { return s3Key; }
    public String getOriginalFilename() { return originalFilename; }
    public String getFileType() { return fileType; }
    public Long getFileSizeBytes() { return fileSizeBytes; }
    public String getDescription() { return description; }
    public String getErrorMessage() { return errorMessage; }
    public UUID getReportId() { return reportId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
