package com.ajustadoati.sc.application.service;

import com.ajustadoati.sc.adapter.rest.dto.request.LoanPaymentRequest;
import com.ajustadoati.sc.adapter.rest.dto.request.LoanRequest;
import com.ajustadoati.sc.adapter.rest.dto.response.LoanResponse;
import com.ajustadoati.sc.adapter.rest.repository.LoanPaymentRepository;
import com.ajustadoati.sc.adapter.rest.repository.LoanPaymentTypeRepository;
import com.ajustadoati.sc.adapter.rest.repository.LoanRepository;
import com.ajustadoati.sc.adapter.rest.repository.LoanTypeRepository;
import com.ajustadoati.sc.domain.Loan;
import com.ajustadoati.sc.domain.LoanPayment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanService {

  private final LoanRepository loanRepository;
  private final LoanPaymentRepository loanPaymentRepository;
  private final LoanTypeRepository loanTypeRepository;
  private final LoanPaymentTypeRepository loanPaymentTypeRepository;
  private final UserService userService;

  public LoanResponse createLoan(LoanRequest request) {
    var loanType = loanTypeRepository.findById(request.getLoanTypeId())
      .orElseThrow(() -> new IllegalArgumentException("Invalid Loan Type"));
    var user =  userService.getUserById(request.getUserId());

    Loan loan = new Loan();
    loan.setUser(user);
    loan.setLoanAmount(request.getLoanAmount());
    loan.setInterestRate(request.getInterestRate());
    loan.setLoanBalance(request.getLoanBalance());
    loan.setStartDate(request.getStartDate());
    loan.setEndDate(request.getEndDate());
    loan.setLoanType(loanType);

    loan = loanRepository.save(loan);

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

    if ("Abono".equals(paymentType.getLoanPaymentTypeName())) {
      loan.setLoanBalance(loan.getLoanBalance().subtract(request.getAmount()));
    }

    loanPaymentRepository.save(payment);
    loanRepository.save(loan);
  }

  public List<LoanResponse> getLoansByUser(Integer userId) {
    return loanRepository.findByUser_UserId(userId).stream()
      .map(this::mapToLoanResponse).toList();
  }

  private LoanResponse mapToLoanResponse(Loan loan) {
    LoanResponse response = new LoanResponse();
    response.setLoanId(loan.getLoanId());
    response.setLoanAmount(loan.getLoanAmount());
    response.setInterestRate(loan.getInterestRate());
    response.setLoanBalance(loan.getLoanBalance());
    response.setStartDate(loan.getStartDate());
    response.setEndDate(loan.getEndDate());
    response.setLoanTypeName(loan.getLoanType().getLoanTypeName());
    return response;
  }

}

