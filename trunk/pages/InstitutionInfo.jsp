<%@ include file="KiezAtlas.jsp" %>

<% begin(session, out, true); %>
<%
	HTMLGenerator html = (HTMLGenerator) session.getAttribute("html");
	Institution inst = (Institution) session.getAttribute("selectedInst");
	SearchCriteria[] criterias = (SearchCriteria[]) session.getAttribute("criterias");
	String forumActivition = (String) session.getAttribute("forumActivition");
	Integer commentCount = (Integer) session.getAttribute("commentCount");
	//
	out.println(inst.imageURL != null ? "<p><img src=\"" + inst.imageURL + "\"><br>" : "<br><br>");
	out.println("<b>" + inst.name + "</b><br>");
	if (isSet(inst.street)) {
		out.println(mapLink(inst.street, inst.postalCode, inst.city) + "<br>");
	}
	out.println(inst.postalCode + " " + inst.city + "<p>");
	//
	out.println("<span class=\"small\">&Ouml;ffnungszeiten:</span> " + inst.open + "<p>");
	//
	out.println("<span class=\"small\">Tel:</span> " + inst.phone + "<br>");
	out.println("<span class=\"small\">Fax:</span> " + inst.fax + "<br>");
	out.println("<span class=\"small\">Ansprechpartner/in:</span> " + inst.person + "<br>");
	out.println("<span class=\"small\">Email:</span> " +
		"<a href=\"mailto:" + inst.email + "\">" + inst.email + "</a><br>");
	out.println("<span class=\"small\">Website:</span> " +
		"<a href=\"" + inst.webpageURL + "\" target=\"_bank\">" + inst.webpageURL + "</a>");
	//
	for (int i = 0; i < criterias.length; i++) {
		String critName = criterias[i].criteria.getPluralName();
		out.println("<p><div class=\"small\">" + critName + ":</div>");
		topicList(inst.categories[i], KiezAtlas.ACTION_SEARCH_BY_CATEGORY + "&critNr=" + i, html, out);
	}
	//
	out.println("<p><span class=\"small\">Tr&auml;ger:</span> " + inst.traeger +
		(isSet(inst.traegerArt) ? " (" + inst.traegerArt + ")" : ""));
	//
	out.println("<p><div class=\"small\">Weitere Infos:</div>");
	out.println(inst.misc);
	if (isSet(inst.misc) && isSet(inst.adminInfo)) {
		out.println("<p>");
	}
	out.println(inst.adminInfo);
	//
	if (forumActivition.equals(KiezAtlas.SWITCH_ON)) {
		// link to forum page
		out.println("<p>\r<hr>\rDas " + html.link("Forum", KiezAtlas.ACTION_SHOW_INSTITUTION_FORUM) +
			" enth&auml;lt "+ commentCount + " Kommentare</p>");
	}
%>
<% end(out); %>
