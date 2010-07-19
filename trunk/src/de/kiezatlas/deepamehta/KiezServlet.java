package de.kiezatlas.deepamehta;

import de.deepamehta.BaseTopic;
import de.deepamehta.service.*;
import de.deepamehta.service.web.JSONRPCServlet;
import de.deepamehta.topics.TypeTopic;
import de.kiezatlas.deepamehta.topics.CityMapTopic;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class KiezServlet extends JSONRPCServlet implements KiezAtlas {

    // --- 3 Hooks overriden

    protected String performPostRequest(String remoteMethod, String params, Session session, CorporateDirectives directives)
    {
        String result = "";
        if(remoteMethod.equals("getMapTopics")) {
            result = getGeoMapTopics(params, session, directives);
        } else if(remoteMethod.equals("getGeoObjectInfo")) {
            result = getGeoObjectInfo(params);
        } else if(remoteMethod.equals("getWorkspaceCriterias")) {
            result = getWorkspaceCriterias(params);
        } else if(remoteMethod.equals("getWorkspaceInfos")) {
            result = getWorkspaceInfos(params);
        } else if(remoteMethod.equals("searchGeoObjects")) {
            result = searchTopics(params, directives);
        }
        return result;
    }

    protected String performAction(String topicId, String params, Session session, CorporateDirectives directives)
    {
        session.setAttribute("info", "<h3>Willkommen zu dem Kiezatlas Dienst unter "+as.getCorporateWebBaseURL()+"</h3>" +
                "<br>F&uuml;r die Nutzung des Dienstes steht Entwicklern " +
                "<a href=\"http://www.deepamehta.de/wiki/en/Application:_Web_Service\">hier</a> die Software Dokumentation zur Verfügung. " +
                "Ein Beispiel zur Nutzung eines Kiezatlas Dienstes ist <a href=\"http://www.kiezatlas.de/maps/map.php?topicId=t-ka-schoeneberg&workspaceId=t-ka-workspace\">hier</a> abrufbar.");
        return PAGE_SERVE;
    }

    protected void preparePage(String page, String params, Session session, CorporateDirectives directives)
    {
        session.setAttribute("forumActivition", "off");
    }

    // --- Remote Methods

    private String getGeoObjectInfo(String params)
    {
        String topicId = params.substring(2,params.length()-2);
        System.out.println(">>>> getGeoObjectInfo(" + topicId + ")");
        // String parameters[] = params.split(",");
        StringBuffer messages = new StringBuffer("\"");
        StringBuffer result = new StringBuffer("{\"result\": ");
        String geoObjectString;
        try {
            BaseTopic t = cm.getTopic(topicId, 1);
            if (t != null && t.getType().equals("tt-user")) {
                System.out.println("*** KiezServlet.SecurityAccessDenied: not allowed to access user information");
                messages = new StringBuffer("Access Denied");
                geoObjectString= "\"\"";
            } else if (t != null) {
                geoObjectString = createGeoObjectBean(t, messages);
            } else {
                geoObjectString = "{}";
                messages.append("404 - Topic not found");
            }
        } catch (Exception tex) {
            geoObjectString = "{}";
            messages.append(""+toJSON(tex.toString())+" - 404 - Topic not found");
        }
        result.append(geoObjectString);
        messages.append("\"");
        result.append(", \"error\": " + messages + "}");
        // System.out.println("result: "+ result.toString());
        return result.toString();
    }


    /**
     * Serializes Criterias and their Categories for a worksapce into JSON
     *
     * @param params
     * @return
     */
    private String getWorkspaceCriterias(String params)
    {
        System.out.println(">>>> getWorkspaceCriterias(" + params + ")");
        String parameters[] = params.split(",");
        String workspaceId = parameters[0];
        String mapId = parameters[1];
        StringBuffer messages = null;
        StringBuffer result = new StringBuffer("{\"result\": ");
        String criteriaList = createCritCatSystemList(workspaceId.substring(2, workspaceId.length()-2), mapId.substring(2, mapId.length()-2));
        result.append(criteriaList);
        result.append(", \"error\": " + messages + "}");
        return result.toString();
    }

    /**
     * Serializes Workspace Infos into JSON
     *
     * @param params
     * @return
     */
    private String getWorkspaceInfos(String params)
    {
        System.out.println(">>>> getWorkspaceInfos(" + params + ")");
        String parameters[] = params.split(":");
        String workspaceId = parameters[0];
        StringBuffer messages = null;
        StringBuffer result = new StringBuffer("{\"result\": ");
        String infos = createWorkspaceInfos(workspaceId.substring(2, workspaceId.length()-2));
        result.append(infos);
        result.append(", \"error\": " + messages + "}");
        return result.toString();
    }

    /**
     * search for topic property name and returns a list of slim geo objects as
     * results
     *
     * @param params
     * @param directives
     * @return
     */
    private String searchTopics(String params, CorporateDirectives directives) {
        System.out.println(">>>> searchGeoObjects(" + params +")");
        StringBuffer result = new StringBuffer("{\"result\": ");
        StringBuffer messages = null;
        String parameters[] = params.split(",");
        String query = parameters[0];
        String topicmapId = parameters[1];
        String workspaceId = parameters[2];
        // String topicmapId = parameters[2];
        query = query.substring(2, query.length()-1);
        topicmapId = topicmapId.substring(2, topicmapId.length()-1);
        workspaceId = workspaceId.substring(2, workspaceId.length()-2);
        // LOG System.out.println("INFO: query: " + query + ", " + "workspaceId: " + workspaceId);
        Vector criterias = getKiezCriteriaTypes(workspaceId);
        BaseTopic geoType = getWorkspaceGeoType(workspaceId);
        Hashtable props = new Hashtable();
        props.put(PROPERTY_NAME, query);
        // String typeID, Hashtable propertyFilter, String topicmapID
        Vector results = cm.getTopics(geoType.getID(), props, topicmapId, false); // getTopic(query, props, topicmapId, directives);
        // getTopics: String typeID, String nameFilter, Hashtable propertyFilter, String relatedTopicID
        // getRelated: String topicID, String assocType, String relTopicType, int relTopicPos
        Vector topicsToQuery = cm.getViewTopics(topicmapId, 1);
        Vector streetResults = new Vector();
        for (int i = 0; i < topicsToQuery.size(); i++) {
            BaseTopic topic = (BaseTopic) topicsToQuery.get(i);
            Vector addresses = cm.getRelatedTopics(topic.getID(), ASSOCTYPE_ASSOCIATION, TOPICTYPE_ADDRESS, 2);
            for (int j = 0; j < addresses.size(); j++) {
                BaseTopic addressTopic = (BaseTopic) addresses.get(j);
                String streetName = as.getTopicProperty(addressTopic, PROPERTY_STREET);
                if (streetName.indexOf(query) != -1) {
                    streetResults.add(topic);
                    // System.out.println(">>>> streetFound + " + streetName+ " for " + topic.getName());
                }
            }
        }
        results.addAll(streetResults);
        System.out.println(">>>> found " + results.size() + " named and "+streetResults.size()+ " streetnames like " + query);
        //
        result.append("[");
        for (int i=0; i < results.size(); i++) {
            BaseTopic topic = (BaseTopic) results.get(i);
            result.append(createSlimGeoObject(topic, criterias, new StringBuffer()));
            if (results.indexOf(topic) == results.size()-1) {
                result.append("]");
            } else {
                result.append(", ");
            }
        }
        result.append(", \"error\": " + messages + "}");
        return result.toString();
    }

    /**
     * delivers all topics in a citymap as "slim" geoobjects
     *
     * @param params (mapId, workspaceId)
     * @param session
     * @param directives
     * @return
     */
    private String getGeoMapTopics(String params, Session session, CorporateDirectives directives)
    {
        System.out.println(">>>> getGeoMapTopics(" + params + ")");
        String parameters[] = params.split(",");
        String mapId = parameters[0];
        String workspaceId = parameters[1];
        mapId = mapId.substring(2,mapId.length()-2);
        workspaceId = workspaceId.substring(2, workspaceId.length()-2);
        // LOG System.out.println("INFO: mapId is " + mapId + ", " + " and workspaceId is " + workspaceId);
        StringBuffer messages = null;
        StringBuffer result = new StringBuffer("{\"result\": ");
        StringBuffer mapTopics = new StringBuffer("{ \"map\": \"" + mapId + "\", \"topics\": [");
        //
        BaseTopic geoType = (BaseTopic) getWorkspaceGeoType(workspaceId);
        Vector criterias = getKiezCriteriaTypes(workspaceId);
        Vector allTopics = cm.getTopics(geoType.getID(), new Hashtable(), mapId);
        //System.out.println(">>> " + topics.size() +" topics for map " + mapTopic.getName() + " of type : " + mapTopic.getType());
        System.out.println(">>> " + allTopics.size() +" within current map");
        //for(int t = 0; t < allTopics.size(); t++)
        for(int i = 0; i< allTopics.size(); i++) {
            BaseTopic topic = (BaseTopic) allTopics.get(i);
            // System.out.println("    e: " + topic.getName());
            String geo  = createSlimGeoObject(topic, criterias, messages);
            // System.out.println("geo: " + geo);
            mapTopics.append(geo);
            // mapTopics.append("}");
            if(allTopics.indexOf(topic) != allTopics.size() - 1) {
                mapTopics.append(",");
            }
        }
        mapTopics.append("]}");
        result.append(mapTopics);
        result.append(", \"error\": " + messages + "}");
        return result.toString();
    }

    // -------------------
    // --- Utility Methods
    // -------------------

    private String createWorkspaceInfos(String workspaceId)
    {
        // System.out.println(">>> createWorkspaceInfos(" + workspaceId+") ...");
        StringBuffer object = new StringBuffer();
        String workspaceName = as.getTopicProperty(workspaceId, 1, PROPERTY_NAME);
        String logoURL = "";
        String impressumURL = "";
        String homepageURL = "";
        //
        BaseTopic logo = as.getRelatedTopic(workspaceId, ASSOCTYPE_ASSOCIATION, TOPICTYPE_IMAGE, 2, true);
        if (logo != null) logoURL = as.getTopicProperty(logo, PROPERTY_FILE);
        BaseTopic homepage = as.getRelatedTopic(workspaceId, KiezAtlas.ASSOCTYPE_HOMEPAGE_LINK, TOPICTYPE_WEBPAGE, 2, true);
        if (homepage != null) homepageURL = as.getTopicProperty(homepage, PROPERTY_URL);
        BaseTopic impressum = as.getRelatedTopic(workspaceId, KiezAtlas.ASSOCTYPE_IMPRESSUM_LINK, TOPICTYPE_WEBPAGE, 2, true);
        if (impressum != null) impressumURL = as.getTopicProperty(impressum, PROPERTY_URL);
        //
        object.append("{\"name\": \"" + workspaceName + "\",");
        object.append("\"logo\": \"" + logoURL + "\",");
        object.append("\"imprint\": \"" + impressumURL + "\",");
        object.append("\"homepage\": \"" + homepageURL + "\"");
        object.append("}");
        // System.out.println("workspaceInfos are imp:" + impressumURL + " home:" + homepageURL + " logo:" + logoURL);
        return object.toString();
    }

    private String createSlimGeoObject(BaseTopic topic, Vector criterias, StringBuffer messages)
    {
        StringBuffer object = new StringBuffer();
        //
        String latitude = as.getTopicProperty(topic, "LAT"); // ### get an interface place for this final string value
        String longnitude = as.getTopicProperty(topic, "LONG"); // ### get an interface place for this final string value
        String originId = as.getTopicProperty(topic, ImportServlet.PROPERTY_PROJECT_ORIGIN_ID);
        if(latitude.equals("") && longnitude.equals(""))
        {
            latitude = "0.0";
            longnitude = "0.0";
        }
        String name = topic.getName();
        name = toJSON(name);
        //}
        object.append("{\"name\": \"" + name + "\",");
        object.append("\"id\": \"" + topic.getID() + "\",");
        object.append("\"originId\": \"" + originId + "\",");
        object.append("\"lat\": \"" + latitude + "\",");
        object.append("\"long\": \"" + longnitude + "\",");
        // System.out.println(">>> createCritCatList(" + topic.getID()+") ...");
        object.append("\"criterias\": " + createTopicCategorizations(topic.getID(), criterias));
        object.append("}");
        return object.toString();
    }

    /**
     * Serializes TopicBean into JSON
     *
     * @param topic
     * @param messages
     * @return
     */
    private String createGeoObjectBean(BaseTopic topic, StringBuffer messages) {
        StringBuffer bean = new StringBuffer();
        //
        TopicBean topicBean = as.createTopicBean(topic.getID(), 1);
        removeCredentialInformation(topicBean);
        String topicName = removeQuotationMarksFromNames(topicBean.name);
        bean.append("{\"id\": \"" + topicBean.id + "\",");
        bean.append("\"name\": \"" + topicName + "\",");
        bean.append("\"icon\": \"" + topicBean.icon + "\",");
        bean.append("\"properties\": [");
        Vector properties = topicBean.fields;
        for(int p = 0; p < properties.size(); p++)
        {
            TopicBeanField field = (TopicBeanField)properties.get(p);
            bean.append("{\"type\": \"" + field.type + "\", ");
            bean.append("\"name\": \"" + field.name + "\", ");
            bean.append("\"label\": \"" + field.label + "\", ");
            if(field.type == 0) {
                String value = field.value;
                value = toJSON(value);
                bean.append("\"value\":  \"" + value + "\"");
            } else {
                Vector relatedFields = field.values;
                if (relatedFields.size() == 0) {
                    bean.append("\"values\": []");
                } else {
                    bean.append("\"values\": [");
                    for (int r = 0; r < relatedFields.size(); r++) {
                        BaseTopic relatedTopic = (BaseTopic) relatedFields.get(r);
                        bean.append("{\"name\": \"" + relatedTopic.getName() + "\",");
                        // ### geoObject has it's own icon ?
                        bean.append("\"icon\": \"" + as.getLiveTopic(relatedTopic).getIconfile() + "\"}");
                        if ( r == relatedFields.size()-1 )  {
                            bean.append("]");
                        } else {
                            bean.append(", ");
                        }
                    }
                }
            }
            if(properties.indexOf(field) == properties.size() - 1)
                bean.append("}");
            else
                bean.append("},");
        }
        bean.append("]");
        bean.append("}");
        return bean.toString();
    }

    private TopicBean removeCredentialInformation(TopicBean topicBean) {
        topicBean.removeFieldsContaining(PROPERTY_PASSWORD);
        topicBean.removeFieldsContaining(PROPERTY_OWNER_ID);
        topicBean.removeFieldsContaining(PROPERTY_WEB_ALIAS);
        topicBean.removeFieldsContaining(PROPERTY_LAST_MODIFIED);
        //
        return topicBean;
    }

    /**
     * creates a list of categorizations for each slim topic
     * checks for each criteria type if one is directly associated with the category
     *
     * @param topicId
     * @param workspaceId
     * @return
     */
    private String createTopicCategorizations(String topicId, Vector criterias)
    {
        StringBuffer catList = new StringBuffer("[");
        BaseTopic topic = cm.getTopic(topicId, 1);
        for(int i = 0; i < criterias.size(); i++)
        {
            BaseTopic criteria = (BaseTopic) criterias.get(i);
            // which topics are related and are of type criteria
            Vector categories = as.getRelatedTopics(topic.getID(), "at-association", criteria.getID(), 2);
            int andex;
            if(categories.size() == 0)
            {
                catList.append("{\"critId\": \"" + criteria.getID() + "\", ");
                catList.append("\"categories\": []");
                andex = criterias.indexOf(criteria);
                // awkyard
                if(andex == criterias.size() - 1)
                    catList.append("}]");
                else
                    catList.append("},");
                continue;
            }
            catList.append("{\"critId\": \"" + criteria.getID() + "\", ");
            catList.append("\"categories\": [");
            for(int c = 0; c < categories.size(); c++)
            {
                BaseTopic cat = (BaseTopic)categories.get(c);
                int index = categories.indexOf(cat);
                // awkyard
                if(index == categories.size() - 1)
                    catList.append("\"" + cat.getID() + "\"");
                else
                    catList.append("\"" + cat.getID() + "\", ");
            }
            int c = criterias.indexOf(criteria);
            if(c == criterias.size() - 1)
                catList.append("]}]");
            else
                catList.append("]},");
        }
        if (criterias.size() == 0) catList.append("]");
        return catList.toString();
    }

    /**
     * list of all categorizations for one workspace
     *
     * @param workspaceId
     * @return
     */
    private String createCritCatSystemList(String workspaceId, String mapId)
    {
        StringBuffer objectList = new StringBuffer();
        objectList.append("[");
        // Vector criterias = getKiezCriteriaTypes(workspaceId);
        CityMapTopic mapTopic = (CityMapTopic) as.getLiveTopic(mapId, 1);
        SearchCriteria[] crits = mapTopic.getSearchCriterias();
        // Vector collectedCrits = new Vector();
        for (int i = 0; i < crits.length; i++) {
            SearchCriteria searchCriteria = crits[i];
            // collectedCrits.add(searchCriteria.criteria.getID());
            objectList.append("{\"critId\": \"" + searchCriteria.criteria.getID() + "\", ");
            objectList.append("\"critName\": \"" + searchCriteria.criteria.getName() + "\", ");
            objectList.append("\"categories\": [");
            Vector categories = cm.getTopics(searchCriteria.criteria.getID());
            for (int c = 0; c < categories.size(); c++) {
                objectList.append("{");
                objectList.append("\"catId\":");
                BaseTopic cat = (BaseTopic)categories.get(c);
                // System.out.println(">>>> category(" + cat.getName() +" icon: "+ as.getLiveTopic(cat).getIconfile());
                objectList.append("\"" + cat.getID() + "\", ");
                objectList.append("\"catName\":");
                objectList.append("\"" + cat.getName() + "\", ");
                objectList.append("\"catIcon\":");
                objectList.append("\"" + as.getLiveTopic(cat).getIconfile() + "\"");
                //
                int index = categories.indexOf(cat);
                if(index == categories.size() - 1) {
                    objectList.append("}");
                } else {
                    objectList.append("},");
                }
            }
            if(i == crits.length - 1)
                objectList.append("]}");
            else
                objectList.append("]},");
        }
        objectList.append("]");
        // System.out.println("list is: " + objectList);
        return objectList.toString();
    }

    /**
     * simply retrieves the topics assigned to a workspace which are used
     * for navigation in web-frontends
     *
     * @param workspaceId
     * @return
     */
    private Vector getKiezCriteriaTypes(String workspaceId) {
        //
        Vector criterias = new Vector();
        TypeTopic critType = as.type("tt-ka-kriterium", 1);
        Vector subtypes = critType.getSubtypeIDs();
        Vector workspacetypes = as.getRelatedTopics(workspaceId, "at-uses", TOPICTYPE_TOPICTYPE, 2, true, true);
        for ( int i = 0; i < workspacetypes.size(); i++ ) {
            BaseTopic topic = (BaseTopic)workspacetypes.get(i);
            for ( int a = 0; a < subtypes.size(); a++ ) {
                String derivedOne = (String)subtypes.get(a);
                if ( derivedOne.equals(topic.getID()) ) {
                    // System.out.println(">>> use criteria (" + derivedOne + ") " + topic.getName());
                    criterias.add(topic);
                }
            }
        }
        return criterias;
    }

    // ### ToDo: as.getLiveTopic().getSearchCriteria();
    
    /**
     * returns null if no topictype whihc is assigned to the given workspace,
     * is a subtype of "GeoObjectTopic"
     *
     * @param workspaceId
     * @return
     */
    private BaseTopic getWorkspaceGeoType(String workspaceId) {
        //
        TypeTopic geotype = as.type(TOPICTYPE_KIEZ_GEO, 1);
        Vector subtypes = geotype.getSubtypeIDs();
        Vector workspacetypes = as.getRelatedTopics(workspaceId, ASSOCTYPE_USES, 2);
        int i;
        for (i = 0; i < workspacetypes.size(); i++) {
            BaseTopic topic = (BaseTopic) workspacetypes.get(i);
            int a;
            for (a = 0; a < subtypes.size(); a++) {
                // System.out.println(" counter: " + a);
                String derivedOne = (String) subtypes.get(a);
                // System.out.println("    " + derivedOne.getID() + ":" + derivedOne.getName());
                if (derivedOne.equals(topic.getID())) {
                    // System.out.println(" found geoType " + topic.getID() + ":" + topic.getName());
                    return topic;
                }
            }
        }
        return null;
    }

    private boolean hasQuotationMarks(String value)
    {
        return value.indexOf("\"") != -1;
    }

    private String removeQuotationMarksFromNames(String name) {
        name = name.replaceAll("\"", "");
        return name;
    }

    private String removeControlChars(String value) {
        // html uses carriage-return, line-feed and horizontal tab
        value = value.replaceAll("\r", "\\\\r");
        value = value.replaceAll("\n", "\\\\n");
        value = value.replaceAll("\t", "\\\\t");
        value = value.replaceAll("\f", "\\\\f");
        value = value.replaceAll("\b", "\\\\b");
        value = value.replaceAll("\"", "\\\\\"");
        //System.out.println("replaced value is : " + value);
        return value;
    }

    private String toJSON(String text) {
        // strip HTML tags
        text = text.replaceAll("<html>", "");
        text = text.replaceAll("</html>", "");
        text = text.replaceAll("<head>", "");
        text = text.replaceAll("</head>", "");
        text = text.replaceAll("<body>", "");
        text = text.replaceAll("</body>", "");
        text = text.replaceAll("<p>", "");
        text = text.replaceAll("<p style=\"margin-top: 0\">", "");
        text = text.replaceAll("</p>", "");
        // convert HTML entities
        text = toUnicode(text);
        //
        text = text.trim();
        // JSON conformity
        text = removeControlChars(text);
        // text = text.replaceAll("\r", "\\\\n");
        // text = text.replaceAll("\n", "\\\\n");
        // text = text.replaceAll("\"", "\\\\\"");
        //
        return text;
    }

    private String convertHTMLForJSON(String html)
    {
        html = html.replaceAll("\"", "\\\\\"");
        // html = html.replaceAll("\r", "\\\\n");
        // html = html.replaceAll("\n", "\\\\n");
        // html = html.replaceAll("\t", "\\\\t");
        return html;
    }

    private String toUnicode(String text)
    {
        StringBuffer buffer = new StringBuffer();
        Pattern p = Pattern.compile("&#(\\d+);");
        Matcher m = p.matcher(text);
        while (m.find()) {
            int c = Integer.parseInt(m.group(1));
            m.appendReplacement(buffer, Character.toString((char) c));
        }
        m.appendTail(buffer);
        return buffer.toString();
    }

}
