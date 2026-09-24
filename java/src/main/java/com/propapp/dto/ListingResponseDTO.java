package com.propapp.dto;

import com.propapp.model.Listing;
import java.time.LocalDate;

public class ListingResponseDTO {
    private String id;
    private String source;
    private String address;
    private String city;
    private String state;
    private Double price;
    private Integer bedrooms;
    private Float bathrooms;   // Fixed to Float
    private Integer sqft;       
    private Float latitude;    // Fixed to Float
    private Float longitude;   // Fixed to Float
    private LocalDate listedDate;
    private String description;
    private double relevanceScore;

    // Constructor
    public ListingResponseDTO(Listing listing, double relevanceScore) {
        this.id = listing.getId();
        this.source = listing.getSource();
        this.address = listing.getAddress();
        this.city = listing.getCity();
        this.state = listing.getState();
        this.price = listing.getPrice();
        this.bedrooms = listing.getBedrooms();
        this.bathrooms = listing.getBathrooms(); // Compiles perfectly now!
        this.sqft = listing.getSqft();           
        this.latitude = listing.getLatitude();   // Compiles perfectly now!
        this.longitude = listing.getLongitude(); // Compiles perfectly now!
        this.listedDate = listing.getListedDate();
        this.description = listing.getDescription(); 
        this.relevanceScore = Math.round(relevanceScore * 100.0) / 100.0;
    }

    // --- STANDARD JAVA GETTERS & SETTERS (Fixes compilation errors) ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Integer getBedrooms() { return bedrooms; }
    public void setBedrooms(Integer bedrooms) { this.bedrooms = bedrooms; }

    public LocalDate getListedDate() { return listedDate; }
    public void setListedDate(LocalDate listedDate) { this.listedDate = listedDate; }

    public double getRelevanceScore() { return relevanceScore; }
    public void setRelevanceScore(double relevanceScore) { this.relevanceScore = relevanceScore; }
    
 // 3. Update the matching Getters and Setters further down:
    public Float getBathrooms() { return bathrooms; }
    public void setBathrooms(Float bathrooms) { this.bathrooms = bathrooms; }

    public Float getLatitude() { return latitude; }
    public void setLatitude(Float latitude) { this.latitude = latitude; }

    public Float getLongitude() { return longitude; }
    public void setLongitude(Float longitude) { this.longitude = longitude; }
}
