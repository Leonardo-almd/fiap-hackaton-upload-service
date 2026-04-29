package br.com.fiap.upload.adapter.in.dto;

import br.com.fiap.upload.domain.model.Job;

import java.time.Instant;
import java.util.UUID;

public record JobStatusResponse(
        UUID jobId,
        String status,
        UUID reportId,
        String errorMessage,
        Instant createdAt,
        Instant updatedAt
) {
    public static JobStatusResponse from(Job job) {
        return new JobStatusResponse(
                job.getId(),
                job.getStatus().name(),
                job.getReportId(),
                job.getErrorMessage(),
                job.getCreatedAt(),
                job.getUpdatedAt()
        );
    }
}
