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
import org.springframework.data.redis.core.RedisTemplate;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;

@Service
public class ListingService {

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
public Page<ListingResponseDTO> getRankedListings(
        String city, Double minPrice, Double maxPrice, Integer minBedrooms, String keyword,
        Double targetBudget, int page, int size) { 
    
    String cacheKey = "none";
    try {
        // 1. CRITICAL BUG FIX: Exclude page and size from the hash string entirely
        String rawParamsString = String.format("city:%s|minP:%s|maxP:%s|beds:%s|key:%s|budg:%s",
                city != null ? city.toLowerCase().trim() : "all",
                minPrice != null ? minPrice.toString() : "0",
                maxPrice != null ? maxPrice.toString() : "max",
                minBedrooms != null ? minBedrooms.toString() : "0",
                keyword != null ? keyword.toLowerCase().trim() : "none",
                targetBudget != null ? targetBudget.toString() : "none"
        );

        MessageDigest digest = MessageDigest.getInstance("MD5");
        byte[] hashBytes = digest.digest(rawParamsString.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        cacheKey = "search::master::" + hexString.toString();

        // 2. TIER 1 CACHE CHECK: Pull the entire pre-sorted master list from memory
        Object cachedData = redisTemplate.opsForValue().get(cacheKey);
        if (cachedData != null) {
            List<ListingResponseDTO> masterRankedList = (List<ListingResponseDTO>) cachedData;
           
            // Execute the client boundary pagination cuts instantly on the cached master list
            int start = Math.min(page * size, masterRankedList.size());
            int end = Math.min((start + size), masterRankedList.size());
            List<ListingResponseDTO> pageContent = masterRankedList.subList(start, end);
            
            return new PageImpl<>(pageContent, PageRequest.of(page, size), masterRankedList.size());
        }
    } catch (Exception e) {
        System.err.println("Redis Master Lookup Jitter: " + e.getMessage());
    }
        
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
    try {
        if (rankedList != null && !rankedList.isEmpty() && !"none".equals(cacheKey)) {
            redisTemplate.opsForValue().set(cacheKey, rankedList, 5, TimeUnit.MINUTES);
        }
    } catch (Exception e) {
        System.err.println("Failed to write master list to ElastiCache: " + e.getMessage());
    }

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
