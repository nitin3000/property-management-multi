package com.propapp.controller;

import com.propapp.dto.ListingResponseDTO;
import com.propapp.model.Listing;
import com.propapp.repository.ListingRepository;
import com.propapp.service.ListingService;
import com.propapp.producer.PropertyProducerService; // Injecting your Kafka Producer
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/listings")
public class ListingController {

    @Autowired
    private ListingService listingService;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private PropertyProducerService propertyProducerService; // Added for async ingestion

    /**
     * POST /api/listings
     * Refactored to drop synchronous DB overhead. Publishes message directly to Confluent Cloud.
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> createListing(@RequestBody Listing listing) {
        // 1. Generate or extract an idempotency key (Message Key)
        String propertyId = listing.getId() != null ? listing.getId() : UUID.randomUUID().toString();
        listing.setId(propertyId);

        // 2. Offload work asynchronously to Kafka
        propertyProducerService.publishPropertyEvent(listing, propertyId);

        // 3. Instantly respond 202 Accepted to the client for extreme throughput capacity
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(Map.of(
                    "status", "Accepted",
                    "message", "Property listing payload queued safely for asynchronous streaming processing.",
                    "id", propertyId
                ));
    }

    /**
     * GET /api/listings/search
     * (Remains unchanged for read routing queries)
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchListings(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer minBedrooms,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Double targetBudget,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        if (size <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid Input: Page size configuration must be 1 or higher. Passed: " + size));
        }
        if (page < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid Input: Page index cannot be less than zero. Passed: " + page));
        }
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid Filter Variant: Minimum pricing cannot exceed maximum bounds. Min: $" + minPrice + " Max: $" + maxPrice));
        }
        if (minBedrooms != null && minBedrooms < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid Constraint: Bedrooms criteria filters cannot receive negative values."));
        }
        if (city != null && !city.trim().isEmpty()) {
            boolean cityExists = listingRepository.existsByCityIgnoreCase(city.trim());
            if (!cityExists) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No Match Exception: The city '" + city + "' does not exist anywhere within our records directory."));
            }
        }

        Page<ListingResponseDTO> results = listingService.getRankedListings(
                city, minPrice, maxPrice, minBedrooms, keyword, targetBudget, page, size
        );

        if (results.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No Match Exception: Listings exist in this city, but none fit your specific pricing, bedroom, or keyword filter matrix."));
        }

        return ResponseEntity.ok(results);
    }
}
