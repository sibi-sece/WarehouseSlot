package com.warehouseslot.warehouse.controller;

import com.warehouseslot.warehouse.dto.ReceiveRequest;
import com.warehouseslot.warehouse.service.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    private final InventoryService service;

    public InventoryController(InventoryService service) { this.service = service; }

    @PostMapping("/receive")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> receive(@RequestBody ReceiveRequest request) {
        return service.receive(request);
    }

    @GetMapping
    public List<Map<String, Object>> all() { return service.all(); }

    @GetMapping("/product/{productId}")
    public Object byProduct(@PathVariable Long productId) {
        return service.byProduct(productId);
    }
}
