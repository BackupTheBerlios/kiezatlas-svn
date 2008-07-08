package de.kiezatlas.deepamehta;

import de.kiezatlas.deepamehta.topics.CityMapTopic;
import de.kiezatlas.deepamehta.topics.GeoObjectTopic;
//
import de.deepamehta.BaseTopic;
import de.deepamehta.service.CorporateDirectives;
import de.deepamehta.service.Session;
import de.deepamehta.service.web.DeepaMehtaServlet;
import de.deepamehta.service.web.Notification;
import de.deepamehta.service.web.RequestParameter;
import de.deepamehta.util.DeepaMehtaUtils;
//
import javax.servlet.ServletException;
//
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;



/**
 * Kiezatlas 1.6<br>
 * Requires DeepaMehta 2.0b8
 * <p>
 * Last change: 8.7.2008<br>
 * J&ouml;rg Richter<br>
 * jri@deepamehta.de
 */
public class ListServlet extends DeepaMehtaServlet implements KiezAtlas {

	protected String performAction(String action, RequestParameter params, Session session, CorporateDirectives directives)
																									throws ServletException {
		if (action == null) {
			return PAGE_LIST_LOGIN;
		} else if (action.equals(ACTION_TRY_LOGIN)) {
			String username = params.getValue("username");
			String password = params.getValue("password");
			if (as.loginCheck(username, password)) {
				setUser(cm.getTopic(TOPICTYPE_USER, username, 1), session);
				return PAGE_LIST_HOME;
			} else {
				return PAGE_LIST_LOGIN;
			}
		} else if (action.equals(ACTION_SHOW_INSTITUTIONS)) {
			BaseTopic cityMap = cm.getTopic(params.getValue("cityMapID"), 1);
			String instTypeID = ((CityMapTopic) as.getLiveTopic(cityMap)).getInstitutionType().getID();
			setCityMap(cityMap, session);
			setInstTypeID(instTypeID, session);
			return PAGE_LIST;
		} else if (action.equals(ACTION_SHOW_GEO_FORM)) {
			String geoObjectID = params.getValue("id");
			setGeoObject(cm.getTopic(geoObjectID, 1), session);
			return PAGE_GEO_ADMIN_FORM;
		} else if (action.equals(ACTION_UPDATE_GEO)) {
			GeoObjectTopic geo = getGeoObject(session);
			CityMapTopic cityMap = getCityMap(session);
			// --- notification ---
			// Note: the check for warnings is performed before the form input is processed
			// because the processing (updateTopic()) eat the parameters up.
			checkForWarnings(params, session, directives);
			// --- update geo object ---
			// Note: the timestamp is updated through geo object's propertiesChanged() hook
			updateTopic(geo.getType(), params, session, directives, cityMap.getID(), VIEWMODE_USE);
			// --- store image ---
			EditServlet.writeImage(params.getUploads(), geo.getImage(), PROPERTY_FILE, as);
			//
			return PAGE_LIST;
		} else if (action.equals(ACTION_SHOW_EMPTY_GEO_FORM)) {
			return PAGE_GEO_EMPTY_FORM;
		} else if (action.equals(ACTION_CREATE_GEO)) {
			String geoObjectID = as.getNewTopicID();
			CityMapTopic cityMap = getCityMap(session);
			// --- notification ---
			// Note: the check for warnings is performed before the form input is processed
			// because the processing (createTopic()) eat the parameters up.
			checkForWarnings(params, session, directives);
			// --- place in city map ---
			// Note: the geo object is placed in city map before it is actually created.
			// This way YADE-based autopositioning can perform through geo object's propertiesChanged() hook.
			cm.createViewTopic(cityMap.getID(), 1, VIEWMODE_USE, geoObjectID, 1, 0, 0, false);	// performExistenceCheck=false
			// --- create geo object ---
			// Note: timestamp, password, and geometry-lock are initialized through geo object's evoke() hook
			createTopic(getInstTypeID(session), params, session, directives, cityMap.getID(), geoObjectID);
			// --- get geo object ---
			setGeoObject(cm.getTopic(geoObjectID, 1), session);
			GeoObjectTopic geo = getGeoObject(session);
			// --- store image ---
			EditServlet.writeImage(params.getUploads(), geo.getImage(), PROPERTY_FILE, as);
			//
			return PAGE_LIST;
		} else if (action.equals(ACTION_GO_HOME)) {
			return PAGE_LIST_HOME;
		}
		//
		return super.performAction(action, params, session, directives);
	}

