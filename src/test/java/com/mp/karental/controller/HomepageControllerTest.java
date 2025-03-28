package com.mp.karental.controller;

import com.mp.karental.dto.response.ApiResponse;
import com.mp.karental.dto.response.homepage.HomepageCityResponse;
import com.mp.karental.dto.response.homepage.HomepageFeedbackResponse;
import com.mp.karental.service.HomepageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class HomepageControllerTest {

    @Mock
    private HomepageService homepageService;

    @InjectMocks
    private HomepageController homepageController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getHomepageData_ShouldReturnLatestFeedbacks() {
        // Arrange
        HomepageFeedbackResponse mockResponse = new HomepageFeedbackResponse(List.of());
        when(homepageService.getHomepageFeedbackData()).thenReturn(mockResponse);

        // Act
        ApiResponse<HomepageFeedbackResponse> response = homepageController.getHomepageData();

        // Assert
        assertEquals(mockResponse, response.getData());
        verify(homepageService, times(1)).getHomepageFeedbackData();
    }

    @Test
    void getCityData_ShouldReturnTopCities() {
        // Arrange
        HomepageCityResponse mockResponse = new HomepageCityResponse(List.of());
        when(homepageService.getHomepageCityData()).thenReturn(mockResponse);

        // Act
        ApiResponse<HomepageCityResponse> response = homepageController.getCityData();

        // Assert
        assertEquals(mockResponse, response.getData());
        verify(homepageService, times(1)).getHomepageCityData();
    }
}
