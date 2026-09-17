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
    <div className="min-h-screen bg-gray-50 p-6 text-gray-900 font-sans">
      <header className="mb-8 border-b border-gray-200 pb-4">
        <h1 className="text-3xl font-extrabold tracking-tight text-indigo-700">PropApp Intelligence Portal</h1>
      </header>

      <div className="grid grid-cols-1 gap-8 md:grid-cols-3">
        {/* Left Column: Form Controls */}
        <div className="md:col-span-1 bg-white p-6 rounded-xl shadow-sm border border-gray-200">
          <h2 className="text-xl font-bold mb-4 text-gray-800 border-b border-gray-100 pb-2">Query Engine</h2>
          
          <div className="space-y-4">
            <div>
              <label htmlFor="targetBudget" className="block text-sm font-semibold text-gray-700 mb-1">Target Budget ($)</label>
              <input type="number" id="targetBudget" value={filters.targetBudget} onChange={handleInputChange} className="w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm border p-2" />
            </div>
            <div>
              <label htmlFor="city" className="block text-sm font-semibold text-gray-700 mb-1">City</label>
              <input type="text" id="city" value={filters.city} onChange={handleInputChange} className="w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm border p-2" />
            </div>
            <div className="grid grid-cols-2 gap-2">
              <div>
                <label htmlFor="minPrice" className="block text-sm font-semibold text-gray-700 mb-1">Min Price ($)</label>
                <input type="number" id="minPrice" value={filters.minPrice} onChange={handleInputChange} className="w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm border p-2" />
              </div>
              <div>
                <label htmlFor="maxPrice" className="block text-sm font-semibold text-gray-700 mb-1">Max Price ($)</label>
                <input type="number" id="maxPrice" value={filters.maxPrice} onChange={handleInputChange} className="w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm border p-2" />
              </div>
            </div>
            <div>
              <label htmlFor="minBedrooms" className="block text-sm font-semibold text-gray-700 mb-1">Min Bedrooms</label>
              <input type="number" id="minBedrooms" value={filters.minBedrooms} onChange={handleInputChange} className="w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm border p-2" />
            </div>
            <div>
              <label htmlFor="keyword" className="block text-sm font-semibold text-gray-700 mb-1">Keyword</label>
              <input type="text" id="keyword" value={filters.keyword} onChange={handleInputChange} className="w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm border p-2" />
            </div>
            
            <button onClick={() => executeQuery(0)} className="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-medium py-2 px-4 rounded-md shadow transition duration-150 ease-in-out mt-2">
              Search Listings
            </button>
          </div>
        </div>

        {/* Right Column: Status & Results Table */}
        <div className="md:col-span-2 bg-white p-6 rounded-xl shadow-sm border border-gray-200">
          <h2 className="text-xl font-bold mb-4 text-gray-800 border-b border-gray-100 pb-2">
            Search Results <span className="bg-gray-100 text-gray-700 text-sm font-semibold ml-2 px-2.5 py-0.5 rounded-full">{totalElements} total</span>
          </h2>

          {errorMsg && (
            <div className="bg-red-50 border-l-4 border-red-400 p-4 mb-4 rounded shadow-sm text-sm text-red-700">
              <strong className="font-bold">Error:</strong> {errorMsg}
            </div>
          )}

          {loading ? (
            <div className="flex justify-center py-12">
              <p className="text-gray-500 text-sm animate-pulse">Loading market data...</p>
            </div>
          ) : listings.length === 0 ? (
            <div className="text-center py-12 border-2 border-dashed border-gray-200 rounded-lg">
              <p className="text-gray-500">No properties match your active search filters.</p>
            </div>
          ) : (
            <>
              <div className="overflow-x-auto rounded-lg border border-gray-200">
                <table className="min-w-full divide-y divide-gray-200 text-sm text-left">
                  <thead className="bg-gray-50 text-xs font-semibold text-gray-600 uppercase tracking-wider">
                    <tr>
                      <th className="px-6 py-3">ID</th>
                      <th className="px-6 py-3">City</th>
                      <th className="px-6 py-3">Price</th>
					  <th className="px-6 py-3">Listed</th>
                      <th className="px-6 py-3">Beds</th>
                      <th className="px-6 py-3">Description</th>
                      <th className="px-6 py-3">Score</th>
					</tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {listings.map((item) => (
                      <tr key={item.id} className="hover:bg-gray-50 transition duration-150">
                        <td className="px-6 py-4 font-mono font-medium text-gray-500">{item.id}</td>
                        <td className="px-6 py-4 font-semibold text-gray-900">{item.city}</td>
                        <td className="px-6 py-4 font-semibold text-indigo-600">${Number(item.price).toLocaleString()}</td>
						<td className="px-6 py-4 font-semibold text-indigo-600">{item.listedDate}</td>
                        <td className="px-6 py-4">{item.bedrooms}</td>
                        <td className="px-6 py-4 text-gray-600 truncate max-w-xs">{item.keyword || item.description || 'N/A'}</td>
                        <td className="px-6 py-4 font-semibold text-indigo-600">{item.relevanceScore}%</td>
					</tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {/* Pagination Controls */}
              <div className="mt-6 flex items-center justify-between border-t border-gray-200 pt-4">
                <button 
                  disabled={currentPage === 0} 
                  onClick={() => executeQuery(currentPage - 1)}
                  className="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium bg-white text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed shadow-sm transition"
                >
                  Previous
                </button>
                <span className="text-sm text-gray-600">
                  Page <span className="font-semibold text-gray-900">{currentPage + 1}</span> of <span className="font-semibold text-gray-900">{totalPages || 1}</span>
                </span>
                <button 
                  disabled={currentPage >= totalPages - 1} 
                  onClick={() => executeQuery(currentPage + 1)}
                  className="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium bg-white text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed shadow-sm transition"
                >
                  Next
                </button>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
