<%@ include file="KiezAtlas.jsp" %>

<html>
<head>
	<title>Kiezatlas</title>
	<link href="../pages/kiezatlas.css" rel="stylesheet" type="text/css">
	<!--[if lt IE 7]>
	<script defer type="text/javascript" src="../pages/pngfix.js"></script>
	<![endif]-->
</head>
<body>
	<%
		String mapImage = (String) session.getAttribute("mapImage");
		Vector hotspots = (Vector) session.getAttribute("hotspots");
		Vector shapeTypes = (Vector) session.getAttribute("shapeTypes");
		Vector shapes = (Vector) session.getAttribute("shapes");
		GeoObject selectedInst = (GeoObject) session.getAttribute("selectedGeo");
	%>
	<img src="<%= mapImage %>" style="position:absolute; top:0px; left:0px;">
	<%
		// --- shapes ---
		Enumeration e = shapes.elements();
		while (e.hasMoreElements()) {
			Shape shape = (Shape) e.nextElement();
			if (isSet(shape.targetWebalias)) {
				out.println("<a href=\"" + request.getContextPath() + "/browse/" + shape.targetWebalias + "\" target=\"_top\">");
			}
			out.println("<img src=\"" + shape.url + "\" class=\"fixpng\" style=\"position:absolute; top:" +
				shape.point.y + "px; left:" + shape.point.x + "px; width:" + shape.size.width + "px; height:" +
				shape.size.height + "px;\">");
			if (isSet(shape.targetWebalias)) {
				out.println("</a>");
			}
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
				if (selectedInst != null && selectedInst.geoID.equals(inst.getID())) {
					out.println("<img src=\"../images/marker.gif\" style=\"position:absolute; top:" +
						(p.y - 20) + "px; left:" + (p.x - 20) + "px;\">");
				}
				// hotspot
				out.println("<a href=\"javascript:top.frames.right.location.href='controller?action=" +
					KiezAtlas.ACTION_SHOW_GEO_INFO + "&id=" + inst.getID() + "'\">" +
					"<img src=\"" + icon + "\" style=\"position:absolute; top:" + (p.y - 10) + "px; left:" +
					(p.x - 10) + "px;\" alt=\"" + inst.getName() + "\" title=\"" + inst.getName() + "\" border=\"0\"></a>");
			}
		}
		// --- shape display switches ---
		if (shapeTypes.size() > 0) {
			out.println("<form style=\"position:fixed; left:0px; bottom:0px; width:300px\">");
			for (int i = 0; i < shapeTypes.size(); i++) {
				ShapeType shapeType = (ShapeType) shapeTypes.elementAt(i);
				int y = KiezAtlas.SHAPE_LEGEND_HEIGHT * (shapeTypes.size() - i - 1);
				out.println("<input type=\"checkbox\"" + (shapeType.isSelected ? " checked" : "") +
					" style=\"position:absolute; left:0px; bottom:" + y + "px;\" onclick=\"location.href='controller?action=" +
					KiezAtlas.ACTION_TOGGLE_SHAPE_DISPLAY + "&typeID=" + shapeType.typeID + "'\">");
				out.println("<div style=\"position:absolute; left:20px; bottom:" + y + "px; color:" + shapeType.color +
					"; background:" + shapeType.color + "; opacity:0.5;\">" + shapeType.name + "</div>");
				out.println("<div style=\"position:absolute; left:20px; bottom:" + y + "px;\">" + shapeType.name + "</div>");
			}
			out.println("</form>");
		}
	%>
</body>
</html>
