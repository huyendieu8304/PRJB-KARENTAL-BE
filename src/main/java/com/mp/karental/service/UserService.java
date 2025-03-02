package com.mp.karental.service;

import com.mp.karental.constant.ERole;
import com.mp.karental.dto.request.AccountRegisterRequest;
import com.mp.karental.dto.request.EditPasswordRequest;
import com.mp.karental.dto.request.EditProfileRequest;
import com.mp.karental.dto.response.EditProfileResponse;
import com.mp.karental.dto.response.UserResponse;
import com.mp.karental.entity.Account;
import com.mp.karental.entity.Role;
import com.mp.karental.entity.UserProfile;
import com.mp.karental.entity.Wallet;
import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.mapper.UserMapper;
import com.mp.karental.repository.AccountRepository;
import com.mp.karental.repository.RoleRepository;
import com.mp.karental.repository.UserProfileRepository;
import com.mp.karental.security.SecurityUtil;
import com.mp.karental.repository.WalletRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    WalletRepository walletRepository;

    UserMapper userMapper;

    PasswordEncoder passwordEncoder;
    private final FileService fileService;

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

        //create user's wallet
        Wallet wallet = Wallet.builder()
                .account(account)
                .balance(0)
                .build();
        walletRepository.save(wallet);

        return userMapper.toUserResponse(account, userProfile);
    }

    /**
     * Edits an existing user profile.
     *
     * @param request the updated profile information
     * @return the updated profile response
     */
    public EditProfileResponse editProfile(EditProfileRequest request) {
        log.info("Editing profile for user with phone number: {}", request.getPhoneNumber());

        String accountID = SecurityUtil.getCurrentAccountId();
        String email = SecurityUtil.getCurrentEmail();

        UserProfile userProfile = userProfileRepository.findById(accountID)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB));

        Account account = userProfile.getAccount();
        if (account == null) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB);
        }

        // Check phoneNumber: If diff old value then check duplicate. Else update
        if (!request.getPhoneNumber().equals(userProfile.getPhoneNumber())) {
            if (userProfileRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                throw new AppException(ErrorCode.NOT_UNIQUE_PHONE_NUMBER);
            }
            userProfile.setPhoneNumber(request.getPhoneNumber());
        }

        // Check nationalId: If diff old value then check duplicate. Else update
        if (!request.getNationalId().equals(userProfile.getNationalId())) {
            if (userProfileRepository.existsByNationalId(request.getNationalId())) {
                throw new AppException(ErrorCode.NOT_UNIQUE_NATIONAL_ID);
            }
            userProfile.setNationalId(request.getNationalId());
        }

        // Check and upload file when have new file. Else not update (not change uri)
        if (request.getDrivingLicense() != null) {
            String newUri = "user/" + accountID + "/driving-license";

            if (userProfile.getDrivingLicenseUri() == null ||
                    !userProfile.getDrivingLicenseUri().equals(newUri)) {
                fileService.uploadFile(request.getDrivingLicense(), newUri);
                userProfile.setDrivingLicenseUri(newUri);
            }
        }


        userMapper.updateUserProfileFromRequest(request, userProfile);

//        // Upload file
//        if (request.getDrivingLicense() != null) {
//            String drivingLicenseUri = "user/" + accountID + "/driving-license";
//            fileService.uploadFile(request.getDrivingLicense(), drivingLicenseUri);
//            userProfile.setDrivingLicenseUri(drivingLicenseUri);
//        }

        userProfileRepository.save(userProfile);
        accountRepository.save(account);

        EditProfileResponse editProfileResponse = userMapper.toEditProfileResponse(userProfile);
        editProfileResponse.setEmail(email);

        if (userProfile.getDrivingLicenseUri() != null) {
            editProfileResponse.setDrivingLicenseUrl(fileService.getFileUrl(userProfile.getDrivingLicenseUri()));
        }

        return editProfileResponse;
    }


    /**
     * Retrieves the profile of the current user.
     *
     * @return {@code EditProfileResponse} containing user profile details.
     * @throws AppException if the user profile is not found in the database.
     */
    public EditProfileResponse getUserProfile() {
        String userId = SecurityUtil.getCurrentAccountId();
        String email = SecurityUtil.getCurrentEmail();

        UserProfile userProfile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB));

        EditProfileResponse response = userMapper.toEditProfileResponse(userProfile);
        response.setEmail(email);

        if (userProfile.getDrivingLicenseUri() != null) {
            response.setDrivingLicenseUrl(fileService.getFileUrl(userProfile.getDrivingLicenseUri()));
        }

        return response;
    }

    /**
     * Updates the password for the currently authenticated user.
     *
     * @param request the request containing the current password, new password, and confirmation password
     * @throws AppException if the account is not found, the current password is incorrect, or the new password is invalid
     */
    public void editPassword(EditPasswordRequest request) {
        // Get information of current password
        String accountID = SecurityUtil.getCurrentAccountId();
        Account account = accountRepository.findById(accountID)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB));

        // Confirm current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), account.getPassword())) {
            throw new AppException(ErrorCode.INCORRECT_PASSWORD);
        }

        // Check new password not null
        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

        // Encode and update new password
        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);
    }



}