<%@ include file="KiezAtlas.jsp" %>

<% begin(session, out, false); %>
<%
	HTMLGenerator html = (HTMLGenerator) session.getAttribute("html");
	Institution inst = (Institution) session.getAttribute("selectedInst");
	Vector comments = (Vector) session.getAttribute("instComments");
%>
	<br><br>
	<p><b><%= inst.name %> -- Forum</b></p>
	<p class="small">Das Forum enth&auml;lt <%= comments.size() %> Kommentare</p>
	<p><%= html.link("Kommentar schreiben", KiezAtlas.ACTION_SHOW_COMMENT_FORM) %></p>
	<%
		Enumeration e = comments.elements();
		while (e.hasMoreElements()) {
			Comment comment = (Comment) e.nextElement();
			comment(comment, out);
		}
	%>
	<br>
	<p><%= html.link("Zur&uuml;ck zu " + inst.name, KiezAtlas.ACTION_SHOW_INSTITUTION_INFO, "id=" + inst.id) %></p>
<% end(out); %>
