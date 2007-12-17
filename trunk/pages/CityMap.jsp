<%@ include file="KiezAtlas.jsp" %>

<html>
<head>
	<title>Kiezatlas</title>
	<link href="../pages/kiezatlas.css" rel="stylesheet" type="text/css">
	<!--[if lt IE 7]>
	<script defer type="text/javascript" src="../pages/pngfix.js"></script>
	<![endif]-->
	<script type="text/javascript">
		var hidden = true;
		function showMenu(id) {
			var currentMenu = "clusterMenu"+id;
			if (hidden) {
				if (document.getElementById) {
					document.getElementById(currentMenu).style.visibility = 'visible';
					hidden=false;
				}
			} else {
				if (document.getElementById) {
					document.getElementById(currentMenu).style.visibility = 'hidden';
					hidden=true;
				}
			}
			
		}
	</script>
</head>
<body>
	<%
		String mapImage = (String) session.getAttribute("mapImage");
		Vector hotspots = (Vector) session.getAttribute("hotspots");
		Vector shapeTypes = (Vector) session.getAttribute("shapeTypes");
		Vector shapes = (Vector) session.getAttribute("shapes");
		Vector cluster = (Vector) session.getAttribute("cluster");
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
		// --- geoObjects ---
		e = hotspots.elements();
		while (e.hasMoreElements()) {
			Vector presentables = (Vector) e.nextElement();
			Enumeration e2 = presentables.elements();
			String icon = (String) e2.nextElement();
			while (e2.hasMoreElements()) {
				PresentableTopic inst = (PresentableTopic) e2.nextElement();
				Point p = inst.getGeometry();
				// marker
				// marker and hotspot can't overlap exactly, caus of the new icons don't fit in the old convention of 20x20px
				if (selectedInst != null && selectedInst.geoID.equals(inst.getID())) {
					out.println("<img src=\"../images/marker.gif\" style=\"position:absolute; top:" +
						(p.y - 18) + "px; left:" + (p.x - 18) + "px;\">");
				}
				// hotspot
				out.println("<a href=\"javascript:top.frames.right.location.href='controller?action=" +
					KiezAtlas.ACTION_SHOW_GEO_INFO + "&id=" + inst.getID() + "'\">" +
					"<img src=\"" + icon + "\" style=\"position:absolute; top:" + (p.y - 7) + "px; left:" +
					(p.x - 7) + "px;\" alt=\"" + inst.getName() + "\" title=\"" + inst.getName() + "\" border=\"0\"></a>");
			}
		}
		//cluster
		e = cluster.elements();
		while (e.hasMoreElements()) {
			//entered vector of cluster
			Cluster myCluster = (Cluster) e.nextElement();
			Point p = myCluster.getPoint();
			Vector presentables = myCluster.getPresentables();
			Enumeration e2 = presentables.elements();
			String iconPath = myCluster.getIcon();
			out.println("<span onMouseOver=\"showMenu('"+p.x+"."+p.y+"')\"><img src=\""+iconPath+"\" style=\"position:absolute; top:" + (p.y - 10) + "px; left:" + (p.x - 10) + "px;\" alt=\"Cluster\" border=\"0\"></span>");
			out.println("<div id=\"clusterMenu"+p.x+"."+p.y+"\" onMouseOut=\"showMenu('"+p.x+"."+p.y+"')\" style=\"position:fixed; visibility:hidden; border:1px; dashed black; padding:5px; top:"+ (p.y -10) + "px; left:" + (p.x + 10)+ "px; overflow:visible; background-color:#b0b0ea;\">");
			while(e2.hasMoreElements()) {
				PresentableTopic currentTopic = (PresentableTopic) e2.nextElement();
				out.println("<a href=\"javascript:top.frames.right.location.href='controller?action=" +
						KiezAtlas.ACTION_SHOW_GEO_INFO + "&id=" + currentTopic.getID() + "'\" font-color=\"black\" font-style=\"none\" font-size=\"10px\">"+currentTopic.getName()+"</a></br>");
			}
			out.println("</div>");
				
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
