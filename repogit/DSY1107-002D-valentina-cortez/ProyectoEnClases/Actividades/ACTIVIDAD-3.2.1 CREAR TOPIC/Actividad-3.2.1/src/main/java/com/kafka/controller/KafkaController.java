package com.kafka.controller;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kafka")
public class KafkaController {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaController(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @GetMapping("/send")
    public String sendMessage(
            @RequestParam String message,
            @RequestParam(required = false) String key) {

        String topicName = "tutorial-particiones";

        if (key != null && !key.isEmpty()) {
            // Con Key: Hash(key) asegura que vayan siempre a la misma partición
            kafkaTemplate.send(topicName, key, message);
            return "Mensaje enviado al topic '" + topicName + "' con KEY: " + key + " | MENSAJE: " + message;
        } else {
            // Sin Key: Uso de Round Robin entre particiones 0, 1, 2
            kafkaTemplate.send(topicName, message);
            return "Mensaje enviado al topic '" + topicName + "' (sin key - distribución automática) | MENSAJE: " + message;
        }
    }

    @GetMapping("/health")
    public String health() {
        return "Aplicación de Kafka Tutorial está corriendo en puerto 8888";
    }
}