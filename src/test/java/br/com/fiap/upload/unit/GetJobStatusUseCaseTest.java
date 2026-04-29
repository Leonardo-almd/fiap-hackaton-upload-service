package br.com.fiap.upload.unit;

import br.com.fiap.upload.application.usecase.GetJobStatusUseCaseImpl;
import br.com.fiap.upload.domain.model.Job;
import br.com.fiap.upload.domain.model.JobStatus;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetJobStatusUseCaseTest {

    @Mock private JobRepository jobRepository;

    private GetJobStatusUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetJobStatusUseCaseImpl(jobRepository);
    }

    @Test
    void shouldReturnJobWhenFound() {
        UUID jobId = UUID.randomUUID();
        Job job = new Job(jobId, JobStatus.RECEBIDO, "uploads/file.pdf", "diagram.pdf",
                "PDF", 1024L, null, null, null, Instant.now(), Instant.now());

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));

        Job result = useCase.execute(jobId);

        assertThat(result.getId()).isEqualTo(jobId);
        assertThat(result.getStatus()).isEqualTo(JobStatus.RECEBIDO);
    }

    @Test
    void shouldThrowWhenJobNotFound() {
        UUID jobId = UUID.randomUUID();
        when(jobRepository.findById(jobId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(jobId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining(jobId.toString());
    }
}
