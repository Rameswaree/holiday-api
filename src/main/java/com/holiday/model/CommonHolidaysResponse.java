package com.holiday.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a response containing common holidays between two countries.
 *
 * @param countryOne The name of the first country.
 * @param countryTwo The name of the second country.
 * @param commonHolidays A list of common holidays between the two countries.
 * @param message An optional message, e.g., if no common holidays are found.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonHolidaysResponse {
    private String countryOne;
    private String countryTwo;
    private List<CommonHoliday> commonHolidays;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String message;
}