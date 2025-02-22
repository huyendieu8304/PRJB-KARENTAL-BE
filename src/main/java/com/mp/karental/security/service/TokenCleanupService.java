package com.mp.karental.security.service;

import com.mp.karental.repository.InvalidateAccessTokenRepo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class TokenCleanupService {
    InvalidateAccessTokenRepo invalidateAccessTokenRepo;

//    @Scheduled(cron = "0 0 * * * *") //every hour
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanExpiredTokens() {
        Date now = new Date();
        invalidateAccessTokenRepo.deleteByExpiresAtBefore(now);
        //TODO: xoa sout
        System.out.println("Đã xóa token hết hạn trước: " + now);
    }
}
