package com.propapp.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "listings")
@IdClass(ListingId.class)
public class Listing {

    @Id
    private String id;

    @Id
    private String source;

    private String address;
    private String city;
    private String state;
    private String zip;
    private Double price;
    private Integer bedrooms;
    private Float bathrooms;
    private Integer sqft;
    private Float latitude;
    private Float longitude;

    @Column(name = "listed_date")
    private LocalDate listedDate;
    
    private String status;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Empty Constructor
    public Listing() {}

    // --- STANDARD JAVA GETTERS & SETTERS ---
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

    public String getZip() { return zip; }
    public void setZip(String zip) { this.zip = zip; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Integer getBedrooms() { return bedrooms; }
    public void setBedrooms(Integer bedrooms) { this.bedrooms = bedrooms; }

    public Float getBathrooms() { return bathrooms; }
    public void setBathrooms(Float bathrooms) { this.bathrooms = bathrooms; }

    public Integer getSqft() { return sqft; }
    public void setSqft(Integer sqft) { this.sqft = sqft; }

    public Float getLatitude() { return latitude; }
    public void setLatitude(Float latitude) { this.latitude = latitude; }

    public Float getLongitude() { return longitude; }
    public void setLongitude(Float longitude) { this.longitude = longitude; }

    public LocalDate getListedDate() { return listedDate; }
    public void setListedDate(LocalDate listedDate) { this.listedDate = listedDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
