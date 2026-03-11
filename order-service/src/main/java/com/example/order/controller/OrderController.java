package com.example.order.controller;

import com.example.order.entity.OrderEntity;
import com.example.order.repository.OrderRepository;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderRepository orderRepository;

    public OrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @PostMapping
    public OrderEntity createOrder(@RequestBody OrderEntity order) {
        return orderRepository.save(order);
    }

    @GetMapping
    public List<OrderEntity> getOrders() {
        return orderRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderEntity> getOrderById(@PathVariable Long id) {
        return orderRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
