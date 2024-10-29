package com.ajustadoati.sc.adapter.rest.repository;

import com.ajustadoati.sc.domain.Saving;
import com.ajustadoati.sc.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Saving repository interface
 *
 * @author rojasric
 */
public interface SavingRepository extends JpaRepository<Saving, Long> {

  Page<Saving> getAllByUser(User user, Pageable pageable);

  @Query("SELECT e FROM Saving e WHERE (:savingDate is null OR e.savingDate = :savingDate)")
  Page<Saving> findAllBySavingDate(LocalDate savingDate, Pageable pageable);

  List<Saving> findByUserAndSavingDateBetween(User user, LocalDate startDate,
      LocalDate endDate);

  @Query("SELECT SUM(s.amount) FROM Saving s")
  BigDecimal getTotalAmount();

}
