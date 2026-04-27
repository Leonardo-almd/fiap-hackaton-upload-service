package br.com.fiap.upload.unit;

import br.com.fiap.upload.application.usecase.UploadDiagramUseCaseImpl;
import br.com.fiap.upload.domain.model.Job;
import br.com.fiap.upload.domain.model.JobStatus;
import br.com.fiap.upload.domain.port.in.UploadDiagramUseCase;
import br.com.fiap.upload.domain.port.out.FileStoragePort;
import br.com.fiap.upload.domain.port.out.JobRepository;
import br.com.fiap.upload.domain.port.out.MessageQueuePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadDiagramUseCaseTest {

    @Mock private FileStoragePort fileStoragePort;
    @Mock private JobRepository jobRepository;
    @Mock private MessageQueuePort messageQueuePort;

    private UploadDiagramUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new UploadDiagramUseCaseImpl(fileStoragePort, jobRepository, messageQueuePort);
    }

    @Test
    void shouldUploadPdfSuccessfully() {
        byte[] content = "fake-pdf-content".getBytes();
        var command = new UploadDiagramUseCase.Command(
                content, "diagram.pdf", "application/pdf", (long) content.length, "Meu diagrama");

        when(fileStoragePort.store(any(), anyString(), eq("diagram.pdf"), eq("application/pdf")))
                .thenReturn("uploads/test-uuid.pdf");
        when(jobRepository.save(any(Job.class))).thenAnswer(inv -> inv.getArgument(0));

        Job result = useCase.execute(command);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(JobStatus.RECEBIDO);
        assertThat(result.getFileType()).isEqualTo("PDF");
        assertThat(result.getOriginalFilename()).isEqualTo("diagram.pdf");
        verify(fileStoragePort).store(any(), anyString(), eq("diagram.pdf"), eq("application/pdf"));
        verify(jobRepository).save(any(Job.class));
        verify(messageQueuePort).publish(any(Job.class));
    }

    @Test
    void shouldUploadPngSuccessfully() {
        byte[] content = "fake-png".getBytes();
        var command = new UploadDiagramUseCase.Command(
                content, "arch.png", "image/png", (long) content.length, null);

        when(fileStoragePort.store(any(), anyString(), eq("arch.png"), eq("image/png")))
                .thenReturn("uploads/test-uuid.png");
        when(jobRepository.save(any(Job.class))).thenAnswer(inv -> inv.getArgument(0));

        Job result = useCase.execute(command);

        assertThat(result.getFileType()).isEqualTo("PNG");
        assertThat(result.getDescription()).isNull();
    }

    @Test
    void shouldRejectFileTooLarge() {
        long oversizedFile = 11 * 1024 * 1024L;
        var command = new UploadDiagramUseCase.Command(
                new byte[0], "big.pdf", "application/pdf", oversizedFile, null);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tamanho máximo");

        verifyNoInteractions(fileStoragePort, jobRepository, messageQueuePort);
    }

    @Test
    void shouldRejectUnsupportedFileType() {
        byte[] content = "fake".getBytes();
        var command = new UploadDiagramUseCase.Command(
                content, "diagram.exe", "application/octet-stream", (long) content.length, null);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tipo de arquivo não suportado");

        verifyNoInteractions(fileStoragePort, jobRepository, messageQueuePort);
    }

    @Test
    void shouldPublishMessageAfterSuccessfulUpload() {
        byte[] content = "fake".getBytes();
        var command = new UploadDiagramUseCase.Command(
                content, "d.pdf", "application/pdf", (long) content.length, null);

        when(fileStoragePort.store(any(), anyString(), anyString(), anyString()))
                .thenReturn("uploads/uuid.pdf");
        when(jobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(command);

        verify(messageQueuePort, times(1)).publish(any(Job.class));
    }
}
