package de.kiezatlas.deepamehta.topics;

import de.kiezatlas.deepamehta.KiezAtlas;
import de.kiezatlas.deepamehta.SearchCriteria;
//
import de.deepamehta.BaseTopic;
import de.deepamehta.PresentableTopic;
import de.deepamehta.DeepaMehtaConstants;
import de.deepamehta.DeepaMehtaException;
import de.deepamehta.service.ApplicationService;
import de.deepamehta.service.CorporateDirectives;
import de.deepamehta.service.CorporateCommands;
import de.deepamehta.service.Session;
import de.deepamehta.topics.TopicMapTopic;
import de.deepamehta.topics.TypeTopic;
//
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.*;



/**
 * Kiez-Atlas 1.5.1<br>
 * Requires DeepaMehta 2.0b8
 * <p>
 * Last change: 29.1.2008<br>
 * J&ouml;rg Richter<br>
 * jri@freenet.de
 */
public class CityMapTopic extends TopicMapTopic implements KiezAtlas {



	static final String VERSION = "1.5.1";
	static {
		System.out.println(">>> Kiez-Atlas " + VERSION);
	}



	// *******************
	// *** Constructor ***
	// *******************



	public CityMapTopic(BaseTopic topic, ApplicationService as) {
		super(topic, as);
	}



	// **********************
	// *** Defining Hooks ***
	// **********************



	// --------------------------
	// --- Providing Commands ---
	// --------------------------



	/**
	 * @see		de.deepamehta.service.ApplicationService#showViewMenu
	 */
	public CorporateCommands viewCommands(String topicmapID, String viewmode,
								Session session, CorporateDirectives directives) {
		CorporateCommands commands = new CorporateCommands(as);
		//
		int editorContext = as.editorContext(topicmapID);
		if (editorContext == EDITOR_CONTEXT_PERSONAL) {
			commands.addWorkspaceTopicTypeCommands(session, directives);
			commands.addImportCommand(session);
		} else if (editorContext == EDITOR_CONTEXT_VIEW) {
			commands.addSearchByTopictypeCommand(viewmode, session, directives);
			commands.addSeparator();
			commands.addCreateCommands(viewmode, session, directives);
			commands.addSeparator();
			//
			commands.addCloseCommand(session);
			commands.addHideAllCommands(topicmapID, viewmode, session);
			commands.addDeleteTopicmapCommand(session);
			commands.addSeparator();
			//
			commands.addPublishCommand(getID(), session, directives);
			commands.addSeparator();
			//
			commands.addExportCommand(session, directives);
			commands.addSeparator();
			//
			// additional command: "Reposition all"
			commands.addCommand(ITEM_REPOSITION_ALL, CMD_REPOSITION_ALL, FILESERVER_ICONS_PATH, ICON_REPOSITION_ALL);
			commands.addSeparator();
			//
			commands.addHelpCommand(this, session);
		} else if (editorContext == EDITOR_CONTEXT_WORKGROUP) {
		} else {
			throw new DeepaMehtaException(this + " has unexpected editor context: " + editorContext);
		}
		//
		return commands;
	}



	// --------------------------
	// --- Executing Commands ---
	// --------------------------



	public CorporateDirectives executeCommand(String command, Session session, String topicmapID, String viewmode) {
		CorporateDirectives directives = new CorporateDirectives();
		//
		StringTokenizer st = new StringTokenizer(command, COMMAND_SEPARATOR);
		String cmd = st.nextToken();
		if (cmd.equals(CMD_REPOSITION_ALL)) {
			repositionAllInstitutions(directives);
		} else {
			return super.executeCommand(command, session, topicmapID, viewmode);
		}
		//
		return directives;
	}
		


	// ---------------------------
	// --- Handling Properties ---
	// ---------------------------



	public boolean propertiesChangeAllowed(Hashtable oldProps, Hashtable newProps, CorporateDirectives directives) {
		String webAlias = (String) newProps.get(PROPERTY_WEB_ALIAS);
		if (webAlias != null) {
			BaseTopic cityMap = lookupCityMap(webAlias, as);	// DME (Mehrdeutigkeit) ###
			if (cityMap != null) {
				String errText = "Web Alias \"" + webAlias + "\" ist bereits an Stadtplan \"" +
					cityMap.getName() + "\" vergeben -- Bitte anderen Web Alias verwenden";
				directives.add(DIRECTIVE_SHOW_MESSAGE, errText, new Integer(NOTIFICATION_WARNING));
				System.out.println("*** CityMapTopic.propertiesChangeAllowed(): " + errText);
				return false;
			}
		}
		return super.propertiesChangeAllowed(oldProps, newProps, directives);
	}



