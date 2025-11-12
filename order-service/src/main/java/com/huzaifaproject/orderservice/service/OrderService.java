package com.huzaifaproject.orderservice.service;

import com.huzaifaproject.orderservice.dto.InventoryResponse;
import com.huzaifaproject.orderservice.dto.OrderLineItemsDto;
import com.huzaifaproject.orderservice.dto.OrderRequest;
import com.huzaifaproject.orderservice.dto.OrderResponse;
import com.huzaifaproject.orderservice.model.Order;
import com.huzaifaproject.orderservice.model.OrderLineItems;
import com.huzaifaproject.orderservice.repository.OrderRepository;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
    private final ObservationRegistry observationRegistry;

    public String placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setOrderDate(LocalDateTime.now());
        
        // Get username from JWT token
        String username = getUsernameFromToken();
        order.setUsername(username);

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();

        order.setOrderLineItemsList(orderLineItems);
        
        // Set delivery details
        order.setDeliveryAddress(orderRequest.getDeliveryAddress());
        order.setPhoneNumber(orderRequest.getPhoneNumber());
        order.setEmail(orderRequest.getEmail());
        order.setPaymentMethod(orderRequest.getPaymentMethod());

        List<String> skuCodes = order.getOrderLineItemsList().stream()
                .map(OrderLineItems::getSkuCode)
                .toList();

        // Call Inventory Service, and place order if product is in stock
        Observation inventoryServiceObservation = Observation.createNotStarted("inventory-service-lookup",
                this.observationRegistry);
        inventoryServiceObservation.lowCardinalityKeyValue("call", "inventory-service");
        return inventoryServiceObservation.observe(() -> {
            InventoryResponse[] inventoryResponseArray = webClientBuilder.build().get()
                    .uri("http://inventory-service/api/inventory",
                            uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                    .retrieve()
                    .bodyToMono(InventoryResponse[].class)
                    .block();

            boolean allProductsInStock = Arrays.stream(inventoryResponseArray)
                    .allMatch(InventoryResponse::isInStock);

            if (allProductsInStock) {
                orderRepository.save(order);
                
                // Update product quantities in Product Service
                updateProductQuantities(orderRequest.getOrderLineItemsDtoList());
                
                return order.getOrderNumber();
            } else {
                throw new IllegalArgumentException("Product is not in stock, please try again later");
            }
        });
    }
    
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc().stream()
                .map(this::mapToOrderResponse)
                .toList();
    }
    
    public List<OrderResponse> getOrdersByUsername(String username) {
        return orderRepository.findByUsernameOrderByOrderDateDesc(username).stream()
                .map(this::mapToOrderResponse)
                .toList();
    }
    
    public List<OrderResponse> getMyOrders() {
        String username = getUsernameFromToken();
        return getOrdersByUsername(username);
    }
    
    private String getUsernameFromToken() {
        // For now, return a default username
        // In production, extract from JWT token
        return "user";
    }
    
    private void updateProductQuantities(List<OrderLineItemsDto> orderItems) {
        try {
            for (OrderLineItemsDto item : orderItems) {
                // Call Product Service to decrease quantity
                webClientBuilder.build().post()
                        .uri("http://product-service/api/product/decrease-quantity")
                        .bodyValue(item)
                        .retrieve()
                        .bodyToMono(Void.class)
                        .block();
            }
        } catch (Exception e) {
            log.error("Failed to update product quantities: {}", e.getMessage());
            // Don't fail the order, just log the error
        }
    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }
    
    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderLineItemsDto> orderLineItemsDtos = order.getOrderLineItemsList().stream()
                .map(item -> {
                    OrderLineItemsDto dto = new OrderLineItemsDto();
                    dto.setSkuCode(item.getSkuCode());
                    dto.setPrice(item.getPrice());
                    dto.setQuantity(item.getQuantity());
                    return dto;
                })
                .toList();
                
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .username(order.getUsername())
                .orderDate(order.getOrderDate())
                .orderLineItemsList(orderLineItemsDtos)
                .deliveryAddress(order.getDeliveryAddress())
                .phoneNumber(order.getPhoneNumber())
                .email(order.getEmail())
                .paymentMethod(order.getPaymentMethod())
                .build();
    }
}
