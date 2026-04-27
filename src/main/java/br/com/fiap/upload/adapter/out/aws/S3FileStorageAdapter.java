package br.com.fiap.upload.adapter.out.aws;

import br.com.fiap.upload.domain.port.out.FileStoragePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
public class S3FileStorageAdapter implements FileStoragePort {

    private static final Logger log = LoggerFactory.getLogger(S3FileStorageAdapter.class);

    private final S3Client s3Client;
    private final String bucketName;

    public S3FileStorageAdapter(S3Client s3Client,
                                @Value("${aws.s3.bucket-name}") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    @Override
    public String store(byte[] fileContent, String jobId, String filename, String contentType) {
        String extension = extractExtension(filename);
        String s3Key = "uploads/" + jobId + (extension.isEmpty() ? "" : "." + extension);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType(contentType)
                .contentLength((long) fileContent.length)
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(fileContent));
        log.info("Arquivo armazenado no S3. bucket={}, key={}", bucketName, s3Key);

        return s3Key;
    }

    private String extractExtension(String filename) {
        if (filename == null) return "";
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) return "";
        return filename.substring(dotIndex + 1).toLowerCase();
    }
}
