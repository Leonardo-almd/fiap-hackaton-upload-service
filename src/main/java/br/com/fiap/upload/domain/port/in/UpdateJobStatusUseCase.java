package br.com.fiap.upload.domain.port.in;

import br.com.fiap.upload.domain.model.Job;
import br.com.fiap.upload.domain.model.JobStatus;

import java.util.UUID;

/**
 * Endpoint interno — chamado exclusivamente pelo processing-service
 * para atualizar o status do job durante o pipeline de análise.
 */
public interface UpdateJobStatusUseCase {

    record Command(UUID jobId, JobStatus newStatus, String errorMessage, UUID reportId) {
        public static Command toProcessing(UUID jobId) {
            return new Command(jobId, JobStatus.EM_PROCESSAMENTO, null, null);
        }

        public static Command toAnalyzed(UUID jobId, UUID reportId) {
            return new Command(jobId, JobStatus.ANALISADO, null, reportId);
        }

        public static Command toError(UUID jobId, String errorMessage) {
            return new Command(jobId, JobStatus.ERRO, errorMessage, null);
        }
    }

    Job execute(Command command);
}
