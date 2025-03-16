package com.mp.karental.controller;

import com.mp.karental.KarentalApplication;
import com.mp.karental.constant.EBookingStatus;
import com.mp.karental.constant.EPaymentType;
import com.mp.karental.dto.request.booking.EditBookingRequest;
import com.mp.karental.dto.response.booking.BookingThumbnailResponse;
import com.mp.karental.dto.request.booking.BookingRequest;
import com.mp.karental.dto.response.booking.BookingResponse;
import com.mp.karental.dto.response.booking.WalletResponse;
import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.service.BookingService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = KarentalApplication.class)
@ExtendWith(MockitoExtension.class)
@Slf4j
@AutoConfigureMockMvc
class BookingControllerTest {
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;

    private BookingRequest bookingRequest;
    private EditBookingRequest editBookingRequest;
    private BookingResponse bookingResponse;


    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();

        // Given
        bookingRequest = new BookingRequest();
        bookingRequest.setCarId("123");
        bookingRequest.setDriverFullName("John Doe");
        bookingRequest.setDriverPhoneNumber("0123456789");
        bookingRequest.setDriverNationalId("123456789000");
        bookingRequest.setDriverDob(LocalDate.of(1990, 1, 1));
        bookingRequest.setDriverEmail("johndoe@example.com");
        bookingRequest.setDriverCityProvince("Thành phố Hà Nội");
        bookingRequest.setDriverDistrict("Quận Ba Đình");
        bookingRequest.setDriverWard("Phường Phúc Xá");
        bookingRequest.setDriverHouseNumberStreet("123 Kim Ma");
        bookingRequest.setPickUpLocation("Thành phố Hà Nội,Quận Ba Đình,Phường Phúc Xá,123 Kim Ma");

