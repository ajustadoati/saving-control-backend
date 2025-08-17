package com.ajustadoati.sc.application.service;

import com.ajustadoati.sc.adapter.rest.dto.request.ContributionPaymentRequest;
import com.ajustadoati.sc.adapter.rest.dto.request.LoanPaymentRequest;
import com.ajustadoati.sc.adapter.rest.dto.request.PaymentDetail;
import com.ajustadoati.sc.adapter.rest.dto.request.PaymentRequest;
import com.ajustadoati.sc.adapter.rest.dto.request.PaymentReversalRequest;
import com.ajustadoati.sc.adapter.rest.dto.request.SavingRequest;
import com.ajustadoati.sc.adapter.rest.dto.request.SupplyPaymentRequest;
import com.ajustadoati.sc.adapter.rest.dto.request.enums.PaymentTypeEnum;
import com.ajustadoati.sc.adapter.rest.dto.response.AssociateDto;
import com.ajustadoati.sc.adapter.rest.dto.response.DailyResponse;
import com.ajustadoati.sc.adapter.rest.dto.response.PaymentResponse;
import com.ajustadoati.sc.adapter.rest.dto.response.PaymentResponse.PaymentStatus;
import com.ajustadoati.sc.adapter.rest.dto.response.PaymentReversalResponse;
import com.ajustadoati.sc.adapter.rest.dto.response.PaymentReversalResponse.ReversedPaymentDetail;
import com.ajustadoati.sc.adapter.rest.repository.*;
import com.ajustadoati.sc.adapter.rest.repository.ContributionTypeRepository;
import com.ajustadoati.sc.adapter.rest.repository.PagoRepository;
import com.ajustadoati.sc.adapter.rest.repository.SavingRepository;
import com.ajustadoati.sc.adapter.rest.repository.UserRepository;
import com.ajustadoati.sc.application.mapper.PagoMapper;
import com.ajustadoati.sc.application.service.dto.DistributionInterestDto;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.ajustadoati.sc.adapter.rest.dto.request.enums.PaymentTypeEnum.WHEELS;

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
    private final UserAccountSummaryService userAccountSummaryService;
    private final UserSavingsBoxService userSavingsBoxService;
    private final ContributionPaymentRepository contributionPaymentRepository;
    private final LoanPaymentRepository loanPaymentRepository;
    private final SupplyPaymentRepository supplyPaymentRepository;
    private final OtherPaymentRepository otherPaymentRepository;
    private final BalanceHistoryService balanceHistoryService;

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
                .map(AssociateDto::getId)
                .findFirst();
            associateId.ifPresent(paymentDetail::setUserId);

        } else if (paymentDetail.getPaymentType() == PaymentTypeEnum.CHILDRENS_SAVING) {
            log.info("Creating saving request for children");
            var associateId = associates.stream()
                .filter(associateDto -> Util.CHILDREN.contains(associateDto.getRelationship()))
                .map(AssociateDto::getId)
                .findFirst();
            associateId.ifPresent(paymentDetail::setUserId);
        }
        savingRequests.add(getSavingRequest(user.getUserId(), paymentDetail, date));
        log.info("saving dto");
        pagoDtos.add(buildPagoDto(user, date, paymentDetail, TipoPagoEnum.AHORRO));
    }

    private void processSupplies(User user, LocalDate date, PaymentDetail paymentDetail,
                                 List<PagoDto> pagoDtos) {
        pagoDtos.add(buildPagoDto(user, date, paymentDetail, TipoPagoEnum.SUMINISTROS));
        processSuppliesPayment(user.getUserId(), paymentDetail, date);
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
        return PagoDto.builder()
            .tipoPago(tipoPago)
            .monto(paymentDetail.getAmount()
                .doubleValue())
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
        var loans = loanService.getLoansByUser(userId);
        var loan = loans.stream()
            .filter(loanResponse -> Objects.equals(loanResponse.getLoanId(),
                paymentDetail.getReferenceId()))
            .findFirst();
        loan.ifPresent(loanResponse -> {
            var request = LoanPaymentRequest.builder()
                .loanId(loanResponse.getLoanId())
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
        var loan = loans.stream()
            .filter(loanResponse -> Objects.equals(loanResponse.getLoanId(),
                paymentDetail.getReferenceId()))
            .findFirst();
        loan.ifPresent(loanResponse -> {
            var request = LoanPaymentRequest.builder()
                .loanId(loanResponse.getLoanId())
                .paymentDate(date)
                .paymentTypeId(2)
                .amount(paymentDetail.getAmount())
                .build();
            loanService.registerPayment(request);
        });

    }

    private void processSuppliesPayment(Integer userId, PaymentDetail paymentDetail, LocalDate date) {
        var supplies = supplyService.getSuppliesByUser(userId);
        var supply = supplies.stream()
            .filter(supplyResponse -> Objects.equals(
                supplyResponse.getSupplyId(), paymentDetail.getReferenceId()))
            .findFirst();
        if (supply.isPresent()) {
            var request = new SupplyPaymentRequest();
            request.setPaymentDate(date);
            request.setSupplyId(supply.get()
                .getSupplyId());
            request.setAmount(paymentDetail.getAmount());
            supplyService.registerPayment(request);
        }
    }

    private void processOthersPayment(User user, PaymentDetail paymentDetail, LocalDate date, List<PagoDto> pagos) {
        if (paymentDetail.getPaymentType().equals(WHEELS)){
            pagos.add(buildPagoDto(user, date, paymentDetail, TipoPagoEnum.CAUCHOS));
        }

        OtherPayment other = new OtherPayment();
        other.setName(paymentDetail.getPaymentType()
            .name());
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

        BigDecimal interestAmount = BigDecimal.valueOf(totalPorTipoPago.get(TipoPagoEnum.ABONO_INTERES));

        Double montoTotalPagos = pagosDelDia.stream()
            .mapToDouble(PagoDto::getMonto)
            .sum();
        // Calcular el monto total
        Double montoTotal = montoTotalPagos - totalLoans;

        return new DailyResponse(fecha, pagosAgrupados, totalPorTipoPago, totalLoans, montoTotalPagos,
            montoTotal, null, distribuirIntereses(interestAmount));
    }

    public List<DistributionInterestDto> distribuirIntereses(BigDecimal montoTotal) {
        List<UserSavingsBox> socios = userSavingsBoxService.findAll();

        List<UserAssociate> asociados = userRepository.findAll()
            .stream()
            .filter(user -> CollectionUtils.isNotEmpty(user.getAssociates()))
            .flatMap(user -> user.getAssociates().stream())
            .toList();

        Map<User, BigDecimal> balances = new LinkedHashMap<>();

        socios.forEach(user -> {
            BigDecimal balance = userAccountSummaryService.findByUserId(user.getUser().getUserId()).getCurrentBalance();
            balances.put(user.getUser(), balance);
        });

        asociados.forEach(user -> {
            BigDecimal balance = userAccountSummaryService.findByUserId(user.getUserAssociate().getUserId()).getCurrentBalance();
            balances.put(user.getUserAssociate(), balance);
        });

        BigDecimal totalBalance = balances.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<User, BigDecimal> rawDistributed = new LinkedHashMap<>();
        for (Map.Entry<User, BigDecimal> entry : balances.entrySet()) {
            BigDecimal balance = entry.getValue();
            if (balance.compareTo(BigDecimal.ZERO) == 0 || totalBalance.compareTo(BigDecimal.ZERO) == 0) {
                rawDistributed.put(entry.getKey(), BigDecimal.ZERO);
            } else {
                BigDecimal porcentaje = balance.divide(totalBalance, 10, RoundingMode.HALF_UP);
                rawDistributed.put(entry.getKey(), montoTotal.multiply(porcentaje));
            }
        }

        // Redondear a 2 decimales y calcular diferencia
        Map<User, BigDecimal> finalAmounts = new LinkedHashMap<>();
        BigDecimal totalDistributed = BigDecimal.ZERO;
        for (Map.Entry<User, BigDecimal> entry : rawDistributed.entrySet()) {
            BigDecimal rounded = entry.getValue().setScale(2, RoundingMode.HALF_UP);
            finalAmounts.put(entry.getKey(), rounded);
            totalDistributed = totalDistributed.add(rounded);
        }

        BigDecimal diferencia = montoTotal.subtract(totalDistributed);

        // Compensar diferencia (±0.01)
        if (diferencia.compareTo(BigDecimal.ZERO) != 0) {
            List<Map.Entry<User, BigDecimal>> ordenado = rawDistributed.entrySet().stream()
                .sorted((a, b) -> b.getValue().remainder(BigDecimal.ONE)
                    .compareTo(a.getValue().remainder(BigDecimal.ONE)))
                .toList();

            for (Map.Entry<User, BigDecimal> entry : ordenado) {
                if (diferencia.abs().compareTo(new BigDecimal("0.01")) < 0) break;

                User user = entry.getKey();
                BigDecimal actual = finalAmounts.get(user);
                BigDecimal ajuste = diferencia.signum() > 0 ? new BigDecimal("0.01") : new BigDecimal("-0.01");
                finalAmounts.put(user, actual.add(ajuste));
                diferencia = diferencia.subtract(ajuste);
            }
        }

        // Construir lista final
        List<DistributionInterestDto> result = new ArrayList<>();
        for (Map.Entry<User, BigDecimal> entry : finalAmounts.entrySet()) {
            User user = entry.getKey();
            result.add(DistributionInterestDto.builder()
                .userId(user.getUserId())
                .name(user.getFirstName() + " " + user.getLastName())
                .totalBalance(balances.get(user).setScale(2, RoundingMode.HALF_UP))
                .distributedAmount(entry.getValue())
                .build());
        }

        return result;
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
        deleteRelatedPaymentRecords(user, request.date());
        
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
    
    private void deleteRelatedPaymentRecords(User user, LocalDate date) {
        log.info("Deleting related payment records for userId: {}, date: {}", user.getUserId(), date);
        
        // Eliminar ahorros
        var savings = savingRepository.findByUserAndSavingDate(user, date);
        if (!savings.isEmpty()) {
            savingRepository.deleteAll(savings);
            log.info("Deleted {} saving records", savings.size());
        }
        
        // Eliminar pagos de contribuciones
        var contributionPayments = contributionPaymentRepository.findByUserAndPaymentDate(user, date);
        if (!contributionPayments.isEmpty()) {
            contributionPaymentRepository.deleteAll(contributionPayments);
            log.info("Deleted {} contribution payment records", contributionPayments.size());
        }
        
        // Eliminar pagos de préstamos
        var loanPayments = loanPaymentRepository.findByUserAndPaymentDate(user, date);
        if (!loanPayments.isEmpty()) {
            loanPaymentRepository.deleteAll(loanPayments);
            log.info("Deleted {} loan payment records", loanPayments.size());
        }
        
        // Eliminar pagos de suministros
        var supplyPayments = supplyPaymentRepository.findByUserAndPaymentDate(user, date);
        if (!supplyPayments.isEmpty()) {
            supplyPaymentRepository.deleteAll(supplyPayments);
            log.info("Deleted {} supply payment records", supplyPayments.size());
        }
        
        // Eliminar otros pagos
        var otherPayments = otherPaymentRepository.findByUserAndPaymentDate(user, date);
        if (!otherPayments.isEmpty()) {
            otherPaymentRepository.deleteAll(otherPayments);
            log.info("Deleted {} other payment records", otherPayments.size());
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
