<%@ include file="KiezAtlas.jsp" %>

<% begin(KiezAtlas.SERVLET_IMPORT, session, out); %>
<%
	String membership = (String) session.getAttribute("membership");
	String timingInterval = (String) session.getAttribute("timingInterval");
	String workerThreadState = (String) session.getAttribute("workerThread");
	String workerThreadTime = (String) session.getAttribute("workerThreadTime");
    BaseTopic user = (BaseTopic) session.getAttribute("user");
    BaseTopic workspace = (BaseTopic) session.getAttribute("workspaces");
    Hashtable criterias = (Hashtable) session.getAttribute("importCriterias");
    Vector geoObjects = (Vector) session.getAttribute("geoObjects");
    Vector unusableGeoObjects = (Vector) session.getAttribute("unusableGeoObjects");
    //
	out.println("<dl style=\"width: 700px;\">");
	//
    //Enumeration e = workspaces.elements();
    //while (e.hasMoreElements()) {
		// BaseTopic workspace = (BaseTopic) e.nextElement();
		out.println("<dt><b>" + workspace.getName() + "</b> beinhaltet "+geoObjects.size() +" Projekte </dt>");
		Enumeration e2 = criterias.keys();
		while (e2.hasMoreElements()) {
			Object key = e2.nextElement();
            Object critInNumbers = criterias.get(key);
            out.println("<dd>");
                out.println(key.toString() + " (" +critInNumbers.toString()+ ")");
            out.println("</dd>");
		}
        out.println("</dl>");
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
        out.println("<h3> Ehrenamt Berlin Import Report </h3>");
        out.println("<pre> "+unusableGeoObjects.size()+" unlocatable Projects identified:</pre> <p/>");
        for (int i = 0; i < unusableGeoObjects.size(); i++) {
            GeoObjectTopic topic = (GeoObjectTopic) unusableGeoObjects.get(i);
            if (topic.getAddress() != null) {
                out.println(topic.getName()+", <b>Adress:</b> <i>"+topic.getAddress().getName()+"</i><br/>");
            } else {
                out.println(topic.getName()+", <b>Adress:</b> <i>empty</i><br/>");
            }
        }
	//}
%>
<% end(out); %>
