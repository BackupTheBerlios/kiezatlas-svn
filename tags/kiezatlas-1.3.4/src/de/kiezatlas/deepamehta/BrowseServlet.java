package de.kiezatlas.deepamehta;

import de.kiezatlas.deepamehta.topics.CityMapTopic;
import de.kiezatlas.deepamehta.topics.InstitutionTopic;
//
import de.deepamehta.BaseTopic;
import de.deepamehta.DeepaMehtaException;
import de.deepamehta.DeepaMehtaUtils;
import de.deepamehta.PresentableTopic;
import de.deepamehta.service.Session;
import de.deepamehta.service.web.DeepaMehtaServlet;
import de.deepamehta.service.web.RequestParameter;
import de.deepamehta.service.web.WebSession;
import de.deepamehta.topics.TypeTopic;
import de.deepamehta.topics.EmailTopic;
//
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.swing.ImageIcon;
//
import java.awt.Image;
import java.awt.Point;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.net.URL;
import java.util.*;



/**
 * Kiez-Atlas 1.3.4<br>
 * Requires DeepaMehta 2.0b7-post1
 * <p>
 * Last change: 30.8.2006<br>
 * J&ouml;rg Richter<br>
 * jri@freenet.de
 */
public class BrowseServlet extends DeepaMehtaServlet implements KiezAtlas {

