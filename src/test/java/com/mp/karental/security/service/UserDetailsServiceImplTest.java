package com.mp.karental.security.service;

import com.mp.karental.constant.ERole;
import com.mp.karental.entity.Account;
import com.mp.karental.entity.Role;
import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.repository.AccountRepository;
import com.mp.karental.security.entity.UserDetailsImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    AccountRepository accountRepository;

    @InjectMocks
    UserDetailsServiceImpl userDetailsServiceImpl;

    @Test
    void loadUserByUsername_success() {
        //Given
        String email = "test@email.com";

        Account dummyAccount = Account.builder()
                .email(email)
                .isActive(true)
                .role(Role.builder().name(ERole.CUSTOMER).build())
                .build();

        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(dummyAccount));

        //call method
        UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsServiceImpl.loadUserByUsername(email);

        //asert
        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        assertEquals(dummyAccount, userDetails.getAccount());

    }

    @Test
    void loadUserByUsername_emailNotExist() {
        String email = "test@email.com";
        when(accountRepository.findByEmail(email)).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userDetailsServiceImpl.loadUserByUsername(email));

    }

    @Test
    void loadUserByUsername_inactiveAccount() {
        //Given
        String email = "test@email.com";

        Account dummyAccount = Account.builder()
                .email(email)
                .isActive(false)
                .role(Role.builder().name(ERole.CUSTOMER).build())
                .build();

        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(dummyAccount));

        //asert
        assertThrows(InternalAuthenticationServiceException.class, () -> userDetailsServiceImpl.loadUserByUsername(email));

    }
}