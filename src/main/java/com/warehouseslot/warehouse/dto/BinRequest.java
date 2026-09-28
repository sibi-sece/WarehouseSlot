package com.warehouseslot.warehouse.dto;

import com.warehouseslot.warehouse.entity.Zone;

public record BinRequest(
        String binCode,
        Zone zone,
        Integer capacity,
        Integer routeOrder
) {}
