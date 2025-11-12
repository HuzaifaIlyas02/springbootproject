# 🏗️ Microservices Architecture Overview

## 📋 Table of Contents
1. [System Architecture](#system-architecture)
2. [Service Communication Flow](#service-communication-flow)
3. [Request Flow Diagrams](#request-flow-diagrams)
4. [Module Interactions](#module-interactions)
5. [Database Architecture](#database-architecture)

---

## 🎯 System Architecture

### High-Level Architecture
```
┌─────────────────┐
│  React Frontend │ (Port 3000)
│   + TypeScript  │
└────────┬────────┘
         │ HTTP/OAuth2
         ▼
┌─────────────────┐
│   Keycloak      │ (Port 8080)
│  OAuth2 Server  │
└────────┬────────┘
         │ JWT Tokens
         ▼
┌─────────────────┐
│   API Gateway   │ (Port 8181) ◄─────┐
│  + RBAC + CORS  │                   │
└────────┬────────┘                   │
         │                            │
         │ Routes to:          ┌──────┴────────┐
         │                     │ Discovery     │
         ├──────────────┬──────┤ Server        │
         │              │      │ (Eureka)      │
         │              │      │ Port 8761     │
         │              │      └───────────────┘
         │              │              ▲
         ▼              ▼              │
┌──────────────┐  ┌──────────────┐    │ Register
│   Product    │  │    Order     │    │ Services
│   Service    │  │   Service    │────┘
│  Port 8080   │  │  Port 8081   │
│  (MongoDB)   │  │  (MySQL)     │
└──────────────┘  └──────┬───────┘
                         │
                         │ Check Stock
                         ▼
                  ┌──────────────┐
                  │  Inventory   │
                  │   Service    │
                  │  Port 8082   │
                  │  (MySQL)     │
                  └──────────────┘
```

### Services Summary

| Service | Port | Database | Purpose |
|---------|------|----------|---------|
| **Discovery Server** | 8761 | N/A | Service registry (Eureka) |
| **API Gateway** | 8181 | N/A | Entry point, routing, security, RBAC |
| **Product Service** | 8080 | MongoDB | Manage product catalog |
| **Order Service** | 8081 | MySQL | Handle customer orders |
| **Inventory Service** | 8082 | MySQL | Track product availability |
| **Keycloak** | 8080 | H2/PostgreSQL | Authentication & Authorization |
| **React Frontend** | 3000 | N/A | User interface |

---

## 🔄 Service Communication Flow

### 1. **Service Discovery Pattern**

**Discovery Server (Eureka) is the Heart of Service Communication**

```
Step 1: Service Registration
├── Product Service starts → Registers with Eureka as "product-service"
├── Order Service starts → Registers with Eureka as "order-service"
├── Inventory Service starts → Registers with Eureka as "inventory-service"
└── API Gateway starts → Registers with Eureka as "api-gateway"

Step 2: Service Discovery
├── Order Service needs to call Inventory Service
├── Asks Eureka: "Where is inventory-service?"
├── Eureka responds: "inventory-service is at http://192.168.1.100:8082"
└── Order Service makes the call using WebClient

Step 3: Load Balancing
├── If multiple instances of inventory-service are running:
│   ├── Instance 1: http://192.168.1.100:8082
│   ├── Instance 2: http://192.168.1.101:8082
│   └── Instance 3: http://192.168.1.102:8082
└── Eureka provides all instances, @LoadBalanced distributes requests
```

**Why Discovery Server?**
- **Dynamic Service Locations**: Services can move to different IPs/ports
- **Scalability**: Easily add more instances of any service
- **Fault Tolerance**: If one instance dies, others handle requests
- **No Hard-coded URLs**: Services find each other automatically

---

### 2. **User Authentication Flow (OAuth2 + JWT)**

```
┌─────────┐                                    ┌──────────┐
│ User    │                                    │ Keycloak │
└────┬────┘                                    └────┬─────┘
     │                                              │
     │ 1. Click "Login"                             │
     ├──────────────────────────────────────────────►
     │                                              │
     │ 2. Redirect to Keycloak Login Page           │
     │◄──────────────────────────────────────────────┤
     │                                              │
     │ 3. Enter username/password                   │
     ├──────────────────────────────────────────────►
     │                                              │
     │ 4. Validate credentials                      │
     │    Check user exists                         │
     │    Verify password                           │
     │    Get user roles (ADMIN/USER)               │
     │◄──────────────────────────────────────────────┤
     │                                              │
     │ 5. Return JWT Token                          │
     │    Token contains:                           │
     │    - username                                │
     │    - realm_access.roles: ["ADMIN"]           │
     │    - expiry time (5 minutes)                 │
     │◄──────────────────────────────────────────────┤
     │                                              │
     │ 6. Store token in localStorage               │
     │    Store user_info with roles                │
     └──────────────────────────────────────────────┘
```

---

### 3. **Create Product Flow (Admin Only - RBAC)**

```
┌────────┐    ┌─────────┐    ┌─────────────┐    ┌─────────┐
│ Admin  │    │ React   │    │ API Gateway │    │ Product │
│  User  │    │   App   │    │   + RBAC    │    │ Service │
└───┬────┘    └────┬────┘    └──────┬──────┘    └────┬────┘
    │              │                 │                │
    │ 1. Fill form │                 │                │
    │ "Add Product"│                 │                │
    ├─────────────►│                 │                │
    │              │                 │                │
    │              │ 2. POST /api/product            │
    │              │    Headers:                      │
    │              │    Authorization: Bearer <JWT>   │
    │              │    Body: {name, description, price}
    │              ├────────────────►│                │
    │              │                 │                │
    │              │            3. Extract JWT        │
    │              │               Get realm_access.roles
    │              │               Convert to ROLE_ADMIN
    │              │                 │                │
    │              │            4. Check Authorization │
    │              │               POST /api/product   │
    │              │               requires ROLE_ADMIN │
    │              │               ✅ User has ROLE_ADMIN
    │              │                 │                │
    │              │            5. Route to           │
    │              │               product-service    │
    │              │                 ├───────────────►│
    │              │                 │                │
    │              │                 │           6. Create Product
    │              │                 │              Save to MongoDB
    │              │                 │              Generate ID
    │              │                 │                │
    │              │            7. 201 CREATED        │
    │              │◄────────────────┼────────────────┤
    │              │                 │                │
    │ 8. Success!  │                 │                │
    │◄─────────────┤                 │                │
    └──────────────┴─────────────────┴────────────────┘

If Regular User (without ADMIN role):
    Step 4: ❌ 403 Forbidden (Access Denied)
    No routing to product-service happens
```

---

### 4. **Place Order Flow (Any Authenticated User)**

```
┌──────┐  ┌───────┐  ┌─────────┐  ┌─────────┐  ┌───────────┐
│ User │  │ React │  │   API   │  │  Order  │  │ Inventory │
│      │  │  App  │  │ Gateway │  │ Service │  │  Service  │
└──┬───┘  └───┬───┘  └────┬────┘  └────┬────┘  └─────┬─────┘
   │          │            │            │             │
   │ 1. Click "Place Order"│            │             │
   ├─────────►│            │            │             │
   │          │            │            │             │
   │          │ 2. POST /api/order      │             │
   │          │    Body: {              │             │
   │          │      orderLineItemsDtoList: [         │
   │          │        {skuCode: "iphone_13",         │
   │          │         quantity: 2,                  │
   │          │         price: 999.99}                │
   │          │      ]                                │
   │          │    }                                  │
   │          ├───────────►│            │             │
   │          │            │            │             │
   │          │       3. Check JWT      │             │
   │          │          ✅ Authenticated            │
   │          │            │            │             │
   │          │       4. Route to       │             │
   │          │          order-service  │             │
   │          │            ├───────────►│             │
   │          │            │            │             │
   │          │            │       5. Generate Order  │
   │          │            │          orderNumber: UUID
   │          │            │          Create Order entity
   │          │            │            │             │
   │          │            │       6. Extract SKU Codes
   │          │            │          ["iphone_13"]   │
   │          │            │            │             │
   │          │            │       7. Call Inventory  │
   │          │            │          GET /api/inventory?skuCode=iphone_13
   │          │            │            ├────────────►│
   │          │            │            │             │
   │          │            │            │        8. Check Stock
   │          │            │            │           Query DB
   │          │            │            │           Find by skuCode
   │          │            │            │           Check quantity > 0
   │          │            │            │             │
   │          │            │            │        9. Return Response
   │          │            │            │           [{skuCode: "iphone_13",
   │          │            │            │             isInStock: true}]
   │          │            │            │◄────────────┤
   │          │            │            │             │
   │          │            │      10. Check allProductsInStock
   │          │            │          ✅ All products available
   │          │            │            │             │
   │          │            │      11. Save Order      │
   │          │            │          to MySQL        │
   │          │            │          Return "Order Placed"
   │          │            │            │             │
   │          │            │      12. 201 CREATED     │
   │          │◄───────────┼────────────┤             │
   │          │            │            │             │
   │ 13. Success!          │            │             │
   │◄─────────┤            │            │             │
   └──────────┴────────────┴────────────┴─────────────┘

If Product Not in Stock:
    Step 10: ❌ throw IllegalArgumentException
    Result: 400 Bad Request "Product is not in stock"
```

---

### 5. **Get All Products Flow (Any Authenticated User)**

```
┌──────┐  ┌───────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐
│ User │  │ React │  │   API   │  │ Product │  │ MongoDB │
│      │  │  App  │  │ Gateway │  │ Service │  │         │
└──┬───┘  └───┬───┘  └────┬────┘  └────┬────┘  └────┬────┘
   │          │            │            │            │
   │ 1. Navigate to       │            │            │
   │    /products page    │            │            │
   ├─────────►│            │            │            │
   │          │            │            │            │
   │          │ 2. GET /api/product     │            │
   │          ├───────────►│            │            │
   │          │            │            │            │
   │          │       3. Check JWT      │            │
   │          │          ✅ Authenticated           │
   │          │          (No role check for GET)    │
   │          │            │            │            │
   │          │       4. Route to       │            │
   │          │          product-service│            │
   │          │            ├───────────►│            │
   │          │            │            │            │
   │          │            │       5. Find All       │
   │          │            │          Products       │
   │          │            │            ├───────────►│
   │          │            │            │            │
   │          │            │            │      6. Query
   │          │            │            │         db.product.find({})
   │          │            │            │            │
   │          │            │            │      7. Return List
   │          │            │            │◄───────────┤
   │          │            │            │            │
   │          │            │       8. Map to Response
   │          │            │          DTOs           │
   │          │            │          [{id, name,    │
   │          │            │            description, │
   │          │            │            price}]      │
   │          │            │            │            │
   │          │            │       9. 200 OK         │
   │          │◄───────────┼────────────┤            │
   │          │            │            │            │
   │ 10. Display Products  │            │            │
   │    in UI Grid         │            │            │
   │◄─────────┤            │            │            │
   └──────────┴────────────┴────────────┴────────────┘
```

---

## 🔗 Module Interactions

### API Gateway → All Services
**Role**: Central entry point and router

**Interactions**:
1. **Receives** all external requests from React frontend
2. **Validates** JWT tokens from Keycloak
3. **Extracts** user roles from JWT (realm_access.roles)
4. **Enforces** RBAC rules:
   - POST /api/product → Only ADMIN
   - All other endpoints → Any authenticated user
5. **Routes** requests to appropriate microservices via Eureka
6. **Handles** CORS for React frontend (localhost:3000)

**Why Gateway?**
- **Single Entry Point**: Clients call one URL, not multiple services
- **Security**: Centralized authentication & authorization
- **Routing**: Dynamic routing based on Eureka discovery
- **Cross-Cutting Concerns**: CORS, logging, rate limiting in one place

---

### Order Service → Inventory Service
**Communication**: WebClient (Reactive, Non-blocking HTTP client)

**Why They Communicate**:
- Order Service needs to verify product availability before placing order
- Prevents overselling (selling items that are out of stock)
- Ensures data consistency between orders and inventory

**How They Communicate**:
```java
// In OrderService.java
WebClient.Builder webClientBuilder; // Injected

// Make HTTP call
InventoryResponse[] response = webClientBuilder.build().get()
    .uri("http://inventory-service/api/inventory",
         uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
    .retrieve()
    .bodyToMono(InventoryResponse[].class)
    .block();
```

**Key Features**:
- **@LoadBalanced**: WebClient uses Eureka for service discovery
- **Service Name**: Uses "inventory-service" (not hardcoded IP)
- **Reactive**: Non-blocking, better performance
- **Resilience**: Circuit breaker, retry, timeout patterns

---

### All Services → Discovery Server
**Pattern**: Service Registration & Discovery

**How It Works**:
1. **Startup**: Each service registers with Eureka on startup
2. **Heartbeat**: Services send heartbeat every 30 seconds
3. **Discovery**: Services query Eureka to find other services
4. **Health Check**: Eureka removes dead services automatically

**Registration Data**:
- Service Name (e.g., "order-service")
- IP Address
- Port Number
- Health Check URL
- Metadata

---

## 💾 Database Architecture

### Product Service → MongoDB
```
Database: product_db
Collection: product

Document Structure:
{
  "_id": "507f1f77bcf86cd799439011",
  "name": "iPhone 13",
  "description": "Latest Apple smartphone",
  "price": NumberDecimal("999.99")
}

Why MongoDB?
- Schema flexibility for product attributes
- Easy to add new fields (color, size, etc.)
- Fast read operations for product catalog
- Good for catalog data with varying structures
```

---

### Order Service → MySQL
```
Database: order_db

Tables:
┌──────────────────────┐
│     t_orders         │
├──────────────────────┤
│ id (PK)              │
│ order_number (UUID)  │
└──────────────────────┘
         │ 1
         │
         │ *
┌──────────────────────┐
│ t_order_line_items   │
├──────────────────────┤
│ id (PK)              │
│ sku_code             │
│ price                │
│ quantity             │
│ order_id (FK)        │
└──────────────────────┘

Why MySQL?
- ACID transactions for orders
- Relational data (Order has many OrderLineItems)
- Data integrity constraints
- Complex queries with JOINs
```

---

### Inventory Service → MySQL
```
Database: inventory_db

Table: inventory
┌──────────────────────┐
│     inventory        │
├──────────────────────┤
│ id (PK)              │
│ sku_code             │
│ quantity             │
└──────────────────────┘

Sample Data:
| id | sku_code       | quantity |
|----|----------------|----------|
| 1  | iphone_13      | 100      |
| 2  | iphone_13_red  | 0        |

Why MySQL?
- Simple structure
- Need for transactional integrity
- Fast lookups by sku_code (indexed)
- Easy to update quantities
```

---

## 🔐 Security Architecture

### OAuth2 + JWT Flow

```
┌─────────────────────────────────────────────────────────┐
│                    JWT Token Structure                   │
├─────────────────────────────────────────────────────────┤
│ Header:                                                 │
│   {                                                     │
│     "alg": "RS256",                                     │
│     "typ": "JWT"                                        │
│   }                                                     │
├─────────────────────────────────────────────────────────┤
│ Payload:                                                │
│   {                                                     │
│     "sub": "admin",                                     │
│     "preferred_username": "admin",                      │
│     "realm_access": {                                   │
│       "roles": ["ADMIN"]                                │
│     },                                                  │
│     "exp": 1699876543,                                  │
│     "iat": 1699876243                                   │
│   }                                                     │
├─────────────────────────────────────────────────────────┤
│ Signature: (Signed by Keycloak's private key)          │
└─────────────────────────────────────────────────────────┘
```

### Role-Based Access Control (RBAC)

```
┌──────────────────┬────────────┬──────────────┐
│    Endpoint      │   Admin    │ Regular User │
├──────────────────┼────────────┼──────────────┤
│ POST /product    │     ✅     │      ❌      │
│ GET /product     │     ✅     │      ✅      │
│ POST /order      │     ✅     │      ✅      │
│ GET /order       │     ✅     │      ✅      │
│ GET /inventory   │     ✅     │      ✅      │
└──────────────────┴────────────┴──────────────┘
```

---

## 🎭 Resilience Patterns

### Circuit Breaker in Order Service
```
┌─────────────────────────────────────────────────────┐
│          Circuit Breaker States                      │
├─────────────────────────────────────────────────────┤
│                                                     │
│  CLOSED (Normal)                                    │
│  ├─ Requests pass through                          │
│  ├─ Failures tracked                               │
│  └─ If failures > threshold → OPEN                 │
│                                                     │
│  OPEN (Failing)                                     │
│  ├─ Requests immediately fail                      │
│  ├─ Fallback method executed                       │
│  ├─ Wait timeout period                            │
│  └─ After timeout → HALF_OPEN                      │
│                                                     │
│  HALF_OPEN (Testing)                                │
│  ├─ Allow few test requests                        │
│  ├─ If success → CLOSED                            │
│  └─ If failure → OPEN                              │
│                                                     │
└─────────────────────────────────────────────────────┘

Implementation:
@CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
public CompletableFuture<String> placeOrder(OrderRequest request) {
    // Call inventory service
}

public CompletableFuture<String> fallbackMethod(...) {
    return "Oops! Something went wrong, please order after some time!";
}
```

### Why Resilience Patterns?
- **Prevent Cascading Failures**: If Inventory Service is down, Order Service doesn't crash
- **Better User Experience**: Show friendly message instead of error
- **System Stability**: Failed service can recover without affecting others
- **Retry Logic**: Temporary network issues are automatically retried

---

## 📊 Observability

### Distributed Tracing (Micrometer)
```java
// In OrderService
Observation inventoryServiceObservation = 
    Observation.createNotStarted("inventory-service-lookup", 
                                  this.observationRegistry);
inventoryServiceObservation.lowCardinalityKeyValue("call", "inventory-service");
return inventoryServiceObservation.observe(() -> {
    // Call inventory service
});
```

**What This Does**:
- Tracks how long inventory service call takes
- Traces request across services
- Helps identify bottlenecks
- Provides metrics for monitoring

**Trace Example**:
```
Request ID: abc-123-xyz
├─ API Gateway: 50ms
│  └─ Order Service: 200ms
│     └─ Inventory Service: 150ms
Total: 250ms
```

---

## 🚀 Deployment Architecture

### Docker Compose Setup
```yaml
services:
  discovery-server:    # Port 8761
  api-gateway:         # Port 8181
  product-service:     # Port 8080
  order-service:       # Port 8081
  inventory-service:   # Port 8082
  keycloak:           # Port 8080
  mongodb:            # Port 27017
  mysql:              # Port 3306
```

### Service Startup Order
```
1. Databases (MySQL, MongoDB) - Must start first
2. Keycloak - For authentication
3. Discovery Server (Eureka) - For service registry
4. Business Services (Product, Order, Inventory) - Register with Eureka
5. API Gateway - Routes to registered services
6. React Frontend - Consumes APIs
```

---

## 🎯 Key Architectural Decisions

### 1. Why Microservices?
- **Scalability**: Scale Product Service independently if catalog is huge
- **Technology Freedom**: Product uses MongoDB, Order uses MySQL
- **Team Independence**: Different teams can work on different services
- **Fault Isolation**: If Product Service crashes, Order still works

### 2. Why API Gateway?
- **Single Entry Point**: Clients don't need to know about all services
- **Security**: Centralized authentication and authorization
- **Routing**: Dynamic routing based on service discovery
- **Cross-Cutting**: CORS, rate limiting, logging in one place

### 3. Why Service Discovery?
- **Dynamic IPs**: Services can move, IP addresses change
- **Load Balancing**: Distribute load across multiple instances
- **Health Checks**: Automatically remove dead instances
- **No Configuration**: No need to configure service URLs

### 4. Why OAuth2 + JWT?
- **Stateless**: No session storage, tokens contain all info
- **Scalable**: Works across multiple service instances
- **Secure**: Industry-standard protocol
- **Fine-grained Control**: Roles and scopes for RBAC

### 5. Why Reactive (WebFlux)?
- **Better Performance**: Non-blocking I/O
- **Resource Efficient**: Handle more requests with fewer threads
- **Backpressure**: Handle slow consumers gracefully
- **Modern Stack**: Future-proof architecture

---

## 📈 Scalability Strategy

### Horizontal Scaling
```
Before:
Product Service (1 instance) - Port 8080

After Scaling:
Product Service - Instance 1 - Port 8080
Product Service - Instance 2 - Port 8081
Product Service - Instance 3 - Port 8082

API Gateway automatically distributes load across all 3 instances
```

### Why This Architecture Scales?
- **Stateless Services**: No session affinity needed
- **Database per Service**: No shared database bottleneck
- **Service Discovery**: Automatically finds all instances
- **Load Balancing**: Built-in with @LoadBalanced WebClient
- **Cache Ready**: Easy to add Redis for session/data caching

---

This architecture provides a solid foundation for building scalable, resilient, and maintainable microservices! 🎉
