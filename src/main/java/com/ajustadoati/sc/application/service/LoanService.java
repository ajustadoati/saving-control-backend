package com.ajustadoati.sc.application.service;

import com.ajustadoati.sc.adapter.rest.dto.request.LoanPaymentRequest;
import com.ajustadoati.sc.adapter.rest.dto.request.LoanRequest;
import com.ajustadoati.sc.adapter.rest.dto.response.BalanceHistoryDto;
import com.ajustadoati.sc.adapter.rest.dto.response.LoanPaymentResponse;
import com.ajustadoati.sc.adapter.rest.dto.response.LoanResponse;
import com.ajustadoati.sc.adapter.rest.repository.LoanPaymentRepository;
import com.ajustadoati.sc.adapter.rest.repository.LoanPaymentTypeRepository;
import com.ajustadoati.sc.adapter.rest.repository.LoanRepository;
import com.ajustadoati.sc.adapter.rest.repository.LoanTypeRepository;
import com.ajustadoati.sc.application.service.enums.FundsType;
import com.ajustadoati.sc.domain.Loan;
import com.ajustadoati.sc.domain.LoanPayment;
import com.ajustadoati.sc.domain.LoanType;
import com.ajustadoati.sc.domain.enums.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanService {

  private final LoanRepository loanRepository;
  private final LoanPaymentRepository loanPaymentRepository;
  private final LoanTypeRepository loanTypeRepository;
  private final LoanPaymentTypeRepository loanPaymentTypeRepository;
  private final UserService userService;
  private final FundsService fundsService;
  private final SharingFundsService sharingFundsService;
  private final BalanceHistoryService balanceHistoryService;

  public LoanResponse createLoan(LoanRequest request) {
    var loanType = loanTypeRepository.findById(request.getLoanTypeId())
      .orElseThrow(() -> new IllegalArgumentException("Invalid Loan Type"));
    var user = userService.getUserById(request.getUserId());

    Loan loan = new Loan();
    loan.setUser(user);
    loan.setLoanAmount(request.getLoanAmount());
    loan.setInterestRate(request.getInterestRate());
    loan.setLoanBalance(request.getLoanBalance());
    loan.setStartDate(request.getStartDate());
    loan.setReason(request.getReason());
    loan.setEndDate(request.getEndDate());
    loan.setLoanType(loanType);

    loan = loanRepository.save(loan);
    if (loan.getLoanType().getLoanTypeId() == 4) {
      log.info("Loan type is sharing, saving funds to sharing funds");
      sharingFundsService.saveFunds(request.getLoanAmount(), FundsType.SUBTRACT);
    } else {
      log.info("Loan type is not sharing, saving funds to funds");
        fundsService.saveFunds(request.getLoanAmount(), FundsType.SUBTRACT);
    }

    return mapToLoanResponse(loan);
  }

  public void registerPayment(LoanPaymentRequest request) {
    var loan = loanRepository.findById(request.getLoanId())
      .orElseThrow(() -> new IllegalArgumentException("Invalid Loan"));

    var paymentType = loanPaymentTypeRepository.findById(request.getPaymentTypeId())
      .orElseThrow(() -> new IllegalArgumentException("Invalid Payment Type"));

    LoanPayment payment = new LoanPayment();
    payment.setLoan(loan);
    payment.setPaymentDate(request.getPaymentDate());
    payment.setPaymentType(paymentType);
    payment.setAmount(request.getAmount());

    if (paymentType.getLoanPaymentTypeId() == 1) {
      var currentBalance = loan.getLoanBalance();

      if (currentBalance.subtract(request.getAmount()).intValue() < 0) {
        log.info("Balance is less than payment amount, it will be set to 0");
        request.setAmount(loan.getLoanBalance());
      }
      loan.setLoanBalance(loan.getLoanBalance().subtract(request.getAmount()));
      var history = new BalanceHistoryDto(0, loan.getUser().getUserId(), LocalDate.now(),
        TransactionType.LOAN_PAYMENT, request.getAmount(), "Loan payment");

      balanceHistoryService.save(history);
    } else {
      var history = new BalanceHistoryDto(0, loan.getUser().getUserId(), LocalDate.now(),
        TransactionType.LOAN_INTEREST_PAYMENT, request.getAmount(), "Loan payment interest");

      balanceHistoryService.save(history);
    }
    loanPaymentRepository.save(payment);
    loanRepository.save(loan);
      if (loan.getLoanType().getLoanTypeId() == 4) {
          log.info("Loan type is sharing, adding funds to sharing funds");
          sharingFundsService.saveFunds(request.getAmount(), FundsType.ADD);
      } else {
          log.info("Loan type is not sharing, adding funds to funds");
          fundsService.saveFunds(request.getAmount(), FundsType.ADD);
      }

  }

  public List<LoanResponse> getLoansByUser(Integer userId) {
    return loanRepository.findByUser_UserId(userId).stream()
      .map(this::mapToLoanResponse).toList();
  }

    public List<LoanResponse> getAllLoans() {
        return loanRepository.findAll().stream()
            .map(this::mapToLoanResponse).toList();
    }

  public List<Loan> getLoanByStartDate(LocalDate startDate) {

    return loanRepository.findByStartDate(startDate);
  }

  public List<LoanPaymentResponse> getPaymentsByLoan(Integer loanId) {
    return loanPaymentRepository.findByLoan_LoanId(loanId).stream()
      .map(payment -> {
        LoanPaymentResponse response = new LoanPaymentResponse();
        response.setPaymentId(payment.getPaymentId());
        response.setLoanId(payment.getLoan().getLoanId());
        response.setPaymentDate(payment.getPaymentDate());
        response.setAmount(payment.getAmount());
        response.setPaymentTypeName(payment.getPaymentType().getLoanPaymentTypeName());
        return response;
      }).toList();
  }

  private LoanResponse mapToLoanResponse(Loan loan) {
    LoanResponse response = new LoanResponse();
    response.setNumberId(loan.getUser().getNumberId());
    response.setLoanId(loan.getLoanId());
    response.setLoanAmount(loan.getLoanAmount());
    response.setInterestRate(loan.getInterestRate());
    response.setLoanBalance(loan.getLoanBalance());
    response.setStartDate(loan.getStartDate());
    response.setReason(loan.getReason());
    response.setEndDate(loan.getEndDate());
    response.setLoanTypeName(loan.getLoanType().getLoanTypeName());
    return response;
  }

}

