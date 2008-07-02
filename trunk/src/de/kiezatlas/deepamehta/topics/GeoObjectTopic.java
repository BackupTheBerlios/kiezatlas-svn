package de.kiezatlas.deepamehta.topics;

import de.kiezatlas.deepamehta.Comment;
import de.kiezatlas.deepamehta.KiezAtlas;

import de.deepamehta.AmbiguousSemanticException;
import de.deepamehta.BaseTopic;
import de.deepamehta.DeepaMehtaException;
import de.deepamehta.PresentableTopic;
import de.deepamehta.PropertyDefinition;
import de.deepamehta.service.ApplicationService;
import de.deepamehta.service.CorporateCommands;
import de.deepamehta.service.CorporateDirectives;
import de.deepamehta.service.Session;
import de.deepamehta.topics.LiveTopic;
import de.deepamehta.topics.TopicTypeTopic;
import de.deepamehta.topics.TypeTopic;
import de.deepamehta.util.DeepaMehtaUtils;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;



/**
 * Kiezatlas 1.5.1<br>
 * Requires DeepaMehta 2.0b8
 * <p>
 * Last change: 25.6.2008<br>
 * J&ouml;rg Richter<br>
 * jri@deepamehta.de
 */
public class GeoObjectTopic extends LiveTopic implements KiezAtlas{



	// *****************
	// *** Constants ***
	// *****************



	static final String DEFAULT_PASSWORD = "geheim";



	// *******************
	// *** Constructor ***
	// *******************



	public GeoObjectTopic(BaseTopic topic, ApplicationService as) {
		super(topic, as);
	}



	// **********************
	// *** Defining Hooks ***
	// **********************



	// ------------------
	// --- Life Cycle ---
	// ------------------



	public CorporateDirectives evoke(Session session, String topicmapID, String viewmode) {
		setProperty(PROPERTY_LOCKED_GEOMETRY, SWITCH_ON);
		setProperty(PROPERTY_PASSWORD, DEFAULT_PASSWORD);
		setProperty(PROPERTY_LAST_MODIFIED, DeepaMehtaUtils.getDate());
		return super.evoke(session, topicmapID, viewmode);
	}



	// --------------------------
	// --- Providing Commands ---
	// --------------------------



	public CorporateCommands contextCommands(String topicmapID, String viewmode,
								Session session, CorporateDirectives directives) {
		CorporateCommands commands = new CorporateCommands(as);
		int editorContext = as.editorContext(topicmapID);
		//
		commands.addNavigationCommands(this, editorContext, session);
		commands.addSeparator();
		// --- "Lock"/"Unlock" ---
		boolean isLocked = getProperty(PROPERTY_LOCKED_GEOMETRY).equals(SWITCH_ON);
		int lockState = !isLocked ? COMMAND_STATE_DEFAULT : COMMAND_STATE_DISABLED;
		int unlockState = isLocked ? COMMAND_STATE_DEFAULT : COMMAND_STATE_DISABLED;
		//
		commands.addCommand(ITEM_LOCK_GEOMETRY, CMD_LOCK_GEOMETRY, lockState);
		commands.addCommand(ITEM_UNLOCK_GEOMETRY, CMD_UNLOCK_GEOMETRY, unlockState);
		// --- standard topic commands ---
		commands.addStandardCommands(this, editorContext, viewmode, session, directives);
		//
		return commands;
	}



	// --------------------------
	// --- Executing Commands ---
	// --------------------------



	public CorporateDirectives executeCommand(String command, Session session, String topicmapID, String viewmode) {
		CorporateDirectives directives = new CorporateDirectives();
		StringTokenizer st = new StringTokenizer(command, COMMAND_SEPARATOR);
		String cmd = st.nextToken();
		if (cmd.equals(CMD_LOCK_GEOMETRY) || cmd.equals(CMD_UNLOCK_GEOMETRY)) {
			String value = cmd.equals(CMD_LOCK_GEOMETRY) ? SWITCH_ON : SWITCH_OFF;
			directives.add(as.setTopicProperty(this, PROPERTY_LOCKED_GEOMETRY, value, topicmapID, session));
		} else {
			return super.executeCommand(command, session, topicmapID, viewmode);
		}
		return directives;
	}



