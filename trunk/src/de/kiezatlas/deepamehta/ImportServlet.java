package de.kiezatlas.deepamehta;

import de.kiezatlas.deepamehta.topics.GeoObjectTopic;
//
import de.deepamehta.BaseTopic;
import de.deepamehta.service.CorporateDirectives;
import de.deepamehta.service.Session;
import de.deepamehta.service.web.DeepaMehtaServlet;
import de.deepamehta.service.web.RequestParameter;
import de.deepamehta.topics.TypeTopic;
import javax.servlet.ServletException;
//
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;



/**
 * Kiezatlas 1.6.2<br>
 * Requires DeepaMehta rev. 369
 * <p>
 * Last change: 06.12.2009<br>
 * J&ouml;rg Richter / Malte Rei&szlig;ig<br>
 * jri@deepamehta.de / mre@deepamehta.de
 */
public class ImportServlet extends DeepaMehtaServlet implements KiezAtlas {

    private ImportWorker worker = null;

    public static final long UPDATE_INTERVAL = 86000000; // approximately 24 hours
    // public static final long UPDATE_INTERVAL = 600000; // approximately 10 mines
    // 
    public static final String ENGAGEMENT_WORKSPACE = "t-331306";
    public static final String CITYMAP_TO_PUBLISH = "t-331302";
    //
    public static final String TOPICTYPE_ENG_PROJECT = "t-331314";
    public static final String TOPICTYPE_ENG_ZIELGRUPPE = "t-331319";
    public static final String TOPICTYPE_ENG_TAETIGKEIT = "t-331323";
    public static final String TOPICTYPE_ENG_EINSATZBEREICH = "t-331321";
    public static final String TOPICTYPE_ENG_MERKMAL = "t-331325";
    public static final String TOPICTYPE_ENG_BEZIRK = "t-331327";
    //
    public static final String PROPERTY_PROJECT_ORIGIN_ID = "OriginId";
    public static final String PROPERTY_PROJECT_LAST_MODIFIED = "Timestamp";
    public static final String PROPERTY_PROJECT_ORGANISATION = "Organisation";

	protected String performAction(String action, RequestParameter params, Session session, CorporateDirectives directives)
																									throws ServletException {
        if (action == null) {
            return PAGE_IMPORTS_LOGIN;
        } else if (action.equals(ACTION_TRY_LOGIN)) {
            String username = params.getValue("username");
            String password = params.getValue("password");
            if (as.loginCheck(username, password)) {
                BaseTopic user = cm.getTopic(TOPICTYPE_USER, username, 1);
                if (!user.getName().equals("root")) {
                    return PAGE_IMPORTS_LOGIN;
                }
                setUser(user, session);
                session.setAttribute("timingInterval", ""+UPDATE_INTERVAL/1000/60);
                if (worker != null) {
                    session.setAttribute("workerThread", worker.getState().toString());
                    session.setAttribute("workerThreadTime", worker.getKickOffTime());
                } else {
                    session.setAttribute("workerThread", "inactive");
                }
                Vector unusables = getUnlocatableGeoObjects();
                //
                session.setAttribute("unusableGeoObjects", unusables);
                return PAGE_IMPORTS_HOME;
            } else {
                return PAGE_IMPORTS_LOGIN;
            }
        } else if (action.equals(ACTION_SHOW_IMPORTS)) {
            if (session.getAttribute("membership") == null) {
                session.setAttribute("membership", "Affiliated");
            }
            session.setAttribute("workerThread", worker.getState().toString());
            
            return PAGE_IMPORTS_HOME;
        } else if (action.equals(ACTION_RESET_CRITERIAS)) {
            //
            Vector taetigkeiten = cm.getTopics(TOPICTYPE_ENG_TAETIGKEIT);
            Vector einsatzbereiche = cm.getTopics(TOPICTYPE_ENG_EINSATZBEREICH);
            Vector merkmale = cm.getTopics(TOPICTYPE_ENG_MERKMAL);
            Vector zielgruppen = cm.getTopics(TOPICTYPE_ENG_ZIELGRUPPE);
            Vector bezirke = cm.getTopics(TOPICTYPE_ENG_BEZIRK);
            bezirke.addAll(taetigkeiten);
            bezirke.addAll(einsatzbereiche);
            bezirke.addAll(merkmale);
            bezirke.addAll(zielgruppen);
            //
            for (int i = 0; i < bezirke.size(); i++) {
                BaseTopic category = (BaseTopic) bezirke.get(i);
                CorporateDirectives newDirectives = as.deleteTopic(category.getID(), 1);
                newDirectives.updateCorporateMemory(as, session, null, null);
            }
            return PAGE_IMPORTS_HOME;
        } else if (action.equals(ACTION_SHOW_REPORT)) {
            //
            return PAGE_IMPORTS_HOME;
        } else if (action.equals(ACTION_DO_IMPORT)) {
            BaseTopic workspace = (BaseTopic) session.getAttribute("importWorkspaces");
            if (session == null || workspace == null) {
                return PAGE_IMPORTS_LOGIN;
            }
            if (worker == null) {
                worker = new ImportWorker(as, cm, ENGAGEMENT_WORKSPACE, UPDATE_INTERVAL, directives);
                worker.setDaemon(true);
            }
            worker.run();
            //
            return PAGE_IMPORTS_HOME;
        }
		//
		return super.performAction(action, params, session, directives);
	}

