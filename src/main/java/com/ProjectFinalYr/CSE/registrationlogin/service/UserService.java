package com.ProjectFinalYr.CSE.registrationlogin.service;

import com.ProjectFinalYr.CSE.registrationlogin.dto.UserDto;
import com.ProjectFinalYr.CSE.registrationlogin.entity.User;

import java.util.List;

public interface UserService {
    void saveUser(UserDto userDto);

    User findByEmail(String email);

    List<UserDto> findAllUsers();
}
