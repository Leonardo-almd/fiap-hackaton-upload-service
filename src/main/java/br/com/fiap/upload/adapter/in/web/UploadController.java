package br.com.fiap.upload.adapter.in.web;

import br.com.fiap.upload.adapter.in.dto.JobStatusResponse;
import br.com.fiap.upload.adapter.in.dto.UpdateJobStatusRequest;
import br.com.fiap.upload.adapter.in.dto.UploadResponse;
import br.com.fiap.upload.domain.model.Job;
import br.com.fiap.upload.domain.port.in.GetJobStatusUseCase;
import br.com.fiap.upload.domain.port.in.UpdateJobStatusUseCase;
import br.com.fiap.upload.domain.port.in.UploadDiagramUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/v1")
@Tag(name = "Uploads", description = "Upload de diagramas e consulta de status de processamento")
public class UploadController {

    private final UploadDiagramUseCase uploadDiagramUseCase;
    private final GetJobStatusUseCase getJobStatusUseCase;
    private final UpdateJobStatusUseCase updateJobStatusUseCase;

    public UploadController(UploadDiagramUseCase uploadDiagramUseCase,
                            GetJobStatusUseCase getJobStatusUseCase,
                            UpdateJobStatusUseCase updateJobStatusUseCase) {
        this.uploadDiagramUseCase = uploadDiagramUseCase;
        this.getJobStatusUseCase = getJobStatusUseCase;
        this.updateJobStatusUseCase = updateJobStatusUseCase;
    }

    @PostMapping(value = "/uploads", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Fazer upload de um diagrama de arquitetura",
               description = "Aceita arquivos PNG, JPG, JPEG ou PDF com até 10MB.")
    public ResponseEntity<UploadResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description) throws IOException {

        var command = new UploadDiagramUseCase.Command(
                file.getBytes(),
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                description
        );

        Job job = uploadDiagramUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(UploadResponse.from(job));
    }

    @GetMapping("/jobs/{jobId}/status")
    @Operation(summary = "Consultar o status de processamento de um job")
    public ResponseEntity<JobStatusResponse> getStatus(@PathVariable UUID jobId) {
        Job job = getJobStatusUseCase.execute(jobId);
        return ResponseEntity.ok(JobStatusResponse.from(job));
    }

    @PatchMapping("/jobs/{jobId}/status")
    @Operation(summary = "Atualizar o status de um job (uso interno — processing-service)",
               description = "Endpoint interno chamado exclusivamente pelo processing-service.")
    public ResponseEntity<JobStatusResponse> updateStatus(
            @PathVariable UUID jobId,
            @Valid @RequestBody UpdateJobStatusRequest request) {

        var command = new UpdateJobStatusUseCase.Command(
                jobId, request.status(), request.errorMessage(), request.reportId());

        Job job = updateJobStatusUseCase.execute(command);
        return ResponseEntity.ok(JobStatusResponse.from(job));
    }
}
