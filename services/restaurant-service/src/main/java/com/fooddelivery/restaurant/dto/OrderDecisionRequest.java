package com.fooddelivery.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDecisionRequest {

    @NotBlank(message = "Decision is required")
    @Pattern(regexp = "ACCEPTED|REJECTED", message = "Decision must be either ACCEPTED or REJECTED")
    private String decision;

    private String reason;
}
