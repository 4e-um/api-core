package com.project.core.controller.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlanChangeResponse {
  private Long subId;
  private Long oldPlanId;
  private Long newPlanId;
  private LocalDateTime changedAt;
}
