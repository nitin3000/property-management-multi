package com.propapp.repository;

import com.propapp.model.Listing;
import com.propapp.model.ListingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ListingRepository extends JpaRepository<Listing, ListingId> {
    
    // --- ADD THIS LINE (Fixes the missing symbol compilation failure) ---
    @Query("SELECT COUNT(l) > 0 FROM Listing l WHERE LOWER(CAST(l.city AS string)) = LOWER(CAST(:city AS string))")
    boolean existsByCityIgnoreCase(@Param("city") String city);

    @Query("SELECT l FROM Listing l WHERE " +
           "(:city IS NULL OR LOWER(CAST(l.city AS string)) = LOWER(CAST(:city AS string))) AND " +
           "(:minPrice IS NULL OR l.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR l.price <= :maxPrice) AND " +
           "(:minBedrooms IS NULL OR l.bedrooms >= :minBedrooms) AND " +
           "(:keyword IS NULL OR LOWER(CAST(l.description AS string)) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')))")
    List<Listing> findWithFilters(
            @Param("city") String city,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("minBedrooms") Integer minBedrooms,
            @Param("keyword") String keyword
    );
}
