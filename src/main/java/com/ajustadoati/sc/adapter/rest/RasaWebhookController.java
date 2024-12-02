package com.ajustadoati.sc.adapter.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/rasa")
@Slf4j
public class RasaWebhookController {

  @PostMapping("/webhook")
  public ResponseEntity<Map<String, String>> handleRasaWebhook(@RequestBody Map<String, Object> payload) {
    System.out.println("Received message from Rasa: " + payload);

    // Procesa el mensaje recibido desde Rasa
    String userMessage = (String) payload.get("text");

    // Respuesta que Rasa mostrará al usuario
    Map<String, String> response = new HashMap<>();
    response.put("text", "Recibido: " + userMessage);
    log.info("responso {}", response);

    return ResponseEntity.ok(response);
  }
}
