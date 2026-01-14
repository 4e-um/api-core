package com.project.core.infra.entity.vas;

import com.project.core.infra.entity.subscription.Subscription;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class SubscriptionVas {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "sv_id")
  private Long svId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sub_id", nullable = false)
  private Subscription subscription;
}
