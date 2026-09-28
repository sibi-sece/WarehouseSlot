package com.warehouseslot.warehouse.controller;

import com.warehouseslot.warehouse.dto.BinRequest;
import com.warehouseslot.warehouse.entity.WarehouseBin;
import com.warehouseslot.warehouse.service.BinService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bins")
public class BinController {
    private final BinService service;

    public BinController(BinService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WarehouseBin create(@RequestBody BinRequest request) { return service.create(request); }

    @GetMapping
    public List<WarehouseBin> all() { return service.all(); }

    @GetMapping("/{id}")
    public WarehouseBin get(@PathVariable Long id) { return service.get(id); }

    @GetMapping("/{id}/utilization")
    public Map<String, Object> utilization(@PathVariable Long id) {
        WarehouseBin bin = service.get(id);
        return Map.of(
                "binCode", bin.getBinCode(),
                "capacity", bin.getCapacity(),
                "currentOccupancy", bin.getCurrentOccupancy(),
                "utilizationPercent", service.utilization(id)
        );
    }
}
