package com.ajustadoati.sc.application.service;

import com.ajustadoati.sc.adapter.rest.dto.response.BalanceHistoryDto;
import com.ajustadoati.sc.adapter.rest.exception.BalanceAlreadyExistException;
import com.ajustadoati.sc.adapter.rest.repository.DistributionInterestRepository;
import com.ajustadoati.sc.application.service.dto.DistributionInterestDto;
import com.ajustadoati.sc.application.service.enums.FundsType;
import com.ajustadoati.sc.domain.DistributionInterest;
import com.ajustadoati.sc.domain.User;
import com.ajustadoati.sc.domain.enums.TransactionType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DistributionInterestService {

    private final BalanceHistoryService balanceHistoryService;

    private final FundsService fundsService;

    private final UserAccountSummaryService userAccountSummaryService;

    private final DistributionInterestRepository distributionInterestRepository;

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
        userAccountSummaryService.updateBalance(distributionInterestDto.getUserId(), distributionInterestDto.getDistributedAmount());
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
