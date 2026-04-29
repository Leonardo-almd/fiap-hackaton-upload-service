package br.com.fiap.upload.adapter.out.persistence;

import br.com.fiap.upload.adapter.out.persistence.entity.JobEntity;
import br.com.fiap.upload.adapter.out.persistence.repository.JobJpaRepository;
import br.com.fiap.upload.domain.model.Job;
import br.com.fiap.upload.domain.port.out.JobRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class JobRepositoryAdapter implements JobRepository {

    private final JobJpaRepository jpaRepository;

    public JobRepositoryAdapter(JobJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Job save(Job job) {
        JobEntity saved = jpaRepository.save(JobEntity.from(job));
        return saved.toDomain();
    }

    @Override
    public Optional<Job> findById(UUID id) {
        return jpaRepository.findById(id).map(JobEntity::toDomain);
    }
}
