package com.ajustadoati.sc.adapter.rest.repository;

import com.ajustadoati.sc.domain.User;
import com.ajustadoati.sc.domain.UserSavingsBox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserSavingsBoxRepository extends JpaRepository<UserSavingsBox, Integer> {

  List<UserSavingsBox> findByUser(User user);

  Optional<UserSavingsBox> findTopByUser_UserIdOrderByUpdatedAtDesc(Integer userId);
}