        bookingResponse = new BookingResponse();
        bookingResponse.setBookingNumber("BK123456"); 

    }

    @Test
    void testCreateBooking_MultipartFormData() throws Exception {
        
        BookingResponse bookingResponse = new BookingResponse();
        bookingResponse.setBookingNumber("BK123456");
        bookingResponse.setStatus(EBookingStatus.WAITING_CONFIRM);

        when(bookingService.createBooking(any(BookingRequest.class))).thenReturn(bookingResponse);

        
        mockMvc.perform(multipart("/booking/customer/create-book")
                        .file(new MockMultipartFile("driverLicense", "license.jpg", "image/jpeg", "fake-image-data".getBytes())) 
                        .param("carId", "123")
                        .param("driverFullName", "John Doe")
                        .param("driverPhoneNumber", "0123456789")
                        .param("driverNationalId", "123456789000")
                        .param("driverDob", "1990-01-01")
                        .param("driverEmail", "johndoe@example.com")
                        .param("driverCityProvince", "Thành phố Hà Nội")
                        .param("driverDistrict", "Quận Ba Đình")
                        .param("driverWard", "Phường Phúc Xá")
                        .param("driverHouseNumberStreet", "123 Kim Ma")
                        .param("pickUpLocation", "Thành phố Hà Nội,Quận Ba Đình,Phường Phúc Xá,123 Kim Ma")
                        .param("pickUpTime", "2025-03-25T07:00:00")
                        .param("dropOffTime", "2025-03-25T10:00:00")
                        .param("paymentType", EPaymentType.WALLET.name())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());
    }


    @Test
    void testEditBooking_Success() throws Exception {
        // Prepare mock EditBookingRequest
        EditBookingRequest editBookingRequest = new EditBookingRequest();
        editBookingRequest.setCarId("12345");
        editBookingRequest.setDriverFullName("John Doe");
        editBookingRequest.setDriverPhoneNumber("0886980035");
        editBookingRequest.setDriverNationalId("123456789012");
        editBookingRequest.setDriverDob(LocalDate.parse("1990-01-01"));
        // Populate other fields as required

        BookingResponse bookingResponse = new BookingResponse();
        // Set the expected response from your service method
        bookingResponse.setBookingNumber("12345");

        // Mock the service call
        when(bookingService.editBooking(any(EditBookingRequest.class), eq("12345"))).thenReturn(bookingResponse);

        // Perform the PUT request
        mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PUT,"/booking/customer/edit-book/{bookingNumber}", "12345")
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .param("driverFullName", editBookingRequest.getDriverFullName()) // Add all fields you want to test
                        .param("driverPhoneNumber", editBookingRequest.getDriverPhoneNumber())
                        .param("driverNationalId", editBookingRequest.getDriverNationalId())
                        .param("driverDob", String.valueOf(editBookingRequest.getDriverDob()))
                        .param("carId", editBookingRequest.getCarId()))
                .andExpect(status().isOk()) ;// Assert that the status code is 200
    }

    @Test
    void testEditBooking_BadRequest() throws Exception {
        // Prepare mock EditBookingRequest
        EditBookingRequest editBookingRequest = new EditBookingRequest();
        editBookingRequest.setCarId("12345");
        editBookingRequest.setDriverFullName("John Doe");
        editBookingRequest.setDriverPhoneNumber("0886980035");
        editBookingRequest.setDriverNationalId("123456789012");
        editBookingRequest.setDriverDob(LocalDate.parse("1990-01-01"));
        // Populate other fields as required

        BookingResponse bookingResponse = new BookingResponse();
        // Set the expected response from your service method
        bookingResponse.setBookingNumber("12345");

        // Mock the service call
        when(bookingService.editBooking(any(EditBookingRequest.class), eq("12345"))).thenReturn(bookingResponse);

        // Perform the PUT request
        mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PUT,"/booking/customer/edit-book/{bookingNumber}", "12345")
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .param("driverFullName", editBookingRequest.getDriverFullName()) // Add all fields you want to test
                        .param("driverPhoneNumber", editBookingRequest.getDriverPhoneNumber())
                        .param("driverNationalId", editBookingRequest.getDriverNationalId())
                        .param("driverDob", String.valueOf(editBookingRequest.getDriverDob())))

                .andExpect(status().isBadRequest()) ;// Assert that the status code is 200
    }


    @Test
    void getBookings_Success() throws Exception {
        // Arrange
        Page<BookingThumbnailResponse> mockPage = new PageImpl<>(List.of(new BookingThumbnailResponse()));
        when(bookingService.getBookingsByUserId(0, 10, "createdAt,DESC")).thenReturn(mockPage);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/booking/customer/my-bookings")  
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "createdAt,DESC")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1));

        verify(bookingService, times(1)).getBookingsByUserId(0, 10, "createdAt,DESC");
    }

    @Test
    void getBookings_EmptyResult() throws Exception {
        // Arrange
        Page<BookingThumbnailResponse> emptyPage = Page.empty();
        when(bookingService.getBookingsByUserId(0, 10, "createdAt,DESC")).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/booking/customer/my-bookings")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "createdAt,DESC")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isEmpty());

        verify(bookingService, times(1)).getBookingsByUserId(0, 10, "createdAt,DESC");
    }

    @Test
    void getBookings_DefaultParams() throws Exception {
        // Arrange
        Page<BookingThumbnailResponse> mockPage = new PageImpl<>(List.of(new BookingThumbnailResponse()));
        when(bookingService.getBookingsByUserId(0, 10, "createdAt,DESC")).thenReturn(mockPage);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/booking/customer/my-bookings")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());

        verify(bookingService, times(1)).getBookingsByUserId(0, 10, "createdAt,DESC");
    }

    @Test
    void getWallet_Success() throws Exception {
        
        WalletResponse mockWalletResponse = new WalletResponse("user123", 500000);

        when(bookingService.getWallet()).thenReturn(mockWalletResponse);

        
        mockMvc.perform(get("/booking/get-wallet")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("user123"))
                .andExpect(jsonPath("$.data.balance").value(500000));
    }

    @Test
    void getWallet_AccountNotFound() throws Exception {
        
        when(bookingService.getWallet()).thenThrow(new AppException(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB));

        
        mockMvc.perform(get("/booking/get-wallet")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB.getCode()));
    }

    @Test
    void testGetCarById_Success() throws Exception {
        // Prepare the mock response from bookingService
        String bookingNumber = "12345";
        BookingResponse bookingResponse = new BookingResponse();
        bookingResponse.setBookingNumber(bookingNumber);
        bookingResponse.setCarId("car-001");

        // Mock the service method
        when(bookingService.getBookingDetailsByBookingNumber(bookingNumber)).thenReturn(bookingResponse);

        // Perform the GET request and assert that the response is correct
        mockMvc.perform(get("/booking/customer/{bookingNumber}", bookingNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())  // Assert that the response status is 200 OK
                .andExpect(jsonPath("$.data.bookingNumber").value(bookingNumber))  // Assert that the booking number is returned
                .andExpect(jsonPath("$.data.carId").value("car-001"));  // Assert that the car ID is returned
    }

    @Test
    void testGetCarById_NotFound() throws Exception {
        // Prepare the booking number that will not be found in the service
        String bookingNumber = "non-existent-id";

        // Mock the service method to return null or throw an exception (based on your logic)
        when(bookingService.getBookingDetailsByBookingNumber(bookingNumber)).thenThrow(new AppException(ErrorCode.BOOKING_NOT_FOUND_IN_DB));

        // Perform the GET request and expect a 404 Not Found error
        mockMvc.perform(get("/booking/customer/{bookingNumber}", bookingNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());  // Assert that the response status is 404
    }

}
