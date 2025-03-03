package com.mp.karental.mapper;

import com.mp.karental.dto.request.AddCarRequest;
import com.mp.karental.dto.response.CarDetailResponse;
import com.mp.karental.dto.response.CarResponse;
import com.mp.karental.dto.response.CarThumbnailResponse;
import com.mp.karental.entity.Car;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CarMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "registrationPaperUri", ignore = true)
    @Mapping(target = "certificateOfInspectionUri", ignore = true)
    @Mapping(target = "insuranceUri", ignore = true)
    @Mapping(target = "carImageFront", ignore = true)
    @Mapping(target = "carImageBack", ignore = true)
    @Mapping(target = "carImageLeft", ignore = true)
    @Mapping(target = "carImageRight", ignore = true)
    @Mapping(target = "cityProvince", ignore = true)
    @Mapping(target = "district", ignore = true)
    @Mapping(target = "ward", ignore = true)
    @Mapping(target = "houseNumberStreet", ignore = true)
    Car toCar(AddCarRequest addCarRequest);

    @Mapping(target = "isAutomatic", source = "automatic")
    @Mapping(target = "isGasoline", source = "gasoline")
    CarResponse toCarResponse(Car car);

    @Mapping(target = "address", ignore = true)
    CarThumbnailResponse toCarThumbnailResponse(Car car);

    @Mapping(target = "address", expression = "java(isBooked ? (car.getHouseNumberStreet() + \", \" + car.getWard() + \", \" + car.getDistrict() + \", \" + car.getCityProvince()) : null)")
    @Mapping(target = "registrationPaperUri", expression = "java(isBooked ? car.getRegistrationPaperUri() : \"Verified\")")
    @Mapping(target = "certificateOfInspectionUri", expression = "java(isBooked ? car.getCertificateOfInspectionUri() : \"Verified\")")
    @Mapping(target = "insuranceUri", expression = "java(isBooked ? car.getInsuranceUri() : \"Verified\")")
    CarDetailResponse toCarDetailResponse(Car car, boolean isBooked);


}
