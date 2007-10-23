<%@ include file="KiezAtlas.jsp" %>

<% begin(session, out, true); %>

<%
	//html render engine
	HTMLGenerator html = (HTMLGenerator) session.getAttribute("html");
	
	//ForumSpecific
	String forumActivition = (String) session.getAttribute("forumActivition");
	Integer commentCount = (Integer) session.getAttribute("commentCount");
	
	//hide properties by removing
	TopicBean topicBean = (TopicBean) session.getAttribute("topicBean");
	topicBean.removeFieldsContaining("Password");
	topicBean.removeFieldsContaining("YADE");
	topicBean.removeFieldsContaining("Owner");
	topicBean.removeFieldsContaining("Locked Geometry");
	
	//render image if available from the virtual tomcat directory 8080/dm-images
	String imageFile = topicBean.getValue("Image / File");
	if (imageFile != null) {
		String relativePath = "../../dm-images/images/";
		String imageHtmlString = "<img src=" + relativePath + imageFile + ">";
		out.println(imageHtmlString);
		String imageDescr = topicBean.getValue("Image / Description");
		if (imageDescr != null) {
			out.println("<br><i><font-size=-2>" + imageDescr + "</font></i>");	
		}
		
		
	}
	
	//Print Name
	out.println("<h3>" + topicBean.getValue("Name") + "</br></h3>");
	
	//Print Adress if available on this Topic includind directlink to bvg
	try{ 
		if (topicBean.getValue("Address / Street") != null) {
			String city = topicBean.getValues("Address / City").elementAt(0).toString();
			System.out.println("Stadt: " + city);
			//Falls noch keine city zugewiesen wurde
			if (city == null) {
				city = "Stadt ist noch nicht zugewiesen."; 
			} else {
				int beginQuoteMarks = city.indexOf("\"")+1;
				int endQuoteMarks  = city.lastIndexOf("\"");
				city = city.substring(beginQuoteMarks, endQuoteMarks);
			}
			String street = topicBean.getValue("Address / Street");
			String postalCode = topicBean.getValue("Address / Postal Code");
			//fetch maplink block
			out.println(mapLink(street, postalCode, city));
		}
	}catch (DeepaMehtaException ex){
		out.println("Bitte Entschuldigung Sie, es ist ein Fehler aufgetreten: " + ex);	
	}
	
	topicBean.removeFieldsContaining("Image");
	topicBean.removeFieldsContaining("Address");
	topicBean.removeField("Name");
	//print properties which were not removed, starting generic content rendering 
	out.println(html.info(topicBean));
	//If forum is activated by the owner, it will be shown a link here
	if (forumActivition.equals(KiezAtlas.SWITCH_ON)) {
		// link to forum page
		out.println("<p>\r<hr>\rDas " + html.link("Forum", KiezAtlas.ACTION_SHOW_GEO_FORUM) +
			" enth&auml;lt "+ commentCount + " Kommentare</p>");
	}
%>
<% end(out); %>
