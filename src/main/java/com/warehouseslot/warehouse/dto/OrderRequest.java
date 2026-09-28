package com.warehouseslot.warehouse.dto;

import java.util.List;

public record OrderRequest(
        String customerName,
        List<OrderLine> items
) {
    public record OrderLine(Long productId, Integer quantity) {}
}
