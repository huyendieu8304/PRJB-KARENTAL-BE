package com.mp.karental.util;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RedisUtil {
    RedisTemplate<String, String> redisTemplate;

    private static final String BOOKING_SEQUENCE_KEY = "booking-sequence";
    private static final String VERIFY_EMAIL_TOKEN_PREFIX = "verify-email-tk:";

    public String generateBookingNumber() {
        Long sequence = redisTemplate.opsForValue().increment(BOOKING_SEQUENCE_KEY, 1);
        if(sequence!= null && sequence == 1){ //the first booking is placed
            //reset sequence every day
            redisTemplate.expireAt(BOOKING_SEQUENCE_KEY, new Date(System.currentTimeMillis() + 86400000));
            //TODO: TEST
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
     * verify whether the verifyEmailToken valid or not
     * @param token the token
     * @return valued of the key (aka accountId) if the key still valid (exist)
     *          or else return null
     */
    public String verifyEmailToken(String token){
        String key = VERIFY_EMAIL_TOKEN_PREFIX + token;
        String accountId = redisTemplate.opsForValue().getAndDelete(key);
        return accountId;
    }


}
