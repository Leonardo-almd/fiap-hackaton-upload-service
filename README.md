# fiap-hackaton-upload-service

Microsserviço responsável por:
- Receber diagramas de arquitetura (PNG, JPG, JPEG, PDF) via REST
- Armazenar arquivos no AWS S3
- Criar e gerenciar jobs de análise com controle de status
- Publicar mensagens na fila AWS SQS para acionar o processamento

**Responsável**: Pessoa 1

## Contrato de API

Documentação completa: [`fiap-hackaton-infrastructure/docs/api/upload-service-api.yaml`](https://github.com/org/fiap-hackaton-infrastructure/blob/main/docs/api/upload-service-api.yaml)

### Endpoints

| Método | Path | Descrição |
|---|---|---|
| `POST` | `/v1/uploads` | Upload de diagrama |
| `GET` | `/v1/jobs/{jobId}/status` | Status do processamento |

### Swagger UI (local)

http://localhost:8080/swagger-ui.html

## Desenvolvimento local

```bash
# Pré-requisito: infraestrutura rodando (ver fiap-hackaton-infrastructure)
# Compilar e rodar
mvn spring-boot:run

# Rodar testes
mvn test

# Build completo
mvn clean verify
```

## Como rodar com Docker Compose

Este serviço eh executado via `docker compose` no repositório de infraestrutura (`fiap-hackaton-infrastructure`).
O Compose sobe:
- `localstack` (S3 + SQS)
- `upload-db` (PostgreSQL)
- `upload-service` (esta API, usando este `Dockerfile`)

### 1) Pre-requisitos

- Docker Desktop ativo
- Docker Compose v2 (`docker compose version`)
- Repositórios como pastas irmãs no mesmo diretório:

```text
Hackaton/
├── fiap-hackaton-upload-service/
├── fiap-hackaton-processing-service/
├── fiap-hackaton-report-service/
└── fiap-hackaton-infrastructure/
```

### 2) Subir dependências e API

No diretório `fiap-hackaton-infrastructure`:

```bash
cd ../fiap-hackaton-infrastructure

# Sobe os serviços necessários para o upload-service
docker compose up -d localstack upload-db upload-service
```

### 3) Verificar se tudo subiu corretamente

```bash
docker compose ps
```

Estado esperado:
- `fiap-localstack`: `healthy`
- `fiap-upload-db`: `healthy`
- `fiap-upload-service`: `healthy` (porta `8080:8080`)

Healthcheck da API:

```bash
curl http://localhost:8080/actuator/health
```

Resposta esperada:

```json
{
  "status": "UP"
}
```

Swagger local:
- http://localhost:8080/swagger-ui.html

### 4) Teste rapido de uso (simulando usuario)

Upload de arquivo:

```bash
echo "PNG_DATA_SIMULADO" > /tmp/diagrama.png

curl -X POST "http://localhost:8080/v1/uploads" \
  -F "file=@/tmp/diagrama.png;type=image/png" \
  -F "description=Diagrama de arquitetura"
```

Consultar status:

```bash
curl "http://localhost:8080/v1/jobs/<jobId>/status"
```

### 5) Rebuild da API apos alterar codigo

Sempre execute no `fiap-hackaton-infrastructure`:

```bash
docker compose up -d --build upload-service
```

### 6) Parar ambiente

```bash
docker compose stop upload-service upload-db localstack
```

Para remover containers e volumes (reset completo do ambiente):

```bash
docker compose down -v --remove-orphans
```

### 7) Troubleshooting

#### Erro de Flyway: schema nao vazio sem `flyway_schema_history`

Sintoma:
- API sobe e cai logo na inicializacao
- Mensagem com `Found non-empty schema(s) "public" but no schema history table`

Causa:
- Banco foi inicializado com schema legado e depois Flyway tentou assumir controle.

Como resolver:

```bash
cd ../fiap-hackaton-infrastructure
docker compose down -v --remove-orphans
docker compose up -d localstack upload-db upload-service
```

#### Erro de porta ocupada (`bind: address already in use` na 8080)

Causa:
- Ja existe outra instancia da API rodando (ex.: `mvn spring-boot:run`).

Como resolver:

```bash
# pare a instancia local que ocupa a 8080
pkill -f "spring-boot:run"

# suba novamente pelo compose
docker compose up -d upload-service
```

#### Confirmar que o Compose esta usando o Dockerfile correto

No `fiap-hackaton-infrastructure/docker-compose.yml`, o servico esta configurado assim:

```yaml
upload-service:
  build:
    context: ../fiap-hackaton-upload-service
    dockerfile: Dockerfile
```

Ou seja, sim: o arquivo `fiap-hackaton-upload-service/Dockerfile` eh o Dockerfile usado pelo Compose.

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
