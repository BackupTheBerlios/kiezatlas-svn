<%@ include file="KiezAtlas.jsp" %>

<html>
<head>
	<title>Kiezatlas</title>
	<link href="../pages/kiezatlas.css" rel="stylesheet" type="text/css">
</head>
<body>
	<%
		String mapImage = (String) session.getAttribute("mapImage");
		Vector hotspots = (Vector) session.getAttribute("hotspots");
		Vector shapeTypes = (Vector) session.getAttribute("shapeTypes");
		Vector shapes = (Vector) session.getAttribute("shapes");
		Institution selectedInst = (Institution) session.getAttribute("selectedInst");
	%>
	<img src="<%= mapImage %>" style="position:absolute; top:0px; left:0px;">
	<%
		// --- shapes ---
		Enumeration e = shapes.elements();
		while (e.hasMoreElements()) {
			Shape shape = (Shape) e.nextElement();
			out.println("<img src=\"" + shape.url + "\" style=\"position:absolute; top:" + shape.point.y +
				"px; left:" + shape.point.x + "px;\">");
		}
		// --- institutions ---
		e = hotspots.elements();
		while (e.hasMoreElements()) {
			Vector presentables = (Vector) e.nextElement();
			Enumeration e2 = presentables.elements();
			String icon = (String) e2.nextElement();
			while (e2.hasMoreElements()) {
				PresentableTopic inst = (PresentableTopic) e2.nextElement();
				Point p = inst.getGeometry();
				// marker
				if (selectedInst != null && selectedInst.id.equals(inst.getID())) {
					out.println("<img src=\"../images/marker.gif\" style=\"position:absolute; top:" +
						(p.y - 20) + "px; left:" + (p.x - 20) + "px;\">");
				}
				// hotspot
				out.println("<a href=\"javascript:top.frames.right.location.href='controller?action=" +
					KiezAtlas.ACTION_SHOW_INSTITUTION_INFO + "&id=" + inst.getID() + "'\">" +
					"<img src=\"" + icon + "\" style=\"position:absolute; top:" + (p.y - 10) + "px; left:" +
					(p.x - 10) + "px;\" alt=\"" + inst.getName() + "\" border=\"0\"></a>");
			}
		}
		// --- shape display switches ---
		out.println("<form style=\"position:fixed; left:0px; bottom:0px;\">");
		e = shapeTypes.elements();
		while (e.hasMoreElements()) {
			ShapeType shapeType = (ShapeType) e.nextElement();
			out.println("<input type=\"checkbox\"" + (shapeType.isSelected ? " checked" : "") +
				" onclick=\"location.href='controller?action=" + KiezAtlas.ACTION_TOGGLE_SHAPE_DISPLAY +
				"&typeID=" + shapeType.typeID + "'\"> " + shapeType.name + "<br>");
		}
		out.println("</form>");
	%>
</body>
</html>
