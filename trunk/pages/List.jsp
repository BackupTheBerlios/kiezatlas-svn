<%@ include file="KiezAtlas.jsp" %>

<% begin(session, out); %>
<%
	HTMLGenerator html = (HTMLGenerator) session.getAttribute("html");
	String mapName = (String) session.getAttribute("mapName");
	String critName = (String) session.getAttribute("critName");
	Vector topics = (Vector) session.getAttribute("topics");
	// --- list of institutions ---
	out.println("<h2>" + mapName + "</h2>");
	out.println(topics.size() + " Objekte<br><br><br>");
	out.println(html.list(topics));
	
%>
<% end(out); %>
