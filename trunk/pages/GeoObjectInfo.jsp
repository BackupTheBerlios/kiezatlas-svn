<%@ include file="KiezAtlas.jsp" %>

<% begin(session, out, true); %>
<%!
	// don´t know what to do with these lines, dropped unknown lines :)
	static String[] hiddenProps = {
		"Name",
		KiezAtlas.PROPERTY_PASSWORD,
		KiezAtlas.PROPERTY_YADE_X,
		KiezAtlas.PROPERTY_YADE_Y,
		"Width", "Height"};
%>
<%
	HTMLGenerator html = (HTMLGenerator) session.getAttribute("html");
	GeoObject geo = (GeoObject) session.getAttribute("selectedGeo");
	SearchCriteria[] criterias = (SearchCriteria[]) session.getAttribute("criterias");
	String forumActivition = (String) session.getAttribute("forumActivition");
	Integer commentCount = (Integer) session.getAttribute("commentCount");

	out.println(html.info(geo.geoID, hiddenProps, true));
	
	//out.println(geo.adminInfo);
	//
	if (forumActivition.equals(KiezAtlas.SWITCH_ON)) {
		// link to forum page
		out.println("<p>\r<hr>\rDas " + html.link("Forum", KiezAtlas.ACTION_SHOW_GEO_FORUM) +
			" enth&auml;lt "+ commentCount + " Kommentare</p>");
	}
	//
	// link to form page
	// out.println("<p>\r" + html.link("Zum &Auml;nderungsformular", KiezAtlas.ACTION_SHOW_GEO_FORM) + "</p>");
    // link to forum administration page
	 out.println("<p>\r<hr>\r" + html.link("Zum Forum ", KiezAtlas.ACTION_SHOW_GEO_FORUM) + "</p>");
%>
<% end(out); %>
