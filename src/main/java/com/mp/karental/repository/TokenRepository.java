package com.mp.karental.repository;

import com.mp.karental.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, String> {

//    Optional<Token> findByAccessToken(String accessToken);
}
