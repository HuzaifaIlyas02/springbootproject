# 🎓 Spring Boot Concepts & Complete Documentation

## 📋 Table of Contents
1. [Order Service (Continued)](#order-service-continued)
2. [Inventory Service](#inventory-service)
3. [Spring Boot Concepts Used](#spring-boot-concepts-used)
4. [Why These Concepts](#why-these-concepts)

---

# Order Service (Continued)

## 📁 OrderService.java
**Location**: `order-service/src/main/java/com/huzaifaproject/orderservice/service/OrderService.java`

```java
package com.huzaifaproject.orderservice.service;

import com.huzaifaproject.orderservice.dto.InventoryResponse;
import com.huzaifaproject.orderservice.dto.OrderLineItemsDto;
import com.huzaifaproject.orderservice.dto.OrderRequest;
import com.huzaifaproject.orderservice.model.Order;
import com.huzaifaproject.orderservice.model.OrderLineItems;
import com.huzaifaproject.orderservice.repository.OrderRepository;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
```
**Line Purpose**: Imports for service layer
- DTOs for data transfer
- Models and repository
- Micrometer for observability
- WebClient for HTTP calls
- Transaction management

```java
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderService {
```
**Line Purpose**: Service class with transaction management
**@Transactional**: All public methods run in database transaction
**Why**: 
- Ensures data consistency
- Auto-rollback on exceptions
- Multiple DB operations are atomic

```java
    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
    private final ObservationRegistry observationRegistry;
```
**Line Purpose**: Injected dependencies
**orderRepository**: Database operations
**webClientBuilder**: HTTP client (configured with @LoadBalanced)
**observationRegistry**: Distributed tracing and metrics

```java
    public String placeOrder(OrderRequest orderRequest) {
```
**Line Purpose**: Main business logic for placing orders
**Parameters**: `OrderRequest` - order details from client
**Returns**: `String` - success message or throws exception

```java
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
```
**Line Purpose**: Create new order with unique number
**What It Does**:
- Creates Order entity
- Generates unique order number using UUID
**UUID Format**: "550e8400-e29b-41d4-a716-446655440000"
**Why UUID**: Guaranteed unique across distributed systems

```java
        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();
```
**Line Purpose**: Convert DTOs to entities
**What It Does**:
1. Get list of OrderLineItemsDto from request
2. Stream through each DTO
3. Convert each DTO to OrderLineItems entity
4. Collect to list
**Why**: Separate DTO (API) from Entity (Database)

```java
        order.setOrderLineItemsList(orderLineItems);
```
**Line Purpose**: Associate line items with order
**Relationship**: One Order has many OrderLineItems
**JPA**: Cascade.ALL ensures line items are saved with order

```java
        List<String> skuCodes = order.getOrderLineItemsList().stream()
                .map(OrderLineItems::getSkuCode)
                .toList();
```
**Line Purpose**: Extract SKU codes from order items
**What It Does**:
1. Get all line items
2. Extract skuCode from each
3. Collect to list
**Example**: ["iphone_13", "iphone_13_red"]
**Why**: Need to check inventory for these products

```java
        // Call Inventory Service, and place order if product is in
        // stock
```
**Line Purpose**: Comment explaining inventory check logic

```java
        Observation inventoryServiceObservation = Observation.createNotStarted("inventory-service-lookup",
                this.observationRegistry);
```
**Line Purpose**: Create observability span for inventory call
**What It Does**:
- Creates observation (trace span)
- Name: "inventory-service-lookup"
- Not started yet (will start with .observe())
**Why**: Distributed tracing - see how long inventory check takes

```java
        inventoryServiceObservation.lowCardinalityKeyValue("call", "inventory-service");
```
**Line Purpose**: Add metadata to trace
**What It Does**: Tags this span with "call=inventory-service"
**Why**: Filter traces by service in monitoring tools (Zipkin, Jaeger)
**Low Cardinality**: Limited values (service names), not high cardinality (user IDs)

```java
        return inventoryServiceObservation.observe(() -> {
```
**Line Purpose**: Execute inventory check within observation
**What It Does**:
- Starts observation (begins timing)
- Executes lambda
- Stops observation (records duration)
- Returns lambda result
**Why**: Automatic timing and tracing

```java
            InventoryResponse[] inventoryResponseArray = webClientBuilder.build().get()
```
**Line Purpose**: Start building HTTP GET request
**webClientBuilder**: Injected WebClient.Builder with @LoadBalanced
**.build()**: Creates WebClient instance
**.get()**: Initiates GET request
**Why WebClient**: 
- Non-blocking, reactive
- Better than RestTemplate
- Supports Eureka service discovery

```java
                    .uri("http://inventory-service/api/inventory",
                            uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
```
**Line Purpose**: Build request URI with query parameters
**Base URL**: `http://inventory-service` - service name, not IP!
**Path**: `/api/inventory`
**Query Params**: `?skuCode=iphone_13&skuCode=iphone_13_red`
**How It Works**:
1. @LoadBalanced WebClient asks Eureka for "inventory-service"
2. Eureka returns IP:Port (e.g., 192.168.1.100:8082)
3. WebClient makes call to actual IP
**Why Service Name**: Dynamic IPs, load balancing, multiple instances

```java
                    .retrieve()
```
**Line Purpose**: Execute request and get response
**What It Does**: Makes actual HTTP call

```java
                    .bodyToMono(InventoryResponse[].class)
```
**Line Purpose**: Parse response body to array
**What It Does**: Deserializes JSON to InventoryResponse[]
**Mono**: Reactive type representing 0 or 1 element (the array)
**Example Response**:
```json
[
  {"skuCode": "iphone_13", "isInStock": true},
  {"skuCode": "iphone_13_red", "isInStock": false}
]
```

```java
                    .block();
```
**Line Purpose**: Wait for async operation to complete
**What It Does**:
- Blocks current thread
- Waits for HTTP response
- Returns result or throws exception
**Why Block**: Need result before proceeding
**Note**: In fully reactive app, would use .subscribe() instead

```java
            boolean allProductsInStock = Arrays.stream(inventoryResponseArray)
                    .allMatch(InventoryResponse::isInStock);
```
**Line Purpose**: Check if ALL products are in stock
**What It Does**:
1. Stream through inventory responses
2. Check each response's isInStock flag
3. allMatch returns true only if ALL are in stock
**Why**: Can't fulfill order if any item is out of stock
**Example**: 
- [true, true] → true (place order)
- [true, false] → false (reject order)

```java
            if (allProductsInStock) {
```
**Line Purpose**: Proceed only if all items available

```java
                orderRepository.save(order);
```
**Line Purpose**: Persist order to database
**What It Does**:
1. Inserts into t_orders table
2. Inserts into t_order_line_items table (cascade)
3. Generates IDs for both
4. Commits transaction
**Transaction**: @Transactional ensures atomic save

```java
                return "Order Placed";
```
**Line Purpose**: Return success message
**Returned To**: Controller → Client (HTTP 201 response)

```java
            } else {
                throw new IllegalArgumentException("Product is not in stock, please try again later");
            }
```
**Line Purpose**: Reject order if items unavailable
**What It Does**: Throws exception with user-friendly message
**Result**: 
- Transaction rolled back (no data saved)
- Controller returns 400 Bad Request with message
**Why IllegalArgumentException**: Business rule violation

```java
        });
    }
```
**Line Purpose**: End of observe() lambda and method
**What Happens**:
- Observation automatically records duration
- Trace span is closed
- Metrics are recorded

```java
    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
```
**Line Purpose**: Helper method to convert DTO to entity
**Parameters**: `OrderLineItemsDto` - DTO from request
**Returns**: `OrderLineItems` - JPA entity
**Visibility**: private helper method

```java
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
```
**Line Purpose**: Copy fields from DTO to entity
**Why**: Separation of API layer (DTO) and persistence layer (Entity)
**Note**: ID not set (database generates it)

---

## 📁 Order.java (Model)
**Location**: `order-service/src/main/java/com/huzaifaproject/orderservice/model/Order.java`

```java
package com.huzaifaproject.orderservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.List;
```
**Line Purpose**: Imports for JPA entity

```java
@Entity
```
**Line Purpose**: Marks class as JPA entity
**What It Does**: 
- Creates table in database
- Enables ORM (Object-Relational Mapping)
- Managed by JPA/Hibernate
**Why**: Automatic database operations

```java
@Table(name = "t_orders")
```
**Line Purpose**: Specifies table name
**Why "t_orders"**: "order" is SQL reserved keyword, prefix with "t_"
**Alternative**: Could use @Table(name = "`order`") with backticks

```java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
```
**Line Purpose**: Lombok annotations for getters/setters/constructors
**Why**: Less boilerplate than @Data (more control)

```java
public class Order {
```
**Line Purpose**: JPA entity class

```java
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
```
**Line Purpose**: Primary key with auto-increment
**@Id**: Marks as primary key
**@GeneratedValue**: Database generates value
**IDENTITY**: Uses database auto-increment (MySQL AUTO_INCREMENT)
**Alternative**: GenerationType.SEQUENCE for PostgreSQL

```java
    private Long id;
```
**Line Purpose**: Primary key field
**Type**: Long - supports large numbers

```java
    private String orderNumber;
```
**Line Purpose**: Business identifier (UUID)
**Why**: User-friendly order number, not database ID

```java
    @OneToMany(cascade = CascadeType.ALL)
```
**Line Purpose**: One-to-Many relationship
**What It Does**:
- One Order has many OrderLineItems
- CascadeType.ALL: Save/delete order also saves/deletes line items
**Why Cascade**: Line items don't exist without order
**Database**: Foreign key in t_order_line_items table

```java
    private List<OrderLineItems> orderLineItemsList;
}
```
**Line Purpose**: Collection of line items
**Type**: List - ordered collection
**JPA**: Automatically queries and populates this list

---

## 📁 OrderLineItems.java (Model)
**Location**: `order-service/src/main/java/com/huzaifaproject/orderservice/model/OrderLineItems.java`

```java
package com.huzaifaproject.orderservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "t_order_line_items")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderLineItems {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String skuCode;
    private BigDecimal price;
    private Integer quantity;
}
```
**Line Purpose**: JPA entity for order line items
**Fields**:
- **id**: Primary key (auto-generated)
- **skuCode**: Product identifier (e.g., "iphone_13")
- **price**: Item price at time of order
- **quantity**: Number of units ordered
**Why Separate Table**: Normalized database design, multiple items per order

---

## 📁 OrderRepository.java
**Location**: `order-service/src/main/java/com/huzaifaproject/orderservice/repository/OrderRepository.java`

```java
package com.huzaifaproject.orderservice.repository;

import com.huzaifaproject.orderservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
```
**Line Purpose**: JPA repository for Order entity
**Extends**: `JpaRepository<Order, Long>`
- **Order**: Entity type
- **Long**: ID type
**Provides**: save(), findAll(), findById(), delete(), etc.
**Why**: Standard CRUD operations without implementation

---

## 📁 OrderRequest.java (DTO)
**Location**: `order-service/src/main/java/com/huzaifaproject/orderservice/dto/OrderRequest.java`

```java
package com.huzaifaproject.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    private List<OrderLineItemsDto> orderLineItemsDtoList;
}
```
**Line Purpose**: DTO for order creation request
**Structure**: Contains list of items to order
**Example JSON**:
```json
{
  "orderLineItemsDtoList": [
    {
      "skuCode": "iphone_13",
      "price": 999.99,
      "quantity": 2
    }
  ]
}
```

---

## 📁 OrderLineItemsDto.java
**Location**: `order-service/src/main/java/com/huzaifaproject/orderservice/dto/OrderLineItemsDto.java`

```java
package com.huzaifaproject.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderLineItemsDto {
    private Long id;
    private String skuCode;
    private BigDecimal price;
    private Integer quantity;
}
```
**Line Purpose**: DTO for individual order items
**Fields**: Same as entity but used for API contract

---

## 📁 InventoryResponse.java (DTO)
**Location**: `order-service/src/main/java/com/huzaifaproject/orderservice/dto/InventoryResponse.java`

```java
package com.huzaifaproject.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryResponse {
    private String skuCode;
    private boolean isInStock;
}
```
**Line Purpose**: DTO for inventory service response
**Purpose**: Deserializes inventory service JSON response
**Why Here**: Order Service needs to parse inventory response

---

## 📁 WebClientConfig.java
**Location**: `order-service/src/main/java/com/huzaifaproject/orderservice/config/WebClientConfig.java`

```java
package com.huzaifaproject.orderservice.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
```
**Line Purpose**: Configures WebClient with load balancing
**@LoadBalanced**: Enables Eureka service discovery
**What It Does**:
- WebClient uses service names instead of IP:Port
- Eureka resolves service names to actual instances
- Built-in client-side load balancing
**How**:
- `http://inventory-service` → Eureka → `http://192.168.1.100:8082`
**Why Bean**: WebClient.Builder is reusable, injected where needed

---

# Inventory Service

## 📁 InventoryServiceApplication.java
**Location**: `inventory-service/src/main/java/com/huzaifaproject/inventoryservice/InventoryServiceApplication.java`

```java
package com.huzaifaproject.inventoryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InventoryServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }
}
```
**Line Purpose**: Entry point for Inventory Service
**What It Enables**: MySQL, REST endpoints, service discovery

---

## 📁 InventoryController.java
**Location**: `inventory-service/src/main/java/com/huzaifaproject/inventoryservice/controller/InventoryController.java`

```java
package com.huzaifaproject.inventoryservice.controller;

import com.huzaifaproject.inventoryservice.dto.InventoryResponse;
import com.huzaifaproject.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;
```
**Line Purpose**: REST controller for inventory checks
**Base URL**: `/api/inventory`

```java
    // http://localhost:8082/api/inventory/iphone-13,iphone13-red
    // http://localhost:8082/api/inventory?skuCode=iphone-13&skuCode=iphone13-red
```
**Line Purpose**: Comment showing two ways to call endpoint
**Why Comments**: Documentation for developers

```java
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<InventoryResponse> isInStock(@RequestParam List<String> skuCode) {
```
**Line Purpose**: Check if products are in stock
**Method**: GET (reading data)
**@RequestParam**: Extracts query parameters
**Parameters**: 
- `List<String> skuCode` - multiple SKU codes
- Query: `?skuCode=iphone_13&skuCode=iphone_13_red`
**Returns**: List of inventory status for each SKU

```java
        log.info("Received inventory check request for skuCode: {}", skuCode);
```
**Line Purpose**: Log incoming request
**Why**: Monitoring, debugging, audit trail
**Example**: "Received inventory check request for skuCode: [iphone_13, iphone_13_red]"

```java
        return inventoryService.isInStock(skuCode);
    }
}
```
**Line Purpose**: Delegate to service layer and return result

---

## 📁 InventoryService.java
**Location**: `inventory-service/src/main/java/com/huzaifaproject/inventoryservice/service/InventoryService.java`

```java
package com.huzaifaproject.inventoryservice.service;

import com.huzaifaproject.inventoryservice.dto.InventoryResponse;
import com.huzaifaproject.inventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;
```
**Line Purpose**: Service layer for inventory operations

```java
    @Transactional(readOnly = true)
```
**Line Purpose**: Read-only transaction
**What It Does**:
- Opens database transaction
- Optimizes for read operations
- No data modifications allowed
**Why**: Better performance, clear intent

```java
    @SneakyThrows
```
**Line Purpose**: Lombok annotation to hide checked exceptions
**What It Does**: Wraps checked exceptions in RuntimeException
**Why**: Simplifies method signature (no throws declaration needed)
**Use Carefully**: Can hide important errors

```java
    public List<InventoryResponse> isInStock(List<String> skuCode) {
```
**Line Purpose**: Check stock for multiple products
**Parameters**: List of SKU codes
**Returns**: List of InventoryResponse (skuCode + isInStock boolean)

```java
        return inventoryRepository.findBySkuCodeIn(skuCode).stream()
```
**Line Purpose**: Query database and start stream
**findBySkuCodeIn**: Spring Data query method
**SQL**: `SELECT * FROM inventory WHERE sku_code IN ('iphone_13', 'iphone_13_red')`
**Returns**: List of Inventory entities matching SKU codes

```java
                .map(inventory ->
                        InventoryResponse.builder()
                                .skuCode(inventory.getSkuCode())
                                .isInStock(inventory.getQuantity() > 0)
                                .build()
                ).toList();
```
**Line Purpose**: Transform entities to DTOs
**What It Does**:
1. For each Inventory entity
2. Create InventoryResponse DTO
3. Set skuCode
4. Set isInStock to true if quantity > 0
5. Collect to list
**Business Logic**: `quantity > 0` means in stock
**Returns**: List of InventoryResponse DTOs

---

## 📁 Inventory.java (Model)
**Location**: `inventory-service/src/main/java/com/huzaifaproject/inventoryservice/model/Inventory.java`

```java
package com.huzaifaproject.inventoryservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Table
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String skuCode;
    private Integer quantity;
}
```
**Line Purpose**: JPA entity for inventory
**Fields**:
- **id**: Primary key
- **skuCode**: Product identifier
- **quantity**: Stock level
**Simple Structure**: No complex relationships

---

## 📁 InventoryRepository.java
**Location**: `inventory-service/src/main/java/com/huzaifaproject/inventoryservice/repository/InventoryRepository.java`

```java
package com.huzaifaproject.inventoryservice.repository;

import com.huzaifaproject.inventoryservice.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    List<Inventory> findBySkuCodeIn(List<String> skuCode);
}
```
**Line Purpose**: Repository with custom query method
**Custom Method**: `findBySkuCodeIn(List<String> skuCode)`
**Spring Data Magic**: Method name parsed to generate SQL
- **findBy**: SELECT query
- **SkuCode**: Column name
- **In**: SQL IN operator
**Generated SQL**: `WHERE sku_code IN (?)`
**Why**: Need to check multiple products at once

---

## 📁 InventoryResponse.java (DTO)
**Location**: `inventory-service/src/main/java/com/huzaifaproject/inventoryservice/dto/InventoryResponse.java`

```java
package com.huzaifaproject.inventoryservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryResponse {
    private String skuCode;
    private boolean isInStock;
}
```
**Line Purpose**: DTO for inventory status
**Fields**:
- **skuCode**: Product identifier
- **isInStock**: Availability flag (true/false)
**Example JSON**:
```json
{
  "skuCode": "iphone_13",
  "isInStock": true
}
```

---

## 📁 DataLoader.java
**Location**: `inventory-service/src/main/java/com/huzaifaproject/inventoryservice/util/DataLoader.java`

```java
package com.huzaifaproject.inventoryservice.util;

import com.huzaifaproject.inventoryservice.model.Inventory;
import com.huzaifaproject.inventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
```
**Line Purpose**: Loads sample data on startup
**@Component**: Spring manages this bean
**implements CommandLineRunner**: run() method executes after app starts
**Why**: Pre-populate database for testing/demo

```java
    private final InventoryRepository inventoryRepository;
    @Override
    public void run(String... args) throws Exception {
```
**Line Purpose**: Method called on application startup
**Parameters**: Command-line arguments (unused here)

```java
        Inventory inventory = new Inventory();
        inventory.setSkuCode("iphone_13");
        inventory.setQuantity(100);

        Inventory inventory1 = new Inventory();
        inventory1.setSkuCode("iphone_13_red");
        inventory1.setQuantity(0);

        inventoryRepository.save(inventory);
        inventoryRepository.save(inventory1);
    }
}
```
**Line Purpose**: Creates sample inventory data
**What It Does**:
1. Create inventory for "iphone_13" with 100 units (IN STOCK)
2. Create inventory for "iphone_13_red" with 0 units (OUT OF STOCK)
3. Save both to database
**Why**: Demo data for testing order placement

---

# Spring Boot Concepts Used

## 1. 🎯 Spring Boot Core

### @SpringBootApplication
**What**: Composite annotation combining three annotations
**Includes**:
- `@Configuration`: Class is source of bean definitions
- `@EnableAutoConfiguration`: Auto-configure based on classpath
- `@ComponentScan`: Scan for components in package and sub-packages

**Why Used**: 
- Single annotation to bootstrap entire application
- Reduces configuration boilerplate
- Convention over configuration

**Example Use**:
```java
@SpringBootApplication
public class ProductServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
```

**What Happens**:
1. Spring scans for `@Component`, `@Service`, `@Repository`, `@Controller`
2. Auto-configures MongoDB, MySQL, Web, Security based on dependencies
3. Creates ApplicationContext with all beans
4. Starts embedded Tomcat server
5. Application ready to handle requests

---

### SpringApplication.run()
**What**: Static method to launch Spring Boot application

**What It Does**:
1. Creates ApplicationContext
2. Loads configurations
3. Auto-configures beans
4. Starts embedded server
5. Publishes events

**Why**: Single-line application startup

---

### @Configuration
**What**: Indicates class contains bean definitions

**Usage**:
```java
@Configuration
public class WebClientConfig {
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
```

**Why**: 
- Java-based configuration (vs XML)
- Type-safe
- Refactor-friendly

---

### @Bean
**What**: Declares method returns Spring-managed bean

**Lifecycle**:
1. Spring calls method
2. Stores returned object in ApplicationContext
3. Manages lifecycle (init, destroy)
4. Injects into other beans

**Scope**: Singleton by default (one instance per container)

---

## 2. 🌐 Spring Web (REST APIs)

### @RestController
**What**: Combines `@Controller` + `@ResponseBody`

**What It Does**:
- Every method returns data (not view name)
- Automatic JSON serialization
- HTTP status codes

**Why**: RESTful web services

**Example**:
```java
@RestController
@RequestMapping("/api/product")
public class ProductController {
    @GetMapping
    public List<ProductResponse> getAllProducts() {
        return productService.getAllProducts();
    }
}
```

---

### @RequestMapping
**What**: Maps HTTP requests to handler methods

**Usage**:
```java
@RequestMapping("/api/product")  // Base path for all methods
public class ProductController {
    @GetMapping  // GET /api/product
    @PostMapping // POST /api/product
}
```

**Why**: Clean URL routing

---

### @GetMapping, @PostMapping
**What**: Shorthand for `@RequestMapping(method = GET/POST)`

**RESTful Convention**:
- **GET**: Retrieve data (idempotent, safe)
- **POST**: Create resource (not idempotent)
- **PUT**: Update resource
- **DELETE**: Remove resource

---

### @RequestBody
**What**: Deserializes HTTP request body to Java object

**Example**:
```java
@PostMapping
public void createProduct(@RequestBody ProductRequest request) {
    // request is populated from JSON body
}
```

**JSON → Java**:
```json
{"name": "iPhone", "price": 999.99}
```
↓ Deserialized to
```java
ProductRequest(name="iPhone", price=999.99)
```

---

### @RequestParam
**What**: Extracts query parameters from URL

**Example**:
```java
@GetMapping
public List<InventoryResponse> check(@RequestParam List<String> skuCode) {
    // ?skuCode=iphone&skuCode=samsung
}
```

**Why**: Type-safe parameter extraction

---

### @ResponseStatus
**What**: Sets HTTP status code for response

**Example**:
```java
@PostMapping
@ResponseStatus(HttpStatus.CREATED)  // 201
public void create(...) { }
```

**Why**: Semantic HTTP responses

---

## 3. 💾 Spring Data JPA

### @Entity
**What**: Marks class as JPA entity (database table)

**What It Does**:
- Creates table schema
- Enables ORM (Object-Relational Mapping)
- Automatic CRUD operations

**Example**:
```java
@Entity
@Table(name = "t_orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
```

**Benefits**:
- No SQL queries needed
- Type-safe database operations
- Automatic schema generation

---

### @Id
**What**: Marks primary key field

**Why**: JPA needs to identify entities uniquely

---

### @GeneratedValue
**What**: Specifies primary key generation strategy

**Strategies**:
- **IDENTITY**: Database auto-increment (MySQL)
- **SEQUENCE**: Database sequence (PostgreSQL)
- **AUTO**: JPA chooses strategy
- **TABLE**: Separate table for ID generation

---

### @OneToMany
**What**: Defines one-to-many relationship

**Example**:
```java
@OneToMany(cascade = CascadeType.ALL)
private List<OrderLineItems> orderLineItemsList;
```

**What It Does**:
- One Order has many OrderLineItems
- Cascade operations (save order saves line items)
- Foreign key in child table

**Why**: Models real-world relationships

---

### JpaRepository<T, ID>
**What**: Interface providing CRUD operations

**Auto-Implemented Methods**:
- `save(T)` - Insert/update
- `findAll()` - Get all
- `findById(ID)` - Get by ID
- `delete(T)` - Remove
- `count()` - Count records

**Custom Queries**:
```java
List<Inventory> findBySkuCodeIn(List<String> skuCode);
```
**Spring Data parses method name to generate SQL**

---

### @Transactional
**What**: Manages database transactions

**What It Does**:
1. Begins transaction before method
2. Commits if method succeeds
3. Rolls back if exception thrown

**Example**:
```java
@Transactional
public void placeOrder(OrderRequest request) {
    orderRepository.save(order);
    // If exception here, order is NOT saved (rolled back)
}
```

**readOnly = true**: Optimizes for read operations

**Why**: Data consistency and integrity

---

## 4. 🗃️ Spring Data MongoDB

### @Document
**What**: Marks class as MongoDB document (like @Entity for JPA)

**Example**:
```java
@Document(value = "product")
public class Product {
    @Id
    private String id;  // MongoDB ObjectId
}
```

**What It Does**:
- Maps to MongoDB collection
- Stores as BSON document
- Auto-serialization

---

### MongoRepository<T, ID>
**What**: MongoDB-specific repository interface

**Provides**: 
- All JpaRepository methods
- MongoDB-specific operations
- Query by example

**Example**:
```java
public interface ProductRepository extends MongoRepository<Product, String> {
    // Inherits save(), findAll(), etc.
}
```

---

## 5. 🔐 Spring Security

### @EnableWebFluxSecurity
**What**: Enables reactive (WebFlux) security

**Why Reactive**:
- Non-blocking I/O
- Better performance
- Handles concurrent requests efficiently

---

### SecurityWebFilterChain
**What**: Configures security for reactive apps

**Example**:
```java
@Bean
public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
    http
        .authorizeExchange(exchange ->
            exchange
                .pathMatchers("/api/product").hasRole("ADMIN")
                .anyExchange().authenticated()
        )
        .oauth2ResourceServer(spec -> spec.jwt());
    return http.build();
}
```

---

### OAuth2 Resource Server
**What**: Validates JWT tokens from OAuth2 provider (Keycloak)

**How It Works**:
1. Client sends request with JWT in Authorization header
2. Spring extracts JWT
3. Validates signature with Keycloak public key
4. Extracts claims (username, roles)
5. Creates Authentication object
6. Checks authorization rules

**Why**: Stateless authentication, no sessions

---

### CORS Configuration
**What**: Cross-Origin Resource Sharing configuration

**Example**:
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:3000"));
    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
    config.setAllowCredentials(true);
    return source;
}
```

**Why**: Allows React frontend to call backend APIs

---

## 6. ☁️ Spring Cloud Netflix

### @EnableEurekaServer
**What**: Activates Netflix Eureka Server

**What It Does**:
- Starts service registry
- Provides REST API for registration
- Dashboard UI on port 8761
- Health checking

**Why**: Service discovery in microservices

---

### @EnableDiscoveryClient
**What**: Registers service with Eureka

**What It Does**:
- Registers on startup
- Sends heartbeat every 30 seconds
- De-registers on shutdown
- Can discover other services

---

### @LoadBalanced
**What**: Enables client-side load balancing

**Example**:
```java
@Bean
@LoadBalanced
public WebClient.Builder webClientBuilder() {
    return WebClient.builder();
}
```

**How It Works**:
1. WebClient uses service name: `http://inventory-service`
2. `@LoadBalanced` intercepts
3. Queries Eureka for instances
4. Chooses instance (round-robin)
5. Replaces with actual URL: `http://192.168.1.100:8082`

**Why**: Dynamic service resolution, load distribution

---

## 7. 🛡️ Resilience4j

### @CircuitBreaker
**What**: Implements circuit breaker pattern

**States**:
- **CLOSED**: Normal operation
- **OPEN**: Too many failures, stop calling
- **HALF_OPEN**: Testing if service recovered

**Example**:
```java
@CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
public CompletableFuture<String> placeOrder(OrderRequest request) {
    // Call inventory service
}

public CompletableFuture<String> fallbackMethod(OrderRequest request, Exception ex) {
    return CompletableFuture.completedFuture("Service unavailable");
}
```

**Why**: Prevents cascading failures

---

### @Retry
**What**: Automatic retry on failure

**Configuration**:
```yaml
resilience4j.retry:
  instances:
    inventory:
      max-attempts: 3
      wait-duration: 1s
```

**Why**: Transient failures often succeed on retry

---

### @TimeLimiter
**What**: Sets timeout for operations

**Why**: Don't wait forever for slow services

---

## 8. 📊 Observability (Micrometer)

### Observation API
**What**: Distributed tracing and metrics

**Example**:
```java
Observation observation = Observation.createNotStarted("inventory-lookup", registry);
observation.observe(() -> {
    // Timed operation
    return webClient.get()...;
});
```

**What It Does**:
- Records operation duration
- Creates trace spans
- Exports to Zipkin, Jaeger
- Provides metrics

**Why**: Monitor microservices performance

---

## 9. 🌐 Reactive Programming

### WebClient
**What**: Non-blocking HTTP client

**vs RestTemplate**:
- RestTemplate: Blocking, one thread per request
- WebClient: Non-blocking, async, reactive

**Example**:
```java
InventoryResponse[] response = webClient.get()
    .uri("http://inventory-service/api/inventory")
    .retrieve()
    .bodyToMono(InventoryResponse[].class)
    .block();
```

**Why**: Better performance, scalability

---

### Mono<T>
**What**: Reactive type representing 0 or 1 element

**Usage**: Single result (like Optional)

### Flux<T>
**What**: Reactive type representing 0 to N elements

**Usage**: Stream of data

---

## 10. 🔧 Dependency Injection

### Constructor Injection
**What**: Dependencies injected via constructor

**Example**:
```java
@RequiredArgsConstructor  // Lombok generates constructor
public class ProductService {
    private final ProductRepository repository;
}
```

**Why**:
- Immutability (final fields)
- Testability
- Clear dependencies

---

### @Service, @Repository, @Component
**What**: Stereotype annotations for Spring beans

- **@Service**: Business logic layer
- **@Repository**: Data access layer
- **@Component**: Generic Spring component

**Why**: Semantic meaning, component scanning

---

## 11. 📝 Lombok

### @Data
**Generates**:
- Getters for all fields
- Setters for non-final fields
- `toString()`
- `equals()` and `hashCode()`
- Required args constructor

---

### @Builder
**What**: Builder pattern implementation

**Example**:
```java
@Builder
public class Product {
    private String name;
    private BigDecimal price;
}

// Usage
Product product = Product.builder()
    .name("iPhone")
    .price(new BigDecimal("999.99"))
    .build();
```

**Why**: Fluent, readable object creation

---

### @Slf4j
**Generates**: Logger field

```java
@Slf4j
public class OrderService {
    // Generates:
    // private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    
    public void method() {
        log.info("Order placed");
    }
}
```

---

### @RequiredArgsConstructor
**Generates**: Constructor for final fields

**Why**: Constructor injection without boilerplate

---

## 12. 🎪 Application Lifecycle

### CommandLineRunner
**What**: Interface to run code after startup

**Example**:
```java
@Component
public class DataLoader implements CommandLineRunner {
    @Override
    public void run(String... args) {
        // Load initial data
    }
}
```

**Why**: Initialize data, run startup tasks

---

# 🎯 Why These Concepts?

## Microservices Architecture
**Why**: 
- **Scalability**: Scale services independently
- **Technology Freedom**: Different DBs per service
- **Fault Isolation**: One service failure doesn't crash all
- **Team Autonomy**: Teams work independently

---

## Service Discovery (Eureka)
**Why**:
- **Dynamic IPs**: Services can move, scale up/down
- **Load Balancing**: Distribute load across instances
- **No Configuration**: No hardcoded URLs
- **Health Checks**: Auto-remove dead instances

---

## API Gateway
**Why**:
- **Single Entry Point**: Clients call one URL
- **Security**: Centralized authentication/authorization
- **Routing**: Dynamic routing to services
- **Cross-Cutting**: CORS, rate limiting, logging

---

## OAuth2 + JWT
**Why**:
- **Stateless**: No session storage needed
- **Scalable**: Works across multiple instances
- **Standard**: Industry-standard protocol
- **Fine-Grained**: Roles and permissions (RBAC)

---

## Reactive Programming
**Why**:
- **Performance**: Non-blocking I/O
- **Scalability**: Handle more requests with fewer threads
- **Backpressure**: Handle slow consumers
- **Modern**: Future-proof architecture

---

## Circuit Breaker (Resilience4j)
**Why**:
- **Prevent Cascading Failures**: Stop calling failing services
- **Graceful Degradation**: Provide fallback
- **System Stability**: Let services recover
- **Better UX**: Friendly errors vs timeouts

---

## Observability (Micrometer)
**Why**:
- **Monitoring**: Track performance
- **Debugging**: Trace requests across services
- **Alerting**: Detect issues early
- **Optimization**: Find bottlenecks

---

## Spring Data JPA/MongoDB
**Why**:
- **Productivity**: No SQL/Mongo queries
- **Type Safety**: Compile-time checking
- **Maintainability**: Less boilerplate
- **Abstraction**: Change DB easily

---

## DTO Pattern
**Why**:
- **API Stability**: Entity changes don't affect API
- **Security**: Don't expose all entity fields
- **Flexibility**: Different views of same data
- **Validation**: Different rules for input/output

---

## Layered Architecture
```
Controller → Service → Repository
```
**Why**:
- **Separation of Concerns**: Each layer has clear responsibility
- **Testability**: Mock layers easily
- **Maintainability**: Changes isolated to layer
- **Reusability**: Services used by multiple controllers

---

# 🎓 Summary

This microservices project demonstrates:

1. **Modern Architecture**: Microservices with service discovery
2. **Security**: OAuth2 + JWT + RBAC
3. **Resilience**: Circuit breakers, retries, timeouts
4. **Observability**: Distributed tracing and metrics
5. **Best Practices**: DTOs, layered architecture, dependency injection
6. **Scalability**: Reactive programming, load balancing
7. **Productivity**: Spring Boot auto-configuration, Lombok

Every concept serves a purpose:
- **Service Discovery**: Dynamic service location
- **API Gateway**: Single entry point, security
- **Circuit Breaker**: Fault tolerance
- **JWT**: Stateless authentication
- **Reactive**: Better performance
- **Spring Data**: Less boilerplate

The result is a **production-ready, scalable, secure, and maintainable** microservices ecosystem! 🎉
