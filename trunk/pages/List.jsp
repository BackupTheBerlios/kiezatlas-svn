<%@ include file="KiezAtlas.jsp" %>

<% begin(session, out); %>
<%
	String mapName = (String) session.getAttribute("mapName");
	Vector insts = (Vector) session.getAttribute("insts");
	// --- list of institutions ---
	out.println("<h2>" + mapName + "</h2>");
	out.println(insts.size() + " Einrichtungen<br><br><br>");
	out.println("<table cellpadding=\"4\" cellspacing=\"0\">");
	out.println("<tr>" +
		"<td class=\"small\"></td><td class=\"small\">Stra&szlig;e</td><td class=\"small\">PLZ</td><td class=\"small\">Ort</td>" +
		"<td class=\"small\">Website</td><td class=\"small\">Ansprechpartner/in</td>" +
		"<td class=\"small\">Tel</td><td class=\"small\">Fax</td><td class=\"small\">Email</td><td class=\"small\">Alias</td>" +
		"<td class=\"small\">YADE x</td><td class=\"small\">YADE y</td></tr>");
	Enumeration e = insts.elements();
	while (e.hasMoreElements()) {
		Institution inst = (Institution) e.nextElement();
		out.println("<tr valign=\"top\">");
		out.println("<td><b>" + inst.name + "</b></td>");
		out.println("<td>" + inst.street + "</td>");
		out.println("<td>" + inst.postalCode + "</td>");
		out.println("<td>" + inst.city + "</td>");
		out.println("<td>" + inst.webpageURL + "</td>");
		out.println("<td>" + inst.person + "</td>");
		out.println("<td>" + inst.phone + "</td>");
		out.println("<td>" + inst.fax + "</td>");
		out.println("<td>" + inst.email + "</td>");
		out.println("<td>" + inst.webAlias + "</td>");
		out.println("<td>" + inst.yadeX + "</td>");
		out.println("<td>" + inst.yadeY + "</td>");
		out.println("</tr>");
	}
	out.println("</table>");
	
%>
<% end(out); %>