	// ----------------------
	// --- Topicmap Hooks ---
	// ----------------------



	public PresentableTopic getPresentableTopic(BaseTopic topic, String nearTopicID) {
		PresentableTopic pt = super.getPresentableTopic(topic, nearTopicID);
		//
		if (as.isInstanceOf(topic, TOPICTYPE_KIEZ_GEO)) {
			try {
				GeoObjectTopic inst = (GeoObjectTopic) as.getLiveTopic(topic);
				// determine new geometry
				Point p = inst.getPoint(getID());	// throws DME
				// set new geometry
				if (p != null) {	// Note: p is null if YADE is "off"
					// ### System.out.println(">>> CityMapTopic.getPresentableTopic(): " + topic + " prgramatically placed to " + p);
					pt.setGeometry(p);
				}
			} catch (DeepaMehtaException e) {
				// not an error
			}
		}
		//
		return pt;
	}



	// **********************
	// *** Custom Methods ***
	// **********************



	/**
	 * Pre-condition: this topic map is the published version.
	 *
	 * @see		de.kiezatlas.deepamehta.BrowseServlet#initInstitutaionType
	 * @see		de.kiezatlas.deepamehta.ListServlet#preparePage
	 */
	public BaseTopic getInstitutionType() {
		BaseTopic workspace = as.getTopicmapOwner(getID());
		System.out.println(">>> map belongs to workspace " + workspace);
		// institution type
		Vector typeIDs = as.type(TOPICTYPE_KIEZ_GEO, 1).getSubtypeIDs();
		Vector instTypes = cm.getRelatedTopics(workspace.getID(), SEMANTIC_WORKSPACE_TYPES, TOPICTYPE_TOPICTYPE, 2, typeIDs, true);
		// error check
		if (instTypes.size() == 0) {
			throw new DeepaMehtaException("Administrator-Fehler: Einrichtungstyp für Workspace \"" + workspace.getName() + "\" ist nicht bekannt");
		}
		//
		return (BaseTopic) instTypes.firstElement();
	}

	public SearchCriteria[] getSearchCriterias() {
		BaseTopic instType = getInstitutionType();
		// search criterias
		Vector typeIDs = as.type(TOPICTYPE_CRITERIA, 1).getSubtypeIDs();
		Vector crits = cm.getRelatedTopics(instType.getID(), SEMANTIC_RELATION_DEFINITION, TOPICTYPE_TOPICTYPE, 2, typeIDs, true);
		SearchCriteria[] criterias = new SearchCriteria[crits.size()];
		System.out.println(">>> there are " + crits.size() + " search criterias:");
		for (int i = 0; i < crits.size(); i++) {
			TypeTopic crit = (TypeTopic) as.getLiveTopic((BaseTopic) crits.elementAt(i));
			criterias[i] = new SearchCriteria(crit, new Vector());
			System.out.println("  > " + crit);
		}
		return criterias;
	}

	/**
	 * Pre-condition: this topic map is the published version.
	 *
	 * @see		de.kiezatlas.deepamehta.BrowseServlet#initShapeTypes
	 */
	public Vector getShapeTypes() {
		BaseTopic workspace = as.getTopicmapOwner(getID());
		// institution type
		Vector typeIDs = as.type(TOPICTYPE_SHAPE, 1).getSubtypeIDs();
		Vector shapeTypes = cm.getRelatedTopics(workspace.getID(), SEMANTIC_WORKSPACE_SHAPETYPE, TOPICTYPE_TOPICTYPE, 2, typeIDs, true);	// sortAssociations=true
		//
		return shapeTypes;
	}

	// ---

	/**
	 * @return	2-element array of YADE-reference topics, or <code>null</code> if there are no YADE-reference topics
	 *			inside this city map (YADE is "off").
	 *
	 * @see		GeeObjecTopic#getPoint
	 * @see		GeoObjectTopic#getYadePoint
	 */
	public PresentableTopic[] getYADEReferencePoints() throws DeepaMehtaException {
		Vector yp = cm.getViewTopics(getID(), 1, TOPICTYPE_YADE_POINT);
		if (yp.size() == 0) {
			// Note: this is not an error (YADE is "off").
			return null;
		} else if (yp.size() != 2) {
			throw new DeepaMehtaException("Administrator-Fehler: Stadtplan \"" + getName() + "\" hat " + yp.size() + " YADE Referenzpunkte. Notwendig sind 0 oder 2.");
		}
		PresentableTopic[] yt = new PresentableTopic[2];
		yt[0] = (PresentableTopic) yp.elementAt(0);
		yt[1] = (PresentableTopic) yp.elementAt(1);
		return yt;
	}

