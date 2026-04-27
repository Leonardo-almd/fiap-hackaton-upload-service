package br.com.fiap.upload.application.usecase;

import br.com.fiap.upload.domain.model.Job;
import br.com.fiap.upload.domain.port.in.GetJobStatusUseCase;
import br.com.fiap.upload.domain.port.out.JobRepository;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class GetJobStatusUseCaseImpl implements GetJobStatusUseCase {

    private final JobRepository jobRepository;

    public GetJobStatusUseCaseImpl(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Override
    public Job execute(UUID jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Job não encontrado com id: " + jobId));
    }
}
