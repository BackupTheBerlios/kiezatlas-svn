<%@ include file="KiezAtlas.jsp" %>

<% begin(session, out, true); %>

<%
	HTMLGenerator html = (HTMLGenerator) session.getAttribute("html");
	String imagePath = (String) session.getAttribute("imagePath");	
	String forumActivition = (String) session.getAttribute("forumActivition");
	Integer commentCount = (Integer) session.getAttribute("commentCount");
	// hide properties by removing
	TopicBean topicBean = (TopicBean) session.getAttribute("topicBean");
	topicBean.removeFieldsContaining("Password");
	topicBean.removeFieldsContaining("YADE");
	topicBean.removeFieldsContaining("Owner");
	topicBean.removeFieldsContaining("Web Alias");
	topicBean.removeFieldsContaining("Locked Geometry");
	topicBean.removeFieldsContaining("Description");
	out.println("<br>");
	// image
	String imageFile = topicBean.getValue("Image / File");
	if (!imageFile.equals("")) {
		out.println("<img src=" + imagePath + imageFile + "><br>");
	}	
	// name
	out.println("<b>" + topicBean.getValue("Name") + "</b><br>");
	// print address if available on this topic including fahrinfo-link
	try { 
		// first comes the property and as second the possible related address topic 
		String city = topicBean.getValue("Stadt");
		if (city == null) {
			Vector tmp = topicBean.getValues("Address / City");
			if (tmp != null) {
				if (tmp.size() > 0){
					city = ((BaseTopic) tmp.elementAt(0)).getName();
				} else {
					city = "";
				}						
			} else {
				city = "";
			}
		}			
		String street = topicBean.getValue("Address / Street");
		String postalCode = topicBean.getValue("Address / Postal Code");
		// fetch maplink block
		out.println(mapLink(street, postalCode, city));
	} catch (DeepaMehtaException ex) {
		out.println("Es ist ein Fehler aufgetreten: " + ex);	
	}
	// remove fields which are rendered manually
	topicBean.removeFieldsContaining("Image");
	topicBean.removeFieldsContaining("Address");
	topicBean.removeFieldsContaining("Icon");
	topicBean.removeField("Name");
	topicBean.removeField("Stadt");
	// generic topic info rendering 
	out.println("<br><br>");
	out.println(html.info(topicBean, DeepaMehtaConstants.LAYOUT_STYLE_FLOW));
	// forum
	if (forumActivition.equals(KiezAtlas.SWITCH_ON)) {
		// link to forum page
		out.println("<p>\r<hr>\rDas " + html.link("Forum", KiezAtlas.ACTION_SHOW_GEO_FORUM) +
			" enth&auml;lt "+ commentCount + " Kommentare</p>");
	}
%>
<% end(out); %>