	/**
	 * Reposition all institutions of this city map based on their YADE coordinates.
	 *
	 * @see		#executeCommand
	 */
	private void repositionAllInstitutions(CorporateDirectives directives) {
		Vector typeIDs = as.type(TOPICTYPE_KIEZ_GEO, 1).getSubtypeIDs();
		Vector insts = cm.getViewTopics(getID(), 1, typeIDs);	// ### BaseTopics would be sufficient, but there is no such cm call
		Enumeration e = insts.elements();
		while (e.hasMoreElements()) {
			try {
				GeoObjectTopic inst = (GeoObjectTopic) as.getLiveTopic((PresentableTopic) e.nextElement());
				// calculate screen coordinate
				Point p = inst.getPoint(getID());	// throws DME
				// Note: if YADE is "off" p is null
				if (p == null) {
					String txt = "Die Einrichtungen konnten nicht neuplatziert werden. Es müssen erst 2 Referenzpunkte gesetzt werden.";
					directives.add(DIRECTIVE_SHOW_MESSAGE, txt, new Integer(NOTIFICATION_WARNING));
					System.out.println(">>> CityMapTopic.repositionAllInstitutions(): " + txt);
					return;
				}
				// reposition
				directives.add(DIRECTIVE_SET_TOPIC_GEOMETRY, inst.getID(), p, getID());
				System.out.println(">>> CityMapTopic.repositionAllInstitutions(): " + inst + " -> moved to " + p.x + " " + p.y);
			} catch (DeepaMehtaException dme) {
				String txt = "Die Neuplatzierung der Einrichtungen wurde abgebrochen (" + dme.getMessage() + ")";
				directives.add(DIRECTIVE_SHOW_MESSAGE, txt, new Integer(NOTIFICATION_WARNING));
				System.out.println(">>> CityMapTopic.repositionAllInstitutions(): " + txt);
				return;
			}
		}
	}

	// --- lookupCityMap (3 forms) ---

	// Note: only published city maps are considered
	public static BaseTopic lookupCityMap(String alias, ApplicationService as) throws DeepaMehtaException {
		return lookupCityMap(alias, false, as);
	}

	public static BaseTopic lookupCityMap(String alias, boolean throwIfNotFound, ApplicationService as)
																				throws DeepaMehtaException {
		// search "Kiez-Atlas" workspace
		BaseTopic map = lookupCityMap(alias, WORKSPACE_KIEZATLAS, as);
		// search sub-workspaces
		if (map == null) {
			Vector workspaces = as.getRelatedTopics(WORKSPACE_KIEZATLAS, SEMANTIC_SUB_WORKSPACE,
				TOPICTYPE_WORKSPACE, 2, false, true);	// sortAssociations=false, emptyAllowed=true
			Enumeration e = workspaces.elements();
			while (e.hasMoreElements()) {
				String workspaceID = ((BaseTopic) e.nextElement()).getID();
				BaseTopic m = lookupCityMap(alias, workspaceID, as);
				if (m != null) {
					map = m;
					break;
				}
			}
		}
		if (map == null && throwIfNotFound) {
			throw new DeepaMehtaException("Fehler in URL: Stadtplan \"" + alias + "\" ist nicht bekannt");
		}
		//
		return map;
	}

	private static BaseTopic lookupCityMap(String alias, String workspaceID, ApplicationService as)
																				throws DeepaMehtaException {
		Hashtable props = new Hashtable();
		props.put(PROPERTY_WEB_ALIAS, alias);
		//
		BaseTopic topicmap = as.getWorkspaceTopicmap(workspaceID);
		Vector maps = as.cm.getTopics(TOPICTYPE_CITYMAP, props, topicmap.getID(), true);	// caseSensitiv=true
		// error check
		if (maps.size() > 1) {
			throw new DeepaMehtaException("Mehrdeutigkeit: es gibt " + maps.size() + " \"" + alias + "\" Stadtpläne");
		}
		//
		BaseTopic map = maps.size() == 1 ? (BaseTopic) maps.firstElement() : null;
		return map;
	}
}
