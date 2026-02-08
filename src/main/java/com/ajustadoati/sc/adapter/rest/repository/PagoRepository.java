package com.ajustadoati.sc.adapter.rest.repository;

import com.ajustadoati.sc.application.service.dto.enums.TipoPagoEnum;
import com.ajustadoati.sc.domain.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Integer> {

  List<Pago> findByFecha(LocalDate fecha);

  List<Pago> findByFechaAndCedula(LocalDate fecha, String cedula);

  @Query("select max(p.fecha) from Pago p where function('dayofweek', p.fecha) = 4")
  Optional<LocalDate> findLatestWednesdayWithPayments();

  @Query("select max(p.fecha) from Pago p where function('dayofweek', p.fecha) = 4 and p.fecha < :date")
  Optional<LocalDate> findPreviousWednesdayWithPayments(@Param("date") LocalDate date);

  @Query("select p.tipoPago, sum(p.monto) from Pago p " +
    "where p.fecha < :date and p.tipoPago in :types group by p.tipoPago")
  List<Object[]> sumByTipoPagoBefore(@Param("date") LocalDate date,
                                     @Param("types") Collection<TipoPagoEnum> types);

}
