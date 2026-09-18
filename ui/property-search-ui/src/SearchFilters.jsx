
import SearchItem from './SearchItem.jsx';

export default function SearchFilters({filters, handleInputChange}) {
	return (
		
		  <div className="md:col-span-1 bg-white p-6 rounded-xl shadow-sm border border-gray-200">
		    <h2 className="text-xl font-bold mb-4 text-gray-800 border-b border-gray-100 pb-2">Query Engine</h2>
		    
		    <div className="space-y-4">

			<SearchItem filters={filters} handleInputChange={handleInputChange} type={"number"} htmlFor={"targetBudget"} label={"Target Budget ($)"} value={filters.targetBudget} />			            

			<SearchItem filters={filters} handleInputChange={handleInputChange} type={"text"} htmlFor={"city"} label={"City"} value={filters.city} />			            
			
			<div className="grid grid-cols-2 gap-2">
			  <SearchItem filters={filters} handleInputChange={handleInputChange} type={"number"} htmlFor={"minPrice"} label={"Min Price ($)"} value={filters.minPrice} />			            
			  <SearchItem filters={filters} handleInputChange={handleInputChange} type={"number"} htmlFor={"maxPrice"} label={"Max Price ($)"} value={filters.maxPrice} />			            
		      </div>

			<SearchItem filters={filters} handleInputChange={handleInputChange} type={"number"} htmlFor={"minBedrooms"} label={"Min Bedrooms"} value={filters.minBedrooms} />			            
			
			<SearchItem filters={filters} handleInputChange={handleInputChange} type={"text"} htmlFor={"keyword"} label={"Keyword"} value={filters.keyword} />		
				            
		      <button onClick={() => executeQuery(0)} className="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-medium py-2 px-4 rounded-md shadow transition duration-150 ease-in-out mt-2">
		        Search Listings
		      </button>
		    </div>
		  </div>
		
	);
}