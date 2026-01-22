package com.project.notification.infra.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.notification.infra.entity.TemplateVersion;
import com.project.notification.infra.entity.enums.Channel;
import com.project.notification.infra.entity.enums.TemplateStatus;

public interface TemplateVersionRepository extends JpaRepository<TemplateVersion, Long> {
    @Query(
            """
            select coalesce(max(v.version), 0)
            from TemplateVersion v
            where v.templateGroup.id = :groupId
              and v.channel = :channel
            """)
    int findMaxVersion(@Param("groupId") Long groupId, @Param("channel") Channel channel);

    @Query(
            """
            select v
            from TemplateVersion v
            where v.templateGroup.id = :groupId
              and v.channel = :channel
              and v.status = com.project.notification.infra.entity.enums.TemplateStatus.ACTIVE
              and v.isDeleted = false
            """)
    Optional<TemplateVersion> findActiveVersion(
            @Param("groupId") Long groupId, @Param("channel") Channel channel);

    Optional<TemplateVersion> findByIdAndTemplateGroupId(Long id, Long groupId);

    List<TemplateVersion> findAllByTemplateGroupId(Long groupId);

    @Query(
            """
            select v
            from TemplateVersion v
            where v.templateGroup.id = :groupId
              and (:channel is null or v.channel = :channel)
              and (:status is null or v.status = :status)
              and (:includeDeleted = true or v.isDeleted = false)
            """)
    Page<TemplateVersion> search(
            @Param("groupId") Long groupId,
            @Param("channel") Channel channel,
            @Param("status") TemplateStatus status,
            @Param("includeDeleted") boolean includeDeleted,
            Pageable pageable);
}
