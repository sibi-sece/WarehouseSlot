package com.warehouseslot.warehouse.dto;

public record PickLine(
        Long productId,
        String productName,
        String sku,
        Long binId,
        String binCode,
        String zone,
        int routeOrder,
        int quantity
) {}
