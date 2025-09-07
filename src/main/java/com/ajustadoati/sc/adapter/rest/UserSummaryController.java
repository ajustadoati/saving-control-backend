package com.ajustadoati.sc.adapter.rest;

import com.ajustadoati.sc.adapter.rest.dto.request.WithdrawalRequest;
import com.ajustadoati.sc.adapter.rest.dto.response.SummaryDto;
import com.ajustadoati.sc.adapter.rest.dto.response.WithdrawalResponse;
import com.ajustadoati.sc.application.service.UserAccountSummaryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/summary")
public class UserSummaryController {

    @Autowired
    private UserAccountSummaryService userAccountSummaryService;

    @GetMapping("/{userId}")
    public ResponseEntity<SummaryDto> getBalance(@PathVariable Integer userId) {
        var summary = userAccountSummaryService.findByUserId(userId);

        return ResponseEntity.ok(
            new SummaryDto(summary.getUser()
                .getUserId(), summary.getInitialBalance(),
                summary.getCurrentBalance(),
                summary.getInterestEarned(),
                summary.getLastUpdated()));
    }

    @GetMapping
    public ResponseEntity<List<SummaryDto>> getAll() {
        var resume = userAccountSummaryService.getAll()
            .stream()
            .map(summary -> new SummaryDto(summary.getUser()
                    .getUserId(), summary.getInitialBalance(),
                summary.getCurrentBalance(),
                summary.getInterestEarned(),
                summary.getLastUpdated())).toList();

        return ResponseEntity.ok(resume);
    }

    @PostMapping
    public ResponseEntity<SummaryDto> saveBalance(@RequestBody SummaryDto summaryDto) {
        var summary = userAccountSummaryService.save(summaryDto);

        return ResponseEntity.ok(
            new SummaryDto(summary.getUser()
                .getUserId(), summary.getInitialBalance(),
                summary.getCurrentBalance(), summary.getInterestEarned(), summary.getLastUpdated()));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<WithdrawalResponse> withdrawFunds(@Valid @RequestBody WithdrawalRequest request) {
        // Obtener balance actual antes del retiro
        var currentSummary = userAccountSummaryService.findByUserId(request.userId());
        var previousBalance = currentSummary.getCurrentBalance();

        // Procesar retiro con tipo especificado
        var updatedSummary = userAccountSummaryService.withdrawFunds(
            request.userId(), 
            request.amount(), 
            request.description(),
            request.withdrawalType()
        );

        // Crear respuesta exitosa
        var response = WithdrawalResponse.success(
            request.userId(),
            request.amount(),
            previousBalance,
            updatedSummary.getCurrentBalance(),
            request.description()
        );

        return ResponseEntity.ok(response);
    }

}