	// ------------------------------------------
	// --- Reacting upon dedicated situations ---
	// ------------------------------------------



	public CorporateDirectives moved(String topicmapID, int topicmapVersion, int x, int y, Session session) {
		CorporateDirectives directives = super.moved(topicmapID, topicmapVersion, x, y, session);
		// abort if we are not inside a city map, but inside a plain topic map
		if (!(as.getLiveTopic(topicmapID, 1) instanceof CityMapTopic)) {
			return directives;
		}
		//
		Point2D.Float yadePoint = getYadePoint(x, y, topicmapID);
		// set properties
		if (yadePoint != null) {	// Note: yadePoint is null if YADE is "off"
			Hashtable props = new Hashtable();
			props.put(PROPERTY_YADE_X, Float.toString(yadePoint.x));
			props.put(PROPERTY_YADE_Y, Float.toString(yadePoint.y));
			directives.add(DIRECTIVE_SHOW_TOPIC_PROPERTIES, getID(), props, new Integer(1));
		}
		//
		return directives;
	}



	// ---------------------------
	// --- Handling Properties ---
	// ---------------------------



	public boolean propertyChangeAllowed(String propName, String propValue, Session session, CorporateDirectives directives) {
		// compare to CityMapTopic.propertyChangeAllowed()
		if (propName.equals(PROPERTY_WEB_ALIAS)) {
			String webAlias = propValue;
			// ### compare to lookupInstitution()
			Vector typeIDs = as.type(TOPICTYPE_KIEZ_GEO, 1).getSubtypeIDs();
			Hashtable props = new Hashtable();
			props.put(PROPERTY_WEB_ALIAS, webAlias);
			Vector insts = cm.getTopics(typeIDs, props, true);	// caseSensitive=true
			//
			if (insts.size() > 0) {
				BaseTopic inst = (BaseTopic) insts.firstElement();
				String errText = "Web Alias \"" + webAlias + "\" ist bereits an \"" + inst.getName() +
					"\" vergeben -- Für \"" + getName() + "\" bitte anderen Web Alias verwenden";
				directives.add(DIRECTIVE_SHOW_MESSAGE, errText, new Integer(NOTIFICATION_WARNING));
				System.out.println("*** GeoObjectTopic.propertyChangeAllowed(): " + errText);
				return false;
			}
		}
		return super.propertyChangeAllowed(propName, propValue, session, directives);
	}

	public CorporateDirectives propertiesChanged(Hashtable newProps, Hashtable oldProps,
											String topicmapID, String viewmode, Session session) {
		CorporateDirectives directives = super.propertiesChanged(newProps, oldProps, topicmapID, viewmode, session);
		// --- "YADE" ---
		if (newProps.get(PROPERTY_YADE_X) != null || newProps.get(PROPERTY_YADE_Y) != null) {
			// determine new geometry
			Point p = getPoint(topicmapID);	// throws DME
			// set new geometry
			if (p != null) {	// Note: p is null if YADE is "off"
				directives.add(DIRECTIVE_SET_TOPIC_GEOMETRY, getID(), p, topicmapID);
			} else {
				// ###
				System.out.println(">>> GeoObjectTopic.propertiesChanged(): " + this + " not (re)positioned (VADE is off)");
			}
		}
		//
		return directives;
	}

	public static Vector hiddenProperties(TypeTopic type) {
		Vector props = new Vector();
		props.addElement(PROPERTY_DESCRIPTION);
		return props;
	}

	public static Vector hiddenProperties(TypeTopic type, String relTopicTypeID) {
		Vector props = null;
		if (relTopicTypeID.equals(TOPICTYPE_EMAIL_ADDRESS)) {
			props = new Vector();
			props.addElement(PROPERTY_MAILBOX_URL);
		} else if (relTopicTypeID.equals(TOPICTYPE_IMAGE)) {
			props = new Vector();
			props.addElement(PROPERTY_NAME);
		}
		return props;
	}

