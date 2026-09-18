export default function SearchItem({filters, handleInputChange, type , htmlFor, value, label}) {
	
	if (type == 'text') {
		return (
			<div>
			  <label htmlFor={htmlFor} className="block text-sm font-semibold text-gray-700 mb-1">{label}</label>
			  <input type={type} id={htmlFor} value={value} onChange={handleInputChange} className="w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm border p-2" />
			</div>
		);
	} else if (type == 'number') {
		return (
				<div>
		  			<label htmlFor={htmlFor} className="block text-sm font-semibold text-gray-700 mb-1">{label}</label>
		  			<input type={type} id={htmlFor} value={value} onChange={handleInputChange} className="w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm border p-2" />
				</div>
		);
	}
}
