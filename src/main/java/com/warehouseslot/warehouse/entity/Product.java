package com.warehouseslot.warehouse.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Velocity velocity = Velocity.MEDIUM;

    @Column(nullable = false)
    private boolean active = true;

    public Product() {}

    public Product(String sku, String name, String description) {
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.velocity = Velocity.MEDIUM;
        this.active = true;
    }

    public Long getId() { return id; }
    public String getSku() { return sku; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Velocity getVelocity() { return velocity == null ? Velocity.MEDIUM : velocity; }
    public boolean isActive() { return active; }

    public void setId(Long id) { this.id = id; }
    public void setSku(String sku) { this.sku = sku; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setVelocity(Velocity velocity) { this.velocity = velocity; }
    public void setActive(boolean active) { this.active = active; }
}