	public static void propertyLabel(PropertyDefinition propDef, ApplicationService as, Session session) {
		String propName = propDef.getPropertyName();
		if (propName.equals(PROPERTY_SONSTIGES)) {
			propDef.setPropertyLabel("Weitere Infos");
		}
	}

	public static String propertyLabel(PropertyDefinition propDef, String relTopicTypeID, ApplicationService as) {
		String propName = propDef.getPropertyName();
		if (relTopicTypeID.equals(TOPICTYPE_ADDRESS)) {
			if (propName.equals(PROPERTY_STREET)) {
				return "Stra&szlig;e";
			} else if (propName.equals(PROPERTY_POSTAL_CODE)) {
				return "Postleitzahl";
			}
		} else if (relTopicTypeID.equals(TOPICTYPE_WEBPAGE)) {
			if (propName.equals(PROPERTY_URL)) {
				return "Website (URL)";
			}
		} else if (relTopicTypeID.equals(TOPICTYPE_AGENCY)) {
			if (propName.equals(PROPERTY_NAME)) {
				return "Tr&auml;ger";
			} else if (propName.equals(PROPERTY_AGENCY_KIND)) {
				return "Art des Tr&auml;gers";
			}
		} else if (relTopicTypeID.equals(TOPICTYPE_PERSON)) {
			if (propName.equals(PROPERTY_FIRST_NAME)) {
				return "Ansprechpartner/in (Vorname)";
			} else if (propName.equals(PROPERTY_NAME)) {
				return "Ansprechpartner/in (Nachname)";
			} else if (propName.equals(PROPERTY_GENDER)) {
				return "Ansprechpartner/in";
			}
		} else if (relTopicTypeID.equals(TOPICTYPE_PHONE_NUMBER)) {
			if (propName.equals(PROPERTY_NAME)) {
				return "Telefon";
			}
		} else if (relTopicTypeID.equals(TOPICTYPE_FAX_NUMBER)) {
			if (propName.equals(PROPERTY_NAME)) {
				return "Fax";
			}
		} else if (relTopicTypeID.equals(TOPICTYPE_EMAIL_ADDRESS)) {
			if (propName.equals(PROPERTY_EMAIL_ADDRESS)) {
				return "E-mail";
			}
		}
		return LiveTopic.propertyLabel(propDef, relTopicTypeID, as);
	}



	// ------------------------
	// --- Topic Type Hooks ---
	// ------------------------



	/**
	 * @return	the ID of the search type
	 *
	 * @see		TopicTypeTopic#createSearchType
	 */
	public static String getSearchTypeID() {
		return TOPICTYPE_KIEZ_GEO_SEARCH;
	}



	// **********************
	// *** Custom Methods ***
	// **********************



	public static BaseTopic lookupInstitution(String alias, ApplicationService as) throws DeepaMehtaException {
		Vector typeIDs = as.type(TOPICTYPE_KIEZ_GEO, 1).getSubtypeIDs();
		Hashtable props = new Hashtable();
		props.put(PROPERTY_WEB_ALIAS, alias);
		Vector institutions = as.cm.getTopics(typeIDs, props, true);		// caseSensitiv=true
		// error check
		if (institutions.size() == 0) {
			throw new DeepaMehtaException("Fehler in URL: Einrichtung \"" + alias + "\" ist nicht bekannt");
		}
		if (institutions.size() > 1) {
			throw new DeepaMehtaException("Mehrdeutigkeit: es gibt " + institutions.size() + " \"" + alias + "\" Einrichtungen");
		}
		//
		BaseTopic inst = (BaseTopic) institutions.firstElement();
		return inst;
	}

	// ---

	public BaseTopic getAddress() {
		try {
			return as.getRelatedTopic(getID(), ASSOCTYPE_ASSOCIATION, TOPICTYPE_ADDRESS, 2, true);		// emptyAllowed=true
		} catch (AmbiguousSemanticException e) {
			System.out.println("*** GeoObjectTopic.getAddress(): " + e);
			return e.getDefaultTopic();
		}
	}

