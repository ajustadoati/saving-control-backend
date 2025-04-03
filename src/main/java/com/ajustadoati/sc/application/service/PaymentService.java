package com.ajustadoati.sc.application.service;

import com.ajustadoati.sc.adapter.rest.dto.request.ContributionPaymentRequest;
import com.ajustadoati.sc.adapter.rest.dto.request.LoanPaymentRequest;
import com.ajustadoati.sc.adapter.rest.dto.request.PaymentDetail;
import com.ajustadoati.sc.adapter.rest.dto.request.PaymentRequest;
import com.ajustadoati.sc.adapter.rest.dto.request.SavingRequest;
import com.ajustadoati.sc.adapter.rest.dto.request.SupplyPaymentRequest;
import com.ajustadoati.sc.adapter.rest.dto.request.enums.PaymentTypeEnum;
import com.ajustadoati.sc.adapter.rest.dto.response.AssociateDto;
import com.ajustadoati.sc.adapter.rest.dto.response.DailyResponse;
import com.ajustadoati.sc.adapter.rest.dto.response.PaymentResponse;
import com.ajustadoati.sc.adapter.rest.dto.response.PaymentResponse.PaymentStatus;
import com.ajustadoati.sc.adapter.rest.repository.ContributionTypeRepository;
import com.ajustadoati.sc.adapter.rest.repository.PagoRepository;
import com.ajustadoati.sc.adapter.rest.repository.SavingRepository;
import com.ajustadoati.sc.adapter.rest.repository.UserRepository;
import com.ajustadoati.sc.application.mapper.PagoMapper;
import com.ajustadoati.sc.application.service.dto.PagoDto;
import com.ajustadoati.sc.application.service.dto.enums.TipoPagoEnum;
import com.ajustadoati.sc.application.service.enums.FundsType;
import com.ajustadoati.sc.domain.Loan;
import com.ajustadoati.sc.domain.OtherPayment;
import com.ajustadoati.sc.domain.User;
import com.ajustadoati.sc.utils.Util;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

  private final ContributionTypeRepository contributionTypeRepository;
  private final SavingRepository savingRepository;
  private final UserRepository userRepository;
  private final SavingService savingService;
  private final ContributionPaymentService contributionPaymentService;
  private final PagoMapper pagoMapper;
  private final PagoRepository pagoRepository;
  private final LoanService loanService;
  private final FundsService fundsService;
  private final AssociateService associateService;
  private final SupplyService supplyService;
  private final OtherPaymentService otherPaymentService;

  @Transactional
  public PaymentResponse processPayments(PaymentRequest request) {
    var user = getUser(request.getUserId());
    if (CollectionUtils.isNotEmpty(pagoRepository.findByFechaAndCedula(request.getDate(), user.getNumberId()))) {
      throw new IllegalArgumentException("Payments already registered for user");
    }

    List<PagoDto> pagoDtos = new ArrayList<>();
    List<PaymentStatus> paymentStatuses = new ArrayList<>();
    List<SavingRequest> savingRequests = new ArrayList<>();
    List<ContributionPaymentRequest> contributionPaymentRequests = new ArrayList<>();
    BigDecimal totalPaid = BigDecimal.ZERO;

    for (PaymentDetail paymentDetail : request.getPayments()) {
      PaymentStatus status = processPaymentDetail(user, request.getDate(), paymentDetail,
        pagoDtos, savingRequests, contributionPaymentRequests);
      paymentStatuses.add(status);
      if ("SUCCESS".equals(status.getStatus())) {
        totalPaid = totalPaid.add(paymentDetail.getAmount());
      }
    }

    persistPayments(request, user, pagoDtos, savingRequests, contributionPaymentRequests);

    return buildPaymentResponse(user, totalPaid, paymentStatuses);
  }

  private User getUser(Integer userId) {
    return userRepository.findById(userId)
      .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));
  }

  private PaymentStatus processPaymentDetail(User user, LocalDate date, PaymentDetail paymentDetail,
    List<PagoDto> pagoDtos, List<SavingRequest> savingRequests,
    List<ContributionPaymentRequest> contributionPaymentRequests) {
    PaymentStatus status = new PaymentResponse.PaymentStatus();
    status.setPaymentType(paymentDetail.getPaymentType());
    status.setReferenceId(paymentDetail.getReferenceId());
    status.setAmount(paymentDetail.getAmount());
    try {
      processByType(user, date, paymentDetail, pagoDtos, savingRequests,
        contributionPaymentRequests);
      status.setStatus("SUCCESS");
      status.setMessage("Payment processed successfully");
    } catch (Exception e) {
      status.setStatus("FAILURE");
      status.setMessage(e.getMessage());
    }
    return status;
  }

  private void processByType(User user, LocalDate date, PaymentDetail paymentDetail,
    List<PagoDto> pagoDtos, List<SavingRequest> savingRequests,
    List<ContributionPaymentRequest> contributionPaymentRequests) {
    switch (paymentDetail.getPaymentType()) {
      case ADMINISTRATIVE, SHARED_CONTRIBUTION ->
        processContribution(user, date, paymentDetail, pagoDtos, contributionPaymentRequests);
      case SAVING, PARTNER_SAVING, CHILDRENS_SAVING ->
        processSaving(user, date, paymentDetail, pagoDtos, savingRequests);
      case SUPPLIES -> processSupplies(user, date, paymentDetail, pagoDtos);
      case LOAN_INTEREST_PAYMENT -> processLoanInterest(user, date, paymentDetail, pagoDtos);
      case LOAN_PAYMENT -> processLoan(user, date, paymentDetail, pagoDtos);
      case WHEELS, OTHER_PAYMENTS -> processOthersPayment(user, paymentDetail, date, pagoDtos);
      default -> throw new IllegalArgumentException("Invalid payment type");
    }
  }

  private void processContribution(User user, LocalDate date, PaymentDetail paymentDetail,
    List<PagoDto> pagoDtos,
    List<ContributionPaymentRequest> contributionPaymentRequests) {
    contributionPaymentRequests.add(
      getContributionPaymentRequest(user.getUserId(), paymentDetail, date));
    if (paymentDetail.getPaymentType() == PaymentTypeEnum.ADMINISTRATIVE) {
      paymentDetail.setReferenceId(5);
    } else {
      paymentDetail.setReferenceId(6);
    }
    TipoPagoEnum tipoPago =
      paymentDetail.getPaymentType() == PaymentTypeEnum.ADMINISTRATIVE ? TipoPagoEnum.ADMINISTRATIVO
        : TipoPagoEnum.COMPARTIR;
    pagoDtos.add(buildPagoDto(user, date, paymentDetail, tipoPago));
  }

  private void processSaving(User user, LocalDate date, PaymentDetail paymentDetail,
    List<PagoDto> pagoDtos,
    List<SavingRequest> savingRequests) {
    var associates = associateService.getAssociatesByUserId(user.getUserId());

    if (paymentDetail.getPaymentType() == PaymentTypeEnum.PARTNER_SAVING) {
      log.info("Creating saving request for partner");
      var associateId = associates.stream()
        .filter(associateDto -> Util.PARTNERS.contains(associateDto.getRelationship()))
        .map(AssociateDto::getId).findFirst();
      paymentDetail.setUserId(associateId.get());
    } else if (paymentDetail.getPaymentType() == PaymentTypeEnum.CHILDRENS_SAVING) {
      log.info("Creating saving request for children");
      var associateId = associates.stream()
        .filter(associateDto -> Util.CHILDREN.contains(associateDto.getRelationship()))
        .map(AssociateDto::getId).findFirst();
      paymentDetail.setUserId(associateId.get());
    }
    savingRequests.add(getSavingRequest(user.getUserId(), paymentDetail, date));
    pagoDtos.add(buildPagoDto(user, date, paymentDetail, TipoPagoEnum.AHORRO));
  }

  private void processSupplies(User user, LocalDate date, PaymentDetail paymentDetail,
    List<PagoDto> pagoDtos) {
    pagoDtos.add(buildPagoDto(user, date, paymentDetail, TipoPagoEnum.SUMINISTROS));
    processSuppliesPayment(user.getUserId(), paymentDetail, date);
  }

  private void processLoanInterest(User user, LocalDate date, PaymentDetail paymentDetail,
    List<PagoDto> pagoDtos) {
    pagoDtos.add(buildPagoDto(user, date, paymentDetail, TipoPagoEnum.ABONO_INTERES));
    processLoanInterestPayment(user.getUserId(), paymentDetail, date);
  }

  private void processLoan(User user, LocalDate date, PaymentDetail paymentDetail,
    List<PagoDto> pagoDtos) {
    TipoPagoEnum tipoPago = switch (paymentDetail.getPaymentType()) {
      case LOAN_PAYMENT -> TipoPagoEnum.ABONO_CAPITAL;
      case WHEELS -> TipoPagoEnum.CAUCHOS;
      case OTHER_PAYMENTS -> TipoPagoEnum.OTROS;
      default ->
        throw new IllegalStateException("Unexpected value: " + paymentDetail.getPaymentType());
    };
    pagoDtos.add(buildPagoDto(user, date, paymentDetail, tipoPago));
    processLoanPayment(user.getUserId(), paymentDetail, date);
  }

  private PagoDto buildPagoDto(User user, LocalDate date, PaymentDetail paymentDetail,
    TipoPagoEnum tipoPago) {
    return PagoDto.builder()
      .tipoPago(tipoPago)
      .monto(paymentDetail.getAmount().doubleValue())
      .fecha(date.toString())
      .cedula(user.getNumberId())
      .build();
  }

  private void persistPayments(PaymentRequest request, User user, List<PagoDto> pagoDtos,
    List<SavingRequest> savingRequests,
    List<ContributionPaymentRequest> contributionPaymentRequests) {
    if (!savingRequests.isEmpty()) {
      savingService.addSavingSet(request.getUserId(), savingRequests);
    }
    if (!contributionPaymentRequests.isEmpty()) {
      contributionPaymentService.saveList(contributionPaymentRequests);
    }
    if (pagoRepository.findByFechaAndCedula(request.getDate(), user.getNumberId()).isEmpty()) {
      pagoRepository.saveAll(pagoDtos.stream().map(pagoMapper::toEntity).toList());
      pagoDtos.stream()
        .filter(pagoDto -> Set.of(TipoPagoEnum.AHORRO, TipoPagoEnum.ABONO_CAPITAL,
          TipoPagoEnum.ABONO_INTERES).contains(pagoDto.getTipoPago()))
        .forEach(
          pagoDto -> fundsService.saveFunds(BigDecimal.valueOf(pagoDto.getMonto()), FundsType.ADD));
    } else {
      throw new IllegalArgumentException("Payments already registered for user");
    }
    log.info("Pagos {}", pagoDtos);
  }

  private PaymentResponse buildPaymentResponse(User user, BigDecimal totalPaid,
    List<PaymentStatus> paymentStatuses) {
    PaymentResponse response = new PaymentResponse();
    response.setUserId(user.getUserId());
    response.setTotalPaid(totalPaid);
    response.setPaymentStatuses(paymentStatuses);
    return response;
  }


  private void processLoanPayment(Integer userId, PaymentDetail paymentDetail, LocalDate date) {
    var loans = loanService.getLoansByUser(userId);
    var loan = loans.stream().filter(loanResponse -> Objects.equals(loanResponse.getLoanId(),
      paymentDetail.getReferenceId())).findFirst();
    loan.ifPresent(loanResponse -> {
      var request = LoanPaymentRequest.builder().loanId(loanResponse.getLoanId())
        .paymentDate(date)
        .paymentTypeId(1)
        .amount(paymentDetail.getAmount())
        .build();
      loanService.registerPayment(request);
    });
  }

  private void processWheelsPayment(Integer userId, PaymentDetail paymentDetail, LocalDate date) {
  }

  private void processLoanInterestPayment(Integer userId, PaymentDetail paymentDetail,
    LocalDate date) {
    var loans = loanService.getLoansByUser(userId);
    var loan = loans.stream().filter(loanResponse -> Objects.equals(loanResponse.getLoanId(),
      paymentDetail.getReferenceId())).findFirst();
    loan.ifPresent(loanResponse -> {
      var request = LoanPaymentRequest.builder().loanId(loanResponse.getLoanId())
        .paymentDate(date)
        .paymentTypeId(2)
        .amount(paymentDetail.getAmount())
        .build();
      loanService.registerPayment(request);
    });

  }

  private void processSuppliesPayment(Integer userId, PaymentDetail paymentDetail, LocalDate date) {
    var supplies = supplyService.getSuppliesByUser(userId);
    var supply = supplies.stream().filter(supplyResponse -> Objects.equals(
      supplyResponse.getSupplyId(), paymentDetail.getReferenceId())).findFirst();
    if (supply.isPresent()) {
      var request = new SupplyPaymentRequest();
      request.setPaymentDate(date);
      request.setSupplyId(supply.get().getSupplyId());
      request.setAmount(paymentDetail.getAmount());
      supplyService.registerPayment(request);
    }
  }

  private void processOthersPayment(User user, PaymentDetail paymentDetail, LocalDate date, List<PagoDto> pagos) {

    OtherPayment other = new OtherPayment();
    other.setName(paymentDetail.getPaymentType().name());
    other.setUser(user);
    other.setAmount(paymentDetail.getAmount());
    other.setPaymentDate(date);
    otherPaymentService.save(other);
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

  public DailyResponse generateDailyReport(LocalDate fecha) {

    var pagosDelDia = pagoRepository.findByFecha(fecha).stream().map(pagoMapper::toDto).toList();
    var loansByUser = loanService.getLoanByStartDate(fecha)
      .stream()
      .collect(Collectors.groupingBy(
        loan -> loan.getUser().getNumberId(),
        Collectors.mapping(
          Loan::getLoanAmount,
          Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
        )
      ));

    var totalLoans = loansByUser.values().stream()
      .reduce(BigDecimal.ZERO, BigDecimal::add)
      .doubleValue();
    if (pagosDelDia.isEmpty()) {
      return new DailyResponse(fecha, null, null, 0.0, 0.0, 0.0,
        "No se registraron pagos en la fecha: " + fecha);
    }

    // Agrupar pagos por cédula y luego por tipo de pago
    Map<String, Map<TipoPagoEnum, Double>> pagosAgrupados = pagosDelDia.stream()
      .collect(Collectors.groupingBy(PagoDto::getCedula,
        Collectors.groupingBy(PagoDto::getTipoPago,
          Collectors.summingDouble(PagoDto::getMonto))));

    loansByUser.forEach((cedula, loanAmount) ->
      pagosAgrupados.computeIfAbsent(cedula, k -> new HashMap<>())
        .put(TipoPagoEnum.PRESTAMOS, loanAmount.doubleValue()));

    // Calcular el total por tipo de pago
    Map<TipoPagoEnum, Double> totalPorTipoPago = pagosDelDia.stream()
      .collect(Collectors.groupingBy(PagoDto::getTipoPago,
        Collectors.summingDouble(PagoDto::getMonto)));

    Double montoTotalPagos = pagosDelDia.stream()
      .mapToDouble(PagoDto::getMonto)
      .sum();
    // Calcular el monto total
    Double montoTotal = montoTotalPagos - totalLoans;

    return new DailyResponse(fecha, pagosAgrupados, totalPorTipoPago, totalLoans, montoTotalPagos,
      montoTotal, null);
  }
}