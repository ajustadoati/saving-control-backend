
package com.ajustadoati.sc.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "associate")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Associate {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "associate_id")
  private Integer associateId;

  @ManyToOne
  @JoinColumn(name = "main_associate_id", nullable = false)
  private User mainAssociate;

  @ManyToOne
  @JoinColumn(name = "guess_associate_id", nullable = false)
  private User guessAssociate;

  @Column(name = "relationship", length = 50)
  private String relationship;
}

