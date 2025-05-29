package com.holiday.client;

import com.holiday.exception.DataNotFoundException;
import com.holiday.exception.HolidayServiceException;
import com.holiday.exception.InvalidCountryException;
import com.holiday.model.Country;
import com.holiday.model.Holiday;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Client for interacting with the Holiday API.
 * This client fetches holiday data for a specific country and year,
 * and retrieves the list of available countries.
 */
@Component
public class HolidayApiClient {

    private final WebClient webClient;


    @Autowired
    public HolidayApiClient(@Value("${holiday.api.base-url}") String holidayApiUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(holidayApiUrl)
                .build();
    }

    // Additional constructor for testing
    public HolidayApiClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public List<Holiday> fetchHolidaysForYear(String countryCode, int year) {
        try {
            Holiday[] holidays = webClient.get()
                    .uri("/PublicHolidays/{year}/{countryCode}", year, countryCode)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, response -> {
                        if (response.statusCode() == HttpStatus.NOT_FOUND) {
                            return Mono.error(new InvalidCountryException(
                                    "Invalid country code: " + countryCode + " or no data available for year: " + year));
                        }
                        return Mono.error(new HolidayServiceException("Error fetching holiday data: " + response.statusCode()));
                    })
                    .bodyToMono(Holiday[].class)
                    .block();

            if (holidays == null || holidays.length == 0) {
                throw new DataNotFoundException("No holidays found for country: " + countryCode + " and year: " + year);
            }

            return Arrays.asList(Objects.requireNonNull(holidays));
        } catch (WebClientResponseException e) {
            throw new HolidayServiceException("Error fetching holiday data: " + e.getMessage());
        } catch (Exception e) {
            throw new DataNotFoundException("Unexpected error while fetching holiday data: " + e.getMessage());
        }
    }

    public Set<String> fetchAvailableCountries() {
        try {
            Country[] countries = webClient.get()
                    .uri("/AvailableCountries")
                    .retrieve()
                    .bodyToMono(Country[].class)
                    .block();

            if (countries == null) {
                throw new HolidayServiceException("Unable to fetch available countries");
            }

            return Arrays.stream(Objects.requireNonNull(countries))
                    .map(Country::countryCode)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            throw new HolidayServiceException("Error fetching available countries: " + e.getMessage());
        }
    }
}
