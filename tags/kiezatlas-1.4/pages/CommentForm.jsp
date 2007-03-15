<%@ include file="KiezAtlas.jsp" %>

<% begin(session, out, false); %>
<%
	HTMLGenerator html = (HTMLGenerator) session.getAttribute("html");
	Institution inst = (Institution) session.getAttribute("selectedInst");
	Vector comments = (Vector) session.getAttribute("instComments");
%>
	<br><br>
	<p><b><%= inst.name %> -- Forum</b></p>
	<p>Kommentar schreiben</p>
	<form>
		<table>
		<tr>
			<td colspan="2"><textarea name="<%=KiezAtlas.PROPERTY_TEXT%>" rows="8" cols="36"></textarea></td>
		</tr>
		<tr>
			<td class="small">Autor</td>
			<td><input type="Text" name="<%=KiezAtlas.PROPERTY_COMMENT_AUTHOR%>"></td>
		</tr>
		<tr>
			<td class="small">Emailadresse<br>(wird nicht ver&ouml;ffentlicht)</td>
			<td><input type="Text" name="<%=KiezAtlas.PROPERTY_EMAIL_ADDRESS%>"></td>
		</tr>
		<tr>
			<td></td>
			<td><input type="Hidden" name="action" value="<%=KiezAtlas.ACTION_CREATE_COMMENT%>">
				<input type="Submit" value="OK"></td>
		</tr>
		</table>
	</form>
	<hr>
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
