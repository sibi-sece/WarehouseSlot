package com.warehouseslot.warehouse.repository;

import com.warehouseslot.warehouse.entity.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
}
