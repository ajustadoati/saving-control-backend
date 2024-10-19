
package com.ajustadoati.sc.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_associate")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAssociate {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "associate_id")
  private Integer associateId;


  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;


  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "user_associate_id", nullable = false)
  private User userAssociate;

  @Column(name = "relationship", length = 50)
  private String relationship;

}

