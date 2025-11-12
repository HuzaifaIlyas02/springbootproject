package com.huzaifaproject.orderservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "t_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String orderNumber;
    private String username; // User who placed the order
    private LocalDateTime orderDate;
    
    @OneToMany(cascade = CascadeType.ALL)
    private List<OrderLineItems> orderLineItemsList;
    
    // Delivery Details
    private String deliveryAddress;
    private String phoneNumber;
    private String email;
    
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
}
