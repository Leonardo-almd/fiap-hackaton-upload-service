package br.com.fiap.upload.integration;

import br.com.fiap.upload.domain.model.Job;
import br.com.fiap.upload.domain.model.JobStatus;
import br.com.fiap.upload.domain.port.in.GetJobStatusUseCase;
import br.com.fiap.upload.domain.port.in.UpdateJobStatusUseCase;
import br.com.fiap.upload.domain.port.in.UploadDiagramUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.com.fiap.upload.adapter.in.web.UploadController;

@WebMvcTest(UploadController.class)
class UploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private UploadDiagramUseCase uploadDiagramUseCase;
    @MockBean private GetJobStatusUseCase getJobStatusUseCase;
    @MockBean private UpdateJobStatusUseCase updateJobStatusUseCase;

    private Job buildJob(UUID id, JobStatus status) {
        return new Job(id, status, "uploads/file.pdf", "diagram.pdf", "PDF",
                1024L, "Teste", null, null, Instant.now(), Instant.now());
    }

    @Test
    void postUploadShouldReturn202WhenValid() throws Exception {
        UUID jobId = UUID.randomUUID();
        when(uploadDiagramUseCase.execute(any())).thenReturn(buildJob(jobId, JobStatus.RECEBIDO));

        MockMultipartFile file = new MockMultipartFile(
                "file", "diagram.pdf", "application/pdf", "fake-pdf".getBytes());

        mockMvc.perform(multipart("/v1/uploads")
                        .file(file)
                        .param("description", "Meu diagrama"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId").value(jobId.toString()))
                .andExpect(jsonPath("$.status").value("RECEBIDO"));
    }

    @Test
    void postUploadShouldReturn400ForUnsupportedFile() throws Exception {
        when(uploadDiagramUseCase.execute(any()))
                .thenThrow(new IllegalArgumentException("Tipo de arquivo não suportado"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "virus.exe", "application/octet-stream", "bad".getBytes());

        mockMvc.perform(multipart("/v1/uploads").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Tipo de arquivo não suportado"));
    }

    @Test
    void getStatusShouldReturn200WhenJobExists() throws Exception {
        UUID jobId = UUID.randomUUID();
        when(getJobStatusUseCase.execute(jobId)).thenReturn(buildJob(jobId, JobStatus.EM_PROCESSAMENTO));

        mockMvc.perform(get("/v1/jobs/{jobId}/status", jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value(jobId.toString()))
                .andExpect(jsonPath("$.status").value("EM_PROCESSAMENTO"));
    }

    @Test
    void getStatusShouldReturn404WhenJobNotFound() throws Exception {
        UUID jobId = UUID.randomUUID();
        when(getJobStatusUseCase.execute(jobId))
                .thenThrow(new NoSuchElementException("Job não encontrado com id: " + jobId));

        mockMvc.perform(get("/v1/jobs/{jobId}/status", jobId))
                .andExpect(status().isNotFound());
    }

    @Test
    void patchStatusShouldReturn200WhenValid() throws Exception {
        UUID jobId = UUID.randomUUID();
        when(updateJobStatusUseCase.execute(any()))
                .thenReturn(buildJob(jobId, JobStatus.EM_PROCESSAMENTO));

        mockMvc.perform(patch("/v1/jobs/{jobId}/status", jobId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"EM_PROCESSAMENTO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_PROCESSAMENTO"));
    }
}
