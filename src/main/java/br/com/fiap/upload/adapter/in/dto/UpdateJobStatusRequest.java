package br.com.fiap.upload.adapter.in.dto;

import br.com.fiap.upload.domain.model.JobStatus;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateJobStatusRequest(
        @NotNull(message = "O campo 'status' é obrigatório")
        JobStatus status,
        String errorMessage,
        UUID reportId
) {}
