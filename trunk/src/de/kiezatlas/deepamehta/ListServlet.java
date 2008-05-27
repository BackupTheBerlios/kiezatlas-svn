package de.kiezatlas.deepamehta;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
//
import javax.servlet.ServletException;
//
import de.deepamehta.BaseTopic;
import de.deepamehta.DeepaMehtaConstants;
import de.deepamehta.service.Session;
import de.deepamehta.service.TopicBean;
import de.deepamehta.service.web.DeepaMehtaServlet;
import de.deepamehta.service.web.RequestParameter;
//
import de.kiezatlas.deepamehta.topics.CityMapTopic;
import de.kiezatlas.deepamehta.topics.GeoObjectTopic;



/**
 * Kiez-Atlas 1.5<br>
 * Requires DeepaMehta 2.0b8
 * <p>
 * Last functional change: 26.5.2008<br>
 * J&ouml;rg Richter<br>
 * jri@freenet.de
 */
public class ListServlet extends DeepaMehtaServlet implements KiezAtlas {

	protected String performAction(String action, RequestParameter params, Session session) throws ServletException {
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
			String geoID = params.getValue("id");
			setGeoObject(cm.getTopic(geoID, 1), session);
			return PAGE_GEO_ADMIN_FORM;
		} else if (action.equals(ACTION_UPDATE_GEO)) {
			GeoObjectTopic geo = getGeoObject(session);
			updateTopic(geo.getType(), params, session);
			String newFilename = EditServlet.writeImage(params.getUploads());
			if (newFilename != null) {
				as.setTopicProperty(geo.getImage(), PROPERTY_FILE, newFilename);
			}
			return PAGE_LIST;
		}
		//
		return super.performAction(action, params, session);
	}

	protected void preparePage(String page, RequestParameter params, Session session) {
		if (page.equals(PAGE_LIST_HOME)) {
			Vector workspaces = getWorkspaces(getUserID(session));
			Hashtable cityMaps = getCityMaps(workspaces);
			session.setAttribute("workspaces", workspaces);
			session.setAttribute("cityMaps", cityMaps);
		} else if (page.equals(PAGE_LIST)) {
			String cityMapID = getCityMap(session).getID();
			String instTypeID = getInstTypeID(session);
			Vector insts = cm.getTopicIDs(instTypeID, cityMapID, true);		// sortByTopicName=true
			session.setAttribute("topics", insts);
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

	private BaseTopic getCityMap(Session session) {
		return (BaseTopic) session.getAttribute("cityMap");
	}

	private String getInstTypeID(Session session) {
		return (String) session.getAttribute("instTypeID");
	}

	private GeoObjectTopic getGeoObject(Session session) {
		return (GeoObjectTopic) as.getLiveTopic((BaseTopic) session.getAttribute("geo"));
	}
}
