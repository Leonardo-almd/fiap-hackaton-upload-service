package br.com.fiap.upload.domain.port.out;

import br.com.fiap.upload.domain.model.Job;

public interface MessageQueuePort {

    /**
     * Publica uma mensagem na fila SQS para acionar o processing-service.
     * O formato da mensagem segue o schema definido em docs/schemas/sqs-message.json.
     */
    void publish(Job job);
}
