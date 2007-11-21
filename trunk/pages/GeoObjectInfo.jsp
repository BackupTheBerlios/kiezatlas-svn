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
	topicBean.removeFieldsContaining("Web Alias");
	topicBean.removeFieldsContaining("Locked Geometry");
	topicBean.removeFieldsContaining("Description");
	out.println("<br />");
	//render image if available from the virtual tomcat directory 8080/dm-images
	String imageFile = topicBean.getValue("Image / File");
	out.println("");
	if (imageFile != null) {
		String relativePath = "../../dm-images/images/";
		String imageHtmlString = "<img src=" + relativePath + imageFile + "><br />";
		out.println(imageHtmlString);
	}
	
	//Print Name
	out.println("<b>" + topicBean.getValue("Name") + "</b><br />");
	
	//Print Adress if available on this Topic includind directlink to bvg
	try { 
		if (topicBean.getValue("Address / Street") != null) {
			// first comes the property and as second the possible related address topic 
			String city = topicBean.getValue("Stadt");
			if (city == null) {
				Vector tmp = topicBean.getValues("Address / City");
				if (tmp.size() > 0) {
					city = ((BaseTopic) tmp.elementAt(0)).getName();
				} else {
					city = "";
				}
			}
			
			String street = topicBean.getValue("Address / Street");
			String postalCode = topicBean.getValue("Address / Postal Code");
			// fetch maplink block
			out.println(mapLink(street, postalCode, city));
		} else {
			// Trotzdem die Stadt rausschreiben wenn keine adresse da ist..
			String city = topicBean.getValue("Stadt");
			Vector tmp = topicBean.getValues("Address / City");
			if (city == null && tmp.size() > 0) {
				city = ((BaseTopic) tmp.elementAt(0)).getName();
			}
			out.println(city);
		}
	} catch (DeepaMehtaException ex){
		out.println("Bitte Entschuldigung Sie, es ist ein Fehler aufgetreten: " + ex);	
	}
	
	topicBean.removeFieldsContaining("Image");
	topicBean.removeFieldsContaining("Address");
	topicBean.removeField("Name");
	topicBean.removeFieldsContaining("Icon");
	topicBean.removeField("Stadt");
	//print properties which were not removed, starting generic content rendering 
	out.println("<br/><br/>");
	out.println(html.info(topicBean, 2));
	//If forum is activated by the owner, it will be shown a link here
	if (forumActivition.equals(KiezAtlas.SWITCH_ON)) {
		// link to forum page
		out.println("<p>\r<hr>\rDas " + html.link("Forum", KiezAtlas.ACTION_SHOW_GEO_FORUM) +
			" enth&auml;lt "+ commentCount + " Kommentare</p>");
	}
	// cleanup session for switching maps within one session
%>
<% end(out); %>
