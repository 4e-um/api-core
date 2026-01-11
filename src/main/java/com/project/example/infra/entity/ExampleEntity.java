package com.project.example.infra.entity;

import com.project.example.controller.dto.SaveExampleRequest;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "EXAMPLE")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExampleEntity {

    @Id @GeneratedValue
    private Long exampleId;

    private String exampleName;

    private String exampleContent;

    public static ExampleEntity create(SaveExampleRequest request) {
        return ExampleEntity.builder()
                .exampleName(request.exampleName())
                .exampleContent(request.exampleContent())
                .build();
    }
}
