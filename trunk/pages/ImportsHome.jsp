<%@ include file="KiezAtlas.jsp" %>

<% begin(KiezAtlas.SERVLET_IMPORT, session, out); %>
<%
    String membership = (String) session.getAttribute("membership");
    String timingInterval = (String) session.getAttribute("timingInterval");
    String timingIntervalTwo = (String) session.getAttribute("timingIntervalTwo");
    String workerThreadState = (String) session.getAttribute("workerThread");
    String workerThreadStateTwo = (String) session.getAttribute("workerThreadTwo");
    String workerThreadTime = (String) session.getAttribute("workerThreadTime");
    String workerThreadTimeTwo = (String) session.getAttribute("workerThreadTimeTwo");
    BaseTopic user = (BaseTopic) session.getAttribute("user");
    Vector workspaces = (Vector) session.getAttribute("importWorkspaces");
    // Hashtable criterias = (Hashtable) session.getAttribute("importCriterias");
    Vector geoProjectObjects = (Vector) session.getAttribute("geoProjectObjects");
    Vector geoEventObjects = (Vector) session.getAttribute("geoEventObjects");
    Vector unusableGeoObjects = (Vector) session.getAttribute("unusableGeoObjects");
    //
    out.println("<dl style=\"width: 700px;\">");
    //
    Enumeration e = workspaces.elements();
    while (e.hasMoreElements()) {
      BaseTopic workspace = (BaseTopic) e.nextElement();
      if (workspace.getName().equals("Ehrenamt Berlin")) {
        out.println("<dt><b>" + workspace.getName() + "</b> beinhaltet "+geoProjectObjects.size() +" Projekte </dt>");
      } else {
        out.println("<dt><b>" + workspace.getName() + "</b> beinhaltet "+geoEventObjects.size() +" Veranstaltungen </dt>");
      }
      /*Enumeration e2 = criterias.keys();
      while (e2.hasMoreElements()) {
          Object key = e2.nextElement();
          Object critInNumbers = criterias.get(key);
          out.println("<dd>");
              out.println(key.toString() + " (" +critInNumbers.toString()+ ")");
          out.println("</dd>");
      }*/
      out.println("<dd><a href=\"?action="+KiezAtlas.ACTION_RESET_CRITERIAS+"&workspaceId="+workspace.getID()+"\"> resetCategories</a></dd>");
      if (e.hasMoreElements()) {
        if (workerThreadState.equals("NEW")) {
            out.println("<p><i>");
            out.println("The Import Worker for this workspace was kicked off at " + workerThreadTime + " and is currently working / sleeping. ");
            out.println("<br/> Import Intervall is set to " +timingInterval+ " min.</i><p/>");
        } else {
            out.println("<a href=\"?action="+KiezAtlas.ACTION_DO_IMPORT+"&workspaceId="+workspace.getID()+"\">> kickoff Import</a>");
            out.println("<p><i>");
            out.println("The Import Worker for this Workspace was NOT kicked off yet.");
            out.println("</i><p/>");
        }
      } else {
        if (workerThreadStateTwo.equals("NEW")) {
            out.println("<p><i>");
            out.println("The Import Worker for this workspace was kicked off at " + workerThreadTimeTwo + " and is currently working / sleeping. ");
            out.println("<br/> Import Intervall is set to " +timingIntervalTwo+ " min.</i><p/>");
        } else {
            out.println("<a href=\"?action="+KiezAtlas.ACTION_DO_IMPORT+"&workspaceId="+workspace.getID()+"\">> kickoff Import</a>");
            out.println("<p><i>");
            out.println("<br/> The Import Worker for this Workspace was NOT kicked off yet.</i><p/>");
        }
      }
    }
    out.println("</dl>");
    out.println("<h3> Import Report for both Workspaces </h3>");
    out.println("<pre> "+unusableGeoObjects.size()+" unlocatable Projects identified:</pre> <p/>");
    for (int i = 0; i < unusableGeoObjects.size(); i++) {
        GeoObjectTopic topic = (GeoObjectTopic) unusableGeoObjects.get(i);
        if (topic.getAddress() != null) {
            out.println(topic.getName()+", <b>Address:</b> <i>"+topic.getAddress().getName()+"</i><br/>");
        } else {
            out.println(topic.getName()+", <b>Address:</b> <i>empty</i><br/>");
        }
    }
%>
<% end(out); %>
