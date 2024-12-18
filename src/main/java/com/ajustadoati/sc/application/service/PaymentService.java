package com.ajustadoati.sc.application.service;


import static com.ajustadoati.sc.adapter.rest.dto.request.enums.PaymentTypeEnum.ADMINISTRATIVE;
import static com.ajustadoati.sc.adapter.rest.dto.request.enums.PaymentTypeEnum.CHILDRENS_SAVING;
import static com.ajustadoati.sc.adapter.rest.dto.request.enums.PaymentTypeEnum.PARTNER_SAVING;
import static com.ajustadoati.sc.adapter.rest.dto.request.enums.PaymentTypeEnum.SAVING;
import static com.ajustadoati.sc.adapter.rest.dto.request.enums.PaymentTypeEnum.SHARED_CONTRIBUTION;

import com.ajustadoati.sc.adapter.rest.dto.request.ContributionPaymentRequest;
import com.ajustadoati.sc.adapter.rest.dto.request.PaymentDetail;
import com.ajustadoati.sc.adapter.rest.dto.request.PaymentRequest;
import com.ajustadoati.sc.adapter.rest.dto.request.SavingRequest;
import com.ajustadoati.sc.adapter.rest.dto.request.enums.PaymentTypeEnum;
import com.ajustadoati.sc.adapter.rest.dto.response.PaymentResponse;
import com.ajustadoati.sc.adapter.rest.dto.response.PaymentResponse.PaymentStatus;
import com.ajustadoati.sc.adapter.rest.repository.ContributionTypeRepository;
import com.ajustadoati.sc.adapter.rest.repository.SavingRepository;
import com.ajustadoati.sc.adapter.rest.repository.UserRepository;
import com.ajustadoati.sc.application.service.dto.Pago;
import com.ajustadoati.sc.application.service.dto.enums.TipoPagoEnum;
import com.ajustadoati.sc.application.service.file.FileService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {


  private final ContributionTypeRepository contributionTypeRepository;
  private final SavingRepository savingRepository;
  private final UserRepository userRepository;
  private final SavingService savingService;
  private final ContributionPaymentService contributionPaymentService;
  private final FileService fileService;

  @Transactional
  public PaymentResponse processPayments(PaymentRequest request) {
    var user = userRepository.findById(request.getUserId())
      .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

    List<PaymentStatus> paymentStatuses = new ArrayList<>();
    BigDecimal totalPaid = BigDecimal.ZERO;

    List<SavingRequest> savingRequests = new ArrayList<>();
    List<ContributionPaymentRequest> contributionPaymentRequests = new ArrayList<>();

    List<Pago> pagos = new ArrayList<>();

    for (PaymentDetail paymentDetail : request.getPayments()) {
      var status = new PaymentResponse.PaymentStatus();
      status.setPaymentType(paymentDetail.getPaymentType());
      status.setReferenceId(paymentDetail.getReferenceId());
      status.setAmount(paymentDetail.getAmount());

      try {
        switch (paymentDetail.getPaymentType()) {
          case ADMINISTRATIVE, SHARED_CONTRIBUTION:
            contributionPaymentRequests.add(
              getContributionPaymentRequest(user.getUserId(), paymentDetail, request.getDate()));
            if (paymentDetail.getPaymentType()
              .equals(ADMINISTRATIVE)) {
              pagos.add(
                Pago.builder().tipoPago(TipoPagoEnum.ADMINISTRATIVO)
                  .monto(paymentDetail.getAmount().doubleValue())
                  .fecha(request.getDate().toString()).cedula(user.getNumberId()).build());
            } else {
              pagos.add(
                Pago.builder().tipoPago(TipoPagoEnum.COMPARTIR)
                  .monto(paymentDetail.getAmount().doubleValue())
                  .fecha(request.getDate().toString()).cedula(user.getNumberId()).build());
            }

            break;
          case SAVING, PARTNER_SAVING, CHILDRENS_SAVING:
            savingRequests.add(
              getSavingRequest(user.getUserId(), paymentDetail, request.getDate()));
            pagos.add(
              Pago.builder().tipoPago(TipoPagoEnum.AHORRO)
                .monto(paymentDetail.getAmount().doubleValue())
                .fecha(request.getDate().toString()).cedula(user.getNumberId()).build());

            break;

          case SUPPLIES:
            processSuppliesPayment(user.getUserId(), paymentDetail, request.getDate());
            break;

          case LOAN_INTEREST_PAYMENT:
            pagos.add(
              Pago.builder().tipoPago(TipoPagoEnum.ABONO_INTERES)
                .monto(paymentDetail.getAmount().doubleValue())
                .fecha(request.getDate().toString()).cedula(user.getNumberId()).build());
            processLoanInterestPayment(user.getUserId(), paymentDetail, request.getDate());
            break;

          case LOAN_PAYMENT:
            pagos.add(
              Pago.builder().tipoPago(TipoPagoEnum.ABONO_CAPITAL)
                .monto(paymentDetail.getAmount().doubleValue())
                .fecha(request.getDate().toString()).cedula(user.getNumberId()).build());
            processLoanRepayment(user.getUserId(), paymentDetail, request.getDate());
            break;

          case OTHER_PAYMENTS:

            processLoanRepayment(user.getUserId(), paymentDetail, request.getDate());
            break;

          default:
            throw new IllegalArgumentException("Invalid payment type");
        }

        status.setStatus("SUCCESS");
        status.setMessage("Payment processed successfully");
        totalPaid = totalPaid.add(paymentDetail.getAmount());
      } catch (Exception e) {
        status.setStatus("FAILURE");
        status.setMessage(e.getMessage());
      }

      paymentStatuses.add(status);
    }

    if (!savingRequests.isEmpty()) {
      savingService.addSavingSet(request.getUserId(), savingRequests);
    }
    if (!contributionPaymentRequests.isEmpty()) {
      contributionPaymentService.saveList(contributionPaymentRequests);
    }

    try {
      fileService.registrarMultiplesPagos(pagos, user);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    PaymentResponse response = new PaymentResponse();
    response.setUserId(user.getUserId());
    response.setTotalPaid(totalPaid);
    response.setPaymentStatuses(paymentStatuses);

    return response;
  }

  private void processLoanRepayment(Integer userId, PaymentDetail paymentDetail, LocalDate date) {
  }

  private void processLoanInterestPayment(Integer userId, PaymentDetail paymentDetail,
    LocalDate date) {
  }

  private void processSuppliesPayment(Integer userId, PaymentDetail paymentDetail, LocalDate date) {
  }

  private void processOthersPayment(Integer userId, PaymentDetail paymentDetail, LocalDate date) {
    log.info("Not Payment type for processing {}", paymentDetail.getPaymentType());
  }

  private ContributionPaymentRequest getContributionPaymentRequest(Integer userId,
    PaymentDetail paymentDetail, LocalDate date) {

    return ContributionPaymentRequest.builder()
      .contributionId(paymentDetail.getReferenceId())
      .paymentDate(date)
      .amount(paymentDetail.getAmount())
      .userId(Objects.nonNull(paymentDetail.getUserId()) ? paymentDetail.getUserId() : userId)
      .build();

  }

  private SavingRequest getSavingRequest(Integer userId, PaymentDetail paymentDetail,
    LocalDate date) {
    var saving = new SavingRequest();
    if (Objects.nonNull(paymentDetail.getUserId())) {
      saving.setAssociateId(paymentDetail.getUserId());
    }

    saving.setAmount(paymentDetail.getAmount());
    saving.setSavingDate(date);

    return saving;
  }
}