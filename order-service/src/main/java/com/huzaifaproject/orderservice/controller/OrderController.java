package com.huzaifaproject.orderservice.controller;

import com.huzaifaproject.orderservice.dto.OrderRequest;
import com.huzaifaproject.orderservice.dto.OrderResponse;
import com.huzaifaproject.orderservice.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
    @TimeLimiter(name = "inventory")
    @Retry(name = "inventory")
    public CompletableFuture<String> placeOrder(@RequestBody OrderRequest orderRequest) {
        log.info("Placing Order");
        return CompletableFuture.supplyAsync(new java.util.function.Supplier<String>() {
            public String get() {
                return orderService.placeOrder(orderRequest);
            }
        });
    }
    
    @GetMapping("/history")
    @ResponseStatus(HttpStatus.OK)
    public List<OrderResponse> getMyOrders() {
        log.info("Getting my order history");
        return orderService.getMyOrders();
    }
    
    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    public List<OrderResponse> getAllOrders() {
        log.info("Getting all orders (admin)");
        return orderService.getAllOrders();
    }

    public CompletableFuture<String> fallbackMethod(OrderRequest orderRequest, RuntimeException runtimeException) {
        log.info("Cannot Place Order Executing Fallback logic");
        return CompletableFuture.supplyAsync(new java.util.function.Supplier<String>() {
            public String get() {
                return "Oops! Something went wrong, please order after some time!";
            }
        });
    }
}
