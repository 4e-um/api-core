package com.project.core.infra.repository;

import com.project.core.infra.entity.subscription.Subscription;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
//    private final ExampleJpaRepository exampleJpaRepository;
//
//    public ExampleEntity find(Long exampleId) {
//        return exampleJpaRepository.findById(exampleId)
//                .orElseThrow(() -> new ApplicationException(ExampleErrorCode.EXAMPLE_NOT_FOUND));
//    }
//
//    public void save(ExampleEntity example) {
//        exampleJpaRepository.save(example);
//    }
}