	protected void preparePage(String page, RequestParameter params, Session session, CorporateDirectives directives) {
		if (page.equals(PAGE_IMPORTS_HOME)) {
            // next line: membership preferences are set according to workspaces
            // Vector workspaces = getWorkspaces(getUserID(session), session);
            //String workspaceId = ((BaseTopic)workspaces.get(0)).getID(); // take the first best
            BaseTopic workspace = as.getLiveTopic(ENGAGEMENT_WORKSPACE, 1);
            Vector criterias = getKiezCriteriaTypes(ENGAGEMENT_WORKSPACE);
            Hashtable critWithNumbers = new Hashtable(criterias.size());
            for (int i = 0; i < criterias.size(); i++) {
                BaseTopic topic = (BaseTopic) criterias.get(i);
                Vector instancesOfTopic = cm.getTopics(topic.getID());
                critWithNumbers.put(topic.getName(), instancesOfTopic.size());
            }
            Vector geoObjects = getGeoObjectInformation(ENGAGEMENT_WORKSPACE);
			session.setAttribute("importWorkspaces", workspace);
            session.setAttribute("importCriterias", critWithNumbers);
            session.setAttribute("geoObjects", geoObjects);
		} else if (page.equals(PAGE_REPORT_HOME)) {
            session.setAttribute("report", null);
            // 
            session.setAttribute("notifications", directives.getNotifications());
		}
	}

    @Override
    public void destroy() {
        // worker.done();
        worker.setThreadDead();
        // call stop method, too
        worker.done();
        System.out.println("---- WorkerThread destroyed (" + worker.getClass() + ") ---");
        worker = null;
        System.out.println("--- DeepaMehtaServlet destroyed (" + getClass() + ") ---");
		as.shutdown();
	}

	// **********************
	// *** Custom Methods ***
	// **********************


    private Vector getUnlocatableGeoObjects() {
        Vector allTopics = cm.getTopics(TOPICTYPE_ENG_PROJECT);
        Vector unusableTopics = new Vector();
        for (int i = 0; i < allTopics.size(); i++) {
            BaseTopic geoTopic = (BaseTopic) allTopics.get(i);
            GeoObjectTopic geoObject = (GeoObjectTopic) as.getLiveTopic(geoTopic);
            // validate stored topic
            String lat = as.getTopicProperty(geoObject, PROPERTY_GPS_LAT);
            String lon = as.getTopicProperty(geoObject, PROPERTY_GPS_LONG);
            if (lat.equals("") || lon.equals("")) {
                unusableTopics.add(geoObject);
            }
            BaseTopic addressTopic = geoObject.getAddress();
            if (addressTopic == null) {
                unusableTopics.add(geoObject);
            } else {
                String street = as.getTopicProperty(addressTopic, PROPERTY_STREET);
                if (street.equals("über Gute-Tat.de")) {
                    // unlogically google`s geocoder provides wgs84 coordinates for this !webaddress
                    unusableTopics.add(geoObject);
                }
            }
        }
        return unusableTopics;
    }
    
    private Vector getGeoObjectInformation(String workspaceId) {
        BaseTopic geoType = getWorkspaceGeoType(workspaceId);
        if (geoType == null) {
            System.out.println(">> Workspace ("+workspaceId+") is not configured properly");
            return new Vector();
        }
        return cm.getTopics(geoType.getID());
    }
	
	private Vector getWorkspaces(String userID, Session session) {
		Vector workspaces = new Vector();
		//
        session.setAttribute("membership", "");
        Vector ws = as.getRelatedTopics(userID, SEMANTIC_MEMBERSHIP, TOPICTYPE_WORKSPACE, 2);
        Enumeration e = ws.elements();
        if (!e.hasMoreElements()) {
            Vector aws = as.getRelatedTopics(userID, SEMANTIC_AFFILIATED_MEMBERSHIP, TOPICTYPE_WORKSPACE, 2);
            session.setAttribute("membership", "Affiliated");
            e = aws.elements();
        }
		while (e.hasMoreElements()) {
			BaseTopic w = (BaseTopic) e.nextElement();
			//if (isKiezatlasWorkspace(w.getID())) {
			workspaces.addElement(w);
			//}
		}
		//
		return workspaces;
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
        TypeTopic critType = as.type(TOPICTYPE_CRITERIA, 1);
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


	// *************************
	// *** Session Utilities ***
	// *************************



	// --- Methods to maintain data in the session


	private void setUser(BaseTopic user, Session session) {
		session.setAttribute("user", user);
		System.out.println("> \"user\" stored in session: " + user);
	}

}
