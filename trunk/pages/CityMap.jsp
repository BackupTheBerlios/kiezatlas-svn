<%@ include file="KiezAtlas.jsp" %>

<html>
<head>
	<title>Kiezatlas</title>
</head>
<body>
	<%
		String mapImage = (String) session.getAttribute("mapImage");
		Vector hotspots = (Vector) session.getAttribute("hotspots");
		Institution selectedInst = (Institution) session.getAttribute("selectedInst");
	%>
	<img src="<%= mapImage %>">
	<%
		Enumeration e = hotspots.elements();
		while (e.hasMoreElements()) {
			Vector presentables = (Vector) e.nextElement();
			Enumeration e2 = presentables.elements();
			String icon = (String) e2.nextElement();
			while (e2.hasMoreElements()) {
				PresentableTopic inst = (PresentableTopic) e2.nextElement();
				Point p = inst.getGeometry();
				// marker
				if (selectedInst != null && selectedInst.id.equals(inst.getID())) {
					out.println("<img src=\"../images/marker.gif\" style=\"position:absolute; top:" + (p.y - 10) + "px; left:" + (p.x - 10) + "px;\">");
				}
				// hotspot
				out.println("<a href=\"javascript:top.frames.right.location.href='controller?action=" +
					KiezAtlas.ACTION_SHOW_INSTITUTION_INFO + "&id=" + inst.getID() + "'\">" +
					"<img src=\"" + icon + "\" style=\"position:absolute; top:" + p.y + "px; left:" + p.x + "px;\" alt=\"" + inst.getName() + "\" border=\"0\"></a>");
			}
		}
	%>
</body>
</html>