	protected String performAction(String action, RequestParameter params, Session session) throws ServletException {
		if (action == null) {
			try {
				String pathInfo = params.getPathInfo();
				// error check
				if (pathInfo == null || pathInfo.length() == 1) {
					throw new DeepaMehtaException("Fehler in URL");
				}
				//
				String alias = pathInfo.substring(1);
				setCityMap(CityMapTopic.lookupCityMap(alias, true, as), session);	// throwIfNotFound=true
				initInstitutaionType(session);	// relies on city map
				initSearchCriterias(session);	// relies on institutaion type
				initShapeTypes(session);		// relies on city map
				updateShapes(session);			// relies on shape types;
				return PAGE_FRAMESET;
			} catch (DeepaMehtaException e) {
				System.out.println("*** BrowseServlet.performAction(): " + e);
				session.setAttribute("error", e.getMessage());
				return PAGE_ERROR;
			}
		}
		// session timeout?
		if (getCityMap(session) == null) {
			System.out.println("*** Session Expired ***");
			session.setAttribute("error", "Timeout: Kiez-Atlas wurde mehr als " +
				((WebSession) session).session.getMaxInactiveInterval() / 60 + " Minuten nicht benutzt");
			return PAGE_ERROR;
		}
		//
		if (action.equals(ACTION_INIT_FRAME)) {
			String frame = params.getValue("frame");
			if (frame.equals(FRAME_LEFT)) {
				return PAGE_CITY_MAP;
			} else if (frame.equals(FRAME_RIGHT)) {
				// list categories of 1st search criteria, if there is a criteria at all
				if (getCriterias(session).length > 0) {
					setSearchMode("0", session);	// ### was SEARCHMODE_BY_CATEGORY
					return PAGE_CATEGORY_LIST;
				} else {
					// otherwise list all institutions
					setSearchMode(SEARCHMODE_BY_NAME, session);
					setSearchValue("", session);	// searching for "" retrieves all institutions
					return PAGE_INSTITUTION_LIST;
				}
			} else {
				throw new DeepaMehtaException("unexpected frame \"" + frame + "\"");
			}
		// search
		} else if (action.equals(ACTION_SEARCH)) {
			setSearchMode(SEARCHMODE_BY_NAME, session);
			setSearchValue(params.getValue("search"), session);
			return PAGE_INSTITUTION_LIST;
		// show
		} else if (action.equals(ACTION_SHOW_CATEGORIES)) {
			String critNr = params.getValue("critNr");
			setSearchMode(critNr, session);
			return PAGE_CATEGORY_LIST;
		// select
		} else if (action.equals(ACTION_SELECT_CATEGORY)) {
			Vector selCats = getSelectedCats(session);
			String catID = params.getValue("id");
			toggle(selCats, catID);
			return PAGE_CATEGORY_LIST;
		// search by
		} else if (action.equals(ACTION_SEARCH_BY_CATEGORY)) {
			// needed for "cross-links"
			String critNr = params.getValue("critNr");
			if (critNr != null) {
				setSearchMode(critNr, session);
			}
			//
			String catID = params.getValue("id");
			setSearchValue(as.getLiveTopic(catID, 1).getName(), session);
			// ### setSearchMode(SEARCHMODE_BY_CATEGORY, session);		// needed for "cross-links"
			Vector selCats = getSelectedCats(session);
			switchOn(selCats, catID);
			return PAGE_INSTITUTION_LIST;
		// info
		} else if (action.equals(ACTION_SHOW_INSTITUTION_INFO)) {
			String instID = params.getValue("id");
			setSelectedInst(instID, session);
			//
			InstitutionTopic inst = (InstitutionTopic) as.getLiveTopic(instID, 1);
			boolean isForumActivated = inst.isForumActivated();
			session.setAttribute("forumActivition", isForumActivated ? SWITCH_ON : SWITCH_OFF);
			if (isForumActivated) {
				session.setAttribute("commentCount", new Integer(inst.getComments().size()));
			}
			return PAGE_INSTITUTION_INFO;
		// show forum
		} else if (action.equals(ACTION_SHOW_INSTITUTION_FORUM)) {
			return PAGE_INSTITUTION_FORUM;
		// show comment form
		} else if (action.equals(ACTION_SHOW_COMMENT_FORM)) {
			// Note: "instComments" are still in the session
			return PAGE_COMMENT_FORM;
		// create comment
		} else if (action.equals(ACTION_CREATE_COMMENT)) {
			// create comment and set date & time
			String commentID = createTopic(TOPICTYPE_COMMENT, params, session);
			as.setTopicProperty(commentID, 1, PROPERTY_COMMENT_DATE, DeepaMehtaUtils.getDate());
			as.setTopicProperty(commentID, 1, PROPERTY_COMMENT_TIME, DeepaMehtaUtils.getTime());
			// associate comment with forum
			InstitutionTopic inst = getSelectedInst(session);
			String forumID = inst.getForum().getID();
			String assocID = as.getNewAssociationID();
			cm.createAssociation(assocID, 1, SEMANTIC_FORUM_COMMENTS, 1, forumID, 1, commentID, 1);
			// send notification email
			sendNotificationEmail(inst.getID(), commentID);
			//
			return PAGE_INSTITUTION_FORUM;
		// shape display
		} else if (action.equals(ACTION_TOGGLE_SHAPE_DISPLAY)) {
			String shapeTypeID = params.getValue("typeID");
			toggleShapeDisplay(shapeTypeID, session);
			updateShapes(session);
			return PAGE_CITY_MAP;
		} else {
			return super.performAction(action, params, session);
		}
	}

	protected void preparePage(String page, RequestParameter params, Session session) {
		if (page.equals(PAGE_CATEGORY_LIST)) {
			Vector categories = cm.getTopics(getCurrentCriteria(session).criteria.getID());
			Vector selectedCats = getSelectedCats(session);
			session.setAttribute("categories", categories);
			session.setAttribute("selectedCats", selectedCats);
			// hotspots
			setCategoryHotspots(session);
			// clear marker
			setSelectedInst(null, session);
		} else if (page.equals(PAGE_INSTITUTION_LIST)) {
			// institutions
			String mapID = getCityMap(session).getID();
			String instTypeID = getInstitutionType(session).getID();
			String searchMode = getSearchMode(session);
			Vector insts;
			if (searchMode.equals(SEARCHMODE_BY_NAME)) {
				insts = cm.getViewTopics(mapID, 1, instTypeID, getSearchValue(session));
				// hotspots
				setHotspots(insts, ICON_HOTSPOT, session);
			} else {
				String catID = params.getValue("id");
				insts = cm.getRelatedViewTopics(mapID, 1, catID, ASSOCTYPE_ASSOCIATION, instTypeID, 1);	// ### copy in setCategoryHotspots() 
				// hotspots
				setCategoryHotspots(session);
			}
			session.setAttribute("institutions", insts);
			// categories & addresses
			Hashtable categories = new Hashtable();
			Hashtable addresses = new Hashtable();
			for (int i = 0; i < insts.size(); i++) {
				BaseTopic t = (BaseTopic) insts.elementAt(i);
				try {
					InstitutionTopic inst = (InstitutionTopic) as.getLiveTopic(t);
					// categories
					String critTypeID = getCriteria(0, session).criteria.getID();
					categories.put(inst.getID(), inst.getCategories(critTypeID));
					// address
					BaseTopic address = inst.getAddress();
					if (address != null) {
						Hashtable addressProps = as.getTopicProperties(address);
						addressProps.put(PROPERTY_CITY, inst.getCity());
						addresses.put(inst.getID(), addressProps);
					}
				} catch (ClassCastException e) {
					System.out.println("*** BrowseServlet.preparePage(): " + t + ": " + e);
					// ### happens if institution type is not up to date
				}
			}
			session.setAttribute("categories", categories);
			session.setAttribute("addresses", addresses);
			// clear marker
			setSelectedInst(null, session);
		} else if (page.equals(PAGE_INSTITUTION_FORUM)) {
			session.setAttribute("instComments", getSelectedInst(session).getCommentBeans());
		}
	}



