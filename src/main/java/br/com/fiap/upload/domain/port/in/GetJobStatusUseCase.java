package br.com.fiap.upload.domain.port.in;

import br.com.fiap.upload.domain.model.Job;

import java.util.UUID;

public interface GetJobStatusUseCase {

    Job execute(UUID jobId);
}
