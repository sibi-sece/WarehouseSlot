package com.warehouseslot.warehouse.repository;

import com.warehouseslot.warehouse.entity.WarehouseBin;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WarehouseBinRepository extends JpaRepository<WarehouseBin, Long> {
    Optional<WarehouseBin> findByBinCode(String binCode);
    List<WarehouseBin> findByActiveTrue();
}
