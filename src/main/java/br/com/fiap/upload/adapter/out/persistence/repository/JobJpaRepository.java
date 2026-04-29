package br.com.fiap.upload.adapter.out.persistence.repository;

import br.com.fiap.upload.adapter.out.persistence.entity.JobEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JobJpaRepository extends JpaRepository<JobEntity, UUID> {}
