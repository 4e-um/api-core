package com.project.core.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/example")
@RequiredArgsConstructor
public class ExampleController {

//    private final ExampleService exampleService;
//
//    @GetMapping("/{exampleId}")
//    public ResponseEntity<ExampleEntity> find(@PathVariable Long exampleId) {
//        return ResponseEntity.ok(exampleService.find(exampleId));
//    }
//
//    @PostMapping
//    public void save(@RequestBody SaveExampleRequest request) {
//        exampleService.save(request);
//    }
}
