package com.project.example.infra.repository;

import com.project.example.infra.entity.ExampleEntity;

public interface ExampleRepository {

    ExampleEntity find(Long exampleId);

    void save(ExampleEntity exampleEntity);
}
