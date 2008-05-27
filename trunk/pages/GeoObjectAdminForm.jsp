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
	BaseTopic geo = (BaseTopic) session.getAttribute("geo");
	//
	// institution form
	out.println("<H2>" + geo.getName() + "</H2>");
	out.println("<H3>Administrator &Auml;nderungsformular</H3>");
	out.println(html.form(geo.getType(), KiezAtlas.ACTION_UPDATE_GEO,
						  geo.getID(), hiddenProps, true));
%>
<% end(out); %>
