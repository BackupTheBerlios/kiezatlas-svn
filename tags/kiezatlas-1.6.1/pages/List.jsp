<%@ include file="KiezAtlas.jsp" %>

<% begin(KiezAtlas.SERVLET_LIST, session, out); %>
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
	BaseTopic geo = (BaseTopic) session.getAttribute("geo");
	Vector notifications = (Vector) session.getAttribute("notifications");
	//
	out.println("<p><span class=\"heading\">" + cityMap.getName() + "</span>" +
		"&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;" +
		html.link("Alle Stadtpl&auml;ne anzeigen", KiezAtlas.ACTION_GO_HOME) + "</p>");
	out.println("<p>" + topics.size() + " Objekte&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;" +
		html.link("Neues Objekt eingeben", KiezAtlas.ACTION_SHOW_EMPTY_GEO_FORM) + "<br><br><br></p>");	
	out.println(html.notification(notifications) + (notifications.size() > 0 ? "<br><br>" : ""));
	// --- list of institutions ---
	String selectedID = geo != null ? geo.getID() : null;
	out.println(html.list(topics, selectedID, hiddenProps, true, KiezAtlas.ACTION_SHOW_GEO_FORM, true));
														// hideSel=true, doZebraStriping=true
%>
<% end(out); %>
