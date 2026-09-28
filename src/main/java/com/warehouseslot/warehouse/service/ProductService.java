package com.warehouseslot.warehouse.service;

import com.warehouseslot.warehouse.dto.ProductRequest;
import com.warehouseslot.warehouse.entity.Product;
import com.warehouseslot.warehouse.entity.Velocity;
import com.warehouseslot.warehouse.exception.ApiException;
import com.warehouseslot.warehouse.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public Product create(ProductRequest request) {
        if (request.sku() == null || request.sku().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "SKU is required");
        }
        if (request.name() == null || request.name().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Product name is required");
        }
        if (repository.findBySku(request.sku().trim()).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "SKU already exists");
        }

        Product p = new Product(request.sku().trim(), request.name().trim(), request.description());
        p.setVelocity(request.velocity() == null ? Velocity.MEDIUM : request.velocity());
        return repository.save(p);
    }

    public List<Product> all() {
        return repository.findAll();
    }

    public Product get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    public Product update(Long id, ProductRequest request) {
        Product p = get(id);
        if (request.name() != null && !request.name().isBlank()) p.setName(request.name().trim());
        if (request.description() != null) p.setDescription(request.description());
        if (request.velocity() != null) p.setVelocity(request.velocity());
        return repository.save(p);
    }

    public void deactivate(Long id) {
        Product p = get(id);
        p.setActive(false);
        repository.save(p);
    }
}
