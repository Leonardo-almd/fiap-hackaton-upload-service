package br.com.fiap.upload.domain.port.out;

public interface FileStoragePort {

    /**
     * Armazena o arquivo no S3 e retorna a chave (key) do objeto criado.
     *
     * @param fileContent  conteúdo binário do arquivo
     * @param jobId        identificador do job (usado para compor a key)
     * @param filename     nome original do arquivo (usado para determinar extensão)
     * @param contentType  MIME type do arquivo
     * @return a chave S3 do objeto armazenado (ex: "uploads/uuid.pdf")
     */
    String store(byte[] fileContent, String jobId, String filename, String contentType);
}
