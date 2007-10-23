<%@ include file="KiezAtlas.jsp" %>

<% begin(session, out); %>
<%!
	static String[] hiddenProps = {
		KiezAtlas.PROPERTY_ICON,
		KiezAtlas.PROPERTY_YADE_X,
		KiezAtlas.PROPERTY_YADE_Y,
		"Width", "Height"};
%>
<%
	HTMLGenerator html = (HTMLGenerator) session.getAttribute("html");
	BaseTopic geo = (BaseTopic) session.getAttribute("geo");
	String imagefile = (String) session.getAttribute("imagefile");
	//
	// institution info
	out.println("<H2>" + geo.getName() + "</H2>");
	if (imagefile != null) {
		out.println("<img src=\"" + imagefile + "\"><p>");
	}
	//hier die topic bean rinne
	out.println(html.info(geo.getID(), hiddenProps, true));
	//
	// link to form page
	out.println("<p>\r" + html.link("Zum &Auml;nderungsformular", KiezAtlas.ACTION_SHOW_GEO_FORM) + "</p>");
	// link to forum administration page
	out.println("<p>\r<hr>\r" + html.link("Zur Forum Administration", KiezAtlas.ACTION_SHOW_FORUM_ADMINISTRATION) + "</p>");
%>
<% end(out); %>
