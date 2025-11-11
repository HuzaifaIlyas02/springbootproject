# Microservices E-Commerce Application - Complete Documentation

## Table of Contents
1. [Project Overview](#project-overview)
2. [Architecture](#architecture)
3. [Technology Stack](#technology-stack)
4. [Microservices Details](#microservices-details)
5. [Infrastructure Components](#infrastructure-components)
6. [Workflow & Communication](#workflow--communication)
7. [Security & Authentication](#security--authentication)
8. [Observability & Monitoring](#observability--monitoring)
9. [Deployment](#deployment)
10. [Project Structure](#project-structure)
11. [Why These Technologies?](#why-these-technologies)

---

## Project Overview

This is a comprehensive **Spring Boot Microservices** application that implements an e-commerce system with service discovery, API gateway, event-driven architecture, distributed tracing, and monitoring capabilities.

### Key Features:
- **Microservices Architecture**: Independently deployable services
- **Service Discovery**: Automatic service registration and discovery
- **API Gateway**: Single entry point with routing and security
- **Event-Driven**: Asynchronous communication using Apache Kafka
- **Distributed Tracing**: Request tracking across services using Zipkin
- **Circuit Breaker Pattern**: Resilience4j for fault tolerance
- **Security**: OAuth2/OpenID Connect with Keycloak
- **Monitoring**: Prometheus and Grafana for metrics visualization
- **Containerization**: Docker and Docker Compose for deployment

---

## Architecture

### High-Level Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client / Browser                         │
└────────────────────────────┬────────────────────────────────────┘
                             │ (HTTP Requests)
                             ↓
                    ┌────────────────┐
                    │  Keycloak      │ (Authentication)
                    │  Port: 8080    │
                    └────────────────┘
                             │
                             ↓
                    ┌────────────────┐
                    │  API Gateway   │ (Single Entry Point)
                    │  Port: 8181    │ (OAuth2 Resource Server)
                    └────────┬───────┘
                             │
        ┌────────────────────┼────────────────────┐
        ↓                    ↓                    ↓
┌───────────────┐   ┌───────────────┐   ┌───────────────┐
│Product Service│   │ Order Service │   │Inventory Svc  │
│  (MongoDB)    │   │ (PostgreSQL)  │   │ (PostgreSQL)  │
│  Port: Random │   │  Port: 8081   │   │  Port: Random │
└───────────────┘   └───────┬───────┘   └───────────────┘
                             │
                             │ (Kafka Events)
                             ↓
                    ┌────────────────┐
                    │ Apache Kafka   │
                    │  Port: 9092    │
                    └────────┬───────┘
                             │
                             ↓
                    ┌────────────────┐
                    │ Notification   │
                    │    Service     │
                    │  Port: Random  │
                    └────────────────┘
                             
┌─────────────────────────────────────────────────────────────────┐
│                    Discovery Server (Eureka)                     │
│                         Port: 8761                               │
│            (All services register here)                          │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                     Observability Stack                          │
│  Zipkin (9411) │ Prometheus (9090) │ Grafana (3000)            │
└─────────────────────────────────────────────────────────────────┘
```

---

## Technology Stack

### Core Framework
- **Spring Boot 3.3.5**: Modern Java framework for building microservices
- **Spring Cloud 2023.0.6**: Cloud-native patterns implementation
- **Java 21**: Latest LTS version (compiled with Java 17 compatibility)
- **Maven**: Build and dependency management

### Spring Cloud Components
- **Spring Cloud Netflix Eureka**: Service discovery
- **Spring Cloud Gateway**: API Gateway and routing
- **Spring Cloud LoadBalancer**: Client-side load balancing

### Databases
- **MongoDB 4.4.14**: NoSQL database for Product Service
- **PostgreSQL**: Relational database for Order and Inventory Services

### Messaging & Events
- **Apache Kafka 7.0.1**: Event streaming platform
- **Apache Zookeeper 7.0.1**: Kafka coordination service

### Security
- **Keycloak 18.0.0**: Identity and Access Management
- **Spring Security**: Authentication and authorization
- **OAuth2 Resource Server**: Token-based security

### Resilience & Fault Tolerance
- **Resilience4j**: Circuit breaker, retry, and timeout patterns

### Observability
- **Zipkin**: Distributed tracing
- **Micrometer**: Application metrics
- **Prometheus**: Metrics collection and storage
- **Grafana**: Metrics visualization and dashboards
- **Spring Boot Actuator**: Production-ready features

### Additional Tools
- **Lombok**: Reduces boilerplate code
- **WebFlux**: Reactive programming for asynchronous operations
- **Testcontainers**: Integration testing with containers

---

## Microservices Details

### 1. Discovery Server (Eureka Server)
**Port**: 8761  
**Purpose**: Service registry for all microservices

#### Key Features:
- All microservices register themselves with Eureka
- Provides service discovery for inter-service communication
- Health monitoring of registered services
- No client registration (standalone mode)

#### Configuration:
```properties
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
```

#### Dependencies:
- Spring Cloud Netflix Eureka Server
- Spring Security (basic authentication: eureka/password)
- Zipkin tracing
- Prometheus metrics

**Why Eureka?**
- Eliminates hardcoded service URLs
- Automatic service discovery and load balancing
- Built-in health checks and self-preservation mode

---

### 2. API Gateway
**Port**: 8181  
**Purpose**: Single entry point for all client requests

#### Key Features:
- **Request Routing**: Routes requests to appropriate microservices
- **Security**: OAuth2 resource server integration with Keycloak
- **Load Balancing**: Automatically load balances between service instances
- **Cross-Cutting Concerns**: Centralized authentication, logging, monitoring

#### Route Configuration:
1. **Product Service Route**: `/api/product` → `lb://product-service`
2. **Order Service Route**: `/api/order` → `lb://order-service`
3. **Eureka Dashboard Route**: `/eureka/web` → Eureka UI
4. **Eureka Static Resources**: `/eureka/**` → Eureka static files

#### Security:
- All routes except `/eureka/**` require authentication
- JWT token validation using Keycloak
- Issuer URI: `http://localhost:8181/realms/spring-boot-microservices-realm`

#### Dependencies:
- Spring Cloud Gateway
- Spring Security with OAuth2 Resource Server
- Eureka Client
- Zipkin tracing

**Why Spring Cloud Gateway?**
- Non-blocking, reactive approach (built on WebFlux)
- Better performance than Zuul
- Native integration with Spring Cloud ecosystem

---

### 3. Product Service
**Port**: Random (0) - assigned by Eureka  
**Database**: MongoDB  
**Purpose**: Manages product catalog

#### Endpoints:
- `POST /api/product`: Create a new product
- `GET /api/product`: Get all products

#### Data Model:
```java
Product {
    String id;
    String name;
    String description;
    BigDecimal price;
}
```

#### Key Features:
- NoSQL database for flexible product schema
- MongoDB document storage
- Registered with Eureka for discovery

#### Dependencies:
- Spring Data MongoDB
- Spring Web
- Eureka Client
- Testcontainers for MongoDB testing

**Why MongoDB?**
- Flexible schema for product attributes
- Horizontal scalability
- Better performance for read-heavy operations

---

### 4. Order Service
**Port**: 8081  
**Database**: PostgreSQL (port 5431)  
**Purpose**: Handles order creation and management

#### Endpoints:
- `POST /api/order`: Place a new order (with resilience patterns)

#### Data Model:
```java
Order {
    Long id;
    String orderNumber;
    List<OrderLineItems> orderLineItems;
}

OrderLineItems {
    Long id;
    String skuCode;
    BigDecimal price;
    Integer quantity;
}
```

#### Key Features:

**1. Inter-Service Communication:**
- Uses WebClient (reactive) to call Inventory Service
- Checks product availability before placing order
- Load-balanced calls using `lb://inventory-service`

**2. Resilience Patterns (Resilience4j):**
- **Circuit Breaker**: Stops calling failing services
  - Failure threshold: 50%
  - Wait duration: 5 seconds
- **Timeout**: 3 seconds timeout for inventory calls
- **Retry**: Up to 3 retry attempts with 5-second wait
- **Fallback Method**: Returns user-friendly message on failure

**3. Event-Driven Architecture:**
- Publishes `OrderPlacedEvent` to Kafka topic `notificationTopic`
- Asynchronous notification to Notification Service

#### Configuration Highlights:
```properties
resilience4j.circuitbreaker.instances.inventory.failureRateThreshold=50
resilience4j.timelimiter.instances.inventory.timeout-duration=3s
resilience4j.retry.instances.inventory.max-attempts=3
```

#### Dependencies:
- Spring Data JPA with PostgreSQL
- Spring WebFlux (WebClient)
- Spring Kafka
- Resilience4j
- Eureka Client

**Why These Patterns?**
- **Circuit Breaker**: Prevents cascading failures
- **Timeout**: Ensures responsive system
- **Retry**: Handles transient failures
- **Async Events**: Decouples order placement from notification

---

### 5. Inventory Service
**Port**: Random (0)  
**Database**: PostgreSQL (port 5432)  
**Purpose**: Manages product inventory and stock levels

#### Endpoints:
- `GET /api/inventory?skuCode=xxx&skuCode=yyy`: Check stock for multiple products

#### Data Model:
```java
Inventory {
    Long id;
    String skuCode;
    Integer quantity;
}
```

#### Key Features:
- Supports bulk inventory checks (multiple SKU codes)
- Returns stock availability for each product
- Intentional 10-second delay to demonstrate resilience patterns

#### Response Format:
```java
InventoryResponse {
    String skuCode;
    boolean isInStock;
}
```

#### Dependencies:
- Spring Data JPA with PostgreSQL
- Spring Web
- Eureka Client

**Why Separate Inventory Service?**
- Single responsibility principle
- Independent scaling based on traffic
- Can be updated without affecting other services

---

### 6. Notification Service
**Port**: Random (0)  
**Purpose**: Listens to order events and sends notifications

#### Key Features:
- Kafka consumer listening to `notificationTopic`
- Logs order notifications (can be extended to send emails/SMS)
- Event-driven architecture implementation

#### Event Handling:
```java
@KafkaListener(topics = "notificationTopic")
public void handleNotification(OrderPlacedEvent event) {
    log.info("Received Order: {}", event.getOrderNumber());
}
```

#### Dependencies:
- Spring Kafka
- Eureka Client
- No database (stateless service)

**Why Event-Driven?**
- Decouples order creation from notification
- Asynchronous processing
- Can add multiple consumers without modifying Order Service
- Scalable and resilient

---

## Infrastructure Components

### 1. Apache Kafka
**Ports**: 9092 (external), 29092 (internal)  
**Purpose**: Event streaming platform

#### Configuration:
- Broker ID: 1
- Replication Factor: 1 (single node)
- Topic: `notificationTopic`

#### Why Kafka?
- High throughput message broker
- Reliable message delivery
- Support for multiple consumers
- Event sourcing capabilities

---

### 2. Keycloak
**Port**: 8080  
**Purpose**: Identity and Access Management

#### Features:
- OAuth2 / OpenID Connect provider
- JWT token generation
- User authentication and authorization
- Realm: `spring-boot-microservices-realm`

#### Configuration:
- Admin credentials: admin/admin
- MySQL backend for Keycloak data
- Import realm configuration on startup

**Why Keycloak?**
- Industry-standard security
- Easy integration with Spring Security
- Centralized user management
- Support for various authentication protocols

---

### 3. Zipkin
**Port**: 9411  
**Purpose**: Distributed tracing

#### Features:
- Traces requests across microservices
- Identifies performance bottlenecks
- Visualizes service dependencies
- Debug complex microservice interactions

#### Integration:
All services send trace data to: `http://localhost:9411/api/v2/spans`

**Why Zipkin?**
- Essential for debugging distributed systems
- Identifies latency issues
- Correlates logs across services
- Performance monitoring

---

### 4. Prometheus
**Port**: 9090  
**Purpose**: Metrics collection and storage

#### Features:
- Scrapes metrics from all services via `/actuator/prometheus`
- Time-series database
- Powerful query language (PromQL)
- Alerting capabilities

**Why Prometheus?**
- De facto standard for Kubernetes/microservices monitoring
- Efficient time-series storage
- Pull-based architecture
- Rich ecosystem

---

### 5. Grafana
**Port**: 3000  
**Credentials**: admin/password  
**Purpose**: Metrics visualization

#### Features:
- Beautiful dashboards
- Connects to Prometheus as data source
- Real-time monitoring
- Alerting and notifications

**Why Grafana?**
- Best-in-class visualization
- Pre-built dashboards for Spring Boot
- Multi-datasource support
- Customizable alerts

---

## Workflow & Communication

### 1. Service Registration Flow

```
Service Startup → Register with Eureka → Health checks
                        ↓
                  Eureka Dashboard
                  (Port 8761)
```

All services register with credentials: `eureka:password`

---

### 2. Order Placement Flow

```
Client → API Gateway (Authentication)
           ↓
        Order Service (Create Order)
           ↓
        Inventory Service (Check Stock via WebClient)
           ↓ (If stock available)
        Save Order to PostgreSQL
           ↓
        Publish OrderPlacedEvent to Kafka
           ↓
        Notification Service (Consume Event)
           ↓
        Log Notification
```

#### With Resilience Patterns:

```
Order Service → Inventory Service (with timeout)
                    ↓ (If timeout/failure)
                Circuit Breaker Opens
                    ↓
                Retry (3 attempts)
                    ↓ (If still failing)
                Fallback Method
                    ↓
                Return Error Message
```

---

### 3. Distributed Tracing Flow

```
Request arrives → Trace ID generated
                      ↓
                API Gateway (span 1)
                      ↓
                Order Service (span 2)
                      ↓
                Inventory Service (span 3)
                      ↓
                All spans sent to Zipkin
                      ↓
                Visualize in Zipkin UI
```

Trace ID and Span ID are logged in all services:
```
%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]
```

---

## Security & Authentication

### OAuth2 Flow

```
1. User authenticates with Keycloak
2. Keycloak issues JWT token
3. Client includes token in request header:
   Authorization: Bearer <JWT_TOKEN>
4. API Gateway validates token
5. Request forwarded to backend services
```

### Security Configuration

**API Gateway**:
- All routes require authentication except `/eureka/**`
- JWT validation using Keycloak issuer URI
- OAuth2 Resource Server configuration

**Discovery Server**:
- Basic authentication (eureka:password)
- Prevents unauthorized access to service registry

---

## Observability & Monitoring

### 1. Distributed Tracing (Zipkin)
- **URL**: http://localhost:9411
- Tracks requests across services
- Shows service dependencies
- Identifies slow services

### 2. Metrics (Prometheus)
- **URL**: http://localhost:9090
- Collects metrics from `/actuator/prometheus` endpoints
- Stores time-series data
- Supports alerting

### 3. Dashboards (Grafana)
- **URL**: http://localhost:3000
- Visualizes Prometheus data
- Pre-configured dashboards for:
  - JVM metrics
  - HTTP request rates
  - Database connection pools
  - Circuit breaker status

### 4. Health Checks (Actuator)
All services expose:
- `/actuator/health`: Service health status
- `/actuator/prometheus`: Metrics for Prometheus
- `/actuator/info`: Application information

---

## Deployment

### Docker Compose Architecture

The application uses **Docker Compose** for orchestration with the following services:

#### Database Containers:
1. **postgres-order** (port 5431): Order Service database
2. **postgres-inventory** (port 5432): Inventory Service database
3. **mongo** (port 27017): Product Service database
4. **keycloak-mysql** (port 3306): Keycloak database

#### Infrastructure Containers:
5. **zookeeper** (port 2181): Kafka coordination
6. **broker** (Kafka, port 9092): Event streaming
7. **zipkin** (port 9411): Distributed tracing
8. **prometheus** (port 9090): Metrics collection
9. **grafana** (port 3000): Metrics visualization

#### Security Container:
10. **keycloak** (port 8080): Authentication server

#### Application Containers:
11. **discovery-server** (port 8761): Service registry
12. **api-gateway** (port 8181): API Gateway
13. **product-service**: Product management
14. **order-service**: Order management
15. **inventory-service**: Inventory management
16. **notification-service**: Event notifications

### Deployment Steps

#### Local Development:
```bash
# Build all services
mvn clean package -DskipTests

# Start infrastructure
docker-compose up -d

# Services will auto-register with Eureka
```

#### Docker Deployment:
```bash
# Build Docker images
docker build -t huzaifa02/product-service:latest ./product-service
docker build -t huzaifa02/order-service:latest ./order-service
docker build -t huzaifa02/inventory-service:latest ./inventory-service
docker build -t huzaifa02/notification-service:latest ./notification-service
docker build -t huzaifa02/discovery-server:latest ./discovery-server
docker build -t huzaifa02/api-gateway:latest ./api-gateway

# Start all services
docker-compose up -d
```

### Environment Profiles

**Local Profile** (`application.properties`):
- Services connect to localhost databases
- Eureka at localhost:8761
- Keycloak at localhost:8080

**Docker Profile** (`application-docker.properties`):
- Services use Docker container names
- Eureka at discovery-server:8761
- Keycloak at keycloak:8080
- Activated with: `SPRING_PROFILES_ACTIVE=docker`

---

## Project Structure

```
microservices-new/
│
├── pom.xml                          # Parent POM with common dependencies
│
├── docker-compose.yml               # Orchestration configuration
│
├── api-gateway/                     # API Gateway Service
│   ├── pom.xml
│   ├── Dockerfile
│   ├── src/
│   │   └── main/
│   │       ├── java/com/huzaifaproject/apigateway/
│   │       │   ├── ApiGatewayApplication.java
│   │       │   └── config/
│   │       │       └── SecurityConfig.java
│   │       └── resources/
│   │           ├── application.properties
│   │           └── application-docker.properties
│   └── target/                      # Build artifacts
│
├── discovery-server/                # Eureka Server
│   ├── pom.xml
│   ├── src/
│   │   └── main/
│   │       ├── java/com/huzaifaproject/discoveryserver/
│   │       │   ├── DiscoveryServerApplication.java
│   │       │   └── config/
│   │       │       └── SecurityConfig.java
│   │       └── resources/
│   │           ├── application.properties
│   │           └── application-docker.properties
│   └── target/
│
├── product-service/                 # Product Management Service
│   ├── pom.xml
│   ├── src/
│   │   └── main/
│   │       ├── java/com/huzaifaproject/productservice/
│   │       │   ├── ProductServiceApplication.java
│   │       │   ├── controller/
│   │       │   │   └── ProductController.java
│   │       │   ├── dto/
│   │       │   │   ├── ProductRequest.java
│   │       │   │   └── ProductResponse.java
│   │       │   ├── model/
│   │       │   │   └── Product.java
│   │       │   ├── repository/
│   │       │   │   └── ProductRepository.java
│   │       │   └── service/
│   │       │       └── ProductService.java
│   │       └── resources/
│   │           ├── application.properties
│   │           └── application-docker.properties
│   └── target/
│
├── order-service/                   # Order Management Service
│   ├── pom.xml
│   ├── src/
│   │   └── main/
│   │       ├── java/com/huzaifaproject/orderservice/
│   │       │   ├── OrderServiceApplication.java
│   │       │   ├── config/
│   │       │   │   └── WebClientConfig.java
│   │       │   ├── controller/
│   │       │   │   └── OrderController.java
│   │       │   ├── dto/
│   │       │   │   ├── OrderRequest.java
│   │       │   │   ├── OrderLineItemsDto.java
│   │       │   │   └── InventoryResponse.java
│   │       │   ├── event/
│   │       │   │   └── OrderPlacedEvent.java
│   │       │   ├── model/
│   │       │   │   ├── Order.java
│   │       │   │   └── OrderLineItems.java
│   │       │   ├── repository/
│   │       │   │   └── OrderRepository.java
│   │       │   └── service/
│   │       │       └── OrderService.java
│   │       └── resources/
│   │           ├── application.properties
│   │           ├── application-docker.properties
│   │           └── data.sql
│   └── target/
│
├── inventory-service/               # Inventory Management Service
│   ├── pom.xml
│   ├── src/
│   │   └── main/
│   │       ├── java/com/huzaifaproject/inventoryservice/
│   │       │   ├── InventoryServiceApplication.java
│   │       │   ├── controller/
│   │       │   │   └── InventoryController.java
│   │       │   ├── dto/
│   │       │   │   └── InventoryResponse.java
│   │       │   ├── model/
│   │       │   │   └── Inventory.java
│   │       │   ├── repository/
│   │       │   │   └── InventoryRepository.java
│   │       │   └── service/
│   │       │       └── InventoryService.java
│   │       └── resources/
│   │           ├── application.properties
│   │           ├── application-docker.properties
│   │           └── data.sql
│   └── target/
│
└── notification-service/            # Notification Service (Kafka Consumer)
    ├── pom.xml
    ├── src/
    │   └── main/
    │       ├── java/com/huzaifaproject/
    │       │   ├── NotificationServiceApplication.java
    │       │   └── OrderPlacedEvent.java
    │       └── resources/
    │           ├── application.properties
    │           └── application-docker.properties
    └── target/
```

---

## Why These Technologies?

### 1. **Spring Boot**
- **Rapid Development**: Convention over configuration
- **Production-Ready**: Built-in actuator, metrics, health checks
- **Ecosystem**: Massive library support
- **Community**: Large community and excellent documentation

### 2. **Spring Cloud**
- **Microservices Patterns**: Out-of-the-box implementations
- **Netflix OSS**: Battle-tested components (Eureka, Ribbon)
- **Integration**: Seamless Spring Boot integration

### 3. **Eureka (Service Discovery)**
- **Dynamic Discovery**: No hardcoded URLs
- **Self-Healing**: Automatic deregistration of failed services
- **Load Balancing**: Client-side load balancing
- **Alternative**: Consul, but Eureka is easier for Spring apps

### 4. **Spring Cloud Gateway**
- **Reactive**: Non-blocking, better performance
- **Routing**: Flexible routing capabilities
- **Filters**: Request/response transformation
- **Alternative**: Netflix Zuul (older, blocking)

### 5. **PostgreSQL**
- **ACID Compliance**: Strong consistency for orders/inventory
- **Reliability**: Proven in production environments
- **Features**: JSON support, full-text search
- **Alternative**: MySQL (used but PostgreSQL chosen for features)

### 6. **MongoDB**
- **Schema Flexibility**: Products have varying attributes
- **Scalability**: Horizontal scaling for product catalog
- **Performance**: Fast reads for product listings
- **Alternative**: PostgreSQL with JSONB (but MongoDB is specialized)

### 7. **Apache Kafka**
- **High Throughput**: Millions of messages per second
- **Durability**: Persistent message storage
- **Scalability**: Distributed architecture
- **Event Sourcing**: Perfect for event-driven architecture
- **Alternative**: RabbitMQ (but Kafka better for high volume)

### 8. **Keycloak**
- **Standards-Based**: OAuth2, OpenID Connect
- **Feature-Rich**: User management, SSO, MFA
- **Free & Open Source**: No licensing costs
- **Flexible**: Custom themes, extensions
- **Alternative**: Auth0, Okta (but Keycloak is self-hosted)

### 9. **Resilience4j**
- **Lightweight**: No external dependencies
- **Modern**: Functional programming approach
- **Spring Integration**: Easy Spring Boot integration
- **Patterns**: Circuit breaker, retry, rate limiter, bulkhead
- **Alternative**: Netflix Hystrix (deprecated)

### 10. **Zipkin**
- **Distributed Tracing**: Essential for microservices
- **Visualization**: Clear UI for trace analysis
- **Performance**: Lightweight and fast
- **Integration**: Easy Spring Boot integration
- **Alternative**: Jaeger (but Zipkin is simpler)

### 11. **Prometheus & Grafana**
- **Industry Standard**: De facto monitoring stack
- **Prometheus**: Time-series database, pull-based
- **Grafana**: Best visualization tool
- **Ecosystem**: Large community, many integrations
- **Alternative**: ELK Stack, DataDog (but Prom/Graf is free)

### 12. **Docker & Docker Compose**
- **Consistency**: Same environment everywhere
- **Isolation**: Each service in own container
- **Orchestration**: Easy multi-container management
- **Development**: Quick setup for developers
- **Alternative**: Kubernetes (overkill for development)

### 13. **WebClient (WebFlux)**
- **Reactive**: Non-blocking HTTP calls
- **Performance**: Better than RestTemplate
- **Modern**: Recommended by Spring team
- **Backpressure**: Handles overwhelming responses
- **Alternative**: RestTemplate (blocking, legacy)

### 14. **Lombok**
- **Productivity**: Reduces boilerplate code
- **Maintenance**: Less code to maintain
- **Readability**: Cleaner classes
- **Features**: @Data, @Builder, @Slf4j annotations

### 15. **Maven**
- **Dependency Management**: Central repository
- **Multi-Module**: Perfect for microservices
- **Convention**: Standard directory structure
- **Plugins**: Rich plugin ecosystem
- **Alternative**: Gradle (but Maven is more standardized)

### 16. **Testcontainers**
- **Integration Testing**: Real database testing
- **Isolation**: Each test gets fresh container
- **Docker-Based**: Uses actual Docker images
- **Alternative**: H2, embedded databases (but not production-like)

---

## API Endpoints

### Product Service (via API Gateway)

**Base URL**: `http://localhost:8181/api/product`

#### Create Product
```http
POST /api/product
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "name": "iPhone 13",
  "description": "Apple iPhone 13 smartphone",
  "price": 999.99
}

Response: 201 Created
```

#### Get All Products
```http
GET /api/product
Authorization: Bearer <JWT_TOKEN>

Response: 200 OK
[
  {
    "id": "64f7a8b9c7e2f1d4a8b9c7e2",
    "name": "iPhone 13",
    "description": "Apple iPhone 13 smartphone",
    "price": 999.99
  }
]
```

---

### Order Service (via API Gateway)

**Base URL**: `http://localhost:8181/api/order`

#### Place Order
```http
POST /api/order
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "orderLineItemsDtoList": [
    {
      "skuCode": "iphone_13",
      "price": 999.99,
      "quantity": 1
    },
    {
      "skuCode": "iphone_14",
      "price": 1099.99,
      "quantity": 2
    }
  ]
}

Response: 201 Created
"Order Placed Successfully"

OR (if out of stock)

Response: 400 Bad Request
"Product is out of stock"

OR (if inventory service is down)

Response: 200 OK
"Oops! Something went wrong, please order after some time"
```

---

### Inventory Service (via API Gateway)

**Internal Service** - Called by Order Service, not directly exposed.

#### Check Inventory
```http
GET http://inventory-service/api/inventory?skuCode=iphone_13&skuCode=iphone_14

Response: 200 OK
[
  {
    "skuCode": "iphone_13",
    "inStock": true
  },
  {
    "skuCode": "iphone_14",
    "inStock": false
  }
]
```

---

## Configuration Management

### Port Configuration

| Service | Local Port | Docker Port | Database Port |
|---------|-----------|-------------|---------------|
| Discovery Server | 8761 | 8761 | - |
| API Gateway | 8181 | 8080 (internal) | - |
| Product Service | Random | Random | 27017 (MongoDB) |
| Order Service | 8081 | Random | 5431 (PostgreSQL) |
| Inventory Service | Random | Random | 5432 (PostgreSQL) |
| Notification Service | Random | Random | - |
| Keycloak | 8080 | 8080 | 3306 (MySQL) |
| Kafka | 9092 | 9092/29092 | - |
| Zipkin | 9411 | 9411 | - |
| Prometheus | 9090 | 9090 | - |
| Grafana | 3000 | 3000 | - |

### Database Credentials

#### PostgreSQL (Order Service)
```properties
URL: jdbc:postgresql://localhost:5431/order-service
Username: huzaifaproject
Password: huzaifa1122
```

#### PostgreSQL (Inventory Service)
```properties
URL: jdbc:postgresql://localhost:5432/inventory-service
Username: huzaifaproject
Password: huzaifa1122
```

#### MongoDB (Product Service)
```properties
Host: localhost
Port: 27017
Database: product-service
(No authentication in development)
```

#### MySQL (Keycloak)
```properties
Database: keycloak
Username: keycloak
Password: password
```

---

## Testing Strategy

### Unit Tests
- Each service has its own test suite
- Uses JUnit 5 and Mockito
- Located in `src/test/java`

### Integration Tests
- Testcontainers for database testing
- Real MongoDB/PostgreSQL containers
- Tests entire service flow

### Load Testing
- Inventory Service has intentional 10-second delay
- Tests circuit breaker, timeout, and retry mechanisms
- Validates fallback methods

---

## Development Workflow

### Initial Setup
```bash
# Clone repository
git clone <repository-url>
cd microservices-new

# Start infrastructure
docker-compose up -d postgres-order postgres-inventory mongo keycloak keycloak-mysql zookeeper broker zipkin prometheus grafana

# Build all services
mvn clean install -DskipTests

# Run services individually (in separate terminals)
cd discovery-server && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
cd product-service && mvn spring-boot:run
cd order-service && mvn spring-boot:run
cd inventory-service && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
```

### Making Changes
1. Make code changes in specific service
2. Rebuild: `mvn clean package`
3. Restart the service
4. Verify in Eureka dashboard (http://localhost:8761)
5. Check logs in console
6. Verify traces in Zipkin (http://localhost:9411)
7. Check metrics in Prometheus/Grafana

---

## Monitoring & Troubleshooting

### Eureka Dashboard
- **URL**: http://localhost:8761
- **Credentials**: eureka/password
- **Purpose**: View registered services and their health

### Zipkin Dashboard
- **URL**: http://localhost:9411
- **Purpose**: Trace requests across services
- **Usage**: Search by trace ID or service name

### Prometheus
- **URL**: http://localhost:9090
- **Purpose**: Query metrics using PromQL
- **Example Query**: `http_server_requests_seconds_count{service="order-service"}`

### Grafana
- **URL**: http://localhost:3000
- **Credentials**: admin/password
- **Purpose**: Visualize metrics from Prometheus

### Common Issues

#### Service Not Registering
```bash
# Check Eureka logs
docker logs discovery-server

# Verify application.properties
eureka.client.serviceUrl.defaultZone=http://eureka:password@localhost:8761/eureka
```

#### Database Connection Issues
```bash
# Check database container
docker ps | grep postgres

# Check logs
docker logs postgres-order
docker logs postgres-inventory

# Verify credentials in application.properties
```

#### Kafka Connection Issues
```bash
# Check Kafka broker
docker logs broker

# Verify Zookeeper
docker logs zookeeper

# Check Kafka topics
docker exec -it broker kafka-topics --list --bootstrap-server localhost:9092
```

#### Circuit Breaker Not Working
```bash
# Check Actuator endpoint
curl http://localhost:8081/actuator/health

# Verify Resilience4j configuration
resilience4j.circuitbreaker.instances.inventory.failureRateThreshold=50
```

---

## Future Enhancements

### Recommended Improvements
1. **API Documentation**: Add Swagger/OpenAPI
2. **Centralized Configuration**: Spring Cloud Config Server
3. **Message Broker UI**: Kafka UI or Kafdrop
4. **Caching**: Redis for frequently accessed data
5. **Rate Limiting**: API Gateway rate limiting
6. **Database Migration**: Flyway or Liquibase
7. **Service Mesh**: Istio or Linkerd for production
8. **Container Orchestration**: Kubernetes for production
9. **CI/CD Pipeline**: Jenkins, GitHub Actions, or GitLab CI
10. **Log Aggregation**: ELK Stack or Loki

---

## Conclusion

This microservices architecture demonstrates industry best practices for building scalable, resilient, and observable distributed systems. Each technology choice is deliberate, solving specific challenges in microservices architecture:

- **Service Discovery** eliminates configuration complexity
- **API Gateway** provides single entry point and security
- **Event-Driven Architecture** enables loose coupling
- **Resilience Patterns** ensure fault tolerance
- **Distributed Tracing** enables debugging
- **Monitoring** provides operational insights
- **Containerization** ensures consistency

The architecture is production-ready with proper security, monitoring, and fault tolerance mechanisms in place.

---

## Quick Reference

### URLs
- **Eureka Dashboard**: http://localhost:8761 (eureka/password)
- **API Gateway**: http://localhost:8181
- **Keycloak Admin**: http://localhost:8080 (admin/admin)
- **Zipkin**: http://localhost:9411
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/password)

### Commands
```bash
# Start all services
docker-compose up -d

# Stop all services
docker-compose down

# View logs
docker-compose logs -f [service-name]

# Rebuild service
mvn clean package -DskipTests

# Run tests
mvn test

# Check service health
curl http://localhost:8081/actuator/health
```

---

**Project Version**: 1.0-SNAPSHOT  
**Spring Boot Version**: 3.3.5  
**Spring Cloud Version**: 2023.0.6  
**Java Version**: 21 (compiled with Java 17)  
**Build Tool**: Maven 3.x  

**Author**: Huzaifa Project (com.huzaifaproject)  
**License**: Check project repository

---
