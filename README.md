# fiap-upload-service

Microsserviço responsável por:
- Receber diagramas de arquitetura (PNG, JPG, JPEG, PDF) via REST
- Armazenar arquivos no AWS S3
- Criar e gerenciar jobs de análise com controle de status
- Publicar mensagens na fila AWS SQS para acionar o processamento

**Responsável**: Pessoa 1

## Contrato de API

Documentação completa: [`fiap-infrastructure/docs/api/upload-service-api.yaml`](https://github.com/org/fiap-infrastructure/blob/main/docs/api/upload-service-api.yaml)

### Endpoints

| Método | Path | Descrição |
|---|---|---|
| `POST` | `/v1/uploads` | Upload de diagrama |
| `GET` | `/v1/jobs/{jobId}/status` | Status do processamento |

### Swagger UI (local)

http://localhost:8080/swagger-ui.html

## Desenvolvimento local

```bash
# Pré-requisito: infraestrutura rodando (ver fiap-infrastructure)
# Compilar e rodar
mvn spring-boot:run

# Rodar testes
mvn test

# Build completo
mvn clean verify
```

## Estrutura (Arquitetura Hexagonal)

```
src/main/java/br/com/fiap/upload/
├── domain/
│   ├── model/          # Job, JobStatus, DiagramFile
│   └── port/
│       ├── in/         # UploadDiagramUseCase, GetJobStatusUseCase
│       └── out/        # JobRepository, FileStoragePort, MessageQueuePort
├── application/
│   └── usecase/        # UploadDiagramUseCaseImpl, GetJobStatusUseCaseImpl
├── adapter/
│   ├── in/
│   │   ├── web/        # UploadController
│   │   └── dto/        # UploadRequest, UploadResponse, JobStatusResponse
│   └── out/
│       ├── persistence/ # JobJpaRepository, JobEntity
│       └── aws/         # S3FileStorageAdapter, SqsMessageQueueAdapter
└── config/             # AwsConfig, OpenApiConfig
```

## Variáveis de ambiente

| Variável | Padrão (local) | Descrição |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/upload_db` | URL do banco |
| `SPRING_DATASOURCE_USERNAME` | `upload_user` | Usuário do banco |
| `SPRING_DATASOURCE_PASSWORD` | `upload_pass` | Senha do banco |
| `AWS_REGION` | `us-east-1` | Região AWS |
| `AWS_ENDPOINT_OVERRIDE` | *(vazio)* | URL do LocalStack para dev local |
| `S3_BUCKET_NAME` | `fiap-diagrams` | Nome do bucket S3 |
| `SQS_QUEUE_URL` | `http://localhost:4566/...` | URL da fila SQS |
