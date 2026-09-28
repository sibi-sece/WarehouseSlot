package com.warehouseslot.warehouse.service;

import com.warehouseslot.warehouse.entity.Product;
import com.warehouseslot.warehouse.entity.Velocity;
import com.warehouseslot.warehouse.entity.WarehouseBin;
import com.warehouseslot.warehouse.entity.Zone;
import com.warehouseslot.warehouse.exception.ApiException;
import com.warehouseslot.warehouse.repository.WarehouseBinRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class SlottingService {
    private final WarehouseBinRepository binRepository;

    public SlottingService(WarehouseBinRepository binRepository) {
        this.binRepository = binRepository;
    }

    public WarehouseBin chooseBin(Product product, int quantity) {
        if (quantity <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Quantity must be greater than zero");
        }

        List<WarehouseBin> candidates = binRepository.findByActiveTrue().stream()
                .filter(b -> b.getAvailableCapacity() >= quantity)
                .toList();

        Zone preferredZone = product.getVelocity() == Velocity.LOW
                ? Zone.FAR
                : Zone.NEAR_DISPATCH;

        List<WarehouseBin> preferred = candidates.stream()
                .filter(b -> b.getZone() == preferredZone)
                .toList();

        List<WarehouseBin> finalCandidates = preferred.isEmpty() ? candidates : preferred;

        return finalCandidates.stream()
                .sorted(Comparator
                        .comparingInt((WarehouseBin bin) -> bin.getAvailableCapacity())
                        .reversed()
                        .thenComparingInt((WarehouseBin bin) -> bin.getRouteOrder()))
                .findFirst()
                .orElseThrow(() -> new ApiException(
                        HttpStatus.CONFLICT,
                        "No bin has sufficient capacity"
                ));
    }
}
