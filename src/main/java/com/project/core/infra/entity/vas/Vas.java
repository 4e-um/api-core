package com.project.core.infra.entity.vas;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "vas")
public class Vas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vas_id")
    private Long vasId;

    @Column(name = "name", length = 30)
    private String name;

    @Column(name = "monthly_fee")
    private Integer monthlyFee;
}
