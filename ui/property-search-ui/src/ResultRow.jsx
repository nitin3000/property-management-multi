import ResultCell from './ResultCell.jsx';

export default function ResultRow({ item }){
	return (
	  <tr key={item.id} className="hover:bg-gray-50 transition duration-150">
	  	<ResultCell id={item.id} classNm={"font-semibold text-gray-900"} />
		<ResultCell id={item.city} classNm={"font-semibold text-gray-900"} />
		<ResultCell id={"$" + Number(item.price).toLocaleString()} classNm={"font-semibold text-indigo-600"} />
		<ResultCell id={item.listedDate} classNm={"font-semibold text-indigo-600"} />
		<ResultCell id={item.bedrooms} classNm={""} />
		<ResultCell id={item.keyword || item.description || 'N/A'} classNm={"text-gray-600 truncate max-w-xs"} />
		<ResultCell id={item.relevanceScore + "%"} classNm={"font-semibold text-indigo-600"} />
	</tr>
	);
}
