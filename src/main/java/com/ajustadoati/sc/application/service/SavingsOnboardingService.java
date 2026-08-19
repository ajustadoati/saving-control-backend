package com.ajustadoati.sc.application.service;

import com.ajustadoati.sc.adapter.rest.dto.request.SavingsOnboardingRequest;
import com.ajustadoati.sc.adapter.rest.dto.response.SavingsOnboardingResponse;
import com.ajustadoati.sc.adapter.rest.dto.response.SummaryDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SavingsOnboardingService {

  private final UserAccountSummaryService userAccountSummaryService;
  private final UserSavingsBoxService userSavingsBoxService;

  @Transactional
  public SavingsOnboardingResponse save(Integer userId, SavingsOnboardingRequest request) {
    var summary = userAccountSummaryService.saveOrUpdateInitialSetup(
      new SummaryDto(
        userId,
        request.getInitialBalance(),
        request.getInitialBalance(),
        request.getInterestEarned(),
        request.getEffectiveDate()
      ),
      request.getEffectiveDate()
    );

    var savingsBox = request.getJoinSavingsBox()
      ? userSavingsBoxService.saveOrUpdateInitialSetup(
          userId,
          request.getBoxCount(),
          request.getBoxValue(),
          request.getEffectiveDate()
        )
      : null;

    return SavingsOnboardingResponse.builder()
      .summary(new SummaryDto(
        summary.getUser().getUserId(),
        summary.getInitialBalance(),
        summary.getCurrentBalance(),
        summary.getInterestEarned(),
        summary.getLastUpdated()
      ))
      .savingsBox(savingsBox)
      .build();
  }
}
