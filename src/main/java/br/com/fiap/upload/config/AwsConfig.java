package br.com.fiap.upload.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;

import java.net.URI;

@Configuration
public class AwsConfig {

    @Value("${aws.region}")
    private String region;

    /**
     * Se AWS_ENDPOINT_OVERRIDE estiver definido (ex: LocalStack), usa endpoint local.
     * Em produção, a variável fica vazia e o SDK usa o endpoint padrão da AWS.
     */
    @Value("${aws.endpoint-override:}")
    private String endpointOverride;

    @Bean
    public S3Client s3Client() {
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(region));
        applyEndpointOverride(builder);
        return builder.build();
    }

    @Bean
    public SqsClient sqsClient() {
        SqsClientBuilder builder = SqsClient.builder()
                .region(Region.of(region));
        applyEndpointOverride(builder);
        return builder.build();
    }

    private void applyEndpointOverride(S3ClientBuilder builder) {
        if (endpointOverride != null && !endpointOverride.isBlank()) {
            builder.endpointOverride(URI.create(endpointOverride))
                   .credentialsProvider(localCredentialsProvider())
                   .forcePathStyle(true);
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }
    }

    private void applyEndpointOverride(SqsClientBuilder builder) {
        if (endpointOverride != null && !endpointOverride.isBlank()) {
            builder.endpointOverride(URI.create(endpointOverride))
                   .credentialsProvider(localCredentialsProvider());
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }
    }

    private StaticCredentialsProvider localCredentialsProvider() {
        String accessKey = System.getenv().getOrDefault("AWS_ACCESS_KEY_ID", "test");
        String secretKey = System.getenv().getOrDefault("AWS_SECRET_ACCESS_KEY", "test");
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
    }
}
