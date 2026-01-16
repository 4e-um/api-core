package com.project.core.infra.entity.plan;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.project.core.infra.entity.plan.enums.AllowancePeriod;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "plan")
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_id")
    private Long planId;

    @Column(name = "plan_name", nullable = false, length = 30)
    private String planName;

    @Column(name = "plan_base_fee", nullable = false)
    private Integer planBaseFee;

    @Column(name = "allowance_amount", nullable = false)
    private Long allowanceAmount; // MB 단위, -1은 무제한

    @Enumerated(EnumType.STRING)
    @Column(name = "allowance_period", nullable = false, length = 10)
    private AllowancePeriod allowancePeriod; // MONTH / DAY
}
