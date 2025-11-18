package com.huzaifaproject.orderservice.controller;

import com.huzaifaproject.orderservice.dto.OrderRequest;
import com.huzaifaproject.orderservice.dto.OrderResponse;
import com.huzaifaproject.orderservice.security.JwtUsernameResolver;
import com.huzaifaproject.orderservice.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final JwtUsernameResolver jwtUsernameResolver;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
    @TimeLimiter(name = "inventory")
    @Retry(name = "inventory")
    public CompletableFuture<String> placeOrder(@RequestBody OrderRequest orderRequest, HttpServletRequest request) {
        log.info("Placing Order");
        String username = jwtUsernameResolver.resolveUsername(request);
        return CompletableFuture.supplyAsync(() -> orderService.placeOrder(orderRequest, username));
    }
    
    @GetMapping("/history")
    @ResponseStatus(HttpStatus.OK)
    public List<OrderResponse> getMyOrders(HttpServletRequest request) {
        log.info("Getting my order history");
        String username = jwtUsernameResolver.resolveUsername(request);
        return orderService.getMyOrders(username);
    }
    
    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    public List<OrderResponse> getAllOrders() {
        log.info("Getting all orders (admin)");
        return orderService.getAllOrders();
    }

    public CompletableFuture<String> fallbackMethod(OrderRequest orderRequest, RuntimeException runtimeException) {
        log.info("Cannot Place Order Executing Fallback logic");
        return CompletableFuture.supplyAsync(() -> "Oops! Something went wrong, please order after some time!");
    }
}
