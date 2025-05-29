package com.holiday.service;

import com.holiday.client.HolidayApiClient;
import com.holiday.exception.DataNotFoundException;
import com.holiday.exception.InvalidCountryException;
import com.holiday.exception.InvalidYearException;
import com.holiday.model.CommonHolidaysResponse;
import com.holiday.model.Holiday;
import com.holiday.model.LastThreeHolidaysResponse;
import com.holiday.model.WeekdayHolidaysResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the HolidayApiService class.
 * This class tests the methods for retrieving last three holidays, weekday holidays count,
 * and common holidays between two countries.
 */
@ExtendWith(MockitoExtension.class)
public class HolidayApiServiceTest {

    @Mock
    private HolidayApiClient holidayApiClient;
    private HolidayApiService holidayApiService;

    private static Holiday holiday(LocalDate date, String name, String localName, String countryCode) {
        return new Holiday(date, localName, name, countryCode, null, null, List.of("Public"));
    }

    @BeforeEach
    public void setUp() {
        holidayApiService = new HolidayApiService(holidayApiClient);
        Set<String> validCountries = Set.of("US", "NL");
        // Stub only client methods that your service calls
        lenient().when(holidayApiClient.fetchAvailableCountries()).thenReturn(validCountries);
    }

    @Test
    public void testGetLastThreeCelebratedHolidays_success() {
        String countryCode = "US";
        LocalDate today = LocalDate.now();

        List<Holiday> holidays = List.of(
                holiday(today.minusDays(1), "New Year's Day", "Nieuwjaar", "US"),
                holiday(today.minusDays(10), "Christmas Day", "Kerstmis", "US"),
                holiday(today.minusDays(20), "Thanksgiving Day", "Thanksgiving", "US"),
                holiday(today.plusDays(5), "Future Holiday", "Toekomstige", "US")
        );
        when(holidayApiService.getHolidaysForYear(eq(countryCode), anyInt())).thenReturn(holidays);

        LastThreeHolidaysResponse response = holidayApiService.getLastThreeCelebratedHolidays(countryCode);
        assertEquals("US", response.country());
        assertEquals(3, response.holidays().size());
        assertTrue(response.holidays().stream().allMatch(h -> h.date().isBefore(LocalDate.now())));
    }

    @Test
    public void testGetLastThreeCelebratedHolidays_noFutureHolidays() {
        String countryCode = "US";
        List<Holiday> holidays = List.of(
                holiday(LocalDate.of(2076, 1, 1), "Future Holiday", "Toekomstige", "US")
        );

        when(holidayApiService.getHolidaysForYear(eq(countryCode), anyInt())).thenReturn(holidays);

        assertThrows(DataNotFoundException.class,
                () -> holidayApiService.getLastThreeCelebratedHolidays(countryCode));
    }

    @Test
    public void testGetWeekdayHolidaysCount_success() {
        int year = 2024;
        List<String> countries = List.of("US", "NL");

        List<Holiday> usHolidays = List.of(
                holiday(LocalDate.of(year, 1, 1), "New Year", "Nieuwjaar", "US"),       // Monday
                holiday(LocalDate.of(year, 1, 7), "Random Sunday", "Zondag", "US")      // Sunday
        );

        List<Holiday> nlHolidays = List.of(
                holiday(LocalDate.of(year, 5, 1), "Labour Day", "Dag van de Arbeid", "NL") // Wednesday
        );

        when(holidayApiService.getHolidaysForYear("US", year)).thenReturn(usHolidays);
        when(holidayApiService.getHolidaysForYear("NL", year)).thenReturn(nlHolidays);

        WeekdayHolidaysResponse response = holidayApiService.getWeekdayHolidaysCount(year, countries);

        assertEquals(2, response.countries().size());
        assertTrue(response.countries().stream().anyMatch(
                c -> c.countryCode().equals("US") && c.weekdayHolidaysCount() == 1));
        assertTrue(response.countries().stream().anyMatch(
                c -> c.countryCode().equals("NL") && c.weekdayHolidaysCount() == 1));
    }

    @Test
    public void testGetWeekdayHolidaysCount_invalidCountry() {
        List<String> invalidCountries = List.of("XX");

        InvalidCountryException invalidCountryException = assertThrows(InvalidCountryException.class,
                () -> holidayApiService.getWeekdayHolidaysCount(2024, invalidCountries));

        assertEquals("Invalid country code(s): XX. Please use valid ISO 3166-1 alpha-2 country codes.", invalidCountryException.getMessage());
    }

    @Test
    public void testGetWeekdayHolidaysCount_invalidYear() {
        InvalidYearException invalidYearException = assertThrows(InvalidYearException.class,
                () -> holidayApiService.getWeekdayHolidaysCount(1800, List.of("US")));
        assertEquals("Invalid year: 1800. Year must be between 1975 and 2075.", invalidYearException.getMessage());
    }

    @Test
    public void testGetCommonHolidays_found() {
        int year = 2024;
        String countryOne = "US";
        String countryTwo = "NL";
        LocalDate commonDate = LocalDate.of(year, 1, 1);

        List<Holiday> usHolidays = List.of(
                holiday(commonDate, "New Year", "Nieuwjaar", "US")
        );
        List<Holiday> nlHolidays = List.of(
                holiday(commonDate, "Nieuwjaar", "Nieuwjaar", "NL")
        );

        when(holidayApiService.getHolidaysForYear(countryOne, year)).thenReturn(usHolidays);
        when(holidayApiService.getHolidaysForYear(countryTwo, year)).thenReturn(nlHolidays);

        CommonHolidaysResponse response = holidayApiService.getCommonHolidays(year, countryOne, countryTwo);

        assertEquals(1, response.getCommonHolidays().size());
        assertEquals(commonDate, response.getCommonHolidays().getFirst().date());
    }

    @Test
    public void testGetCommonHolidays_none() {
        int year = 2024;
        String countryOne = "US";
        String countryTwo = "NL";

        when(holidayApiService.getHolidaysForYear(anyString(), eq(year))).thenReturn(Collections.emptyList());

        CommonHolidaysResponse response = holidayApiService.getCommonHolidays(year, countryOne, countryTwo);

        assertTrue(response.getCommonHolidays().isEmpty());
        assertEquals("No common holidays found between US and NL for year 2024", response.getMessage());
    }
}
