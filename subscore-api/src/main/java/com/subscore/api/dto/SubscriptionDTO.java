package com.subscore.api.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record SubscriptionDTO(
        UUID id,
        UUID userId,
        CategoryDTO category,
        String name,
        Integer price,
        String billingCycle,
        Integer paymentDate,
        LocalDate nextPaymentDate,
        String status,
        Boolean notificationEnabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}