package de.kiezatlas.deepamehta;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.ServletException;

import de.deepamehta.BaseTopic;
import de.deepamehta.DeepaMehtaConstants;
import de.deepamehta.service.Session;
import de.deepamehta.service.TopicBean;
import de.deepamehta.service.web.DeepaMehtaServlet;
import de.deepamehta.service.web.RequestParameter;
import de.kiezatlas.deepamehta.topics.CityMapTopic;



/**
 * Kiez-Atlas 1.4.1.<br>
 * Requires DeepaMehta 2.0b7-post1.
 * <p>
 * Last functional change: 17.3.2007<br>
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
			String cityMapID = params.getValue("cityMapID");
			CityMapTopic cityMap = (CityMapTopic) as.getLiveTopic(cityMapID, 1);
			String instTypeID = cityMap.getInstitutionType().getID();
			Vector insts = cm.getTopicIDs(instTypeID, cityMapID, true);		// sortByTopicName=true
			SearchCriteria[] criterias = cityMap.getSearchCriterias();
			session.setAttribute("mapName", cityMap.getName());
			session.setAttribute("topics", createBaseTopicBeans(insts, criterias, instTypeID));
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

	private Vector createBaseTopicBeans(Vector instIDs, SearchCriteria[] criterias, String instTypeID) {
		Vector insts = new Vector();
		//
		Enumeration e = instIDs.elements();
		while (e.hasMoreElements()) {
			String instID = (String) e.nextElement();
			GeoObject geo = new GeoObject(instID, criterias, as);
			insts.addElement(new BaseTopic(instID, 1, instTypeID, 1, geo.name));
		}
		//
		return insts;
	}
	
	private Vector createTopicBeans(Vector topicIDs) {
		Vector topics = new Vector();
		//
		Enumeration e = topicIDs.elements();
		while (e.hasMoreElements()) {
			String instID = (String) e.nextElement();
			TopicBean topicBean = as.createTopicBean(instID, 1);
			topics.addElement(topicBean);
		}
		//
		return topics;
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
