package com.huzaifaproject.orderservice.dto;

import com.huzaifaproject.orderservice.model.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private String username;
    private LocalDateTime orderDate;
    private List<OrderLineItemsDto> orderLineItemsList;
    private String deliveryAddress;
    private String phoneNumber;
    private String email;
    private PaymentMethod paymentMethod;
}
