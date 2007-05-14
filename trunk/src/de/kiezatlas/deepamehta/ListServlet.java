package de.kiezatlas.deepamehta;

import de.kiezatlas.deepamehta.topics.CityMapTopic;
//
import de.deepamehta.BaseTopic;
import de.deepamehta.DeepaMehtaException;
import de.deepamehta.service.Session;
import de.deepamehta.service.web.DeepaMehtaServlet;
import de.deepamehta.service.web.RequestParameter;
//
import javax.servlet.ServletException;
import java.util.*;



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
			session.setAttribute("critName", criterias[0].criteria.getPluralName());
			session.setAttribute("insts", createInstitutionBeans(insts, criterias));
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

	private Vector createInstitutionBeans(Vector instIDs, SearchCriteria[] criterias) {
		Vector insts = new Vector();
		//
		Enumeration e = instIDs.elements();
		while (e.hasMoreElements()) {
			String instID = (String) e.nextElement();
			insts.addElement(new Institution(instID, criterias, as));
		}
		//
		return insts;
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
