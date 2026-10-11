package com.ajustadoati.sc.adapter.rest.repository;

import com.ajustadoati.sc.domain.LogicBalance;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import java.util.Optional;

public interface LogicBalanceRepository extends JpaRepository<LogicBalance, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select b from LogicBalance b where b.id = 1")
  Optional<LogicBalance> findForUpdate();
}
