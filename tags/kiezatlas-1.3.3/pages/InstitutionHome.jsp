<%@ include file="KiezAtlas.jsp" %>

<% begin(session, out); %>
<%!
	static String[] hiddenProps = {
		"Name",
		KiezAtlas.PROPERTY_DESCRIPTION,
		KiezAtlas.PROPERTY_ICON,
		KiezAtlas.PROPERTY_WEB_ALIAS,
		KiezAtlas.PROPERTY_YADE_X,
		KiezAtlas.PROPERTY_YADE_Y,
		"City", "Title", "Content",
		"Width", "Height"};
%>
<%
	HTMLGenerator html = (HTMLGenerator) session.getAttribute("html");
	BaseTopic inst = (BaseTopic) session.getAttribute("inst");
	String imagefile = (String) session.getAttribute("imagefile");
	//
	// institution info
	out.println("<H2>" + inst.getName() + "</H2>");
	if (imagefile != null) {
		out.println("<img src=\"" + imagefile + "\"><p>");
	}
	out.println(html.info(inst.getID(), hiddenProps, true));
	//
	// link to form page
	out.println("<p>\r" + html.link("Zum &Auml;nderungsformular", KiezAtlas.ACTION_SHOW_INSTITUTION_FORM) + "</p>");
	// link to forum administration page
	out.println("<p>\r<hr>\r" + html.link("Zur Forum Administration", KiezAtlas.ACTION_SHOW_FORUM_ADMINISTRATION) + "</p>");
%>
<% end(out); %>
