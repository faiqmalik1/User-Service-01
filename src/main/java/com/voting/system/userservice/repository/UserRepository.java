package com.voting.system.userservice.repository;

import com.voting.system.userservice.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  @Query("SELECT u FROM User u INNER JOIN u.userRoles ur INNER JOIN ur.role r WHERE r.roleName = :roleName")
  Page<User> findAllByRoleName(String roleName, Pageable pageable);

  User findByUserCNICAndPassword(String userCNIC, String password);

  Optional<User> findByUserCNIC(String userCNIC);

}
