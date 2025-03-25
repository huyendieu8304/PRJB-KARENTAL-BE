package com.mp.karental.service;

import com.mp.karental.constant.ERole;
import com.mp.karental.dto.request.user.AccountRegisterRequest;
import com.mp.karental.dto.request.user.EditPasswordRequest;
import com.mp.karental.dto.request.user.EditProfileRequest;
import com.mp.karental.dto.response.user.EditProfileResponse;
import com.mp.karental.dto.response.user.UserResponse;
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
import com.mp.karental.util.RedisUtil;
import jakarta.mail.MessagingException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    //TODO: sửa lại khi deploy
    @Value("${front-end.domain-url}")
    @NonFinal
    private String frontEndDomainName;

    AccountRepository accountRepository;
    UserProfileRepository userProfileRepository;
    RoleRepository roleRepository;
    WalletRepository walletRepository;

    UserMapper userMapper;

    PasswordEncoder passwordEncoder;
    FileService fileService;
    EmailService emailService;
    RedisUtil redisUtil;

    /**
     * Creates a new user account along with the associated user profile.
     *
     * @param request the account registration request containing the user's details
     * @return a {@code UserResponse} DTO containing the details of the newly created account and profile
     *
     * @author DieuTTH4
     */
    public UserResponse addNewAccount(AccountRegisterRequest request){
        log.info("Create new account");
        Account account = userMapper.toAccount(request);
        //set role for the account
        Optional<Role> role = roleRepository
                .findByName(request.getIsCustomer().equals("true") ? ERole.CUSTOMER : ERole.CAR_OWNER);
        if (role.isPresent()){
            account.setRole(role.get());
        } else {
            throw new AppException(ErrorCode.ROLE_NOT_FOUND_IN_DB);
        }
        account.setActive(false); //set status of the account, this will change after the email is verified
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

        sendVerifyEmail(account);
        log.info("Account created, email={}", account.getEmail());
        return userMapper.toUserResponse(account, userProfile);
    }

    public String resendVerifyEmail(String email){
        log.info("User request sending verify email for {}", email);
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_USED_BY_ANY_ACCOUNT));
        //account already verify
        if (account.isEmailVerified()){
            //not send email any more
            return "The email is already verified";
        }
        sendVerifyEmail(account);
        return "The verify email is sent successfully. Please check your inbox again and follow instructions to verify your email.";
    }


    private void sendVerifyEmail(Account account) {
        log.info("Send verify email to user.");
        //send email to verified user email
        String verifyEmailToken = redisUtil.generateVerifyEmailToken(account.getId());
        String confirmUrl = frontEndDomainName + "/user/verify-email?t=" + verifyEmailToken;
        log.info("Verify email url: {}", confirmUrl);
        //sending email
        try {
            emailService.sendRegisterEmail(account.getEmail(), confirmUrl);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.SEND_VERIFY_EMAIL_TO_USER_FAIL);
        }
    }

    public void verifyEmail(String verifyEmailToken){
        log.info("Verify email");
        String accountId = redisUtil.getValueOfVerifyEmailToken(verifyEmailToken);
        //check if the token valid
        if (accountId != null && !accountId.isEmpty()) {
            //token valid: exist in redis and still not expired
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_USED_BY_ANY_ACCOUNT));
            //Email is not already verify
            if (!account.isEmailVerified()){
                //set the email verified status to true
                account.setEmailVerified(true);
                //activate the account, so that user could use this account to login
                account.setActive(true);
                accountRepository.save(account);
                log.info("Email={} verified successfully", account.getEmail());
            }
        } else {
            //The verify email token is not valid or
            //has expired or
            //has been used
            log.info("Verify token invalid, can not verify email");
            throw new AppException(ErrorCode.INVALID_ONETIME_TOKEN);
        }
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
        //user upload file
        if (request.getDrivingLicense() != null) {
            String newUri = "user/" + accountID + "/driving-license" + fileService.getFileExtension(request.getDrivingLicense());
            fileService.uploadFile(request.getDrivingLicense(), newUri);
            userProfile.setDrivingLicenseUri(newUri);
        }

        // Update user profile from request
        /**
         * Mapping fields from request to user profile:
         * - ID and account are ignored as they should not be modified.
         * - drivingLicenseUri is also ignored since it's handled separately.
         * - Other fields like fullName, dob, phoneNumber, nationalId, and address details are mapped.
         * - This ensures only relevant fields are updated in the user profile.
         */
        userMapper.updateUserProfileFromRequest(request, userProfile);

        userProfileRepository.save(userProfile);

        EditProfileResponse editProfileResponse = userMapper.toEditProfileResponse(userProfile);
        editProfileResponse.setEmail(email);

        // Account entity is saved only if modifications are applied
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
        log.info("Fetching user profile");

        // Get the current logged-in user's ID and email
        String userId = SecurityUtil.getCurrentAccountId();
        String email = SecurityUtil.getCurrentEmail();

        // Retrieve the user profile from the database, throw an exception if not found
        UserProfile userProfile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB));

        /**
         * Mapping user profile entity to response DTO:
         * - Maps all required fields from the UserProfile entity.
         * - Ignores drivingLicenseUrl as it needs to be handled separately.
         * - Retrieves email from the associated account and sets it in the response.
         */
        EditProfileResponse response = userMapper.toEditProfileResponse(userProfile);
        response.setEmail(email);

        // If the user has a driving license URI, generate and set the file URL
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
        Account account = SecurityUtil.getCurrentAccount();

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