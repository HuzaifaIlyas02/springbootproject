package com.huzaifaproject.inventoryservice.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

// Listens for basic order messages coming from Kafka
@Component
@Slf4j
public class OrderKafkaListener {

    // Consumes plain text messages from the order-placed topic
    @KafkaListener(topics = "${app.topics.order-placed:order-placed}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleOrderMessage(String message) {
        // Logs the received message so we can confirm the async flow
        log.info("Received Kafka message: {}", message);
    }
}
