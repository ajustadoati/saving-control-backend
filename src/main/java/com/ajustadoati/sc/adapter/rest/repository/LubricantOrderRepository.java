package com.ajustadoati.sc.adapter.rest.repository;

import com.ajustadoati.sc.domain.LubricantOrder;
import com.ajustadoati.sc.domain.enums.LubricantOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LubricantOrderRepository extends JpaRepository<LubricantOrder, Integer> {
  List<LubricantOrder> findByUser_UserIdOrderByOrderDateDesc(Integer userId);
  Optional<LubricantOrder> findBySupplyId(Integer supplyId);
  List<LubricantOrder> findByStatusInOrderByOrderDateAsc(List<LubricantOrderStatus> statuses);
  List<LubricantOrder> findByOrderDateLessThanEqualAndStatusInOrderByOrderDateAsc(LocalDate date, List<LubricantOrderStatus> statuses);
}
