package com.propapp.service;

import com.propapp.model.Listing;
import com.propapp.repository.ListingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;

@Component
public class DataSeedRunner implements CommandLineRunner {

    @Autowired
    private ListingRepository listingRepository;

    @Override
    public void run(String... args) throws Exception {
        // Only seed data if the listings table inside PostgreSQL is currently blank
        if (listingRepository.count() == 0) {
            System.out.println("🚀 Table empty. Seeding upgraded 12-item real estate listings into PostgreSQL...");

            Listing l1 = createMock("A1", "MLS_A", "123 Main St, Apt 4B", "Springfield", "VA", "22150", 450000.0, 2, 1.5f, 980, 38.7893f, -77.1873f, "2026-08-29", "active", "Bright top-floor condo near shops and transit. Pet friendly.");
            Listing l2 = createMock("B7", "MLS_B", "123 Main Street, Unit 4B", "Springfield", "VA", "22150", 452000.0, 2, 1.5f, 980, 38.7893f, -77.1873f, "2026-08-27", "active", "Top floor condo, walk to shopping. Pets allowed.");
            Listing l3 = createMock("A2", "MLS_A", "456 Oak Ave", "Springfield", "VA", "22150", 525000.0, 3, 2.0f, 1450, 38.7791f, -77.1901f, "2026-09-02", "active", "Updated kitchen, fenced yard, close to schools.");
            Listing l4 = createMock("B8", "MLS_B", "456 Oak Avenue", "Springfield", "VA", "22151", 527500.0, 3, 2.0f, 1450, 38.7791f, -77.1901f, "2026-08-30", "active", "Renovated kitchen, fenced backyard, near schools.");
            Listing l5 = createMock("A3", "MLS_A", "789 Pine Rd", "Fairfax", "VA", "22030", 399000.0, 2, 1.0f, 850, 38.8462f, -77.3064f, "2026-09-01", "active", "Cozy starter home, no pets.");
            Listing l6 = createMock("B9", "MLS_B", "789 Pine Rd", "Fairfax", "VA", "22030", 399500.0, 2, 1.0f, 850, 38.8462f, -77.3064f, "2026-08-25", "active", "Cozy starter home, pets not permitted.");
            Listing l7 = createMock("A4", "MLS_A", "22 Birch Ln", "Reston", "VA", "20190", 610000.0, 4, 3.0f, 2100, 38.9586f, -77.3570f, "2026-09-03", "active", "Spacious family home near Reston Town Center. Pets welcome.");
            Listing l8 = createMock("B10", "MLS_B", "100 Maple Dr", "Reston", "VA", "20190", 585000.0, 3, 2.5f, 1900, 38.9601f, -77.3499f, "2026-08-20", "active", "Townhome with 2-car garage, community pool.");
            Listing l9 = createMock("A5", "MLS_A", "55 Elm Ct", "Vienna", "VA", "22180", 470000.0, 3, 2.0f, 1300, 38.9012f, -77.2653f, "2026-09-04", "active", "Quiet cul-de-sac, walkable to Metro. No pets.");
            Listing l10 = createMock("B11", "MLS_B", "55 Elm Court", "Vienna", "VA", "22180", 465000.0, 3, 2.0f, 1300, 38.9012f, -77.2653f, "2026-08-15", "active", "Peaceful street, close to Metro. Pet restrictions apply.");
            Listing l11 = createMock("A6", "MLS_A", "300 Cedar Blvd", "Manassas", "VA", "20110", 415000.0, 3, 2.0f, 1600, 38.7509f, -77.4753f, "2026-08-10", "active", "Split-level home, large driveway, pets allowed.");
            Listing l12 = createMock("A7", "MLS_A", "42 Willow Way", "Chantilly", "VA", "20151", 540000.0, 4, 2.5f, 1950, 38.8909f, -77.4316f, "2026-07-28", "pending", "Corner lot, recently painted, no pets due to HOA.");

            listingRepository.saveAll(Arrays.asList(l1, l2, l3, l4, l5, l6, l7, l8, l9, l10, l11, l12));
            System.out.println("✅ Data seeding complete! 12 upgraded properties loaded safely.");
        }
    }

    // --- UPDATED CREATE_MOCK HELPER TO SUPPORT 15 PARAMETERS ---
    private Listing createMock(String id, String source, String address, String city, String state, String zip,
                               Double price, Integer beds, Float baths, Integer sqft, 
                               Float lat, Float lon, String dateStr, String status, String desc) {
        Listing listing = new Listing();
        listing.setId(id);
        listing.setSource(source);
        listing.setAddress(address);
        listing.setCity(city);
        listing.setState(state);
        listing.setZip(zip);
        listing.setPrice(price);
        listing.setBedrooms(beds);
        listing.setBathrooms(baths);
        listing.setSqft(sqft);
        listing.setLatitude(lat);
        listing.setLongitude(lon);
        listing.setListedDate(LocalDate.parse(dateStr)); // Parses date strings natively into LocalDate
        listing.setStatus(status);
        listing.setDescription(desc);
        return listing;
    }
}
