package com.project.notification.infra.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.notification.infra.entity.TemplateGroup;

public interface TemplateGroupRepository extends JpaRepository<TemplateGroup, Long> {
    boolean existsByCode(String code);

    Optional<TemplateGroup> findByIdAndIsDeletedFalse(Long id);

    @Query(
            """
            select g from TemplateGroup g
            where (:includeDeleted = true or g.isDeleted = false)
              and (:isActive is null or g.isActive = :isActive)
              and (
                    :keyword is null
                    or lower(g.name) like lower(concat('%', :keyword, '%'))
                    or lower(coalesce(g.description, '')) like lower(concat('%', :keyword, '%'))
              )
            """)
    Page<TemplateGroup> search(
            @Param("isActive") Boolean isActive,
            @Param("includeDeleted") boolean includeDeleted,
            @Param("keyword") String keyword,
            Pageable pageable);
}
