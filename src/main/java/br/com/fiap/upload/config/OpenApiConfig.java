package br.com.fiap.upload.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Upload Service API")
                        .description("Recebe diagramas de arquitetura, cria jobs e publica na fila SQS para análise automatizada.")
                        .version("1.0.0"));
    }
}
