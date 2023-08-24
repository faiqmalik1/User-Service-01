package com.voting.system.userservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserRole {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  private long userRoleId;

  @ManyToOne
  @JoinColumn(name = "userId")
  private User userId;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "roleId")
  private Role role;

}