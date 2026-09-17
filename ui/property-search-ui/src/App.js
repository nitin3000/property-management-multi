import React, { useState, useEffect } from 'react';

export default function App() {
  const PAGE_SIZE = 4;

  const [filters, setFilters] = useState({
    targetBudget: '550000',
    city: '',
    minPrice: '',
    maxPrice: '',
    minBedrooms: '',
    keyword: '',
  });

  const [currentPage, setCurrentPage] = useState(0);
  const [listings, setListings] = useState([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState(null);

  const handleInputChange = (e) => {
    const { id, value } = e.target;
    setFilters((prev) => ({ ...prev, [id]: value }));
  };

  const executeQuery = async (targetPage) => {
    setLoading(true);
    setErrorMsg(null);

    let query = `page=${targetPage}&size=${PAGE_SIZE}`;
    Object.keys(filters).forEach((key) => {
      if (filters[key]) {
        query += `&${key}=${encodeURIComponent(filters[key])}`;
      }
    });

    try {
      // Direct absolute URL mapping straight to your running Java port
      const response = await fetch(`/api/listings/search?${query}`);
      const data = await response.json();

      if (!response.ok) {
        setErrorMsg(data.error || 'Failed to retrieve listings.');
        setListings([]);
        setTotalElements(0);
        setTotalPages(0);
        return;
      }

      setCurrentPage(targetPage);
      setListings(data.content || []);
      setTotalElements(data.totalElements || 0);
      setTotalPages(data.totalPages || 0);
    } catch (err) {
      setErrorMsg('Critical runtime connection failure.');
      setListings([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    executeQuery(0);
  }, []);

  return (
    <div>
      <header>
        <h1>PropApp Intelligence Portal</h1>
      </header>

      <div>
        {/* Left Column: Form Controls */}
        <div>
          <h2>Query Engine</h2>
          
          {errorMsg && (
            <div style={{ color: 'red', border: '1px solid red', padding: '5px', marginBottom: '10px' }}>
              {errorMsg}
            </div>
          )}

          <div>
            <label>Target Budget (\$)</label><br />
            <input type="number" id="targetBudget" value={filters.targetBudget} onChange={handleInputChange} />
          </div>
          
          <div>
            <label>City Filter</label><br />
            <input type="text" id="city" placeholder="e.g. Miami" value={filters.city} onChange={handleInputChange} />
          </div>
          
          <div>
            <label>Min Price</label><br />
            <input type="number" id="minPrice" value={filters.minPrice} onChange={handleInputChange} />
          </div>
          
          <div>
            <label>Max Price</label><br />
            <input type="number" id="maxPrice" value={filters.maxPrice} onChange={handleInputChange} />
          </div>
          
          <div>
            <label>Min Bedrooms</label><br />
            <input type="number" id="minBedrooms" value={filters.minBedrooms} onChange={handleInputChange} />
          </div>
          
          <div>
            <label>Description Keyword</label><br />
            <input type="text" id="keyword" placeholder="e.g. ocean" value={filters.keyword} onChange={handleInputChange} />
          </div>

          <br />
          <button onClick={() => executeQuery(0)}>Search Properties</button>
        </div>

        <hr />

        {/* Right Column: Dynamic Feed Output */}
        <div>
          <div>
            <span>
              {loading ? "Processing database..." : `${totalElements} matches ranked via algorithms`}
            </span>
            {" | "}
            <button disabled={currentPage === 0 || loading} onClick={() => executeQuery(currentPage - 1)}>Prev</button>
            <span> Page {totalPages === 0 ? 0 : currentPage + 1} of {totalPages} </span>
            <button disabled={currentPage >= totalPages - 1 || loading} onClick={() => executeQuery(currentPage + 1)}>Next</button>
          </div>

          <br />

          <div>
            {loading ? (
              <p>Scanning table index...</p>
            ) : listings.length === 0 ? (
              <p>No properties match your filter selection.</p>
            ) : (
              listings.map((item, index) => (
                <div key={item.id || index} style={{ border: '1px solid #ccc', padding: '10px', marginBottom: '10px' }}>
                  <strong>[{item.source}] {item.address}</strong>
                  <p>{item.city}, {item.state} | {item.bedrooms} Beds | Listed: {item.listedDate}</p>
                  <p>Price: \${item.price?.toLocaleString()} | <strong>Score: {item.relevanceScore}%</strong></p>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
