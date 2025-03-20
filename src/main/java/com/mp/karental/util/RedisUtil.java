package com.mp.karental.util;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RedisUtil {
    RedisTemplate<String, String> redisTemplate;

    private static final String BOOKING_SEQUENCE_KEY = "booking-sequence";
    private static final String PENDING_DEPOSIT_BOOKING_KEY = "booking:";
    private static final String VERIFY_EMAIL_TOKEN_PREFIX = "verify-email-tk:";
    private static final String FORGOT_PASSWORD_TOKEN_PREFIX = "forgot-password-tk:";

    public String generateBookingNumber() {
        Long sequence = redisTemplate.opsForValue().increment(BOOKING_SEQUENCE_KEY, 1);
        if(Objects.equals(sequence, 1L)){ //the first booking is placed
            //reset sequence every day
            redisTemplate.expireAt(BOOKING_SEQUENCE_KEY, new Date(System.currentTimeMillis() + 86400000));
            //TODO: FOR TEST ONLY
//            redisTemplate.expire(BOOKING_SEQUENCE_KEY, 1, TimeUnit.MINUTES);
        }

        //get current date in form yyyyMMdd
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());

        return date + "-" + String.format("%08d", sequence);
    }

    public String generateVerifyEmailToken(String accountId){
        String token = UUID.randomUUID().toString();
        String key = VERIFY_EMAIL_TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(key, accountId, 30, TimeUnit.MINUTES);
        //TODO: test
//        redisTemplate.opsForValue().set(key, accountId, 10, TimeUnit.SECONDS);
        return token;
    }

    /**
     * get the value in the Email
     * @param token the token
     * @return valued of the key (aka accountId) if the key still valid (exist)
     *          or else return null
     */
    public String getValueOfVerifyEmailToken(String token){
        String key = VERIFY_EMAIL_TOKEN_PREFIX + token;
        String accountId = redisTemplate.opsForValue().getAndDelete(key);
        return accountId;
    }


    public String generateForgotPasswordToken(String accountId){
        String token = UUID.randomUUID().toString();
        String key = FORGOT_PASSWORD_TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(key, accountId, 24, TimeUnit.HOURS);
        //TODO: test
//        redisTemplate.opsForValue().set(key, accountId, 20, TimeUnit.SECONDS);
        return token;
    }

    public String getValueOfForgotPasswordToken(String token){
        String key = FORGOT_PASSWORD_TOKEN_PREFIX + token;
        return redisTemplate.opsForValue().get(key); //accountId
    }

   public void deleteForgotPasswordToken(String token){
        String key = FORGOT_PASSWORD_TOKEN_PREFIX + token;
        redisTemplate.delete(key);
   }

   public void cachePendingDepositBooking(String bookingNumber){
        String key = PENDING_DEPOSIT_BOOKING_KEY + bookingNumber;
//       redisTemplate.opsForValue().set(key, key, 1, TimeUnit.HOURS);
       redisTemplate.opsForValue().set(key, key, 20, TimeUnit.SECONDS);
   }

   public void removeCachePendingDepositBooking(String bookingNumber){
        String key = PENDING_DEPOSIT_BOOKING_KEY + bookingNumber;
        redisTemplate.delete(key);
   }

}
