package de.kiezatlas.deepamehta.topics;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import de.deepamehta.AmbiguousSemanticException;
import de.deepamehta.BaseTopic;
import de.deepamehta.DeepaMehtaException;
import de.deepamehta.PresentableTopic;
import de.deepamehta.service.ApplicationService;
import de.deepamehta.service.CorporateCommands;
import de.deepamehta.service.CorporateDirectives;
import de.deepamehta.service.Session;
import de.deepamehta.topics.LiveTopic;
import de.deepamehta.topics.TopicTypeTopic;
import de.kiezatlas.deepamehta.Comment;
import de.kiezatlas.deepamehta.KiezAtlas;

/**
 * 
 * Kiez-Atlas 1.5<br>
 * Requires DeepaMehta 2.0b8-pre2
 * <p>
 * Last change: 10.10.2007<br>
 * Malte Reißig<br>
 * mre@deepamehta.de
 * 
 */
public class GeoObjectTopic extends LiveTopic implements KiezAtlas{

	/**
	 * 
	 */
	static final String DEFAULT_PASSWORD = "geheim";
	
	// ------------------
	// --- Constructor ---
	// ------------------
	
	public GeoObjectTopic(BaseTopic topic, ApplicationService as) {
		super(topic, as);
		// TODO Auto-generated constructor stub
	}
	
	// **********************
	// *** Defining Hooks ***
	// **********************



	// ------------------
	// --- Life Cycle ---
	// ------------------



	public CorporateDirectives evoke(Session session, String topicmapID, String viewmode) {
		setTopicData(PROPERTY_LOCKED_GEOMETRY, SWITCH_ON);
		setTopicData(PROPERTY_PASSWORD, DEFAULT_PASSWORD);
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
			directives.add(as.setTopicProperty(getID(), getVersion(), PROPERTY_LOCKED_GEOMETRY, value,
				topicmapID, viewmode, session));
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
	
	// ---
	
	public Vector getCategories(String critTypeID) {
		return as.getRelatedTopics(getID(), ASSOCTYPE_ASSOCIATION, critTypeID, 2);
	}
	
	public BaseTopic getImage() {
		try {
			return as.getRelatedTopic(getID(), ASSOCTYPE_ASSOCIATION, TOPICTYPE_IMAGE, 2, true);		// emptyAllowed=true
		} catch (AmbiguousSemanticException e) {
			System.out.println("*** GeoObjectTopic.getImage(): " + e);
			return e.getDefaultTopic();
		}
	}
	
	public Vector getComments() {
		BaseTopic forum = getForum();
		if (forum == null) {
			throw new DeepaMehtaException("Institution " + getID() + " has no forum topic");
		}
		String[] sortProps = {PROPERTY_COMMENT_DATE, PROPERTY_COMMENT_TIME};
		return cm.getRelatedTopics(forum.getID(), SEMANTIC_FORUM_COMMENTS, TOPICTYPE_COMMENT, 2, sortProps, true);	// descending=true
	}
	
	public boolean isForumActivated() {
		BaseTopic forum = getForum();
		if (forum == null) {
			return false;
		}
		return as.getTopicProperty(forum, PROPERTY_FORUM_ACTIVITION).equals(SWITCH_ON);
	}
	
	public BaseTopic getForum() {
		try {
			return as.getRelatedTopic(getID(), SEMANTIC_INSTITUTION_FORUM, TOPICTYPE_FORUM, 2, true);		// emptyAllowed=true
		} catch (AmbiguousSemanticException e) {
			System.out.println("*** GeoObjectTopic.getForum(): " + e);
			return e.getDefaultTopic();
		}
	}
	
	
	// ------------------------
	// --- Topic Type Hooks ---
	// ------------------------



	/**
	 * @return  the ID of the search type
	 *
	 * @see     TopicTypeTopic#createSearchType
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
	
	public BaseTopic getAddress() {
		try {
			return as.getRelatedTopic(getID(), ASSOCTYPE_ASSOCIATION, TOPICTYPE_ADDRESS, 2, true);		// emptyAllowed=true
		} catch (AmbiguousSemanticException e) {
			System.out.println("*** GeoObjectTopic.getAddress(): " + e);
			return e.getDefaultTopic();
		}
	}
	
	
	
	public String getCity() {
		return getProperty(PROPERTY_CITY);
	}

	
	/**
	 * Converts the YADE-coordinate of this institution into a screen coordinate.
	 *
	 * @return	the screen coordinate, or <code>null</code> if YADE is "off".
	 *
	 * @see		#propertiesChanged
	 * @see		CityMapTopic#getPresentableTopic
	 * @see		CityMapTopic#repositionAllInstitutions
	 */
	Point getPoint(String topicmapID) throws DeepaMehtaException {
		// ### copied
		CityMapTopic citymap = (CityMapTopic) as.getLiveTopic(topicmapID, 1);
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
			throw new DeepaMehtaException("YADE Koordinate von Einrichtung \"" + getName() + "\" ist ungültig (" + e.getMessage() + ")");
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
	Point2D.Float getYadePoint(int x, int y, String topicmapID) throws DeepaMehtaException {
		// ### copied
		CityMapTopic citymap = (CityMapTopic) as.getLiveTopic(topicmapID, 1);
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


}
