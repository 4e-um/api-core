package com.project.core.controller.dto.response;

import java.util.List;

public record CustomerSearchResponse(
        Long customerId, String name, List<SubscriptionResponse> subscriptions) {}
