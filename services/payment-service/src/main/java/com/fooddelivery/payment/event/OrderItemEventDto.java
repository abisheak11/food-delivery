package com.fooddelivery.payment.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemEventDto implements Serializable {
    private Long id;
    private Long menuItemId;
    private String itemName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}