	protected void preparePage(String page, RequestParameter params, Session session, CorporateDirectives directives) {
		if (page.equals(PAGE_LIST_HOME)) {
			Vector workspaces = getWorkspaces(getUserID(session));
			Hashtable cityMaps = getCityMaps(workspaces);
			session.setAttribute("workspaces", workspaces);
			session.setAttribute("cityMaps", cityMaps);
		} else if (page.equals(PAGE_LIST)) {
			// geo objects
			String cityMapID = getCityMap(session).getID();
			String instTypeID = getInstTypeID(session);
			Vector insts = cm.getTopicIDs(instTypeID, cityMapID, true);		// sortByTopicName=true
			session.setAttribute("topics", insts);
			// notifications
			session.setAttribute("notifications", directives.getNotifications());
		}
	}



	// **********************
	// *** Custom Methods ***
	// **********************



	private Vector getWorkspaces(String userID) {
		Vector workspaces = new Vector();
		//
		Vector ws = as.getRelatedTopics(userID, SEMANTIC_MEMBERSHIP, TOPICTYPE_WORKSPACE, 2);
		Enumeration e = ws.elements();
		while (e.hasMoreElements()) {
			BaseTopic w = (BaseTopic) e.nextElement();
			if (isKiezatlasWorkspace(w.getID())) {
				workspaces.addElement(w);
			}
		}
		//
		return workspaces;
	}
	
	private boolean isKiezatlasWorkspace(String workspaceID) {
		if (workspaceID.equals(WORKSPACE_KIEZATLAS)) {
			return true;
		}
		//
		Vector assocTypes = new Vector();
		assocTypes.addElement(SEMANTIC_SUB_WORKSPACE);
		return cm.associationExists(WORKSPACE_KIEZATLAS, workspaceID, assocTypes);
	}

	// ---

	private Hashtable getCityMaps(Vector workspaces) {
		Hashtable cityMaps = new Hashtable();
		//
		Enumeration e = workspaces.elements();
		while (e.hasMoreElements()) {
			String workspaceID = ((BaseTopic) e.nextElement()).getID();
			BaseTopic topicmap = as.getWorkspaceTopicmap(workspaceID);
			Vector maps = cm.getTopics(TOPICTYPE_CITYMAP, new Hashtable(), topicmap.getID());
			cityMaps.put(workspaceID, maps);
		}
		//
		return cityMaps;
	}

	private void checkForWarnings(RequestParameter params, Session session, CorporateDirectives directives) {
		CityMapTopic cityMap = getCityMap(session);
		String geoName = params.getValue(PROPERTY_NAME);
		// warning if YADE is "off"
		if (!cityMap.isYADEOn()) {
			directives.add(DIRECTIVE_SHOW_MESSAGE, "\"" + geoName + "\" wurde hilfsweise in die Ecke oben/links positioniert " +
				"(Stadtplan \"" + cityMap.getName() + "\" hat keine YADE-Referenzpunkte)", new Integer(NOTIFICATION_WARNING));
		} else if (params.getValue(PROPERTY_YADE_X).equals("") && params.getValue(PROPERTY_YADE_Y).equals("")) {
			directives.add(DIRECTIVE_SHOW_MESSAGE, "\"" + geoName + "\" wurde hilfsweise in die Ecke oben/links positioniert " +
				"-- Bitte YADE-Koordinaten angeben", new Integer(NOTIFICATION_WARNING));
		}
	}



	// *************************
	// *** Session Utilities ***
	// *************************



	// --- Methods to maintain data in the session

	private void setUser(BaseTopic user, Session session) {
		session.setAttribute("user", user);
		System.out.println("> \"user\" stored in session: " + user);
	}

	private void setCityMap(BaseTopic cityMap, Session session) {
		session.setAttribute("cityMap", cityMap);
		System.out.println("> \"cityMap\" stored in session: " + cityMap);
	}

	private void setInstTypeID(String instTypeID, Session session) {
		session.setAttribute("instTypeID", instTypeID);
		System.out.println("> \"instTypeID\" stored in session: " + instTypeID);
	}

	private void setGeoObject(BaseTopic geo, Session session) {
		session.setAttribute("geo", geo);
		System.out.println("> \"geo\" stored in session: " + geo);
	}

	// ---

	private CityMapTopic getCityMap(Session session) {
		return (CityMapTopic) as.getLiveTopic((BaseTopic) session.getAttribute("cityMap"));
	}

	private String getInstTypeID(Session session) {
		return (String) session.getAttribute("instTypeID");
	}

	private GeoObjectTopic getGeoObject(Session session) {
		return (GeoObjectTopic) as.getLiveTopic((BaseTopic) session.getAttribute("geo"));
	}
}
