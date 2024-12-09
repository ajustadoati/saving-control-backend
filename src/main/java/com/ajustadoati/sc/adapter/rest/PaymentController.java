package com.ajustadoati.sc.adapter.rest;

import com.ajustadoati.sc.adapter.rest.dto.request.PaymentFileRequest;
import com.ajustadoati.sc.adapter.rest.dto.request.PaymentRequest;
import com.ajustadoati.sc.adapter.rest.dto.response.PaymentResponse;
import com.ajustadoati.sc.application.service.PaymentService;
import com.ajustadoati.sc.application.service.file.FileService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/payments")
@AllArgsConstructor
public class PaymentController {


  private final PaymentService paymentService;

  private final FileService fileService;

  @PostMapping
  public ResponseEntity<PaymentResponse> processPayments(@RequestBody PaymentRequest request) {
    return ResponseEntity.ok(paymentService.processPayments(request));
  }

  @PostMapping("/to-file")
  public ResponseEntity<String> registrarPago(@RequestBody PaymentFileRequest paymentFileRequest) {
    try {
      // Llama al servicio para agregar el registro al Excel
      fileService.registrarPagoAhorro(
        paymentFileRequest.getNombre(),
        paymentFileRequest.getTipoPago(),
        paymentFileRequest.getMonto(),
        paymentFileRequest.getFecha()
      );

      return ResponseEntity.ok("Pago registrado con éxito en la hoja: " + paymentFileRequest.getTipoPago());

    } catch (IOException e) {
      e.printStackTrace();
      return ResponseEntity.status(500).body("Error al procesar el archivo Excel.");
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }
}
