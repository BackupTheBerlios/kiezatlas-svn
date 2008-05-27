<%@ include file="KiezAtlas.jsp" %>

<% begin(session, out); %>
<%!
	static String[] hiddenProps = {
		KiezAtlas.PROPERTY_DESCRIPTION,
		KiezAtlas.PROPERTY_ICON,
		"Width", "Height"};
%>
<%
	HTMLGenerator html = (HTMLGenerator) session.getAttribute("html");
	BaseTopic cityMap = (BaseTopic) session.getAttribute("cityMap");
	Vector topics = (Vector) session.getAttribute("topics");
	// --- list of institutions ---
	out.println("<h2>" + cityMap.getName() + "</h2>");
	out.println(topics.size() + " Objekte<br><br><br>");
	out.println(html.list(topics, hiddenProps, true, KiezAtlas.ACTION_SHOW_GEO_FORM));	// hideSel=true
%>
<% end(out); %>
