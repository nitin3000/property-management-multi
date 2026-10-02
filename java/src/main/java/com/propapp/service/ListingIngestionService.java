package com.propapp.service;

import com.propapp.model.Listing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Date;
import java.util.List;

@Service
public class ListingIngestionService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional
    public void upsertListingsBatch(List<Listing> listings) {
        String sql = "INSERT INTO listings (id, source, address, city, state, zip, price, bedrooms, bathrooms, sqft, latitude, longitude, listed_date, status, description) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON CONFLICT (id, source) " +
                     "DO UPDATE SET " +
                     "  address = EXCLUDED.address, " +
                     "  city = EXCLUDED.city, " +
                     "  state = EXCLUDED.state, " +
                     "  zip = EXCLUDED.zip, " +
                     "  price = EXCLUDED.price, " +
                     "  bedrooms = EXCLUDED.bedrooms, " +
                     "  bathrooms = EXCLUDED.bathrooms, " +
                     "  sqft = EXCLUDED.sqft, " +
                     "  latitude = EXCLUDED.latitude, " +
                     "  longitude = EXCLUDED.longitude, " +
                     "  listed_date = EXCLUDED.listed_date, " +
                     "  status = EXCLUDED.status, " +
                     "  description = EXCLUDED.description";

        // Bypasses Hibernate lifecycle to send a high-speed multi-row network packet
        jdbcTemplate.batchUpdate(sql, listings, listings.size(), (ps, listing) -> {
            ps.setString(1, listing.getId());
            ps.setString(2, listing.getSource());
            ps.setString(3, listing.getAddress());
            ps.setString(4, listing.getCity());
            ps.setString(5, listing.getState());
            ps.setString(6, listing.getZip());
            
            // Safe mapping for wrapper objects that could be null
            if (listing.getPrice() != null) ps.setDouble(7, listing.getPrice()); else ps.setNull(7, java.sql.Types.DOUBLE);
            if (listing.getBedrooms() != null) ps.setInt(8, listing.getBedrooms()); else ps.setNull(8, java.sql.Types.INTEGER);
            if (listing.getBathrooms() != null) ps.setFloat(9, listing.getBathrooms()); else ps.setNull(9, java.sql.Types.REAL);
            if (listing.getSqft() != null) ps.setInt(10, listing.getSqft()); else ps.setNull(10, java.sql.Types.INTEGER);
            if (listing.getLatitude() != null) ps.setFloat(11, listing.getLatitude()); else ps.setNull(11, java.sql.Types.REAL);
            if (listing.getLongitude() != null) ps.setFloat(12, listing.getLongitude()); else ps.setNull(12, java.sql.Types.REAL);
            
            // Convert LocalDate to java.sql.Date safely
            if (listing.getListedDate() != null) {
                ps.setDate(13, Date.valueOf(listing.getListedDate()));
            } else {
                ps.setNull(13, java.sql.Types.DATE);
            }
            
            ps.setString(14, listing.getStatus());
            ps.setString(15, listing.getDescription());
        });
    }
}
