<%@ include file="KiezAtlas.jsp" %>
<html>
<head>
	<title>Kiezatlas</title>
</head>
<frameset cols="*,320">
	<frame name ="left" src="../pages/blank.html"/>
	<frame name ="right" src="controller?action=initFrame&frame=<%= KiezAtlas.FRAME_RIGHT %>"/>
</frameset>
</html>
