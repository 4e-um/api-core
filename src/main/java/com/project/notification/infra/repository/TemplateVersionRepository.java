package com.project.notification.infra.repository;

import com.project.notification.infra.entity.TemplateVersion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemplateVersionRepository extends JpaRepository<TemplateVersion, Long> {
}
