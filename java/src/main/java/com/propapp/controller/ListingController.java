package com.propapp.controller;

import com.propapp.dto.ListingResponseDTO;
import com.propapp.model.Listing;
import com.propapp.repository.ListingRepository;
import com.propapp.service.ListingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/listings")
public class ListingController {

    @Autowired
    private ListingService listingService;

    @Autowired
    private ListingRepository listingRepository;

    /**
     * POST /api/listings
     * Persists an incoming property listing record directly to the PostgreSQL database table.
     */
    @PostMapping
    public ResponseEntity<Listing> createListing(@RequestBody Listing listing) {
        Listing savedListing = listingRepository.save(listing);
        return ResponseEntity.ok(savedListing);
    }

    /**
     * GET /api/listings/search
     * Performs multi-parameter filtering, dynamic budget/recency relevance scoring, 
     * and strictly bounded pagination transitions over property listings.
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

        // --- DELIBERATE INPUT VALIDATION GUARDRAILS --- [3.3]

        // 1. Guard against page sizes of zero or less
        if (size <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid Input: Page size configuration must be 1 or higher. Passed: " + size));
        }

        // 2. Guard against negative page layouts offset indexes
        if (page < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid Input: Page index cannot be less than zero. Passed: " + page));
        }

        // 3. Guard against logically conflicting pricing boundaries
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid Filter Variant: Minimum pricing cannot exceed maximum bounds. Min: $" + minPrice + " Max: $" + maxPrice));
        }

        // 4. Guard against negative bedroom filters constraints
        if (minBedrooms != null && minBedrooms < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid Constraint: Bedrooms criteria filters cannot receive negative values."));
        }

        // 5. Active database verification for a city with no matches (avoids silent empty arrays)
        if (city != null && !city.trim().isEmpty()) {
            boolean cityExists = listingRepository.existsByCityIgnoreCase(city.trim());
            if (!cityExists) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No Match Exception: The city '" + city + "' does not exist anywhere within our records directory."));
            }
        }

        // --- EXECUTE CORE BUSINESS RANKING LOGIC --- [3.4]
        Page<ListingResponseDTO> results = listingService.getRankedListings(
                city, minPrice, maxPrice, minBedrooms, keyword, targetBudget, page, size
        );

        // 6. Handle cases where parameters are valid but filter results yields zero rows
        if (results.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No Match Exception: Listings exist in this city, but none fit your specific pricing, bedroom, or keyword filter matrix."));
        }

        return ResponseEntity.ok(results);
    }
}
