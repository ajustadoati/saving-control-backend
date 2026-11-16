package com.ajustadoati.sc.adapter.rest.repository;

import com.ajustadoati.sc.domain.DefaultPayment;
import com.ajustadoati.sc.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface DefaultPaymentRepository extends JpaRepository<DefaultPayment, Integer> {

  @Query("select count(d) from DefaultPayment d where d.user = :user and lower(trim(d.paymentName)) like 'suministro%'")
  long countLegacySupplyDefaults(@Param("user") User user);

  Optional<DefaultPayment> findByIdAndUser_UserId(Integer id, Integer userId);

  Page<DefaultPayment> getAllByUser(User user, Pageable pageable);

}