	// *****************
	// *** Utilities ***
	// *****************



	private void toggle(Vector topicIDs, String topicID) {
		if (topicIDs.contains(topicID)) {
			topicIDs.removeElement(topicID);
		} else {
			topicIDs.addElement(topicID);
		}
	}

	private void switchOn(Vector topicIDs, String topicID) {
		if (!topicIDs.contains(topicID)) {
			topicIDs.addElement(topicID);
		}
	}

	// ---

	private void sendNotificationEmail(String instID, String commentID) {
		try {
			InstitutionTopic inst = (InstitutionTopic) as.getLiveTopic(instID, 1);
			// "from"
			String from = as.getEmailAddress("t-rootuser");		// ###
			if (from == null || from.equals("")) {
				throw new DeepaMehtaException("email address of root user is unknown");
			}
			// "to"
			BaseTopic email = inst.getEmail();
			if (email == null || email.getName().equals("")) {
				throw new DeepaMehtaException("email address of \"" + inst.getName() + "\" is unknown");
			}
			String to = email.getName();
			// "subject"
			String subject = "Kiezatlas: neuer Forumsbeitrag f�r \"" + inst.getName() + "\"";
			// "body"
			Hashtable comment = cm.getTopicData(commentID, 1);
			String body = "Dies ist eine automatische Benachrichtigung von www.kiezatlas.de\r\r" +
				"Im Forum der Einrichtung \"" + inst.getName() + "\" wurde ein neuer Kommentar eingetragen:\r\r" +
				"------------------------------\r" +
				comment.get(PROPERTY_TEXT) + "\r\r" +
				"Autor: " + comment.get(PROPERTY_COMMENT_AUTHOR) + "\r" +
				"Email: " + comment.get(PROPERTY_EMAIL_ADDRESS) + "\r" +
				"Datum: " + comment.get(PROPERTY_COMMENT_DATE) + "\r" +
				"Uhrzeit: " + comment.get(PROPERTY_COMMENT_TIME) + "\r" +
				"------------------------------\r\r" +
				"Im Falle des Mi�brauchs: In der \"Forum Administration\" ihres pers�nlichen Kiezatlas-Zugangs haben " +
				"Sie die M�glichkeit, einzelne Kommentare zu l�schen, bzw. das Forum ganz zu deaktivieren.\r" +
				"www.kiezatlas.de:8080/edit/" + inst.getWebAlias() + "\r\r" +
				"Mit freundlichen Gr��en\r" +
				"ihr Kiezatlas-Team";
			//
			System.out.println(">>> send notification email");
			System.out.println("  > SMTP server: \"" + as.getSMTPServer() + "\"");	// as.getSMTPServer() throws DME
			System.out.println("  > from: \"" + from + "\"");
			System.out.println("  > to: \"" + to + "\"");
			// send email
			EmailTopic.sendMail(as.getSMTPServer(), from, to, subject, body);		// EmailTopic.sendMail() throws DME
		} catch (Exception e) {
			System.out.println("*** notification email not send (" + e + ")");
		}
	}



