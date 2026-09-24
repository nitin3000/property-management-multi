package com.propapp.model;

import java.io.Serializable;
import java.util.Objects;

public class ListingId implements Serializable {
    private String id;
    private String source;

    // Constructors
    public ListingId() {}

    public ListingId(String id, String source) {
        this.id = id;
        this.source = source;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    // Required hashcode/equals mapping for composite keys
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListingId listingId = (ListingId) o;
        return Objects.equals(id, listingId.id) && Objects.equals(source, listingId.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, source);
    }
}
