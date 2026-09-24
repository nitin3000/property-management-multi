package com.propapp;

import com.propapp.controller.ListingController;
import com.propapp.repository.ListingRepository;
import com.propapp.service.ListingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ListingControllerIntegrationTest {

    @Mock
    private ListingService listingService;

    @Mock
    private ListingRepository listingRepository;

    @InjectMocks
    private ListingController listingController;

    // --- TEST 1: minPrice Greater Than maxPrice ---
    @Test
    public void shouldReturn400WhenMinPriceExceedsMaxPrice() {
        // Act
        ResponseEntity<?> response = listingController.searchListings(
                "Miami", 600000.0, 400000.0, null, null, 550000.0, 0, 5
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body.get("error")).contains("Minimum pricing cannot exceed maximum bounds");
    }

    // --- TEST 2: Page Size of Zero or Less ---
    @Test
    public void shouldReturn400WhenPageSizeIsZeroOrLess() {
        // Act
        ResponseEntity<?> response = listingController.searchListings(
                "Miami", null, null, null, null, 550000.0, 0, 0
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body.get("error")).contains("Page size configuration must be 1 or higher");
    }

    // --- TEST 3: City with No Matches (Doesn't Exist in DB) ---
    @Test
    public void shouldReturn404WhenCityDoesNotExistInDatabase() {
        // Arrange
        Mockito.when(listingRepository.existsByCityIgnoreCase("Boston")).thenReturn(false);

        // Act
        ResponseEntity<?> response = listingController.searchListings(
                "Boston", null, null, null, null, 550000.0, 0, 5
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body.get("error")).contains("does not exist anywhere within our records directory");
    }
}
