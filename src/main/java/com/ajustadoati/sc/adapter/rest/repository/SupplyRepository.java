package com.ajustadoati.sc.adapter.rest.repository;

import com.ajustadoati.sc.domain.Supply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface SupplyRepository extends JpaRepository<Supply, Integer> {
  List<Supply> findByUser_UserId(Integer userId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select s from Supply s where s.user.userId = :userId order by s.supplyId")
  List<Supply> lockByUserId(@org.springframework.data.repository.query.Param("userId") Integer userId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select s from Supply s where s.supplyId = :id")
  Optional<Supply> findForPaymentById(@org.springframework.data.repository.query.Param("id") Integer id);
}
