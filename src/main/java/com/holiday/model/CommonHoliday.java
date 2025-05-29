package com.holiday.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

/**
 * Represents a common holiday between two countries.
 *
 * @param date The date of the holiday.
 * @param localNameCountryOne The local name of the holiday in country one.
 * @param localNameCountryTwo The local name of the holiday in country two.
 */
public record CommonHoliday (
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate date,
    String localNameCountryOne,
    String localNameCountryTwo
) {}