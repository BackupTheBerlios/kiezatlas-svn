<%@ page import="de.kiezatlas.deepamehta.KiezAtlas" %>
<%@ page import="de.kiezatlas.deepamehta.GeoObject" %>
<%@ page import="de.kiezatlas.deepamehta.SearchCriteria" %>
<%@ page import="de.kiezatlas.deepamehta.ShapeType" %>
<%@ page import="de.kiezatlas.deepamehta.Shape" %>
<%@ page import="de.kiezatlas.deepamehta.Comment" %>

<%@ page import="de.deepamehta.BaseTopic" %>
<%@ page import="de.deepamehta.BaseAssociation" %>
<%@ page import="de.deepamehta.DeepaMehtaException" %>
<%@ page import="de.deepamehta.PresentableTopic" %>
<%@ page import="de.deepamehta.PropertyDefinition" %>
<%@ page import="de.deepamehta.service.Session" %>
<%@ page import="de.deepamehta.topics.TypeTopic" %>
<%@ page import="de.deepamehta.service.web.HTMLGenerator" %>

<%@ page import="java.io.IOException" %>
<%@ page import="java.util.Vector" %>
<%@ page import="java.util.Hashtable" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="java.awt.Point" %>

<%!
	// edit / list
	void begin(HttpSession session, JspWriter out) throws IOException {
		out.println("<html>\r<head>\r<title>Kiezatlas</title>\r" +
			"<link href=\"../pages/kiezatlas.css\" rel=\"stylesheet\" type=\"text/css\">\r</head>\r" +
			"<body>\r<a href=\"http://www.kiezatlas.de/\" target=\"_blank\"><img src=\"../images/logo.jpg\" border=\"0\"></a>");
	}

	// browse
	void begin(HttpSession session, JspWriter out, boolean refreshMap) throws IOException {
		BaseTopic map = (BaseTopic) session.getAttribute("map");
		SearchCriteria[] criterias = (SearchCriteria[]) session.getAttribute("criterias");
		String searchMode = (String) session.getAttribute("searchMode");
		String searchValue = (String) session.getAttribute("searchValue");
		out.println("<html>\r<head>\r<title>Kiezatlas</title>\r" +
			"<link href=\"../pages/kiezatlas.css\" rel=\"stylesheet\" type=\"text/css\">\r</head>\r" +
			"<body" + (refreshMap ? " onLoad=\"top.frames.left.location.href='controller?action=initFrame&frame=" +
				KiezAtlas.FRAME_LEFT + "'\"" : "") + ">\r");
		//
		out.println("<table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" bgcolor=\"#F0E8E0\"><tr valign=\"top\">");
		out.println("<td rowspan=\"" + (criterias.length + 1) + "\">");
		out.println("<a href=\"http://www.kiezatlas.de/\" target=\"_top\"><img src=\"../images/logo.jpg\" border=\"0\"></a>");
		out.println("<div class=\"big\">" + map.getName() + "</div>");
		out.println("</td>");
		//
		boolean byName = searchMode.equals(KiezAtlas.SEARCHMODE_BY_NAME);
		out.println("<td>" + (byName ? "&rarr;" : "") + "</td><td>" +
			"<form>Suchen<br><input type=\"hidden\" name=\"action\" value=\"" + KiezAtlas.ACTION_SEARCH + "\">" +
			"<input type=\"text\" name=\"search\"" + (byName && searchValue != null ? " value=\"" + searchValue + "\"" : "") +
			" size=\"11\"></form></td></tr>");
		for (int i = 0; i < criterias.length; i++) {
			String critName = criterias[i].criteria.getPluralName();
			out.println("<tr valign=\"top\"><td>" + (searchMode.equals(Integer.toString(i)) ? "&rarr;" : "") + "</td><td>" +
				"<a href=\"controller?action=" + KiezAtlas.ACTION_SHOW_CATEGORIES + "&critNr=" + i + "\">" + critName + "</a></td></tr>");
		}
		out.println("</table>");
	}

	void end(JspWriter out) throws IOException {
		out.println("<br><br>\r<hr>\r<table width=\"100%\" cellpadding=\"4\" bgcolor=\"#F0E8E0\"><tr><td class=\"small\">Powered by<br><a href=\"http://www.deepamehta.de/\" target=\"_blank\"><b>DeepaMehta</b></a></td>\r" +
			"<td class=\"small\" align=\"right\"><a href=\"http://www.kiezatlas.de/impressum.html\" target=\"_top\">Impressum +<br>Haftungshinweise</a></td>" +
			"</tr></table>\r</body>\r</html>");
	}

	// ---

	void topicImages(Vector cats, HTMLGenerator html, JspWriter out) throws IOException {
		for (int i = 0; i < cats.size(); i++) {
			BaseTopic cat = (BaseTopic) cats.elementAt(i);
			out.println(html.imageTag(cat, true));		// withTooltip=true
		}
	}

	void topicList(Vector topics, String action, HTMLGenerator html, JspWriter out) throws IOException {
		out.println("<table>");
		Enumeration e = topics.elements();
		while (e.hasMoreElements()) {
			BaseTopic topic = (BaseTopic) e.nextElement();
			out.println("<tr><td>" + html.imageTag(topic) + "</td><td><a href=\"controller?action=" + action +
				"&id=" + topic.getID() + "\">" + topic.getName() + "</td></tr>");
		}
		out.println("</table>");
	}

	// ---

	void comment(Comment comment, JspWriter out) throws IOException {
		comment(comment, false, out);
	}

	void comment(Comment comment, boolean includeEmailAddress, JspWriter out) throws IOException {
		String email = comment.email;
		out.println("<br>");
		//
		out.println("<span class=\"small\">");
		if (isSet(comment.author)) {
			out.println(comment.author + commentEmail(includeEmailAddress, email) + " schrieb am ");
		} else {
			out.println("Anonymer Kommentar" + commentEmail(includeEmailAddress, email) + " vom ");
		}
		out.println(comment.date + ":</span><br>");
		//
		out.println(comment.text + "<br>");
	}

	// ---

	private String commentEmail(boolean includeEmailAddress, String email) {
		if (includeEmailAddress) {
			return isSet(email) ? " (<a href=\"mailto:" + email + "\">" + email + "</a>)" : " (Emailadresse unbekannt)";
		} else {
			return "";
		}
	}

	// ---

	String mapLink(String street, String postalCode, String city) throws IOException {
		if (city.startsWith("Berlin")) {
			// separate street and house nr.
			int pos = street.lastIndexOf(' ');
			String streetname, hnr;
			if (pos != -1) {
				streetname = street.substring(0, pos);
				hnr = street.substring(pos + 1);
				hnr = hnr.substring(hnr.indexOf('-') + 1);
			} else {
				streetname = street;
				hnr = "";
			}
			//
			String mapURL = "http://www.fahrinfo-berlin.de/gis/index.jsp?adr_zip=" + postalCode + "&adr_street=" + streetname + "&adr_house=" + hnr;
			return street + " <a href=\"" + mapURL + "\" target=\"fahrinfo\"><img src=\"../images/fahrinfo.gif\" border=\"0\" hspace=\"20\"></a>";
		} else {
			return street;
		}
	}

	// ---

	boolean isSet(String str) {
		return str.length() > 0;
	}
%>
