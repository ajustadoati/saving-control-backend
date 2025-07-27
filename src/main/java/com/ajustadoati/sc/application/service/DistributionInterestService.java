package com.ajustadoati.sc.application.service;

import com.ajustadoati.sc.adapter.rest.dto.response.BalanceHistoryDto;
import com.ajustadoati.sc.adapter.rest.exception.BalanceAlreadyExistException;
import com.ajustadoati.sc.application.service.dto.DistributionInterestDto;
import com.ajustadoati.sc.application.service.enums.FundsType;
import com.ajustadoati.sc.domain.enums.TransactionType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DistributionInterestService {

    private final BalanceHistoryService balanceHistoryService;

    private final FundsService fundsService;

    @Transactional
    public void save(DistributionInterestDto distributionInterestDto) {
        var history = balanceHistoryService.findAllByUserAndDate(distributionInterestDto.getUserId(), distributionInterestDto.getDate());
         if (CollectionUtils.isNotEmpty(history)) {
            var result = history.stream()
                .filter(h -> h.transactionType()
                    .equals(TransactionType.DISTRIBUTED_INTEREST))
                .toList();
            if (CollectionUtils.isNotEmpty(result)) {
                log.info("Balance exists for user {} {}", distributionInterestDto.getUserId(), distributionInterestDto.getName());
                throw new BalanceAlreadyExistException("Balance exists for user: " + distributionInterestDto.getUserId());
            }
        }

        fundsService.saveFunds(distributionInterestDto.getDistributedAmount(), FundsType.ADD);
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

}
