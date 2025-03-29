package com.mp.karental.service;

import com.mp.karental.dto.response.homepage.HomepageCityResponse;
import com.mp.karental.dto.response.homepage.HomepageFeedbackResponse;
import com.mp.karental.entity.Feedback;
import com.mp.karental.mapper.FeedbackMapper;
import com.mp.karental.repository.CarRepository;
import com.mp.karental.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for handling homepage-related data retrieval.
 * <p>
 * This service provides methods to fetch:
 * - Latest 4 five-star feedbacks for display on the homepage.
 * - Top 6 cities with the highest number of cars.
 * </p>
 *
 * Author: AnhHP9
 * Version: 1.0
 */
@Service
@RequiredArgsConstructor
public class HomepageService {

    private final FeedbackRepository feedbackRepository;
    private final FeedbackMapper feedbackMapper;
    private final CarRepository carRepository;

    /**
     * Retrieves the latest 4 five-star feedbacks for display on the homepage.
     * <p>
     * - Fetches feedbacks with a rating of 5, ordered by creation date (most recent first).
     * - Converts the Feedback entity list to a simplified DTO list using FeedbackMapper.
     * </p>
     *
     * @return HomepageFeedbackResponse containing the latest 4 five-star feedbacks.
     */
    public HomepageFeedbackResponse getHomepageFeedbackData() {
        List<Feedback> latestFiveStarFeedbacks = feedbackRepository.findTop4ByRatingOrderByCreatedDateDesc();

        return HomepageFeedbackResponse.builder()
                .latestFiveStarFeedbacks(feedbackMapper.toSimpleFeedbackResponseList(latestFiveStarFeedbacks))
                .build();
    }

    /**
     * Retrieves the top 6 cities with the highest number of registered cars.
     * <p>
     * - Fetches the count of cars grouped by city (cityProvince) from the database.
     * - Maps the raw query results into a list of CityCarCount DTOs.
     * </p>
     *
     * @return HomepageCityResponse containing the top 6 cities with the highest car count.
     */
    public HomepageCityResponse getHomepageCityData() {
        // Fetch top 6 cities with the highest car count
        List<Object[]> topCities = carRepository.findTop6CitiesByCarCount();

        // Convert query result into a list of CityCarCount DTOs
        List<HomepageCityResponse.CityCarCount> cityCarCounts = topCities.stream()
                .map(obj -> new HomepageCityResponse.CityCarCount(
                        (String) obj[0], // cityProvince (City name)
                        ((Number) obj[1]).intValue() // Number of cars in that city
                ))
                .toList();

        return new HomepageCityResponse(cityCarCounts);
    }
}
