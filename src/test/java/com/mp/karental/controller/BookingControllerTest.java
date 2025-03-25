package com.mp.karental.controller;

import com.mp.karental.KarentalApplication;
import com.mp.karental.constant.EBookingStatus;
import com.mp.karental.constant.EPaymentType;
import com.mp.karental.dto.request.booking.EditBookingRequest;
import com.mp.karental.dto.response.booking.BookingListResponse;
import com.mp.karental.dto.request.booking.CreateBookingRequest;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.mockito.Mockito.*;
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

    private CreateBookingRequest createBookingRequest;
    private EditBookingRequest editBookingRequest;
    private BookingResponse bookingResponse;


    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();

        // Given
        createBookingRequest = new CreateBookingRequest();
        createBookingRequest.setCarId("123");
        createBookingRequest.setDriverFullName("John Doe");
        createBookingRequest.setDriverPhoneNumber("0123456789");
        createBookingRequest.setDriverNationalId("123456789000");
        createBookingRequest.setDriverDob(LocalDate.of(1990, 1, 1));
        createBookingRequest.setDriverEmail("johndoe@example.com");
        createBookingRequest.setDriverCityProvince("Thành phố Hà Nội");
        createBookingRequest.setDriverDistrict("Quận Ba Đình");
        createBookingRequest.setDriverWard("Phường Phúc Xá");
        createBookingRequest.setDriverHouseNumberStreet("123 Kim Ma");
        createBookingRequest.setPickUpLocation("Thành phố Hà Nội,Quận Ba Đình,Phường Phúc Xá,123 Kim Ma");

        bookingResponse = new BookingResponse();
        bookingResponse.setBookingNumber("BK123456");

    }

    @Test
    void testEditBooking_Success() throws Exception {
        // Prepare mock EditBookingRequest
        EditBookingRequest editBookingRequest = new EditBookingRequest();
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
                .andExpect(status().isOk());// Assert that the status code is 200
    }

    @Test
    void testGetBookingsForCustomer_Success() throws Exception {
        BookingListResponse mockResponse = new BookingListResponse(2, 1, Page.empty());
        when(bookingService.getBookingsOfCustomer(anyInt(), anyInt(), anyString(), any())).thenReturn(mockResponse);

        mockMvc.perform(get("/booking/customer/my-bookings")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "createdAt,DESC")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalOnGoingBookings").value(2))
                .andExpect(jsonPath("$.data.totalWaitingConfirmBooking").value(1));
    }

    @Test
    void testGetBookingsForCarOwner_Success() throws Exception {
        BookingListResponse mockResponse = new BookingListResponse(3, 2, Page.empty());
        when(bookingService.getBookingsOfCarOwner(anyInt(), anyInt(), anyString(), any())).thenReturn(mockResponse);

        mockMvc.perform(get("/booking/car-owner/rentals")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "updatedAt,DESC")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalOnGoingBookings").value(3))
                .andExpect(jsonPath("$.data.totalWaitingConfirmBooking").value(2));
    }

    @Test
    void testGetBookingsForCarOwner_WithSpecificStatus() throws Exception {
        BookingListResponse mockResponse = new BookingListResponse(5, 1, Page.empty());
        when(bookingService.getBookingsOfCarOwner(0, 10, "updatedAt,DESC", "CONFIRMED")).thenReturn(mockResponse);

        mockMvc.perform(get("/booking/car-owner/rentals")
                        .param("status", "CONFIRMED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalOnGoingBookings").value(5))
                .andExpect(jsonPath("$.data.totalWaitingConfirmBooking").value(1));
    }

    @Test
    void testGetBookingsForCustomer_StatusIsProvided() throws Exception {
        BookingListResponse mockResponse = new BookingListResponse(5, 1, Page.empty());
        when(bookingService.getBookingsOfCustomer(anyInt(), anyInt(), anyString(), eq("CONFIRMED"))).thenReturn(mockResponse);

        mockMvc.perform(get("/booking/customer/my-bookings")
                        .param("status", "CONFIRMED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalOnGoingBookings").value(5))
                .andExpect(jsonPath("$.data.totalWaitingConfirmBooking").value(1));

        verify(bookingService).getBookingsOfCustomer(0, 10, "updatedAt,DESC", "CONFIRMED");
    }

    @Test
    void testGetBookingsForCustomer_InvalidStatus() throws Exception {
        mockMvc.perform(get("/booking/car-owner/rentals")
                        .param("status", "INVALID_STATUS")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testGetBookingByBookingNumber_Success() throws Exception {
        when(bookingService.getBookingDetailsByBookingNumber("BK123456")).thenReturn(bookingResponse);

        mockMvc.perform(get("/booking/customer/{bookingNumber}", "BK123456")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bookingNumber").value("BK123456"));
    }

    @Test
    void testGetBookingByBookingNumber_NotFound() throws Exception {
        when(bookingService.getBookingDetailsByBookingNumber("INVALID"))
                .thenThrow(new AppException(ErrorCode.BOOKING_NOT_FOUND_IN_DB));

        mockMvc.perform(get("/booking/customer/{bookingNumber}", "INVALID")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetCustomerBookingDetails_Success() throws Exception {
        when(bookingService.getBookingDetailsByBookingNumber("BK123456")).thenReturn(bookingResponse);

        mockMvc.perform(get("/booking/car-owner/{bookingNumber}", "BK123456")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bookingNumber").value("BK123456"));
    }

    @Test
    void testGetCustomerBookingDetails_NotFound() throws Exception {
        when(bookingService.getBookingDetailsByBookingNumber("INVALID"))
                .thenThrow(new AppException(ErrorCode.BOOKING_NOT_FOUND_IN_DB));

        mockMvc.perform(get("/booking/car-owner/{bookingNumber}", "INVALID")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
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
    void testConfirmBooking_Success() throws Exception {
        BookingResponse confirmedBooking = new BookingResponse();
        confirmedBooking.setBookingNumber("BK123456");
        confirmedBooking.setStatus(EBookingStatus.CONFIRMED);

        when(bookingService.confirmBooking("BK123456")).thenReturn(confirmedBooking);

        mockMvc.perform(MockMvcRequestBuilders.put("/booking/car-owner/{bookingNumber}/confirm", "BK123456")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingNumber").value("BK123456"))
                .andExpect(jsonPath("$.status").value(EBookingStatus.CONFIRMED.name()));
    }

    @Test
    void testConfirmBooking_NotFound() throws Exception {
        when(bookingService.confirmBooking("INVALID"))
                .thenThrow(new AppException(ErrorCode.BOOKING_NOT_FOUND_IN_DB));

        mockMvc.perform(MockMvcRequestBuilders.put("/booking/car-owner/{bookingNumber}/confirm", "INVALID")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCancelBooking_Success() throws Exception {
        // Prepare the mock response from bookingService
        String bookingNumber = "12345";
        BookingResponse bookingResponse = new BookingResponse();
        bookingResponse.setBookingNumber(bookingNumber);
        bookingResponse.setCarId("car-001");
        bookingResponse.setStatus(EBookingStatus.CANCELLED);

        // Mock the service method
        when(bookingService.cancelBooking(bookingNumber)).thenReturn(bookingResponse);

        // Perform the GET request and assert that the response is correct
        mockMvc.perform(put("/booking/customer/cancel-booking/{bookingNumber}", bookingNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())  // Assert that the response status is 200 OK
                .andExpect(jsonPath("$.data.bookingNumber").value(bookingNumber))  // Assert that the booking number is returned
                .andExpect(jsonPath("$.data.carId").value("car-001"));
    }

    @Test
    void testConfirmPickUpBooking_Success() throws Exception {
        // Prepare the mock response from bookingService
        String bookingNumber = "12345";
        BookingResponse bookingResponse = new BookingResponse();
        bookingResponse.setBookingNumber(bookingNumber);
        bookingResponse.setCarId("car-001");
        bookingResponse.setStatus(EBookingStatus.IN_PROGRESS);

        // Mock the service method
        when(bookingService.confirmPickUp(bookingNumber)).thenReturn(bookingResponse);

        // Perform the GET request and assert that the response is correct
        mockMvc.perform(put("/booking/customer/confirm-pick-up/{bookingNumber}", bookingNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())  // Assert that the response status is 200 OK
                .andExpect(jsonPath("$.data.bookingNumber").value(bookingNumber))  // Assert that the booking number is returned
                .andExpect(jsonPath("$.data.carId").value("car-001"));
    }

    @Test
    void testReturnCar_Success() throws Exception {
        // Prepare the mock response from bookingService
        String bookingNumber = "12345";
        BookingResponse bookingResponse = new BookingResponse();
        bookingResponse.setBookingNumber(bookingNumber);
        bookingResponse.setCarId("car-001");
        bookingResponse.setStatus(EBookingStatus.COMPLETED);

        // Mock the service method
        when(bookingService.returnCar(bookingNumber)).thenReturn(bookingResponse);

        // Perform the GET request and assert that the response is correct
        mockMvc.perform(put("/booking/customer/return-car/{bookingNumber}", bookingNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())  // Assert that the response status is 200 OK
                .andExpect(jsonPath("$.data.bookingNumber").value(bookingNumber))  // Assert that the booking number is returned
                .andExpect(jsonPath("$.data.carId").value("car-001"));
    }

}