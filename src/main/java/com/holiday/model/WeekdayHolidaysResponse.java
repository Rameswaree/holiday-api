package com.holiday.model;

import java.util.List;

/**
 * Represents the response containing the list of holiday counts for a list of countries.
 *
 * @param countries the list of countries.
 */
public record WeekdayHolidaysResponse(List<CountryHolidayCount> countries) {}