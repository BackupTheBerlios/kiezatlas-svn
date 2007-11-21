<%@ include file="KiezAtlas.jsp" %>
<html>
<head>
	<title>Kiezatlas</title>
</head>

	<%
			out.println("<frameset cols=\"*,320\">");
			out.println("<frame name =\"left\" src=\"../pages/blank.html\"/>");
			out.println("<frame name =\"right\" src=\"controller?action=initFrame&frame=" +KiezAtlas.FRAME_RIGHT+ "\"/>");
			out.println("</frameset>");		
			
	%>
	
</html>
