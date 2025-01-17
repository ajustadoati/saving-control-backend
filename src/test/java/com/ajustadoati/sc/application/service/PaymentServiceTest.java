package com.ajustadoati.sc.application.service;

import static org.junit.jupiter.api.Assertions.*;

import com.ajustadoati.sc.adapter.rest.dto.request.PaymentDetail;
import com.ajustadoati.sc.adapter.rest.dto.request.PaymentRequest;
import com.ajustadoati.sc.adapter.rest.dto.request.enums.PaymentTypeEnum;
import com.ajustadoati.sc.adapter.rest.dto.response.PaymentResponse;
import com.ajustadoati.sc.adapter.rest.repository.ContributionTypeRepository;
import com.ajustadoati.sc.adapter.rest.repository.PagoRepository;
import com.ajustadoati.sc.adapter.rest.repository.SavingRepository;
import com.ajustadoati.sc.adapter.rest.repository.UserRepository;
import com.ajustadoati.sc.application.mapper.PagoMapper;
import com.ajustadoati.sc.application.service.file.FileService;
import com.ajustadoati.sc.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

class PaymentServiceTest {

  @InjectMocks
  private PaymentService paymentService;

  @Mock
  private ContributionTypeRepository contributionTypeRepository;

  @Mock
  private SavingRepository savingRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private SavingService savingService;

  @Mock
  private PagoRepository pagoRepository;

  @Mock
  private PagoMapper pagoMapper;

  @Mock
  private ContributionPaymentService contributionPaymentService;

 /* @Mock
  private FileService fileService;*/

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void processPayments_validRequest_success() throws IOException {
    // Arrange
    var userId = 1;
    var paymentRequest = new PaymentRequest();
    paymentRequest.setUserId(userId);
    paymentRequest.setDate(LocalDate.now());
    paymentRequest.setPayments(List.of(
      createPaymentDetail(PaymentTypeEnum.ADMINISTRATIVE, 101,
        BigDecimal.valueOf(100.00)),
      createPaymentDetail(PaymentTypeEnum.SAVING, 102, BigDecimal.valueOf(50.00))
    ));

    var user = new User();
    user.setUserId(userId);
    user.setNumberId("12345");

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    // Act
    PaymentResponse response = paymentService.processPayments(paymentRequest);

    // Assert
    assertNotNull(response);
    assertEquals(userId, response.getUserId());
    assertEquals(BigDecimal.valueOf(150.00), response.getTotalPaid());
    assertEquals(2, response.getPaymentStatuses().size());
    assertEquals("SUCCESS", response.getPaymentStatuses().get(0).getStatus());
    assertEquals("SUCCESS", response.getPaymentStatuses().get(1).getStatus());

    verify(savingService, times(1)).addSavingSet(eq(userId), anyList());
    verify(contributionPaymentService, times(1)).saveList(anyList());
    //verify(fileService, times(1)).registrarMultiplesPagos(anyList(), any());
  }

  @Test
  void processPayments_invalidUser_throwsException() {
    // Arrange
    var userId = 1;
    var paymentRequest = new PaymentRequest();
    paymentRequest.setUserId(userId);
    paymentRequest.setDate(LocalDate.now());

    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(IllegalArgumentException.class,
      () -> paymentService.processPayments(paymentRequest));

    verifyNoInteractions(savingService);
    verifyNoInteractions(contributionPaymentService);
  }

  private PaymentDetail createPaymentDetail(PaymentTypeEnum type, Integer referenceId, BigDecimal amount) {
    var detail = new PaymentDetail();
    detail.setPaymentType(type);
    detail.setReferenceId(referenceId);
    detail.setAmount(amount);
    return detail;
  }

}
