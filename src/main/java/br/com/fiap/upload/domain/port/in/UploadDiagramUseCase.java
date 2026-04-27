package br.com.fiap.upload.domain.port.in;

import br.com.fiap.upload.domain.model.Job;

public interface UploadDiagramUseCase {

    record Command(byte[] fileContent, String originalFilename,
                   String contentType, Long fileSizeBytes, String description) {}

    Job execute(Command command);
}
