package main

import (
    "context"
    "database/sql"
    "encoding/json"
    "errors"
    "fmt"
    "log"
    "math"
    "net/http"
    "strconv"
    "time"

   _ "github.com/lib/pq" 
)

// ==========================================
// 1. DATA MODELS & STRUCTS
// ==========================================

type Listing struct {
	ID             string    `json:"id"`
	Source         string    `json:"source"`
	Address        string    `json:"address"`
	City           string    `json:"city"`
	State          string    `json:"state"`
	Zip            string    `json:"zip"`
	Price          float64   `json:"price"`
	Bedrooms       int       `json:"bedrooms"`
	Bathrooms      float32   `json:"bathrooms"`   // Maps to Java's Float
	Sqft           int       `json:"sqft"`
	Latitude       float32   `json:"latitude"`    // Maps to Java's Float
	Longitude      float32   `json:"longitude"`   // Maps to Java's Float
	ListedDate     time.Time `json:"listedDate"`  // Scanned safely from PG DATE
	Status         string    `json:"status"`
	Description    string    `json:"description"`
	RelevanceScore float64   `json:"relevanceScore"`
}

// Mimics org.springframework.data.domain.Page exactly
type PaginatedResponse struct {
	Content          []Listing `json:"content"`          // Spring uses "content" instead of "data"
	Number           int       `json:"number"`           // Spring uses "number" for current page index
	Size             int       `json:"size"`             // Spring uses "size" for pageSize
	TotalPages       int       `json:"totalPages"`       // Matches Spring standard
	TotalElements    int       `json:"totalElements"`    // Spring uses "totalElements" instead of "totalResults"
	NumberOfElements int       `json:"numberOfElements"` // Items on current page
	First            bool      `json:"first"`            // Boolean indicators for buttons
	Last             bool      `json:"last"`             // Boolean indicators for buttons
	Empty            bool      `json:"empty"`            // Empty array state indicator
}


type SearchServer struct {
	DB *sql.DB
}

// ==========================================
// 2. SEARCH ENGINE LOGIC
// ==========================================

