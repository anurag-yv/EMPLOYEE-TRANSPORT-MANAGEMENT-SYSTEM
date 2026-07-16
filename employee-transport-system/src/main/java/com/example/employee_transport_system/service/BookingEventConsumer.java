package com.example.employee_transport_system.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Consumes booking events from Kafka.
 * Real-world usages: push notifications, email alerts, audit logging, analytics.
 */
@Service
public class BookingEventConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookingEventConsumer.class);

    @KafkaListener(topics = "booking-events", groupId = "transport-system")
    public void listen(final String eventJson) {
        LOGGER.info("Received booking event: {}", eventJson);
    }
}