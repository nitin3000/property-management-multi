package com.propapp.service;

import com.propapp.dto.ListingResponseDTO;
import com.propapp.model.Listing;
import com.propapp.repository.ListingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class ListingServiceTest {

    @Mock
    private ListingRepository listingRepository;

    @InjectMocks
    private ListingService listingService;

    private List<Listing> dataPool;

    @BeforeEach
    void setUp() {
        dataPool = new ArrayList<>();
        
        Listing item1 = new Listing();
        item1.setId("A1"); item1.setSource("MLS_A"); item1.setCity("Miami");
        item1.setPrice(500000.0); item1.setBedrooms(3); item1.setListedDate(LocalDate.now());
        item1.setDescription("Luxury beach view condo");

        Listing item2 = new Listing();
        item2.setId("B2"); item2.setSource("MLS_B"); item2.setCity("Miami");
        item2.setPrice(600000.0); item2.setBedrooms(4); item2.setListedDate(LocalDate.now().minusDays(14));
        item2.setDescription("Spacious villa setup");

        dataPool.add(item1);
        dataPool.add(item2);
    }

    @Test
    void shouldReturnEmptyPageWhenNoMatchesFound() {
        Mockito.when(listingRepository.findWithFilters("Orlando", null, null, null, null))
               .thenReturn(Collections.emptyList());

        Page<ListingResponseDTO> page = listingService.getRankedListings("Orlando", null, null, null, null, 500000.0, 0, 5);
        assertThat(page.getContent()).isEmpty();
    }

    @Test
    void shouldSortByNewestOnPerfectScoreTie() {
        // Mocking identical elements to test strict grading ties
        Listing oldListing = dataPool.get(0);
        Listing newListing = new Listing();
        newListing.setId("A2"); newListing.setSource("MLS_A"); newListing.setCity("Miami");
        newListing.setPrice(500000.0); newListing.setBedrooms(3); newListing.setListedDate(LocalDate.now());
        oldListing.setListedDate(LocalDate.now().minusDays(2)); // make item A1 slightly older

        List<Listing> tiePool = List.of(oldListing, newListing);
        Mockito.when(listingRepository.findWithFilters("Miami", null, null, null, null)).thenReturn(tiePool);

        Page<ListingResponseDTO> page = listingService.getRankedListings("Miami", null, null, null, null, 500000.0, 0, 5);
        assertThat(page.getContent().get(0).getId()).isEqualTo("A2"); // Newest breaks tie cleanly
    }

    @Test
    void shouldRespectPaginationBounds() {
        Mockito.when(listingRepository.findWithFilters("Miami", null, null, null, null)).thenReturn(dataPool);
        
        // Target an offset window completely outside index boundaries
        Page<ListingResponseDTO> outOfBoundsPage = listingService.getRankedListings("Miami", null, null, null, null, 500000.0, 5, 2);
        assertThat(outOfBoundsPage.getContent()).isEmpty();
    }
}
