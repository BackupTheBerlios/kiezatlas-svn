<%@ include file="KiezAtlas.jsp" %>

<% begin(KiezAtlas.SERVLET_IMPORT, session, out); %>
<%
	String membership = (String) session.getAttribute("membership");
	String timingInterval = (String) session.getAttribute("timingInterval");
	String workerThreadState = (String) session.getAttribute("workerThread");
	String workerThreadTime = (String) session.getAttribute("workerThreadTime");
    BaseTopic user = (BaseTopic) session.getAttribute("user");
    BaseTopic workspace = (BaseTopic) session.getAttribute("workspaces");
    Hashtable criterias = (Hashtable) session.getAttribute("criterias");
    Vector geoObjects = (Vector) session.getAttribute("geoObjects");
    //
	out.println("<dl style=\"width: 700px;\">");
	//
    //Enumeration e = workspaces.elements();
    //while (e.hasMoreElements()) {
		// BaseTopic workspace = (BaseTopic) e.nextElement();
		out.println("<dt><b>" + workspace.getName() + "</b> beinhaltet "+geoObjects.size() +" GeoObjekte</dt>");
		Enumeration e2 = criterias.keys();
		while (e2.hasMoreElements()) {
			Object key = e2.nextElement();
            Object critInNumbers = criterias.get(key);
            out.println("<dd>");
                out.println(key.toString() + " (" +critInNumbers.toString()+ ")");
            out.println("</dd>");
		}
        out.println("&nbsp; <a href=\"?action="+KiezAtlas.ACTION_RESET_CRITERIAS+"&workspaceId="+workspace.getID()+"\"> resetCategories</a><br/>");
        out.println("<a href=\"?action="+KiezAtlas.ACTION_DO_IMPORT+"&workspaceId="+workspace.getID()+"\">> kickoffImport</a>");
        if (workerThreadState.equals("NEW")) {
            out.println("<p><i>");
            out.println("The Import Worker for this workspace was kicked off at " + workerThreadTime + " and is currently working / sleeping. ");
            out.println("<br/> Import Intervall is set to " +timingInterval+ " min.</i><p/>");
        } else {
            out.println("<p><i>");
            out.println("The Import Worker for this Workspace was NOT kicked off yet.");
            out.println("</i><p/>");
        }
	//}
    out.println("</dl>");
%>
<% end(out); %>
