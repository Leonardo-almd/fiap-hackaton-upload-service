package br.com.fiap.upload.adapter.in.dto;

import br.com.fiap.upload.domain.model.Job;

import java.time.Instant;
import java.util.UUID;

public record UploadResponse(
        UUID jobId,
        String status,
        String message,
        Instant createdAt
) {
    public static UploadResponse from(Job job) {
        return new UploadResponse(
                job.getId(),
                job.getStatus().name(),
                "Diagrama recebido. O processamento foi iniciado.",
                job.getCreatedAt()
        );
    }
}
