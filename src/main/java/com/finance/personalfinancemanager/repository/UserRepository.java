package com.finance.personalfinancemanager.repository;

import com.finance.personalfinancemanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}