package com.mp.karental.controller;

import com.mp.karental.dto.response.ApiResponse;
import com.mp.karental.dto.response.feedback.FeedbackResponse;
import com.mp.karental.dto.response.homepage.HomepageCityResponse;
import com.mp.karental.dto.response.homepage.HomepageFeedbackResponse;
import com.mp.karental.service.HomepageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
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
@Validated
@Tag(name = "Homepage", description = "API for managing homepage")
public class HomepageController {

    private final HomepageService homepageService;

    /**
     * Public API: Get homepage data for guest users.
     *
     * @return HomepageResponse containing latest 4 five-star feedbacks and top 6 cities with the most cars.
     */
    @Operation(
            summary = "View feedbacks",
            description = "View latest 4 five-star feedbacks.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Success",
                            content = @Content(
                                    schema = @Schema(type = "object"),
                                    schemaProperties = {
                                            @SchemaProperty(
                                                    name = "code",
                                                    schema = @Schema(type = "string", example = "1000")
                                            ),
                                            @SchemaProperty(
                                                    name = "message",
                                                    schema = @Schema(type = "string", example = "Successful!")
                                            ),
                                            @SchemaProperty(
                                                    name = "data",
                                                    schema = @Schema(type = "object", implementation = HomepageFeedbackResponse.class)
                                            )
                                    }
                            )
                    )
            }
    )
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
    @Operation(
            summary = "View cities",
            description = "View top 6 cities with the most cars.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Success",
                            content = @Content(
                                    schema = @Schema(type = "object"),
                                    schemaProperties = {
                                            @SchemaProperty(
                                                    name = "code",
                                                    schema = @Schema(type = "string", example = "1000")
                                            ),
                                            @SchemaProperty(
                                                    name = "message",
                                                    schema = @Schema(type = "string", example = "Successful!")
                                            ),
                                            @SchemaProperty(
                                                    name = "data",
                                                    schema = @Schema(type = "object", implementation = HomepageCityResponse.class)
                                            )
                                    }
                            )
                    )
            }
    )
    @GetMapping("/city")
    public ApiResponse<HomepageCityResponse> getCityData() {
        return ApiResponse.<HomepageCityResponse>builder()
                .data(homepageService.getHomepageCityData())
                .build();
    }
}
