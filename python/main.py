import math
import os
from datetime import datetime, date
from fastapi import FastAPI, HTTPException, Query, status
from fastapi.responses import FileResponse
from pydantic import BaseModel
from typing import List, Optional

# --- POSTGRESQL CONNECTION SETUP ---
from sqlalchemy import create_engine, Column, String, Integer, Float, Date
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker

# Aligned credentials from your Spring Boot setup
DATABASE_URL = "postgresql://appuser:apppassword@localhost:5432/propdb"

engine = create_engine(DATABASE_URL)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()

# --- DATABASE MODEL SYNCHRONIZED EXACTLY WITH YOUR JAVA ENTITY ---
class ListingModel(Base):
    __tablename__ = "listings"

    # Aligned Composite Primary Key Constraint (@IdClass)
    id = Column(String, primary_key=True, index=True)
    source = Column(String, primary_key=True, index=True)
    
    address = Column(String)
    city = Column(String)
    state = Column(String)
    zip = Column(String)
    price = Column(Float)
    bedrooms = Column(Integer)
    bathrooms = Column(Float)
    sqft = Column(Integer)
    latitude = Column(Float)
    longitude = Column(Float)
    
    # Aligned explicitly from @Column(name = "listed_date") as a pure SQL Date type
    listed_date = Column("listed_date", Date)
    
    status = Column(String)
    description = Column(String)

app = FastAPI(title="PropApp Search Engine API Portal")

# --- PYDANTIC SCHEMAS MATCHING FRONTEND DATA KEY EXPECTATIONS ---
class ScoredListing(BaseModel):
    id: str
    source: str
    address: str
    city: str
    state: str
    price: float
    bedrooms: int
    listedDate: str  # Converted to ISO string format for safe frontend rendering
    relevanceScore: float  # camelCase key explicitly matching your javascript layout logic

    class Config:
        from_attributes = True

class PaginatedResponse(BaseModel):
    totalElements: int  # Aligned to match the pagination status tracking metrics
    totalPages: int
    pageNumber: int
    pageSize: int
    content: List[ScoredListing] # Aligned from results to content structure

# --- ROOT FRONTEND VIEW GATEWAY ---
@app.get("/")
def read_root():
    return FileResponse("index.html")

# --- DATA-DRIVEN SEARCH AND SCORING ENGINE ---
@app.get("/api/listings/search", response_model=PaginatedResponse)
def search_listings(
    minPrice: Optional[float] = None,
    maxPrice: Optional[float] = None,
    minBedrooms: Optional[int] = None,
    city: Optional[str] = None,
    keyword: Optional[str] = None,
    targetBudget: Optional[float] = None,
    page: int = Query(0, ge=0), 
    size: int = Query(4, gt=0) 
):
    db = SessionLocal()
    try:
        # Deliberate input sanity error validations
        if minPrice is not None and maxPrice is not None and minPrice > maxPrice:
            raise HTTPException(status_code=400, detail="Minimum price filtering threshold cannot be greater than maximum price.")
        if (minPrice is not None and minPrice < 0) or (maxPrice is not None and maxPrice < 0):
            raise HTTPException(status_code=400, detail="Price filters cannot be negative numbers.")

        # Build Postgres Query execution plan
        query = db.query(ListingModel)

        if minPrice is not None:
            query = query.filter(ListingModel.price >= minPrice)
        if maxPrice is not None:
            query = query.filter(ListingModel.price <= maxPrice)
        if minBedrooms is not None:
            query = query.filter(ListingModel.bedrooms >= minBedrooms)
        if city:
            query = query.filter(ListingModel.city.ilike(f"%{city.strip()}%"))
        if keyword:
            query = query.filter(ListingModel.description.ilike(f"%{keyword.strip()}%"))

        db_listings = query.all()

        if city and not db_listings:
            raise HTTPException(status_code=404, detail=f"No matching listings found matching city: '{city}'.")

        scored_results = []
        current_datetime = datetime.strptime("2026-09-12", "%Y-%m-%d")

        for item in db_listings:
            # A. Budget proximity calculation (Gaussian decay model)
            if targetBudget and targetBudget > 0:
                price_delta = abs(item.price - targetBudget)
                budget_score = math.exp(- (price_delta / targetBudget) ** 2)
            else:
                budget_score = 1.0

            # B. Temporal Recency calculation handling real Date entities safely
            if item.listed_date:
                # Convert date object into datetime context to run math deltas accurately
                listing_datetime = datetime.combine(item.listed_date, datetime.min.time())
                days_old = max(0, (current_datetime - listing_datetime).days)
                recency_score = math.exp(-days_old / 30.0)
                formatted_date = item.listed_date.isoformat()
            else:
                recency_score = 0.5
                formatted_date = "2026-09-01"

            # C. Combine components and map weights into clean percentage values (e.g., 85.5%)
            final_percentage_score = round(((budget_score * 0.7) + (recency_score * 0.3)) * 100, 1)

            scored_results.append(ScoredListing(
                id=item.id,
                source=item.source,
                address=item.address,
                city=item.city,
                state=item.state or "",
                price=item.price,
                bedrooms=item.bedrooms,
                listedDate=formatted_date,
                relevanceScore=final_percentage_score
            ))

        # Deterministic Sort: Descending relevance weight, then break ties ascending by price
        scored_results.sort(key=lambda x: (-x.relevanceScore, x.price))

        # Pagination computations matching Java Spring Pageable contracts
        total_elements = len(scored_results)
        total_pages = math.ceil(total_elements / size) if total_elements > 0 else 1

        start_idx = page * size
        paginated_results = scored_results[start_idx:start_idx + size]

        return PaginatedResponse(
            totalElements=total_elements,
            totalPages=total_pages,
            pageNumber=page,
            pageSize=size,
            content=paginated_results
        )
    finally:
        db.close()

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="127.0.0.1", port=8000, reload=True)
