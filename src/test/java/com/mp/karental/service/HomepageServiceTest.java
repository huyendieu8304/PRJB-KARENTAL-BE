package com.mp.karental.service;

import com.mp.karental.dto.response.homepage.HomepageCityResponse;
import com.mp.karental.dto.response.homepage.HomepageFeedbackResponse;
import com.mp.karental.entity.Feedback;
import com.mp.karental.mapper.FeedbackMapper;
import com.mp.karental.repository.CarRepository;
import com.mp.karental.repository.FeedbackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HomepageServiceTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private FeedbackMapper feedbackMapper;

    @Mock
    private CarRepository carRepository;

    @InjectMocks
    private HomepageService homepageService;

    @BeforeEach
    void setUp() {
        Mockito.reset(feedbackRepository, feedbackMapper, carRepository);
    }

    /**
     * Positive Test Case: Get latest 4 five-star feedbacks successfully.
     */
    @Test
    void getHomepageFeedbackData_ShouldReturnLatestFeedbacks_WhenDataExists() {
        // Arrange
        List<Feedback> mockFeedbacks = List.of(new Feedback(), new Feedback(), new Feedback(), new Feedback());
        when(feedbackRepository.findTop4ByRatingOrderByCreatedDateDesc()).thenReturn(mockFeedbacks);
        when(feedbackMapper.toSimpleFeedbackResponseList(mockFeedbacks)).thenReturn(Collections.emptyList());

        // Act
        HomepageFeedbackResponse response = homepageService.getHomepageFeedbackData();

        // Assert
        assertNotNull(response);
        assertEquals(0, response.getLatestFiveStarFeedbacks().size());
        verify(feedbackRepository, times(1)).findTop4ByRatingOrderByCreatedDateDesc();
        verify(feedbackMapper, times(1)).toSimpleFeedbackResponseList(mockFeedbacks);
    }

    /**
     * Negative Test Case: Feedback data is null.
     */
    @Test
    void getHomepageFeedbackData_ShouldHandleNullFeedbacks() {
        // Arrange
        when(feedbackRepository.findTop4ByRatingOrderByCreatedDateDesc()).thenReturn(null);

        // Act
        HomepageFeedbackResponse response = homepageService.getHomepageFeedbackData();

        // Assert
        assertNotNull(response);
        assertTrue(response.getLatestFiveStarFeedbacks().isEmpty());
        verify(feedbackRepository, times(1)).findTop4ByRatingOrderByCreatedDateDesc();
    }

    /**
     * Edge Case: Empty feedback list.
     */
    @Test
    void getHomepageFeedbackData_ShouldReturnEmptyList_WhenNoFeedbacksExist() {
        // Arrange
        when(feedbackRepository.findTop4ByRatingOrderByCreatedDateDesc()).thenReturn(Collections.emptyList());

        // Act
        HomepageFeedbackResponse response = homepageService.getHomepageFeedbackData();

        // Assert
        assertNotNull(response);
        assertTrue(response.getLatestFiveStarFeedbacks().isEmpty());
        verify(feedbackRepository, times(1)).findTop4ByRatingOrderByCreatedDateDesc();
    }

    /**
     * Positive Test Case: Get top 6 cities with the most cars.
     */
    @Test
    void getHomepageCityData_ShouldReturnTopCities_WhenDataExists() {
        // Arrange
        List<Object[]> mockCities = List.of(
                new Object[]{"Hanoi", 100},
                new Object[]{"Ho Chi Minh", 90},
                new Object[]{"Da Nang", 80},
                new Object[]{"Can Tho", 70},
                new Object[]{"Hai Phong", 60},
                new Object[]{"Nha Trang", 50}
        );

        when(carRepository.findTop6CitiesByCarCount()).thenReturn(mockCities);

        // Act
        HomepageCityResponse response = homepageService.getHomepageCityData();

        // Assert
        assertNotNull(response);
        assertEquals(6, response.getTopCities().size());
        assertEquals("Hanoi", response.getTopCities().get(0).getCityProvince());
        assertEquals(100, response.getTopCities().get(0).getCarCount());
        verify(carRepository, times(1)).findTop6CitiesByCarCount();
    }

    /**
     * Edge Case: Empty city list.
     */
    @Test
    void getHomepageCityData_ShouldReturnEmptyList_WhenNoCitiesExist() {
        // Arrange
        when(carRepository.findTop6CitiesByCarCount()).thenReturn(Collections.emptyList());

        // Act
        HomepageCityResponse response = homepageService.getHomepageCityData();

        // Assert
        assertNotNull(response);
        assertTrue(response.getTopCities().isEmpty());
        verify(carRepository, times(1)).findTop6CitiesByCarCount();
    }

    /**
     * Edge Case: City with 0 cars.
     * Ensures that the service correctly handles a city with zero registered cars.
     */
    @Test
    void getHomepageCityData_ShouldHandleCityWithZeroCars() {
        // Arrange: Mock data for a city with zero cars
        List<Object[]> mockCities = new ArrayList<>();
        mockCities.add(new Object[]{"Unknown City", 0});
        when(carRepository.findTop6CitiesByCarCount()).thenReturn(mockCities);

        // Act
        HomepageCityResponse response = homepageService.getHomepageCityData();

        // Assert
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getTopCities(), "Top cities list should not be null");
        assertEquals(1, response.getTopCities().size(), "There should be exactly one city in the response");

        // Validate the city name and car count
        HomepageCityResponse.CityCarCount cityData = response.getTopCities().get(0);
        assertEquals("Unknown City", cityData.getCityProvince(), "City name should match the mock data");
        assertEquals(0, cityData.getCarCount(), "Car count should be zero for this city");

        // Verify repository method was called exactly once
        verify(carRepository, times(1)).findTop6CitiesByCarCount();
    }



}
