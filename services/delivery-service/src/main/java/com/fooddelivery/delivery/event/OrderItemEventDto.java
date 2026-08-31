package com.fooddelivery.delivery.event;

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
    private String itemName;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subTotal;
}
