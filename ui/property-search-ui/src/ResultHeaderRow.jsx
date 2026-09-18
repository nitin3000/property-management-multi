import ResultHeaderCell from './ResultHeaderCell.jsx';

export default function ResultHeaderRow(){
	return (
		<thead className="bg-gray-50 text-xs font-semibold text-gray-600 uppercase tracking-wider">
			<tr>
			  <ResultHeaderCell id="ID" />
			  <ResultHeaderCell id="City"  />
			  <ResultHeaderCell id="Price" />
			  <ResultHeaderCell id="Listed" />
			  <ResultHeaderCell id="Beds" />
			  <ResultHeaderCell id="Description" />
			  <ResultHeaderCell id="Score" />
			</tr>
		</thead>
	);
}
