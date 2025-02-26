package com.ajustadoati.sc.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_id")
  private Integer userId;

  private String firstName;

  private String lastName;

  private String numberId;

  private String mobileNumber;

  private String email;

  private Instant createdAt;

  private String password;

  private String company;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "user_role",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Set<Role> roles;

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  private List<UserAssociate> associates;

  @OneToMany(mappedBy = "user")
  private List<DefaultPayment> defaultPayments;

  @OneToMany(mappedBy = "user")
  private List<Saving> savings;

  @OneToMany(mappedBy = "user")
  private List<ContributionPayment> contributionPayments;

  @OneToOne(mappedBy = "user")
  private UserAccountSummary userAccountSummary;

  @OneToMany(mappedBy = "user")
  private List<BalanceHistory> balanceHistories;


}
