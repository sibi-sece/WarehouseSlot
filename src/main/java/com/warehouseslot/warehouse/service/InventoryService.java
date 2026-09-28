package com.warehouseslot.warehouse.service;

import com.warehouseslot.warehouse.dto.ReceiveRequest;
import com.warehouseslot.warehouse.entity.Inventory;
import com.warehouseslot.warehouse.entity.Product;
import com.warehouseslot.warehouse.entity.WarehouseBin;
import com.warehouseslot.warehouse.exception.ApiException;
import com.warehouseslot.warehouse.repository.InventoryRepository;
import com.warehouseslot.warehouse.repository.ProductRepository;
import com.warehouseslot.warehouse.repository.WarehouseBinRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final WarehouseBinRepository binRepository;
    private final SlottingService slottingService;

    public InventoryService(InventoryRepository inventoryRepository,
                            ProductRepository productRepository,
                            WarehouseBinRepository binRepository,
                            SlottingService slottingService) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.binRepository = binRepository;
        this.slottingService = slottingService;
    }

    @Transactional
    public Map<String, Object> receive(ReceiveRequest request) {
        if (request.productId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Product ID is required");
        }
        if (request.quantity() == null || request.quantity() <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Quantity must be greater than zero");
        }

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));

        WarehouseBin bin = slottingService.chooseBin(product, request.quantity());
        int previous = bin.getCurrentOccupancy();

        Inventory inventory = inventoryRepository.findByProductIdAndBinId(product.getId(), bin.getId())
                .orElse(new Inventory(product, bin, 0));

        inventory.setQuantity(inventory.getQuantity() + request.quantity());
        bin.setCurrentOccupancy(previous + request.quantity());

        inventoryRepository.save(inventory);
        binRepository.save(bin);

        return Map.of(
                "productId", product.getId(),
                "productName", product.getName(),
                "quantityReceived", request.quantity(),
                "assignedBin", bin.getBinCode(),
                "zone", bin.getZone(),
                "previousOccupancy", previous,
                "newOccupancy", bin.getCurrentOccupancy(),
                "availableCapacity", bin.getAvailableCapacity(),
                "velocity", product.getVelocity()
        );
    }

    public List<Map<String, Object>> all() {
        return inventoryRepository.findAll().stream().map(i -> {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("id", i.getId());
            result.put("productId", i.getProduct().getId());
            result.put("productName", i.getProduct().getName());
            result.put("sku", i.getProduct().getSku());
            result.put("quantity", i.getQuantity());
            result.put("binId", i.getBin().getId());
            result.put("binCode", i.getBin().getBinCode());
            result.put("zone", i.getBin().getZone());
            return result;
        }).toList();
    }

    public List<Inventory> byProduct(Long productId) {
        return inventoryRepository.findByProductId(productId);
    }
}
