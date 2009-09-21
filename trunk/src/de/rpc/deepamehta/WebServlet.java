package de.rpc.deepamehta;

import de.deepamehta.AmbiguousSemanticException;
import de.deepamehta.BaseAssociation;
import de.deepamehta.BaseTopic;
import de.deepamehta.DeepaMehtaException;
import de.deepamehta.PresentableTopic;
import de.deepamehta.PresentableType;
import de.deepamehta.service.*;
import de.deepamehta.service.web.JSONRPCServlet;
import de.deepamehta.topics.LiveTopic;
import de.deepamehta.topics.TypeTopic;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;


public class WebServlet extends JSONRPCServlet implements WebService {
    
    protected String performPostRequest(String remoteMethod, String params, Session session, CorporateDirectives directives)
    {
        String result = "";
        if(remoteMethod.equals("getMapTopics")) {
            // --- Loads all the Topics in One Map
            result = getMapTopics(params, session, directives);
        } else if(remoteMethod.equals("getTopicDetails")) {
            // --- Loads a complete TopicBean By Id
            result = getTopicDetails(params);
        } else if(remoteMethod.equals("getTopicmap")) {
            // --- Loads a complete Topicmap By Id
            result = getTopicmapById(params, session, directives);
        } else if(remoteMethod.equals("getViewsInUse")) {
            // --- Loads a list of currently opened Topicmaps per User
            result = getViewsInUse(params, session, directives);
        } else if(remoteMethod.equals("getTopicTypes")) {
            // --- Loads all TopicTypes By Id
            result = getTopicTypes(params);
        } else if(remoteMethod.equals("getRelatedTopics")) {
            // --- Loads What's Related
            result = getRelatedTopics(params);
        } else if(remoteMethod.equals("searchTopics")) {
            // --- Nice Search
            result = searchTopics(params, directives);
        } else if(remoteMethod.equals("getUserWorkspaces")) {
            // --- Loads the available workspaces for a user topicId
            result = getUserWorkspaces(params, directives);
        } else if(remoteMethod.equals("doAuthentication")) {
            // --- Authentication against a given username and it's word to pass
            result = doAuthentication(params, directives);
        } else if(remoteMethod.equals("doRegister")) {
            // --- To Test
            result = registerNewUser(params, directives);
        } else if(remoteMethod.equals("storeTopicProperty")) {
            // --- To Test
            result = storeTopicProperty(params, directives);
        } else if(remoteMethod.equals("createAssociation")) {
            // --- ToDo
            //result = createAssociation(params, directives);
        } else if(remoteMethod.equals("createTopic")) {
            // --- ToDo
            //result = createTopic(params, directives);
        }
        System.out.println("\n---");
        System.out.println(result.toString());
        System.out.println("---");
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

    // --- Methods Calls which receive their params from a JSON String

    private String getTopicDetails(String params)
    {
        // String parameters[] = params.split(",");
        String topicId = params.substring(2, params.length()-2);
        LiveTopic liveTopic = as.getLiveTopic(topicId, 1);
        System.out.println(">>>> getTopicDetails(" + topicId + ")");
        // liveTopic.conte
//        CorporateCommands cmds = liveTopic.contextCommands(topicMapId, "viewMode", null, directives);
//        // cmds.params
//        for (int i = 0; i < cmds.groupCommands.size(); i++) {
//            Commands cmd = (Commands) cmds.groupCommands.get(i);
//            System.out.println("    > liveTopic has "+cmd.command+" as a context command");
//        }
        StringBuffer messages = new StringBuffer("\"\"");
        StringBuffer result = new StringBuffer("{\"method\": \"getTopicDetails\", ");
        result.append("\"result\": ");
        String geoObjectString = createBean(cm.getTopic(topicId, 1), messages);
        result.append(geoObjectString);
        result.append(", \"error\": " + messages + "}");
        // System.out.println("result: "+ result.toString());
        return result.toString();
    }

    private String getRelatedTopics(String params) {
        String topicId = params.substring(2, params.length()-2);
        System.out.println(">>>> getRelatedTopics(" + topicId + ")");
        StringBuffer messages = new StringBuffer("\"\"");
        StringBuffer result = new StringBuffer("{\"method\": \"getTopicDetails\", ");
        result.append("\"result\": ");
        String geoObjectString = createRelatedTopics(cm.getTopic(topicId, 1), messages);
        result.append(geoObjectString);
        result.append(", \"error\": " + messages + "}");
        // System.out.println("result: "+ result.toString());
        return result.toString();
    }


    /**
     * Get a list of TopicTypes into a JSON String
     * At it's best with it's TypeDef consisting out of TopicType and it's SuperTypes {@link PropertyDefinitions}
     *
     * @param params
     * @return
     */
    private String getTopicTypes(String params)
    {
        System.out.println(">>>> getTopicTypes(" + params + ")");
        String parameters[] = params.split(",");
        Vector typeIds = new Vector();
        int len = parameters.length;
        for (int i=0; i<len; i++) {
            int start = parameters[i].indexOf("\"");
            int end = parameters[i].lastIndexOf("\"");
            typeIds.add(parameters[i].substring(start+1, end));
        }
        StringBuffer messages = new StringBuffer("\"\"");
        StringBuffer result = new StringBuffer("{\"method\": \"getTopicTypes\", ");
        result.append("\"result\": ");
        String typeList = createListOfTypeDescriptions(typeIds);
        result.append(typeList);
        result.append(", \"error\": " + messages + "}");
        return result.toString();
    }

    private boolean vectorContainsAssocType(String typeId, Vector v) {
        for (int i = 0; i < v.size(); i++) {
            BaseAssociation asso = (BaseAssociation) v.get(i);
            if (asso.getID().equals(typeId)) {
                return true;
            }
        }
        return false;
    }

    private String createRelatedTopics(BaseTopic topic, StringBuffer messages) {
        StringBuffer related = new StringBuffer("{");
        Vector allRelatedTopics = cm.getRelatedTopics(topic.getID());
        Hashtable allAssocTypes = cm.getAssociationTypes(topic.getID());
        Hashtable allTopicTypes = cm.getTopicTypes(topic.getID());
        Enumeration e = allAssocTypes.keys();
        related.append("\"assocTypes\": [");
        while(e.hasMoreElements()) {
            String keyId = (String) e.nextElement();
            String count = (String) allAssocTypes.get(keyId); // PresentableType pa = (PresentableType)
            related.append("{\"typeId\": \""+keyId+"\", \"count\": "+count+"}");
            // System.out.println(">> Found AssocType " +count + " at keyId: " + keyId);// +pa.getAssocTypeColor()+" and "+ pa.getID());
            if (e.hasMoreElements()) related.append(", ");
        }
        related.append("], \"topicTypes\": [");
        Enumeration b = allTopicTypes.keys();
        while(b.hasMoreElements()) {
            String keyId = (String) b.nextElement();
            String count = (String) allTopicTypes.get(keyId); // PresentableType pa = (PresentableType)
            // System.out.println(">> Found TopicType " +count + " at keyId: " + keyId);// pa.getName()+ " between " +pa.getTypeIconfile()+" and "+ pa.getID());
            related.append("{\"typeId\": \""+keyId+"\", \"count\": "+count+"}");
            if (b.hasMoreElements()) {related.append(", ");}
        }
        related.append("], \"topics\": [");
        System.out.println(">>> getRelatedTopics() found " + allRelatedTopics.size() + " related topics");
        for (int i = 0; i < allRelatedTopics.size(); i++) {
            BaseTopic relatedTopic = (BaseTopic) allRelatedTopics.get(i);
            Vector associations = cm.getAssociations(topic.getID(), relatedTopic.getID(), true);
            // System.out.println(">> topic is "+associations.size()+" times linked with "+relatedTopic.getID());
            related.append(createSlimTopic(relatedTopic, messages));
//            Vector assocTypes = new Vector();
//            for (int j = 0; j < associations.size(); j++) {
//                BaseAssociation assoc = (BaseAssociation) associations.get(j);
//                if (vectorContainsAssocType(assoc.getType(), assocTypes)) {
//                    assocTypes.add(assoc);
//                    System.out.println(">> topic "+relatedTopic.getName()+" is related via "+assoc.getType() +" assocType");
//                } else {
//                    System.out.println(">> topic "+relatedTopic.getName()+" is related via "+assoc.getType() + " assocType");
//                }
//            }
            if (i < allRelatedTopics.size() - 1) {
                related.append(", ");
            }
        }
        related.append("]}");
        return related.toString();
    }

    /**
     * The Descriptions are not passed and the accessPermission according to a workspace
     * are not considered when serializing the Topic and AssocTypes into JSON.
     * createListof.... needs to know from which workspace the user acccess the type
     * propably a hashtable is needed instead of passing just a vector with typeIds to
     * the helpers
     *
     * @param params
     * @param directives
     * @return
     */

    private String getUserWorkspaces(String params, CorporateDirectives directives) {
        String userId = params.substring(2, params.length()-2);
        System.out.println(">>>> getUserWorkspaces(" + userId + ")");
        // String mapId = params;
        //String parameters[] = params.split(",");
        // String workspaceId = parameters[1];
        StringBuffer messages = new StringBuffer("\"\"");
        StringBuffer result = new StringBuffer("{\"method\": \"getUserWorkspaces\", ");
        result.append("\"result\": [");
        Hashtable p = new Hashtable();
        p.put(PROPERTY_USERNAME, userId);
        BaseTopic user = as.getTopic(TOPICTYPE_USER, p, null, directives);
        if (user != null) {
            Vector workspaces = as.getRelatedTopics(user.getID(), ASSOCTYPE_MEMBERSHIP, TOPICTYPE_WORKSPACE, 2);
            Enumeration ws = workspaces.elements();
            while ( ws.hasMoreElements() ) {
                BaseTopic workspace = (BaseTopic) ws.nextElement();
                System.out.println(">> membership in workspaces " + workspace.getName());
                result.append("{\"id\": \""+workspace.getID()+"\", ");
                result.append("\"name\": \""+workspace.getName()+"\", ");
                result.append("\"isPublic\": \""+as.getTopicProperty(workspace, PROPERTY_PUBLIC)+"\", ");
                // result.append("\"description\": \""+as.getTopicProperty(workspace, PROPERTY_DESCRIPTION)+"\", ");
                result.append("\"description\": \"\", ");
                result.append("\"icon\": \""+as.getTopicProperty(workspace, PROPERTY_ICON)+"\", ");
                // BaseAssociation membership = as.getAssociation(userId, ASSOCTYPE_MEMBERSHIP, 2, TOPICTYPE_WORKSPACE, true, directives);
                // System.out.println(">> with " + as.getAssocProperties(membership).size() + " preferences ");
                Vector wsTypes = as.getRelatedTopics(workspace.getID(), ASSOCTYPE_USES, TOPICTYPE_TOPICTYPE, 2);
                result.append("\"types\": ");
                Vector typeIds = new Vector();
                for (int i = 0; i < wsTypes.size(); i++) {
                    BaseTopic type = (BaseTopic) wsTypes.get(i);
                    // System.out.println(">>> for type " + type.getName());
                    typeIds.add(type.getID());
//                    result.append("{ \"id\": \""+type.getID()+"\", \"name\": \""+type.getName()+"\", " +
//                        "\"pluralName\": \""+as.getTopicProperty(type, PROPERTY_PLURAL_NAME)+"\", " +
//                        "\"iconFile\": \""+as.getTopicProperty(type, PROPERTY_ICON)+"\", " +
//                        "\"borderColor\": \""+as.getTopicProperty(type, "Border Color")+"\", " +
//                        "\"bgColor\": \""+as.getTopicProperty(type, "Color")+"\"}");
                    // if (i == wsTypes.size()-1) result.append(type.getID()+ ""); else result.append(type.getID()+ ", ");
                }
                // reuse private method
                result.append(createListOfTypeDescriptions(typeIds));
                result.append(", \"assocTypes\": ");
                Vector assocTypes = as.getRelatedTopics(workspace.getID(), ASSOCTYPE_USES, TOPICTYPE_ASSOCTYPE, 2);
                Vector assocTypeIds = new Vector();
                for (int i = 0; i < assocTypes.size(); i++) {
                    BaseTopic assoc = (BaseTopic) assocTypes.get(i);
                    assocTypeIds.add(assoc.getID());
                }
                result.append(createListOfAssocTypeDescriptions(assocTypeIds));
                if (!ws.hasMoreElements()) result.append("}"); else result.append("}, ");
            }
        } else {
            messages.append("\"The username is unknown to DeepaMehta.\"");
        }
        result.append("], \"error\": " + messages + "}");
        return result.toString();
    }

    /**
     * returns either a FXUserTopic or is empty
     * one approach for simple request based authentication might be calculating a Hash or MD5 sum in here and returning it
     * which is temporarly stored and reset after a new login attempt
     *
     * @param params
     * @param directives
     * @return
     */
    private String doAuthentication(String params, CorporateDirectives directives) {
        String parameters[] = params.split(",");
        String userName = parameters[0].substring(2, parameters[0].length()-1);
        String password = parameters[1].substring(2, parameters[1].length()-2);
        System.out.println(">>>> doAuthentication(" + userName + ", "+ password +")");
        // String mapId = params;
        
        StringBuffer messages = new StringBuffer("\"\"");
        StringBuffer result = new StringBuffer("{\"method\": \"doAuthentication\", ");
        result.append("\"result\": ");
        if (as.loginCheck(userName, password)) {
            //cm.getTopicsByName(userName);
            Hashtable properties = new Hashtable();
            properties.put(PROPERTY_USERNAME, userName);
            BaseTopic user = as.getTopic(TOPICTYPE_USER, properties, null, directives);
            result.append("{ \"id\": \""+user.getID()+"\", \"name\": \""+user.getName()+"\", \"typeId\": \""+user.getType()+"\", " +
                    "\"email\": \""+as.getTopicProperty(user, PROPERTY_EMAIL_ADDRESS)+"\", " +
                    "\"website\": \""+as.getTopicProperty(user, "Website")+"\"}");
        } else {
            result.append("\"\"");
            messages = new StringBuffer("\"Login Failed\"");
        }
        result.append(", \"error\": " + messages + "}");
        return result.toString();
    }

    /**
     * yet untested
     *
     * @param params username, password, emailAddress, notifications
     * @param directives
     * @return as a JSON FXUserTopic
     */
    private String registerNewUser(String params, CorporateDirectives directives) {
        String parameters[] = params.split(",");
        String userName = parameters[0].substring(2, parameters[0].length()-1);
        String password = parameters[1].substring(2, parameters[1].length()-2);
        String emailAddress = parameters[2].substring(2, parameters[2].length()-2);
        String notifications = parameters[3].substring(2, parameters[3].length()-2);
        System.out.println(">>>> registerNewUser(" + userName + ", "+ password +")");
        // String mapId = params;
        StringBuffer messages = new StringBuffer("\"\"");
        StringBuffer result = new StringBuffer("{\"method\": \"doRegister\", ");
        result.append("\"result\": ");
        if (cm.getTopic(TOPICTYPE_USER, userName, 1) != null) {
            BaseTopic user = as.createLiveTopic(as.getNewTopicID(), TOPICTYPE_USER, userName, null);
            if (notifications.equals("on")) {
                as.setTopicProperty(user, PROPERTY_EMAIL_ADDRESS, emailAddress);
            } else {
                BaseTopic emailTopic = as.createLiveTopic(as.getNewTopicID(), TOPICTYPE_EMAIL_ADDRESS, emailAddress, null);
                BaseAssociation userToEmail = as.createLiveAssociation(as.getNewAssociationID(), ASSOCTYPE_ASSOCIATION, user.getID(), emailTopic.getID(), null, directives);
            }
            result.append("{ \"id\": \""+user.getID()+"\", \"name\": \""+user.getName()+"\", \"typeId\": \""+user.getType()+"\", " +
                    "\"email\": \""+as.getTopicProperty(user, PROPERTY_EMAIL_ADDRESS)+"\", " +
                    "\"website\": \""+as.getTopicProperty(user, "Website")+"\"}");
            messages = new StringBuffer("\"Your user registration was successfull.\"");
        } else {
            result.append("null");
            messages = new StringBuffer("\"Sorry, that username is already taken.\"");
            result.append(", \"error\": " + messages + "}");
            return result.toString();
        }
        result.append(", \"error\": " + messages + "}");
        return result.toString();
    }


    /**
     * Topic Id, PropertyName and PropertyValue (user credentials: UID and WPD)
     *
     * @param params
     * @param directives
     * @return
     */
    private String storeTopicProperty( String params, CorporateDirectives directives ) {
        System.out.println(">>>> storeTopicProperty(" + params +")");
        StringBuffer result = new StringBuffer("{\"method\": \"storeTopicProperty\", ");
        result.append("\"result\": ");
        StringBuffer messages = new StringBuffer("\"\"");
        // --- Start Parsing the Params
        String parameters[] = params.split(",");
        String topicId = parameters[0];
        String propertyName = parameters[1];
        topicId = topicId.substring(2, topicId.length()-1);
        propertyName = propertyName.substring(2, propertyName.length()-1);
        String propertyValue = parameters[2];
        System.out.println("    TopicId: "+ topicId + " pName: " + propertyName + " pVal: " + propertyValue);
        messages = new StringBuffer("\"Authorization failed while wanting to store that property.\"");
        result.append(messages);
        return result.toString();
    }

    /**
     * search for topic property name and returns a list with kind of searchResult objects as
     *
     * All parameters which are to be ignored must be explicitly set to a "null" string value
     *
     * The input controls is responsible for the doable search
     *
     * You can search in all Properties of all Topics by just providing a queryString
     * You can search throgh all Topics of all Types by just providing a queryString and a propertyName
     * You can search in all Topics of a specific type by providng a queryString, a propertyName and a typeId
     *
     * @param params queryString, typeId, propertyName
     * @param directives
     * @return
     */
    private String searchTopics(String params, CorporateDirectives directives) {
        System.out.println(">>>> searchTopics(" + params +")");
        StringBuffer result = new StringBuffer("{\"method\": \"searchTopics\", ");
        result.append("\"result\": ");
        StringBuffer messages = new StringBuffer("\"\"");
        // --- Start Parsing the Params
        String parameters[] = params.split(",");
        String query = parameters[0];
        String typeId = parameters[1];
        typeId = typeId.substring(2, typeId.length()-1);
        query = query.substring(2, query.length()-1);
        String property = parameters[2];
        property = property.substring(2, property.length()-2);
        // --- End of Parsing Parameters
        System.out.println("INFO: query for propValue " + query + " in " + "propName: " + property + " and type "+ typeId );
        // --- The most powerful "freestyle" search returns an hashtable, the other two a Vector,
        // --- All three types give bgack an enumeration
        Hashtable results = null;
        Vector typedResults = null;
        Enumeration keys;
        //
        Hashtable props = new Hashtable();
        if (typeId.equals("null") || typeId.equals("")) {
            results = cm.getTopicsByProperty(query); // getTopic(query, props, topicmapId, directives);
        } else if (property.equals("null") || property.equals("")) {
            System.out.println("> searching just in the Name Property of " + typeId);
            // default search for a specific type is set to Name and Description
            props.put(PROPERTY_NAME, query);
            // props.put(PROPERTY_DESCRIPTION, query);
            // cm.//
            typedResults = as. cm.getTopics(typeId, props, false);
            System.out.println("props: " + props.size());
        }else {
            // if given, then just in one property of a type
            System.out.println("> searching in the "+property +" of " + typeId);
            props.put(property, query);
            typedResults = cm.getTopics(typeId, props, false);
        }
        // either the hashtable is filled with results or the vector
        if (results != null) {
            keys = results.keys();
            System.out.println(">>>> found " + results.size() + " topics of type "+typeId+" with propValue " + query);
        } else  {
            keys = typedResults.elements();
            System.out.println(">>>> found " + typedResults.size() + " topics of type "+typeId+" with propValue " + query);
        }
        result.append("[");
        String propName = "";
        // String propName = topicIdAndProp[1];
        while (keys.hasMoreElements()) {
            BaseTopic topic;
            if (results == null) {
                topic = (BaseTopic) keys.nextElement();
                // getTopics byType and Property Vector is returned
            } else {
                String key = (String) keys.nextElement();
                String[] topicIdAndProp = key.split(":");
                propName = topicIdAndProp[1];
                topic = (BaseTopic) results.get(key);
            }
            // search result elements store additionaly the propertyName in which the query was found
            // thats just the case for full freestlye Search not for typed search
            result.append("{");
            result.append("\"propName\": \"" + propName + "\", ");
            // fill in result data // need to convert as.getTopicProperty(topic, propName)
            // to have the value containing the query diret in place without fetching further topicDetails
            String value = "";
            if (!propName.equals("")) {
                value = as.getTopicProperty(topic, propName);
                value = convertHTMLForJSON(value);
                value = removeControlChars(value);
            }
            //result.append("\"propValue\": "+value+", ");
            result.append("\"propValue\": \"\", ");
            result.append("\"topic\": ");
            System.out.println("    topic \""+ topic.getName() + "\" was found " +
                    "through property " + propName +", type is \"" + topic.getType()+ "\"");
            // BaseTopic topic = (BaseTopic) results.get(i);
            result.append(createSlimTopic(topic, messages));
            if (keys.hasMoreElements()) {
                result.append("},");
            } else {
                result.append("}");
            }
        }
        if (!keys.hasMoreElements()) result.append("]");
        result.append(", \"error\": " + messages + "}");
        return result.toString();
    }

    private String getTopicmapById(String params, Session session, CorporateDirectives directives) {
        System.out.println(">>>> getTopicmap(" + params + ")");
        // String mapId = params;
        String mapId = params.substring(2, params.length()-2);
        String mapName = as.getTopicProperty(mapId, 1, PROPERTY_NAME);
        // --- map translation ---
        String mapTranslation = as.getTopicProperty(mapId, 1, PROPERTY_TRANSLATION_USE);
        // --- background image ---
        String bgImage = as.getTopicProperty(mapId, 1, PROPERTY_BACKGROUND_IMAGE);
		// --- background color ---
		String bgColor = as.getTopicProperty(mapId, 1, PROPERTY_BACKGROUND_COLOR);
		// --- topicmap typeId
        String typeId = as.getTopicType(mapId, 1).getID();
        // --- Description ---
		String description = as.getTopicProperty(mapId, 1, PROPERTY_DESCRIPTION);
        description = convertHTMLForJSON(description);
        description = removeControlChars(description);
        if (bgColor.equals("")) {
			bgColor = DEFAULT_VIEW_BGCOLOR;
		}
        StringBuffer messages = new StringBuffer("\"\"");
        StringBuffer result = new StringBuffer("{\"method\": \"getTopicmap\", ");
        result.append("\"result\": ");
        StringBuffer mapBuffer = new StringBuffer("{ \"id\": \"" + mapId + "\",\"typeId\": \"" + typeId + "\", \"name\": \"" + mapName + "\", \"translation\": \"" + mapTranslation + "\",");
        mapBuffer.append("\"bgImage\": \"" + bgImage + "\", \"bgColor\": \"" + bgColor + "\", \"description\": \"" + description + "\", \"topics\": [");
        // Vector topicTypes = as.getRelatedTopics(workspaceId, ASSOCTYPE_USES, 2);
        Vector allTopics = cm.getViewTopics(mapId, 1);
        System.out.println(">>> found " + allTopics.size() +" topics for map with name " + mapName);
        //for(int t = 0; t < allTopics.size(); t++)
        for(int i = 0; i< allTopics.size(); i++) {
            PresentableTopic topic = (PresentableTopic) allTopics.get(i);
            // System.out.println("    e: " + topic.getName());
            String topicString  = createSlimPresentable(topic, messages);
            // System.out.println("geo: " + geo);
            mapBuffer.append(topicString);
            // mapTopics.append("}");
            if(allTopics.indexOf(topic) != allTopics.size() - 1) {
                mapBuffer.append(",");
            }
        }
        mapBuffer.append("], \"associations\": [");
        Vector allAssociations = cm.getViewAssociations(mapId, 1);
        System.out.println(">>> found " + allAssociations.size() +" assocs for map with name " + mapName);
        //for(int t = 0; t < allTopics.size(); t++)
        for(int i = 0; i< allAssociations.size(); i++) {
            BaseAssociation assoc = (BaseAssociation) allAssociations.get(i);
            // System.out.println("    e: " + topic.getName());
            String assocString  = createSlimAssociation(assoc, messages);
            // System.out.println("geo: " + geo);
            mapBuffer.append(assocString);
            // mapTopics.append("}");
            if(allAssociations.indexOf(assoc) != allAssociations.size() - 1) {
                mapBuffer.append(",");
            }
        }
        mapBuffer.append("]");
        mapBuffer.append("}");
        result.append(mapBuffer);
        result.append(", \"error\": " + messages + "}");
        return result.toString();
    }

    private String getViewsInUse(String params, Session session, CorporateDirectives directives) {
        System.out.println(">>>> getViewsInUse(" + params + ")");
        String userId = params.substring(2, params.length()-2);
        PresentableTopic personalWorkspace;
		try {
			String userID = userId;
			personalWorkspace = as.getWorkspaceTopicmap(userID);
			// error check
			if (personalWorkspace == null) {
				System.out.println("*** InteractionConnection.addPersonalWorkspace(): " +
					"user \"" + userID + "\" has no workspace");
				directives.add(DIRECTIVE_SHOW_MESSAGE, "user \"" + userID +
					"\" has no workspace", new Integer(NOTIFICATION_WARNING));
			}
		} catch (AmbiguousSemanticException e) {
			System.out.println("*** InteractionConnection.addPersonalWorkspace(): " + e);
			directives.add(DIRECTIVE_SHOW_MESSAGE, e.getMessage(), new Integer(NOTIFICATION_WARNING));
			personalWorkspace = (PresentableTopic) e.getDefaultTopic();
		}
        Vector personalViews = cm.getViewTopics(personalWorkspace.getID(), 1);
        System.out.println(">> Founde Personla Workspace for "+personalWorkspace.getName()+" with "+personalViews.size()+ "Views");
        StringBuffer messages = new StringBuffer("\"\"");
        StringBuffer result = new StringBuffer("{\"method\": \"getViewsInUse\", ");
        result.append("\"result\": [");
        Vector currentMaps = as.getViewsInUse(userId);
        for (int i = 0; i< currentMaps.size(); i++) {
            int homePosX = 150;
            int homePosY = 150;
            BaseTopic mapTopic = (BaseTopic) currentMaps.get(i);
            for (int k = 0; k < personalViews.size(); k++) {
                PresentableTopic viewTopic  = (PresentableTopic) personalViews.get(k);
                if (viewTopic.getID().equals(mapTopic.getID())) {
                    //System.out.println(">>> Touch PersonalizedMap("+viewTopic.getID()+") at X:" + viewTopic.getGeometry().x +" Y: "+ viewTopic.getGeometry().y);
                    homePosX = viewTopic.getGeometry().x;
                    homePosY = viewTopic.getGeometry().y;
                }
            }
            LiveTopic liveMap = as.getLiveTopic(mapTopic);
            String mapName = liveMap.getNameProperty();
            // --- map translation ---
            String mapTranslation = as.getTopicProperty(mapTopic, PROPERTY_TRANSLATION_USE);
            // --- background image ---
            String bgImage = as.getTopicProperty(mapTopic, PROPERTY_BACKGROUND_IMAGE);
            // --- preview image ---
            String mapPreviewImage = as.getTopicProperty(mapTopic, "Preview");
            // --- background color ---
            String bgColor = as.getTopicProperty(mapTopic, PROPERTY_BACKGROUND_COLOR);
            if (bgColor.equals("")) {
                bgColor = DEFAULT_VIEW_BGCOLOR;
            }
            StringBuffer mapBuffer = new StringBuffer("{ \"id\": \"" + mapTopic.getID() + "\", \"name\": \"" + mapTopic.getName() + "\", \"translation\": \"" + mapTranslation + "\",");
            mapBuffer.append("\"posX\": " + homePosX + ", \"posY\": " + homePosY + ", \"typeId\": \"" + mapTopic.getType() + "\", ");
            mapBuffer.append("\"bgImage\": \"" + bgImage + "\", \"bgColor\": \"" + bgColor + "\", \"mapPreviewImage\":  \"" + mapPreviewImage + "\"}");
            if(currentMaps.indexOf(mapTopic) != currentMaps.size() - 1) {
                mapBuffer.append(",");
            }
            result.append(mapBuffer);
        }
        result.append("]");
        // result.append("}");
        // result.append(mapBuffer);
        result.append(", \"error\": " + messages + "}");
        return result.toString();

    }


    /**
     * delivers all topics in a topicmap as "slim" geoobjects
     *
     * @param params (mapId, workspaceId)
     * @param session
     * @param directives
     * @return
     */
    private String getMapTopics(String params, Session session, CorporateDirectives directives)
    {
        System.out.println(">>>> getMapTopics(" + params + ")");
        // String mapId = params;
        String parameters[] = params.split(",");
        String mapId = parameters[0];
        String workspaceId = parameters[1];
        mapId = mapId.substring(2,mapId.length()-2);
        String mapName = as.getTopicProperty(mapId, 1, PROPERTY_NAME);
        workspaceId = workspaceId.substring(2, workspaceId.length()-2);
        // LOG System.out.println("INFO: mapId is " + mapId + ", " + " and workspaceId is " + workspaceId);
        StringBuffer messages = new StringBuffer("\"\"");
        StringBuffer result = new StringBuffer("{\"method\": \"getMapTopics\", ");
        result.append("\"result\": ");
        StringBuffer mapTopics = new StringBuffer("{ \"map\": \"" + mapName + "\", \"topics\": [");
        // 
        // Vector topicTypes = as.getRelatedTopics(workspaceId, ASSOCTYPE_USES, 2);
        Vector allTopics = cm.getViewTopics(mapId, 1);
        System.out.println(">>> found " + allTopics.size() +" for map with name " + mapName);
        //for(int t = 0; t < allTopics.size(); t++)
        for(int i = 0; i< allTopics.size(); i++) {
            BaseTopic topic = (BaseTopic) allTopics.get(i);
            // System.out.println("    e: " + topic.getName());
            String geo  = createSlimTopic(topic, messages);
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

    private String createSlimTopic(BaseTopic topic, StringBuffer messages)
    {
        StringBuffer object = new StringBuffer();
        //
        // LiveTopic live = as.getLiveTopic(topic);
        // int posX = Integer.parseInt(cm.getViewTopic(topic, "x"));
        // int posX = Integer.parseInt(cm.getViewTopic(topic, "x"));
        // int posX = Integer.parseInt("1");
        //int posX = Integer.parseInt(cm.getTopicData(topic.getID(), 1, "x"));
        //int posY = Integer.parseInt(cm.getTopicData(topic.getID(), 1, "y"));
        //
        // int posY = live.getPresentableTopic(topic, "").getGeometry().y;
        //if(posX == 0 && posY.equals(""))
        //{
        int posX = 0;
        int posY = 0;
        //}
        String name = topic.getName();
        if (hasQuotationMarks(name)) {
            name = convertHTMLForJSON(name);
        }
        object.append("{\"id\": \"" + topic.getID() + "\", ");
        object.append("\"name\": \"" + name + "\", ");
        object.append("\"posX\": " + posX + ", ");
        object.append("\"posY\": " + posY + ", ");
        object.append("\"typeId\": \"" + topic.getType() + "\", ");
        object.append("\"iconFile\": \"" + as.getIconfile(topic)  + "\"");
        // object.append("\"isLocked\": " + as.getLiveTopic(topic).getProperty(PROPERTY_LOCKED_GEOMETRY) + "");
        // System.out.println(">>> createCritCatList(" + topic.getID()+") ...");
        // object.append("\"criterias\": " + createTopicCategorizations(topic.getID(), criterias));
        object.append("}");
        return object.toString();
    }

    private String createSlimPresentable(PresentableTopic topic, StringBuffer messages)
    {
        StringBuffer object = new StringBuffer();
        //
        // LiveTopic live = as.getLiveTopic(topic);
        // int posX = Integer.parseInt(cm.getViewTopic(topic, "x"));
        // int posX = Integer.parseInt(cm.getViewTopic(topic, "x"));
        // int posX = Integer.parseInt("1");
        int posX = topic.getGeometry().x;
        int posY = topic.getGeometry().y;
        // int posY = live.getPresentableTopic(topic, "").getGeometry().y;
        //if(posX == 0 && posY.equals(""))
        //{
        //    posX = 0;
        //    posY = 0;
        //}
        String name = topic.getName();
        if (hasQuotationMarks(name)) {
            name = convertHTMLForJSON(name);
        }
        object.append("{\"id\": \"" + topic.getID() + "\", ");
        object.append("\"name\": \"" + name + "\", ");
        object.append("\"label\": \"" + topic.getLabel() + "\", ");
        object.append("\"posX\": " + posX + ", ");
        object.append("\"posY\": " + posY + ", ");
        object.append("\"typeId\": \"" + topic.getType() + "\", ");
        object.append("\"iconFile\": \"" + as.getIconfile(topic)  + "\"");
        // object.append("\"isLocked\": " + as.getLiveTopic(topic).getProperty(PROPERTY_LOCKED_GEOMETRY) + "");
        // System.out.println(">>> createCritCatList(" + topic.getID()+") ...");
        // object.append("\"criterias\": " + createTopicCategorizations(topic.getID(), criterias));
        object.append("}");
        return object.toString();
    }

    private String createSlimAssociation(BaseAssociation association, StringBuffer messages)
    {
        StringBuffer object = new StringBuffer();
        //
        String name = association.getName();
        if (hasQuotationMarks(name)) {
            name = convertHTMLForJSON(name);
        }
        object.append("{\"id\": \"" + association.getID() + "\", ");
        object.append("\"name\": \"" + name + "\", ");
        object.append("\"topic1\": \"" + association.getTopicID1() + "\", ");
        object.append("\"topic2\": \"" + association.getTopicID2() + "\", ");
        object.append("\"typeId\": \"" + association.getType() + "\" ");
        // object.append("\"description\": \"" + as.getAssocProperty(association, PROPERTY_DESCRIPTION) + "\"");
        // object.append("\"isLocked\": " + as.getLiveTopic(topic).getProperty(PROPERTY_LOCKED_GEOMETRY) + "");
        // System.out.println(">>> createCritCatList(" + topic.getID()+") ...");
        // object.append("\"criterias\": " + createTopicCategorizations(topic.getID(), criterias));
        object.append("}");
        return object.toString();
    }

    /**
     * Serializes a TopicBean into JSON
     *
     * @param topic
     * @param messages
     * @return
     */
    private String createBean(BaseTopic topic, StringBuffer messages) {
        StringBuffer bean = new StringBuffer();
        //
        TopicBean topicBean = as.createTopicBean(topic.getID(), 1);
        System.out.println(">>> created bean with "+topicBean.fields.size()+"fields");
        bean.append("{\"id\": \"" + topicBean.id + "\",");
        bean.append("\"name\": \"" + topicBean.name + "\",");
        bean.append("\"iconFile\": \"" + topicBean.icon + "\",");
        bean.append("\"properties\": [");
        Vector properties = topicBean.fields;
        for(int p = 0; p < properties.size(); p++)
        {
            TopicBeanField field = (TopicBeanField)properties.get(p);
            bean.append("{\"type\": " + field.type + ", ");
            bean.append("\"name\": \"" + field.name + "\", ");
            bean.append("\"label\": \"" + field.label + "\", ");
            bean.append("\"visualization\": \"" + field.visualMode + "\", ");
            if(field.type == TopicBeanField.TYPE_SINGLE) {
                String value = field.value;
                //if(hasQuotationMarks(value))
                    // preparing java string for json
                value = convertHTMLForJSON(value);
                value = removeControlChars(value);
                bean.append("\"value\":  \"" + value + "\"");
            } else {
                Vector relatedFields = field.values;
                bean.append("\"values\": [");
                for(int r = 0; r < relatedFields.size(); r++)
                {

                    BaseTopic relatedTopic = (BaseTopic)relatedFields.get(r);
                    bean.append("{");
                    bean.append("\"name\": \"" + relatedTopic.getName() + "\",");
                    bean.append("\"id\": \"" + relatedTopic.getID() + "\",");
                    // ### geoObject has it's own icon ?
                    bean.append("\"iconFile\": \"" + as.getTopicProperty(relatedTopic.getID(), 1, PROPERTY_ICON) + "\"}");
                    if (r == relatedFields.size()-1) {
                        bean.append("]");
                    } else {
                        bean.append(",");
                    }
                }
                if(relatedFields.size() == 0) bean.append("]");
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
     * returns a list of topictype descriptions in JSON for a list of TopicTypeIds
     *
     * @param Vector typeIds
     * @return
     */
    private String createListOfTypeDescriptions(Vector typeIds)
    {
        StringBuffer objectList = new StringBuffer();
        objectList.append("[");
        for(int i = 0; i < typeIds.size(); i++)
        {
            String typeId = (String) typeIds.get(i);
            objectList.append("{\"id\": \"" + typeId + "\", ");
            BaseTopic topicType = as.getLiveTopic(typeId, 1);
            // TopicTypeTopic topicType = as.getTopicType(typeId, 1);
            // System.out.println("The TopicType is: " + topicType.getID());
            objectList.append("\"name\": \"" + topicType.getName() + "\", ");
            objectList.append("\"pluralName\": \"" + as.getTopicProperty(topicType, PROPERTY_PLURAL_NAME) + "\", ");
            //objectList.append("\"description\": \"" + as.getTopicProperty(topicType, PROPERTY_DESCRIPTION) + "\", ");
            objectList.append("\"description\": \"\", ");
            // objectList.append("\"descriptionQuery\": \"" + as.getTopicProperty(topicType, PROPERTY_TYPE_DESCRIPTION_QUERY) + "\", ");
            objectList.append("\"descriptionQuery\": \"\", ");
            // objectList.append("\"accessPermission\": \""+as.getAssociation(typeId, typeId, i, typeId, LOG_MAPS, directives)+"\", ");
            objectList.append("\"bgColor\": \"" + as.getTopicProperty(topicType, PROPERTY_COLOR) + "\", ");
            objectList.append("\"textColor\": \"" + as.getTopicProperty(topicType, "Text Color") + "\", ");
            objectList.append("\"borderColor\": \"" + as.getTopicProperty(topicType, "Border Color") + "\", ");
            objectList.append("\"iconFile\": \"" + as.getTopicProperty(topicType, "Flash Symbol") + "\", ");
            String radius  = as.getTopicProperty(topicType, "Size");
            int lazyRadius = 10;
            if (!radius.equals("")) {
                lazyRadius = Integer.parseInt(radius);
            } else {
                // System.out.println(">> Set Representational Circle Radius of TopicType ("+topicType.getName()+") to 10px by default");
            }
            objectList.append("\"size\": \"" + lazyRadius + "\", ");
            String hiddenLabels = as.getTopicProperty(topicType, PROPERTY_HIDDEN_TOPIC_NAMES);
            if (hiddenLabels.equals("off")){
                objectList.append("\"hiddenLabels\": "+false+", ");
            } else { objectList.append("\"hiddenLabels\": "+true+", "); }
            String lockedPos = as.getTopicProperty(topicType, PROPERTY_LOCKED_GEOMETRY);
            if (lockedPos.equals("off")){
                objectList.append("\"lockedPos\": "+false+", ");
            } else { objectList.append("\"lockedPos\": "+true+", "); }
            // ??? Locked Position TopicType Wide ?
            objectList.append("\"commands\": [], ");
            objectList.append("\"properties\": [");
            //### key value
            try {
                Vector relatedProps = as.getRelatedTopics(typeId, ASSOCTYPE_COMPOSITION, TOPICTYPE_PROPERTY, 2);
                //System.out.println(topicType.getName() + " has " + relatedProps.size() + " direct related properties..");
                for (int j = 0; j < relatedProps.size(); j++) {
                    // through all found elements of TopicType Property
                    BaseTopic p = (BaseTopic) relatedProps.get(j);
                    //System.out.println("    Property is " + p.getName() + " and viusually " + as.getTopicProperty(p, PROPERTY_VISUALIZATION));
                    objectList.append("{");
                    objectList.append("\"name\": ");
                    objectList.append("\"" + p.getName() + "\", ");
                    objectList.append("\"visualization\": ");
                    objectList.append("\"" + as.getTopicProperty(p, PROPERTY_VISUALIZATION) + "\", ");
                    Vector relatedOpts = as.getRelatedTopics(p.getID(), ASSOCTYPE_COMPOSITION, 2);
                    // if (relatedOpts.size()) {System.out.println("    and has ");
                    objectList.append("\"options\": [");
                    for (int k = 0; k < relatedOpts.size(); k++) {
                        BaseTopic o = (BaseTopic) relatedOpts.get(k);
                        //System.out.println("        OptionsValue is " + o.getName() + " visualization: " + as.getTopicProperty(o, PROPERTY_ICON));
                        objectList.append("{ \"name\": \"" + o.getName() + "\", \"icon\": \""+as.getTopicProperty(p, PROPERTY_EDIT_PROPERTY_ICON)+"\"}");
                        if (k < relatedOpts.size() - 1) objectList.append(", ");
                    }
                    objectList.append("] ");
                    if(j == relatedProps.size() - 1)
                        objectList.append("}");
                        // end of type list
                    else
                        objectList.append("},");
                }
            } catch (DeepaMehtaException ex) {
                System.out.println("DeepaMehtaException: " + ex.getMessage());
            } catch (AmbiguousSemanticException aex) {
                System.out.println("AmbigiousSemanticException: " + aex.getMessage());
            }
            if(i == typeIds.size() - 1)
                objectList.append("]}");
                // end of type list
            else
                objectList.append("]},");
                // appending another type
        }
        objectList.append("]");
        // System.out.println("list is: " + objectList);
        return objectList.toString();
    }

    /**
     * returns a list of assocType descriptions in JSON for a list of TopicTypeIds
     *
     * @param Vector typeIds
     * @return
     */
    private String createListOfAssocTypeDescriptions(Vector typeIds)
    {
        StringBuffer objectList = new StringBuffer();
        objectList.append("[");
        for(int i = 0; i < typeIds.size(); i++)
        {
            String typeId = (String) typeIds.get(i);
            objectList.append("{\"id\": \"" + typeId + "\", ");
            BaseTopic topicType = as.getLiveTopic(typeId, 1);
            // TopicTypeTopic topicType = as.getTopicType(typeId, 1);
            // System.out.println("The TopicType is: " + topicType.getID());
            objectList.append("\"name\": \"" + topicType.getName() + "\", ");
            objectList.append("\"pluralName\": \"" + as.getTopicProperty(topicType, PROPERTY_PLURAL_NAME) + "\", ");
            //objectList.append("\"description\": \"" + as.getTopicProperty(topicType, PROPERTY_DESCRIPTION) + "\", ");
            objectList.append("\"description\": \"\", ");
            // objectList.append("\"descriptionQuery\": \"" + as.getTopicProperty(topicType, PROPERTY_TYPE_DESCRIPTION_QUERY) + "\", ");
            objectList.append("\"descriptionQuery\": \"\", ");
            // objectList.append("\"accessPermission\": \""+as.getAssociation(typeId, typeId, i, typeId, LOG_MAPS, directives)+"\", ");
            objectList.append("\"strokeColor\": \"" + as.getTopicProperty(topicType, "Border Color") + "\", ");
            objectList.append("\"bgColor\": \"" + as.getTopicProperty(topicType, PROPERTY_COLOR) + "\", ");
            objectList.append("\"commands\": [], "); // ### Serve Custom Logic
            objectList.append("\"properties\": [");
            //### key value
            try {
                Vector relatedProps = as.getRelatedTopics(typeId, ASSOCTYPE_COMPOSITION, TOPICTYPE_PROPERTY, 2);
                //System.out.println(topicType.getName() + " has " + relatedProps.size() + " direct related properties..");
                for (int j = 0; j < relatedProps.size(); j++) {
                    // through all found elements of TopicType Property
                    BaseTopic p = (BaseTopic) relatedProps.get(j);
                    //System.out.println("    Property is " + p.getName() + " and viusually " + as.getTopicProperty(p, PROPERTY_VISUALIZATION));
                    objectList.append("{");
                    objectList.append("\"name\":");
                    objectList.append("\"" + p.getName() + "\", ");
                    objectList.append("\"visualization\":");
                    objectList.append("\"" + as.getTopicProperty(p, PROPERTY_VISUALIZATION) + "\", ");
                    Vector relatedOpts = as.getRelatedTopics(p.getID(), ASSOCTYPE_COMPOSITION, 2);
                    // if (relatedOpts.size()) {System.out.println("    and has ");
                    objectList.append("\"options\": [");
                    for (int k = 0; k < relatedOpts.size(); k++) {
                        BaseTopic o = (BaseTopic) relatedOpts.get(k);
                        //System.out.println("        OptionsValue is " + o.getName() + " visualization: " + as.getTopicProperty(o, PROPERTY_ICON));
                        objectList.append("{ \"name\": \"" + o.getName() + "\", \"icon\": \""+as.getTopicProperty(p, PROPERTY_EDIT_PROPERTY_ICON)+"\"}");
                        if (k < relatedOpts.size() - 1) objectList.append(", ");
                    }
                    objectList.append("]");
                    if(j == relatedProps.size() - 1)
                        objectList.append("}");
                        // end of type list
                    else
                        objectList.append("},");
                }
            } catch (DeepaMehtaException ex) {
                System.out.println("DeepaMehtaException: " + ex.getMessage());
            } catch (AmbiguousSemanticException aex) {
                System.out.println("AmbigiousSemanticException: " + aex.getMessage());
            }
            if(i == typeIds.size() - 1)
                objectList.append("]}");
                // end of type list
            else
                objectList.append("]},");
                // appending another type
        }
        objectList.append("]");
        // System.out.println("list is: " + objectList);
        return objectList.toString();
    }

    //
    // --- atm. unused but later useful methods
    //


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
     * returns null if no topictype whihc is assigned to the given workspace, 
     * is a subtype of "GeoObjectTopic"
     * 
     * @param workspaceId
     * @return
     */
    private BaseTopic getWorkspaceSubType(String workspaceId, String superTypeId) {
        //
        TypeTopic geotype = as.type(superTypeId, 1);
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

    private boolean hasAnchorMarks(String value)
    {
        if (value.indexOf("<") != -1) {
            return true;
        } else if(value.indexOf(">") != -1) {
            return true;
        } 
        return false;
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
        // System.out.println("> HTML JSON INFO: Aus " + html.toString() + "mach");
        html = html.replaceAll("\"", "\\\\\"");
        html = html.replaceAll("<", "\\<");
        html = html.replaceAll(">", "\\>");
        // System.out.println("> mach \n " + html.toString());
        return html;
        
    }
}
