package com.mp.karental.mapper;

import com.mp.karental.dto.request.BookingRequest;
import com.mp.karental.dto.response.BookingResponse;
import com.mp.karental.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    @Mapping(target = "driverDrivingLicenseUri", ignore = true)
    Booking toBooking(BookingRequest request);

    BookingResponse toBookingResponse(Booking booking);
}
