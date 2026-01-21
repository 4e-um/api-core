package com.project.notification.infra.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.notification.infra.entity.TemplateVersion;

public interface TemplateVersionRepository extends JpaRepository<TemplateVersion, Long> {}
