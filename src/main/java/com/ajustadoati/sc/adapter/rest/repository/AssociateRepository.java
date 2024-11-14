package com.ajustadoati.sc.adapter.rest.repository;

import com.ajustadoati.sc.domain.Product;
import com.ajustadoati.sc.domain.User;
import com.ajustadoati.sc.domain.UserAssociate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssociateRepository extends JpaRepository<UserAssociate, Integer> {

  List<UserAssociate> findByUser(User user);
}
