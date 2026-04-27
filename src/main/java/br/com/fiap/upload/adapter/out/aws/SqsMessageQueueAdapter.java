package br.com.fiap.upload.adapter.out.aws;

import br.com.fiap.upload.domain.model.Job;
import br.com.fiap.upload.domain.port.out.MessageQueuePort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.time.Instant;
import java.util.Map;

@Component
public class SqsMessageQueueAdapter implements MessageQueuePort {

    private static final Logger log = LoggerFactory.getLogger(SqsMessageQueueAdapter.class);

    private final SqsClient sqsClient;
    private final String queueUrl;
    private final ObjectMapper objectMapper;

    public SqsMessageQueueAdapter(SqsClient sqsClient,
                                  @Value("${aws.sqs.queue-url}") String queueUrl) {
        this.sqsClient = sqsClient;
        this.queueUrl = queueUrl;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Override
    public void publish(Job job) {
        String messageBody = buildMessageBody(job);

        SendMessageRequest request = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(messageBody)
                .build();

        sqsClient.sendMessage(request);
        log.info("Mensagem SQS enviada. jobId={}, queueUrl={}", job.getId(), queueUrl);
    }

    private String buildMessageBody(Job job) {
        // Formato definido em fiap-infrastructure/docs/schemas/sqs-message.json
        Map<String, Object> message = Map.of(
                "jobId", job.getId().toString(),
                "s3Key", job.getS3Key(),
                "fileType", job.getFileType(),
                "fileSize", job.getFileSizeBytes(),
                "originalFilename", job.getOriginalFilename(),
                "description", job.getDescription() != null ? job.getDescription() : "",
                "publishedAt", Instant.now().toString()
        );

        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao serializar mensagem SQS para jobId: " + job.getId(), e);
        }
    }
}
