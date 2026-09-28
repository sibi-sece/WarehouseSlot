package com.warehouseslot.warehouse.controller;

import com.warehouseslot.warehouse.dto.OrderRequest;
import com.warehouseslot.warehouse.dto.PickLine;
import com.warehouseslot.warehouse.entity.CustomerOrder;
import com.warehouseslot.warehouse.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerOrder create(
            @RequestBody OrderRequest request) {

        return service.create(request);
    }

    @GetMapping
    public List<CustomerOrder> all() {
        return service.all();
    }

    @GetMapping("/{id}")
    public CustomerOrder get(
            @PathVariable Long id) {

        return service.get(id);
    }

    @GetMapping("/{id}/picklist")
    public List<PickLine> picklist(
            @PathVariable Long id) {

        return service.picklist(id);
    }

    @PostMapping("/{id}/pick")
    public List<PickLine> pick(
            @PathVariable Long id) {

        return service.pick(id);
    }
}