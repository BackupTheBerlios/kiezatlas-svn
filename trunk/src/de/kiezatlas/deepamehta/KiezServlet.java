package de.kiezatlas.deepamehta;

import de.deepamehta.BaseTopic;
import de.deepamehta.service.*;
import de.deepamehta.service.web.JSONRPCServlet;
import de.deepamehta.topics.TypeTopic;
import java.util.Hashtable;
import java.util.Vector;


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
        } else if(remoteMethod.equals("searchGeoObjects")) {
            result = searchTopics(params, directives);
        }
        return result;
    }

    protected String performAction(String topicId, String params, Session session, CorporateDirectives directives)
    {
        return topicId;
    }

    protected void preparePage(String page, String params, Session session, CorporateDirectives directives)
    {
        session.setAttribute("forumActivition", "off");
    }

    // --- Remote Methods

    private String getGeoObjectInfo(String params)
    {
        System.out.println(">>> getGeoObjectInfo(" + params + ")");
        String parameters[] = params.split(":");
        String topicId = parameters[0];
        StringBuffer messages = null;
        StringBuffer result = new StringBuffer("{\"result\": ");
        String geoObjectString = createGeoObjectBean(cm.getTopic(topicId, 1), messages);
        result.append(geoObjectString);
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
        System.out.println(">>> getWorkspaceCriterias(" + params + ")");
        String parameters[] = params.split(":");
        String workspaceId = parameters[0];
        StringBuffer messages = null;
        StringBuffer result = new StringBuffer("{\"result\": ");
        String criteriaList = createListOfCategorizations(workspaceId);
        result.append(criteriaList);
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
        System.out.println(">>> searchGeoObjects(" + params +")");
        StringBuffer result = new StringBuffer("{\"result\": ");
        StringBuffer messages = null;
        String parameters[] = params.split(":");
        String query = parameters[0];
        String workspaceId = parameters[1];
        // String topicmapId = parameters[2];
        Vector criterias = getKiezCriteriaTypes(workspaceId);
        BaseTopic geoType = getWorkspaceGeoType(workspaceId);
        Hashtable props = new Hashtable();
        props.put(PROPERTY_NAME, query);
        Vector results = cm.getTopics(geoType.getID(), props); // getTopic(query, props, topicmapId, directives);
        System.out.println(">>> found " + results.size() + " geoObjects with name " + query);
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
        System.out.println(">>> getGeoMapTopics(" + params + ")");
        String parameters[] = params.split(":");
        String mapId = parameters[0];
        String workspaceId = parameters[1];
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

    private String createSlimGeoObject(BaseTopic topic, Vector criterias, StringBuffer messages)
    {
        StringBuffer object = new StringBuffer();
        //
        String latitude = as.getTopicProperty(topic, "LAT");
        String longnitude = as.getTopicProperty(topic, "LONG");
        if(latitude.equals("") && longnitude.equals(""))
        {
            latitude = "0.0";
            longnitude = "0.0";
        }
        String name = topic.getName();
        if (hasQuotationMarks(name)) {
            name = convertHTMLForJSON(name);
        }
        object.append("{\"name\": \"" + name + "\",");
        object.append("\"id\": \"" + topic.getID() + "\",");
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
        bean.append("{\"id\": \"" + topicBean.id + "\",");
        bean.append("\"name\": \"" + topicBean.name + "\",");
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
                if(hasQuotationMarks(value))
                    // preparing java string for json
                    value = convertHTMLForJSON(value);
                    value = removeControlChars(value);
                bean.append("\"value\":  \"" + value + "\"");
            } else {
                Vector relatedFields = field.values;
                for(int r = 0; r < relatedFields.size(); r++)
                {
                    BaseTopic relatedTopic = (BaseTopic)relatedFields.get(r);
                    bean.append("\"values\": {");
                    bean.append("\"name\": \"" + relatedTopic.getName() + "\",");
                    // ### geoObject has it's own icon ?
                    bean.append("\"icon\": \"" + as.getTopicProperty(relatedTopic.getID(), 1, PROPERTY_ICON) + "\"}");
                }
                if(relatedFields.size() == 0)
                    bean.append("\"values\": {}");
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

        return catList.toString();
    }

    /**
     * list of categorizations for one workspace
     *
     * @param workspaceId
     * @return
     */
    private String createListOfCategorizations(String workspaceId)
    {
        StringBuffer objectList = new StringBuffer();
        objectList.append("[");
        Vector criterias = getKiezCriteriaTypes(workspaceId);
        for(int i = 0; i < criterias.size(); i++)
        {
            BaseTopic criteria = (BaseTopic) criterias.get(i);
            objectList.append("{\"critId\": \"" + criteria.getID() + "\", ");
            objectList.append("\"critName\": \"" + criteria.getName() + "\", ");
            objectList.append("\"categories\": [");
            Vector categories = cm.getTopics(criteria.getID());
            for(int c = 0; c < categories.size(); c++)
            {
                objectList.append("{");
                objectList.append("\"catId\":");
                BaseTopic cat = (BaseTopic)categories.get(c);
                // System.out.println(">>>> category(" + cat.getName() +" icon: "+ as.getLiveTopic(cat).getIconfile());
                objectList.append("\"" + cat.getID() + "\", ");
                objectList.append("\"catName\":");
                objectList.append("\"" + cat.getName() + "\", ");
                objectList.append("\"catIcon\":");
                objectList.append("\"" + as.getLiveTopic(cat).getIconfile() + "\"");

                int index = categories.indexOf(cat);
                if(index == categories.size() - 1) {
                    objectList.append("}");
                } else {
                    objectList.append("},");
                }
            }

            int andex = criterias.indexOf(criteria);
            if(andex == criterias.size() - 1)
                objectList.append("]}");
            else
                objectList.append("]},");
        }
        objectList.append("]");
        // System.out.println("list is: " + objectList);
        return objectList.toString();
    }

    /**
     * simply retrieves the BaseTopics assigned to a workspace which are used
     * for navigation in web-frontends
     *
     * @param workspaceId
     * @return
     */
    private Vector getKiezCriteriaTypes(String workspaceId)
    {
        Vector criterias = new Vector();
        TypeTopic critType = as.type("tt-ka-kriterium", 1);
        Vector subtypes = critType.getSubtypeIDs();
        Vector workspacetypes = as.getRelatedTopics(workspaceId, "at-uses", 2);
        for(int i = 0; i < workspacetypes.size(); i++)
        {
            BaseTopic topic = (BaseTopic)workspacetypes.get(i);
            for(int a = 0; a < subtypes.size(); a++)
            {
                String derivedOne = (String)subtypes.get(a);
                if(derivedOne.equals(topic.getID()))
                {
                    //System.out.println(">>> use criteria (" + derivedOne + ") " + topic.getName());
                    criterias.add(topic);
                }
            }

        }

        return criterias;
    }

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

    private String removeControlChars(String value) {
        // html uses carriage-return, line-feed and horizontal tab
        value = value.replaceAll("\r", "\\\\r");
        value = value.replaceAll("\n", "\\\\n");
        value = value.replaceAll("\t", "\\\\t");
        //value = value.replaceAll("\"", "\\\\\"");
        //System.out.println("replaced value is : " + value);
        return value;
    }

    private String convertHTMLForJSON(String html)
    {
        html = html.replaceAll("\"", "\\\\\"");
        return html;
    }
}
