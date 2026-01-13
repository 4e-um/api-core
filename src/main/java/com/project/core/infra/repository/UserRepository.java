package com.project.core.infra.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.core.infra.entity.user.User;

public interface UserRepository extends JpaRepository<User, Long> {
//  private final ExampleJpaRepository exampleJpaRepository;
//
//  public ExampleEntity find(Long exampleId) {
//      return exampleJpaRepository.findById(exampleId)
//              .orElseThrow(() -> new ApplicationException(ExampleErrorCode.EXAMPLE_NOT_FOUND));
//  }
//
//  public void save(ExampleEntity example) {
//      exampleJpaRepository.save(example);
//  }
}
