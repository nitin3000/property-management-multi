export default function ResultCell({ id, classNm = "" }){
	return (
		<td className={"px-6 py-4 " + classNm}>{id}</td>
	);