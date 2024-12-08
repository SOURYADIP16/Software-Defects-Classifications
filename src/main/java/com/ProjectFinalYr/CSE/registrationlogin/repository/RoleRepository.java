package com.ProjectFinalYr.CSE.registrationlogin.repository;

import com.ProjectFinalYr.CSE.registrationlogin.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);
}
