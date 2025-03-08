package com.mp.karental.dto.request;

import com.mp.karental.constant.EBookingStatus;
import com.mp.karental.constant.EPaymentType;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents the request payload for booking.
 * <p>
 * This class encapsulates the necessary data required to create a booking,
 * including booking information.
 * </p>
 * @author QuangPM20
 *
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class BookingRequest {

    LocalDateTime createdAt;
    
    LocalDateTime updatedAt;
    
    EBookingStatus status;
    
    String pickUpLocation;
    
    LocalDateTime pickUpTime;

    LocalDateTime dropOffTime;

    EPaymentType paymentType;

    //=============================================================
    String driverFullName;
    
    String driverPhoneNumber;
    
    String driverNationalId;


    LocalDate driverDob;

    
    String driverEmail;


    MultipartFile driverDrivingLicense;

    
    String driverCityProvince;

    
    String driverDistrict;

    
    String driverWard;

    
    String driverHouseNumberStreet;
}
