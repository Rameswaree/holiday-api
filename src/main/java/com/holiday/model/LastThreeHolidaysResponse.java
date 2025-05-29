package com.holiday.model;

import java.util.List;

/**
 * Represents the response containing the last three holidays for a specific country.
 *
 * @param country  The name of the country for which the holidays are listed.
 * @param holidays A list of holiday details, each containing the date and name of the holiday.
 */
public record LastThreeHolidaysResponse(
    String country,
    List<HolidayDetails> holidays
) {}