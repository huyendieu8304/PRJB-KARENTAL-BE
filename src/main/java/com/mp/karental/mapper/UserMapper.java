
package com.mp.karental.mapper;
import com.mp.karental.dto.request.AccountRegisterRequest;
import com.mp.karental.dto.request.AddCarRequest;
import com.mp.karental.dto.request.EditProfileRequest;
import com.mp.karental.dto.response.CarResponse;
import com.mp.karental.dto.response.EditProfileResponse;
import com.mp.karental.dto.response.UserResponse;
import com.mp.karental.entity.Account;
import com.mp.karental.entity.Car;
import com.mp.karental.entity.UserProfile;
import com.mp.karental.service.FileService;
import org.apache.poi.ss.formula.functions.T;
import org.mapstruct.*;
/**
 * Mapper interface for converting between user-related DTOs and entities.
 *
 * @author DieuTTH4
 *
 * @version 1.0
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    Account toAccount(AccountRegisterRequest request);

    UserProfile toUserProfile(AccountRegisterRequest request);

    @Mapping(target = "fullName", source = "userProfile.fullName")
    @Mapping(target = "email", source = "account.email")
    @Mapping(target = "phoneNumber", source = "userProfile.phoneNumber")
    @Mapping(target = "role", source = "account.role")
    UserResponse toUserResponse(Account account, UserProfile userProfile);

    // Mapping for feature edit-profile
    @Mapping(target = "drivingLicenseUrl", ignore = true)
    EditProfileResponse toEditProfileResponse(UserProfile userProfile);

    // Update UserProfile from EditProfileRequest
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "drivingLicenseUri", ignore = true) // Set in service
    void updateUserProfileFromRequest(EditProfileRequest request, @MappingTarget UserProfile userProfile);

    // Set value for drivingLicenseUrl
    @AfterMapping
    default void setDrivingLicenseUrl(@MappingTarget EditProfileResponse response, UserProfile userProfile, @Context FileService fileService) {
        if (userProfile.getDrivingLicenseUri() != null) {
            response.setDrivingLicenseUrl(fileService.getFileUrl(userProfile.getDrivingLicenseUri()));
        }
    }
}
