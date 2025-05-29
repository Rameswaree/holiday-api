package com.holiday.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

/**
 * Represents a holiday with various details.
 * This is the base model for holidays details fetched from the NAGER API.
 *
 * @param date The date of the holiday.
 * @param localName The local name of the holiday.
 * @param name The name of the holiday in English.
 * @param countryCode The ISO code of the country where the holiday is observed.
 * @param counties A list of counties within the country that observe this holiday.
 * @param launchYear The year when the holiday was first observed.
 * @param types A list of types that categorize the holiday (e.g., public, bank, etc.).
 */
public record Holiday(
        @JsonProperty("date") @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        @JsonProperty("localName") String localName,
        @JsonProperty("name") String name,
        @JsonProperty("countryCode") String countryCode,
        @JsonProperty("counties") List<String> counties,
        @JsonProperty("launchYear") Integer launchYear,
        @JsonProperty("types") List<String> types
) {}
