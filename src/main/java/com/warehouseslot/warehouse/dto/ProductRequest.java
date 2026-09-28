package com.warehouseslot.warehouse.dto;

import com.warehouseslot.warehouse.entity.Velocity;

public record ProductRequest(
        String sku,
        String name,
        String description,
        Velocity velocity
) {}
