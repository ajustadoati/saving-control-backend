package com.ajustadoati.sc.application.service;

import com.ajustadoati.sc.adapter.rest.dto.response.BalanceHistoryDto;
import com.ajustadoati.sc.adapter.rest.dto.response.DistributionInterestStatusResponse;
import com.ajustadoati.sc.adapter.rest.exception.BalanceAlreadyExistException;
import com.ajustadoati.sc.adapter.rest.repository.DistributionInterestRepository;
import com.ajustadoati.sc.adapter.rest.repository.PagoRepository;
import com.ajustadoati.sc.adapter.rest.repository.UserRepository;
import com.ajustadoati.sc.application.service.dto.DistributionInterestDto;
import com.ajustadoati.sc.application.service.dto.enums.TipoPagoEnum;
import com.ajustadoati.sc.application.service.enums.FundsType;
import com.ajustadoati.sc.domain.DistributionInterest;
import com.ajustadoati.sc.domain.UserAssociate;
import com.ajustadoati.sc.domain.UserSavingsBox;
import com.ajustadoati.sc.domain.User;
import com.ajustadoati.sc.domain.enums.TransactionType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DistributionInterestService {

    private final BalanceHistoryService balanceHistoryService;

    private final FundsService fundsService;

    private final UserAccountSummaryService userAccountSummaryService;

    private final DistributionInterestRepository distributionInterestRepository;

    private final PagoRepository pagoRepository;

    private final UserSavingsBoxService userSavingsBoxService;

    private final UserRepository userRepository;

    @Transactional
    public void save(DistributionInterestDto distributionInterestDto) {
        if (distributionInterestRepository.existsByUser_UserIdAndDistributionDate(
            distributionInterestDto.getUserId(), distributionInterestDto.getDate())) {
            log.info("Distribution exists for user {} {}", distributionInterestDto.getUserId(), distributionInterestDto.getName());
            throw new BalanceAlreadyExistException("Distribution exists for user: " + distributionInterestDto.getUserId());
        }

        DistributionInterest entity = DistributionInterest.builder()
            .user(User.builder().userId(distributionInterestDto.getUserId()).build())
            .distributionDate(distributionInterestDto.getDate())
            .totalBalance(distributionInterestDto.getTotalBalance())
            .interestPercent(distributionInterestDto.getInterest())
            .distributedAmount(distributionInterestDto.getDistributedAmount())
            .createdAt(LocalDateTime.now())
            .build();

        distributionInterestRepository.save(entity);

        fundsService.saveFunds(distributionInterestDto.getDistributedAmount(), FundsType.ADD);
        //userAccountSummaryService.updateBalance(distributionInterestDto.getUserId(), distributionInterestDto.getDistributedAmount());
        userAccountSummaryService.updateInterestBalance(distributionInterestDto.getUserId(), distributionInterestDto.getDistributedAmount());

        balanceHistoryService.save(new BalanceHistoryDto(0,
            distributionInterestDto.getUserId(),
            distributionInterestDto.getDate(),
            TransactionType.DISTRIBUTED_INTEREST,
            distributionInterestDto.getDistributedAmount(), "Interest Distributed"));

    }


    public void saveList(List<DistributionInterestDto> distributionInterestDtoList, LocalDate date) {
        log.info("Saving list");
        distributionInterestDtoList.forEach(distribution -> {
            log.info("Saving distribution for user: {} amount: {} fecha: {}", distribution.getUserId(), distribution.getDistributedAmount(), date);
            distribution.setDate(date);
            save(distribution);
        });

    }

    public List<DistributionInterestDto> getByDate(LocalDate date) {
        return distributionInterestRepository.findByDistributionDate(date)
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    public boolean isDistributed(LocalDate date) {
        return distributionInterestRepository.existsByDistributionDate(date);
    }

    public DistributionInterestStatusResponse getStatus(LocalDate date) {
        List<DistributionInterestDto> distributions = isDistributed(date)
            ? getByDate(date)
            : calculateDistributionForDate(date);

        return DistributionInterestStatusResponse.builder()
            .date(date)
            .distributed(isDistributed(date))
            .previousPendingDate(findPreviousPendingDate(date).orElse(null))
            .distributions(distributions)
            .build();
    }

    public Optional<LocalDate> findPreviousPendingDate(LocalDate date) {
        Optional<LocalDate> previousWednesday = pagoRepository.findPreviousWednesdayWithPayments(date);
        if (previousWednesday.isEmpty()) {
            return Optional.empty();
        }

        LocalDate candidate = previousWednesday.get();
        var interestAmount = pagoRepository.sumMontoByFechaAndTipoPago(candidate, TipoPagoEnum.ABONO_INTERES);
        boolean hasInterest = interestAmount != null && interestAmount.signum() > 0;
        boolean distributed = isDistributed(candidate);

        if (hasInterest && !distributed) {
            return previousWednesday;
        }

        return Optional.empty();
    }

    public List<DistributionInterestDto> calculateDistributionForDate(LocalDate date) {
        BigDecimal interestAmount = pagoRepository.sumMontoByFechaAndTipoPago(date, TipoPagoEnum.ABONO_INTERES);
        if (interestAmount == null || interestAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return List.of();
        }
        return distributeInterests(interestAmount);
    }

    public List<DistributionInterestDto> distributeInterests(BigDecimal totalAmount) {
        List<UserSavingsBox> socios = userSavingsBoxService.findAll();

        List<UserAssociate> asociados = userRepository.findAll()
            .stream()
            .filter(user -> CollectionUtils.isNotEmpty(user.getAssociates()))
            .flatMap(user -> user.getAssociates().stream())
            .toList();

        Map<User, BigDecimal> balances = new LinkedHashMap<>();

        socios.forEach(user -> {
            BigDecimal balance = userAccountSummaryService.findByUserId(user.getUser().getUserId()).getCurrentBalance();
            if (balance.doubleValue() > 0) {
                balances.put(user.getUser(), balance);
            }
        });

        asociados.forEach(user -> {
            BigDecimal balance = userAccountSummaryService.findByUserId(user.getUserAssociate().getUserId()).getCurrentBalance();
            if (balance.doubleValue() > 0) {
                balances.put(user.getUserAssociate(), balance);
            }
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
                rawDistributed.put(entry.getKey(), totalAmount.multiply(porcentaje));
            }
        }

        Map<User, BigDecimal> finalAmounts = new LinkedHashMap<>();
        BigDecimal totalDistributed = BigDecimal.ZERO;
        for (Map.Entry<User, BigDecimal> entry : rawDistributed.entrySet()) {
            BigDecimal rounded = entry.getValue().setScale(2, RoundingMode.HALF_UP);
            finalAmounts.put(entry.getKey(), rounded);
            totalDistributed = totalDistributed.add(rounded);
        }

        BigDecimal difference = totalAmount.subtract(totalDistributed);
        if (difference.compareTo(BigDecimal.ZERO) != 0) {
            List<Map.Entry<User, BigDecimal>> ordered = rawDistributed.entrySet().stream()
                .sorted((a, b) -> b.getValue().remainder(BigDecimal.ONE)
                    .compareTo(a.getValue().remainder(BigDecimal.ONE)))
                .toList();

            for (Map.Entry<User, BigDecimal> entry : ordered) {
                if (difference.abs().compareTo(new BigDecimal("0.01")) < 0) {
                    break;
                }
                User user = entry.getKey();
                BigDecimal current = finalAmounts.get(user);
                BigDecimal adjustment = difference.signum() > 0 ? new BigDecimal("0.01") : new BigDecimal("-0.01");
                finalAmounts.put(user, current.add(adjustment));
                difference = difference.subtract(adjustment);
            }
        }

        List<DistributionInterestDto> result = new ArrayList<>();
        for (Map.Entry<User, BigDecimal> entry : finalAmounts.entrySet()) {
            User user = entry.getKey();
            result.add(DistributionInterestDto.builder()
                .userId(user.getUserId())
                .name(user.getFirstName() + " " + user.getLastName())
                .totalBalance(balances.get(user).setScale(2, RoundingMode.HALF_UP))
                .interest(
                    balances.get(user)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(totalBalance, 2, RoundingMode.HALF_UP)
                )
                .distributedAmount(entry.getValue())
                .build());
        }

        return result;
    }

    private DistributionInterestDto toDto(DistributionInterest entity) {
        String name = entity.getUser() != null
            ? entity.getUser().getFirstName() + " " + entity.getUser().getLastName()
            : "";
        return DistributionInterestDto.builder()
            .userId(entity.getUser() != null ? entity.getUser().getUserId() : null)
            .name(name)
            .totalBalance(entity.getTotalBalance())
            .interest(entity.getInterestPercent())
            .distributedAmount(entity.getDistributedAmount())
            .date(entity.getDistributionDate())
            .build();
    }

}
