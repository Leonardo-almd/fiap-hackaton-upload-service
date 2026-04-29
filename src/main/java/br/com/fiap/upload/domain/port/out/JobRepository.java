package br.com.fiap.upload.domain.port.out;

import br.com.fiap.upload.domain.model.Job;

import java.util.Optional;
import java.util.UUID;

public interface JobRepository {

    Job save(Job job);

    Optional<Job> findById(UUID id);
}
