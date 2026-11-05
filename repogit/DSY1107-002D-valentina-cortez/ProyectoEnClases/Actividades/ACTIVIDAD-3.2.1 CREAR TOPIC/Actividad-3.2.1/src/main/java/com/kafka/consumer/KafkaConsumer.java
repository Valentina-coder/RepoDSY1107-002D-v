package com.kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class KafkaConsumer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);
    private static final String SEPARATOR = "=".repeat(100);

    @KafkaListener(topics = "tutorial-particiones", groupId = "mi-grupo-educativo")
    public void listen(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.RECEIVED_KEY) String key) {

        logger.info("{}", SEPARATOR);
        logger.info("   MENSAJE RECIBIDO");
        logger.info("   Partición: {}", partition);
        logger.info("   Offset: {}", offset);
        logger.info("   Key: {}", key != null ? key : "(sin key)");
        logger.info("   Mensaje: {}", message);
        logger.info("{}", SEPARATOR);
    }
}