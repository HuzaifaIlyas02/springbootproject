package com.huzaifaproject.productservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DecreaseQuantityRequest {
    private String skuCode;
    private BigDecimal price;
    private Integer quantity;
}
