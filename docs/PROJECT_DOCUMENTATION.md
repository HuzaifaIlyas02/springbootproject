# 🏗️ E-Commerce Microservices Platform - Complete Project Documentation

## 📋 Table of Contents
- [Project Overview](#project-overview)
- [System Architecture](#system-architecture)
- [Technology Stack](#technology-stack)
- [Microservices Breakdown](#microservices-breakdown)
- [Security Implementation](#security-implementation)
- [Communication Patterns](#communication-patterns)
- [Resilience & Fault Tolerance](#resilience--fault-tolerance)
- [Event-Driven Architecture](#event-driven-architecture)
- [Database Design](#database-design)
- [API Gateway](#api-gateway)
- [Service Discovery](#service-discovery)
- [Observability & Monitoring](#observability--monitoring)
- [Frontend Integration](#frontend-integration)
- [Deployment Strategy](#deployment-strategy)
- [Key Features](#key-features)
- [Project Structure](#project-structure)
- [How to Run](#how-to-run)
- [Future Enhancements](#future-enhancements)

---

## 🎯 Project Overview

This is a **production-ready, cloud-native e-commerce platform** built using **microservices architecture**. The system demonstrates modern software engineering practices including:

- **Distributed Systems Architecture**
- **OAuth2/JWT Authentication & Authorization**
- **Event-Driven Communication with Apache Kafka**
- **Service Discovery & Load Balancing**
- **API Gateway Pattern**
- **Circuit Breaker & Resilience Patterns**
- **Distributed Tracing & Monitoring**
- **Polyglot Persistence (Multiple Database Technologies)**
- **Containerization with Docker**
- **React-based Modern Frontend**

### Business Use Case
An online e-commerce platform where:
- **Users** can browse products, place orders, and view their order history
- **Administrators** can manage product catalog (CRUD operations)
- **System** automatically checks inventory availability before order placement
- **Services** communicate asynchronously for better scalability and fault tolerance

---

## 🏛️ System Architecture

### High-Level Architecture Diagram

```
┌──────────────────────────────────────────────────────────────┐
│                    CLIENT LAYER                              │
│  ┌────────────────────────────────────────────────────────┐  │
│  │    React SPA (TypeScript) - Port 3000                  │  │
│  │  • Material-UI Components                              │  │
│  │  • Axios HTTP Client                                   │  │
│  │  • JWT Token Management                                │  │
│  │  • React Router for Navigation                         │  │
│  └────────────────────────────────────────────────────────┘  │
└────────────────────────┬─────────────────────────────────────┘
                         │ HTTP/REST + JWT
                         ▼
┌──────────────────────────────────────────────────────────────┐
│                    AUTHENTICATION                            │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  Keycloak (OAuth2/OIDC Server) - Port 8080            │  │
│  │  • User Management                                     │  │
│  │  • JWT Token Generation                                │  │
│  │  • Role-Based Access Control (RBAC)                    │  │
│  │  • MySQL Backend Storage                               │  │
│  └────────────────────────────────────────────────────────┘  │
└────────────────────────┬─────────────────────────────────────┘
                         │ JWT Tokens
                         ▼
┌──────────────────────────────────────────────────────────────┐
│                    GATEWAY LAYER                             │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  API Gateway (Spring Cloud Gateway) - Port 8181       │  │
│  │  • JWT Token Validation                                │  │
│  │  • RBAC Enforcement                                    │  │
│  │  • Dynamic Routing                                     │  │
│  │  • Load Balancing                                      │  │
│  │  • CORS Configuration                                  │  │
│  └────────────────────────────────────────────────────────┘  │
└────────────────────────┬─────────────────────────────────────┘
                         │ Eureka Service Discovery
                         ▼
┌──────────────────────────────────────────────────────────────┐
│                 SERVICE DISCOVERY                            │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  Eureka Server (Netflix) - Port 8761                   │  │
│  │  • Service Registration                                │  │
│  │  • Health Monitoring                                   │  │
│  │  • Client-Side Load Balancing                          │  │
│  └────────────────────────────────────────────────────────┘  │
└────────────────────────┬─────────────────────────────────────┘
                         │ Service Registry
                         ▼
┌──────────────────────────────────────────────────────────────┐
│               BUSINESS SERVICES LAYER                        │
│                                                              │
│  ┌───────────────┐  ┌───────────────┐  ┌─────────────────┐ │
│  │ Product       │  │ Order         │  │ Inventory       │ │
│  │ Service       │  │ Service       │  │ Service         │ │
│  │ Port: 8083    │  │ Port: 8081    │  │ Port: 8082      │ │
│  │               │  │               │  │                 │ │
│  │ MongoDB       │  │ PostgreSQL    │  │ PostgreSQL      │ │
│  │ • Products    │  │ • Orders      │  │ • Stock Levels  │ │
│  │ • CRUD Ops    │  │ • History     │  │ • Availability  │ │
│  │               │  │ • Resilience4j│  │                 │ │
│  └───────────────┘  └───────────────┘  └─────────────────┘ │
└────────────────────────┬─────────────────────────────────────┘
                         │ Kafka Event Bus
                         ▼
┌──────────────────────────────────────────────────────────────┐
│                 MESSAGE BROKER LAYER                         │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  Apache Kafka + Zookeeper - Port 9092                  │  │
│  │  • Topic: order-placed                                 │  │
│  │  • Asynchronous Event Processing                       │  │
│  │  • Decoupled Service Communication                     │  │
│  └────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────┘
```

---

## 💻 Technology Stack

### Backend Technologies

| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 21 | Primary programming language |
| **Spring Boot** | 3.3.5 | Microservices framework |
| **Spring Cloud** | 2023.0.3 | Cloud-native patterns & tools |
| **Spring Cloud Gateway** | - | Reactive API Gateway |
| **Spring Cloud Netflix Eureka** | - | Service discovery & registration |
| **Spring Security OAuth2** | - | JWT resource server |
| **Spring Data JPA** | - | PostgreSQL ORM |
| **Spring Data MongoDB** | - | MongoDB ODM |
| **Spring WebFlux** | - | Reactive programming |
| **Spring Kafka** | - | Event streaming |
| **Resilience4j** | - | Circuit breaker, retry, timeout |
| **Micrometer** | - | Observability & metrics |
| **Lombok** | - | Reduce boilerplate code |
| **Maven** | 3.9.9 | Build & dependency management |

### Infrastructure & Tools

| Technology | Version | Purpose |
|------------|---------|---------|
| **Keycloak** | 18.0.0 | Identity & access management |
| **Apache Kafka** | 3.7.0 | Event streaming platform |
| **Zookeeper** | 3.9.2 | Kafka coordination |
| **PostgreSQL** | Latest | Relational database |
| **MongoDB** | 4.4.14 | NoSQL document database |
| **MySQL** | 5.7 | Keycloak backend storage |
| **Docker** | - | Containerization |
| **Docker Compose** | - | Multi-container orchestration |

### Frontend Technologies

| Technology | Version | Purpose |
|------------|---------|---------|
| **React** | 19.2.0 | UI framework |
| **TypeScript** | 4.9.5 | Type-safe JavaScript |
| **Material-UI (MUI)** | 7.3.5 | Component library |
| **Axios** | 1.13.2 | HTTP client |
| **React Router** | 7.9.5 | Client-side routing |
| **Tailwind CSS** | 4.1.17 | Utility-first CSS |

---

## 🔧 Microservices Breakdown

### 1. **Discovery Server (Eureka)**
**Port:** 8761  
**Purpose:** Service registry and health monitoring

**Key Responsibilities:**
- Service registration when microservices start
- Service discovery for inter-service communication
- Health check monitoring (heartbeat every 30s)
- Load balancing support
- Automatic removal of dead instances

**Technologies:**
- Spring Cloud Netflix Eureka Server
- Spring Boot Actuator for health endpoints

**Configuration Highlights:**
```properties
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
eureka.server.enable-self-preservation=false
```

---

### 2. **API Gateway**
**Port:** 8181 (externally), 8080 (internal)  
**Purpose:** Single entry point for all client requests

**Key Responsibilities:**
- **JWT Token Validation**: Validates tokens from Keycloak
- **Role-Based Access Control (RBAC)**: Enforces admin/user permissions
- **Dynamic Routing**: Routes requests to appropriate services using Eureka
- **Load Balancing**: Distributes traffic across service instances
- **CORS Handling**: Allows React frontend (localhost:3000)
- **Distributed Tracing**: Integrates with Zipkin for request tracking

**Security Rules:**
```java
// Admin-only endpoints
.pathMatchers(HttpMethod.POST, "/api/product").hasRole("ADMIN")
.pathMatchers(HttpMethod.PUT, "/api/product/**").hasRole("ADMIN")
.pathMatchers(HttpMethod.DELETE, "/api/product/**").hasRole("ADMIN")

// Authenticated users
.pathMatchers(HttpMethod.GET, "/api/product/**").authenticated()
.pathMatchers("/api/order/**").authenticated()
.pathMatchers("/api/inventory/**").authenticated()
```

**Technologies:**
- Spring Cloud Gateway (WebFlux - Reactive)
- Spring Security OAuth2 Resource Server
- Custom JWT Converter for Keycloak roles

**Routes Configuration:**
```properties
# Product Service
spring.cloud.gateway.routes[0].uri=lb://product-service
spring.cloud.gateway.routes[0].predicates[0]=Path=/api/product/**

# Order Service
spring.cloud.gateway.routes[1].uri=lb://order-service
spring.cloud.gateway.routes[1].predicates[0]=Path=/api/order/**

# Inventory Service
spring.cloud.gateway.routes[2].uri=lb://inventory-service
spring.cloud.gateway.routes[2].predicates[0]=Path=/api/inventory/**
```

---

### 3. **Product Service**
**Port:** 8083  
**Database:** MongoDB  
**Purpose:** Product catalog management

**Key Responsibilities:**
- Create, Read, Update, Delete (CRUD) products
- Manage product quantities
- Decrease quantity after order placement
- Store product images as base64 strings

**Entity Model:**
```java
@Document(collection = "product")
public class Product {
    @Id
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private String image;  // Base64 encoded
    private Integer quantity;
}
```

**REST Endpoints:**
```
POST   /api/product              - Create product (Admin only)
GET    /api/product              - Get all products
GET    /api/product/{id}         - Get product by ID
PUT    /api/product/{id}         - Update product (Admin only)
DELETE /api/product/{id}         - Delete product (Admin only)
POST   /api/product/decrease-quantity - Decrease product quantity
```

**Technologies:**
- Spring Data MongoDB
- Lombok for reducing boilerplate
- Spring Boot Actuator

---

### 4. **Order Service**
**Port:** 8081  
**Database:** PostgreSQL  
**Purpose:** Order management and orchestration

**Key Responsibilities:**
- Place orders with inventory validation
- Store order details (items, delivery info, payment method)
- Extract username from JWT token for order attribution
- Call Inventory Service to check stock availability
- Call Product Service to decrease quantities
- Publish order events to Kafka
- Resilience patterns (Circuit Breaker, Retry, Timeout)
- Provide order history (all orders for admin, user-specific orders)

**Entity Model:**
```java
@Entity
@Table(name = "t_orders")
public class Order {
    @Id @GeneratedValue
    private Long id;
    private String orderNumber;
    private String username;  // Extracted from JWT
    private LocalDateTime orderDate;
    
    @OneToMany(cascade = CascadeType.ALL)
    private List<OrderLineItems> orderLineItemsList;
    
    // Delivery details
    private String deliveryAddress;
    private String phoneNumber;
    private String email;
    
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
}
```

**JWT Username Resolution:**
```java
@Component
public class JwtUsernameResolver {
    public String resolveUsername(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        JsonNode payload = decodeJWT(token);
        
        // Priority: preferred_username > email > sub
        if (payload.hasNonNull("preferred_username"))
            return payload.get("preferred_username").asText();
        // fallback to email or sub...
    }
}
```

**REST Endpoints:**
```
POST   /api/order              - Place order (Authenticated)
GET    /api/order/history      - Get user's order history
GET    /api/order/all          - Get all orders (Admin only)
```

**Resilience Configuration:**
```java
@PostMapping
@CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
@TimeLimiter(name = "inventory")
@Retry(name = "inventory")
public CompletableFuture<String> placeOrder(@RequestBody OrderRequest request) {
    return CompletableFuture.supplyAsync(() -> 
        orderService.placeOrder(request, username));
}

public CompletableFuture<String> fallbackMethod(...) {
    return CompletableFuture.supplyAsync(() -> 
        "Oops! Something went wrong, please order after some time!");
}
```

**Technologies:**
- Spring Data JPA with PostgreSQL
- Spring WebFlux (WebClient for reactive HTTP calls)
- Resilience4j (Circuit Breaker, Retry, TimeLimiter)
- Spring Kafka (Producer)
- Micrometer for distributed tracing

---

### 5. **Inventory Service**
**Port:** 8082  
**Database:** PostgreSQL  
**Purpose:** Stock availability management

**Key Responsibilities:**
- Check if products are in stock
- Return availability status for multiple SKU codes
- Consume order events from Kafka
- Pre-loaded with sample inventory data

**Entity Model:**
```java
@Entity
@Table(name = "t_inventory")
public class Inventory {
    @Id @GeneratedValue
    private Long id;
    private String skuCode;
    private Integer quantity;
}
```

**REST Endpoints:**
```
GET /api/inventory?skuCode=iphone-13&skuCode=samsung-s23
Response: [
    {"skuCode": "iphone-13", "inStock": true},
    {"skuCode": "samsung-s23", "inStock": false}
]
```

**Kafka Consumer:**
```java
@Component
public class OrderKafkaListener {
    @KafkaListener(topics = "order-placed", groupId = "inventory-service")
    public void handleOrderMessage(String orderNumber) {
        log.info("Received Kafka message: {}", orderNumber);
        // Future: Update inventory, send notifications
    }
}
```

**Technologies:**
- Spring Data JPA with PostgreSQL
- Spring Kafka (Consumer)
- CommandLineRunner for data initialization

---

## 🔐 Security Implementation

### OAuth2 + JWT Authentication Flow

```
┌─────────────┐
│   User      │
│ (Frontend)  │
└──────┬──────┘
       │ 1. Login Request
       │    (username + password)
       ▼
┌──────────────────────┐
│    Keycloak          │
│ (Auth Server)        │
│ Port: 8080           │
└──────┬───────────────┘
       │ 2. Validate Credentials
       │ 3. Generate JWT Token
       │
       ▼
┌──────────────────────────────────┐
│  JWT Token Structure             │
├──────────────────────────────────┤
│ Header:                          │
│ {                                │
│   "alg": "RS256",                │
│   "typ": "JWT"                   │
│ }                                │
│                                  │
│ Payload:                         │
│ {                                │
│   "sub": "user-uuid",            │
│   "preferred_username": "admin", │
│   "email": "admin@example.com",  │
│   "realm_access": {              │
│     "roles": ["ADMIN"]           │
│   },                             │
│   "exp": 1700000000              │
│ }                                │
│                                  │
│ Signature: (RSA-signed)          │
└──────────────────────────────────┘
       │
       │ 4. Return Token to Frontend
       ▼
┌─────────────┐
│  Frontend   │
│  Stores JWT │
│  in Memory  │
└──────┬──────┘
       │ 5. API Request
       │    Authorization: Bearer <JWT>
       ▼
┌──────────────────────┐
│   API Gateway        │
│  Port: 8181          │
├──────────────────────┤
│ 6. Validate JWT      │
│    - Verify Signature│
│    - Check Expiration│
│    - Extract Roles   │
├──────────────────────┤
│ 7. RBAC Check        │
│    - Admin? Allow    │
│    - User? Check     │
│      permissions     │
└──────┬───────────────┘
       │ 8. Forward to Service
       │    (with JWT in header)
       ▼
┌──────────────────────┐
│  Product/Order/      │
│  Inventory Service   │
├──────────────────────┤
│ 9. Extract Username  │
│    from JWT          │
│ 10. Process Request  │
└──────────────────────┘
```

### Keycloak Configuration

**Realm:** `spring-boot-microservices-realm`

**Client Configuration:**
```
Client ID: spring-cloud-client
Client Protocol: openid-connect
Access Type: public
Valid Redirect URIs: http://localhost:3000/*
Web Origins: http://localhost:3000
```

**Roles:**
- **ADMIN**: Full CRUD access to products, view all orders
- **USER**: Read products, place orders, view own order history

**Token Configuration:**
- Access Token Lifespan: 5 minutes
- Refresh Token: Enabled
- JWT Claims: preferred_username, email, realm_access (roles)

### API Gateway Security

**JWT Validation:**
```java
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(
            ServerHttpSecurity serverHttpSecurity) {
        serverHttpSecurity
            .oauth2ResourceServer(spec -> spec.jwt(
                jwtSpec -> jwtSpec.jwtAuthenticationConverter(
                    new KeycloakJwtAuthenticationConverter())));
        return serverHttpSecurity.build();
    }
}
```

**Custom JWT Converter:**
```java
public class KeycloakJwtAuthenticationConverter 
        implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {
    
    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        return Mono.just(new JwtAuthenticationToken(jwt, authorities));
    }
    
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        List<String> roles = (List<String>) realmAccess.get("roles");
        
        return roles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
            .collect(Collectors.toList());
    }
}
```

---

## 🔄 Communication Patterns

### 1. **Synchronous Communication (REST/HTTP)**

**Use Case:** Order Service → Inventory Service

```java
// WebClient Configuration
@Configuration
public class WebClientConfig {
    @Bean
    @LoadBalanced  // Enables client-side load balancing
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}

// Service-to-Service Call
@Service
public class OrderService {
    private final WebClient.Builder webClientBuilder;
    
    public String placeOrder(OrderRequest request) {
        // Call Inventory Service using service name (not IP/port)
        InventoryResponse[] responses = webClientBuilder.build()
            .get()
            .uri("http://inventory-service/api/inventory",
                 uriBuilder -> uriBuilder
                     .queryParam("skuCode", skuCodes)
                     .build())
            .retrieve()
            .bodyToMono(InventoryResponse[].class)
            .block();
        
        boolean allInStock = Arrays.stream(responses)
            .allMatch(InventoryResponse::isInStock);
        
        if (!allInStock) {
            throw new OrderProcessingException("Out of stock!");
        }
        
        // Continue with order...
    }
}
```

**Why WebClient over RestTemplate?**
- **Non-blocking**: Better resource utilization
- **Reactive**: Supports reactive streams
- **Modern**: RestTemplate is in maintenance mode
- **Flexible**: Better error handling and retries

### 2. **Asynchronous Communication (Kafka)**

**Use Case:** Order Service → Inventory Service (Event Notification)

**Architecture Flow:**
```
Order Placed
     │
     ▼
Order Service
     │
     ├─→ Save to Database (PostgreSQL)
     │
     └─→ Publish Event to Kafka
         Topic: order-placed
         Message: "946e8f88-6b4e-482e-b35d-1e8a6a21c3ff"
              │
              ▼
         Kafka Broker
              │
              ▼
     Inventory Service (Consumer)
         Logs: "Received: 946e8f88..."
         Future: Update stock, send notifications
```

**Producer (Order Service):**
```java
@Component
@RequiredArgsConstructor
public class OrderEventProducer {
    @Value("${app.topics.order-placed:order-placed}")
    private String orderPlacedTopic;
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    
    public void sendOrderPlaced(String orderNumber) {
        kafkaTemplate.send(orderPlacedTopic, orderNumber);
    }
}
```

**Consumer (Inventory Service):**
```java
@Component
@Slf4j
public class OrderKafkaListener {
    @KafkaListener(
        topics = "${app.topics.order-placed:order-placed}",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleOrderMessage(String orderNumber) {
        log.info("Received Kafka message: {}", orderNumber);
        // TODO: Update inventory, send email, etc.
    }
}
```

**Why Kafka?**
- **Decoupling**: Services don't need to know about each other
- **Asynchronous**: Order Service doesn't wait for Inventory Service
- **Scalability**: Can add more consumers for parallel processing
- **Reliability**: Messages stored durably, can be replayed
- **Event Sourcing**: Maintains history of all events

---

## 🛡️ Resilience & Fault Tolerance

### Circuit Breaker Pattern

**Problem:** If Inventory Service is down, Order Service crashes and cascade failures occur.

**Solution:** Circuit Breaker prevents cascading failures by:
1. **Monitoring** failures
2. **Opening** circuit after threshold
3. **Returning** fallback response
4. **Testing** service recovery periodically

**States:**
```
CLOSED (Normal)
    ├─ Requests pass through
    ├─ Failures tracked
    └─ If failures > threshold → OPEN

OPEN (Failing Fast)
    ├─ Immediate fallback
    ├─ Service recovery time
    └─ After timeout → HALF_OPEN

HALF_OPEN (Testing)
    ├─ Allow test requests
    ├─ If success → CLOSED
    └─ If failure → OPEN
```

**Implementation:**
```java
@RestController
@RequestMapping("/api/order")
public class OrderController {
    
    @PostMapping
    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
    @TimeLimiter(name = "inventory")
    @Retry(name = "inventory")
    public CompletableFuture<String> placeOrder(@RequestBody OrderRequest request) {
        return CompletableFuture.supplyAsync(() -> 
            orderService.placeOrder(request, username));
    }
    
    // Fallback executed when circuit is OPEN
    public CompletableFuture<String> fallbackMethod(
            OrderRequest request, 
            HttpServletRequest httpRequest,
            RuntimeException ex) {
        log.info("Circuit breaker activated, executing fallback");
        return CompletableFuture.supplyAsync(() -> 
            "Oops! Something went wrong, please order after some time!");
    }
}
```

**Configuration (`application.properties`):**
```properties
# Circuit Breaker
resilience4j.circuitbreaker.instances.inventory.registerHealthIndicator=true
resilience4j.circuitbreaker.instances.inventory.slidingWindowSize=5
resilience4j.circuitbreaker.instances.inventory.failureRateThreshold=50
resilience4j.circuitbreaker.instances.inventory.waitDurationInOpenState=10000
resilience4j.circuitbreaker.instances.inventory.permittedNumberOfCallsInHalfOpenState=3

# Retry
resilience4j.retry.instances.inventory.max-attempts=3
resilience4j.retry.instances.inventory.wait-duration=1s

# Timeout
resilience4j.timelimiter.instances.inventory.timeout-duration=3s
```

### Benefits:
- ✅ **Prevents cascading failures**
- ✅ **Graceful degradation**
- ✅ **Better user experience**
- ✅ **System stability**
- ✅ **Automatic recovery**

---

## 📨 Event-Driven Architecture

### Kafka Implementation

**Topic:** `order-placed`

**Flow:**
```
1. User places order via React frontend
2. Order Service saves order to PostgreSQL
3. Order Service publishes order number to Kafka
4. Kafka stores message in topic partition
5. Inventory Service (consumer) receives message
6. Inventory Service logs/processes the event
```

**Configuration:**

**Docker Compose (Kafka + Zookeeper):**
```yaml
zookeeper:
  image: zookeeper:3.9.2
  ports:
    - "2181:2181"

kafka:
  image: apache/kafka:3.7.0
  ports:
    - "9092:9092"
  environment:
    KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
    KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    CLUSTER_ID: 5L6g3nShT-eMCtK--X86sw
```

**Producer Config (Order Service):**
```properties
spring.kafka.bootstrap-servers=kafka:9092
spring.kafka.producer.properties.spring.json.add.type.headers=false
```

**Consumer Config (Inventory Service):**
```properties
spring.kafka.bootstrap-servers=kafka:9092
spring.kafka.consumer.group-id=inventory-service
spring.kafka.consumer.auto-offset-reset=earliest
app.topics.order-placed=order-placed
```

**Benefits:**
- ✅ **Asynchronous processing**
- ✅ **Service decoupling**
- ✅ **Event replay capability**
- ✅ **Horizontal scalability**
- ✅ **Fault tolerance**

---

## 💾 Database Design

### Polyglot Persistence Strategy

**Why Multiple Databases?**
- Different services have different data needs
- Optimized for specific use cases
- Database independence per service

### 1. **Product Service - MongoDB**

**Justification:**
- Flexible schema for varying product attributes
- Fast document reads for product catalog
- Easy to scale horizontally
- Native JSON support for image storage (base64)

**Schema:**
```javascript
{
  "_id": "673605e8d34b0827cbf2db1b",
  "name": "iPhone 15 Pro",
  "description": "Latest Apple flagship phone",
  "price": 1199.99,
  "image": "data:image/jpeg;base64,/9j/4AAQ...",
  "quantity": 50
}
```

### 2. **Order Service - PostgreSQL**

**Justification:**
- ACID transactions for order integrity
- Complex queries for order history
- Relational data (orders → line items)
- Strong consistency requirements

**Schema:**
```sql
CREATE TABLE t_orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(255) UNIQUE NOT NULL,
    username VARCHAR(255) NOT NULL,
    order_date TIMESTAMP NOT NULL,
    delivery_address TEXT,
    phone_number VARCHAR(20),
    email VARCHAR(255),
    payment_method VARCHAR(50)
);

CREATE TABLE t_order_line_items (
    id BIGSERIAL PRIMARY KEY,
    sku_code VARCHAR(255),
    price DECIMAL(19, 2),
    quantity INTEGER,
    order_id BIGINT REFERENCES t_orders(id)
);
```

### 3. **Inventory Service - PostgreSQL**

**Justification:**
- Transactional updates for stock levels
- Concurrent access control
- Join queries with orders (future)

**Schema:**
```sql
CREATE TABLE t_inventory (
    id BIGSERIAL PRIMARY KEY,
    sku_code VARCHAR(255) UNIQUE NOT NULL,
    quantity INTEGER NOT NULL
);
```

### 4. **Keycloak - MySQL**

**Justification:**
- Keycloak's native support for MySQL
- Reliable user session storage
- Built-in Keycloak schema

---

## 🚪 API Gateway

### Purpose
Single entry point for all client requests with:
- JWT validation
- RBAC enforcement
- Dynamic routing
- Load balancing
- CORS handling

### Routing Rules

```yaml
Product Service:
  Path: /api/product/**
  URI: lb://product-service
  Methods: GET, POST, PUT, DELETE
  Security:
    - GET: Authenticated users
    - POST/PUT/DELETE: Admin only

Order Service:
  Path: /api/order/**
  URI: lb://order-service
  Methods: GET, POST
  Security: Authenticated users

Inventory Service:
  Path: /api/inventory/**
  URI: lb://inventory-service
  Methods: GET
  Security: Authenticated users
```

### Load Balancing

The `lb://` scheme enables client-side load balancing:
```
Request: GET /api/product
         ↓
API Gateway
         ↓
Eureka: "product-service has 3 instances"
         ↓
Round-robin selection:
  - Instance 1: 192.168.1.100:8083
  - Instance 2: 192.168.1.101:8083
  - Instance 3: 192.168.1.102:8083
```

---

## 🔍 Service Discovery

### Netflix Eureka

**Architecture:**
```
Service Startup:
1. Product Service starts
2. Registers with Eureka: "product-service @ 192.168.1.100:8083"
3. Sends heartbeat every 30s

Service Discovery:
1. Order Service needs Product Service
2. Asks Eureka: "Where is product-service?"
3. Eureka responds with all instances
4. Order Service calls using WebClient with @LoadBalanced

Service Failure:
1. Product Service instance crashes
2. Heartbeat stops
3. Eureka marks instance as DOWN (after 90s)
4. Removes from registry
5. Other services get updated list
```

**Configuration (All Services):**
```properties
spring.application.name=order-service
eureka.client.serviceUrl.defaultZone=http://eureka:password@discovery-server:8761/eureka
eureka.instance.prefer-ip-address=false
```

**Benefits:**
- ✅ Dynamic service locations
- ✅ Automatic failover
- ✅ No hard-coded URLs
- ✅ Horizontal scaling support
- ✅ Health monitoring

---

## 📊 Observability & Monitoring

### Distributed Tracing (Micrometer + Zipkin)

**Purpose:** Track requests across multiple services

**Flow:**
```
1. User → API Gateway (Trace ID: abc123)
2. API Gateway → Order Service (Trace ID: abc123, Span ID: span1)
3. Order Service → Inventory Service (Trace ID: abc123, Span ID: span2)
4. Order Service → Product Service (Trace ID: abc123, Span ID: span3)
```

**Implementation:**
```java
@Service
public class OrderService {
    private final ObservationRegistry observationRegistry;
    
    public String placeOrder(OrderRequest request) {
        Observation observation = Observation.createNotStarted(
            "inventory-service-lookup", 
            this.observationRegistry);
        observation.lowCardinalityKeyValue("call", "inventory-service");
        
        return observation.observe(() -> {
            // This entire block is traced
            return callInventoryService();
        });
    }
}
```

**Configuration:**
```properties
# All services
management.zipkin.tracing.endpoint=http://localhost:9411/api/v2/spans
management.tracing.sampling.probability=1.0
logging.pattern.level=%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]
```

### Metrics Export (Prometheus)

```properties
management.endpoints.web.exposure.include=prometheus
management.endpoint.prometheus.enabled=true
```

**Metrics Endpoint:** `http://localhost:8081/actuator/prometheus`

---

## 🎨 Frontend Integration

### React SPA Architecture

**Technology Stack:**
- React 19.2.0 with TypeScript
- Material-UI for components
- Axios for HTTP requests
- React Router for navigation
- Tailwind CSS for styling

**Key Features:**
1. **JWT Token Management**
2. **Role-Based UI (Admin vs User)**
3. **Product Catalog Browsing**
4. **Shopping Cart**
5. **Checkout Flow**
6. **Order History**
7. **Admin Product Management**

**Authentication Flow:**
```typescript
// Login to Keycloak
const loginUser = async (username: string, password: string) => {
  const response = await axios.post('http://localhost:8080/realms/.../token', {
    grant_type: 'password',
    client_id: 'spring-cloud-client',
    username,
    password
  });
  
  const { access_token } = response.data;
  localStorage.setItem('token', access_token);
};

// API calls with token
const fetchProducts = async () => {
  const token = localStorage.getItem('token');
  const response = await axios.get('http://localhost:8181/api/product', {
    headers: {
      Authorization: `Bearer ${token}`
    }
  });
  return response.data;
};
```

**Project Structure:**
```
ecommerce/
├── src/
│   ├── components/         # Reusable UI components
│   ├── pages/             # Main pages (Home, Products, Orders)
│   ├── services/          # API client (Axios)
│   ├── context/           # React Context (Auth, Cart)
│   └── types/             # TypeScript interfaces
```

---

## 🚀 Deployment Strategy

### Docker Compose Orchestration

**Multi-Container Setup:**
- 13 containers in total
- 3 databases (PostgreSQL x2, MongoDB, MySQL)
- 1 message broker (Kafka + Zookeeper)
- 1 auth server (Keycloak)
- 5 microservices (Eureka, Gateway, Product, Order, Inventory)

**Startup Sequence:**
```
1. Databases (PostgreSQL, MongoDB, MySQL)
   ├─ postgres-order
   ├─ postgres-inventory
   ├─ mongo
   └─ keycloak-mysql

2. Infrastructure Services
   ├─ zookeeper
   ├─ kafka
   └─ keycloak (waits for MySQL health check)

3. Service Discovery
   └─ discovery-server (Eureka)

4. API Gateway
   └─ api-gateway (waits for discovery-server + keycloak)

5. Business Services
   ├─ product-service (depends on mongo, kafka, discovery, gateway)
   ├─ order-service (depends on postgres-order, kafka, discovery, gateway)
   └─ inventory-service (depends on postgres-inventory, kafka, discovery, gateway)
```

**Dockerfile (Multi-Stage Build):**
```dockerfile
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -pl order-service -am clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/order-service/target/order-service-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Commands:**
```bash
# Build and start all services
docker-compose up -d --build

# Scale a service
docker-compose up -d --scale product-service=3

# View logs
docker-compose logs -f order-service

# Stop all services
docker-compose down

# Clean volumes
docker-compose down -v
```

---

## ✨ Key Features

### 1. **User Features**
- ✅ Browse product catalog
- ✅ View product details with images
- ✅ Add products to cart
- ✅ Place orders with delivery details
- ✅ View personal order history
- ✅ Check inventory availability

### 2. **Admin Features**
- ✅ Create new products
- ✅ Update product information
- ✅ Delete products
- ✅ View all orders from all users
- ✅ Manage product quantities

### 3. **System Features**
- ✅ JWT-based authentication
- ✅ Role-based authorization (ADMIN/USER)
- ✅ Real-time inventory checks
- ✅ Automatic product quantity updates
- ✅ Asynchronous event processing
- ✅ Circuit breaker for fault tolerance
- ✅ Distributed tracing
- ✅ Service discovery
- ✅ Load balancing
- ✅ CORS support for SPA

---

## 📁 Project Structure

```
microservices-new/
├── pom.xml                      # Parent POM
├── docker-compose.yml           # Orchestration config
├── README.md
├── PROJECT_DOCUMENTATION.md     # This file
│
├── api-gateway/                 # API Gateway
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/.../config/
│       ├── SecurityConfig.java
│       └── KeycloakJwtAuthenticationConverter.java
│
├── discovery-server/            # Eureka Server
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/.../DiscoveryServerApplication.java
│
├── product-service/             # Product Management
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/.../
│       ├── controller/ProductController.java
│       ├── service/ProductService.java
│       ├── repository/ProductRepository.java
│       ├── model/Product.java
│       └── dto/
│
├── order-service/               # Order Management
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/.../
│       ├── controller/OrderController.java
│       ├── service/OrderService.java
│       ├── repository/OrderRepository.java
│       ├── model/
│       │   ├── Order.java
│       │   └── OrderLineItems.java
│       ├── security/JwtUsernameResolver.java
│       ├── event/OrderEventProducer.java
│       └── config/
│           ├── WebClientConfig.java
│           └── KafkaProducerConfig.java
│
├── inventory-service/           # Inventory Checking
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/.../
│       ├── controller/InventoryController.java
│       ├── service/InventoryService.java
│       ├── repository/InventoryRepository.java
│       ├── model/Inventory.java
│       └── event/OrderKafkaListener.java
│
├── ecommerce/                   # React Frontend
│   ├── package.json
│   ├── tsconfig.json
│   ├── tailwind.config.js
│   └── src/
│       ├── components/
│       ├── pages/
│       ├── services/api.ts
│       └── types/
│
├── docs/                        # Documentation
│   ├── MICROSERVICES_ARCHITECTURE.md
│   ├── SPRING_BOOT_CONCEPTS.md
│   └── KAFKA_CONCEPTS.md
│
├── realms/                      # Keycloak Config
│   └── realm-export.json
│
└── postgres-order/              # Persistent volumes
    postgres-inventory/
    mongo-data/
```

---

## 🏃 How to Run

### Prerequisites
- Docker & Docker Compose
- Java 21 (for local development)
- Maven (or use Maven Daemon `mvnd`)
- Node.js 16+ (for React frontend)

### Step 1: Start Backend Services
```bash
# Clone the repository
git clone https://github.com/HuzaifaIlyas02/springbootproject.git
cd microservices-new

# Build all services
mvnd clean package

# Start Docker Compose
docker-compose up -d --build

# Verify all containers are running
docker-compose ps
```

### Step 2: Verify Services
```bash
# Check Eureka Dashboard
open http://localhost:8761

# All 5 services should be registered:
# - API-GATEWAY
# - PRODUCT-SERVICE
# - ORDER-SERVICE
# - INVENTORY-SERVICE
```

### Step 3: Configure Keycloak
```bash
# Access Keycloak Admin Console
open http://localhost:8080

# Login: admin / admin

# Import realm or create manually:
# Realm: spring-boot-microservices-realm
# Client: spring-cloud-client
# Roles: ADMIN, USER

# Create test users:
# 1. admin (ADMIN role)
# 2. user (USER role)
```

### Step 4: Start React Frontend
```bash
cd ecommerce

# Install dependencies
npm install

# Start development server
npm start

# Open browser
open http://localhost:3000
```

### Step 5: Test the System

**Login:**
- Navigate to http://localhost:3000
- Click "Login"
- Enter credentials (admin/admin or user/user)

**Browse Products:**
- View product catalog
- Click product for details

**Place Order (User/Admin):**
- Add products to cart
- Click "Checkout"
- Fill delivery details
- Submit order
- Verify order number returned

**Check Order History:**
- User: See own orders
- Admin: See all orders

**Admin Operations:**
- Create new product
- Update product
- Delete product

### Step 6: Monitor Logs
```bash
# Order Service logs
docker-compose logs -f order-service

# Look for:
# "Creating order for user: admin"
# "Received Kafka message: 946e8f88..."

# Inventory Service logs
docker-compose logs -f inventory-service
```

---

## 🔮 Future Enhancements

### 1. **Payment Integration**
- Stripe/PayPal integration
- Payment status tracking
- Refund handling

### 2. **Notification Service**
- Email notifications (order confirmation)
- SMS alerts (order shipped)
- WebSocket for real-time updates

### 3. **Shipping Service**
- Shipping provider integration
- Tracking number generation
- Delivery status updates

### 4. **Advanced Kafka Usage**
- Multiple topics (order-shipped, payment-processed)
- Dead letter queues for failed messages
- Event sourcing pattern

### 5. **Enhanced Monitoring**
- Grafana dashboards
- Prometheus alerts
- ELK stack for log aggregation

### 6. **Testing**
- Integration tests with Testcontainers
- Contract testing (Pact)
- Load testing (JMeter/Gatling)

### 7. **Deployment**
- Kubernetes deployment
- Helm charts
- CI/CD pipeline (GitHub Actions)

### 8. **Performance**
- Redis caching layer
- Database connection pooling
- CDN for static assets

### 9. **Security**
- API rate limiting
- Input validation
- SQL injection prevention
- HTTPS/TLS encryption

### 10. **Frontend Enhancements**
- Progressive Web App (PWA)
- Mobile app (React Native)
- Product search & filtering
- Product reviews & ratings

---

## 📊 Architecture Decisions

### 1. **Why Microservices?**
- **Independent deployment**: Update one service without affecting others
- **Technology flexibility**: Different databases, languages per service
- **Scalability**: Scale only what needs scaling
- **Team autonomy**: Different teams own different services

### 2. **Why Spring Cloud Gateway over Zuul?**
- **Reactive**: Non-blocking, better performance
- **Modern**: Zuul is in maintenance mode
- **WebFlux**: Native reactive programming support
- **Active development**: Regular updates from Spring team

### 3. **Why Eureka over Consul/etcd?**
- **Spring ecosystem**: Native integration
- **AP system**: Availability over consistency (CAP theorem)
- **Easy setup**: Minimal configuration
- **Dashboard**: Built-in UI for monitoring

### 4. **Why Kafka over RabbitMQ?**
- **Event streaming**: Better for event sourcing
- **Durability**: Messages stored on disk
- **Replay**: Can replay old messages
- **Throughput**: Higher message throughput
- **Scalability**: Horizontal scaling with partitions

### 5. **Why Keycloak over Spring Security?**
- **Full IAM solution**: User management, roles, groups
- **OAuth2/OIDC**: Standard protocols
- **Admin UI**: Visual user management
- **Production-ready**: Used by major enterprises
- **Multi-tenancy**: Realm support

### 6. **Why PostgreSQL and MongoDB?**
- **Polyglot persistence**: Right tool for the job
- **PostgreSQL**: Strong consistency, ACID, relations
- **MongoDB**: Flexible schema, fast reads, JSON-native
- **Learning**: Real-world usage of both SQL and NoSQL

---

## 🎓 Key Learning Outcomes

### Spring Boot & Spring Cloud
- ✅ Microservices architecture patterns
- ✅ Service discovery with Eureka
- ✅ API Gateway implementation
- ✅ Reactive programming with WebFlux
- ✅ OAuth2 resource server configuration
- ✅ Distributed tracing with Micrometer
- ✅ Resilience patterns (Circuit Breaker, Retry, Timeout)

### Security
- ✅ OAuth2/OIDC authentication flow
- ✅ JWT token validation and parsing
- ✅ Role-based access control (RBAC)
- ✅ Keycloak integration
- ✅ Custom JWT claim extraction

### Data Management
- ✅ Polyglot persistence
- ✅ Spring Data JPA with PostgreSQL
- ✅ Spring Data MongoDB
- ✅ Database schema design
- ✅ Transaction management

### Event-Driven Architecture
- ✅ Apache Kafka integration
- ✅ Producer-consumer pattern
- ✅ Asynchronous processing
- ✅ Event sourcing concepts

### DevOps
- ✅ Docker containerization
- ✅ Multi-stage Docker builds
- ✅ Docker Compose orchestration
- ✅ Service dependencies management
- ✅ Health checks and readiness probes

### Frontend
- ✅ React with TypeScript
- ✅ Material-UI components
- ✅ JWT token management
- ✅ Axios HTTP client
- ✅ React Router navigation

---

## 🎯 Conclusion

This project demonstrates a **production-grade microservices architecture** with:

✅ **Scalability**: Each service can scale independently  
✅ **Resilience**: Circuit breakers prevent cascading failures  
✅ **Security**: OAuth2 + JWT + RBAC at gateway level  
✅ **Observability**: Distributed tracing and metrics  
✅ **Flexibility**: Multiple databases, technology choices  
✅ **Maintainability**: Clear separation of concerns  
✅ **Modern Stack**: Latest Spring Boot, React, Docker  

**Access Points:**
- **React Frontend**: http://localhost:3000
- **API Gateway**: http://localhost:8181
- **Keycloak**: http://localhost:8080
- **Eureka Dashboard**: http://localhost:8761

**Key Differentiators:**
- Real OAuth2/JWT implementation (not mocked)
- Actual Kafka event processing
- Custom JWT username extraction
- Resilience4j with fallback methods
- Polyglot persistence strategy
- Full CRUD operations with RBAC

This architecture is **production-ready** and can be extended with:
- Kubernetes deployment
- CI/CD pipelines
- Monitoring stack (Prometheus + Grafana)
- Additional microservices (Payment, Shipping, Notification)

---

*Built with ❤️ by Huzaifa Ilyas*  
*Project: Spring Boot Microservices E-Commerce Platform*  
*Last Updated: November 2025*
