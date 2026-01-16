package com.project.core.infra.entity.micro;

import com.project.core.infra.entity.micro.enums.MicroPaymentStatus;
import com.project.core.infra.entity.subscription.Subscription;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "micro_payment")
public class MicroPayment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "micro_id")
  private Long microId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sub_id", nullable = false)
  private Subscription subscription;

  @Column(name = "name", nullable = false, length = 30)
  private String name;

  @Column(name = "amount", nullable = false)
  private Integer amount;

  @Column(name = "pay_date", nullable = false)
  private LocalDateTime payDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 10)
  private MicroPaymentStatus status;

  @Builder
  public MicroPayment(Subscription subscription, String name, Integer amount, Clock clock) {
    this.subscription = subscription;
    this.name = name;
    this.amount = amount;
    this.payDate = LocalDateTime.now(clock);
    this.status = MicroPaymentStatus.BILLED;
  }

  public void cancel(Long requesterSubId) {
    // 결제 내역의 주인과 요청자가 다르면 예외 발생
    if (!this.subscription.getSubId().equals(requesterSubId)) {
      throw new com.project.global.exception.core.InvalidStateException(
          com.project.global.exception.code.domain.core.CoreErrorCode.MICRO_PAYMENT_BAD_REQUEST);
    }

    // 이미 취소된 건인지 확인
    if (this.status == MicroPaymentStatus.CANCELED) {
      throw new com.project.global.exception.core.InvalidStateException(
          com.project.global.exception.code.domain.core.CoreErrorCode
              .MICRO_PAYMENT_ALREADY_CANCELED);
    }
    this.status = MicroPaymentStatus.CANCELED;
  }
}
