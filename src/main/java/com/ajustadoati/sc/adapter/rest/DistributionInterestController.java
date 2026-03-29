package com.ajustadoati.sc.adapter.rest;

import com.ajustadoati.sc.adapter.rest.dto.request.DistributionInterestRequest;
import com.ajustadoati.sc.application.service.PaymentService;
import com.ajustadoati.sc.application.service.dto.DistributionInterestDto;
import com.ajustadoati.sc.application.service.DistributionInterestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/distributions")
public class DistributionInterestController {

    @Autowired
    private DistributionInterestService distributionInterestService;

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public void saveAllDistributions(@RequestBody DistributionInterestRequest distributionInterestRequest){

        distributionInterestService.saveList(distributionInterestRequest.getDistributionInterestList(), distributionInterestRequest.getDate());
    }

    @PostMapping("/run")
    public ResponseEntity<List<DistributionInterestDto>> runDistribution(@RequestParam("date") String date){
        var localDate = java.time.LocalDate.parse(date);
        List<DistributionInterestDto> distributions = paymentService.calculateDistributionForDate(localDate);
        if (distributions.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        distributionInterestService.saveList(distributions, localDate);
        return ResponseEntity.ok(distributions);
    }

    @GetMapping
    public ResponseEntity<List<DistributionInterestDto>> getByDate(@RequestParam("date") String date) {
        var localDate = java.time.LocalDate.parse(date);
        return ResponseEntity.ok(distributionInterestService.getByDate(localDate));
    }

}
