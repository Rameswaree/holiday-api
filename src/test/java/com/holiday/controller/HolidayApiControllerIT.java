package com.holiday.controller;

import com.holiday.model.*;
import com.holiday.service.HolidayApiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the HolidayApiController.
 * This class tests the endpoints for retrieving last three holidays, weekday holidays count,
 * and common holidays between two countries.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class HolidayApiControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HolidayApiService holidayApiService;

    @Test
    public void testGetLastThreeCelebratedHolidays() throws Exception {
        String countryCode = "US";
        LastThreeHolidaysResponse response = new LastThreeHolidaysResponse(
                countryCode,
                List.of(
                        new HolidayDetails(LocalDate.of(2024, 5, 1), "Labor Day"),
                        new HolidayDetails(LocalDate.of(2024, 4, 1), "Spring Day"),
                        new HolidayDetails(LocalDate.of(2024, 3, 17), "St. Patrick's Day")
                )
        );

        when(holidayApiService.getLastThreeCelebratedHolidays(countryCode)).thenReturn(response);

        mockMvc.perform(get("/api/v1/holidays/lastThree")
                        .header("Authorization", "Bearer holiday-api-token")
                        .param("country", countryCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.country", is(countryCode)))
                .andExpect(jsonPath("$.holidays", hasSize(3)))
                .andExpect(jsonPath("$.holidays[0].name", is("Labor Day")));
    }

    @Test
    public void testGetWeekdayHolidaysCount() throws Exception {
        int year = 2024;
        List<String> countries = List.of("US", "NL");
        WeekdayHolidaysResponse response = new WeekdayHolidaysResponse(
                List.of(
                        new CountryHolidayCount("US", 10),
                        new CountryHolidayCount("NL", 8)
                )
        );

        when(holidayApiService.getWeekdayHolidaysCount(year, countries)).thenReturn(response);

        mockMvc.perform(get("/api/v1/holidays/weekdayCount")
                        .header("Authorization", "Bearer holiday-api-token")
                        .param("year", String.valueOf(year))
                        .param("countries", "US", "NL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.countries", hasSize(2)))
                .andExpect(jsonPath("$.countries[0].countryCode", is("US")));
    }

    @Test
    public void testGetCommonHolidays_found() throws Exception {
        int year = 2024;
        String countryOne = "US";
        String countryTwo = "NL";

        CommonHolidaysResponse response = CommonHolidaysResponse.builder()
                .countryOne(countryOne)
                .countryTwo(countryTwo)
                .commonHolidays(List.of(
                        new CommonHoliday(LocalDate.of(2024, 1, 1), "New Year's Day", "Nieuwjaarsdag")
                ))
                .build();

        when(holidayApiService.getCommonHolidays(year, countryOne, countryTwo)).thenReturn(response);

        mockMvc.perform(get("/api/v1/holidays/common")
                        .header("Authorization", "Bearer holiday-api-token")
                        .param("year", String.valueOf(year))
                        .param("countryOne", countryOne)
                        .param("countryTwo", countryTwo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commonHolidays", hasSize(1)))
                .andExpect(jsonPath("$.commonHolidays[0].localNameCountryOne", is("New Year's Day")));
    }

    @Test
    public void testGetCommonHolidays_none() throws Exception {
        int year = 2024;
        String countryOne = "US";
        String countryTwo = "NL";

        CommonHolidaysResponse response = CommonHolidaysResponse.builder()
                .countryOne(countryOne)
                .countryTwo(countryTwo)
                .commonHolidays(List.of()) // explicitly set to empty list
                .message("No common holidays found between US and NL for year 2024")
                .build();

        when(holidayApiService.getCommonHolidays(year, countryOne, countryTwo)).thenReturn(response);

        mockMvc.perform(get("/api/v1/holidays/common")
                        .header("Authorization", "Bearer holiday-api-token")
                        .param("year", String.valueOf(year))
                        .param("countryOne", countryOne)
                        .param("countryTwo", countryTwo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commonHolidays", hasSize(0)))
                .andExpect(jsonPath("$.message", containsString("No common holidays")));
    }
}
