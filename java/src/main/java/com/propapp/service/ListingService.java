package com.propapp.service;

import com.propapp.dto.ListingResponseDTO;
import com.propapp.model.Listing;
import com.propapp.repository.ListingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ListingService {

    @Autowired
    private ListingRepository listingRepository;

    public Page<ListingResponseDTO> getRankedListings(
            String city, Double minPrice, Double maxPrice, Integer minBedrooms, String keyword,
            Double targetBudget, int page, int size) {
        
        List<Listing> filteredRawList = listingRepository.findWithFilters(city, minPrice, maxPrice, minBedrooms, keyword);

        List<ListingResponseDTO> rankedList = filteredRawList.stream()
                .map(listing -> new ListingResponseDTO(listing, calculateScore(listing, targetBudget)))
                .sorted((a, b) -> {
                    int scoreCompare = Double.compare(b.getRelevanceScore(), a.getRelevanceScore());
                    if (scoreCompare != 0) return scoreCompare;
                    // Tie-breaker: If relevance score matches exactly, show newer listings first
                    return b.getListedDate().compareTo(a.getListedDate());
                })
                .collect(Collectors.toList());

        // Safe client boundary pagination cuts
        int start = Math.min(page * size, rankedList.size());
        int end = Math.min((start + size), rankedList.size());
        
        List<ListingResponseDTO> pageContent = rankedList.subList(start, end);
        return new PageImpl<>(pageContent, PageRequest.of(page, size), rankedList.size());
    }

    public double calculateScore(Listing listing, Double targetBudget) {
        if (targetBudget == null || targetBudget <= 0) return 100.0;

        // 1. Budget Variance Engine (70% Allocation Weight)
        double budgetScore = 0;
        double price = listing.getPrice();
        if (price <= targetBudget) {
            budgetScore = 70.0; 
        } else {
            double variance = (price - targetBudget) / targetBudget;
            budgetScore = Math.max(0, 70.0 * (1.0 - variance)); 
        }

        // 2. Timeline Decay Engine (30% Allocation Weight)
        double recencyScore = 30.0;
        if (listing.getListedDate() != null) {
            long daysOld = ChronoUnit.DAYS.between(listing.getListedDate(), LocalDate.now());
            if (daysOld > 7) {
                double weeksOld = daysOld / 7.0;
                recencyScore = Math.max(0, 30.0 * Math.pow(0.90, weeksOld)); // 10% structural compound decay weekly
            }
        }
        return budgetScore + recencyScore;
    }
}
