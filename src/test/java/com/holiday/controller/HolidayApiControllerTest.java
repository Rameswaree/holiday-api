package com.holiday.controller;

import com.holiday.exception.DataNotFoundException;
import com.holiday.exception.InvalidCountryException;
import com.holiday.exception.InvalidYearException;
import com.holiday.model.*;
import com.holiday.service.HolidayApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the HolidayApiController class.
 * This class tests the endpoints for retrieving last three holidays, weekday holidays count,
 * and common holidays between two countries.
 */
@ExtendWith(MockitoExtension.class)
public class HolidayApiControllerTest {

    @Mock
    private HolidayApiService holidayApiService;

    @InjectMocks
    private HolidayApiController holidayApiController;

    private LastThreeHolidaysResponse mockLastThree;
    private WeekdayHolidaysResponse mockWeekdayResponse;
    private CommonHolidaysResponse mockCommonResponse;

    @BeforeEach
    public void setUp() {
        mockLastThree = new LastThreeHolidaysResponse("US", List.of(
                new HolidayDetails(LocalDate.of(2024, 12, 25), "Christmas"),
                new HolidayDetails(LocalDate.of(2024, 11, 28), "Thanksgiving"),
                new HolidayDetails(LocalDate.of(2024, 7, 4), "Independence Day")
        ));

        mockWeekdayResponse = new WeekdayHolidaysResponse(List.of(
                new CountryHolidayCount("US", 10),
                new CountryHolidayCount("CA", 8)
        ));

        mockCommonResponse = CommonHolidaysResponse.builder()
                .countryOne("US")
                .countryTwo("CA")
                .commonHolidays(List.of(
                        new CommonHoliday(LocalDate.of(2024, 7, 1), "Canada Day", "Canada Day")
                ))
                .build();
    }

    @Test
    public void testGetLastThreeCelebratedHolidaysSuccessFully() {
        when(holidayApiService.getLastThreeCelebratedHolidays("US")).thenReturn(mockLastThree);

        ResponseEntity<LastThreeHolidaysResponse> response =
                holidayApiController.getLastThreeCelebratedHolidays("US");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(mockLastThree, response.getBody());
        verify(holidayApiService).getLastThreeCelebratedHolidays("US");
    }

    @Test
    public void testGetWeekdayHolidaysCountSuccessFully() {
        when(holidayApiService.getWeekdayHolidaysCount(2024, List.of("US", "CA")))
                .thenReturn(mockWeekdayResponse);

        ResponseEntity<WeekdayHolidaysResponse> response =
                holidayApiController.getWeekdayHolidaysCount(2024, List.of("US", "CA"));

        assertEquals(200, response.getStatusCode().value());
        assertEquals(mockWeekdayResponse, response.getBody());
        verify(holidayApiService).getWeekdayHolidaysCount(2024, List.of("US", "CA"));
    }

    @Test
    public void testGetCommonHolidaysSuccessFully() {
        when(holidayApiService.getCommonHolidays(2024, "US", "CA"))
                .thenReturn(mockCommonResponse);

        ResponseEntity<CommonHolidaysResponse> response =
                holidayApiController.getCommonHolidays(2024, "US", "CA");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(mockCommonResponse, response.getBody());
        verify(holidayApiService).getCommonHolidays(2024, "US", "CA");
    }

    @Test
    public void testGetLastThreeCelebratedHolidaysWithInvalidCountry() {
        when(holidayApiService.getLastThreeCelebratedHolidays("ZZ"))
                .thenThrow(new InvalidCountryException("Invalid country code"));

        InvalidCountryException ex = assertThrows(InvalidCountryException.class,
                () -> holidayApiController.getLastThreeCelebratedHolidays("ZZ"));

        assertEquals("Invalid country code", ex.getMessage());
    }

    @Test
    public void testGetWeekdayHolidaysCountWithInvalidYear() {
        when(holidayApiService.getWeekdayHolidaysCount(1800, List.of("US")))
                .thenThrow(new InvalidYearException("Invalid year"));

        InvalidYearException ex = assertThrows(InvalidYearException.class,
                () -> holidayApiController.getWeekdayHolidaysCount(1800, List.of("US")));

        assertEquals("Invalid year", ex.getMessage());
    }

    @Test
    public void testGetWeekdayHolidaysCountWithListContainingInvalidCountry() {
        when(holidayApiService.getWeekdayHolidaysCount(2024, List.of("XX")))
                .thenThrow(new InvalidCountryException("Invalid country code: XX"));

        InvalidCountryException ex = assertThrows(InvalidCountryException.class,
                () -> holidayApiController.getWeekdayHolidaysCount(2024, List.of("XX")));

        assertTrue(ex.getMessage().contains("Invalid country code"));
    }

    @Test
    public void testGetCommonHolidaysWithNoCommonHolidays() {
        when(holidayApiService.getCommonHolidays(2024, "US", "JP"))
                .thenThrow(new DataNotFoundException("No common holidays found"));

        DataNotFoundException ex = assertThrows(DataNotFoundException.class,
                () -> holidayApiController.getCommonHolidays(2024, "US", "JP"));

        assertEquals("No common holidays found", ex.getMessage());
    }
}
