package com.mp.karental.security.service;

import com.mp.karental.entity.Account;
import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    AccountRepository accountRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        //TODO: check whether this exception is catched
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_LOGIN_INFORMATION));

        if (!account.isActive()){
            //the account of user has banned
            throw new AppException(ErrorCode.ACCOUNT_IS_INACTIVE);
        }

        //build UserDetails object
        return UserDetailsImpl.build(account);
    }
}
