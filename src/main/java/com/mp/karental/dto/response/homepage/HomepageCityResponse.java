package com.mp.karental.dto.response.homepage;

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
public class HomepageCityResponse {
    List<CityCarCount> topCities;

    @Data
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class CityCarCount {
        String cityProvince;
        int carCount;
    }
}