	public String getCity() {
		// if a geoobject has a stadt property, take it and return it
		// else if a geoobject has an addressTopic assigned, check if there is a city, if so return this city
		//
		String city = getProperty(PROPERTY_CITY);
		BaseTopic address = getAddress();
		try {
			if (!city.equals("")) {
				return city;
			} else if (address != null) {
				BaseTopic town = as.getRelatedTopic(address.getID(), ASSOCTYPE_ASSOCIATION, "tt-city", 2, true);
				if (town != null) {
					city = town.getName();
					return city;
				}
			}
			return "";
		} catch (AmbiguousSemanticException aex) {
			System.out.println("*** GeoObjectTopic.getCity(): " + aex);
			return aex.getDefaultTopic().getName();
		}
	}

	public BaseTopic getEmail() {
		try {
			return as.getRelatedTopic(getID(), ASSOCTYPE_ASSOCIATION, TOPICTYPE_EMAIL_ADDRESS, 2, true);		// emptyAllowed=true
		} catch (AmbiguousSemanticException e) {
			System.out.println("*** GeoObjectTopic.getEmail(): " + e);
			return e.getDefaultTopic();
		}
	}

	public String getWebAlias() {
		return getProperty(PROPERTY_WEB_ALIAS);
	}

	// ---

	public Vector getCategories(String critTypeID) {
		return as.getRelatedTopics(getID(), ASSOCTYPE_ASSOCIATION, critTypeID, 2);
	}

	// ---

	public BaseTopic getImage() {
		try {
			return as.getRelatedTopic(getID(), ASSOCTYPE_ASSOCIATION, TOPICTYPE_IMAGE, 2, true);		// emptyAllowed=true
		} catch (AmbiguousSemanticException e) {
			System.out.println("*** GeoObjectTopic.getImage(): " + e);
			return e.getDefaultTopic();
		}
	}

	// ---

	/**
	 * Converts the YADE-coordinates of this geo object into screen coordinates for the specified citymap.
	 *
	 * @return	the screen coordinates, or <code>null</code> if YADE is "off".
	 *			YADE is regarded as "off" if there are no YADE-reference points defined in the
	 *			specified city map.
	 *
	 * @throws	DeepaMehtaException	if citymapID is <code>null</code>.
	 * @throws	DeepaMehtaException	if there is only one or more than 2 YADE-reference points.
	 * @throws	DeepaMehtaException	if a YADE-reference point has invalid coordinates (no float format).
	 * @throws	DeepaMehtaException	if this geo object has invalid coordinates (no float format).
	 *
	 * @see		#propertiesChanged
	 * @see		CityMapTopic#getPresentableTopic
	 * @see		CityMapTopic#repositionAllInstitutions
	 */
	Point getPoint(String citymapID) throws DeepaMehtaException {
		// ### copied
		CityMapTopic citymap = (CityMapTopic) as.getLiveTopic(citymapID, 1);	// throws DME
		int x1, y1, x2, y2;
		float yadeX1, yadeY1, yadeX2, yadeY2;
		try {
			PresentableTopic[] yp = citymap.getYADEReferencePoints();	// throws DME
			if (yp == null) {
				// YADE is "off"
				return null;
			}
			x1 = yp[0].getGeometry().x;
			y1 = yp[0].getGeometry().y;
			x2 = yp[1].getGeometry().x;
			y2 = yp[1].getGeometry().y;
			yadeX1 = Float.parseFloat(as.getTopicProperty(yp[0], PROPERTY_YADE_X));
			yadeY1 = Float.parseFloat(as.getTopicProperty(yp[0], PROPERTY_YADE_Y));
			yadeX2 = Float.parseFloat(as.getTopicProperty(yp[1], PROPERTY_YADE_X));
			yadeY2 = Float.parseFloat(as.getTopicProperty(yp[1], PROPERTY_YADE_Y));
		} catch (NumberFormatException e) {
			throw new DeepaMehtaException("Administrator-Fehler: ein YADE Referenzpunkt von " +
				"Stadtplan \"" + citymap.getName() + "\" hat ungültigen Wert (" + e.getMessage() + ")");
		}
		// yade -> pixel
		try {
			float yadeX = Float.parseFloat(getProperty(PROPERTY_YADE_X));
			float yadeY = Float.parseFloat(getProperty(PROPERTY_YADE_Y));
			int x = (int) (x1 + (x2 - x1) * (yadeX - yadeX1) / (yadeX2 - yadeX1));
			int y = (int) (y2 + (y1 - y2) * (yadeY - yadeY2) / (yadeY1 - yadeY2));
			return new Point(x, y);
		} catch (NumberFormatException e) {
			throw new DeepaMehtaException("YADE Koordinate von Einrichtung \"" + getName() + "\" ist ungültig (" +
				e.getMessage() + ")");
		}
	}

