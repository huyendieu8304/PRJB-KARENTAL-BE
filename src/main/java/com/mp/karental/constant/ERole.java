package com.mp.karental.constant;

import lombok.Getter;

/**
 * Represents the various roles available in the application.
 * <p>
 * This enum defines the roles that can be assigned to users for authorization purposes.
 * </p>
 * <ul>
 *     <li>{@code ADMIN} - Role for administrative users with elevated privileges.</li>
 *     <li>{@code CAR_OWNER} - Role for users who own cars.</li>
 *     <li>{@code CUSTOMER} - Role for standard customers.</li>
 * </ul>
 *
 * @author DieuTTH4
 *
 * @version 1.0
 */
@Getter
public enum ERole {
    ADMIN,
    CAR_OWNER,
    CUSTOMER,
    OPERATOR,
}
