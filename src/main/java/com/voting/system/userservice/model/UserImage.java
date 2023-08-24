package com.voting.system.userservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserImage {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  private long userImageId;

  @OneToOne
  @JoinColumn(name = "userId")
  private User user;

  @Lob
  @Column(columnDefinition = "LONGBLOB")
  private byte[] image;

}