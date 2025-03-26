package com.mp.karental.controller;

import com.mp.karental.dto.response.ApiResponse;
import com.mp.karental.dto.response.homepage.HomepageCityResponse;
import com.mp.karental.dto.response.homepage.HomepageFeedbackResponse;
import com.mp.karental.service.HomepageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling homepage-related endpoints.
 * Provides API for guest users to view homepage data.
 *
 * Author: AnhHP9
 * Version: 1.0
 */
@RestController
@RequestMapping("/homepage")
@RequiredArgsConstructor
public class HomepageController {

    private final HomepageService homepageService;

    /**
     * Public API: Get homepage data for guest users.
     *
     * @return HomepageResponse containing latest 4 five-star feedbacks and top 6 cities with the most cars.
     */
    @GetMapping("/feedbacks")
    public ApiResponse<HomepageFeedbackResponse> getHomepageData() {
        return ApiResponse.<HomepageFeedbackResponse>builder()
                .data(homepageService.getHomepageFeedbackData())
                .build();
    }

    /**
     * Public API: Retrieves the top 6 cities with the most registered cars.
     *
     * @return ApiResponse containing a {@link HomepageCityResponse} object,
     * which includes a list of the top 6 cities with the highest car count.
     */
    @GetMapping("/city")
    public ApiResponse<HomepageCityResponse> getCityData() {
        return ApiResponse.<HomepageCityResponse>builder()
                .data(homepageService.getHomepageCityData())
                .build();
    }
}
