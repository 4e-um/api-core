package com.project.example.service;

import com.project.example.controller.dto.SaveExampleRequest;
import com.project.example.infra.entity.ExampleEntity;
import com.project.example.infra.repository.ExampleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExampleService {

    private final ExampleRepository exampleRepository;

    @Transactional
    public ExampleEntity find(Long exampleId) {
        return exampleRepository.find(exampleId);
    }

    @Transactional
    public void save(SaveExampleRequest request) {
        ExampleEntity exampleEntity = ExampleEntity.create(request);
        exampleRepository.save(exampleEntity);
    }
}
