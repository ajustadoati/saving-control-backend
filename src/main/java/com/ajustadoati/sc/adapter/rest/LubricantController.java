package com.ajustadoati.sc.adapter.rest;

import com.ajustadoati.sc.adapter.rest.dto.request.LubricantOrderRequest;
import com.ajustadoati.sc.adapter.rest.dto.request.LubricantProductRequest;
import com.ajustadoati.sc.adapter.rest.dto.request.LubricantStockEntryRequest;
import com.ajustadoati.sc.adapter.rest.dto.response.LubricantOrderResponse;
import com.ajustadoati.sc.adapter.rest.dto.response.LubricantProductResponse;
import com.ajustadoati.sc.adapter.rest.dto.response.LubricantStockMovementResponse;
import com.ajustadoati.sc.adapter.rest.dto.response.LubricantWeeklyCollectionResponse;
import com.ajustadoati.sc.application.service.LubricantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/lubricants")
@RequiredArgsConstructor
public class LubricantController {

  private final LubricantService lubricantService;

  @GetMapping("/products")
  public ResponseEntity<List<LubricantProductResponse>> getProducts() {
    return ResponseEntity.ok(lubricantService.getProducts());
  }

  @PostMapping("/products")
  public ResponseEntity<LubricantProductResponse> createProduct(@RequestBody LubricantProductRequest request) {
    return ResponseEntity.ok(lubricantService.createProduct(request));
  }

  @PutMapping("/products/{productId}")
  public ResponseEntity<LubricantProductResponse> updateProduct(@PathVariable Integer productId,
                                                                @RequestBody LubricantProductRequest request) {
    return ResponseEntity.ok(lubricantService.updateProduct(productId, request));
  }

  @DeleteMapping("/products/{productId}")
  public ResponseEntity<Void> deleteProduct(@PathVariable Integer productId) {
    lubricantService.deleteProduct(productId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/products/{productId}/stock-entries")
  public ResponseEntity<LubricantProductResponse> addStock(@PathVariable Integer productId,
                                                           @RequestBody LubricantStockEntryRequest request) {
    return ResponseEntity.ok(lubricantService.addStock(productId, request));
  }

  @GetMapping("/products/{productId}/movements")
  public ResponseEntity<List<LubricantStockMovementResponse>> getMovements(@PathVariable Integer productId) {
    return ResponseEntity.ok(lubricantService.getMovements(productId));
  }

  @PostMapping("/orders")
  public ResponseEntity<LubricantOrderResponse> createOrder(@RequestBody LubricantOrderRequest request) {
    return ResponseEntity.ok(lubricantService.createOrder(request));
  }

  @GetMapping("/orders/user/{userId}")
  public ResponseEntity<List<LubricantOrderResponse>> getOrdersByUser(@PathVariable Integer userId) {
    return ResponseEntity.ok(lubricantService.getOrdersByUser(userId));
  }

  @GetMapping("/weekly-collection")
  public ResponseEntity<List<LubricantWeeklyCollectionResponse>> getWeeklyCollection(@RequestParam(required = false) LocalDate date) {
    return ResponseEntity.ok(lubricantService.getWeeklyCollection(date));
  }
}