	// *************************
	// *** Session Utilities ***
	// *************************



	// --- Methods to maintain data in the session

	private void setCityMap(BaseTopic map, Session session) {
		String mapImage = as.getCorporateWebBaseURL() + FILESERVER_BACKGROUNDS_PATH +
			as.getTopicProperty(map, PROPERTY_BACKGROUND_IMAGE);
		session.setAttribute("map", map);
		session.setAttribute("mapImage", mapImage);
		System.out.println("  > \"map\" stored in session: " + map);
		System.out.println("  > \"mapImage\" stored in session: " + mapImage);
	}

	private void setSearchMode(String searchMode, Session session) {
		session.setAttribute("searchMode", searchMode);
		System.out.println("> \"searchMode\" stored in session: " + searchMode);
	}

	private void setSearchValue(String searchValue, Session session) {
		session.setAttribute("searchValue", searchValue);
		System.out.println("> \"searchValue\" stored in session: " + searchValue);
	}

	private void initInstitutaionType(Session session) {
		BaseTopic instT = ((CityMapTopic) as.getLiveTopic(getCityMap(session))).getInstitutionType();	// ### ugly
		TypeTopic instType = (TypeTopic) as.getLiveTopic(instT);
		session.setAttribute("instType", instType);
		System.out.println(">>> \"instType\" stored in session: " + instType);
	}

	private void initSearchCriterias(Session session) {
		BaseTopic instType = getInstitutionType(session);
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
		session.setAttribute("criterias", criterias);
	}

	private void initShapeTypes(Session session) {
		Vector st = ((CityMapTopic) as.getLiveTopic(getCityMap(session))).getShapeTypes();	// ### ugly
		System.out.println(">>> there are " + st.size() + " shape types:");
		Vector shapeTypes = new Vector();
		for (int i = 0; i < st.size(); i++) {
			TypeTopic shapeType = (TypeTopic) as.getLiveTopic((BaseTopic) st.elementAt(i));
			shapeTypes.addElement(new ShapeType(shapeType.getID(), shapeType.getPluralNaming(),
				as.getTopicProperty(shapeType, PROPERTY_COLOR), false));	// isSelected=false
			System.out.println("  > " + shapeType.getName());
		}
		session.setAttribute("shapeTypes", shapeTypes);
	}

	private void setSelectedInst(String instID, Session session) {
		Institution institution = instID != null ? new Institution(instID, getCriterias(session), as) : null;
		session.setAttribute("selectedInst", institution);
		System.out.println("> \"selectedInst\" stored in session: " + institution);
	}

	// ---

	private void setCategoryHotspots(Session session) {
		String mapID = getCityMap(session).getID();
		String instTypeID = getInstitutionType(session).getID();
		Vector selCats = getSelectedCats(session);
		//
		Vector hotspots = new Vector();
		Enumeration e = selCats.elements();
		while (e.hasMoreElements()) {
			String catID = (String) e.nextElement();
			Vector presentables = cm.getRelatedViewTopics(mapID, 1, catID, ASSOCTYPE_ASSOCIATION, instTypeID, 1);
			String icon;
			if (getSearchMode(session).equals("0")) {	// ### first search criteria uses distinct visualization
				icon = as.getLiveTopic(catID, 1).getIconfile();
				if (icon.startsWith("ka-")) {	// use only small icon if standard Kiez-Atlas category icons is used
					icon = icon.substring(0, icon.length() - 4) + "-small.gif";		// ### could be property
				}
			} else {
				icon = ICON_HOTSPOT;
			}
			presentables.insertElementAt(as.getCorporateWebBaseURL() + FILESERVER_ICONS_PATH + icon, 0);
			hotspots.addElement(presentables);
		}
		session.setAttribute("hotspots", hotspots);
		System.out.println("> \"hotspots\" stored in session: institutions for " + selCats.size() + " categories");
	}

	// ---

