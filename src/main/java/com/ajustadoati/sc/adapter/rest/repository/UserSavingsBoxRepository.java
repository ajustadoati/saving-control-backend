package com.ajustadoati.sc.adapter.rest.repository;

import com.ajustadoati.sc.domain.User;
import com.ajustadoati.sc.domain.UserSavingsBox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSavingsBoxRepository extends JpaRepository<UserSavingsBox, Integer> {

  List<UserSavingsBox> findByUser(User user);
}