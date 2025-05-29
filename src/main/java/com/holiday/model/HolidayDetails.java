package com.holiday.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

/**
 * Represents the details of a holiday.
 * This record is used to encapsulate the date and name of a holiday.
 *
 * @param date The date of the holiday in ISO format (yyyy-MM-dd).
 * @param name The name of the holiday.
 */
public record HolidayDetails(
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        String name
) {}