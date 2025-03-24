package com.mp.karental.mapper;

import com.mp.karental.dto.request.booking.CreateBookingRequest;
import com.mp.karental.dto.request.booking.EditBookingRequest;
import com.mp.karental.dto.response.booking.BookingResponse;
import com.mp.karental.dto.response.booking.BookingThumbnailResponse;
import com.mp.karental.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    @Mapping(target = "driverDrivingLicenseUri", ignore = true)
    Booking toBooking(CreateBookingRequest request);

    @Mapping(target = "driverDrivingLicenseUri", ignore = true)
    void editBooking(@MappingTarget Booking target, EditBookingRequest request);

    BookingResponse toBookingResponse(Booking booking);

    @Mapping(target = "brand", source = "car.brand")
    @Mapping(target = "model", source = "car.model")
    @Mapping(target = "productionYear", source = "car.productionYear")
    @Mapping(target = "carImageFrontUrl", ignore = true)
    @Mapping(target = "carImageBackUrl", ignore = true)
    @Mapping(target = "carImageLeftUrl", ignore = true)
    @Mapping(target = "carImageRightUrl", ignore = true)
    @Mapping(target = "numberOfDay", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    BookingThumbnailResponse toBookingThumbnailResponse(Booking booking);
}
