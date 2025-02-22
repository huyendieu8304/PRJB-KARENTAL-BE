package com.mp.karental.security.service;

import com.mp.karental.entity.RefreshToken;
import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.repository.AccountRepository;
import com.mp.karental.repository.RefreshTokenRepository;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class RefreshTokenService {

    @Value("${application.security.jwt.refresh-token-expiration}")
    @NonFinal
    private long refreshTokenExpiration;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private AccountRepository accountRepository;

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken createRefreshToken(String accountId) {
        //only allow the user login in a client at the same time
        //delete the exist refresh token of user in db
        //TODO: tối ưu lại hàm này
        refreshTokenRepository.deleteByAccount(accountRepository.findById(accountId).get());
        //create an entity to save to the db
        RefreshToken refreshToken = RefreshToken.builder()
                //TODO: this is Optional Object
                .account(accountRepository.findById(accountId).get())
                .expiryDate(Instant.now().plusMillis(refreshTokenExpiration))
                .token(UUID.randomUUID().toString())
                .build();

        refreshToken = refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    public void verifyExpiration(RefreshToken refreshToken) {
        if(refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new AppException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }
    }


    public void deleteRefreshToken(String token){
        refreshTokenRepository.delete(refreshTokenRepository.findByToken(token).get());
    }


}
