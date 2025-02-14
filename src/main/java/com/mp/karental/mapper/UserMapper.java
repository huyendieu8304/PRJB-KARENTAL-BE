package com.mp.karental.mapper;

import com.mp.karental.constant.ERole;
import com.mp.karental.dto.request.AccountRegisterRequest;
import com.mp.karental.dto.response.UserResponse;
import com.mp.karental.entity.Account;
import com.mp.karental.entity.Role;
import com.mp.karental.entity.UserProfile;
import com.mp.karental.repository.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    Account toAccount(AccountRegisterRequest request);

//    /**
//     * convert role from request to Account object
//     * @param request request from client
//     * @param account return account object
//     */
//    @AfterMapping
//    default void mapRole(AccountRegisterRequest request, @MappingTarget Account account) {
//        ERole userRole = ERole.CUSTOMER;
//        if(!Boolean.parseBoolean(request.getIsCustomer())){
//            userRole = ERole.CAR_OWNER;
//        }
//        account.setRole(Rol);
//    }



    UserProfile toUserProfile(AccountRegisterRequest request);

    @Mapping(target = "fullName", source = "userProfile.fullName")
    @Mapping(target = "email", source = "account.email")
    @Mapping(target = "phoneNumber", source = "userProfile.phoneNumber")
    @Mapping(target = "role", source = "account.role")
    UserResponse toUserResponse(Account account, UserProfile userProfile);
}
