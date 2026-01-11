package com.project.example.infra.repository;

import com.project.example.infra.entity.ExampleEntity;
import com.project.global.exception.ApplicationException;
import com.project.global.exception.code.domain.ExampleErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ExampleRepositoryImpl implements ExampleRepository{

    private final ExampleJpaRepository exampleJpaRepository;

    public ExampleEntity find(Long exampleId) {
        return exampleJpaRepository.findById(exampleId)
                .orElseThrow(() -> new ApplicationException(ExampleErrorCode.EXAMPLE_NOT_FOUND));
    }

    public void save(ExampleEntity example) {
        exampleJpaRepository.save(example);
    }
}
