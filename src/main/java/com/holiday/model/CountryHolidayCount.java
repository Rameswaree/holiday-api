package com.holiday.model;

/**
 * Represents a country with its code and the count of weekday holidays.
 */
public record CountryHolidayCount(String countryCode, int weekdayHolidaysCount) {}