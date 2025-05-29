package com.holiday.client;

import com.holiday.exception.DataNotFoundException;
import com.holiday.exception.HolidayServiceException;
import com.holiday.model.Country;
import com.holiday.model.Holiday;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the HolidayApiClient class.
 * This class tests the methods for fetching holidays and available countries from the Holiday API.
 */
public class HolidayApiClientTest {

    private WebClient mockWebClient;
    private WebClient.RequestHeadersUriSpec mockUriSpec;
    private WebClient.RequestHeadersSpec mockHeadersSpec;
    private WebClient.ResponseSpec mockResponseSpec;

    private HolidayApiClient holidayApiClient;

    @BeforeEach
    public void setUp() {
        mockWebClient = mock(WebClient.class);
        mockUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        mockHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        mockResponseSpec = mock(WebClient.ResponseSpec.class);

        holidayApiClient = new HolidayApiClient(mockWebClient);
    }

    @Test
    public void fetchHolidaysForYear_success() {
        Holiday[] mockHolidays = {
                new Holiday(LocalDate.of(2024, 1, 1), "New Year", "New Year", "US", null, null, List.of("Public"))
        };

        when(mockWebClient.get()).thenReturn(mockUriSpec);
        when(mockUriSpec.uri("/PublicHolidays/{year}/{countryCode}", 2024, "US")).thenReturn(mockHeadersSpec);
        when(mockHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.onStatus(any(), any())).thenReturn(mockResponseSpec);
        when(mockResponseSpec.bodyToMono(Holiday[].class)).thenReturn(Mono.just(mockHolidays));

        List<Holiday> result = holidayApiClient.fetchHolidaysForYear("US", 2024);
        assertEquals(1, result.size());
        assertEquals("US", result.getFirst().countryCode());
    }

    @Test
    public void fetchHolidaysForYear_throws_NoDataFoundException() {
        Holiday[] emptyArray = new Holiday[0];

        when(mockWebClient.get()).thenReturn(mockUriSpec);
        when(mockUriSpec.uri("/PublicHolidays/{year}/{countryCode}", 2024, "XX")).thenReturn(mockHeadersSpec);
        when(mockHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.onStatus(any(), any())).thenReturn(mockResponseSpec);
        when(mockResponseSpec.bodyToMono(Holiday[].class)).thenReturn(Mono.just(emptyArray));

        assertThrows(DataNotFoundException.class, () ->
                holidayApiClient.fetchHolidaysForYear("XX", 2024));
    }

    @Test
    public void fetchAvailableCountries_success() {
        Country[] countries = {
                new Country("United States", "US"),
                new Country("Canada", "CA")
        };

        when(mockWebClient.get()).thenReturn(mockUriSpec);
        when(mockUriSpec.uri("/AvailableCountries")).thenReturn(mockHeadersSpec);
        when(mockHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.bodyToMono(Country[].class)).thenReturn(Mono.just(countries));

        Set<String> result = holidayApiClient.fetchAvailableCountries();
        assertEquals(Set.of("Canada", "United States"), result);
    }

    @Test
    public void fetchAvailableCountries_throws_HolidayServiceException() {
        when(mockWebClient.get()).thenReturn(mockUriSpec);
        when(mockUriSpec.uri("/AvailableCountries")).thenReturn(mockHeadersSpec);
        when(mockHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.bodyToMono(Country[].class)).thenThrow(new RuntimeException("API error"));

        assertThrows(HolidayServiceException.class, () -> holidayApiClient.fetchAvailableCountries());
    }
}