	/**
	 * @param	topics	vector of PresentableTopics
	 */
	private void setHotspots(Vector topics, String icon, Session session) {
		Vector hotspots = new Vector();
		Vector presentables = new Vector(topics);
		presentables.insertElementAt(as.getCorporateWebBaseURL() + FILESERVER_ICONS_PATH + icon, 0);
		hotspots.addElement(presentables);
		session.setAttribute("hotspots", hotspots);
		System.out.println("> \"hotspots\" stored in session: " + topics.size() + " institutions");
	}

	// ---

	private void toggleShapeDisplay(String shapeTypeID, Session session) {
		Vector shapeTypes = getShapeTypes(session);
		Enumeration e = shapeTypes.elements();
		while (e.hasMoreElements()) {
			ShapeType shapeType = (ShapeType) e.nextElement();
			if (shapeType.typeID.equals(shapeTypeID)) {
				shapeType.isSelected = !shapeType.isSelected;
			}
		}
	}

	private void updateShapes(Session session) {
		try {
			Vector shapes = new Vector();
			Vector shapeTypes = getShapeTypes(session);
			String mapID = getCityMap(session).getID();
			// for all shape types ...
			Enumeration e = shapeTypes.elements();
			while (e.hasMoreElements()) {
				ShapeType shapeType = (ShapeType) e.nextElement();
				// if type is selected ...
				if (shapeType.isSelected) {
					// ... query all shapes and add Shape objects to the "shapes" vector 
					Vector shapeTopics = cm.getViewTopics(mapID, 1, shapeType.typeID);
					Enumeration e2 = shapeTopics.elements();
					while (e2.hasMoreElements()) {
						PresentableTopic shapeTopic = (PresentableTopic) e2.nextElement();
						String icon = as.getLiveTopic(shapeTopic).getIconfile();
						// --- load shape image and calculate position ---
						String url = as.getCorporateWebBaseURL() + FILESERVER_ICONS_PATH + icon;
						// Note 1: Toolkit.getImage() is used here instead of createImage() in order to utilize
						// the Toolkits caching mechanism
						// Note 2: ImageIcon is a kluge to make sure the image is fully loaded before we proceed
						Image image = new ImageIcon(Toolkit.getDefaultToolkit().getImage(new URL(url))).getImage();
						int width = image.getWidth(null);
						int height = image.getHeight(null);
						// System.out.println(">>> shape size: " + width + "x" + height);
						Dimension size = new Dimension(width, height);
						Point point = shapeTopic.getGeometry();
						point.translate(-width / 2, -height / 2);
						//
						shapes.addElement(new Shape(url, point, size));
					}
				}
			}
			session.setAttribute("shapes", shapes);
			System.out.println("> \"shapes\" stored in session: " + shapes.size() + " shapes");
		} catch (Throwable e) {
			System.out.println("*** BrowseServlet.updateShapes(): " + e);
		}
	}

	// ---

	private BaseTopic getCityMap(Session session) {
		return (BaseTopic) session.getAttribute("map");
	}

	private SearchCriteria[] getCriterias(Session session) {
		return (SearchCriteria[]) session.getAttribute("criterias");
	}

	private SearchCriteria getCriteria(int critNr, Session session) {
		return getCriterias(session)[critNr];
	}

	private SearchCriteria getCurrentCriteria(Session session) {
		int i = Integer.parseInt(getSearchMode(session));
		return getCriteria(i, session);
	}

	private TypeTopic getInstitutionType(Session session) {
		return (TypeTopic) session.getAttribute("instType");
	}

	private String getSearchMode(Session session) {
		return (String) session.getAttribute("searchMode");
	}

	private String getSearchValue(Session session) {
		return (String) session.getAttribute("searchValue");
	}

	private Vector getShapeTypes(Session session) {
		return (Vector) session.getAttribute("shapeTypes");
	}

	// ---

	private InstitutionTopic getSelectedInst(Session session) {
		return (InstitutionTopic) as.getLiveTopic(((Institution) session.getAttribute("selectedInst")).id, 1);
	}

	private Vector getSelectedCats(Session session) {
		return getCurrentCriteria(session).selectedCategoryIDs;
	}
}
