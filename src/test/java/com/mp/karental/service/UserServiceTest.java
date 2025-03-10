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
import com.mp.karental.repository.WalletRepository;
import com.mp.karental.security.SecurityUtil;
import com.mp.karental.repository.WalletRepository;
import com.mp.karental.util.RedisUtil;
import jakarta.mail.MessagingException;
import lombok.experimental.NonFinal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    @InjectMocks
    private FileService fileService;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private WalletRepository walletRepository;


    @Mock
    private EditProfileRequest editProfileRequest;

    @Mock
    private S3Presigner s3Presigner;

    @Mock
    private RedisUtil redisUtil;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserService userService;

    //TODO: suwar lai khi deploy front ent
    @Value("${front-end.domain-name}")
    @NonFinal
    private String frontEndDomainName;

    private final String VALID_TOKEN = "valid_token";
    private final String INVALID_TOKEN = "invalid_token";
    private final String ACCOUNT_ID = "12345";

    @ParameterizedTest(name = "[{index} isCustomer={0}]")
    @CsvSource({
            "true",
            "false"
    })
    void addNewAccount(String isCustomer) throws MessagingException {
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

        when(redisUtil.generateVerifyEmailToken(account.getId())).thenReturn("verifyEmailToken");

        UserResponse result = userService.addNewAccount(request);

        assertNotNull(result);
        verify(userMapper).toAccount(request);
        verify(roleRepository).findByName(expectedRole);
        verify(accountRepository).save(account);
        verify(userMapper).toUserProfile(request);
        verify(userProfileRepository).save(userProfile);
        verify(userMapper).toUserResponse(account, userProfile);
        verify(walletRepository).save(any(Wallet.class));

        //TODO: vieets laij choox nay khi noi voi front end
        String expectedUrl = frontEndDomainName + "/user/verify-email?t=verifyEmailToken";
        verify(emailService).sendRegisterEmail(account.getEmail(), expectedUrl);

        assertFalse(account.isActive(), "Account must be inactive");
        assertEquals("encodedPassword", account.getPassword(), "Password must be encoded");
    }

    @Test
    public void addNewAccount_RoleNotFound() {
        // Arrange
        AccountRegisterRequest request = new AccountRegisterRequest();
        request.setIsCustomer("true");
        request.setPassword("password");

        // mapper
        Account account = new Account();
        account.setPassword(request.getPassword()); // not encoded
        when(userMapper.toAccount(request)).thenReturn(account);

        // Mock role not found (Optional.empty())
        when(roleRepository.findByName(ERole.CUSTOMER)).thenReturn(Optional.empty());

        // Act & Assert:
        AppException exception = assertThrows(AppException.class, () -> {
            userService.addNewAccount(request);
        });

        assertEquals(ErrorCode.ROLE_NOT_FOUND_IN_DB, exception.getErrorCode());

        // Verify methods befor exception thrown
        verify(userMapper).toAccount(request);
        verify(roleRepository).findByName(ERole.CUSTOMER);

        //these method below should not be called
        verify(accountRepository, times(0)).save(account);
    }

    @Test
    void resendVerifyEmail_ShouldSendEmail_WhenEmailNotVerified() throws MessagingException {
        // Given
        String email = "test@example.com";
        Account account = new Account();
        account.setId(ACCOUNT_ID);
        account.setEmail(email);
        account.setEmailVerified(false);

        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(account));
        when(redisUtil.generateVerifyEmailToken(account.getId())).thenReturn("mock-token");

        // When
        String result = userService.resendVerifyEmail(email);

        // Then
        String expectedUrl = frontEndDomainName + "/user/verify-email?t=mock-token";
        assertEquals("The verify email is sent successfully. Please check your inbox again and follow instructions to verify your email.", result);
        verify(emailService).sendRegisterEmail(email, expectedUrl);
    }

    @Test
    void resendVerifyEmail_ShouldNotSendEmail_WhenEmailAlreadyVerified() throws MessagingException {
        // Given
        String email = "test@example.com";
        Account account = new Account();
        account.setEmail(email);
        account.setEmailVerified(true);

        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(account));

        // When
        String result = userService.resendVerifyEmail(email);

        // Then
        assertEquals("The email is already verified", result);
        verify(emailService, never()).sendRegisterEmail(any(), any());
    }

    @Test
    void resendVerifyEmail_ShouldThrowException_WhenEmailNotFound() throws MessagingException {
        // Given
        String email = "notfound@example.com";
        when(accountRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When + Then
        AppException exception = assertThrows(AppException.class,
                () -> userService.resendVerifyEmail(email));

        assertEquals(ErrorCode.EMAIL_NOT_USED_BY_ANY_ACCOUNT, exception.getErrorCode());
        verify(emailService, never()).sendRegisterEmail(any(), any());
    }

    @Test
    void resendVerifyEmail_ShouldThrowException_WhenSendEmailFails() throws MessagingException {
        // Given
        String email = "test@example.com";
        Account account = new Account();
        account.setId(ACCOUNT_ID);
        account.setEmail(email);
        account.setEmailVerified(false);

        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(account));
        when(redisUtil.generateVerifyEmailToken(account.getId())).thenReturn("mock-token");

        // Giả lập lỗi khi gửi email
        doThrow(new MessagingException("Email sending failed"))
                .when(emailService).sendRegisterEmail(eq(email), anyString());

        // When + Then
        AppException exception = assertThrows(AppException.class,
                () -> userService.resendVerifyEmail(email));

        assertEquals(ErrorCode.SEND_VERIFY_EMAIL_TO_USER_FAIL, exception.getErrorCode());

        // Đảm bảo đã gọi hàm tạo token và gửi email
        verify(redisUtil).generateVerifyEmailToken(account.getId());
        verify(emailService).sendRegisterEmail(eq(email), anyString());
    }

    @Test
    void verifyEmail_WhenTokenIsValid_ShouldVerifyEmailSuccessfully() {
        // Arrange
        Account account = new Account();
        account.setId(ACCOUNT_ID);
        account.setEmail("test@example.com");
        account.setEmailVerified(false);
        account.setActive(false);

        when(redisUtil.getValueOfVerifyEmailToken(VALID_TOKEN)).thenReturn(ACCOUNT_ID);
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));

        // Act
        userService.verifyEmail(VALID_TOKEN);

        // Assert
        assertTrue(account.isEmailVerified());
        assertTrue(account.isActive());
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void verifyEmail_WhenTokenIsValidButAccountNotFound_ShouldThrowException() {
        // Arrange
        Account account = new Account();
        account.setId(ACCOUNT_ID);
        account.setEmail("test@example.com");
        account.setEmailVerified(false);
        account.setActive(false);

        when(redisUtil.getValueOfVerifyEmailToken(VALID_TOKEN)).thenReturn(ACCOUNT_ID);
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            userService.verifyEmail(VALID_TOKEN);
        });
        assertEquals(ErrorCode.EMAIL_NOT_USED_BY_ANY_ACCOUNT, exception.getErrorCode());
    }

    @Test
    void verifyEmail_WhenEmailAlreadyVerified_ShouldNotChangeAnything() {
        // Arrange
        Account account = new Account();
        account.setId(ACCOUNT_ID);
        account.setEmail("test@example.com");
        account.setEmailVerified(false);
        account.setActive(false);
        account.setEmailVerified(true);
        account.setActive(true);

        when(redisUtil.getValueOfVerifyEmailToken(VALID_TOKEN)).thenReturn(ACCOUNT_ID);
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));

        // Act
        userService.verifyEmail(VALID_TOKEN);

        // Assert
        assertTrue(account.isEmailVerified());
        assertTrue(account.isActive());
        // Không lưu lại vì không có gì thay đổi
        verify(accountRepository, never()).save(account);
    }

    @Test
    void verifyEmail_WhenTokenIsInvalid_ShouldThrowInvalidTokenException() {
        // Arrange
        when(redisUtil.getValueOfVerifyEmailToken(INVALID_TOKEN)).thenReturn(null);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            userService.verifyEmail(INVALID_TOKEN);
        });
        assertEquals(ErrorCode.INVALID_ONETIME_TOKEN, exception.getErrorCode());

        // Không có hành động nào trên repository
        verify(accountRepository, never()).findById(any());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void editPassword_IncorrectCurrentPassword() {
        EditPasswordRequest request = new EditPasswordRequest();
        request.setCurrentPassword("wrongPassword");
        request.setNewPassword("newSecurePassword");

        String accountId = "12345";
        Account account = new Account();
        account.setId(accountId);
        account.setPassword("encodedOldPassword");

        // Mock static method của SecurityUtil
        try (MockedStatic<SecurityUtil> mockedStatic = Mockito.mockStatic(SecurityUtil.class)) {
            mockedStatic.when(SecurityUtil::getCurrentAccountId).thenReturn(accountId);

            when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
            when(passwordEncoder.matches(request.getCurrentPassword(), account.getPassword())).thenReturn(false);

            // Act & Assert
            AppException exception = assertThrows(AppException.class, () -> userService.editPassword(request));
            assertEquals(ErrorCode.INCORRECT_PASSWORD, exception.getErrorCode());
        }
    }

    @Test
    void editProfile_UserNotFound() {
        String accountId = "12345";

        try (MockedStatic<SecurityUtil> mockedStatic = Mockito.mockStatic(SecurityUtil.class)) {
            mockedStatic.when(SecurityUtil::getCurrentAccountId).thenReturn(accountId);

            when(userProfileRepository.findById(accountId)).thenReturn(Optional.empty());

            // Act & Assert
            AppException exception = assertThrows(AppException.class, () -> userService.editProfile(new EditProfileRequest()));
            assertEquals(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB, exception.getErrorCode());
        }
    }

    @Test
    void editPassword_Success() {
        EditPasswordRequest request = new EditPasswordRequest();
        request.setCurrentPassword("oldPassword");
        request.setNewPassword("newSecurePassword");

        String accountId = "12345";
        Account account = new Account();
        account.setId(accountId);
        account.setPassword("encodedOldPassword");

        try (MockedStatic<SecurityUtil> mockedStatic = Mockito.mockStatic(SecurityUtil.class)) {
            mockedStatic.when(SecurityUtil::getCurrentAccountId).thenReturn(accountId);

            when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
            when(passwordEncoder.matches(request.getCurrentPassword(), account.getPassword())).thenReturn(true);
            when(passwordEncoder.encode(request.getNewPassword())).thenReturn("encodedNewPassword");

            // Act
            userService.editPassword(request);

            // Assert
            assertEquals("encodedNewPassword", account.getPassword());
            verify(accountRepository).save(account);
        }
    }

    @Test
    void getUserProfile_UserNotFound() {
        String userId = "12345";

        try (MockedStatic<SecurityUtil> mockedStatic = Mockito.mockStatic(SecurityUtil.class)) {
            mockedStatic.when(SecurityUtil::getCurrentAccountId).thenReturn(userId);

            when(userProfileRepository.findById(userId)).thenReturn(Optional.empty());

            AppException exception = assertThrows(AppException.class, () -> userService.getUserProfile());
            assertEquals(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB, exception.getErrorCode());

            verify(userProfileRepository).findById(userId);
        }
    }


    @Test
    void getUserProfile_Success() {
        String userId = "12345";
        String email = "test@example.com";
        UserProfile userProfile = new UserProfile();

        try (MockedStatic<SecurityUtil> mockedStatic = Mockito.mockStatic(SecurityUtil.class)) {
            mockedStatic.when(SecurityUtil::getCurrentAccountId).thenReturn(userId);
            mockedStatic.when(SecurityUtil::getCurrentEmail).thenReturn(email);

            when(userProfileRepository.findById(userId)).thenReturn(Optional.of(userProfile));
            EditProfileResponse expectedResponse = new EditProfileResponse();
            when(userMapper.toEditProfileResponse(userProfile)).thenReturn(expectedResponse);

            EditProfileResponse result = userService.getUserProfile();

            assertNotNull(result);
            verify(userProfileRepository).findById(userId);
            verify(userMapper).toEditProfileResponse(userProfile);
        }
    }

    @Test
    void editPassword_InvalidNewPassword2() {
        EditPasswordRequest request = new EditPasswordRequest();
        request.setCurrentPassword("currentPassword");
        request.setNewPassword("");

        String accountId = "12345";
        Account account = new Account();
        account.setId(accountId);
        account.setPassword("encodedOldPassword");

        try (MockedStatic<SecurityUtil> mockedStatic = Mockito.mockStatic(SecurityUtil.class)) {
            mockedStatic.when(SecurityUtil::getCurrentAccountId).thenReturn(accountId);

            when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

            AppException exception = assertThrows(AppException.class, () -> userService.editPassword(request));
        }
    }

    @Test
    void editPassword_InvalidNewPassword() {
        EditPasswordRequest request = new EditPasswordRequest();
        request.setCurrentPassword("currentPassword");
        request.setNewPassword("");

        String accountId = "12345";
        Account account = new Account();
        account.setId(accountId);
        account.setPassword("encodedOldPassword");

        try (MockedStatic<SecurityUtil> mockedStatic = Mockito.mockStatic(SecurityUtil.class)) {
            mockedStatic.when(SecurityUtil::getCurrentAccountId).thenReturn(accountId);
            when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

            AppException exception = assertThrows(AppException.class, () -> userService.editPassword(request));
        }
    }
}