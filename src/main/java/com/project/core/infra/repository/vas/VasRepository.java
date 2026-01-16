package com.project.core.infra.repository.vas;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.core.infra.entity.vas.Vas;

public interface VasRepository extends JpaRepository<Vas, Long> {}
