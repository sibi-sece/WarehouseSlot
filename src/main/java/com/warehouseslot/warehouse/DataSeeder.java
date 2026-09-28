package com.warehouseslot.warehouse;

import com.warehouseslot.warehouse.entity.Product;
import com.warehouseslot.warehouse.entity.Velocity;
import com.warehouseslot.warehouse.entity.WarehouseBin;
import com.warehouseslot.warehouse.entity.Zone;
import com.warehouseslot.warehouse.repository.ProductRepository;
import com.warehouseslot.warehouse.repository.WarehouseBinRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {
    private final ProductRepository productRepository;
    private final WarehouseBinRepository binRepository;

    public DataSeeder(ProductRepository productRepository, WarehouseBinRepository binRepository) {
        this.productRepository = productRepository;
        this.binRepository = binRepository;
    }

    @Override
    public void run(String... args) {
        if (productRepository.count() == 0) {
            Product rice = new Product("RICE001", "Rice", "Fast moving grocery item");
            rice.setVelocity(Velocity.HIGH);
            productRepository.save(rice);

            Product oil = new Product("OIL001", "Cooking Oil", "Daily-use item");
            oil.setVelocity(Velocity.MEDIUM);
            productRepository.save(oil);

            Product soap = new Product("SOAP001", "Soap", "Slow moving item");
            soap.setVelocity(Velocity.LOW);
            productRepository.save(soap);
        }

        if (binRepository.count() == 0) {
            binRepository.save(makeBin("BIN-N01", Zone.NEAR_DISPATCH, 100, 1));
            binRepository.save(makeBin("BIN-N02", Zone.NEAR_DISPATCH, 100, 2));
            binRepository.save(makeBin("BIN-F01", Zone.FAR, 150, 10));
            binRepository.save(makeBin("BIN-F02", Zone.FAR, 150, 11));
        }
    }

    private WarehouseBin makeBin(String code, Zone zone, int capacity, int routeOrder) {
        WarehouseBin bin = new WarehouseBin();
        bin.setBinCode(code);
        bin.setZone(zone);
        bin.setCapacity(capacity);
        bin.setRouteOrder(routeOrder);
        return bin;
    }
}
