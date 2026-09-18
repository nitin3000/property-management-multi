
export default function ResultHeaderCell({ id, classNm = "" }){
	return (
		<th className={"px-6 py-4 " + classNm}>{id}</th>
	);
}