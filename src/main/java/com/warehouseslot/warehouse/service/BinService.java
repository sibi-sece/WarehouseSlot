package com.warehouseslot.warehouse.service;

import com.warehouseslot.warehouse.dto.BinRequest;
import com.warehouseslot.warehouse.entity.WarehouseBin;
import com.warehouseslot.warehouse.exception.ApiException;
import com.warehouseslot.warehouse.repository.WarehouseBinRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class BinService {
    private final WarehouseBinRepository repository;

    public BinService(WarehouseBinRepository repository) {
        this.repository = repository;
    }

    public WarehouseBin create(BinRequest request) {
        if (request.binCode() == null || request.binCode().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Bin code is required");
        }
        if (request.zone() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Zone is required");
        }
        if (request.capacity() == null || request.capacity() <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Capacity must be greater than zero");
        }
        if (request.routeOrder() == null || request.routeOrder() < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Route order cannot be negative");
        }
        if (repository.findByBinCode(request.binCode().trim()).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "Bin code already exists");
        }

        WarehouseBin bin = new WarehouseBin();
        bin.setBinCode(request.binCode().trim());
        bin.setZone(request.zone());
        bin.setCapacity(request.capacity());
        bin.setRouteOrder(request.routeOrder());
        return repository.save(bin);
    }

    public List<WarehouseBin> all() {
        return repository.findAll().stream()
                .sorted(Comparator.comparingInt((WarehouseBin bin) -> bin.getRouteOrder()))
                .toList();
    }

    public WarehouseBin get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Bin not found"));
    }

    public double utilization(Long id) {
        WarehouseBin b = get(id);
        return b.getCapacity() == 0 ? 0 : (b.getCurrentOccupancy() * 100.0) / b.getCapacity();
    }
}
