package com.mp.karental.mapper;

import com.mp.karental.dto.request.AddCarRequest;
import com.mp.karental.dto.request.EditCarRequest;
import com.mp.karental.dto.response.CarDetailResponse;
import com.mp.karental.dto.response.CarResponse;
import com.mp.karental.dto.response.CarThumbnailResponse;
import com.mp.karental.entity.Car;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper interface for converting between car-related DTOs and entities.
 *
 * @author QuangPM20, AnhPH9
 *
 * @version 1.0
 */
@Mapper(componentModel = "spring")
public interface CarMapper {
    /**
     * Maps an AddCarRequest DTO to a Car entity.
     * Some fields related to images and address details are ignored.
     *
     * @param addCarRequest The request DTO containing car details.
     * @return A Car entity with mapped fields.
     */
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

    /**
     * Updates an existing Car entity with values from an EditCarRequest DTO.
     * Fields related to images and address details are ignored.
     *
     * @param car The existing Car entity to update.
     * @param editCarRequest The request DTO containing updated car details.
     */
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
    void editCar(@MappingTarget Car car, EditCarRequest editCarRequest);

    /**
     * Converts a Car entity into a CarResponse DTO.
     * Maps 'automatic' to 'isAutomatic' and 'gasoline' to 'isGasoline'.
     *
     * @param car The Car entity to be converted.
     * @return A CarResponse DTO containing mapped car details.
     */
    @Mapping(target = "isAutomatic", source = "automatic")
    @Mapping(target = "isGasoline", source = "gasoline")
    CarResponse toCarResponse(Car car);

    @Mapping(target = "address", ignore = true)
    CarThumbnailResponse toCarThumbnailResponse(Car car);

    @Mapping(target = "address", ignore = true)
    @Mapping(target = "registrationPaperUrl", ignore = true)
    @Mapping(target = "certificateOfInspectionUrl", ignore = true)
    @Mapping(target = "insuranceUrl", ignore = true)
    CarDetailResponse toCarDetailResponse(Car car, boolean isAvailable);


}
