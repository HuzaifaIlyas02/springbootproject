package com.huzaifaproject.orderservice.repository;

import com.huzaifaproject.orderservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUsernameOrderByOrderDateDesc(String username);
    List<Order> findAllByOrderByOrderDateDesc();
}
