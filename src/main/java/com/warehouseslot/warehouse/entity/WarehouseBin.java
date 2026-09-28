package com.warehouseslot.warehouse.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "warehouse_bins")
public class WarehouseBin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String binCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Zone zone;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private int currentOccupancy = 0;

    @Column(nullable = false)
    private int routeOrder = 0;

    @Column(nullable = false)
    private boolean active = true;

    public WarehouseBin() {}

    public Long getId() { return id; }
    public String getBinCode() { return binCode; }
    public Zone getZone() { return zone; }
    public int getCapacity() { return capacity; }
    public int getCurrentOccupancy() { return currentOccupancy; }
    public int getRouteOrder() { return routeOrder; }
    public boolean isActive() { return active; }

    public void setId(Long id) { this.id = id; }
    public void setBinCode(String binCode) { this.binCode = binCode; }
    public void setZone(Zone zone) { this.zone = zone; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setCurrentOccupancy(int currentOccupancy) { this.currentOccupancy = currentOccupancy; }
    public void setRouteOrder(int routeOrder) { this.routeOrder = routeOrder; }
    public void setActive(boolean active) { this.active = active; }

    public int getAvailableCapacity() {
        return capacity - currentOccupancy;
    }
}
