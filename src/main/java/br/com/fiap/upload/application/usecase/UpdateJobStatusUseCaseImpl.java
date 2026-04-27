package br.com.fiap.upload.application.usecase;

import br.com.fiap.upload.domain.model.Job;
import br.com.fiap.upload.domain.port.in.UpdateJobStatusUseCase;
import br.com.fiap.upload.domain.port.out.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class UpdateJobStatusUseCaseImpl implements UpdateJobStatusUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdateJobStatusUseCaseImpl.class);

    private final JobRepository jobRepository;

    public UpdateJobStatusUseCaseImpl(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Override
    public Job execute(Command command) {
        MDC.put("jobId", command.jobId().toString());

        Job job = jobRepository.findById(command.jobId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Job não encontrado com id: " + command.jobId()));

        switch (command.newStatus()) {
            case EM_PROCESSAMENTO -> job.markAsProcessing();
            case ANALISADO -> job.markAsAnalyzed(command.reportId());
            case ERRO -> job.markAsError(command.errorMessage());
            default -> throw new IllegalArgumentException(
                    "Transição de status inválida para: " + command.newStatus());
        }

        Job updated = jobRepository.save(job);
        log.info("Status do job atualizado. jobId={}, novoStatus={}", updated.getId(), updated.getStatus());

        MDC.remove("jobId");
        return updated;
    }
}
