package com.example.ct_order_demo.controller;

import com.commercetools.api.client.ProjectApiRoot;
import com.commercetools.api.models.customer.CustomerSignInResult;
import com.commercetools.api.models.order.Order;
import com.example.ct_order_demo.model.OrderRequest;
import com.example.ct_order_demo.security.JwtService;
import com.example.ct_order_demo.service.OrderService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService OrderService;
    private final JwtService jwtService;

    public OrderController(OrderService OrderService, JwtService jwtService) {
        this.OrderService = OrderService;
        this.jwtService = jwtService;
    }


    @PostMapping
    public Order createOrder(@RequestHeader("Authorization") String authHeader, @RequestBody OrderRequest request) {

        String token = authHeader.replace("Bearer", "");

        String customerId = jwtService.extractCustomerId(token);
        request.customerId = customerId;
        return OrderService.createFullOrder(request);
    }

}