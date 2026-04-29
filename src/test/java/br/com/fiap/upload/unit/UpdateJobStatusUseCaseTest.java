package br.com.fiap.upload.unit;

import br.com.fiap.upload.application.usecase.UpdateJobStatusUseCaseImpl;
import br.com.fiap.upload.domain.model.Job;
import br.com.fiap.upload.domain.model.JobStatus;
import br.com.fiap.upload.domain.port.in.UpdateJobStatusUseCase;
import br.com.fiap.upload.domain.port.out.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateJobStatusUseCaseTest {

    @Mock private JobRepository jobRepository;

    private UpdateJobStatusUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateJobStatusUseCaseImpl(jobRepository);
    }

    private Job buildJob(UUID id, JobStatus status) {
        return new Job(id, status, "uploads/f.pdf", "f.pdf", "PDF",
                1024L, null, null, null, Instant.now(), Instant.now());
    }

    @Test
    void shouldTransitionToEmProcessamento() {
        UUID jobId = UUID.randomUUID();
        Job job = buildJob(jobId, JobStatus.RECEBIDO);
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(jobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Job result = useCase.execute(UpdateJobStatusUseCase.Command.toProcessing(jobId));

        assertThat(result.getStatus()).isEqualTo(JobStatus.EM_PROCESSAMENTO);
    }

    @Test
    void shouldTransitionToAnalisado() {
        UUID jobId = UUID.randomUUID();
        UUID reportId = UUID.randomUUID();
        Job job = buildJob(jobId, JobStatus.EM_PROCESSAMENTO);
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(jobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Job result = useCase.execute(UpdateJobStatusUseCase.Command.toAnalyzed(jobId, reportId));

        assertThat(result.getStatus()).isEqualTo(JobStatus.ANALISADO);
        assertThat(result.getReportId()).isEqualTo(reportId);
    }

    @Test
    void shouldTransitionToErro() {
        UUID jobId = UUID.randomUUID();
        Job job = buildJob(jobId, JobStatus.EM_PROCESSAMENTO);
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(jobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Job result = useCase.execute(UpdateJobStatusUseCase.Command.toError(jobId, "Falha na IA"));

        assertThat(result.getStatus()).isEqualTo(JobStatus.ERRO);
        assertThat(result.getErrorMessage()).isEqualTo("Falha na IA");
    }

    @Test
    void shouldThrowWhenJobNotFound() {
        UUID jobId = UUID.randomUUID();
        when(jobRepository.findById(jobId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(UpdateJobStatusUseCase.Command.toProcessing(jobId)))
                .isInstanceOf(NoSuchElementException.class);
    }
}
