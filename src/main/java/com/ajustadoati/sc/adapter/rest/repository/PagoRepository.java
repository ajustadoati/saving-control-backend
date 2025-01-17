package com.ajustadoati.sc.adapter.rest.repository;

import com.ajustadoati.sc.domain.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Integer> {

  List<Pago> findByFecha(LocalDate fecha);

}
