package com.project.example.infra.repository;

import com.project.example.infra.entity.ExampleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExampleJpaRepository extends JpaRepository<ExampleEntity, Long> {
}
