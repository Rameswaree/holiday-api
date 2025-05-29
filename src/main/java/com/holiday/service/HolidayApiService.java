package com.holiday.service;

import com.holiday.client.HolidayApiClient;
import com.holiday.exception.DataNotFoundException;
import com.holiday.exception.HolidayServiceException;
import com.holiday.exception.InvalidCountryException;
import com.holiday.exception.InvalidYearException;
import com.holiday.model.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for interacting with the Holiday API.
 * This service provides methods to fetch holiday data for various use cases,
 * including retrieving the last three celebrated holidays, counting weekday holidays,
 * and finding common holidays between two countries.
 */
@Service
@AllArgsConstructor
@Slf4j
public class HolidayApiService {

    private static final int START_YEAR = 1975;
    private static final int END_YEAR = 2075;
    private final HolidayApiClient holidayApiClient;

    public LastThreeHolidaysResponse getLastThreeCelebratedHolidays(String countryCode) {
        log.info("Fetching last three celebrated holidays for country: {}", countryCode);
        validateCountryCode(countryCode);

        List<Holiday> allHolidays = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();
        int currentYear = currentDate.getYear();

        if (validateCurrentYear(currentYear)) {
            try {
                List<Holiday> yearHolidays = getHolidaysForYear(countryCode, currentYear);
                allHolidays.addAll(yearHolidays);
            } catch (Exception e) {
                throw new HolidayServiceException("Error fetching holiday data: " + e.getMessage());
            }
        }

        if (allHolidays.isEmpty()) {
            throw new DataNotFoundException("No holiday data found for country: " + countryCode);
        }

        List<Holiday> celebratedHolidays = allHolidays.stream()
                .filter(holiday -> holiday.date().isBefore(currentDate))
                .sorted((h1, h2) -> h2.date().compareTo(h1.date()))
                .limit(3)
                .toList();

        if (celebratedHolidays.isEmpty()) {
            throw new DataNotFoundException("No celebrated holidays found for country: " + countryCode);
        }

        List<HolidayDetails> holidayDetails = celebratedHolidays.stream()
                .map(h -> new HolidayDetails(h.date(), h.name()))
                .collect(Collectors.toList());

        return new LastThreeHolidaysResponse(countryCode.toUpperCase(), holidayDetails);
    }

    public WeekdayHolidaysResponse getWeekdayHolidaysCount(int year, List<String> countryCodes) {
        log.info("Fetching weekday holidays count for year: {} and countries: {}", year, countryCodes);
        validateYear(year);

        if (countryCodes == null || countryCodes.isEmpty()) {
            throw new InvalidCountryException("Country codes list cannot be empty");
        }

        List<CountryHolidayCount> results = new ArrayList<>();
        List<String> invalidCountries = new ArrayList<>();

        for (String countryCode : countryCodes) {
            try {
                validateCountryCode(countryCode);
                List<Holiday> holidays = getHolidaysForYear(countryCode, year);

                long weekdayHolidays = holidays.stream()
                        .filter(holiday -> !isWeekend(holiday.date()))
                        .count();

                results.add(new CountryHolidayCount(
                        countryCode.toUpperCase(), (int) weekdayHolidays));

            } catch (InvalidCountryException e) {
                invalidCountries.add(countryCode);
            } catch (Exception e) {
                throw new HolidayServiceException("Error fetching holiday data: " + e.getMessage());
            }
        }

        if (!invalidCountries.isEmpty()) {
            throw new InvalidCountryException(
                    "Invalid country code(s): " + String.join(", ", invalidCountries) +
                            ". Please use valid ISO 3166-1 alpha-2 country codes.");
        }

        if (results.isEmpty()) {
            throw new DataNotFoundException(
                    "No valid holiday data found for any of the provided countries in year: " + year);
        }

        results.sort((a, b) -> Integer.compare(b.weekdayHolidaysCount(), a.weekdayHolidaysCount()));

        return new WeekdayHolidaysResponse(results);
    }

    public CommonHolidaysResponse getCommonHolidays(int year, String countryCodeOne, String countryCodeTwo) {
        log.info("Fetching common holidays for year: {}, countryOne: {}, countryTwo: {}", year, countryCodeOne, countryCodeTwo);
        validateYear(year);
        validateCountryCode(countryCodeOne);
        validateCountryCode(countryCodeTwo);

        List<Holiday> holidaysOne = getHolidaysForYear(countryCodeOne, year);
        List<Holiday> holidaysTwo = getHolidaysForYear(countryCodeTwo, year);

        Map<LocalDate, Holiday> holidayMapOne = holidaysOne.stream()
                .collect(Collectors.toMap(Holiday::date, h -> h, (existing, replacement) -> existing));

        List<CommonHoliday> commonHolidays = holidaysTwo.stream()
                .filter(h2 -> holidayMapOne.containsKey(h2.date()))
                .map(h2 -> {
                    Holiday h1 = holidayMapOne.get(h2.date());
                    return new CommonHoliday(
                            h2.date(), h1.localName(), h2.localName());
                })
                .sorted(Comparator.comparing(CommonHoliday::date))
                .collect(Collectors.toList());

        if (commonHolidays.isEmpty()) {
            return CommonHolidaysResponse
                    .builder()
                    .countryOne(countryCodeOne.toUpperCase())
                    .countryTwo(countryCodeTwo.toUpperCase())
                    .commonHolidays(Collections.emptyList())
                    .message(
                    String.format("No common holidays found between %s and %s for year %d",
                            countryCodeOne.toUpperCase(), countryCodeTwo.toUpperCase(), year))
                    .build();
        }

        return CommonHolidaysResponse
                .builder()
                .countryOne(countryCodeOne.toUpperCase())
                .countryTwo(countryCodeTwo.toUpperCase())
                .commonHolidays(commonHolidays)
                .build();
    }

    @Cacheable(value = "holidays", key = "#countryCode + '_' + #year")
    protected List<Holiday> getHolidaysForYear(String countryCode, int year) {
        return holidayApiClient.fetchHolidaysForYear(countryCode, year);
    }

    @Cacheable(value = "countries", key = "'available_countries'")
    protected Set<String> getAvailableCountries() {
        return holidayApiClient.fetchAvailableCountries();
    }

    private void validateCountryCode(String countryCode) {
        if (countryCode == null || countryCode.trim().isEmpty()) {
            throw new InvalidCountryException("Country code cannot be empty");
        }

        Set<String> availableCountries = getAvailableCountries();
        if (!availableCountries.contains(countryCode.toUpperCase())) {
            throw new InvalidCountryException(
                    "Invalid country code: " + countryCode + ". Please use a valid ISO 3166-1 alpha-2 country code.");
        }
    }

    private void validateYear(int year) {
        if (year < START_YEAR || year > END_YEAR) {
            throw new InvalidYearException(
                    "Invalid year: " + year + ". Year must be between 1975 and 2075.");
        }
    }

    private boolean validateCurrentYear(int year) {
        return year >= START_YEAR && year <= END_YEAR;
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }
}