func (s *SearchServer) ExecuteSearch(ctx context.Context, r *http.Request) (PaginatedResponse, error) {
	q := r.URL.Query()

	page, _ := strconv.Atoi(q.Get("page"))
	pageSize, _ := strconv.Atoi(q.Get("pageSize"))
	if page < 0 {
		return PaginatedResponse{}, errors.New("validation error: Page index cannot be negative")
	}
	if pageSize <= 0 {
		pageSize = 10
	}

	var minPrice, maxPrice, targetBudget *float64
	var minBedrooms *int

	if v, err := strconv.ParseFloat(q.Get("minPrice"), 64); err == nil { minPrice = &v }
	if v, err := strconv.ParseFloat(q.Get("maxPrice"), 64); err == nil { maxPrice = &v }
	if v, err := strconv.ParseFloat(q.Get("targetBudget"), 64); err == nil { targetBudget = &v }
	if v, err := strconv.Atoi(q.Get("minBedrooms")); err == nil { minBedrooms = &v }

	if minPrice != nil && maxPrice != nil && *minPrice > *maxPrice {
		return PaginatedResponse{}, errors.New("validation error: minPrice cannot be greater than maxPrice")
	}
	if (minPrice != nil && *minPrice < 0) || (maxPrice != nil && *maxPrice < 0) {
		return PaginatedResponse{}, errors.New("validation error: Financial boundaries cannot be negative")
	}

	city := q.Get("city")
	keyword := q.Get("keyword")

	// Target Table updated to "listings" to match your Java @Table declaration.
	// Scoring extracts epoch intervals directly from DATE structures seamlessly.
	query := `
		SELECT 
			id, 
			source, 
			COALESCE(address, '') AS address, 
			COALESCE(city, '') AS city, 
			COALESCE(state, '') AS state, 
			COALESCE(zip, '') AS zip, 
			COALESCE(price, 0.0) AS price, 
			COALESCE(bedrooms, 0) AS bedrooms, 
			COALESCE(bathrooms, 0.0) AS bathrooms, 
			COALESCE(sqft, 0) AS sqft, 
			COALESCE(latitude, 0.0) AS latitude, 
			COALESCE(longitude, 0.0) AS longitude, 
			COALESCE(listed_date, CURRENT_DATE) AS listed_date, 
			COALESCE(status, '') AS status, 
			COALESCE(description, '') AS description,
			ROUND(((EXP(-POW((price - COALESCE($1, price)) / 50000.0, 2)) * 0.7) + (EXP(-EXTRACT(EPOCH FROM (NOW() - listed_date)) / (24 * 60 * 60 * 30.0)) * 0.3))::numeric * 10, 2) AS relevance_score,
			COUNT(*) OVER() as total_count
		FROM listings
		WHERE 1=1
	`

	var args []interface{}
	args = append(args, targetBudget) // $1
	placeholderIdx := 2

	if minPrice != nil { query += fmt.Sprintf(" AND price >= $%d", placeholderIdx); args = append(args, *minPrice); placeholderIdx++ }
	if maxPrice != nil { query += fmt.Sprintf(" AND price <= $%d", placeholderIdx); args = append(args, *maxPrice); placeholderIdx++ }
	if minBedrooms != nil { query += fmt.Sprintf(" AND bedrooms >= $%d", placeholderIdx); args = append(args, *minBedrooms); placeholderIdx++ }
	if city != "" { query += fmt.Sprintf(" AND city ILIKE $%d", placeholderIdx); args = append(args, city); placeholderIdx++ }
	if keyword != "" { query += fmt.Sprintf(" AND description ILIKE $%d", placeholderIdx); args = append(args, "%"+keyword+"%"); placeholderIdx++ }

	query += " ORDER BY relevance_score DESC, source ASC, id ASC"

	offset := page * pageSize
	query += fmt.Sprintf(" LIMIT $%d OFFSET $%d", placeholderIdx, placeholderIdx+1)
	args = append(args, pageSize, offset)

	rows, err := s.DB.QueryContext(ctx, query, args...)
	if err != nil {
		return PaginatedResponse{}, fmt.Errorf("database query failure: %w", err)
	}
	defer rows.Close()

	var listings []Listing
	totalResults := 0

	for rows.Next() {
		var l Listing
		err := rows.Scan(
			&l.ID, &l.Source, &l.Address, &l.City, &l.State, &l.Zip, &l.Price,
			&l.Bedrooms, &l.Bathrooms, &l.Sqft, &l.Latitude, &l.Longitude,
			&l.ListedDate, &l.Status, &l.Description, &l.RelevanceScore, &totalResults,
		)
		if err != nil {
			return PaginatedResponse{}, fmt.Errorf("row mapping scanning error: %w", err)
		}
		listings = append(listings, l)
	}

	// Calculate total pages safely matching Spring metrics
	totalPages := int(math.Ceil(float64(totalResults) / float64(pageSize)))
	if totalPages == 0 {
		totalPages = 1
	}

	// Safe structural protection if database returns zero results
	if listings == nil {
		listings = []Listing{}
	}

	// Return response matching Spring Data REST standards exactly
	return PaginatedResponse{
		Content:          listings,
		Number:           page, // 0-indexed page number matching Spring
		Size:             pageSize,
		TotalPages:       totalPages,
		TotalElements:    totalResults,
		NumberOfElements: len(listings),
		First:            page == 0,
		Last:             page >= totalPages-1,
		Empty:            len(listings) == 0,
	}, nil

}


// ==========================================
// 3. HTTP ENGINE HANDLER
// ==========================================

func main() {
	// Credentials set to your specific spring.datasource variables
	connStr := "postgres://appuser:apppassword@localhost:5432/propdb?sslmode=disable"
	db, err := sql.Open("postgres", connStr)
	if err != nil {
		log.Fatalf("Critical database connectivity failure: %v", err)
	}
	defer db.Close()

	if err := db.Ping(); err != nil {
		log.Fatalf("Database verification handshake failed: %v", err)
	}

	server := &SearchServer{DB: db}

	http.HandleFunc("/api/listings/search", func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		w.Header().Set("Access-Control-Allow-Origin", "*")

		if r.Method != http.MethodGet {
			w.WriteHeader(http.StatusMethodNotAllowed)
			json.NewEncoder(w).Encode(map[string]string{"error": "Method not allowed"})
			return
		}

		response, err := server.ExecuteSearch(r.Context(), r)
		if err != nil {
			w.WriteHeader(http.StatusBadRequest)
			json.NewEncoder(w).Encode(map[string]string{"error": err.Error()})
			return
		}

		json.NewEncoder(w).Encode(response)
	})


http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
    http.ServeFile(w, r, "index.html")
})

	fmt.Println("📡 Go Server running securely on http://localhost:8080")
	log.Fatal(http.ListenAndServe(":8080", nil))
}
