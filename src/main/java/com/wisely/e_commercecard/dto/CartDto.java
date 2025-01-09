package com.wisely.e_commercecard.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;
@Data
public class CartDto {
    private Long cartId;
    private Set<CartItemDto> cartItemDto;
    private BigDecimal totalAmount;
}
