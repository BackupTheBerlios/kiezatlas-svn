<%@ include file="KiezAtlas.jsp" %>
<html>
<head>
	<title>Kiezatlas</title>
</head>

	<%
		TopicBean topicBean = (TopicBean) session.getAttribute("topicBean");
			
		if (topicBean != null) {
			System.out.println("> set rightFrame to  " + topicBean.id);
			out.println("<frameset cols=\"*,320\">");
				out.println("<frame name =\"left\" src=\"../pages/blank.html\"/>");
				out.println("<frame name =\"right\" src=\"controller?action=initFrame&frame=" +KiezAtlas.FRAME_RIGHT+ "&id=" +topicBean.id+ "\"/>");
			out.println("</frameset>");
		} else {
			System.out.println("> set rightFrame to criterias or list all institutions");
			out.println("<frameset cols=\"*,320\">");
				out.println("<frame name =\"left\" src=\"../pages/blank.html\"/>");
				out.println("<frame name =\"right\" src=\"controller?action=initFrame&frame=" +KiezAtlas.FRAME_RIGHT+ "\"/>");
			out.println("</frameset>");		
		}
	%>
	
</html>
