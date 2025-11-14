package com.huzaifaproject.orderservice.event;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

// Sends very simple String messages to Kafka
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    // Holds the topic name with a default value for local runs
    @Value("${app.topics.order-placed:order-placed}")
    private String orderPlacedTopic;

    // Spring injects a basic KafkaTemplate that publishes String messages
    private final KafkaTemplate<String, String> kafkaTemplate;

    // Sends the order number as plain text to Kafka
    public void sendOrderPlaced(String orderNumber) {
        kafkaTemplate.send(orderPlacedTopic, orderNumber);
    }
}
