package com.mp.karental.service;

import com.mp.karental.constant.ERole;
import com.mp.karental.dto.request.AccountRegisterRequest;
import com.mp.karental.dto.request.EditProfileRequest;
import com.mp.karental.dto.response.EditProfileResponse;
import com.mp.karental.dto.response.UserResponse;
import com.mp.karental.entity.Account;
import com.mp.karental.entity.Role;
import com.mp.karental.entity.UserProfile;
import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.mapper.UserMapper;
import com.mp.karental.repository.AccountRepository;
import com.mp.karental.repository.RoleRepository;
import com.mp.karental.repository.UserProfileRepository;
import com.mp.karental.security.SecurityUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

/**
 * Service class for handling user account operations.
 *
 * @author DieuTTH4
 *
 * @version 1.0
 */
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

    /**
     * Creates a new user account along with the associated user profile.
     *
     * @param request the account registration request containing the user's details
     * @return a {@code UserResponse} DTO containing the details of the newly created account and profile
     *
     * @author DieuTTH4
     *
     * @version 1.0
     */
    public UserResponse addNewAccount(AccountRegisterRequest request) {
        Account account = userMapper.toAccount(request);
        //set role for the account
        Optional<Role> role = roleRepository
                .findByName(request.getIsCustomer().equals("true") ? ERole.CUSTOMER : ERole.CAR_OWNER);
        if (role.isPresent()){
            account.setRole(role.get());
        } else {
            throw new AppException(ErrorCode.ROLE_NOT_FOUND_IN_DB);
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

    /**
     * Edits an existing user profile.
     */
    @Transactional
    public EditProfileResponse editProfile(EditProfileRequest request, MultipartFile drivingLicense) {
        String userId = SecurityUtil.getCurrentAccountId();
        UserProfile userProfile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB));

        // Cập nhật thông tin từ request
        userProfile.setFullName(request.getFullName());
        userProfile.setDob(request.getDob());
        userProfile.setPhoneNumber(request.getPhoneNumber());
        userProfile.setNationalId(request.getNationalId());
        userProfile.setAddress(request.getAddress());
        userProfile.setCityProvince(request.getCityProvince());
        userProfile.setDistrict(request.getDistrict());
        userProfile.setWard(request.getWard());
        userProfile.setHouseNumberStreet(request.getHouseNumberStreet());

        // Xử lý ảnh giấy phép lái xe
        if (drivingLicense != null && !drivingLicense.isEmpty()) {
            String fileUrl = FileService.upload(drivingLicense);
            userProfile.setDrivingLicense(fileUrl);
        }

        userProfileRepository.save(userProfile);
        return userMapper.toUserProfileResponse(userProfile);
    }
}