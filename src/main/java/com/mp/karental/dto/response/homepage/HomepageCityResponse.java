package com.mp.karental.dto.response.homepage;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;


/**
 * DTO for homepage city data response.
 * <p>
 * This class represents the response object that contains the top 6 cities
 * with the highest number of registered cars. It includes:
 * - A list of {@link CityCarCount} objects, each representing a city and its corresponding car count.
 * </p>
 *
 * Author: AnhHP9
 * Version: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(name = "response.car.HomepageCityResponse",description = "Response object containing the top 6 cities with the highest number of registered cars.")
public class HomepageCityResponse {

    @Schema(description = "List of cities with their respective car counts.")
    List<CityCarCount> topCities;

    @Data
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Schema(description = "Represents a city and its corresponding number of registered cars.")
    public static class CityCarCount {

        @Schema(description = "City or province name", example = "Ho Chi Minh City")
        String cityProvince;

        @Schema(description = "Number of registered cars in the city", example = "150")
        int carCount;
    }
}
