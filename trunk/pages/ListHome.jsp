<%@ include file="KiezAtlas.jsp" %>

<% begin(KiezAtlas.SERVLET_LIST, session, out); %>
<%
	Vector workspaces = (Vector) session.getAttribute("workspaces");
	Hashtable cityMaps = (Hashtable) session.getAttribute("cityMaps");
	//
	out.println("<dl>");
	//
	Enumeration e = workspaces.elements();
	while (e.hasMoreElements()) {
		BaseTopic workspace = (BaseTopic) e.nextElement();
		out.println("<dt>" + workspace.getName() + "</dt>");
		Enumeration e2 = ((Vector) cityMaps.get(workspace.getID())).elements();
		while (e2.hasMoreElements()) {
			BaseTopic cityMap = (BaseTopic) e2.nextElement();
			out.println("<dd><a href=\"?action=" + KiezAtlas.ACTION_SHOW_INSTITUTIONS +
				"&cityMapID=" + cityMap.getID() + "\">" + cityMap.getName() + "</a></dd>");
		}
	}
	out.println("</dl>");
%>
<% end(out); %>
