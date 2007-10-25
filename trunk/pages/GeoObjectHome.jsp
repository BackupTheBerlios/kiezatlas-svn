<%@ include file="KiezAtlas.jsp" %>

<% begin(session, out); %>
<%
	HTMLGenerator html = (HTMLGenerator) session.getAttribute("html");
	TopicBean topicBean = (TopicBean) session.getAttribute("topicBean");
	//render image if available from the virtual tomcat directory 8080/dm-images
	String imageFile = topicBean.getValue("Image / File");
	if (imageFile != null) {
		String relativePath = "../../dm-images/images/";
		String imageHtmlString = "<img src=" + relativePath + imageFile + ">";
		out.println(imageHtmlString);
	}
	topicBean.removeFieldsContaining("Image");
	topicBean.removeField("Web Alias");
	topicBean.removeField("Password");	
	topicBean.removeField("Owner ID");
	topicBean.removeField("Description");
	topicBean.removeField("Locked Geometry");
	topicBean.removeField("Icon");
	topicBean.removeFieldsContaining("YADE");
	// Name as Headline
	out.println("<H2>" + topicBean.getValue("Name") + "</H2>");
	//hier die topic bean rinne
	out.println(html.info(topicBean));
	// link to form page
	out.println("<p>\r" + html.link("Zum &Auml;nderungsformular", KiezAtlas.ACTION_SHOW_GEO_FORM) + "</p>");
	// link to forum administration page
	out.println("<p>\r<hr>\r" + html.link("Zur Forum Administration", KiezAtlas.ACTION_SHOW_FORUM_ADMINISTRATION) + "</p>");
%>
<% end(out); %>
