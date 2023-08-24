package com.voting.system.userservice.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class User implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  private long userId;
  private String userName;
  private String userCNIC;
  private String password;
  private String email;
  private Date createdAt;

  @OneToOne(mappedBy = "user", fetch = FetchType.EAGER)
  private UserImage userImage;

  @OneToMany(mappedBy = "userId", fetch = FetchType.EAGER)
  private Set<UserRole> userRoles;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    Set<GrantedAuthority> authorities = new HashSet<>();
    for (UserRole userRole : userRoles) {
      authorities.add(new SimpleGrantedAuthority(userRole.getRole().getRoleName()));
    }
    return authorities;
  }

  public String getUserName() {
    return userName;
  }

  @Override
  public String getUsername() {
    return Long.toString(userId);
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}