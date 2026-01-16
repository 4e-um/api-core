package com.project.core.infra.entity.discount;

import com.project.core.infra.entity.discount.enums.Active;
import com.project.core.infra.entity.discount.enums.Category;
import com.project.core.infra.entity.discount.enums.DiscountType;
import com.project.core.infra.entity.discount.enums.TargetScope;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "discount_policy")
public class DiscountPolicy {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "discount_id")
  private Long discountId;

  @Column(name = "name", nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "discount_type", nullable = false, length = 10)
  private DiscountType discountType;

  @Column(name = "value", nullable = false)
  private BigDecimal value;

  @Enumerated(EnumType.STRING)
  @Column(name = "category", nullable = false, length = 10)
  private Category category;

  @Enumerated(EnumType.STRING)
  @Column(name = "target_scope", nullable = false, length = 20)
  private TargetScope targetScope;

  @Enumerated(EnumType.STRING)
  @Column(name = "active", nullable = false, length = 10)
  private Active active;

  @OneToMany(mappedBy = "discountPolicy")
  private List<SubscriptionDiscount> discountHistory = new ArrayList<>();
  
  @Builder
  private DiscountPolicy(
      String name,
      DiscountType discountType,
      BigDecimal value,
      Category category,
      TargetScope targetScope,
      Active active) {

    this.name = name;
    this.discountType = discountType;
    this.value = value;
    this.category = category;
    this.targetScope = targetScope;
    this.active = active;
  }
}
