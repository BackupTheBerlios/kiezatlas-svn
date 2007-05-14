<%@ include file="KiezAtlas.jsp" %>

<% begin(session, out); %>
<%
	HTMLGenerator html = (HTMLGenerator) session.getAttribute("html");
	String mapName = (String) session.getAttribute("mapName");
	String critName = (String) session.getAttribute("critName");
	Vector insts = (Vector) session.getAttribute("insts");
	// --- list of institutions ---
	out.println("<h2>" + mapName + "</h2>");
	out.println(insts.size() + " Einrichtungen<br><br><br>");
	out.println("<table cellpadding=\"4\" cellspacing=\"0\">");
	out.println("<tr>" +
		"<td class=\"small\"></td><td class=\"small\">Stra&szlig;e</td><td class=\"small\">PLZ</td><td class=\"small\">Ort</td>" +
		"<td class=\"small\">Website</td><td class=\"small\">Ansprechpartner/in</td><td class=\"small\">Tel</td>" +
		"<td class=\"small\">Fax</td><td class=\"small\">Email</td><td class=\"small\">" + critName + "</td>" +
		"<td class=\"small\">&Ouml;ffnungszeiten</td><td class=\"small\">Weitere&nbsp;Infos</td></tr>" +
		"<td class=\"small\">Alias</td><td class=\"small\">YADE x</td><td class=\"small\">YADE y</td></tr>");
	for (int i = 0; i < insts.size(); i++) {
		Institution inst = (Institution) insts.elementAt(i);
		out.println("<tr class=\"" + (i % 2 == 0 ? "evenrow" : "oddrow") + "\" valign=\"top\">");
		out.println("<td><b>" + inst.name + "</b></td>");
		out.println("<td>" + inst.street + "</td>");
		out.println("<td>" + inst.postalCode + "</td>");
		out.println("<td>" + inst.city + "</td>");
		out.println("<td>" + inst.webpageURL + "</td>");
		out.println("<td>" + inst.person + "</td>");
		out.println("<td>" + inst.phone + "</td>");
		out.println("<td>" + inst.fax + "</td>");
		out.println("<td>" + inst.email + "</td>");
		//
		// list categories of first search criteria
		out.println("<td>");
		Enumeration e = inst.categories[0].elements();
		while (e.hasMoreElements()) {
			BaseTopic topic = (BaseTopic) e.nextElement();
			out.println(html.imageTag(topic) + "&nbsp;" + topic.getName() + "<br>");
		}
		out.println("</td>");
		//
		out.println("<td>" + inst.open + "</td>");
		out.println("<td>" + inst.misc + "</td>");
		out.println("<td>" + inst.webAlias + "</td>");
		out.println("<td>" + inst.yadeX + "</td>");
		out.println("<td>" + inst.yadeY + "</td>");
		out.println("</tr>");
	}
	out.println("</table>");
	
%>
<% end(out); %>