	/**
	 * Converts the specified screen coordinate into a YADE-coordinate.
	 *
	 * @return	the YADE-coordinate, or <code>null</code> if YADE is "off".
	 *
	 * @see		#moved
	 * @see		CityMapTopic#updateYADECoordinates
	 */
	Point2D.Float getYadePoint(int x, int y, String citymapID) throws DeepaMehtaException {
		// ### copied
		CityMapTopic citymap = (CityMapTopic) as.getLiveTopic(citymapID, 1);
		try {
			PresentableTopic[] yp = citymap.getYADEReferencePoints();	// throws DME
			if (yp == null) {
				return null;
			}
			int x1 = yp[0].getGeometry().x;
			int y1 = yp[0].getGeometry().y;
			int x2 = yp[1].getGeometry().x;
			int y2 = yp[1].getGeometry().y;
			float yadeX1 = Float.parseFloat(as.getTopicProperty(yp[0], PROPERTY_YADE_X));
			float yadeY1 = Float.parseFloat(as.getTopicProperty(yp[0], PROPERTY_YADE_Y));
			float yadeX2 = Float.parseFloat(as.getTopicProperty(yp[1], PROPERTY_YADE_X));
			float yadeY2 = Float.parseFloat(as.getTopicProperty(yp[1], PROPERTY_YADE_Y));
			// pixel -> yade
			float yadeX = yadeX1 + (yadeX2 - yadeX1) * (x - x1) / (x2 - x1);
			float yadeY = yadeY2 + (yadeY1 - yadeY2) * (y2 - y) / (y2 - y1);
			return new Point2D.Float(yadeX, yadeY);
		} catch (NumberFormatException e) {
			throw new DeepaMehtaException("Administrator-Fehler: ein YADE Referenzpunkt von " +
				"Stadtplan \"" + citymap.getName() + "\" hat ungültigen Wert (" + e.getMessage() + ")");
		}
	}

	// ---

	public boolean isForumActivated() {
		BaseTopic forum = getForum();
		if (forum == null) {
			return false;
		}
		return as.getTopicProperty(forum, PROPERTY_FORUM_ACTIVITION).equals(SWITCH_ON);
	}

	public Vector getComments() {
		BaseTopic forum = getForum();
		if (forum == null) {
			throw new DeepaMehtaException("Institution " + getID() + " has no forum topic");
		}
		String[] sortProps = {PROPERTY_COMMENT_DATE, PROPERTY_COMMENT_TIME};
		return cm.getRelatedTopics(forum.getID(), SEMANTIC_FORUM_COMMENTS, TOPICTYPE_COMMENT, 2, sortProps, true);	// descending=true
	}

	public BaseTopic getForum() {
		try {
			return as.getRelatedTopic(getID(), SEMANTIC_INSTITUTION_FORUM, TOPICTYPE_FORUM, 2, true);		// emptyAllowed=true
		} catch (AmbiguousSemanticException e) {
			System.out.println("*** GeoObjectTopic.getForum(): " + e);
			return e.getDefaultTopic();
		}
	}

	public Vector getCommentBeans() {
		Vector commentBeans = new Vector();
		//
		Enumeration e = getComments().elements();
		while (e.hasMoreElements()) {
			BaseTopic comment = (BaseTopic) e.nextElement();
			commentBeans.addElement(new Comment(comment.getID(), as));
		}
		//
		return commentBeans;
	}
}
