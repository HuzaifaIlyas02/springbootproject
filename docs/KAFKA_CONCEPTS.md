# Apache Kafka - Complete Guide

## Table of Contents
1. [What is Kafka?](#what-is-kafka)
2. [Core Concepts](#core-concepts)
3. [Kafka Architecture](#kafka-architecture)
4. [How Kafka Works](#how-kafka-works)
5. [Implementation in Your Project](#implementation-in-your-project)
6. [Verification Results](#verification-results)
7. [Frontend vs Backend](#frontend-vs-backend)
8. [Use Cases](#use-cases)
9. [Best Practices](#best-practices)

---

## What is Kafka?

Apache Kafka is a **distributed streaming platform** and **message broker** that enables applications to:
- Publish and subscribe to streams of records (messages)
- Store streams of records in a fault-tolerant way
- Process streams of records as they occur

**Key Characteristics:**
- **High Throughput**: Can handle millions of messages per second
- **Distributed**: Runs as a cluster across multiple servers
- **Persistent**: Messages are stored on disk and replicated
- **Real-time**: Low latency message delivery (milliseconds)
- **Scalable**: Can scale horizontally by adding more brokers

---

## Core Concepts

### 1. **Topic**
- A **category or feed name** to which records are published
- Similar to a database table or folder in a filesystem
- Topics are **multi-subscriber** (many consumers can read from the same topic)
- **Example**: `order-placed`, `payment-processed`, `inventory-updated`

### 2. **Producer**
- An application that **publishes (writes) messages** to Kafka topics
- Producers decide which partition to send messages to
- Can send messages synchronously or asynchronously
- **In your project**: Order Service acts as a producer

### 3. **Consumer**
- An application that **subscribes to (reads) messages** from Kafka topics
- Consumers read messages at their own pace
- Can be part of a consumer group for load balancing
- **In your project**: Inventory Service acts as a consumer

### 4. **Broker**
- A **Kafka server** that stores and serves messages
- A Kafka cluster consists of multiple brokers
- Each broker handles read/write requests and replicates data
- **In your project**: Single broker running in Docker container

### 5. **Partition**
- Topics are split into **partitions** for parallel processing
- Each partition is an ordered, immutable sequence of messages
- Messages within a partition have a unique **offset** (sequential ID)
- Enables horizontal scaling and parallelism
- **Example**: `order-placed-0` (partition 0 of order-placed topic)

### 6. **Consumer Group**
- A group of consumers working together to consume a topic
- Each partition is consumed by **only one consumer** in a group
- Enables load balancing and fault tolerance
- **In your project**: `inventory-service` is the consumer group ID

### 7. **Offset**
- A **unique sequential ID** assigned to each message in a partition
- Consumers track their position using offsets
- Enables replay (re-reading old messages)
- Stored in Kafka for durability

### 8. **Replication**
- Copies of data stored across multiple brokers
- Provides **fault tolerance** and high availability
- Leader handles all reads/writes, followers replicate data
- **In your project**: Replication factor = 1 (single broker setup)

### 9. **ZooKeeper (Legacy) vs KRaft**
- **ZooKeeper**: Coordination service used in older Kafka versions
- **KRaft**: New consensus protocol (Kafka Raft) - no ZooKeeper needed
- **In your project**: Using KRaft mode (Apache Kafka 3.7.0)

---

## Kafka Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      Kafka Cluster                          │
│                                                             │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐                │
│  │ Broker 1 │  │ Broker 2 │  │ Broker 3 │                │
│  │          │  │          │  │          │                │
│  │ Topic A  │  │ Topic A  │  │ Topic B  │                │
│  │ Part 0   │  │ Part 1   │  │ Part 0   │                │
│  └──────────┘  └──────────┘  └──────────┘                │
│                                                             │
└─────────────────────────────────────────────────────────────┘
         ▲                            │
         │                            │
    [Producer]                   [Consumer]
    Publishes                     Subscribes
    Messages                      to Messages
```

### Message Flow:
1. **Producer** sends message to a topic
2. **Kafka** stores message in a partition (append-only log)
3. **Consumer** reads message from the partition
4. **Consumer** commits offset (marks message as read)

---

## How Kafka Works

### Publishing a Message (Producer):
```java
// 1. Producer creates a message
String orderNumber = "946e8f88-6b4e-482e-b35d-1e8a6a21c3ff";

// 2. Producer sends to Kafka topic
kafkaTemplate.send("order-placed", orderNumber);

// 3. Kafka appends to partition log
// order-placed-0: [offset-0] -> "946e8f88..."
```

### Consuming a Message (Consumer):
```java
// 1. Consumer subscribes to topic
@KafkaListener(topics = "order-placed", groupId = "inventory-service")
public void handleOrderMessage(String message) {
    // 2. Kafka delivers message to consumer
    log.info("Received: {}", message);
    
    // 3. Consumer processes message
    // Update inventory, send notification, etc.
    
    // 4. Kafka auto-commits offset (message marked as read)
}
```

### Message Persistence:
- Messages are written to **disk** (not just memory)
- Kafka uses **sequential writes** for high performance
- Messages are retained for a **configurable time** (default: 7 days)
- Consumers can **replay** old messages by resetting offsets

### Parallelism:
```
Topic: order-placed (3 partitions)
├── Partition 0 → Consumer A (Group: inventory-service)
├── Partition 1 → Consumer B (Group: inventory-service)
└── Partition 2 → Consumer C (Group: inventory-service)
```
Each consumer reads from one partition independently!

---

## Implementation in Your Project

### Architecture:
```
┌─────────────────┐          ┌─────────────┐
│  Order Service  │          │    Kafka    │
│   (Producer)    │─────────>│   Broker    │
│                 │  sends   │             │
│                 │  order#  │ Topic:      │
└─────────────────┘          │ order-placed│
                             └──────┬──────┘
                                    │
                                    │ delivers
                                    │ message
                                    ▼
                          ┌────────────────────┐
                          │ Inventory Service  │
                          │   (Consumer)       │
                          │                    │
                          │ Logs order number  │
                          └────────────────────┘
```

### Configuration Files:

#### 1. Docker Compose (`docker-compose.yml`):
```yaml
kafka:
  container_name: kafka
  image: apache/kafka:3.7.0
  ports:
    - "9092:9092"
  environment:
    KAFKA_NODE_ID: 1
    KAFKA_PROCESS_ROLES: broker,controller
    KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093
    KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
    CLUSTER_ID: 5L6g3nShT-eMCtK--X86sw
```

#### 2. Order Service (Producer):

**POM Dependency:**
```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
    <!-- Version managed by Spring Boot parent -->
</dependency>
```

**Configuration (`KafkaProducerConfig.java`):**
```java
@Configuration
public class KafkaProducerConfig {
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(
        ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
```

**Producer (`OrderEventProducer.java`):**
```java
@Component
@RequiredArgsConstructor
public class OrderEventProducer {
    @Value("${app.topics.order-placed:order-placed}")
    private String orderPlacedTopic;
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    
    // Sends order number as plain text to Kafka
    public void sendOrderPlaced(String orderNumber) {
        kafkaTemplate.send(orderPlacedTopic, orderNumber);
    }
}
```

**Service Layer (`OrderService.java`):**
```java
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderEventProducer orderEventProducer;
    
    public String placeOrder(OrderRequest request) {
        // 1. Save order to database
        Order order = orderRepository.save(newOrder);
        
        // 2. Publish event to Kafka
        orderEventProducer.sendOrderPlaced(order.getOrderNumber());
        
        return order.getOrderNumber();
    }
}
```

**Application Properties:**
```properties
# Local
spring.kafka.bootstrap-servers=localhost:9092

# Docker
spring.kafka.bootstrap-servers=kafka:9092
```

#### 3. Inventory Service (Consumer):

**POM Dependency:**
```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

**Configuration (`KafkaConsumerConfig.java`):**
```java
@Configuration
public class KafkaConsumerConfig {
    // Spring Boot auto-configures Kafka consumer
    // No additional config needed for basic setup
}
```

**Consumer (`OrderKafkaListener.java`):**
```java
@Component
@Slf4j
public class OrderKafkaListener {
    
    // Listens for messages from order-placed topic
    @KafkaListener(
        topics = "${app.topics.order-placed:order-placed}",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleOrderMessage(String message) {
        // Logs the order number received from Kafka
        log.info("Received Kafka message: {}", message);
        
        // TODO: Update inventory, send notifications, etc.
    }
}
```

**Application Properties:**
```properties
spring.kafka.bootstrap-servers=kafka:9092
spring.kafka.consumer.group-id=inventory-service
spring.kafka.consumer.auto-offset-reset=earliest
```

---

## Verification Results

### ✅ Kafka is Working Successfully!

**Evidence:**
1. **Message Produced**: Order Service sent order number `946e8f88-6b4e-482e-b35d-1e8a6a21c3ff`
2. **Message Stored**: Kafka stored message in topic `order-placed` partition 0
3. **Message Consumed**: Inventory Service received and logged the message

**Logs Confirmation:**

**Inventory Service Consumer Log:**
```
2025-11-14T16:30:41.567Z INFO [inventory-service] 
c.h.i.event.OrderKafkaListener : Received Kafka message: 946e8f88-6b4e-482e-b35d-1e8a6a21c3ff
```

**Kafka Topic Content:**
```bash
$ kafka-console-consumer.sh --topic order-placed --from-beginning
946e8f88-6b4e-482e-b35d-1e8a6a21c3ff
Processed a total of 1 messages
```

**Consumer Group Status:**
```
Successfully joined group with generation Generation{
    generationId=1,
    memberId='consumer-inventory-service-1-7ba10687-3cc8-4925-ba30-ab8a3cabb039',
    protocol='range'
}
Successfully synced group in generation 1
Adding newly assigned partitions: order-placed-0
```

### Message Flow:
```
Order Placed (Frontend)
         │
         ▼
Order Service REST API
         │
         ▼
Save to Database
         │
         ▼
orderEventProducer.sendOrderPlaced("946e8f88...")
         │
         ▼
Kafka Topic: order-placed
         │
         ▼
@KafkaListener in Inventory Service
         │
         ▼
Log: "Received Kafka message: 946e8f88..."
```

---

## Frontend vs Backend

### ❌ **Frontend Does NOT Need to Know About Kafka**

Kafka operates **entirely in the backend**. Here's why:

#### Frontend Interaction:
```javascript
// Frontend makes normal HTTP request
const response = await fetch('/api/order', {
    method: 'POST',
    body: JSON.stringify(orderData)
});

// Gets immediate response with order number
console.log(response.orderNumber); // "946e8f88..."

// Frontend is DONE - doesn't know about Kafka!
```

#### Backend Asynchronous Processing:
```
1. Frontend → HTTP POST → Order Service
2. Order Service → Saves order → Returns order number to frontend
3. Order Service → Publishes to Kafka → (Asynchronous, happens later)
4. Inventory Service → Reads from Kafka → (Asynchronous, independent)
5. Inventory Service → Updates inventory → (Happens in background)
```

### Why Kafka is Backend-Only:

| Aspect | Frontend | Backend |
|--------|----------|---------|
| **Request** | Sends HTTP request | Receives HTTP request |
| **Response** | Gets immediate response | Returns response immediately |
| **Kafka** | ❌ No knowledge | ✅ Publishes to Kafka |
| **Processing** | Synchronous | Asynchronous (after response) |
| **Updates** | Uses WebSocket/polling for real-time updates | Kafka for service-to-service communication |

### Real-Time Updates to Frontend:

If you want frontend to see inventory updates in real-time:

**Option 1: WebSocket**
```java
@Controller
public class InventoryWebSocketController {
    @MessageMapping("/inventory")
    @SendTo("/topic/inventory-updates")
    public InventoryUpdate sendUpdate(InventoryUpdate update) {
        return update;
    }
}
```

**Option 2: Server-Sent Events (SSE)**
```java
@GetMapping("/inventory/stream")
public SseEmitter streamInventory() {
    SseEmitter emitter = new SseEmitter();
    // Send updates to frontend
    return emitter;
}
```

**Option 3: Polling (Simple)**
```javascript
// Frontend polls every 5 seconds
setInterval(() => {
    fetch('/api/inventory')
        .then(res => res.json())
        .then(data => updateUI(data));
}, 5000);
```

### Current Implementation:
- ✅ Kafka handles **backend communication** (Order → Inventory)
- ✅ Frontend uses **REST API** for user actions
- ⚠️ Frontend does NOT receive automatic updates (would need WebSocket/SSE)

---

## Use Cases

### 1. **Event-Driven Microservices** (Your Implementation)
```
Order Placed → Kafka → Inventory Update
                    ├→ Email Notification
                    ├→ Payment Processing
                    └→ Shipping Service
```

### 2. **Real-Time Data Pipelines**
```
User Activity → Kafka → Analytics Engine → Dashboard
                      ├→ Recommendation Engine
                      └→ Fraud Detection
```

### 3. **Log Aggregation**
```
Application Logs → Kafka → Log Processor → Elasticsearch
                                        ├→ Alert System
                                        └→ Archive Storage
```

### 4. **Stream Processing**
```
Stock Prices → Kafka → Stream Processor → Trading Alerts
                                       ├→ Price Analysis
                                       └→ Historical Data
```

### 5. **Change Data Capture (CDC)**
```
Database Changes → Kafka → Cache Update
                        ├→ Search Index
                        └→ Data Warehouse
```

---

## Best Practices

### 1. **Topic Design**
- ✅ Use meaningful topic names: `order-placed`, not `topic1`
- ✅ One topic per event type
- ✅ Use consistent naming convention: `entity-action`
- ⚠️ Don't create too many topics (harder to manage)

### 2. **Message Design**
- ✅ Keep messages small (< 1MB recommended)
- ✅ Use JSON for flexibility or Avro for schema evolution
- ✅ Include timestamp and correlation ID
- ❌ Don't send sensitive data without encryption

### 3. **Partitioning Strategy**
```java
// Partition by key for ordering within key
kafkaTemplate.send("topic", key, value); // Same key → same partition

// Round-robin for load balancing
kafkaTemplate.send("topic", value); // No key → round-robin
```

### 4. **Consumer Configuration**
```properties
# Start from beginning (good for new consumers)
spring.kafka.consumer.auto-offset-reset=earliest

# Start from latest (only new messages)
spring.kafka.consumer.auto-offset-reset=latest

# Enable auto-commit (simpler, but less control)
spring.kafka.consumer.enable-auto-commit=true

# Manual commit (more control, better reliability)
spring.kafka.consumer.enable-auto-commit=false
```

### 5. **Error Handling**
```java
@KafkaListener(topics = "order-placed", groupId = "inventory-service")
public void handleOrder(String message) {
    try {
        processOrder(message);
    } catch (Exception e) {
        // Option 1: Log and skip
        log.error("Failed to process: {}", message, e);
        
        // Option 2: Send to dead letter queue
        kafkaTemplate.send("order-placed-dlq", message);
        
        // Option 3: Retry with backoff
        throw new RetryableException(e);
    }
}
```

### 6. **Monitoring**
- ✅ Monitor consumer lag (messages behind)
- ✅ Track message throughput
- ✅ Alert on failed consumers
- ✅ Monitor broker disk usage

### 7. **Testing**
```java
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"order-placed"})
class KafkaIntegrationTest {
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @Test
    void testOrderPlaced() {
        // Send message
        kafkaTemplate.send("order-placed", "test-order");
        
        // Verify consumer received it
        await().atMost(5, SECONDS).until(() -> 
            listenerCapture.getMessages().contains("test-order")
        );
    }
}
```

### 8. **Production Considerations**
- ✅ Use multiple brokers (3+ for production)
- ✅ Set replication factor ≥ 3
- ✅ Enable compression (gzip, snappy, lz4)
- ✅ Configure retention policy (time/size based)
- ✅ Use consumer groups for scalability
- ✅ Implement idempotent consumers (handle duplicates)

---

## Common Kafka Commands

### Topics:
```bash
# List all topics
kafka-topics.sh --list --bootstrap-server localhost:9092

# Create topic
kafka-topics.sh --create --topic order-placed \
  --bootstrap-server localhost:9092 \
  --partitions 3 --replication-factor 1

# Describe topic
kafka-topics.sh --describe --topic order-placed \
  --bootstrap-server localhost:9092

# Delete topic
kafka-topics.sh --delete --topic order-placed \
  --bootstrap-server localhost:9092
```

### Messages:
```bash
# Produce messages (console)
kafka-console-producer.sh --topic order-placed \
  --bootstrap-server localhost:9092

# Consume messages (from beginning)
kafka-console-consumer.sh --topic order-placed \
  --from-beginning --bootstrap-server localhost:9092

# Consume messages (latest only)
kafka-console-consumer.sh --topic order-placed \
  --bootstrap-server localhost:9092
```

### Consumer Groups:
```bash
# List consumer groups
kafka-consumer-groups.sh --list \
  --bootstrap-server localhost:9092

# Describe consumer group
kafka-consumer-groups.sh --describe \
  --group inventory-service \
  --bootstrap-server localhost:9092

# Reset offsets (replay messages)
kafka-consumer-groups.sh --reset-offsets \
  --group inventory-service --topic order-placed \
  --to-earliest --execute \
  --bootstrap-server localhost:9092
```

---

## Summary

### What Kafka Does:
1. ✅ **Decouples services**: Order Service doesn't wait for Inventory Service
2. ✅ **Asynchronous processing**: Messages processed in the background
3. ✅ **Reliable delivery**: Messages stored durably, can be replayed
4. ✅ **Scalable**: Can handle millions of messages per second
5. ✅ **Fault-tolerant**: Replication ensures no data loss

### In Your Project:
- ✅ **Order Service**: Publishes order numbers to `order-placed` topic
- ✅ **Kafka**: Stores messages reliably
- ✅ **Inventory Service**: Consumes and logs order numbers
- ✅ **Frontend**: Uses REST API, no Kafka knowledge needed

### Verification:
```
Order Number: 946e8f88-6b4e-482e-b35d-1e8a6a21c3ff
Status: ✅ Successfully published and consumed
Topic: order-placed
Partition: 0
Consumer Group: inventory-service
```

**Kafka is working perfectly in your microservices architecture! 🎉**

---

## Next Steps

1. **Enhance Consumer**: Add actual inventory update logic instead of just logging
2. **Add More Events**: `order-shipped`, `payment-processed`, `order-cancelled`
3. **Error Handling**: Implement dead letter queue for failed messages
4. **Monitoring**: Add metrics collection (Prometheus, Grafana)
5. **Real-time Updates**: Add WebSocket for frontend notifications
6. **Testing**: Write integration tests with embedded Kafka

---

*Last Updated: November 14, 2025*
