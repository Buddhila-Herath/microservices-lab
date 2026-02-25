package com.example.orderservice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private List<Order> orders = new ArrayList<>();
    private AtomicInteger idCounter = new AtomicInteger(1);

    @GetMapping
    public List<Order> getAllOrders() {
        return orders;
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        if (order.getItem() == null || order.getQuantity() <= 0) {
            return ResponseEntity.badRequest().build();
        }
        order.setId(idCounter.getAndIncrement());
        orders.add(order);
        return ResponseEntity.status(201).body(order);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable int id) {
        return orders.stream()
                .filter(o -> o.getId() == id)
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
