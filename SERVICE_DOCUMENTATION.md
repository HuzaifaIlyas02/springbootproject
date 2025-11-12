# 📚 Complete Service Documentation - Line by Line

## 📋 Table of Contents
1. [Discovery Server](#1-discovery-server)
2. [API Gateway](#2-api-gateway)
3. [Product Service](#3-product-service)
4. [Order Service](#4-order-service)
5. [Inventory Service](#5-inventory-service)

---

# 1. Discovery Server (Eureka)

## 📁 DiscoveryServerApplication.java
**Location**: `discovery-server/src/main/java/com/huzaifaproject/discoveryserver/DiscoveryServerApplication.java`

```java
package com.huzaifaproject.discoveryserver;
```
**Line Purpose**: Declares the package structure following Java naming conventions
- Uses reverse domain notation: `com.huzaifaproject.discoveryserver`
- Groups all discovery server related classes together
- Prevents naming conflicts with other packages

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
```
**Line Purpose**: Imports necessary Spring Boot and Eureka classes
- `SpringApplication`: Launches Spring Boot application
- `SpringBootApplication`: Main annotation for Spring Boot apps
- `EnableEurekaServer`: Activates Eureka Server functionality

```java
@SpringBootApplication
```
**Line Purpose**: Composite annotation that enables:
- `@Configuration`: Marks this as a configuration class
- `@EnableAutoConfiguration`: Automatically configures Spring based on dependencies
- `@ComponentScan`: Scans for Spring components in this package and sub-packages
**Why Useful**: One annotation instead of three, simplifies configuration

```java
@EnableEurekaServer
```
**Line Purpose**: Activates Netflix Eureka Server capabilities
**What It Does**:
- Starts embedded Eureka Server on port 8761 (default)
- Creates REST endpoints for service registration
- Enables service registry UI dashboard
- Starts heartbeat checking mechanism
**Why Useful**: Turns regular Spring Boot app into service registry

```java
public class DiscoveryServerApplication {
```
**Line Purpose**: Main application class declaration
- Entry point for Discovery Server microservice
- Contains the main method to start application

```java
    public static void main(String[] args) {
```
**Line Purpose**: Java application entry point
- `public`: Accessible from anywhere
- `static`: Can be called without creating instance
- `void`: Returns nothing
- `String[] args`: Command-line arguments

```java
        SpringApplication.run(DiscoveryServerApplication.class, args);
```
**Line Purpose**: Launches the Spring Boot application
**What It Does**:
1. Creates ApplicationContext (Spring container)
2. Scans for components and configurations
3. Auto-configures beans based on dependencies
4. Starts embedded Tomcat server
5. Initializes Eureka Server
6. Opens HTTP endpoint on port 8761
**Returns**: ApplicationContext object
**Why Useful**: Single line starts entire application with all configurations

---

## 📁 SecurityConfig.java
**Location**: `discovery-server/src/main/java/com/huzaifaproject/discoveryserver/SecurityConfig.java`

```java
package com.huzaifaproject.discoveryserver;
```
**Line Purpose**: Package declaration for security configuration classes

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
```
**Line Purpose**: Imports Spring Security classes
- `@Bean`: Declares Spring-managed bean
- `@Configuration`: Marks this as configuration class
- `HttpSecurity`: Configures web security
- `SecurityFilterChain`: Security filter chain for requests

```java
@Configuration
```
**Line Purpose**: Marks class as Spring configuration
**What It Does**: Spring scans this class for @Bean methods and creates beans
**Why Useful**: Centralizes security configuration

```java
public class SecurityConfig {
```
**Line Purpose**: Security configuration class for Eureka Server

```java
    @Bean
```
**Line Purpose**: Declares method returns Spring-managed bean
**What It Does**: Spring calls this method and manages returned object's lifecycle
**Why Useful**: Bean is singleton by default, reusable across application

```java
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
```
**Line Purpose**: Defines security filter chain configuration
- **Parameters**: `HttpSecurity` - builder for web security
- **Returns**: `SecurityFilterChain` - configured security chain
- **Throws**: `Exception` - if configuration fails
**Why Useful**: Customizes Spring Security behavior

```java
        httpSecurity.csrf().ignoringRequestMatchers("/eureka/**");
```
**Line Purpose**: Disables CSRF protection for Eureka endpoints
**What It Does**:
- CSRF (Cross-Site Request Forgery) protection disabled for `/eureka/**`
- Allows services to register without CSRF tokens
- Eureka uses heartbeat mechanism, not session-based auth
**Why Necessary**: Service registration would fail with CSRF enabled
**Security Note**: Safe because services authenticate via other means

```java
        return httpSecurity.build();
```
**Line Purpose**: Builds and returns configured SecurityFilterChain
**What It Does**: Compiles all security configurations into filter chain
**Returns**: `SecurityFilterChain` object applied to all HTTP requests

---

# 2. API Gateway

## 📁 ApiGatewayApplication.java
**Location**: `api-gateway/src/main/java/com/huzaifaproject/apigateway/ApiGatewayApplication.java`

```java
package com.huzaifaproject.apigateway;
```
**Line Purpose**: Package declaration for API Gateway classes

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
```
**Line Purpose**: Imports Spring Boot and Spring Cloud classes
- `EnableDiscoveryClient`: Enables service discovery client

```java
@SpringBootApplication
```
**Line Purpose**: Main Spring Boot annotation
**What It Does**: Enables auto-configuration and component scanning

```java
@EnableDiscoveryClient
```
**Line Purpose**: Registers this service as Eureka client
**What It Does**:
- Registers with Discovery Server on startup
- Sends heartbeat every 30 seconds
- Can discover other services
- Gets service locations from Eureka
**Why Useful**: API Gateway can route to services dynamically

```java
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
```
**Line Purpose**: Main class and entry point
**What It Does**: Starts API Gateway with service discovery enabled

---

## 📁 SecurityConfig.java
**Location**: `api-gateway/src/main/java/com/huzaifaproject/apigateway/config/SecurityConfig.java`

```java
package com.huzaifaproject.apigateway.config;
```
**Line Purpose**: Package for configuration classes

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
```
**Line Purpose**: Imports for reactive security and CORS
- `EnableWebFluxSecurity`: For reactive (non-blocking) security
- `ServerHttpSecurity`: Reactive version of HttpSecurity
- `SecurityWebFilterChain`: Reactive security filter chain
- `CorsConfiguration`: CORS settings configuration

```java
import java.util.Arrays;
import java.util.List;
```
**Line Purpose**: Java utility imports for collections

```java
@Configuration
```
**Line Purpose**: Marks as Spring configuration class

```java
@EnableWebFluxSecurity
```
**Line Purpose**: Enables WebFlux (reactive) security
**Why Reactive**: API Gateway handles many concurrent requests, reactive programming provides better performance
**What It Does**: Configures security for reactive web applications

```java
public class SecurityConfig {
```
**Line Purpose**: Security configuration class for API Gateway

```java
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity serverHttpSecurity) {
```
**Line Purpose**: Defines reactive security filter chain
**Parameters**: `ServerHttpSecurity` - reactive security builder
**Returns**: `SecurityWebFilterChain` - security chain for reactive apps

```java
        serverHttpSecurity
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
```
**Line Purpose**: Configures CORS (Cross-Origin Resource Sharing)
**What It Does**: Allows React frontend (localhost:3000) to call API Gateway
**Why Necessary**: Browser blocks cross-origin requests by default
**How It Works**: Calls `corsConfigurationSource()` method for CORS rules

```java
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
```
**Line Purpose**: Disables CSRF protection
**Why Disabled**: 
- REST APIs are stateless (no cookies)
- Using JWT tokens for auth (not session-based)
- CSRF only matters for session-based auth
**Security Note**: Safe for token-based auth

```java
                .authorizeExchange(exchange ->
```
**Line Purpose**: Starts authorization rules configuration
**What It Does**: Defines which URLs require what permissions
**`exchange`**: Represents HTTP request/response exchange

```java
                        exchange.pathMatchers("/eureka/**").permitAll()
```
**Line Purpose**: Allows unrestricted access to Eureka endpoints
**Why**: Eureka dashboard and registration need to be accessible
**Pattern**: `/eureka/**` matches all Eureka paths

```java
                                .pathMatchers(HttpMethod.POST, "/api/product").hasRole("ADMIN")
```
**Line Purpose**: **RBAC RULE** - Only ADMIN can create products
**What It Does**:
- Checks if request is POST method
- Checks if path is `/api/product`
- Requires user to have ROLE_ADMIN
- Returns 403 Forbidden if user lacks ADMIN role
**Why Useful**: Prevents regular users from adding products
**How It Works**: Checks JWT token's realm_access.roles claim

```java
                                .pathMatchers(HttpMethod.GET, "/api/product").authenticated()
```
**Line Purpose**: Any authenticated user can view products
**What It Does**: Requires valid JWT token, no specific role needed
**Why**: Product catalog should be visible to all logged-in users

```java
                                .pathMatchers("/api/order/**").authenticated()
```
**Line Purpose**: Any authenticated user can access order endpoints
**What It Does**: All order operations require valid JWT
**Pattern**: `/**` matches all sub-paths (POST, GET, etc.)
**Why**: Anyone logged in can place orders

```java
                                .pathMatchers("/api/inventory/**").authenticated()
```
**Line Purpose**: Any authenticated user can check inventory
**What It Does**: Requires valid JWT for inventory checks
**Why**: Logged-in users can check product availability

```java
                                .anyExchange().authenticated())
```
**Line Purpose**: Default rule - all other requests need authentication
**What It Does**: Catches any URL not matched above
**Why**: Secure by default, explicit about what's public

```java
                .oauth2ResourceServer(spec -> spec.jwt(jwtSpec -> jwtSpec.jwtAuthenticationConverter(new KeycloakJwtAuthenticationConverter())));
```
**Line Purpose**: Configures OAuth2 Resource Server with JWT
**What It Does**:
1. Marks this as OAuth2 Resource Server
2. Validates JWT tokens from Keycloak
3. Uses custom converter to extract roles
4. Converts JWT to Spring Security Authentication
**How It Works**:
- Validates JWT signature using Keycloak's public key
- Extracts claims (username, roles, expiry)
- Converts to Authentication object
**Why Custom Converter**: Keycloak stores roles in `realm_access.roles`, Spring expects different format

```java
        return serverHttpSecurity.build();
```
**Line Purpose**: Builds and returns security configuration
**Returns**: Configured `SecurityWebFilterChain`

---

```java
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
```
**Line Purpose**: Creates CORS configuration bean
**Returns**: `CorsConfigurationSource` - CORS rules for reactive apps
**Why Bean**: Reusable across application

```java
        CorsConfiguration configuration = new CorsConfiguration();
```
**Line Purpose**: Creates new CORS configuration object
**What It Does**: Holds all CORS settings

```java
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
```
**Line Purpose**: Allows requests from React frontend
**What It Does**: 
- Browser checks Origin header
- If origin is `http://localhost:3000`, request allowed
- Other origins are blocked
**Why**: Security - only our frontend can call APIs
**Production Note**: Change to actual frontend domain

```java
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
```
**Line Purpose**: Allows specified HTTP methods
**What It Does**: Permits these methods in cross-origin requests
**Why OPTIONS**: Browser sends preflight OPTIONS request before actual request

```java
        configuration.setAllowedHeaders(Arrays.asList("*"));
```
**Line Purpose**: Allows all headers in requests
**What It Does**: Permits any header (Authorization, Content-Type, etc.)
**`"*"`**: Wildcard means all headers allowed
**Why**: JWT tokens sent in Authorization header

```java
        configuration.setAllowCredentials(true);
```
**Line Purpose**: Allows cookies and credentials
**What It Does**: 
- Allows cookies in cross-origin requests
- Allows Authorization header
- Required for JWT token transmission
**Why True**: Frontend needs to send JWT tokens

```java
        configuration.setMaxAge(3600L);
```
**Line Purpose**: Cache preflight response for 1 hour
**What It Does**: Browser caches CORS preflight for 3600 seconds
**Why Useful**: Reduces preflight requests, improves performance
**How It Works**: Browser remembers CORS rules for 1 hour

```java
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
```
**Line Purpose**: Creates URL-based CORS source
**What It Does**: Maps CORS configurations to URL patterns

```java
        source.registerCorsConfiguration("/**", configuration);
```
**Line Purpose**: Applies CORS config to all endpoints
**What It Does**: Pattern `/**` means apply to all URLs
**Why**: Consistent CORS rules across entire API

```java
        return source;
```
**Line Purpose**: Returns configured CORS source
**Returns**: `CorsConfigurationSource` used by Spring Security

---

## 📁 KeycloakJwtAuthenticationConverter.java
**Location**: `api-gateway/src/main/java/com/huzaifaproject/apigateway/config/KeycloakJwtAuthenticationConverter.java`

```java
package com.huzaifaproject.apigateway.config;
```
**Line Purpose**: Package for configuration classes

```java
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;
```
**Line Purpose**: Imports for JWT conversion and reactive types
- `Converter`: Interface for converting one type to another
- `GrantedAuthority`: Represents an authority (role/permission)
- `Jwt`: Represents JWT token
- `Mono`: Reactive type representing 0 or 1 element

```java
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
```
**Line Purpose**: Java utility imports

```java
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {
```
**Line Purpose**: Custom JWT converter for Keycloak tokens
**What It Does**: Converts Keycloak JWT to Spring Security Authentication
**Implements**: `Converter<Jwt, Mono<AbstractAuthenticationToken>>`
- **Input**: JWT token
- **Output**: Reactive Mono wrapping Authentication
**Why Needed**: Keycloak JWT structure differs from Spring's expectations

```java
    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
```
**Line Purpose**: Main conversion method
**Parameters**: `Jwt jwt` - Keycloak JWT token
**Returns**: `Mono<AbstractAuthenticationToken>` - Reactive authentication
**What It Does**: Extracts roles and creates authentication object

```java
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
```
**Line Purpose**: Extracts user authorities (roles) from JWT
**What It Does**: Calls helper method to get roles from token
**Returns**: Collection of `GrantedAuthority` objects (roles)
**Example**: [ROLE_ADMIN, ROLE_USER]

```java
        return Mono.just(new JwtAuthenticationToken(jwt, authorities));
```
**Line Purpose**: Creates and returns authentication token
**What It Does**:
1. Creates `JwtAuthenticationToken` with JWT and authorities
2. Wraps in `Mono.just()` for reactive API
3. Spring Security uses this for authorization checks
**Returns**: Reactive authentication token
**Why Useful**: Spring Security uses this to check `.hasRole("ADMIN")`

```java
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
```
**Line Purpose**: Helper method to extract authorities from JWT
**Parameters**: `Jwt jwt` - JWT token
**Returns**: Collection of `GrantedAuthority` objects
**Visibility**: `private` - internal helper method

```java
        // Extract realm roles from Keycloak JWT
```
**Line Purpose**: Comment explaining what this method does
**Why Comment**: Clarifies Keycloak-specific JWT structure

```java
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
```
**Line Purpose**: Gets realm_access claim from JWT
**What It Does**: 
- Keycloak stores realm roles in `realm_access` claim
- Returns Map with roles array
**Example JWT Structure**:
```json
{
  "realm_access": {
    "roles": ["ADMIN", "USER"]
  }
}
```
**Returns**: Map containing roles information or null

```java
        if (realmAccess == null) {
            return Collections.emptyList();
        }
```
**Line Purpose**: Handle case where realm_access doesn't exist
**What It Does**: If no realm_access claim, return empty list
**Why**: User might not have any roles, or token format differs
**Returns**: Empty list (no authorities)

```java
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) realmAccess.get("roles");
```
**Line Purpose**: Extracts roles array from realm_access
**What It Does**: Gets "roles" key from realm_access map
**Type Cast**: Casts to `List<String>` (roles are strings)
**@SuppressWarnings**: Suppresses unchecked cast warning
**Example**: ["ADMIN", "USER"]

```java
        if (roles == null) {
            return Collections.emptyList();
        }
```
**Line Purpose**: Handle case where roles array doesn't exist
**What It Does**: If no roles in realm_access, return empty list
**Why**: Token might have realm_access but no roles

```java
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toList());
```
**Line Purpose**: Converts Keycloak roles to Spring Security authorities
**What It Does**:
1. **`roles.stream()`**: Creates stream from roles list
2. **`.map()`**: Transforms each role
3. **`"ROLE_" + role.toUpperCase()`**: Adds ROLE_ prefix, converts to uppercase
   - "ADMIN" becomes "ROLE_ADMIN"
   - "user" becomes "ROLE_USER"
4. **`new SimpleGrantedAuthority()`**: Creates Spring Security authority
5. **`.collect(Collectors.toList())`**: Collects to list
**Why ROLE_ Prefix**: Spring Security convention, `.hasRole("ADMIN")` checks for "ROLE_ADMIN"
**Why Uppercase**: Consistency, Spring Security is case-sensitive
**Returns**: List of GrantedAuthority objects

---

# 3. Product Service

## 📁 ProductServiceApplication.java
**Location**: `product-service/src/main/java/com/huzaifaproject/productservice/ProductServiceApplication.java`

```java
package com.huzaifaproject.productservice;
```
**Line Purpose**: Package declaration

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
```
**Line Purpose**: Spring Boot imports

```java
@SpringBootApplication
```
**Line Purpose**: Main Spring Boot annotation
**What It Enables**:
- Component scanning in this package
- Auto-configuration (MongoDB, Web, etc.)
- Configuration processing

```java
public class ProductServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
```
**Line Purpose**: Entry point
**What It Does**: Starts Product Service with MongoDB and REST capabilities

---

## 📁 ProductController.java
**Location**: `product-service/src/main/java/com/huzaifaproject/productservice/controller/ProductController.java`

```java
package com.huzaifaproject.productservice.controller;
```
**Line Purpose**: Package for controller classes

```java
import com.huzaifaproject.productservice.dto.ProductRequest;
import com.huzaifaproject.productservice.dto.ProductResponse;
import com.huzaifaproject.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
```
**Line Purpose**: Imports for REST controller
- DTOs for request/response
- Service layer
- Lombok for constructor injection
- Spring Web annotations

```java
import java.util.List;
```
**Line Purpose**: Java List import

```java
@RestController
```
**Line Purpose**: Marks class as REST controller
**What It Does**:
- Combines `@Controller` and `@ResponseBody`
- All methods return data (not views)
- Automatically serializes to JSON
**Why Useful**: No need to add `@ResponseBody` to each method

```java
@RequestMapping("/api/product")
```
**Line Purpose**: Base URL for all endpoints in this controller
**What It Does**: All methods inherit this base path
**Example**: Method `@GetMapping` becomes `/api/product` (GET)
**Why Useful**: Centralizes URL prefix, easy to change

```java
@RequiredArgsConstructor
```
**Line Purpose**: Lombok generates constructor for final fields
**What It Does**: Generates:
```java
public ProductController(ProductService productService) {
    this.productService = productService;
}
```
**Why Useful**: Less boilerplate, automatic dependency injection

```java
public class ProductController {
```
**Line Purpose**: REST controller class declaration

```java
    private final ProductService productService;
```
**Line Purpose**: Injects ProductService dependency
**`final`**: Must be set in constructor, cannot change
**Why**: Controller delegates business logic to service layer
**Pattern**: Controller → Service → Repository (layered architecture)

```java
    @PostMapping
```
**Line Purpose**: Maps HTTP POST to this method
**Full URL**: POST `/api/product`
**What It Does**: Handles product creation requests
**Why POST**: Creating new resource (REST convention)

```java
    @ResponseStatus(HttpStatus.CREATED)
```
**Line Purpose**: Returns 201 Created status code
**What It Does**: Overrides default 200 OK status
**Why 201**: REST convention for successful resource creation
**Useful**: Client knows resource was created, not just "OK"

```java
    public void createProduct(@RequestBody ProductRequest productRequest) {
```
**Line Purpose**: Method to create product
**`@RequestBody`**: Deserializes JSON request body to ProductRequest object
**Example JSON**:
```json
{
  "name": "iPhone 13",
  "description": "Latest Apple phone",
  "price": 999.99
}
```
**Returns**: `void` - no response body, just 201 status
**Why void**: Status code 201 is sufficient confirmation

```java
        productService.createProduct(productRequest);
```
**Line Purpose**: Delegates to service layer
**What It Does**: Service layer handles business logic and database save
**Why**: Separation of concerns - controller handles HTTP, service handles business logic
**Returns**: Nothing (void method)

```java
    @GetMapping
```
**Line Purpose**: Maps HTTP GET to this method
**Full URL**: GET `/api/product`
**What It Does**: Handles retrieve all products request
**Why GET**: Reading data (REST convention)

```java
    @ResponseStatus(HttpStatus.OK)
```
**Line Purpose**: Returns 200 OK status
**What It Does**: Explicit status code (though 200 is default for GET)
**Why**: Clear documentation of expected status

```java
    public List<ProductResponse> getAllProducts() {
```
**Line Purpose**: Method to get all products
**Returns**: `List<ProductResponse>` - array of product objects
**No Parameters**: Getting all products, no filtering
**Why List**: Multiple products returned as array

```java
        return productService.getAllProducts();
```
**Line Purpose**: Delegates to service, returns result
**What It Does**: Service fetches from database, maps to DTOs
**Returns**: List of ProductResponse objects
**Serialization**: Spring automatically converts to JSON array

---

## 📁 ProductService.java
**Location**: `product-service/src/main/java/com/huzaifaproject/productservice/service/ProductService.java`

```java
package com.huzaifaproject.productservice.service;
```
**Line Purpose**: Package for service layer classes

```java
import com.huzaifaproject.productservice.dto.ProductRequest;
import com.huzaifaproject.productservice.dto.ProductResponse;
import com.huzaifaproject.productservice.model.Product;
import com.huzaifaproject.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
```
**Line Purpose**: Imports for service layer
- DTOs for data transfer
- Model for entity
- Repository for database
- Lombok utilities
- @Service annotation

```java
import java.util.List;
```
**Line Purpose**: Java List import

```java
@Service
```
**Line Purpose**: Marks class as Spring service component
**What It Does**:
- Spring creates singleton bean
- Component scanning detects it
- Available for dependency injection
**Why**: Business logic layer, between controller and repository

```java
@RequiredArgsConstructor
```
**Line Purpose**: Lombok generates constructor for final fields
**Generates**: Constructor with `productRepository` parameter

```java
@Slf4j
```
**Line Purpose**: Lombok generates logger field
**Generates**: `private static final Logger log = LoggerFactory.getLogger(ProductService.class);`
**Why Useful**: No need to manually create logger
**Usage**: `log.info()`, `log.error()`, etc.

```java
public class ProductService {
```
**Line Purpose**: Service class declaration

```java
    private final ProductRepository productRepository;
```
**Line Purpose**: Injects repository dependency
**`final`**: Must be set in constructor
**Why**: Service uses repository for database operations

```java
    public void createProduct(ProductRequest productRequest) {
```
**Line Purpose**: Business logic method to create product
**Parameters**: `ProductRequest` - DTO with product data
**Returns**: `void` - no return value
**Why void**: Operation is fire-and-forget, success indicated by no exception

```java
        Product product = Product.builder()
```
**Line Purpose**: Starts building Product entity using builder pattern
**What It Does**: Lombok's @Builder generates fluent API
**Why Builder**: Clean, readable way to create objects
**Pattern**: Builder pattern for object construction

```java
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
```
**Line Purpose**: Sets product fields from request DTO
**What It Does**: Copies data from DTO to entity
**Why Separate**: DTO for API contract, Entity for database
**Benefit**: Can validate/transform data here if needed

```java
                .build();
```
**Line Purpose**: Builds final Product object
**Returns**: Immutable Product entity with set fields
**ID**: Not set here, MongoDB generates it on save

```java
        productRepository.save(product);
```
**Line Purpose**: Saves product to MongoDB
**What It Does**:
1. Connects to MongoDB
2. Inserts document into "product" collection
3. Generates unique `_id` field
4. Returns saved product (with ID)
**Why**: Persist data to database
**Transaction**: MongoDB operation is atomic

```java
        log.info("Product {} is saved", product.getId());
```
**Line Purpose**: Logs successful save operation
**What It Does**:
- Writes to application log
- Includes generated product ID
- Log level: INFO (informational message)
**Why**: Monitoring, debugging, audit trail
**Example Output**: `Product 507f1f77bcf86cd799439011 is saved`

```java
    public List<ProductResponse> getAllProducts() {
```
**Line Purpose**: Method to retrieve all products
**Returns**: `List<ProductResponse>` - all products as DTOs
**No Parameters**: Retrieves entire catalog

```java
        List<Product> products = productRepository.findAll();
```
**Line Purpose**: Fetches all products from MongoDB
**What It Does**:
- Queries MongoDB: `db.product.find({})`
- Returns all documents from "product" collection
- Deserializes to Product entities
**Returns**: List of Product entities
**Performance Note**: For large catalogs, consider pagination

```java
        return products.stream().map(this::mapToProductResponse).toList();
```
**Line Purpose**: Transforms Product entities to ProductResponse DTOs
**What It Does**:
1. **`.stream()`**: Creates stream from list
2. **`.map(this::mapToProductResponse)`**: Transforms each Product to ProductResponse
3. **`this::mapToProductResponse`**: Method reference to mapper method
4. **`.toList()`**: Collects to List (Java 16+)
**Why Map**: Never expose entities directly, use DTOs
**Returns**: List of ProductResponse DTOs

```java
    private ProductResponse mapToProductResponse(Product product) {
```
**Line Purpose**: Helper method to map entity to DTO
**Parameters**: `Product` - database entity
**Returns**: `ProductResponse` - DTO for API
**Visibility**: `private` - internal helper

```java
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .build();
```
**Line Purpose**: Creates ProductResponse using builder
**What It Does**: Copies fields from entity to DTO
**Why**: Separate API contract from database model
**Benefit**: Can exclude fields, add computed fields, etc.
**Returns**: ProductResponse DTO

---

## 📁 Product.java (Model)
**Location**: `product-service/src/main/java/com/huzaifaproject/productservice/model/Product.java`

```java
package com.huzaifaproject.productservice.model;
```
**Line Purpose**: Package for domain models/entities

```java
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
```
**Line Purpose**: Imports for MongoDB entity
- Lombok annotations for boilerplate
- `@Id`: Marks primary key
- `@Document`: MongoDB document mapping

```java
import java.math.BigDecimal;
```
**Line Purpose**: Import for precise decimal numbers
**Why BigDecimal**: Accurate money calculations (avoids float rounding errors)

```java
@Document(value = "product")
```
**Line Purpose**: Maps class to MongoDB collection
**What It Does**:
- Collection name: "product"
- Spring Data MongoDB stores instances here
- Equivalent to table in SQL
**Example**: Creates/uses `product` collection in MongoDB

```java
@AllArgsConstructor
```
**Line Purpose**: Lombok generates constructor with all fields
**Generates**: `Product(String id, String name, String description, BigDecimal price)`
**Why**: Easy to create fully populated objects

```java
@NoArgsConstructor
```
**Line Purpose**: Lombok generates no-args constructor
**Generates**: `Product()`
**Why Needed**: MongoDB and Spring require it for deserialization

```java
@Builder
```
**Line Purpose**: Lombok generates builder pattern
**Generates**: Fluent API for object construction
**Usage**: `Product.builder().name("iPhone").price(999.99).build()`
**Why**: Clean, readable object creation

```java
@Data
```
**Line Purpose**: Lombok generates getters, setters, toString, equals, hashCode
**Generates**:
- Getter for each field
- Setter for each non-final field
- `toString()` method
- `equals()` and `hashCode()` methods
**Why**: Eliminates boilerplate

```java
public class Product {
```
**Line Purpose**: Entity class declaration

```java
    @Id
```
**Line Purpose**: Marks field as MongoDB document ID
**What It Does**:
- Maps to `_id` field in MongoDB
- MongoDB generates unique value if null
- Used for document identification
**Type**: String (MongoDB ObjectId as string)

```java
    private String id;
```
**Line Purpose**: Primary key field
**Type**: `String` - MongoDB ObjectId in string format
**Example**: "507f1f77bcf86cd799439011"

```java
    private String name;
    private String description;
```
**Line Purpose**: Product text fields
**Type**: `String`
**Why**: Product catalog information

```java
    private BigDecimal price;
```
**Line Purpose**: Product price field
**Type**: `BigDecimal` - precise decimal arithmetic
**Why BigDecimal**: Avoids floating-point rounding errors with money
**Example**: `new BigDecimal("999.99")` (always use string constructor)
**Alternative**: `double` would cause rounding issues (0.1 + 0.2 ≠ 0.3)

---

## 📁 ProductRepository.java
**Location**: `product-service/src/main/java/com/huzaifaproject/productservice/repository/ProductRepository.java`

```java
package com.huzaifaproject.productservice.repository;
```
**Line Purpose**: Package for repository interfaces

```java
import com.huzaifaproject.productservice.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
```
**Line Purpose**: Imports for MongoDB repository
- `Product`: Entity to manage
- `MongoRepository`: Spring Data MongoDB interface

```java
public interface ProductRepository extends MongoRepository<Product, String> {
}
```
**Line Purpose**: Repository interface for Product
**Extends**: `MongoRepository<Product, String>`
- **Product**: Entity type
- **String**: ID type (MongoDB ObjectId as String)
**What It Provides**: (Auto-implemented by Spring Data)
- `save(Product)` - Insert/update
- `findAll()` - Get all documents
- `findById(String)` - Get by ID
- `delete(Product)` - Remove document
- `count()` - Count documents
- And many more...
**No Implementation Needed**: Spring Data generates implementation at runtime
**Why**: Reduces boilerplate, standard CRUD operations provided

---

## 📁 ProductRequest.java (DTO)
**Location**: `product-service/src/main/java/com/huzaifaproject/productservice/dto/ProductRequest.java`

```java
package com.huzaifaproject.productservice.dto;
```
**Line Purpose**: Package for Data Transfer Objects

```java
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
```
**Line Purpose**: Lombok imports

```java
import java.math.BigDecimal;
```
**Line Purpose**: BigDecimal for price

```java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
```
**Line Purpose**: Lombok annotations for POJO
**What It Generates**: Full POJO with getters, setters, constructors, builder

```java
public class ProductRequest {
    private String name;
    private String description;
    private BigDecimal price;
}
```
**Line Purpose**: DTO for creating products
**Purpose**: 
- Separates API contract from database model
- No `id` field (server generates it)
- Client sends only necessary data
**Why DTO**: 
- Validation can differ from entity
- Can add annotations like @NotNull
- API stability (entity changes don't affect API)

---

## 📁 ProductResponse.java (DTO)
**Location**: `product-service/src/main/java/com/huzaifaproject/productservice/dto/ProductResponse.java`

```java
package com.huzaifaproject.productservice.dto;
```
**Line Purpose**: Package for DTOs

```java
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
```
**Line Purpose**: Imports

```java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
}
```
**Line Purpose**: DTO for returning products
**Purpose**: Shapes API response
**Includes**: 
- `id` field (client needs to identify product)
- All display fields
**Why DTO**: Can exclude sensitive fields, add computed fields, etc.

---

# 4. Order Service

## 📁 OrderServiceApplication.java
**Location**: `order-service/src/main/java/com/huzaifaproject/orderservice/OrderServiceApplication.java`

```java
package com.huzaifaproject.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
```
**Line Purpose**: Entry point for Order Service
**What It Enables**:
- MySQL database connectivity
- REST endpoints
- Service discovery client
- WebClient for calling Inventory Service
- Resilience4j for circuit breaker

---

## 📁 OrderController.java
**Location**: `order-service/src/main/java/com/huzaifaproject/orderservice/controller/OrderController.java`

```java
package com.huzaifaproject.orderservice.controller;

import com.huzaifaproject.orderservice.dto.OrderRequest;
import com.huzaifaproject.orderservice.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;
```
**Line Purpose**: Imports for REST controller with resilience
- Resilience4j annotations for fault tolerance
- CompletableFuture for async operations

```java
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
```
**Line Purpose**: REST controller setup
**Base URL**: `/api/order`

```java
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
```
**Line Purpose**: POST endpoint to create order
**URL**: POST `/api/order`
**Status**: 201 Created

```java
    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
```
**Line Purpose**: Enables circuit breaker pattern
**What It Does**:
- Monitors calls to inventory service
- If failures exceed threshold, opens circuit
- When circuit is open, calls fallback directly (no inventory call)
- Prevents cascading failures
**name**: "inventory" - references configuration in application.properties
**fallbackMethod**: Method to call when circuit is open or request fails
**Why**: If Inventory Service is down, don't crash Order Service

```java
    @TimeLimiter(name = "inventory")
```
**Line Purpose**: Sets timeout for operation
**What It Does**:
- Operation must complete within configured time
- If timeout exceeded, triggers fallback
- Prevents hanging requests
**Configuration**: Defined in application.properties
**Why**: Don't wait forever for slow services

```java
    @Retry(name = "inventory")
```
**Line Purpose**: Enables automatic retry logic
**What It Does**:
- Retries failed requests automatically
- Configurable max attempts and delay
- Helps with transient failures (network blips)
**Why**: Temporary failures might succeed on retry

```java
    public CompletableFuture<String> placeOrder(@RequestBody OrderRequest orderRequest) {
```
**Line Purpose**: Async method to place order
**Parameters**: `OrderRequest` - order details
**Returns**: `CompletableFuture<String>` - async result
**Why CompletableFuture**: 
- Non-blocking, reactive
- Required by @TimeLimiter
- Better performance for I/O operations

```java
        log.info("Placing Order");
```
**Line Purpose**: Log order placement attempt

```java
        return CompletableFuture.supplyAsync(() -> orderService.placeOrder(orderRequest));
```
**Line Purpose**: Executes order placement asynchronously
**What It Does**:
1. Creates CompletableFuture
2. Runs orderService.placeOrder() in separate thread
3. Returns future immediately
4. Client gets result when operation completes
**Why Async**: Non-blocking, handles concurrent requests better
**Returns**: Future resolving to "Order Placed" or error

```java
    public CompletableFuture<String> fallbackMethod(OrderRequest orderRequest, RuntimeException runtimeException) {
```
**Line Purpose**: Fallback method when service fails
**Parameters**:
- `OrderRequest`: Original request (for logging/alternative processing)
- `RuntimeException`: The exception that triggered fallback
**Returns**: `CompletableFuture<String>` - friendly error message
**Why**: Provide graceful degradation instead of error

```java
        log.info("Cannot Place Order Executing Fallback logic");
```
**Line Purpose**: Log fallback execution
**Why**: Important for monitoring and debugging

```java
        return CompletableFuture.supplyAsync(() -> "Oops! Something went wrong, please order after some time!");
```
**Line Purpose**: Return user-friendly error message
**What It Does**: Instead of 500 error, returns polite message
**Why**: Better user experience during outages
**Alternative**: Could queue order for later processing

---

## 📁 OrderService.java
**Location**: `order-service/src/main/java/com/huzaifaproject/orderservice/service/OrderService.java`

*Due to length limits, I'll continue with the remaining services in the next file...*
