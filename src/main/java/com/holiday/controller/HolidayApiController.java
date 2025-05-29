package com.holiday.controller;

import com.holiday.model.CommonHolidaysResponse;
import com.holiday.model.LastThreeHolidaysResponse;
import com.holiday.model.WeekdayHolidaysResponse;
import com.holiday.service.HolidayApiService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for handling holiday-related API requests.
 * Provides endpoints to retrieve last three holidays, count of weekday holidays,
 * and common holidays between two countries.
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/holidays")
public class HolidayApiController {

    private final HolidayApiService holidayApiService;

    @GetMapping("/lastThree")
    public ResponseEntity<LastThreeHolidaysResponse> getLastThreeCelebratedHolidays(
            @RequestParam("country") String countryCode) {

        LastThreeHolidaysResponse response = holidayApiService.getLastThreeCelebratedHolidays(countryCode);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/weekdayCount")
    public ResponseEntity<WeekdayHolidaysResponse> getWeekdayHolidaysCount(
            @RequestParam("year") int year,
            @RequestParam("countries") List<String> countries) {

        WeekdayHolidaysResponse response = holidayApiService.getWeekdayHolidaysCount(year, countries);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/common")
    public ResponseEntity<CommonHolidaysResponse> getCommonHolidays(
            @RequestParam("year") int year,
            @RequestParam("countryOne") String countryCodeOne,
            @RequestParam("countryTwo") String countryCodeTwo) {

        CommonHolidaysResponse response = holidayApiService.getCommonHolidays(year, countryCodeOne, countryCodeTwo);
        return ResponseEntity.ok(response);
    }
}
