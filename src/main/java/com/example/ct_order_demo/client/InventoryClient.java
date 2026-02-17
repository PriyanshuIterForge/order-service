package com.example.ct_order_demo.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "inventory-service", url = "http://localhost:8081")
public interface InventoryClient {

    @GetMapping("/api/inventory/check")
    Map<String, Object> checkStock(
            @RequestParam String skuCode,
            @RequestParam Integer quantity
    );
}