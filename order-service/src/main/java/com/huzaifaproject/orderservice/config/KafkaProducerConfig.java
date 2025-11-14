package com.huzaifaproject.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

// Simple Kafka configuration that relies on Spring Boot defaults
@Configuration
public class KafkaProducerConfig {

    // Creates a KafkaTemplate bean using the auto-configured producer factory
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
