<%@ include file="KiezAtlas.jsp" %>

<% begin(session, out); %>
<%!
	static String[] hiddenProps = {
		"Name",
		KiezAtlas.PROPERTY_DESCRIPTION,
		KiezAtlas.PROPERTY_ICON,
		KiezAtlas.PROPERTY_WEB_ALIAS,
		KiezAtlas.PROPERTY_BIRTHDAY,
		KiezAtlas.PROPERTY_YADE_X,
		KiezAtlas.PROPERTY_YADE_Y,
		"City", "Title", "Content",
		"Width", "Height"};
%>
<%
	HTMLGenerator html = (HTMLGenerator) session.getAttribute("html");
	BaseTopic inst = (BaseTopic) session.getAttribute("inst");
	//
	// institution form
	out.println("<H2>" + inst.getName() + "</H2>");
	out.println("<H3>&Auml;nderungsformular</H3>");
	out.println(html.form(inst.getType(), KiezAtlas.ACTION_UPDATE_INSTITUTION,
						  inst.getID(), hiddenProps, true));
%>
<% end(out); %>
