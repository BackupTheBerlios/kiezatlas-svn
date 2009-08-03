<%@ include file="KiezAtlas.jsp" %>

<% begin(KiezAtlas.SERVLET_LIST, session, out); %>
<%
	Vector workspaces = (Vector) session.getAttribute("workspaces");
	Hashtable cityMaps = (Hashtable) session.getAttribute("cityMaps");
	Hashtable mapCounts = (Hashtable) session.getAttribute("mapCounts");
	String membership = (String) session.getAttribute("membership");
	//
	out.println("<dl>");
	//
	Enumeration e = workspaces.elements();
	while (e.hasMoreElements()) {
		BaseTopic workspace = (BaseTopic) e.nextElement();
		out.println("<dt><b>" + workspace.getName() + "</b></dt>");
		Enumeration e2 = ((Vector) cityMaps.get(workspace.getID())).elements();
		while (e2.hasMoreElements()) {
			BaseTopic cityMap = (BaseTopic) e2.nextElement();
					out.println("<dd><a href=\"?action=" + KiezAtlas.ACTION_SHOW_INSTITUTIONS + "&cityMapID=" + cityMap.getID() + "\">" +
                            ""+cityMap.getName()+"</a>&nbsp;<span class=\"small\">("+ mapCounts.get(cityMap.getID()) +")</span>&nbsp;&nbsp");
                if (!membership.equals("Affiliated")) {
                    out.println("<a href=\"?action=" + KiezAtlas.ACTION_SHOW_INSTITUTIONS_SLIM +
                    "&cityMapID=" + cityMap.getID() + "\" class=\"small\"> zur schlanken Liste </a></dd>");
                }
		}
	}
	out.println("</dl>");
%>
<% end(out); %>
