package com.mp.karental.service;

import com.mp.karental.constant.ERole;
import com.mp.karental.dto.request.AccountRegisterRequest;
import com.mp.karental.dto.response.UserResponse;
import com.mp.karental.entity.Account;
import com.mp.karental.entity.Role;
import com.mp.karental.entity.UserProfile;
import com.mp.karental.mapper.UserMapper;
import com.mp.karental.repository.AccountRepository;
import com.mp.karental.repository.RoleRepository;
import com.mp.karental.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
/**
 * This is a class used to test UserService
 *
 * @author DieuTTH4
 *
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;


    @ParameterizedTest(name = "[{index} isCustomer={0}]")
    @CsvSource({
            "true",
            "false"
    })
    void addNewAccount(String isCustomer) {
        AccountRegisterRequest request = new AccountRegisterRequest();
        request.setIsCustomer(isCustomer);
        request.setPassword("password");

        //mock object created after mapper toAccount
        Account account = new Account();
        account.setPassword(request.getPassword()); //after mapper, this still not encoded
        UserProfile userProfile = new UserProfile();

        ERole expectedRole = isCustomer.equals("true") ? ERole.CUSTOMER : ERole.CAR_OWNER;
        Role role = new Role();
        role.setName(expectedRole);

        UserResponse userResponse = new UserResponse();

        when(userMapper.toAccount(request)).thenReturn(account);
        when(roleRepository.findByName(expectedRole)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(accountRepository.save(account)).thenReturn(account);

        when(userMapper.toUserProfile(request)).thenReturn(userProfile);
        when(userProfileRepository.save(userProfile)).thenReturn(userProfile);
        when(userMapper.toUserResponse(account, userProfile)).thenReturn(userResponse);

        UserResponse result = userService.addNewAccount(request);

        assertNotNull(result);
        verify(userMapper).toAccount(request);
        verify(roleRepository).findByName(expectedRole);
        verify(accountRepository).save(account);
        verify(userMapper).toUserProfile(request);
        verify(userProfileRepository).save(userProfile);
        verify(userMapper).toUserResponse(account, userProfile);

        assertTrue(account.isActive(), "Account must be active");
        assertEquals("encodedPassword", account.getPassword(), "Password must be encoded");


    }
}