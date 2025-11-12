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
