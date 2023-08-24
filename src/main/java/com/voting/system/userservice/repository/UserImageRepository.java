package com.voting.system.userservice.repository;

import com.voting.system.userservice.model.User;
import com.voting.system.userservice.model.UserImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserImageRepository extends JpaRepository<UserImage, Long> {

  Optional<UserImage> findByUser(User user);
}
