package com.ajustadoati.sc.application.service;

import com.ajustadoati.sc.adapter.rest.dto.request.ContributionPaymentRequest;
import com.ajustadoati.sc.adapter.rest.dto.request.LoanPaymentRequest;
import com.ajustadoati.sc.adapter.rest.dto.request.PaymentDetail;
import com.ajustadoati.sc.adapter.rest.dto.request.PaymentRequest;
import com.ajustadoati.sc.adapter.rest.dto.request.PaymentReversalRequest;
import com.ajustadoati.sc.adapter.rest.dto.request.SavingRequest;
import com.ajustadoati.sc.adapter.rest.dto.request.SupplyPaymentRequest;
import com.ajustadoati.sc.adapter.rest.dto.request.enums.PaymentTypeEnum;
import com.ajustadoati.sc.adapter.rest.dto.response.*;
import com.ajustadoati.sc.adapter.rest.dto.response.PaymentResponse.PaymentStatus;
import com.ajustadoati.sc.adapter.rest.dto.response.PaymentReversalResponse.ReversedPaymentDetail;
import com.ajustadoati.sc.adapter.rest.repository.*;
import com.ajustadoati.sc.adapter.rest.repository.ContributionTypeRepository;
import com.ajustadoati.sc.adapter.rest.repository.PagoRepository;
import com.ajustadoati.sc.adapter.rest.repository.SavingRepository;
import com.ajustadoati.sc.adapter.rest.repository.UserRepository;
import com.ajustadoati.sc.application.mapper.PagoMapper;
import com.ajustadoati.sc.application.service.dto.PagoDto;
import com.ajustadoati.sc.application.service.dto.enums.TipoPagoEnum;
import com.ajustadoati.sc.application.service.enums.FundsType;
import com.ajustadoati.sc.domain.*;
import com.ajustadoati.sc.utils.Util;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.ajustadoati.sc.adapter.rest.dto.request.enums.PaymentTypeEnum.OTHER_PAYMENTS;
import static com.ajustadoati.sc.adapter.rest.dto.request.enums.PaymentTypeEnum.WHEELS;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private static final String URBAN_FORECAST_FUND_NUMBER_ID = "1426590417";
    private static final String INTERURBAN_FORECAST_FUND_NUMBER_ID = "1426590416";

    private final ContributionTypeRepository contributionTypeRepository;
    private final SavingRepository savingRepository;
    private final UserRepository userRepository;
    private final SavingService savingService;
    private final ContributionPaymentService contributionPaymentService;
    private final PagoMapper pagoMapper;
    private final PagoRepository pagoRepository;
    private final LoanService loanService;
    private final LoanRepository loanRepository;
    private final FundsService fundsService;
    private final AssociateService associateService;
    private final SupplyService supplyService;
    private final OtherPaymentService otherPaymentService;
    private final ContributionPaymentRepository contributionPaymentRepository;
    private final LoanPaymentRepository loanPaymentRepository;
    private final SupplyPaymentRepository supplyPaymentRepository;
    private final OtherPaymentRepository otherPaymentRepository;
    private final BalanceHistoryService balanceHistoryService;
    private final DistributionInterestService distributionInterestService;
    private final UserAccountSummaryService userAccountSummaryService;

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
            case LOAN_SHARING -> processLoan(user, date, paymentDetail, pagoDtos);
            case LOAN_SHARING_INTEREST -> processLoanInterest(user, date, paymentDetail, pagoDtos);
            case LOAN_INTEREST_PAYMENT_EXTERNAL -> processLoanInterest(user, date, paymentDetail, pagoDtos);
            case LOAN_PAYMENT_EXTERNAL -> processLoan(user, date, paymentDetail, pagoDtos);
            case LOAN_EXTERNAL -> processLoan(user, date, paymentDetail, pagoDtos);
            case LOAN_EXTERNAL_INTEREST -> processLoanInterest(user, date, paymentDetail, pagoDtos);
            case URBAN_FORECAST_FUND, INTERURBAN_FORECAST_FUND ->
                processForecastFundPayment(user, date, paymentDetail, pagoDtos, savingRequests);
            case WHEELS, OTHER_PAYMENTS ->
                processOthersPayment(user, paymentDetail, date, pagoDtos);
            default -> throw new IllegalArgumentException("Invalid payment type");
        }
    }

    private void processContribution(User user, LocalDate date, PaymentDetail paymentDetail,
                                     List<PagoDto> pagoDtos,
                                     List<ContributionPaymentRequest> contributionPaymentRequests) {
        contributionPaymentRequests.add(getContributionPaymentRequest(user.getUserId(), paymentDetail, date));
        TipoPagoEnum tipoPago = resolveContributionTipoPago(paymentDetail.getPaymentType());

        pagoDtos.add(buildPagoDto(user, date, paymentDetail.getAmount(), tipoPago));
    }

    private void processSaving(User user, LocalDate date, PaymentDetail paymentDetail,
                               List<PagoDto> pagoDtos,
                               List<SavingRequest> savingRequests) {
        Integer targetAssociateId = resolveSavingAssociateId(user.getUserId(), paymentDetail);
        savingRequests.add(getSavingRequest(paymentDetail.getAmount(), date, targetAssociateId));
        log.info("saving dto");
        pagoDtos.add(buildPagoDto(user, date, paymentDetail.getAmount(), TipoPagoEnum.AHORRO));
    }

    private void processSupplies(User user, LocalDate date, PaymentDetail paymentDetail,
                                 List<PagoDto> pagoDtos) {
        pagoDtos.add(buildPagoDto(user, date, paymentDetail, TipoPagoEnum.SUMINISTROS));
        processSuppliesPayment(user.getUserId(), paymentDetail, date);
    }

    private void processForecastFundPayment(User user, LocalDate date, PaymentDetail paymentDetail,
                                            List<PagoDto> pagoDtos,
                                            List<SavingRequest> savingRequests) {
        User targetUser = resolveForecastFundUser(paymentDetail.getPaymentType());
        userAccountSummaryService.findByUserId(targetUser.getUserId());

        TipoPagoEnum tipoPago = paymentDetail.getPaymentType() == PaymentTypeEnum.URBAN_FORECAST_FUND
            ? TipoPagoEnum.FONDO_PREVISION_URBANO
            : TipoPagoEnum.FONDO_PREVISION_INTERURBANO;

        pagoDtos.add(buildPagoDto(user, date, paymentDetail, tipoPago));
        savingRequests.add(SavingRequest.builder()
            .associateId(targetUser.getUserId())
            .savingDate(date)
            .amount(paymentDetail.getAmount())
            .build());
    }

    private void processLoanInterest(User user, LocalDate date, PaymentDetail paymentDetail,
                                     List<PagoDto> pagoDtos) {
        if ( paymentDetail.getPaymentType() == PaymentTypeEnum.LOAN_INTEREST_PAYMENT_EXTERNAL) {
            pagoDtos.add(buildPagoDto(user, date, paymentDetail, TipoPagoEnum.INTERESES_2));
            log.info("Processing external loan interest payment");
        } else if (paymentDetail.getPaymentType() == PaymentTypeEnum.LOAN_EXTERNAL_INTEREST) {
            pagoDtos.add(buildPagoDto(user, date, paymentDetail, TipoPagoEnum.INTERES_EXTERNO));
            log.info("Processing external interest payment");
        }else if (paymentDetail.getPaymentType() == PaymentTypeEnum.LOAN_SHARING_INTEREST) {
            pagoDtos.add(buildPagoDto(user, date, paymentDetail, TipoPagoEnum.INTERES_COMPARTIR));
            log.info("Processing sharing interest payment");
        }
        else {
            pagoDtos.add(buildPagoDto(user, date, paymentDetail, TipoPagoEnum.ABONO_INTERES));
            log.info("Processing internal loan interest payment");
        }

        processLoanInterestPayment(user.getUserId(), paymentDetail, date);
    }

    private void processLoan(User user, LocalDate date, PaymentDetail paymentDetail,
                             List<PagoDto> pagoDtos) {
        TipoPagoEnum tipoPago = switch (paymentDetail.getPaymentType()) {
            case LOAN_PAYMENT -> TipoPagoEnum.ABONO_CAPITAL;
            case WHEELS -> TipoPagoEnum.CAUCHOS;
            case OTHER_PAYMENTS -> TipoPagoEnum.OTROS;
            case LOAN_PAYMENT_EXTERNAL -> TipoPagoEnum.PRESTAMOS_2;
            case LOAN_EXTERNAL -> TipoPagoEnum.PRESTAMO_EXTERNO;
            case LOAN_SHARING -> TipoPagoEnum.PRESTAMO_COMPARTIR;
            default -> throw new IllegalStateException("Unexpected value: " + paymentDetail.getPaymentType());
        };
        pagoDtos.add(buildPagoDto(user, date, paymentDetail, tipoPago));
        processLoanPayment(user.getUserId(), paymentDetail, date);
    }

    private PagoDto buildPagoDto(User user, LocalDate date, PaymentDetail paymentDetail,
                                 TipoPagoEnum tipoPago) {
        return buildPagoDto(user, date, paymentDetail.getAmount(), tipoPago);
    }

    private PagoDto buildPagoDto(User user, LocalDate date, BigDecimal amount,
                                 TipoPagoEnum tipoPago) {
        return PagoDto.builder()
            .tipoPago(tipoPago)
            .monto(amount.doubleValue())
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
        if (pagoRepository.findByFechaAndCedula(request.getDate(), user.getNumberId())
            .isEmpty()) {
            pagoRepository.saveAll(pagoDtos.stream()
                .map(pagoMapper::toEntity)
                .toList());
            pagoDtos.stream()
                .filter(pagoDto -> Set.of(TipoPagoEnum.AHORRO, TipoPagoEnum.ABONO_CAPITAL,
                        TipoPagoEnum.ABONO_INTERES, TipoPagoEnum.PRESTAMOS_2
                        )
                    .contains(pagoDto.getTipoPago()))
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
        if (paymentDetail.getReferenceId() != null) {
            applyLoanPaymentById(paymentDetail.getReferenceId(), date, paymentDetail.getAmount(), 1);
            return;
        }
        distributePaymentByLoanType(userId, date, paymentDetail.getPaymentType(), paymentDetail.getAmount(), 1);
    }

    private void processWheelsPayment(Integer userId, PaymentDetail paymentDetail, LocalDate date) {
    }

    private void processLoanInterestPayment(Integer userId, PaymentDetail paymentDetail,
                                            LocalDate date) {
        if (paymentDetail.getReferenceId() != null) {
            applyLoanPaymentById(paymentDetail.getReferenceId(), date, paymentDetail.getAmount(), 2);
            return;
        }
        distributePaymentByLoanType(userId, date, paymentDetail.getPaymentType(), paymentDetail.getAmount(), 2);

    }

    private void applyLoanPaymentById(Integer loanId, LocalDate date, BigDecimal amount, int paymentTypeId) {
        var request = LoanPaymentRequest.builder()
            .loanId(loanId)
            .paymentDate(date)
            .paymentTypeId(paymentTypeId)
            .amount(amount)
            .build();
        loanService.registerPayment(request);
    }

    private void distributePaymentByLoanType(Integer userId, LocalDate date, PaymentTypeEnum paymentType,
                                              BigDecimal amount, int paymentTypeId) {
        String loanTypeName = mapLoanTypeName(paymentType);
        if (loanTypeName == null) {
            throw new IllegalArgumentException("Loan type not supported for payment type: " + paymentType);
        }

        var loans = loanService.getLoansByUser(userId).stream()
            .filter(loan -> loan.getLoanTypeName() != null
                && loan.getLoanTypeName().trim().equalsIgnoreCase(loanTypeName))
            .filter(loan -> loan.getLoanBalance() != null && loan.getLoanBalance().compareTo(BigDecimal.ZERO) > 0)
            .toList();

        if (loans.isEmpty()) {
            throw new IllegalArgumentException("No active loans for type: " + loanTypeName);
        }

        BigDecimal remaining = amount;
        for (var loan : loans) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            BigDecimal balance = loan.getLoanBalance();
            BigDecimal payment = remaining.min(balance);
            applyLoanPaymentById(loan.getLoanId(), date, payment, paymentTypeId);
            remaining = remaining.subtract(payment);
        }
    }

    private String mapLoanTypeName(PaymentTypeEnum paymentType) {
        return switch (paymentType) {
            case LOAN_PAYMENT, LOAN_INTEREST_PAYMENT -> "Préstamos1";
            case LOAN_PAYMENT_EXTERNAL, LOAN_INTEREST_PAYMENT_EXTERNAL -> "Préstamos2";
            case LOAN_EXTERNAL, LOAN_EXTERNAL_INTEREST -> "Externos";
            case LOAN_SHARING, LOAN_SHARING_INTEREST -> "Compartir";
            default -> null;
        };
    }

    private void processSuppliesPayment(Integer userId, PaymentDetail paymentDetail, LocalDate date) {
        var supplies = supplyService.getSuppliesByUser(userId);
        var supply = paymentDetail.getReferenceId() != null
            ? supplies.stream()
                .filter(supplyResponse -> Objects.equals(
                    supplyResponse.getSupplyId(), paymentDetail.getReferenceId()))
                .findFirst()
            : supplies.stream()
                .filter(supplyResponse -> supplyResponse.getSupplyBalance() != null
                    && supplyResponse.getSupplyBalance().compareTo(BigDecimal.ZERO) > 0)
                .findFirst();
        if (supply.isEmpty()) {
            throw new IllegalArgumentException("Active supply not found for payment");
        }

        var request = new SupplyPaymentRequest();
        request.setPaymentDate(date);
        request.setSupplyId(supply.get()
            .getSupplyId());
        request.setAmount(paymentDetail.getAmount());
        supplyService.registerPayment(request);
    }

    private void processOthersPayment(User user, PaymentDetail paymentDetail, LocalDate date, List<PagoDto> pagos) {
        TipoPagoEnum tipoPago = switch (paymentDetail.getPaymentType()) {
            case WHEELS -> TipoPagoEnum.CAUCHOS;
            case OTHER_PAYMENTS -> TipoPagoEnum.OTROS;
            default -> throw new IllegalStateException("Unexpected other payment type: " + paymentDetail.getPaymentType());
        };

        pagos.add(buildPagoDto(user, date, paymentDetail, tipoPago));

        OtherPayment other = new OtherPayment();
        String reason = paymentDetail.getReason();
        if (reason == null || reason.trim().isEmpty()) {
            reason = paymentDetail.getPaymentType() == OTHER_PAYMENTS
                ? paymentDetail.getPaymentType().name()
                : paymentDetail.getPaymentType().getDescription();
        }
        other.setName(reason);
        other.setUser(user);
        other.setAmount(paymentDetail.getAmount());
        other.setPaymentDate(date);
        otherPaymentService.save(other);
    }

    private User resolveForecastFundUser(PaymentTypeEnum paymentType) {
        String numberId = switch (paymentType) {
            case URBAN_FORECAST_FUND -> URBAN_FORECAST_FUND_NUMBER_ID;
            case INTERURBAN_FORECAST_FUND -> INTERURBAN_FORECAST_FUND_NUMBER_ID;
            default -> throw new IllegalArgumentException("Payment type is not a forecast fund: " + paymentType);
        };

        return userRepository.findByNumberId(numberId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Forecast fund account not found for numberId: " + numberId));
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

    private SavingRequest getSavingRequest(BigDecimal amount, LocalDate date, Integer associateId) {
        var saving = new SavingRequest();
        if (Objects.nonNull(associateId)) {
            saving.setAssociateId(associateId);
        }

        saving.setAmount(amount);
        saving.setSavingDate(date);

        return saving;
    }

    private TipoPagoEnum resolveContributionTipoPago(PaymentTypeEnum paymentType) {
        return switch (paymentType) {
            case ADMINISTRATIVE -> TipoPagoEnum.ADMINISTRATIVO;
            case SHARED_CONTRIBUTION -> TipoPagoEnum.COMPARTIR;
            default -> throw new IllegalArgumentException("Contribution payment type not supported: " + paymentType);
        };
    }

    private Integer resolveSavingAssociateId(Integer userId, PaymentDetail paymentDetail) {
        if (Objects.nonNull(paymentDetail.getUserId())) {
            return paymentDetail.getUserId();
        }
        if (paymentDetail.getPaymentType() == PaymentTypeEnum.SAVING) {
            return null;
        }

        var associates = associateService.getAssociatesByUserId(userId);
        if (paymentDetail.getPaymentType() == PaymentTypeEnum.PARTNER_SAVING) {
            log.info("Creating saving request for partner");
            return associates.stream()
                .filter(associateDto -> Util.PARTNERS.contains(associateDto.getRelationship()))
                .map(AssociateDto::getId)
                .findFirst()
                .orElse(null);
        }
        if (paymentDetail.getPaymentType() == PaymentTypeEnum.CHILDRENS_SAVING) {
            log.info("Creating saving request for children");
            return associates.stream()
                .filter(associateDto -> Util.CHILDREN.contains(associateDto.getRelationship()))
                .map(AssociateDto::getId)
                .findFirst()
                .orElse(null);
        }
        return null;
    }

    public DailyResponse generateDailyReport(LocalDate fecha) {

        var pagosDelDia = pagoRepository.findByFecha(fecha)
            .stream()
            .map(pagoMapper::toDto)
            .toList();
        var loansByUser = loanService.getLoanByStartDate(fecha)
            .stream()
            .collect(Collectors.groupingBy(
                loan -> loan.getUser()
                    .getNumberId(),
                Collectors.mapping(
                    Loan::getLoanAmount,
                    Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                )
            ));

        var totalLoans = loansByUser.values()
            .stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .doubleValue();
        if (pagosDelDia.isEmpty()) {
            return new DailyResponse(fecha, null, null, 0.0, 0.0, 0.0,
                "No se registraron pagos en la fecha: " + fecha, null);
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

        BigDecimal interestAmount = BigDecimal.valueOf(
            totalPorTipoPago.getOrDefault(TipoPagoEnum.ABONO_INTERES, 0.0)
        );

        Double montoTotalPagos = pagosDelDia.stream()
            .mapToDouble(PagoDto::getMonto)
            .sum();
        // Calcular el monto total
        Double montoTotal = montoTotalPagos - totalLoans;

        return new DailyResponse(fecha, pagosAgrupados, totalPorTipoPago, totalLoans, montoTotalPagos,
            montoTotal, null, distributionInterestService.getByDate(fecha));
    }


    public WeeklySummaryResponse getLatestWednesdaySummary() {
        var latestWednesday = pagoRepository.findLatestWednesdayWithPayments();
        if (latestWednesday.isEmpty()) {
            return WeeklySummaryResponse.builder()
                .message("No hay pagos registrados en miércoles.")
                .build();
        }

        return buildWeeklyCajaSummary(latestWednesday.get());
    }

    public WeeklySummaryResponse getWeeklySummaryForDate(LocalDate date) {
        return buildWeeklyCajaSummary(date);
    }

    private double sumTypes(Map<TipoPagoEnum, Double> totals, Set<TipoPagoEnum> types) {
        return totals.entrySet().stream()
            .filter(entry -> types.contains(entry.getKey()))
            .mapToDouble(entry -> Objects.requireNonNullElse(entry.getValue(), 0.0))
            .sum();
    }

    private WeeklySummaryResponse buildWeeklyCajaSummary(LocalDate date) {
        var dailyReport = generateDailyReport(date);
        if (dailyReport.getTotalPorTipoPago() == null) {
            return WeeklySummaryResponse.builder()
                .date(date)
                .message("No se encontraron pagos para el miércoles más reciente.")
                .build();
        }

        var totals = dailyReport.getTotalPorTipoPago();

        double ahorro = sumTypes(totals, EnumSet.of(TipoPagoEnum.AHORRO));
        double intereses1 = sumTypes(totals, EnumSet.of(TipoPagoEnum.ABONO_INTERES));
        double capital1 = sumTypes(totals, EnumSet.of(TipoPagoEnum.ABONO_CAPITAL));
        double capital2 = sumTypes(totals, EnumSet.of(TipoPagoEnum.PRESTAMOS_2));
        double capitalExt = sumTypes(totals, EnumSet.of(TipoPagoEnum.PRESTAMO_EXTERNO));
        double fondoPrevisionUrbano = sumTypes(totals, EnumSet.of(TipoPagoEnum.FONDO_PREVISION_URBANO));
        double fondoPrevisionInterurbano = sumTypes(totals, EnumSet.of(TipoPagoEnum.FONDO_PREVISION_INTERURBANO));

        double ingresos = ahorro + intereses1 + capital1 + capital2 + capitalExt
            + fondoPrevisionUrbano + fondoPrevisionInterurbano;
        double egresos = Objects.requireNonNullElse(dailyReport.getTotalPrestamos(), 0.0);
        double totalDia = ingresos - egresos;

        BigDecimal saldoAnterior = calculateSaldoAnterior(date);
        BigDecimal saldoFinal = saldoAnterior.add(BigDecimal.valueOf(totalDia));

        return WeeklySummaryResponse.builder()
            .date(date)
            .ahorro(ahorro)
            .intereses1(intereses1)
            .capital1(capital1)
            .capital2(capital2)
            .capitalExt(capitalExt)
            .fondoPrevisionUrbano(fondoPrevisionUrbano)
            .fondoPrevisionInterurbano(fondoPrevisionInterurbano)
            .ingresos(ingresos)
            .egresos(egresos)
            .totalDia(totalDia)
            .saldoAnterior(saldoAnterior.doubleValue())
            .saldoFinal(saldoFinal.doubleValue())
            .interestIncome(intereses1)
            .savingsIncome(ahorro)
            .loanPrincipalIncome(capital1)
            .loansOutflow(egresos)
            .build();
    }

    private BigDecimal calculateSaldoAnterior(LocalDate date) {
        var previousWednesday = pagoRepository.findPreviousWednesdayWithPayments(date);
        if (previousWednesday.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(calculateTotalDia(previousWednesday.get()));
    }

    private BigDecimal sumPagosBefore(LocalDate date, Set<TipoPagoEnum> types) {
        var rows = pagoRepository.sumByTipoPagoBefore(date, types);
        BigDecimal total = BigDecimal.ZERO;
        for (Object[] row : rows) {
            if (row.length < 2 || row[1] == null) {
                continue;
            }
            total = total.add((BigDecimal) row[1]);
        }
        return total;
    }

    private double calculateTotalDia(LocalDate date) {
        var dailyReport = generateDailyReport(date);
        if (dailyReport.getTotalPorTipoPago() == null) {
            return 0.0;
        }
        var totals = dailyReport.getTotalPorTipoPago();

        double ahorro = sumTypes(totals, EnumSet.of(TipoPagoEnum.AHORRO));
        double intereses1 = sumTypes(totals, EnumSet.of(TipoPagoEnum.ABONO_INTERES));
        double capital1 = sumTypes(totals, EnumSet.of(TipoPagoEnum.ABONO_CAPITAL));
        double capital2 = sumTypes(totals, EnumSet.of(TipoPagoEnum.PRESTAMOS_2));
        double capitalExt = sumTypes(totals, EnumSet.of(TipoPagoEnum.PRESTAMO_EXTERNO));
        double fondoPrevisionUrbano = sumTypes(totals, EnumSet.of(TipoPagoEnum.FONDO_PREVISION_URBANO));
        double fondoPrevisionInterurbano = sumTypes(totals, EnumSet.of(TipoPagoEnum.FONDO_PREVISION_INTERURBANO));

        double ingresos = ahorro + intereses1 + capital1 + capital2 + capitalExt
            + fondoPrevisionUrbano + fondoPrevisionInterurbano;
        double egresos = Objects.requireNonNullElse(dailyReport.getTotalPrestamos(), 0.0);
        return ingresos - egresos;
    }


    @Transactional
    public PaymentReversalResponse reversePayments(PaymentReversalRequest request) {
        log.info("Starting payment reversal for userId: {}, date: {}", request.userId(), request.date());
        
        var user = getUser(request.userId());
        
        // 1. Buscar todos los pagos del usuario en la fecha específica
        var pagosToReverse = pagoRepository.findByFechaAndCedula(request.date(), user.getNumberId());
        
        if (pagosToReverse.isEmpty()) {
            log.info("No payments found for userId: {} on date: {}", request.userId(), request.date());
            return PaymentReversalResponse.noPaymentsFound(request.userId(), request.date(), request.reason());
        }
        
        List<ReversedPaymentDetail> reversedDetails = new ArrayList<>();
        BigDecimal totalReversedAmount = BigDecimal.ZERO;
        
        // 2. Revertir cada tipo de pago
        for (var pago : pagosToReverse) {
            BigDecimal amount = pago.getMonto();
            totalReversedAmount = totalReversedAmount.add(amount);
            
            // Crear detalle de reversión
            reversedDetails.add(new ReversedPaymentDetail(
                pago.getTipoPago(),
                amount,
                pago.getTipoPago().getDescription()
            ));
            
            // Revertir fondos si aplica
            if (Set.of(TipoPagoEnum.AHORRO, TipoPagoEnum.ABONO_CAPITAL,
                      TipoPagoEnum.ABONO_INTERES, TipoPagoEnum.PRESTAMOS_2)
                .contains(pago.getTipoPago())) {
                fundsService.saveFunds(amount, FundsType.SUBTRACT);
            }
        }

        // 3. Eliminar registros relacionados
        deleteRelatedPaymentRecords(user, request.date(), pagosToReverse);
        
        // 4. Eliminar registros de pago principales
        pagoRepository.deleteAll(pagosToReverse);
        
        // 5. Registrar la reversión en el historial
        recordPaymentReversal(request.userId(), totalReversedAmount, request.reason());
        
        log.info("Payment reversal completed for userId: {}. Total reversed: {}", 
                request.userId(), totalReversedAmount);
        
        return PaymentReversalResponse.success(
            request.userId(),
            request.date(),
            totalReversedAmount,
            reversedDetails,
            request.reason()
        );
    }
    
    private void deleteRelatedPaymentRecords(User user, LocalDate date, List<Pago> pagosToReverse) {
        log.info("Deleting related payment records for userId: {}, date: {}", user.getUserId(), date);

        reverseSavings(user, date, pagosToReverse);

        // Eliminar pagos de contribuciones
        var contributionPayments = contributionPaymentRepository.findByUserAndPaymentDate(user, date);
        if (!contributionPayments.isEmpty()) {
            contributionPaymentRepository.deleteAll(contributionPayments);
            log.info("Deleted {} contribution payment records", contributionPayments.size());
        }

        reverseLoanPayments(user, date);
        reverseSupplyPayments(user, date);

        // Eliminar otros pagos
        var otherPayments = otherPaymentRepository.findByUserAndPaymentDate(user, date);
        if (!otherPayments.isEmpty()) {
            otherPaymentRepository.deleteAll(otherPayments);
            log.info("Deleted {} other payment records", otherPayments.size());
        }
    }

    private void reverseSavings(User user, LocalDate date, List<Pago> pagosToReverse) {
        var savingsToReverse = new ArrayList<>(savingRepository.findByUserAndSavingDate(user, date));

        var associates = associateService.getAssociatesByUserId(user.getUserId());
        associates.stream()
            .filter(associate -> Util.PARTNERS.contains(associate.getRelationship())
                || Util.CHILDREN.contains(associate.getRelationship()))
            .map(AssociateDto::getId)
            .map(this::getUser)
            .forEach(associateUser ->
                savingsToReverse.addAll(savingRepository.findByUserAndSavingDate(associateUser, date)));

        pagosToReverse.stream()
            .filter(pago -> pago.getTipoPago() == TipoPagoEnum.FONDO_PREVISION_URBANO
                || pago.getTipoPago() == TipoPagoEnum.FONDO_PREVISION_INTERURBANO)
            .forEach(pago -> findForecastFundSaving(pago, date)
                .ifPresent(savingsToReverse::add));

        if (!savingsToReverse.isEmpty()) {
            savingService.reverseSavingSet(savingsToReverse.stream().distinct().toList());
            log.info("Deleted {} saving records", savingsToReverse.size());
        }
    }

    private Optional<Saving> findForecastFundSaving(Pago pago, LocalDate date) {
        var forecastFundUser = switch (pago.getTipoPago()) {
            case FONDO_PREVISION_URBANO -> resolveForecastFundUser(PaymentTypeEnum.URBAN_FORECAST_FUND);
            case FONDO_PREVISION_INTERURBANO -> resolveForecastFundUser(PaymentTypeEnum.INTERURBAN_FORECAST_FUND);
            default -> null;
        };

        if (forecastFundUser == null) {
            return Optional.empty();
        }

        return savingRepository.findByUserAndSavingDate(forecastFundUser, date).stream()
            .filter(saving -> saving.getAmount() != null && saving.getAmount().compareTo(pago.getMonto()) == 0)
            .findFirst();
    }

    private void reverseLoanPayments(User user, LocalDate date) {
        var loanPayments = loanPaymentRepository.findByUserAndPaymentDate(user, date);
        if (!loanPayments.isEmpty()) {
            loanService.reversePayments(loanPayments);
            log.info("Deleted {} loan payment records", loanPayments.size());
        }
    }

    private void reverseSupplyPayments(User user, LocalDate date) {
        var supplyPayments = supplyPaymentRepository.findByUserAndPaymentDate(user, date);
        if (!supplyPayments.isEmpty()) {
            supplyService.reversePayments(supplyPayments);
            log.info("Deleted {} supply payment records", supplyPayments.size());
        }
    }
    
    private void recordPaymentReversal(Integer userId, BigDecimal totalAmount, String reason) {
        log.info("Recording payment reversal in history for userId: {}, amount: {}", userId, totalAmount);
        
        var balanceHistory = BalanceHistory.builder()
            .user(User.builder().userId(userId).build())
            .transactionDate(LocalDate.now())
            .transactionType(com.ajustadoati.sc.domain.enums.TransactionType.PAYMENT_REVERSAL)
            .amount(totalAmount)
            .description(reason != null ? reason : "Payment reversal")
            .build();
        
        balanceHistoryService.saveList(List.of(balanceHistory));
        log.info("Payment reversal recorded successfully in history for userId: {}", userId);
    }

}
