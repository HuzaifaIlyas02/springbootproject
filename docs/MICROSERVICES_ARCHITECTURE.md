# 🏗️ Microservices Architecture Overview

## 📋 Table of Contents
1. [System Architecture](#system-architecture)
2. [Technology Stack](#technology-stack)
3. [Service Communication Flow](#service-communication-flow)
4. [Request Flow Diagrams](#request-flow-diagrams)
5. [Module Interactions](#module-interactions)
6. [Database Architecture](#database-architecture)
7. [Observability & Monitoring](#observability--monitoring)

---

## 🎯 System Architecture

### High-Level Architecture
```
┌─────────────────────────────────────────────────────────────────┐
│                     MONITORING & OBSERVABILITY                   │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐                  │
│  │Prometheus│◄───│ Grafana  │    │  Zipkin  │                  │
│  │ :9090    │    │  :3000   │    │  :9411   │                  │
│  └──────────┘    └──────────┘    └─────▲────┘                  │
└────────────────────────────────────────┼─────────────────────────┘
                                         │ Distributed Tracing
┌────────────────────────────────────────┼─────────────────────────┐
│                     CLIENT LAYER        │                         │
│  ┌─────────────────────────────────────┼──────────┐              │
│  │     React Frontend (TypeScript)     │          │              │
│  │     Port: 3000                      │          │              │
│  │     - Material-UI Components        │          │              │
│  │     - React Router                  │          │              │
│  │     - Context API (Auth, Cart)      │          │              │
│  │     - Axios HTTP Client             │          │              │
│  └─────────────────┬───────────────────┘          │              │
└────────────────────┼────────────────────────────────┼─────────────┘
                     │ HTTP/REST                     │
                     │ OAuth2 JWT                    │
┌────────────────────▼────────────────────────────────▼─────────────┐
│                  SECURITY LAYER                                   │
│  ┌───────────────────────────┐    ┌──────────────────────────┐   │
│  │      Keycloak             │    │    Keycloak MySQL        │   │
│  │      Port: 8080           │───►│    Database              │   │
│  │  - OAuth2 Authorization   │    │    - User Credentials    │   │
│  │  - JWT Token Generation   │    │    - Realm Config        │   │
│  │  - User/Admin Roles       │    │    - Client Settings     │   │
│  │  - Realm: spring-boot-    │    └──────────────────────────┘   │
│  │    microservices-realm    │                                   │
│  └───────────────────────────┘                                   │
└───────────────────────────────────────────────────────────────────┘
                     │
                     │ JWT Validation
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│                     GATEWAY LAYER                                │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │            API Gateway (Port 8181)                       │   │
│  │  ┌────────────────────────────────────────────────────┐  │   │
│  │  │ Spring Cloud Gateway (WebFlux - Reactive)          │  │   │
│  │  ├────────────────────────────────────────────────────┤  │   │
│  │  │ • JWT Token Validation (OAuth2 Resource Server)    │  │   │
│  │  │ • Role-Based Access Control (RBAC)                 │  │   │
│  │  │ • CORS Configuration (localhost:3000)              │  │   │
│  │  │ • Dynamic Routing (Service Discovery)              │  │   │
│  │  │ • Load Balancing (lb://)                           │  │   │
│  │  │ • Distributed Tracing (Micrometer + Zipkin)        │  │   │
│  │  │ • Metrics Export (Prometheus)                      │  │   │
│  │  └────────────────────────────────────────────────────┘  │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────┬───────────────────────────────────────┘
                          │
                          │ Service Registry
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                  SERVICE DISCOVERY LAYER                         │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │         Discovery Server (Eureka) - Port 8761            │   │
│  │  • Service Registration & Health Checks                  │   │
│  │  • Service Instance Management                           │   │
│  │  • Client-side Load Balancing                            │   │
│  │  • Automatic Service Discovery                           │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────┬───────────────────────────────────────┘
                          │ Register & Discover
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                  BUSINESS SERVICES LAYER                         │
│                                                                  │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────┐  │
│  │ Product Service  │  │  Order Service   │  │  Inventory   │  │
│  │   Port: 8083     │  │   Port: 8081     │  │  Service     │  │
│  │                  │  │                  │  │  Port: 8082  │  │
│  │ ┌──────────────┐ │  │ ┌──────────────┐ │  │ ┌──────────┐│  │
│  │ │REST API      │ │  │ │REST API      │ │  │ │REST API  ││  │
│  │ │- Create      │ │  │ │- Place Order │ │  │ │- Check   ││  │
│  │ │- Read        │ │  │ │- Get Orders  │ │  │ │  Stock   ││  │
│  │ │- Update      │ │  │ │- History     │ │  │ │- Update  ││  │
│  │ │- Delete      │ │  │ └──────────────┘ │  │ │  Qty     ││  │
│  │ │- Decrease Qty│ │  │                  │  │ └──────────┘│  │
│  │ └──────────────┘ │  │ ┌──────────────┐ │  │            │  │
│  │                  │  │ │WebClient     │ │  │            │  │
│  │ ┌──────────────┐ │  │ │- Inventory   │ │  │            │  │
│  │ │MongoDB       │ │  │ │  Service     │───────►          │  │
│  │ │- Document DB │ │  │ │- Product     │ │  │            │  │
│  │ │- Flexible    │ │  │ │  Service◄────┼────┼────────────┤  │
│  │ │  Schema      │ │  │ └──────────────┘ │  │            │  │
│  │ │              │ │  │                  │  │            │  │
│  │ │Collections:  │ │  │ ┌──────────────┐ │  │ ┌──────────┐│  │
│  │ │• product     │ │  │ │Resilience4j  │ │  │ │PostgreSQL││  │
│  │ │  - id        │ │  │ │- Circuit     │ │  │ │          ││  │
│  │ │  - name      │ │  │ │  Breaker     │ │  │ │Tables:   ││  │
│  │ │  - desc      │ │  │ │- Retry       │ │  │ │inventory ││  │
│  │ │  - price     │ │  │ │- TimeLimiter │ │  │ │- id      ││  │
│  │ │  - quantity  │ │  │ └──────────────┘ │  │ │- skuCode ││  │
│  │ └──────────────┘ │  │                  │  │ │- quantity││  │
│  │                  │  │ ┌──────────────┐ │  │ └──────────┘│  │
│  │ • Eureka Client  │  │ │PostgreSQL    │ │  │            │  │
│  │ • Actuator       │  │ │              │ │  │ • Eureka   │  │
│  │ • Prometheus     │  │ │Tables:       │ │  │   Client   │  │
│  │ • Zipkin Tracing │  │ │• t_orders    │ │  │ • Actuator │  │
│  │ • Testcontainers │  │ │  - id        │ │  │ • Prometheus│ │
│  │                  │  │ │  - orderNum  │ │  │ • Zipkin   │  │
│  └──────────────────┘  │ │  - username  │ │  └──────────────┘
│                        │ │  - orderDate │ │                  │
│                        │ │  - delivery  │ │                  │
│                        │ │  - phone     │ │                  │
│                        │ │  - email     │ │                  │
│                        │ │  - payment   │ │                  │
│                        │ │• t_order_    │ │                  │
│                        │ │  line_items  │ │                  │
│                        │ │  - id        │ │                  │
│                        │ │  - skuCode   │ │                  │
│                        │ │  - price     │ │                  │
│                        │ │  - quantity  │ │                  │
│                        │ │  - order_id  │ │                  │
│                        │ └──────────────┘ │                  │
│                        │                  │                  │
│                        │ • Eureka Client  │                  │
│                        │ • WebFlux        │                  │
│                        │ • Resilience4j   │                  │
│                        │ • Actuator       │                  │
│                        │ • Prometheus     │                  │
│                        │ • Zipkin Tracing │                  │
│                        └──────────────────┘                  │
└─────────────────────────────────────────────────────────────────┘
                          │
                          │ Event-Driven
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                   MESSAGE BROKER LAYER                           │
│  ┌─────────────┐          ┌────────────────────────────────┐    │
│  │  Zookeeper  │─────────►│     Apache Kafka Broker        │    │
│  │  Port: 2181 │          │     Port: 9092                 │    │
│  │             │          │  • Topic Management            │    │
│  │ • Kafka     │          │  • Message Queuing             │    │
│  │   Cluster   │          │  • Event Streaming             │    │
│  │   Coord.    │          │  • Async Communication         │    │
│  └─────────────┘          └────────────────────────────────┘    │
│                                       │                          │
│                                       │ Publish/Subscribe        │
│                                       ▼                          │
│                          ┌────────────────────────┐              │
│                          │ Notification Service   │              │
│                          │ (Not in local modules) │              │
│                          │ • Kafka Consumer       │              │
│                          │ • Email/SMS Notif.     │              │
│                          └────────────────────────┘              │
└─────────────────────────────────────────────────────────────────┘

```

### Services Summary

| Service | Port | Database | Framework | Purpose |
|---------|------|----------|-----------|---------|
| **Discovery Server** | 8761 | N/A | Spring Cloud Netflix Eureka | Service registry, health checks |
| **API Gateway** | 8181 | N/A | Spring Cloud Gateway (WebFlux) | Entry point, JWT validation, RBAC, routing |
| **Product Service** | 8083 | MongoDB | Spring Boot + Spring Data MongoDB | Product catalog CRUD, quantity management |
| **Order Service** | 8081 | PostgreSQL | Spring Boot + Spring Data JPA | Order placement, history, inter-service calls |
| **Inventory Service** | 8082 | PostgreSQL | Spring Boot + Spring Data JPA | Stock availability checking |
| **Keycloak** | 8080 | MySQL | Keycloak 18.0.0 | OAuth2/OIDC authentication, JWT tokens, user management |
| **React Frontend** | 3000 | N/A | React 19 + TypeScript + Material-UI | E-commerce UI, shopping cart, checkout |
| **Kafka Broker** | 9092 | N/A | Confluent Kafka | Event streaming, async messaging |
| **Zookeeper** | 2181 | N/A | Apache Zookeeper | Kafka cluster coordination |
| **Zipkin** | 9411 | N/A | OpenZipkin | Distributed tracing visualization |
| **Prometheus** | 9090 | N/A | Prometheus | Metrics collection & storage |
| **Grafana** | 3000 | N/A | Grafana OSS | Metrics visualization dashboards |

---

## �️ Technology Stack

### Backend Technologies
| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 21 | Programming language |
| **Spring Boot** | 3.3.5 | Microservices framework |
| **Spring Cloud** | 2023.0.3 | Cloud-native patterns |
| **Spring Cloud Gateway** | - | API Gateway (Reactive) |
| **Spring Cloud Netflix Eureka** | - | Service discovery |
| **Spring Security OAuth2** | - | JWT resource server |
| **Spring Data JPA** | - | ORM for PostgreSQL |
| **Spring Data MongoDB** | - | MongoDB ODM |
| **Spring WebFlux** | - | Reactive web framework |
| **Resilience4j** | - | Circuit breaker, retry, timeout |
| **Micrometer** | - | Observability facade |
| **Lombok** | - | Reduce boilerplate code |
| **Testcontainers** | 1.20.4 | Integration testing |

### Databases
| Database | Version | Used By | Purpose |
|----------|---------|---------|---------|
| **MongoDB** | 4.4.14 | Product Service | Document store for products |
| **PostgreSQL** | Latest | Order Service | Relational DB for orders (port 5431) |
| **PostgreSQL** | Latest | Inventory Service | Relational DB for inventory (port 5432) |
| **MySQL** | 5.7 | Keycloak | User & realm data |

### Frontend Technologies
| Technology | Version | Purpose |
|------------|---------|---------|
| **React** | 19.2.0 | UI framework |
| **TypeScript** | 4.9.5 | Type-safe JavaScript |
| **Material-UI (MUI)** | 7.3.5 | Component library |
| **React Router** | 7.9.5 | Client-side routing |
| **Axios** | 1.13.2 | HTTP client |
| **Tailwind CSS** | 4.1.17 | Utility-first CSS |

### Infrastructure & DevOps
| Technology | Version | Purpose |
|------------|---------|---------|
| **Docker** | - | Containerization |
| **Docker Compose** | 3.7 | Multi-container orchestration |
| **Apache Kafka** | 7.0.1 | Event streaming platform |
| **Zookeeper** | 7.0.1 | Kafka coordination |
| **Keycloak** | 18.0.0 | Identity & access management |
| **Zipkin** | Latest | Distributed tracing |
| **Prometheus** | 2.37.1 | Metrics collection |
| **Grafana** | 8.5.2 | Metrics visualization |
| **Maven** | - | Build tool |

### Build & Deployment
```xml
<!-- Parent POM Configuration -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.5</version>
</parent>

<!-- Modules -->
- product-service
- order-service
- inventory-service
- discovery-server
- api-gateway
```

---

## �🔄 Service Communication Flow

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
    │              │    Body: {name, description,     │
    │              │           price, quantity}       │
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
    │              │                 │              Collection: product
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

RBAC Rules in API Gateway:
- POST /api/product     → ROLE_ADMIN only
- PUT /api/product/**   → ROLE_ADMIN only
- DELETE /api/product/** → ROLE_ADMIN only
- GET /api/product/**   → Any authenticated user
```

---

### 4. **Place Order Flow (Any Authenticated User)**

```
┌──────┐  ┌───────┐  ┌─────────┐  ┌─────────┐  ┌───────────┐  ┌─────────┐
│ User │  │ React │  │   API   │  │  Order  │  │ Inventory │  │ Product │
│      │  │  App  │  │ Gateway │  │ Service │  │  Service  │  │ Service │
└──┬───┘  └───┬───┘  └────┬────┘  └────┬────┘  └─────┬─────┘  └────┬────┘
   │          │            │            │             │             │
   │ 1. Add items to cart  │            │             │             │
   │    Fill delivery info │            │             │             │
   ├─────────►│            │            │             │             │
   │          │            │            │             │             │
   │          │ 2. POST /api/order      │             │             │
   │          │    Body: {              │             │             │
   │          │      orderLineItemsDtoList: [         │             │
   │          │        {skuCode: "iphone_13",         │             │
   │          │         quantity: 2,                  │             │
   │          │         price: 999.99}                │             │
   │          │      ],                               │             │
   │          │      deliveryAddress: "...",          │             │
   │          │      phoneNumber: "...",              │             │
   │          │      email: "...",                    │             │
   │          │      paymentMethod: "CREDIT_CARD"     │             │
   │          │    }                                  │             │
   │          ├───────────►│            │             │             │
   │          │            │            │             │             │
   │          │       3. Check JWT      │             │             │
   │          │          ✅ Authenticated            │             │
   │          │            │            │             │             │
   │          │       4. Route to       │             │             │
   │          │          order-service  │             │             │
   │          │            ├───────────►│             │             │
   │          │            │            │             │             │
   │          │            │       5. Generate Order  │             │
   │          │            │          orderNumber: UUID            │
   │          │            │          Create Order entity          │
   │          │            │          Extract username from JWT    │
   │          │            │            │             │             │
   │          │            │       6. Extract SKU Codes            │
   │          │            │          ["iphone_13"]   │             │
   │          │            │            │             │             │
   │          │            │       7. Call Inventory  │             │
   │          │            │          (WebClient)     │             │
   │          │            │          GET http://inventory-service/ │
   │          │            │              api/inventory?            │
   │          │            │              skuCode=iphone_13         │
   │          │            │            ├────────────►│             │
   │          │            │            │             │             │
   │          │            │            │        8. Check Stock    │
   │          │            │            │           Query PostgreSQL
   │          │            │            │           Find by skuCode
   │          │            │            │           Check quantity > 0
   │          │            │            │             │             │
   │          │            │            │        9. Return Response│
   │          │            │            │           [{skuCode:     │
   │          │            │            │             "iphone_13", │
   │          │            │            │             isInStock: true}]
   │          │            │            │◄────────────┤             │
   │          │            │            │             │             │
   │          │            │      10. Check allProductsInStock     │
   │          │            │          ✅ All products available    │
   │          │            │            │             │             │
   │          │            │      11. Save Order      │             │
   │          │            │          to PostgreSQL   │             │
   │          │            │          (t_orders +     │             │
   │          │            │           t_order_line_items)          │
   │          │            │            │             │             │
   │          │            │      12. Update Product Quantities    │
   │          │            │          POST http://product-service/ │
   │          │            │               api/product/            │
   │          │            │               decrease-quantity       │
   │          │            │            ├─────────────┼────────────►
   │          │            │            │             │             │
   │          │            │            │             │    13. Decrease
   │          │            │            │             │        Quantity
   │          │            │            │             │        in MongoDB
   │          │            │            │◄────────────┼─────────────┤
   │          │            │            │             │             │
   │          │            │      14. Return Order Number          │
   │          │            │          "abc-123-xyz"   │             │
   │          │            │            │             │             │
   │          │            │      15. 201 CREATED     │             │
   │          │◄───────────┼────────────┤             │             │
   │          │            │            │             │             │
   │ 16. Navigate to       │            │             │             │
   │     /order-success    │            │             │             │
   │◄─────────┤            │            │             │             │
   └──────────┴────────────┴────────────┴─────────────┴─────────────┘

If Product Not in Stock:
    Step 10: ❌ throw IllegalArgumentException
    Result: "Product is not in stock, please try again later"
    
Resilience Patterns Applied:
    - @CircuitBreaker(name = "inventory")
    - @Retry(name = "inventory")
    - @TimeLimiter(name = "inventory")
    - Fallback: "Oops! Something went wrong, please order after some time!"
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
   │          │    Authorization: Bearer <JWT>       │
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
   │          │            │            price,       │
   │          │            │            quantity}]   │
   │          │            │            │            │
   │          │            │       9. 200 OK         │
   │          │◄───────────┼────────────┤            │
   │          │            │            │            │
   │ 10. Display Products  │            │            │
   │    in Material-UI Grid│            │            │
   │    with Add to Cart   │            │            │
   │◄─────────┤            │            │            │
   └──────────┴────────────┴────────────┴────────────┘
```

---

## 🔗 Module Interactions

### API Gateway → All Services
**Role**: Central entry point and intelligent router

**Routing Configuration** (application.properties):
```properties
# Product Service Route
spring.cloud.gateway.routes[0].id=product-service
spring.cloud.gateway.routes[0].uri=lb://product-service
spring.cloud.gateway.routes[0].predicates[0]=Path=/api/product/**

# Order Service Route
spring.cloud.gateway.routes[1].id=order-service
spring.cloud.gateway.routes[1].uri=lb://order-service
spring.cloud.gateway.routes[1].predicates[0]=Path=/api/order/**

# Inventory Service Route
spring.cloud.gateway.routes[2].id=inventory-service
spring.cloud.gateway.routes[2].uri=lb://inventory-service
spring.cloud.gateway.routes[2].predicates[0]=Path=/api/inventory/**

# Eureka Discovery Server
spring.cloud.gateway.routes[3].uri=http://eureka:password@localhost:8761
spring.cloud.gateway.routes[3].predicates[0]=Path=/eureka/**
```

**Security Configuration**:
```java
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
            .csrf().disable()
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeExchange(exchange ->
                exchange
                    .pathMatchers("/eureka/**").permitAll()
                    // Admin-only product operations
                    .pathMatchers(HttpMethod.POST, "/api/product").hasRole("ADMIN")
                    .pathMatchers(HttpMethod.PUT, "/api/product/**").hasRole("ADMIN")
                    .pathMatchers(HttpMethod.DELETE, "/api/product/**").hasRole("ADMIN")
                    // Anyone authenticated can view products
                    .pathMatchers(HttpMethod.GET, "/api/product/**").authenticated()
                    // Order and inventory access
                    .pathMatchers("/api/order/**").authenticated()
                    .pathMatchers("/api/inventory/**").authenticated()
                    .anyExchange().authenticated())
            .oauth2ResourceServer(spec -> 
                spec.jwt(jwt -> jwt.jwtAuthenticationConverter(
                    new KeycloakJwtAuthenticationConverter())))
            .build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);
        return source;
    }
}
```

**Key Responsibilities**:
1. **JWT Validation**: Validates JWT tokens from Keycloak
2. **Role Extraction**: Extracts roles from `realm_access.roles` in JWT
3. **RBAC Enforcement**: 
   - POST/PUT/DELETE /api/product → ROLE_ADMIN required
   - All other endpoints → Any authenticated user
4. **Service Discovery**: Uses `lb://` protocol for load-balanced routing
5. **CORS Handling**: Allows React frontend (localhost:3000)
6. **Distributed Tracing**: Integrates with Zipkin
7. **Metrics Export**: Exposes /actuator/prometheus endpoint

---

### Order Service → Inventory Service
**Communication**: WebClient (Reactive, Non-blocking HTTP client)

**Why They Communicate**:
- Order Service needs to verify product availability before placing order
- Prevents overselling (selling items that are out of stock)
- Ensures data consistency between orders and inventory

**Implementation**:
```java
@Service
@RequiredArgsConstructor
public class OrderService {
    private final WebClient.Builder webClientBuilder;
    private final ObservationRegistry observationRegistry;
    
    public String placeOrder(OrderRequest orderRequest) {
        // Extract SKU codes from order
        List<String> skuCodes = order.getOrderLineItemsList()
            .stream()
            .map(OrderLineItems::getSkuCode)
            .toList();
        
        // Create observation for distributed tracing
        Observation inventoryServiceObservation = 
            Observation.createNotStarted("inventory-service-lookup", 
                                        this.observationRegistry);
        inventoryServiceObservation.lowCardinalityKeyValue("call", "inventory-service");
        
        return inventoryServiceObservation.observe(() -> {
            // Call Inventory Service using service name (not IP)
            InventoryResponse[] inventoryResponseArray = 
                webClientBuilder.build().get()
                    .uri("http://inventory-service/api/inventory",
                         uriBuilder -> uriBuilder
                             .queryParam("skuCode", skuCodes)
                             .build())
                    .retrieve()
                    .bodyToMono(InventoryResponse[].class)
                    .block();
            
            // Check if all products are in stock
            boolean allProductsInStock = 
                Arrays.stream(inventoryResponseArray)
                    .allMatch(InventoryResponse::isInStock);
            
            if (allProductsInStock) {
                orderRepository.save(order);
                updateProductQuantities(orderRequest.getOrderLineItemsDtoList());
                return order.getOrderNumber();
            } else {
                throw new IllegalArgumentException(
                    "Product is not in stock, please try again later");
            }
        });
    }
}
```

**Controller with Resilience**:
```java
@RestController
@RequestMapping("/api/order")
public class OrderController {
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
    @TimeLimiter(name = "inventory")
    @Retry(name = "inventory")
    public CompletableFuture<String> placeOrder(@RequestBody OrderRequest orderRequest) {
        return CompletableFuture.supplyAsync(() -> 
            orderService.placeOrder(orderRequest));
    }
    
    public CompletableFuture<String> fallbackMethod(OrderRequest orderRequest, 
                                                     RuntimeException ex) {
        return CompletableFuture.supplyAsync(() -> 
            "Oops! Something went wrong, please order after some time!");
    }
}
```

**Key Features**:
- **Service Discovery**: Uses "inventory-service" name (resolved by Eureka)
- **Reactive**: Non-blocking, better resource utilization
- **Resilience4j Patterns**:
  - **Circuit Breaker**: Prevents cascading failures
  - **Retry**: Automatic retry on transient failures
  - **TimeLimiter**: Timeout protection
  - **Fallback**: User-friendly error message
- **Observability**: Micrometer observation for distributed tracing

---

### Order Service → Product Service
**Communication**: WebClient (Reactive HTTP)

**Purpose**: Update product quantities after successful order placement

**Implementation**:
```java
private void updateProductQuantities(List<OrderLineItemsDto> orderItems) {
    try {
        for (OrderLineItemsDto item : orderItems) {
            webClientBuilder.build().post()
                .uri("http://product-service/api/product/decrease-quantity")
                .bodyValue(item)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
        }
    } catch (Exception e) {
        log.error("Failed to update product quantities: {}", e.getMessage());
        // Don't fail the order, just log the error
    }
}
```

**Product Service Endpoint**:
```java
@PostMapping("/decrease-quantity")
@ResponseStatus(HttpStatus.OK)
public void decreaseQuantity(@RequestBody DecreaseQuantityRequest request) {
    productService.decreaseQuantity(request);
}
```

---

### All Services → Discovery Server
**Pattern**: Service Registration & Discovery (Netflix Eureka)

**How It Works**:
1. **Startup**: Each service registers with Eureka on startup
2. **Heartbeat**: Services send heartbeat every 30 seconds
3. **Discovery**: Services query Eureka to find other services
4. **Health Check**: Eureka removes dead services automatically

**Configuration** (Common to all services):
```properties
# Service Name
spring.application.name=product-service  # or order-service, inventory-service

# Eureka Client Configuration
eureka.client.serviceUrl.defaultZone=http://eureka:password@localhost:8761/eureka
```

**Service Registration Data**:
- Service Name (e.g., "order-service")
- IP Address
- Port Number
- Health Check URL (/actuator/health)
- Metadata

**Discovery Server Configuration**:
```java
@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServerApplication.class, args);
    }
}
```

```properties
server.port=8761
eureka.instance.hostname=localhost
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
```

**Why Service Discovery?**
- **Dynamic Service Locations**: Services can move to different IPs/ports
- **Scalability**: Easily add more instances of any service
- **Fault Tolerance**: If one instance dies, others handle requests
- **No Hard-coded URLs**: Services find each other automatically
- **Load Balancing**: Distribute load across multiple instances

---

## 💾 Database Architecture

### Product Service → MongoDB
```
Database: product_db (MongoDB 4.4.14)
Collection: product

Document Structure:
{
  "_id": ObjectId("507f1f77bcf86cd799439011"),
  "name": "iPhone 13",
  "description": "Latest Apple smartphone",
  "price": NumberDecimal("999.99"),
  "quantity": 100
}

Entity Mapping:
@Document(value = "product")
public class Product {
    @Id
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer quantity;  // NEW: Track available stock
}

Operations:
- Create: productRepository.save(product)
- Read: productRepository.findAll()
- Update: productRepository.save(updatedProduct)
- Delete: productRepository.deleteById(id)
- Decrease Qty: product.setQuantity(current - ordered)

Why MongoDB?
✅ Schema flexibility for product attributes
✅ Easy to add new fields (color, size, images, etc.)
✅ Fast read operations for product catalog
✅ Good for catalog data with varying structures
✅ Horizontal scaling for large product catalogs
```

---

### Order Service → PostgreSQL
```
Database: order-service (PostgreSQL)
Port: 5431
Connection: jdbc:postgresql://postgres-order:5431/order-service

Table Structure:

┌──────────────────────────────────────┐
│          t_orders                    │
├──────────────────────────────────────┤
│ id              BIGSERIAL PRIMARY KEY│
│ order_number    VARCHAR(255)         │
│ username        VARCHAR(255)         │
│ order_date      TIMESTAMP            │
│ delivery_address VARCHAR(500)        │
│ phone_number    VARCHAR(50)          │
│ email           VARCHAR(255)         │
│ payment_method  VARCHAR(50)          │
└──────────────────┬───────────────────┘
                   │ 1
                   │
                   │ *
┌──────────────────▼───────────────────┐
│      t_order_line_items              │
├──────────────────────────────────────┤
│ id              BIGSERIAL PRIMARY KEY│
│ sku_code        VARCHAR(255)         │
│ price           DECIMAL(19,2)        │
│ quantity        INTEGER              │
│ order_id        BIGINT FOREIGN KEY   │
└──────────────────────────────────────┘

Entity Mapping:

@Entity
@Table(name = "t_orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String orderNumber;
    private String username;
    private LocalDateTime orderDate;
    
    @OneToMany(cascade = CascadeType.ALL)
    private List<OrderLineItems> orderLineItemsList;
    
    private String deliveryAddress;
    private String phoneNumber;
    private String email;
    
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
}

@Entity
@Table(name = "t_order_line_items")
public class OrderLineItems {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String skuCode;
    private BigDecimal price;
    private Integer quantity;
}

Sample Data:
t_orders:
| id | order_number | username | order_date          | delivery_address | payment_method |
|----|--------------|----------|---------------------|------------------|----------------|
| 1  | abc-123-xyz  | john     | 2024-11-12 10:30:00 | 123 Main St     | CREDIT_CARD    |

t_order_line_items:
| id | sku_code    | price  | quantity | order_id |
|----|-------------|--------|----------|----------|
| 1  | iphone_13   | 999.99 | 2        | 1        |
| 2  | macbook_pro | 1999.99| 1        | 1        |

Why PostgreSQL?
✅ ACID transactions for orders (critical business data)
✅ Relational data (Order has many OrderLineItems)
✅ Strong data integrity constraints
✅ Complex queries with JOINs (order history, analytics)
✅ Referential integrity with foreign keys
✅ Better for financial/transactional data
```

---

### Inventory Service → PostgreSQL
```
Database: inventory-service (PostgreSQL)
Port: 5432 (default)
Connection: jdbc:postgresql://postgres-inventory:5432/inventory-service

Table Structure:

┌──────────────────────────────────────┐
│          inventory                   │
├──────────────────────────────────────┤
│ id              BIGSERIAL PRIMARY KEY│
│ sku_code        VARCHAR(255) UNIQUE  │
│ quantity        INTEGER              │
└──────────────────────────────────────┘

Entity Mapping:

@Entity
@Table(name = "inventory")
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String skuCode;
    private Integer quantity;
}

Sample Data:
| id | sku_code       | quantity |
|----|----------------|----------|
| 1  | iphone_13      | 100      |
| 2  | iphone_13_red  | 0        |
| 3  | macbook_pro    | 50       |

Repository Query:
List<Inventory> findBySkuCodeIn(List<String> skuCode);

Business Logic:
public List<InventoryResponse> isInStock(List<String> skuCode) {
    return inventoryRepository.findBySkuCodeIn(skuCode)
        .stream()
        .map(inventory -> InventoryResponse.builder()
            .skuCode(inventory.getSkuCode())
            .isInStock(inventory.getQuantity() > 0)
            .build())
        .toList();
}

Why PostgreSQL?
✅ Simple structure but needs ACID guarantees
✅ Concurrent access (multiple orders checking stock)
✅ Transactional integrity (prevent race conditions)
✅ Fast lookups by sku_code (indexed)
✅ Easy to update quantities
✅ Row-level locking for concurrent updates
```

---

### Keycloak → MySQL
```
Database: keycloak (MySQL 5.7)

Purpose:
- User credentials storage
- Realm configuration
- Client settings
- Role mappings
- Session data

Container Configuration:
keycloak-mysql:
  image: mysql:5.7
  environment:
    MYSQL_DATABASE: keycloak
    MYSQL_USER: keycloak
    MYSQL_PASSWORD: password

Keycloak Configuration:
  DB_VENDOR: MYSQL
  DB_ADDR: mysql
  DB_DATABASE: keycloak

Realm: spring-boot-microservices-realm
Roles: ADMIN, USER
Token Lifetime: 5 minutes

Why MySQL for Keycloak?
✅ Mature, stable database
✅ Good performance for auth operations
✅ Officially supported by Keycloak
✅ Persistent storage for users/realms
```

---

## 🎭 Resilience Patterns

### Circuit Breaker in Order Service
```
┌─────────────────────────────────────────────────────┐
│          Circuit Breaker States                      │
├─────────────────────────────────────────────────────┤
│                                                     │
│  CLOSED (Normal Operation)                          │
│  ├─ Requests pass through normally                 │
│  ├─ Failures are tracked                           │
│  ├─ Success rate monitored                         │
│  └─ If failures > threshold → OPEN                 │
│                                                     │
│  OPEN (Failing Fast)                                │
│  ├─ Requests immediately fail                      │
│  ├─ Fallback method executed                       │
│  ├─ Service given time to recover                  │
│  ├─ Wait timeout period (e.g., 10s)                │
│  └─ After timeout → HALF_OPEN                      │
│                                                     │
│  HALF_OPEN (Testing Recovery)                       │
│  ├─ Allow limited test requests                    │
│  ├─ Monitor success rate                           │
│  ├─ If success → CLOSED                            │
│  └─ If failure → OPEN                              │
│                                                     │
└─────────────────────────────────────────────────────┘

Implementation:
@RestController
@RequestMapping("/api/order")
public class OrderController {
    
    @PostMapping
    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
    @TimeLimiter(name = "inventory")
    @Retry(name = "inventory")
    public CompletableFuture<String> placeOrder(@RequestBody OrderRequest request) {
        return CompletableFuture.supplyAsync(() -> 
            orderService.placeOrder(request));
    }
    
    public CompletableFuture<String> fallbackMethod(OrderRequest request, 
                                                     RuntimeException ex) {
        log.info("Cannot Place Order, Executing Fallback logic");
        return CompletableFuture.supplyAsync(() -> 
            "Oops! Something went wrong, please order after some time!");
    }
}
```

### Why Resilience Patterns?
- **Prevent Cascading Failures**: If Inventory Service is down, Order Service doesn't crash
- **Better User Experience**: Show friendly message instead of error 500
- **System Stability**: Failed service can recover without affecting others
- **Retry Logic**: Temporary network issues are automatically retried
- **Timeout Protection**: Don't wait forever for slow services
- **Graceful Degradation**: System partially works even if dependencies fail

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
│     "typ": "JWT",                                       │
│     "kid": "key-id"                                     │
│   }                                                     │
├─────────────────────────────────────────────────────────┤
│ Payload:                                                │
│   {                                                     │
│     "exp": 1699876543,                                  │
│     "iat": 1699876243,                                  │
│     "jti": "token-unique-id",                           │
│     "iss": "http://localhost:8080/realms/              │
│             spring-boot-microservices-realm",           │
│     "sub": "user-uuid",                                 │
│     "typ": "Bearer",                                    │
│     "azp": "spring-cloud-client",                       │
│     "preferred_username": "admin",                      │
│     "email_verified": true,                             │
│     "email": "admin@example.com",                       │
│     "realm_access": {                                   │
│       "roles": ["ADMIN", "USER"]                        │
│     },                                                  │
│     "scope": "openid email profile"                     │
│   }                                                     │
├─────────────────────────────────────────────────────────┤
│ Signature: (Signed by Keycloak's RS256 private key)    │
└─────────────────────────────────────────────────────────┘
```

### Role-Based Access Control (RBAC)

```
┌──────────────────────┬────────────┬──────────────┐
│      Endpoint        │   Admin    │ Regular User │
├──────────────────────┼────────────┼──────────────┤
│ POST /api/product    │     ✅     │      ❌      │
│ PUT /api/product/**  │     ✅     │      ❌      │
│ DELETE /api/product/**│    ✅     │      ❌      │
│ GET /api/product     │     ✅     │      ✅      │
│ GET /api/product/**  │     ✅     │      ✅      │
│ POST /api/order      │     ✅     │      ✅      │
│ GET /api/order/**    │     ✅     │      ✅      │
│ GET /api/inventory   │     ✅     │      ✅      │
└──────────────────────┴────────────┴──────────────┘
```

### Keycloak Configuration
```
Realm: spring-boot-microservices-realm
Client ID: spring-cloud-client
Client Protocol: openid-connect
Access Type: public (for React SPA)

Roles:
- ADMIN: Full access to all operations
- USER: Read access + order placement

Token Settings:
- Access Token Lifespan: 5 minutes
- Refresh Token Enabled: Yes
- Standard Flow Enabled: Yes
- Direct Access Grants: Yes

Valid Redirect URIs:
- http://localhost:3000/*
- http://localhost:8181/*

Web Origins:
- http://localhost:3000
```

---

## 📊 Observability & Monitoring
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

---

## 📊 Observability & Monitoring

### Distributed Tracing (Zipkin + Micrometer)

**Architecture**:
```
┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│   Order     │─────►│  Inventory  │─────►│   Product   │
│   Service   │      │   Service   │      │   Service   │
└──────┬──────┘      └──────┬──────┘      └──────┬──────┘
       │                    │                     │
       │ Send Spans         │ Send Spans          │ Send Spans
       ▼                    ▼                     ▼
┌──────────────────────────────────────────────────────┐
│              Zipkin Server (Port 9411)               │
│  • Collects traces from all services                │
│  • Visualizes request flow across services          │
│  • Shows latency breakdown                          │
│  • Identifies bottlenecks                           │
└──────────────────────────────────────────────────────┘
```

**Implementation in OrderService**:
```java
@Service
@RequiredArgsConstructor
public class OrderService {
    private final ObservationRegistry observationRegistry;
    
    public String placeOrder(OrderRequest orderRequest) {
        // Create observation for distributed tracing
        Observation inventoryServiceObservation = 
            Observation.createNotStarted("inventory-service-lookup", 
                                        this.observationRegistry);
        inventoryServiceObservation.lowCardinalityKeyValue("call", "inventory-service");
        
        return inventoryServiceObservation.observe(() -> {
            // This code is traced
            return webClientBuilder.build().get()
                .uri("http://inventory-service/api/inventory")
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();
        });
    }
}
```

**Configuration**:
```properties
# All Services
management.zipkin.tracing.endpoint=http://localhost:9411/api/v2/spans
management.tracing.sampling.probability=1.0  # 100% sampling
logging.pattern.level=%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]
```

**Trace Example**:
```
Request ID: abc-123-xyz (traceId)
├─ API Gateway: 50ms (spanId: 001)
│  └─ Order Service: 200ms (spanId: 002)
│     ├─ Inventory Service: 150ms (spanId: 003)
│     └─ Product Service: 100ms (spanId: 004)
Total: 250ms

Visualized in Zipkin UI:
http://localhost:9411
```

---

### Metrics Collection (Prometheus + Grafana)

**Architecture**:
```
┌─────────────────────────────────────────────────────┐
│              Business Services                       │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐          │
│  │ Product  │  │  Order   │  │Inventory │          │
│  │ Service  │  │ Service  │  │ Service  │          │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘          │
│       │             │             │                 │
│       │ /actuator/prometheus      │                 │
│       └─────────────┼─────────────┘                 │
└─────────────────────┼───────────────────────────────┘
                      │ Scrape Metrics (every 10s)
                      ▼
┌─────────────────────────────────────────────────────┐
│         Prometheus Server (Port 9090)               │
│  • Scrapes /actuator/prometheus endpoints          │
│  • Stores time-series data                         │
│  • Provides PromQL query language                  │
│  • Retention: 15 days default                      │
└─────────────────────┬───────────────────────────────┘
                      │ Query Metrics
                      ▼
┌─────────────────────────────────────────────────────┐
│           Grafana Dashboard (Port 3000)             │
│  • Visualizes metrics from Prometheus              │
│  • Pre-built dashboards                            │
│  • Alerts on thresholds                            │
│  • Real-time monitoring                            │
└─────────────────────────────────────────────────────┘
```

**Prometheus Configuration** (prometheus.yml):
```yaml
global:
  scrape_interval: 10s
  evaluation_interval: 10s

scrape_configs:
  - job_name: 'product_service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['product-service:8080']
        labels:
          application: 'Product Service Application'
          
  - job_name: 'order_service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['order-service:8080']
        labels:
          application: 'Order Service Application'
          
  - job_name: 'inventory_service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['inventory-service:8080']
        labels:
          application: 'Inventory Service Application'
```

**Service Configuration**:
```properties
# Actuator Configuration
management.endpoints.web.exposure.include=prometheus,health,info
management.metrics.export.prometheus.enabled=true

# Dependencies
spring-boot-starter-actuator
micrometer-registry-prometheus
```

**Available Metrics**:
- **JVM Metrics**: Memory usage, GC pauses, thread count
- **HTTP Metrics**: Request count, latency, error rate
- **Database Metrics**: Connection pool, query performance
- **Custom Metrics**: Business-specific metrics (orders placed, products sold)

**Grafana Dashboards**:
- JVM (Micrometer): Dashboard ID 4701
- Spring Boot Statistics: Dashboard ID 6756
- Custom dashboards for business metrics

---

### Health Checks & Actuator

**Endpoints**:
```
GET http://localhost:8083/actuator/health
Response:
{
  "status": "UP",
  "components": {
    "mongo": {
      "status": "UP"
    },
    "diskSpace": {
      "status": "UP"
    }
  }
}

GET http://localhost:8081/actuator/prometheus
Response: (Prometheus-formatted metrics)
# HELP jvm_memory_used_bytes The amount of used memory
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{area="heap",id="G1 Eden Space",} 3.3554432E7

GET http://localhost:8081/actuator/info
Response: Application information
```

---

## 🚀 Deployment Architecture

### Docker Compose Setup

**Services Startup Order**:
```
1. Infrastructure Services (Databases & Supporting)
   ├─ keycloak-mysql (MySQL for Keycloak)
   ├─ postgres-order (PostgreSQL for Order Service - Port 5431)
   ├─ postgres-inventory (PostgreSQL for Inventory - Port 5432)
   ├─ mongo (MongoDB for Product Service - Port 27017)
   ├─ zookeeper (Kafka coordination - Port 2181)
   ├─ broker (Kafka - Port 9092)
   └─ zipkin (Distributed Tracing - Port 9411)

2. Authentication & Discovery
   ├─ keycloak (OAuth2 Server - Port 8080)
   │   depends_on: keycloak-mysql
   └─ discovery-server (Eureka - Port 8761)
       depends_on: zipkin

3. API Gateway
   └─ api-gateway (Port 8181)
       depends_on: zipkin, discovery-server, keycloak

4. Business Services
   ├─ product-service (Port 8083)
   │   depends_on: mongo, discovery-server, api-gateway
   ├─ order-service (Port 8081)
   │   depends_on: postgres-order, broker, zipkin, discovery-server, api-gateway
   ├─ inventory-service (Port 8082)
   │   depends_on: postgres-inventory, discovery-server, api-gateway
   └─ notification-service (Port not exposed)
       depends_on: zipkin, broker, discovery-server, api-gateway

5. Monitoring Stack
   ├─ prometheus (Port 9090)
   │   depends_on: all business services
   └─ grafana (Port 3000)
       depends_on: prometheus
```

**Environment Variables**:
```yaml
product-service:
  environment:
    - SPRING_PROFILES_ACTIVE=docker
    
order-service:
  environment:
    - SPRING_PROFILES_ACTIVE=docker
    - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-order:5431/order-service
    
inventory-service:
  environment:
    - SPRING_PROFILES_ACTIVE=docker
    - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-inventory:5432/inventory-service
```

**Docker Images**:
- All services use custom Docker images: `huzaifa02/{service-name}:latest`
- Built from Dockerfile in each service directory
- Base image: `eclipse-temurin:17-jre-alpine` (lightweight JDK 17)

---

## 🎯 Key Architectural Decisions

### 1. Why Microservices?
- **Scalability**: Scale Product Service independently if catalog is huge
- **Technology Freedom**: Product uses MongoDB, Order uses PostgreSQL
- **Team Independence**: Different teams can work on different services
- **Fault Isolation**: If Product Service crashes, Order Service still works
- **Deployment Independence**: Deploy services separately without downtime

### 2. Why API Gateway?
- **Single Entry Point**: Clients call one URL, not multiple services
- **Security**: Centralized authentication & authorization
- **Routing**: Dynamic routing based on service discovery
- **Cross-Cutting Concerns**: CORS, rate limiting, logging in one place
- **Load Balancing**: Distribute load across service instances
- **Protocol Translation**: Can translate between protocols if needed

### 3. Why Service Discovery?
- **Dynamic IPs**: Services can move, IP addresses change in containers
- **Load Balancing**: Distribute load across multiple instances
- **Health Checks**: Automatically remove unhealthy instances
- **No Configuration**: No need to configure service URLs manually
- **Auto-scaling**: New instances automatically registered

### 4. Why OAuth2 + JWT?
- **Stateless**: No session storage, tokens contain all info
- **Scalable**: Works across multiple service instances
- **Secure**: Industry-standard protocol with RS256 signing
- **Fine-grained Control**: Roles and scopes for RBAC
- **SSO Support**: Single Sign-On across multiple applications

### 5. Why Reactive (WebFlux)?
- **Better Performance**: Non-blocking I/O for high throughput
- **Resource Efficient**: Handle more requests with fewer threads
- **Backpressure**: Handle slow consumers gracefully
- **Modern Stack**: Future-proof architecture
- **Ideal for API Gateway**: Perfect for routing and proxying

### 6. Why PostgreSQL for Orders & Inventory?
- **ACID Transactions**: Critical for financial data
- **Relational Integrity**: Foreign keys, constraints
- **Mature Ecosystem**: Excellent tooling and support
- **Performance**: Great for transactional workloads
- **Concurrent Access**: Row-level locking

### 7. Why MongoDB for Products?
- **Flexible Schema**: Product attributes vary widely
- **Fast Reads**: Product catalog has high read traffic
- **Horizontal Scaling**: Sharding for large catalogs
- **Document Model**: Natural fit for product data
- **Easy Evolution**: Add new fields without migration

### 8. Why Kafka?
- **Event Streaming**: Decouple services with events
- **High Throughput**: Handle millions of messages
- **Durability**: Messages persisted to disk
- **Scalability**: Partition for parallel processing
- **Real-time**: Low latency message delivery

---

## 📈 Scalability Strategy

### Horizontal Scaling

**Before Scaling**:
```
Product Service (1 instance) - Port 8083
Order Service (1 instance) - Port 8081
Inventory Service (1 instance) - Port 8082
```

**After Scaling**:
```
Product Service:
├─ Instance 1 (Container 1) - 192.168.1.10:8083
├─ Instance 2 (Container 2) - 192.168.1.11:8083
└─ Instance 3 (Container 3) - 192.168.1.12:8083

API Gateway automatically distributes load:
- Round Robin (default)
- Random
- Least Connections
- Custom strategies
```

**Docker Compose Scaling**:
```bash
docker-compose up --scale product-service=3
docker-compose up --scale order-service=2
docker-compose up --scale inventory-service=2
```

### Why This Architecture Scales?
✅ **Stateless Services**: No session affinity needed
✅ **Database per Service**: No shared database bottleneck
✅ **Service Discovery**: Automatically finds all instances
✅ **Load Balancing**: Built-in with Spring Cloud LoadBalancer
✅ **Reactive Stack**: Non-blocking I/O for better resource utilization
✅ **Message Queue**: Async communication with Kafka
✅ **Cache Ready**: Easy to add Redis for session/data caching
✅ **Container Ready**: Easy to deploy to Kubernetes

---

## 🎉 Conclusion

This microservices architecture provides:
- ✅ **Scalability**: Independent scaling of each service
- ✅ **Resilience**: Circuit breakers, retries, fallbacks
- ✅ **Security**: OAuth2, JWT, RBAC at gateway
- ✅ **Observability**: Distributed tracing, metrics, logging
- ✅ **Flexibility**: Multiple databases, technology choices
- ✅ **Maintainability**: Clear separation of concerns
- ✅ **Developer Experience**: Hot reload, testcontainers, clear documentation

**Access Points**:
- React Frontend: http://localhost:3000
- API Gateway: http://localhost:8181
- Keycloak: http://localhost:8080
- Eureka Dashboard: http://localhost:8761
- Zipkin Dashboard: http://localhost:9411
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (different from React)

This architecture is production-ready and can scale to handle thousands of requests per second! 🚀
