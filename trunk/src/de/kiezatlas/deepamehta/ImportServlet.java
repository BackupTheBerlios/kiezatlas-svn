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
import java.util.Vector;


/**
 * Kiezatlas 1.6.5<br>
 * Requires DeepaMehta rev. 369
 * <p>
 * Last change: 06.12.2009<br>
 * J&ouml;rg Richter / Malte Rei&szlig;ig<br>
 * jri@deepamehta.de / mre@deepamehta.de
 */
public class ImportServlet extends DeepaMehtaServlet implements KiezAtlas {

  private ImportWorker ehrenamtWorker = null;
  private ImportWorkerEvent eventWorker = null;

  public static final long UPDATE_INTERVAL = 86000000; // approximately 24 hours
  public static final long UPDATE_INTERVAL_TESTING = 3600000; // approximately 60 mins
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
  public static final String EVENTMENT_WORKSPACE = "t-453282";
  public static final String EVENTMAP_TO_PUBLISH = "t-453286";
  //
  public static final String TOPICTYPE_EVT_EVENT = "t-453276";
  public static final String TOPICTYPE_EVT_BEZIRK = "t-453278";
  public static final String TOPICTYPE_EVT_KATEGORIE = "t-453280";
  //
  public static final String PROPERTY_PROJECT_ORIGIN_ID = "OriginId";
  public static final String PROPERTY_PROJECT_LAST_MODIFIED = "Timestamp";
  public static final String PROPERTY_PROJECT_ORGANISATION = "Organisation";
  public static final String PROPERTY_EVENT_DESCRIPTION = "Beschreibung";
  public static final String PROPERTY_EVENT_TIME = "Datum / Zeit";

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
                session.setAttribute("timingIntervalTwo", ""+UPDATE_INTERVAL/1000/60);
                if (ehrenamtWorker != null) {
                    session.setAttribute("workerThread", ehrenamtWorker.getState().toString());
                    session.setAttribute("workerThreadTime", ehrenamtWorker.getKickOffTime());
                } else {
                    session.setAttribute("workerThread", "inactive");
                }
                if (eventWorker != null) {
                    session.setAttribute("workerThreadTwo", eventWorker.getState().toString());
                    session.setAttribute("workerThreadTimeTwo", eventWorker.getKickOffTime());
                } else {
                    session.setAttribute("workerThreadTwo", "inactive");
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
            session.setAttribute("workerThread", ehrenamtWorker.getState().toString());
            
            return PAGE_IMPORTS_HOME;
        } else if (action.equals(ACTION_RESET_CRITERIAS)) {
            // 4 lines of security check
            Vector workspaces = (Vector) session.getAttribute("importWorkspaces");
            BaseTopic workspace = (BaseTopic) workspaces.get(0);
            if (session == null || workspace == null) {
                return PAGE_IMPORTS_LOGIN;
            }
            String workspaceId = params.getValue("workspaceId");
            //
            if (workspaceId.equals(ENGAGEMENT_WORKSPACE)) {
                ehrenamtWorker.resetCriteriaFlag();
            } else if (workspaceId.equals(EVENTMENT_WORKSPACE)) {
                eventWorker.resetCriteriaFlag();
            }
            return PAGE_IMPORTS_HOME;
        } else if (action.equals(ACTION_SHOW_REPORT)) {
            //
            return PAGE_IMPORTS_HOME;
        } else if (action.equals(ACTION_DO_IMPORT)) {
            // 4 lines of security check
            Vector workspaces = (Vector) session.getAttribute("importWorkspaces");
            BaseTopic workspace = (BaseTopic) workspaces.get(0);
            if (session == null || workspace == null) {
                return PAGE_IMPORTS_LOGIN;
            }
            String workspaceId = params.getValue("workspaceId");
            //
            if (workspaceId.equals(ENGAGEMENT_WORKSPACE)) {
                if (ehrenamtWorker == null) {
                  ehrenamtWorker = new ImportWorker(as, cm, ENGAGEMENT_WORKSPACE, UPDATE_INTERVAL, directives, "http://buerger-aktiv.index.de/kiezatlas/");
                  ehrenamtWorker.setDaemon(true);
                }
                ehrenamtWorker.run();
           } else if (workspaceId.equals(EVENTMENT_WORKSPACE)) {
                if (eventWorker == null) {
                  eventWorker = new ImportWorkerEvent(as, cm, EVENTMENT_WORKSPACE, UPDATE_INTERVAL, directives, "http://www.berlin.de/land/kalender/export_kiezatlas.php");
                  eventWorker.setDaemon(true);
                }
                eventWorker.run();
            }
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
            BaseTopic workspaceEngagement = as.getLiveTopic(ENGAGEMENT_WORKSPACE, 1);
            BaseTopic workspaceEvent = as.getLiveTopic(EVENTMENT_WORKSPACE, 1);
            Vector workspaces = new Vector();
            workspaces.add(workspaceEngagement);
            workspaces.add(workspaceEvent);
            //
            Vector geoProjectObjects = getGeoObjectInformation(ENGAGEMENT_WORKSPACE);
            Vector geoEventObjects = getGeoObjectInformation(EVENTMENT_WORKSPACE);
            session.setAttribute("importWorkspaces", workspaces);
            session.setAttribute("geoProjectObjects", geoProjectObjects);
            session.setAttribute("geoEventObjects", geoEventObjects);
		} else if (page.equals(PAGE_REPORT_HOME)) {
            session.setAttribute("report", null);
            // 
            session.setAttribute("notifications", directives.getNotifications());
		}
	}

    
    public void destroy() {
        // worker.done();
        ehrenamtWorker.setThreadDead();
        eventWorker.setThreadDead();
        // call stop method, too
        ehrenamtWorker.done();
        eventWorker.done();
        System.out.println("---- WorkerThread destroyed (" + ehrenamtWorker.getClass() + ") ---");
        System.out.println("---- WorkerThread destroyed (" + eventWorker.getClass() + ") ---");
        ehrenamtWorker = null;
        eventWorker = null;
        System.out.println("--- DeepaMehtaServlet destroyed (" + getClass() + ") ---");
		as.shutdown();
	}

	// **********************
	// *** Custom Methods ***
	// **********************


    private Vector getUnlocatableGeoObjects() {
        Vector allTopics = cm.getTopics(TOPICTYPE_ENG_PROJECT);
        allTopics.addAll(cm.getTopics(TOPICTYPE_EVT_EVENT));
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
