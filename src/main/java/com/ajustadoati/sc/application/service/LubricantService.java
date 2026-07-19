package com.ajustadoati.sc.application.service;

import com.ajustadoati.sc.adapter.rest.dto.request.LubricantOrderItemRequest;
import com.ajustadoati.sc.adapter.rest.dto.request.LubricantOrderRequest;
import com.ajustadoati.sc.adapter.rest.dto.request.LubricantProductRequest;
import com.ajustadoati.sc.adapter.rest.dto.request.LubricantStockEntryRequest;
import com.ajustadoati.sc.adapter.rest.dto.request.SupplyRequest;
import com.ajustadoati.sc.adapter.rest.dto.response.LubricantOrderItemResponse;
import com.ajustadoati.sc.adapter.rest.dto.response.LubricantOrderResponse;
import com.ajustadoati.sc.adapter.rest.dto.response.LubricantProductResponse;
import com.ajustadoati.sc.adapter.rest.dto.response.LubricantStockMovementResponse;
import com.ajustadoati.sc.adapter.rest.dto.response.LubricantWeeklyCollectionResponse;
import com.ajustadoati.sc.adapter.rest.dto.response.SupplyResponse;
import com.ajustadoati.sc.adapter.rest.repository.LubricantOrderRepository;
import com.ajustadoati.sc.adapter.rest.repository.LubricantProductRepository;
import com.ajustadoati.sc.adapter.rest.repository.LubricantStockMovementRepository;
import com.ajustadoati.sc.domain.LubricantOrder;
import com.ajustadoati.sc.domain.LubricantOrderItem;
import com.ajustadoati.sc.domain.LubricantProduct;
import com.ajustadoati.sc.domain.LubricantStockMovement;
import com.ajustadoati.sc.domain.enums.LubricantMovementType;
import com.ajustadoati.sc.domain.enums.LubricantOrderStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LubricantService {

  private final LubricantProductRepository productRepository;
  private final LubricantStockMovementRepository stockMovementRepository;
  private final LubricantOrderRepository orderRepository;
  private final UserService userService;
  private final SupplyService supplyService;

  @Transactional
  public LubricantProductResponse createProduct(LubricantProductRequest request) {
    String code = requireText(request.getCode(), "Código requerido");
    String name = requireText(request.getName(), "Nombre requerido");

    productRepository.findByCodeIgnoreCase(code)
      .ifPresent(existing -> {
        throw new IllegalArgumentException("Ya existe un producto con el mismo código");
      });

    var product = LubricantProduct.builder()
      .code(code)
      .name(name)
      .costPrice(requirePositive(request.getCostPrice(), "Costo inválido"))
      .salePrice(requirePositive(request.getSalePrice(), "Precio de venta inválido"))
      .stock(request.getInitialStock() == null ? 0 : request.getInitialStock())
      .active(Boolean.TRUE)
      .build();

    product = productRepository.save(product);

    if (product.getStock() > 0) {
      stockMovementRepository.save(LubricantStockMovement.builder()
        .product(product)
        .movementDate(LocalDate.now())
        .movementType(LubricantMovementType.IN)
        .quantity(product.getStock())
        .previousStock(0)
        .newStock(product.getStock())
        .notes("Stock inicial")
        .build());
    }

    return toProductResponse(product);
  }

  public List<LubricantProductResponse> getProducts() {
    return productRepository.findAll().stream()
      .sorted(Comparator.comparing(LubricantProduct::getActive).reversed()
        .thenComparing(LubricantProduct::getName, String.CASE_INSENSITIVE_ORDER))
      .map(this::toProductResponse)
      .toList();
  }

  @Transactional
  public LubricantProductResponse updateProduct(Integer productId, LubricantProductRequest request) {
    var product = getProduct(productId);

    String code = requireText(request.getCode(), "Código requerido");
    String name = requireText(request.getName(), "Nombre requerido");

    productRepository.findByCodeIgnoreCase(code)
      .filter(existing -> !existing.getLubricantProductId().equals(productId))
      .ifPresent(existing -> {
        throw new IllegalArgumentException("Ya existe un producto con el mismo código");
      });

    product.setCode(code);
    product.setName(name);
    product.setCostPrice(requirePositive(request.getCostPrice(), "Costo inválido"));
    product.setSalePrice(requirePositive(request.getSalePrice(), "Precio de venta inválido"));

    return toProductResponse(productRepository.save(product));
  }

  @Transactional
  public void deleteProduct(Integer productId) {
    var product = getProduct(productId);
    product.setActive(Boolean.FALSE);
    productRepository.save(product);
  }

  @Transactional
  public LubricantProductResponse restoreProduct(Integer productId) {
    var product = getProduct(productId);
    product.setActive(Boolean.TRUE);
    return toProductResponse(productRepository.save(product));
  }

  public List<LubricantProductResponse> getActiveProducts() {
    return productRepository.findAll().stream()
      .filter(product -> Boolean.TRUE.equals(product.getActive()))
      .sorted(Comparator.comparing(LubricantProduct::getName, String.CASE_INSENSITIVE_ORDER))
      .map(this::toProductResponse)
      .toList();
  }

  @Transactional
  public LubricantProductResponse addStock(Integer productId, LubricantStockEntryRequest request) {
    var product = getProduct(productId);
    int quantity = request.getQuantity() == null ? 0 : request.getQuantity();
    if (quantity <= 0) {
      throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
    }

    int previousStock = product.getStock();
    int newStock = previousStock + quantity;
    product.setStock(newStock);
    productRepository.save(product);

    stockMovementRepository.save(LubricantStockMovement.builder()
      .product(product)
      .movementDate(request.getMovementDate() != null ? request.getMovementDate() : LocalDate.now())
      .movementType(LubricantMovementType.IN)
      .quantity(quantity)
      .previousStock(previousStock)
      .newStock(newStock)
      .notes(request.getNotes())
      .build());

    return toProductResponse(product);
  }

  public List<LubricantStockMovementResponse> getMovements(Integer productId) {
    return stockMovementRepository.findByProduct_LubricantProductIdOrderByMovementDateDesc(productId).stream()
      .map(movement -> LubricantStockMovementResponse.builder()
        .id(movement.getLubricantStockMovementId())
        .movementDate(movement.getMovementDate())
        .movementType(movement.getMovementType())
        .quantity(movement.getQuantity())
        .previousStock(movement.getPreviousStock())
        .newStock(movement.getNewStock())
        .notes(movement.getNotes())
        .build())
      .toList();
  }

  @Transactional
  public LubricantOrderResponse createOrder(LubricantOrderRequest request) {
    if (request.getItems() == null || request.getItems().isEmpty()) {
      throw new IllegalArgumentException("El pedido debe tener al menos un producto");
    }

    var user = userService.getUserById(request.getUserId());
    LocalDate orderDate = request.getOrderDate() != null ? request.getOrderDate() : LocalDate.now();

    var order = LubricantOrder.builder()
      .user(user)
      .orderDate(orderDate)
      .weeklyInstallment(BigDecimal.ZERO)
      .installmentCount(request.getInstallmentCount() != null && request.getInstallmentCount() > 0 ? request.getInstallmentCount() : 4)
      .totalAmount(BigDecimal.ZERO)
      .balance(BigDecimal.ZERO)
      .status(LubricantOrderStatus.PENDING)
      .createdAt(LocalDateTime.now())
      .build();

    final LubricantOrder persistedOrder = orderRepository.save(order);

    List<LubricantOrderItem> items = new ArrayList<>(request.getItems().stream()
      .map(itemRequest -> buildOrderItem(persistedOrder, itemRequest))
      .toList());

    BigDecimal totalAmount = items.stream()
      .map(LubricantOrderItem::getLineTotal)
      .reduce(BigDecimal.ZERO, BigDecimal::add);

    Integer installmentCount = resolveInstallmentCount(request, totalAmount);
    BigDecimal weeklyInstallment = resolveWeeklyInstallment(request, totalAmount, installmentCount);

    persistedOrder.setItems(items);
    persistedOrder.setTotalAmount(totalAmount);
    persistedOrder.setBalance(totalAmount);
    persistedOrder.setInstallmentCount(installmentCount);
    persistedOrder.setWeeklyInstallment(weeklyInstallment);

    for (LubricantOrderItem item : items) {
      moveStockOut(item.getProduct(), item.getQuantity(), persistedOrder.getOrderDate(),
        "Pedido lubricantes #" + persistedOrder.getLubricantOrderId());
    }

    SupplyRequest supplyRequest = new SupplyRequest();
    supplyRequest.setUserId(user.getUserId());
    supplyRequest.setSupplyName("LUBRICANTES PEDIDO #" + persistedOrder.getLubricantOrderId());
    supplyRequest.setSupplyAmount(totalAmount);
    supplyRequest.setSupplyDate(persistedOrder.getOrderDate());
    SupplyResponse supply = supplyService.createSupply(supplyRequest);

    persistedOrder.setSupplyId(supply.getSupplyId());
    return toOrderResponse(orderRepository.save(persistedOrder));
  }

  public List<LubricantOrderResponse> getOrdersByUser(Integer userId) {
    return orderRepository.findByUser_UserIdOrderByOrderDateDesc(userId).stream()
      .map(this::toOrderResponse)
      .toList();
  }

  public List<LubricantWeeklyCollectionResponse> getWeeklyCollection(LocalDate date) {
    LocalDate reportDate = date != null ? date : LocalDate.now();
    return orderRepository.findByOrderDateLessThanEqualAndStatusInOrderByOrderDateAsc(
        reportDate,
        List.of(LubricantOrderStatus.PENDING, LubricantOrderStatus.PARTIALLY_PAID))
      .stream()
      .map(order -> LubricantWeeklyCollectionResponse.builder()
        .orderId(order.getLubricantOrderId())
        .userId(order.getUser().getUserId())
        .userName(order.getUser().getFirstName() + " " + order.getUser().getLastName())
        .numberId(order.getUser().getNumberId())
        .orderDate(order.getOrderDate())
        .weeklyInstallment(order.getWeeklyInstallment())
        .balance(order.getBalance())
        .suggestedPayment(order.getWeeklyInstallment().min(order.getBalance()))
        .build())
      .toList();
  }

  private LubricantOrderItem buildOrderItem(LubricantOrder order, LubricantOrderItemRequest request) {
    if (request.getQuantity() == null || request.getQuantity() <= 0) {
      throw new IllegalArgumentException("Cantidad inválida en el pedido");
    }

    var product = getProduct(request.getProductId());
    if (!Boolean.TRUE.equals(product.getActive())) {
      throw new IllegalArgumentException("El producto no está activo para nuevos pedidos");
    }

    BigDecimal unitPrice = request.getUnitPrice() != null && request.getUnitPrice().compareTo(BigDecimal.ZERO) > 0
      ? request.getUnitPrice()
      : product.getSalePrice();

    return LubricantOrderItem.builder()
      .order(order)
      .product(product)
      .productCode(product.getCode())
      .productName(product.getName())
      .quantity(request.getQuantity())
      .unitPrice(unitPrice)
      .lineTotal(unitPrice.multiply(BigDecimal.valueOf(request.getQuantity())))
      .build();
  }

  private void moveStockOut(LubricantProduct product, Integer quantity, LocalDate date, String notes) {
    if (product.getStock() < quantity) {
      throw new IllegalArgumentException("Stock insuficiente para el producto: " + product.getName());
    }

    int previousStock = product.getStock();
    int newStock = previousStock - quantity;
    product.setStock(newStock);
    productRepository.save(product);

    stockMovementRepository.save(LubricantStockMovement.builder()
      .product(product)
      .movementDate(date)
      .movementType(LubricantMovementType.OUT)
      .quantity(quantity)
      .previousStock(previousStock)
      .newStock(newStock)
      .notes(notes)
      .build());
  }

  private LubricantProduct getProduct(Integer productId) {
    return productRepository.findById(productId)
      .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
  }

  private LubricantProductResponse toProductResponse(LubricantProduct product) {
    return LubricantProductResponse.builder()
      .id(product.getLubricantProductId())
      .code(product.getCode())
      .name(product.getName())
      .costPrice(product.getCostPrice())
      .salePrice(product.getSalePrice())
      .stock(product.getStock())
      .active(product.getActive())
      .build();
  }

  private LubricantOrderResponse toOrderResponse(LubricantOrder order) {
    return LubricantOrderResponse.builder()
      .orderId(order.getLubricantOrderId())
      .userId(order.getUser().getUserId())
      .userName(order.getUser().getFirstName() + " " + order.getUser().getLastName())
      .numberId(order.getUser().getNumberId())
      .orderDate(order.getOrderDate())
      .totalAmount(order.getTotalAmount())
      .balance(order.getBalance())
      .weeklyInstallment(order.getWeeklyInstallment())
      .installmentCount(order.getInstallmentCount())
      .status(order.getStatus())
      .supplyId(order.getSupplyId())
      .items(order.getItems() == null ? List.of() : order.getItems().stream()
        .map(item -> LubricantOrderItemResponse.builder()
          .productId(item.getProduct().getLubricantProductId())
          .productCode(item.getProductCode())
          .productName(item.getProductName())
          .quantity(item.getQuantity())
          .unitPrice(item.getUnitPrice())
          .lineTotal(item.getLineTotal())
          .build())
        .toList())
      .build();
  }

  private String requireText(String value, String message) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(message);
    }
    return value.trim();
  }

  private BigDecimal requirePositive(BigDecimal value, String message) {
    if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException(message);
    }
    return value;
  }

  private Integer resolveInstallmentCount(LubricantOrderRequest request, BigDecimal totalAmount) {
    if (request.getInstallmentCount() != null && request.getInstallmentCount() > 0) {
      return request.getInstallmentCount();
    }

    if (request.getWeeklyInstallment() != null && request.getWeeklyInstallment().compareTo(BigDecimal.ZERO) > 0) {
      return totalAmount.divide(request.getWeeklyInstallment(), 0, RoundingMode.UP).intValue();
    }

    return 4;
  }

  private BigDecimal resolveWeeklyInstallment(LubricantOrderRequest request, BigDecimal totalAmount, Integer installmentCount) {
    if (request.getInstallmentCount() != null && request.getInstallmentCount() > 0) {
      return totalAmount.divide(BigDecimal.valueOf(installmentCount), 2, RoundingMode.HALF_UP);
    }

    if (request.getWeeklyInstallment() != null && request.getWeeklyInstallment().compareTo(BigDecimal.ZERO) > 0) {
      return request.getWeeklyInstallment().setScale(2, RoundingMode.HALF_UP);
    }

    return totalAmount.divide(BigDecimal.valueOf(installmentCount), 2, RoundingMode.HALF_UP);
  }
}
