package de.kiezatlas.deepamehta;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.swing.ImageIcon;

import de.deepamehta.BaseTopic;
import de.deepamehta.DeepaMehtaConstants;
import de.deepamehta.DeepaMehtaException;
import de.deepamehta.PresentableTopic;
import de.deepamehta.service.Session;
import de.deepamehta.service.TopicBean;
import de.deepamehta.service.web.DeepaMehtaServlet;
import de.deepamehta.service.web.RequestParameter;
import de.deepamehta.service.web.WebSession;
import de.deepamehta.topics.TypeTopic;
import de.deepamehta.util.DeepaMehtaUtils;
import de.kiezatlas.deepamehta.topics.CityMapTopic;
import de.kiezatlas.deepamehta.topics.GeoObjectTopic;



/**
 * Kiez-Atlas 1.5<br>
 * Requires DeepaMehta 2.0b8
 * <p>
 * Last change: 15.11.2007<br>
 * Malte Rei&szlig;ig<br>
 * mre@deepamehta.de
 */
public class BrowseServlet extends DeepaMehtaServlet implements KiezAtlas {

	protected String performAction(String action, RequestParameter params, Session session) throws ServletException {
		if (action == null) {
			try {
				//first visit regular or special 
				String pathInfo = params.getPathInfo();
				String alias = pathInfo.substring(1);
				//extension for optional parameters needed for a special visit
				String geoID = params.getParameter("id");
				String kiez = params.getParameter("kiez");
				// error check
				if (pathInfo == null || pathInfo.length() == 1) {
					throw new DeepaMehtaException("Fehler in URL");
				}
				//handling for single geoObjectUrls in the right CityMap, the old url scheme was not applicable for this task
				//cause of incorrect paths to icons and css files, if relative to /browse/nk-reuter/controller?action=showInfo....
				//for this task now a special kiez param was introduced, for the code it means, eliminating redundancy
				if(geoID != null && kiez != null) {
					TopicBean topicBean = as.createTopicBean(geoID, 1);
					session.setAttribute("topicBean", topicBean);
					setCityMap(CityMapTopic.lookupCityMap(kiez, true, as), session);	// throwIfNotFound=true
				} else {
					session.setAttribute("topicBean", null);
					setCityMap(CityMapTopic.lookupCityMap(alias, true, as), session);	// throwIfNotFound=true
				}
				initInstitutaionType(session);	// relies on city map
				initSearchCriterias(session);	// relies on city map
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
			TopicBean topicBean = (TopicBean) session.getAttribute("topicBean");
			if (frame.equals(FRAME_LEFT)) {
				//selectMarker if topicBean is set / form external call
				if(topicBean != null){
					setSelectedGeo(topicBean.id, session);
				}					
				return PAGE_CITY_MAP;
			} else if (frame.equals(FRAME_RIGHT)) {
				if (topicBean != null) {
					//try to show a given GeoObject, have to set a hotspot
					String mapID = getCityMap(session).getID(); // ### just for the hotspot
					String instTypeID = getInstitutionType(session).getID(); // ### just for the hotspot 
					setSearchMode("0", session); // ### i have to set a searchmode
					String imagePath = as.getCorporateWebBaseURL() + FILESERVER_IMAGES_PATH;
					session.setAttribute("imagePath", imagePath);
					GeoObjectTopic geo = (GeoObjectTopic) as.getLiveTopic(topicBean.id, 1);
					boolean isForumActivated = geo.isForumActivated();
					session.setAttribute("forumActivition", isForumActivated ? SWITCH_ON : SWITCH_OFF);
					if (isForumActivated) {
						session.setAttribute("commentCount", new Integer(geo.getComments().size()));
					}
					//just trial and error, somehow it looks good, but don´t now what i did exactly here
					Vector insts = cm.getViewTopics(mapID, 1, instTypeID, getSearchValue(session));
					setHotspots(insts, ICON_HOTSPOT, session);
					return PAGE_GEO_INFO;
				} else if (getCriterias(session).length > 0) {
					// list categories of 1st search criteria, if there is a criteria at all
					setSearchMode("0", session);	// ### was SEARCHMODE_BY_CATEGORY
					return PAGE_CATEGORY_LIST; 
				} else {
					// otherwise list all institutions
					setSearchMode(SEARCHMODE_BY_NAME, session);
					setSearchValue("", session);	// searching for "" retrieves all institutions
					return PAGE_GEO_LIST;
				}
			} else {
				throw new DeepaMehtaException("unexpected frame \"" + frame + "\"");
			}
		// search
		} else if (action.equals(ACTION_SEARCH)) {
			setSearchMode(SEARCHMODE_BY_NAME, session);
			setSearchValue(params.getValue("search"), session);
			return PAGE_GEO_LIST;
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
			return PAGE_GEO_LIST;
		// info
		} else if (action.equals(ACTION_SHOW_GEO_INFO)) {
				String geoID = params.getValue("id");
				setSelectedGeo(geoID, session);
				TopicBean topicBean = as.createTopicBean(geoID, 1);
				session.setAttribute("topicBean", topicBean);
				String imagePath = as.getCorporateWebBaseURL() + FILESERVER_IMAGES_PATH;
				System.out.println("***imagePath: "+imagePath);
				
				session.setAttribute("imagePath", imagePath);
				GeoObjectTopic geo = (GeoObjectTopic) as.getLiveTopic(geoID, 1);
				boolean isForumActivated = geo.isForumActivated();
				session.setAttribute("forumActivition", isForumActivated ? SWITCH_ON : SWITCH_OFF);
				if (isForumActivated) {
					session.setAttribute("commentCount", new Integer(geo.getComments().size()));
				}
				return PAGE_GEO_INFO;
		// TODO show forum if wanted
		} else if (action.equals(ACTION_SHOW_GEO_FORUM)) {
			return PAGE_GEO_FORUM;
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
			GeoObjectTopic geo = getSelectedGeo(session);
			String forumID = geo.getForum().getID();
			String assocID = as.getNewAssociationID();
			cm.createAssociation(assocID, 1, SEMANTIC_FORUM_COMMENTS, 1, forumID, 1, commentID, 1);
			// send notification email
			// sendNotificationEmail(inst.getID(), commentID);
			//
			return PAGE_GEO_FORUM;
		// shape display
		} else if (action.equals(ACTION_TOGGLE_SHAPE_DISPLAY)) {
			String shapeTypeID = params.getValue("typeID");
			toggleShapeDisplay(shapeTypeID, session);
			updateShapes(session);
			return PAGE_CITY_MAP;
		} else if (action.equals(ACTION_SHOW_GEO_FORM)) {
			//should this  be possible from the browseServlet, if so the action has to redirect to edit/GeoLogin or am i wrong? 
			return PAGE_GEO_FORM;
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
			setSelectedGeo(null, session);
		} else if (page.equals(PAGE_GEO_LIST)) {
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
					GeoObjectTopic geo = (GeoObjectTopic) as.getLiveTopic(t);
					// categories
					String critTypeID = getCriteria(0, session).criteria.getID();
					categories.put(geo.getID(), geo.getCategories(critTypeID));
					// address		
					BaseTopic address = geo.getAddress();
					// if no related address put new hashtable in it for property city
					Hashtable addressProps = address != null ?  as.getTopicProperties(address) : new Hashtable(); 
					addressProps.put(PROPERTY_CITY, geo.getCity());
					addresses.put(geo.getID(), addressProps);
				} catch (ClassCastException e) {
					System.out.println("*** BrowseServlet.preparePage(): " + t + ": " + e);
					// ### happens if geo object type is not up to date
				}
			}
			session.setAttribute("categories", categories);
			session.setAttribute("addresses", addresses);
			// clear marker
			setSelectedGeo(null, session);
		} else if (page.equals(PAGE_GEO_FORUM)) {
			session.setAttribute("geoComments", getSelectedGeo(session).getCommentBeans());
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

//	private void sendNotificationEmail(String instID, String commentID) {
//		try {
//			GeoObjectTopic inst = (GeoObjectTopic) as.getLiveTopic(instID, 1);
//			// "from"
//			String from = as.getEmailAddress("t-rootuser");		// ###
//			if (from == null || from.equals("")) {
//				throw new DeepaMehtaException("email address of root user is unknown");
//			}
//			// "to"
//			BaseTopic email = inst.getEmail();
//			if (email == null || email.getName().equals("")) {
//				throw new DeepaMehtaException("email address of \"" + inst.getName() + "\" is unknown");
//			}
//			String to = email.getName();
//			// "subject"
//			String subject = "Kiezatlas: neuer Forumsbeitrag für \"" + inst.getName() + "\"";
//			// "body"
//			Hashtable comment = cm.getTopicData(commentID, 1);
//			String body = "Dies ist eine automatische Benachrichtigung von www.kiezatlas.de\r\r" +
//				"Im Forum der Einrichtung \"" + inst.getName() + "\" wurde ein neuer Kommentar eingetragen:\r\r" +
//				"------------------------------\r" +
//				comment.get(PROPERTY_TEXT) + "\r\r" +
//				"Autor: " + comment.get(PROPERTY_COMMENT_AUTHOR) + "\r" +
//				"Email: " + comment.get(PROPERTY_EMAIL_ADDRESS) + "\r" +
//				"Datum: " + comment.get(PROPERTY_COMMENT_DATE) + "\r" +
//				"Uhrzeit: " + comment.get(PROPERTY_COMMENT_TIME) + "\r" +
//				"------------------------------\r\r" +
//				"Im Falle des Mißbrauchs: In der \"Forum Administration\" ihres persönlichen Kiezatlas-Zugangs haben " +
//				"Sie die Möglichkeit, einzelne Kommentare zu löschen, bzw. das Forum ganz zu deaktivieren.\r" +
//				"www.kiezatlas.de:8080/edit/" + inst.getWebAlias() + "\r\r" +
//				"Mit freundlichen Grüßen\r" +
//				"ihr Kiezatlas-Team";
//			//
//			System.out.println(">>> send notification email");
//			System.out.println("  > SMTP server: \"" + as.getSMTPServer() + "\"");	// as.getSMTPServer() throws DME
//			System.out.println("  > from: \"" + from + "\"");
//			System.out.println("  > to: \"" + to + "\"");
//			// send email
//			EmailTopic.sendMail(as.getSMTPServer(), from, to, subject, body);		// EmailTopic.sendMail() throws DME
//		} catch (Exception e) {
//			System.out.println("*** notification email not send (" + e + ")");
//		}
//	}



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
		SearchCriteria[] criterias = ((CityMapTopic) as.getLiveTopic(getCityMap(session))).getSearchCriterias();	// ### ugly
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

	private void setSelectedGeo(String geoID, Session session) {
		GeoObject geo = geoID != null ? new GeoObject(geoID, getCriterias(session), as) : null;
		session.setAttribute("selectedGeo", geo);
		System.out.println("> \"selectedGeoObject\" stored in session: " + geo);
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
						String targetWebalias = as.getTopicProperty(shapeTopic, PROPERTY_TARGET_WEBALIAS);
						//
						shapes.addElement(new Shape(url, point, size, targetWebalias));
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

	private GeoObjectTopic getSelectedGeo(Session session) {
		return (GeoObjectTopic) as.getLiveTopic(((GeoObject) session.getAttribute("selectedGeo")).geoID, 1);
	}

	private Vector getSelectedCats(Session session) {
		return getCurrentCriteria(session).selectedCategoryIDs;
	}
}
