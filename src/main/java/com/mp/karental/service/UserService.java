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
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    AccountRepository accountRepository;
    UserProfileRepository userProfileRepository;
    RoleRepository roleRepository;

    UserMapper userMapper;

    PasswordEncoder passwordEncoder;

    public UserResponse addNewAccount(AccountRegisterRequest request) {
        Account account = userMapper.toAccount(request);
        //set role for the account
        Optional<Role> role = roleRepository
                .findByName(request.isCustomer() ? ERole.CUSTOMER : ERole.CAR_OWNER);
        if (role.isPresent()){
            account.setRole(role.get());
        } else {
//
        }
        account.setActive(true); //set status of the account
        //encode password
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        account = accountRepository.save(account);

        //save the profile of the user to the db
        UserProfile userProfile = userMapper.toUserProfile(request);
        userProfile.setAccount(account);
        userProfile = userProfileRepository.save(userProfile);

        return userMapper.toUserResponse(account, userProfile);
    }
}
