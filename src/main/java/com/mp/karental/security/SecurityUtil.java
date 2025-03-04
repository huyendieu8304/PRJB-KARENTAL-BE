package com.mp.karental.security;

import com.mp.karental.entity.Account;
import com.mp.karental.security.entity.UserDetailsImpl;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * This is a util class
 * <p>
 *     current purpose of this class just is get out the information of current authenticated user
 * </p>
 * @author DieuTTH4
 *
 * @version 1.0
 */
public class SecurityUtil {
    /**
     * Use this method to get the current login user
     * @return an UserDetailsImplement, where the information like accountId, or email could be extract from it
     */
    private static UserDetailsImpl getCurrentUser() {
        return (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    /**
     * Use this method to the  account id of current login user
     * @return a String of accountId
     */
    public static String getCurrentAccountId(){
        return getCurrentUser().getAccoutnId();
    }

    /**
     * Use this method to the email of current loggin user
     * @return a String of user's email
     */
    public static String getCurrentEmail(){
        return getCurrentUser().getEmail();
    }

    public static Account getCurrentAccount(){
        return getCurrentUser().getAccount();
    }
}
