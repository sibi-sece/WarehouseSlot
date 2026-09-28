# Warehouse Slotting and Picklist Generator

Simple Java + Spring Boot + JPA warehouse project with a browser frontend.

## Core features

1. Product registration with velocity: HIGH / MEDIUM / LOW.
2. Warehouse bin registration with zone, capacity and route order.
3. Automatic stock slotting:
   - HIGH and MEDIUM prefer NEAR_DISPATCH.
   - LOW prefers FAR.
   - Full preferred bins fall back to another suitable bin.
   - Bin capacity is never exceeded.
4. Inventory tracking and bin occupancy.
5. Outgoing order creation.
6. Optimized picklist sorted by route order.
7. Picking an order reduces inventory and bin occupancy.
8. Dashboard and utilization/stock reports.

## Run immediately

Requirements:
- JDK 17 or newer
- VS Code

From the project folder:

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

Open:

http://localhost:8080/

The default database is H2 file storage, so MySQL is NOT required for the first run.

The project automatically creates a few demo products and bins when the database is empty.

## MySQL mode

If your college requires MySQL, edit:

`src/main/resources/application-mysql.properties`

Change:

`spring.datasource.password=YOUR_MYSQL_PASSWORD`

Then run:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=mysql"
```

Database name:

`warehouse_slotting`

## Main API endpoints

GET /api/health

Products:
- POST /api/products
- GET /api/products
- PUT /api/products/{id}
- DELETE /api/products/{id}

Bins:
- POST /api/bins
- GET /api/bins
- GET /api/bins/{id}
- GET /api/bins/{id}/utilization

Inventory:
- POST /api/inventory/receive
- GET /api/inventory

Orders:
- POST /api/orders
- GET /api/orders
- GET /api/orders/{id}/picklist
- POST /api/orders/{id}/pick

Reports:
- GET /api/reports/summary
- GET /api/reports/products
- GET /api/reports/bins

## Demo flow

1. Open Dashboard.
2. Check Products and Bins.
3. Go to Receive Stock.
4. Receive stock for a product.
5. The system automatically chooses a bin.
6. Create an outgoing order.
7. Open its Picklist.
8. Click Pick Order.
9. Check Reports to see reduced stock and occupancy.
