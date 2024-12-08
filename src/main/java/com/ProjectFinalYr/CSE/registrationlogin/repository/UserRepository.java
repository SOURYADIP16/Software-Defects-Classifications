package com.ProjectFinalYr.CSE.registrationlogin.repository;

import com.ProjectFinalYr.CSE.registrationlogin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
}
