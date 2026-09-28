package com.warehouseslot.warehouse.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "inventory",
       uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "bin_id"}))
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Product product;

    @ManyToOne(optional = false)
    private WarehouseBin bin;

    @Column(nullable = false)
    private int quantity;

    public Inventory() {}

    public Inventory(Product product, WarehouseBin bin, int quantity) {
        this.product = product;
        this.bin = bin;
        this.quantity = quantity;
    }

    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public WarehouseBin getBin() { return bin; }
    public int getQuantity() { return quantity; }

    public void setId(Long id) { this.id = id; }
    public void setProduct(Product product) { this.product = product; }
    public void setBin(WarehouseBin bin) { this.bin = bin; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
