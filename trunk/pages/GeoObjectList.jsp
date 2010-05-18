<%@ include file="KiezAtlas.jsp" %>

<% begin(session, out, true); %>
<%
	HTMLGenerator html = (HTMLGenerator) session.getAttribute("html");
	Vector insts = (Vector) session.getAttribute("institutions");
	Hashtable cats = (Hashtable) session.getAttribute("categories");
	Hashtable addresses = (Hashtable) session.getAttribute("addresses");
	String searchMode = (String) session.getAttribute("searchMode");
	String searchValue = (String) session.getAttribute("searchValue");
	String selectedCriteria = (String) session.getAttribute("defaultCriteria");
	// --- heading ---
	if (searchMode.equals(KiezAtlas.SEARCHMODE_BY_NAME)) {
		out.println("Suchergebnis ");
	}
        // --- ### align new back button at the right side
	out.println("<b>" + searchValue + "</b>&nbsp;&nbsp;&nbsp;<a class=\"small\" href==\"controller?action=" + KiezAtlas.ACTION_SHOW_CATEGORIES + "&critNr=" +selectedCriteria+ ">zur&uuml;ck</a>");
        out.println("<div class=\"small\">" + insts.size() + " Objekte ");
	out.println("</div>");
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
		out.println("<a href=\"controller?action=" + KiezAtlas.ACTION_SHOW_GEO_INFO + "&id=" + inst.getID() +
			"\">" + inst.getName() + "</a>");
		// address
		Hashtable address = (Hashtable) addresses.get(inst.getID());
		
		String street = (String) address.get(KiezAtlas.PROPERTY_STREET);
	    String postcode = (String) address.get(KiezAtlas.PROPERTY_POSTAL_CODE);
		String city = (String) address.get(KiezAtlas.PROPERTY_CITY);
		out.println("<br><small>" + (street != null ? street + "&nbsp;&nbsp;&nbsp;" : "") + (postcode != null ? postcode + " " : "") + city + "</small>");
		out.println("</td>");
		out.println("</tr>");
	}
	out.println("</table>");
	
%>
<% end(session, out); %>
