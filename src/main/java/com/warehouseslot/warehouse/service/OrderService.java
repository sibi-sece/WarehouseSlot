package com.warehouseslot.warehouse.service;

import com.warehouseslot.warehouse.dto.OrderRequest;
import com.warehouseslot.warehouse.dto.PickLine;
import com.warehouseslot.warehouse.entity.*;
import com.warehouseslot.warehouse.exception.ApiException;
import com.warehouseslot.warehouse.repository.CustomerOrderRepository;
import com.warehouseslot.warehouse.repository.InventoryRepository;
import com.warehouseslot.warehouse.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class OrderService {

    private final CustomerOrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    public OrderService(
            CustomerOrderRepository orderRepository,
            ProductRepository productRepository,
            InventoryRepository inventoryRepository) {

        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }


    // =========================================================
    // CREATE ORDER
    // =========================================================

    @Transactional
    public CustomerOrder create(OrderRequest request) {

        if (request.customerName() == null ||
                request.customerName().isBlank()) {

            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Customer name is required"
            );
        }

        if (request.items() == null ||
                request.items().isEmpty()) {

            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "At least one order item is required"
            );
        }


        CustomerOrder order = new CustomerOrder();

        order.setCustomerName(
                request.customerName().trim()
        );


        List<OrderItem> items = new ArrayList<>();


        for (OrderRequest.OrderLine line : request.items()) {

            if (line.productId() == null ||
                    line.quantity() == null ||
                    line.quantity() <= 0) {

                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "Each order item needs a valid product and quantity"
                );
            }


            Product product =
                    productRepository.findById(line.productId())
                            .orElseThrow(() ->
                                    new ApiException(
                                            HttpStatus.NOT_FOUND,
                                            "Product not found: "
                                                    + line.productId()
                                    )
                            );


            int available =
                    inventoryRepository
                            .findByProductId(product.getId())
                            .stream()
                            .mapToInt(inventory -> inventory != null ? inventory.getQuantity() : 0)
                            .sum();


            if (available < line.quantity()) {

                throw new ApiException(
                        HttpStatus.CONFLICT,
                        "Insufficient stock for "
                                + product.getName()
                                + ". Available: "
                                + available
                );
            }


            items.add(
                    new OrderItem(
                            order,
                            product,
                            line.quantity()
                    )
            );
        }


        order.setItems(items);


        return orderRepository.save(order);
    }


    // =========================================================
    // GET ALL ORDERS
    // =========================================================

    public List<CustomerOrder> all() {

        return orderRepository.findAll();
    }


    // =========================================================
    // GET SINGLE ORDER
    // =========================================================

    public CustomerOrder get(Long id) {

        return orderRepository.findById(id)
                .orElseThrow(() ->
                        new ApiException(
                                HttpStatus.NOT_FOUND,
                                "Order not found"
                        )
                );
    }


    // =========================================================
    // GENERATE PICKLIST
    // =========================================================
    //
    // IMPORTANT:
    // This method is transactional because CustomerOrder.items
    // is a lazy-loaded JPA collection.
    //
    // Without this annotation:
    //
    // CustomerOrder
    //      ↓
    // getItems()
    //      ↓
    // Hibernate session closed
    //      ↓
    // LazyInitializationException
    //
    // =========================================================

    @Transactional(readOnly = true)
    public List<PickLine> picklist(Long orderId) {

        CustomerOrder order = get(orderId);


        List<PickLine> result = new ArrayList<>();


        for (OrderItem item : order.getItems()) {

            int remaining = item.getQuantity();


            List<Inventory> stock =
                    inventoryRepository
                            .findByProductId(
                                    item.getProduct().getId()
                            )
                            .stream()

                            .filter(i ->
                                    i.getQuantity() > 0
                            )

                            .sorted(
                                    Comparator
                                            .comparingInt(
                                                    (Inventory i) ->
                                                            i.getBin()
                                                                    .getRouteOrder()
                                            )

                                            .thenComparing(
                                                    i ->
                                                            i.getBin()
                                                                    .getZone()
                                            )
                            )

                            .toList();


            for (Inventory inventory : stock) {

                if (remaining == 0) {
                    break;
                }


                int take =
                        Math.min(
                                remaining,
                                inventory.getQuantity()
                        );


                result.add(
                        new PickLine(

                                item.getProduct().getId(),

                                item.getProduct().getName(),

                                item.getProduct().getSku(),

                                inventory.getBin().getId(),

                                inventory.getBin().getBinCode(),

                                inventory.getBin()
                                        .getZone()
                                        .name(),

                                inventory.getBin()
                                        .getRouteOrder(),

                                take
                        )
                );


                remaining -= take;
            }


            if (remaining > 0) {

                throw new ApiException(
                        HttpStatus.CONFLICT,
                        "Stock changed. Unable to generate complete picklist."
                );
            }
        }


        return result

                .stream()

                .sorted(
                        Comparator.comparingInt(
                                (PickLine pickLine) -> pickLine.routeOrder()
                        )
                )

                .toList();
    }


    // =========================================================
    // PICK ORDER
    // =========================================================

    @Transactional
    public List<PickLine> pick(Long orderId) {

        CustomerOrder order = get(orderId);


        if (order.getStatus() == OrderStatus.PICKED) {

            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Order is already picked"
            );
        }


        List<PickLine> lines =
                picklist(orderId);


        for (PickLine line : lines) {

            Inventory inventory =
                    inventoryRepository
                            .findByProductIdAndBinId(
                                    line.productId(),
                                    line.binId()
                            )

                            .orElseThrow(() ->
                                    new ApiException(
                                            HttpStatus.CONFLICT,
                                            "Inventory changed"
                                    )
                            );


            // Reduce inventory quantity
            inventory.setQuantity(
                    inventory.getQuantity()
                            - line.quantity()
            );


            // Reduce bin occupancy
            WarehouseBin bin =
                    inventory.getBin();

            bin.setCurrentOccupancy(
                    bin.getCurrentOccupancy()
                            - line.quantity()
            );


            inventoryRepository.save(inventory);
        }


        // Mark order as picked
        order.setStatus(
                OrderStatus.PICKED
        );


        orderRepository.save(order);


        return lines;
    }
}