package org.example.hatealcohol.user.repository;

import org.example.hatealcohol.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);
}