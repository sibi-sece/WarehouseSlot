package com.warehouseslot.warehouse.controller;

import com.warehouseslot.warehouse.repository.InventoryRepository;
import com.warehouseslot.warehouse.repository.ProductRepository;
import com.warehouseslot.warehouse.repository.WarehouseBinRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ProductRepository productRepository;
    private final WarehouseBinRepository binRepository;
    private final InventoryRepository inventoryRepository;

    public ReportController(ProductRepository productRepository,
                            WarehouseBinRepository binRepository,
                            InventoryRepository inventoryRepository) {
        this.productRepository = productRepository;
        this.binRepository = binRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        int totalCapacity = binRepository.findAll().stream().mapToInt(b -> b.getCapacity()).sum();
        int occupied = binRepository.findAll().stream().mapToInt(b -> b.getCurrentOccupancy()).sum();

        return Map.of(
                "products", productRepository.count(),
                "bins", binRepository.count(),
                "inventoryRecords", inventoryRepository.count(),
                "totalCapacity", totalCapacity,
                "occupiedCapacity", occupied,
                "freeCapacity", totalCapacity - occupied,
                "utilizationPercent", totalCapacity == 0 ? 0 : (occupied * 100.0) / totalCapacity
        );
    }

    @GetMapping("/products")
    public List<Map<String, Object>> products() {
        return productRepository.findAll().stream().map(p -> {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("id", p.getId());
            result.put("sku", p.getSku());
            result.put("name", p.getName());
            result.put("velocity", p.getVelocity());
            result.put("active", p.isActive());
            result.put("stock", inventoryRepository.findByProductId(p.getId()).stream()
                    .mapToInt(i -> i.getQuantity()).sum());
            return result;
        }).toList();
    }

    @GetMapping("/bins")
    public List<Map<String, Object>> bins() {
        return binRepository.findAll().stream().map(b -> {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("id", b.getId());
            result.put("binCode", b.getBinCode());
            result.put("zone", b.getZone());
            result.put("capacity", b.getCapacity());
            result.put("occupied", b.getCurrentOccupancy());
            result.put("available", b.getAvailableCapacity());
            result.put("utilizationPercent", b.getCapacity() == 0 ? 0.0 : (b.getCurrentOccupancy() * 100.0) / b.getCapacity());
            return result;
        }).toList();
    }
}
