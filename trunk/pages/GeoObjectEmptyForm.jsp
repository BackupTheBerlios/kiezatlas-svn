<%@ include file="KiezAtlas.jsp" %>

<% begin(session, out); %>
<%!
	static String[] hiddenProps = {
		KiezAtlas.PROPERTY_DESCRIPTION,
		KiezAtlas.PROPERTY_ICON,
		"Title", "Content",
		"Width", "Height"};
%>
<%
	HTMLGenerator html = (HTMLGenerator) session.getAttribute("html");
	String instTypeID = (String) session.getAttribute("instTypeID");
	//
	// institution form
	out.println("<H2>Neues Objekt eingeben</H2>");
	out.println(html.form(instTypeID, KiezAtlas.ACTION_CREATE_GEO, hiddenProps, true));
%>
<% end(out); %>
