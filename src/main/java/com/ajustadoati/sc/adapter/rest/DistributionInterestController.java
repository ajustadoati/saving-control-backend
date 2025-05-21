package com.ajustadoati.sc.adapter.rest;

import com.ajustadoati.sc.adapter.rest.dto.request.DistributionInterestRequest;
import com.ajustadoati.sc.application.service.DistributionInterestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/distributions")
public class DistributionInterestController {

    @Autowired
    private DistributionInterestService distributionInterestService;

    @PostMapping
    public void saveAllDistributions(@RequestBody DistributionInterestRequest distributionInterestRequest){

        distributionInterestService.saveList(distributionInterestRequest.getDistributionInterestList(), distributionInterestRequest.getDate());
    }

}
