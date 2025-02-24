package com.mp.karental.mapper;

import com.mp.karental.dto.request.AddCarRequest;
import com.mp.karental.dto.response.CarResponse;
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
    Car toCar(AddCarRequest addCarRequest);

    @Mapping(target = "isAutomatic", source = "automatic")
    @Mapping(target = "isGasoline", source = "gasoline")
    CarResponse toCarResponse(Car car);
}
