package com.holiday.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the CommonHolidaysResponse class.
 * This class tests the builder pattern and the handling of common holidays between two countries.
 */
public class CommonHolidaysResponseTest {

    @Test
    public void testCommonHolidaysResponseBuilder() {
        CommonHoliday holiday = new CommonHoliday(LocalDate.of(2025, 12, 25), "US", "GB");

        CommonHolidaysResponse response = CommonHolidaysResponse.builder()
                .countryOne("US")
                .countryTwo("GB")
                .commonHolidays(List.of(holiday))
                .build();

        assertEquals("US", response.getCountryOne());
        assertEquals("GB", response.getCountryTwo());
        assertEquals(1, response.getCommonHolidays().size());
        assertNull(response.getMessage());
    }

    @Test
    public void testCommonHolidaysResponseMessageForNoCommonHolidays() {
        CommonHolidaysResponse response = CommonHolidaysResponse.builder()
                .message("No common holidays found")
                .build();

        assertEquals("No common holidays found", response.getMessage());
        assertNull(response.getCountryOne());
        assertNull(response.getCountryTwo());
        assertNull(response.getCommonHolidays());
    }
}
