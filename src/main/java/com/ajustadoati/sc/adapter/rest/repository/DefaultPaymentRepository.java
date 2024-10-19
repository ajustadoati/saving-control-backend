package com.ajustadoati.sc.adapter.rest.repository;

import com.ajustadoati.sc.domain.DefaultPayment;
import com.ajustadoati.sc.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface DefaultPaymentRepository extends JpaRepository<DefaultPayment, Integer> {

  Page<DefaultPayment> getAllByUser(User user, Pageable pageable);

}
