package br.com.fiap.upload.application.usecase;

import br.com.fiap.upload.domain.model.Job;
import br.com.fiap.upload.domain.port.in.UploadDiagramUseCase;
import br.com.fiap.upload.domain.port.out.FileStoragePort;
import br.com.fiap.upload.domain.port.out.JobRepository;
import br.com.fiap.upload.domain.port.out.MessageQueuePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class UploadDiagramUseCaseImpl implements UploadDiagramUseCase {

    private static final Logger log = LoggerFactory.getLogger(UploadDiagramUseCaseImpl.class);

    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024L; // 10 MB
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/png", "image/jpeg", "application/pdf"
    );
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg", "pdf");

    private final FileStoragePort fileStoragePort;
    private final JobRepository jobRepository;
    private final MessageQueuePort messageQueuePort;

    public UploadDiagramUseCaseImpl(FileStoragePort fileStoragePort,
                                    JobRepository jobRepository,
                                    MessageQueuePort messageQueuePort) {
        this.fileStoragePort = fileStoragePort;
        this.jobRepository = jobRepository;
        this.messageQueuePort = messageQueuePort;
    }

    @Override
    public Job execute(Command command) {
        validateFile(command);

        String fileType = resolveFileType(command.contentType(), command.originalFilename());

        // Cria o job com ID antes do upload para usar o UUID como key no S3
        Job job = Job.create("pending", command.originalFilename(), fileType,
                command.fileSizeBytes(), command.description());

        MDC.put("jobId", job.getId().toString());
        log.info("Iniciando upload do diagrama. arquivo={}, tamanho={}bytes",
                command.originalFilename(), command.fileSizeBytes());

        String s3Key = fileStoragePort.store(
                command.fileContent(),
                job.getId().toString(),
                command.originalFilename(),
                command.contentType()
        );

        // Reconstrói o job com a s3Key real
        Job jobWithKey = new Job(
                job.getId(), job.getStatus(), s3Key,
                job.getOriginalFilename(), job.getFileType(), job.getFileSizeBytes(),
                job.getDescription(), null, null, job.getCreatedAt(), job.getUpdatedAt()
        );

        Job savedJob = jobRepository.save(jobWithKey);
        log.info("Job criado com sucesso. jobId={}, s3Key={}", savedJob.getId(), s3Key);

        messageQueuePort.publish(savedJob);
        log.info("Mensagem publicada na fila SQS. jobId={}", savedJob.getId());

        MDC.remove("jobId");
        return savedJob;
    }

    private void validateFile(Command command) {
        if (command.fileSizeBytes() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException(
                    "Arquivo excede o tamanho máximo permitido de 10MB. Tamanho recebido: "
                            + command.fileSizeBytes() + " bytes");
        }

        boolean validContentType = ALLOWED_CONTENT_TYPES.contains(command.contentType());
        boolean validExtension = command.originalFilename() != null &&
                ALLOWED_EXTENSIONS.contains(extractExtension(command.originalFilename()));

        if (!validContentType || !validExtension) {
            throw new IllegalArgumentException(
                    "Tipo de arquivo não suportado: " + command.contentType() +
                            ". Formatos aceitos: PNG, JPG, JPEG, PDF.");
        }
    }

    private String resolveFileType(String contentType, String filename) {
        if ("application/pdf".equals(contentType)) return "PDF";
        String ext = extractExtension(filename).toUpperCase();
        return ext.isEmpty() ? "UNKNOWN" : ext;
    }

    private String extractExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) return "";
        return filename.substring(dotIndex + 1).toLowerCase();
    }
}
