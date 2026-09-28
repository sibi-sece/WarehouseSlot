package com.warehouseslot.warehouse.repository;

import com.warehouseslot.warehouse.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByProductIdAndBinId(Long productId, Long binId);
    List<Inventory> findByProductId(Long productId);
    List<Inventory> findByBinId(Long binId);
}
