<%@ include file="KiezAtlas.jsp" %>

<% begin(session, out, true); %>
<%
	HTMLGenerator html = (HTMLGenerator) session.getAttribute("html");
	Vector insts = (Vector) session.getAttribute("institutions");
	Hashtable cats = (Hashtable) session.getAttribute("categories");
	Hashtable addresses = (Hashtable) session.getAttribute("addresses");
	String searchMode = (String) session.getAttribute("searchMode");
	String searchValue = (String) session.getAttribute("searchValue");
	// --- heading ---
	out.println("<p>");
	if (searchMode.equals(KiezAtlas.SEARCHMODE_BY_NAME)) {
		out.println("Suchergebnis ");
	}
	out.println("<b>" + searchValue + "</b><div class=\"small\">" + insts.size() + " Einrichtungen</div>");
	out.println("<p>");
	// --- list of institutions ---
	out.println("<table cellpadding=\"4\" cellspacing=\"0\">");
	Enumeration e = insts.elements();
	while (e.hasMoreElements()) {
		BaseTopic inst = (BaseTopic) e.nextElement();
		out.println("<tr valign=\"top\">");
		out.println("<td align=\"right\">");
		topicImages((Vector) cats.get(inst.getID()), html, out);
		out.println("</td>");
		out.println("<td>");
		out.println("<a href=\"controller?action=" + KiezAtlas.ACTION_SHOW_INSTITUTION_INFO + "&id=" + inst.getID() +
			"\">" + inst.getName() + "</a>");
		// address
		Hashtable address = (Hashtable) addresses.get(inst.getID());
		if (address != null) {
			String street = (String) address.get(KiezAtlas.PROPERTY_STREET);
			String postcode = (String) address.get(KiezAtlas.PROPERTY_POSTAL_CODE);
			String city = (String) address.get(KiezAtlas.PROPERTY_CITY);
			if (street != null || postcode != null || city != null) {
				out.println("<br><small>" + street + "&nbsp;&nbsp;&nbsp;" + postcode + " " + city + "</small>");
			}
		}
		out.println("</td>");
		out.println("</tr>");
	}
	out.println("</table>");
	
%>
<% end(out); %>
