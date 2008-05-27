package de.kiezatlas.deepamehta;

import de.kiezatlas.deepamehta.topics.GeoObjectTopic;
//
import de.deepamehta.BaseTopic;
import de.deepamehta.DeepaMehtaException;
import de.deepamehta.service.Session;
import de.deepamehta.service.TopicBean;
import de.deepamehta.service.web.DeepaMehtaServlet;
import de.deepamehta.service.web.RequestParameter;
import de.deepamehta.service.web.WebSession;
//
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.*;
//
import org.apache.commons.fileupload.FileItem;


/**
 * Kiez-Atlas 1.5<br>
 * Requires DeepaMehta 2.0b8.
 * <p>
 * Last functional change: 15.11.2007<br>
 * Last change: 15.10.2007<br>
 * Malte Rei&szlig;ig<br>
 * mre@deepamehta.de
 */
public class EditServlet extends DeepaMehtaServlet implements KiezAtlas {

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
				setGeoObject(GeoObjectTopic.lookupInstitution(alias, as), session);
				return PAGE_GEO_LOGIN;
			} catch (DeepaMehtaException e) {
				System.out.println("*** EditServlet.performAction(): " + e);
				session.setAttribute("error", e.getMessage());
				return PAGE_ERROR;
			}
		}
		// session timeout?
		if (getGeoObject(session) == null) {	// ### doesn't return null but throws exception!
			System.out.println("*** Session Expired ***");
			session.setAttribute("error", "Timeout: Kiez-Atlas wurde mehr als " +
				((WebSession) session).session.getMaxInactiveInterval() / 60 + " Minuten nicht benutzt");
			return PAGE_ERROR;
		}
		//
		if (action.equals(ACTION_TRY_LOGIN)) {
			GeoObjectTopic geo = getGeoObject(session);
			String password = params.getValue(PROPERTY_PASSWORD);
			String geoPw = as.getTopicProperty(geo, PROPERTY_PASSWORD);
			TopicBean topicBean = as.createTopicBean(geo.getID(), 1);
			session.setAttribute("topicBean", topicBean);
			return password.equals(geoPw) ? PAGE_GEO_HOME : PAGE_GEO_LOGIN;
			//
		} else if (action.equals(ACTION_SHOW_GEO_FORM)) {
			return PAGE_GEO_FORM;
			//
		} else if (action.equals(ACTION_UPDATE_GEO)) {
			GeoObjectTopic geo = getGeoObject(session);
			updateTopic(geo.getType(), params, session);
			TopicBean topicBean = as.createTopicBean(geo.getID(), 1);
			session.setAttribute("topicBean", topicBean);
			String newFilename = writeImage(params.getUploads());
			if (newFilename != null) {
				as.setTopicProperty(geo.getImage(), PROPERTY_FILE, newFilename);
			}
			return PAGE_GEO_HOME;
			//
		} else if (action.equals(ACTION_SHOW_FORUM_ADMINISTRATION)) {
			return PAGE_FORUM_ADMINISTRATION;
			//
		} else if (action.equals(ACTION_ACTIVATE_FORUM)) {
			GeoObjectTopic inst = getGeoObject(session);
			// create forum topic if not yet exist
			BaseTopic forum = inst.getForum();
			String forumID;
			if (forum == null) {
				forumID = as.getNewTopicID();
				cm.createTopic(forumID, 1, TOPICTYPE_FORUM, 1, "");
				String assocID = as.getNewAssociationID();
				cm.createAssociation(assocID, 1, SEMANTIC_INSTITUTION_FORUM, 1, inst.getID(), 1, forumID, 1);
			} else {
				forumID = forum.getID();
			}
			// activate forum
			cm.setTopicData(forumID, 1, PROPERTY_FORUM_ACTIVITION, SWITCH_ON);
			return PAGE_FORUM_ADMINISTRATION;
			//
		} else if (action.equals(ACTION_DEACTIVATE_FORUM)) {
			// deactivate forum
			BaseTopic forum = getGeoObject(session).getForum();
			cm.setTopicData(forum.getID(), 1, PROPERTY_FORUM_ACTIVITION, SWITCH_OFF);
			return PAGE_FORUM_ADMINISTRATION;
			//
		} else if (action.equals(ACTION_DELETE_COMMENT)) {
			String commentID = params.getValue("commentID");
			deleteTopic(commentID);
			return PAGE_FORUM_ADMINISTRATION;
			//
		} else if (action.equals(ACTION_GO_HOME)) {
			GeoObjectTopic geo = getGeoObject(session);
			// error check
			// System.out.println("geo is: " + geo.getID());
			if (geo != null) {
				TopicBean topicBean = as.createTopicBean(geo.getID(), 1);
				session.setAttribute("topicBean", topicBean);
			} else {
				System.out.println(">>>> Could not retrieve GeoObjectTopic from Session");
			}
			//
			return PAGE_GEO_HOME;
			//
		} else {
			return super.performAction(action, params, session);
		}
	}

	protected void preparePage(String page, RequestParameter params, Session session) {
		if (page.equals(PAGE_GEO_HOME)) {
			//GeoObjectTopic geoId = (BaseTopic) session.getAttribute("geo");
			
			updateImagefile(session);
		} else if (page.equals(PAGE_FORUM_ADMINISTRATION)) {
			GeoObjectTopic geo = getGeoObject(session);
			boolean isForumActivated = geo.isForumActivated();
			session.setAttribute("activition", isForumActivated ? SWITCH_ON : SWITCH_OFF);
			if (isForumActivated) {
				session.setAttribute("comments", geo.getCommentBeans());
			}
		}
	}



	// *****************
	// *** Utilities ***
	// *****************



	static String writeImage(Vector fileItems) {
		try {
			System.out.println(">>> EditServlet.writeImage(): " + fileItems.size() + " files uploaded");
			if (fileItems.size() > 0) {
				String path = "/home/jrichter/deepamehta/install/client/images/";	// ### hardcoded
				// ### String path = "/Users/jri/Projects/DeepaMehta/trunk/install/client/images/";
				FileItem item = (FileItem) fileItems.firstElement();
				String filename = getFilename(item.getName());	// ### explorer includes entire path
				System.out.println("  > filename=\"" + filename + "\"");
				File fileToWrite = new File(path + filename);
				// find new filename if already exists
				int copyCount = 0;
				String newFilename = null;
				int pos = filename.lastIndexOf('.');
				while (fileToWrite.exists()) {
					copyCount++;
					newFilename = filename.substring(0, pos) + "-" + copyCount + filename.substring(pos);
					fileToWrite = new File(path + newFilename);
					System.out.println("  > file already exists, try \"" + newFilename + "\"");
				}
				//
				item.write(fileToWrite);
				// ### item.write(new File(as.getCorporateWebBaseURL().substring(5) + "images/" + filename));
				System.out.println("  > file \"" + fileToWrite + "\" written successfully");
				if (copyCount > 0) {
					return newFilename;
				}
			}
		} catch (Exception e) {
			System.out.println("*** EditServlet.writeImage(): " + e);
		}
		return null;
	}

	// ###
	static String getFilename(String path) {
		int pos = path.lastIndexOf('\\');
		return pos != -1 ? path.substring(pos + 1) : path;
	}



	// *************************
	// *** Session Utilities ***
	// *************************



	private void setGeoObject(BaseTopic geo, Session session) {
		session.setAttribute("geo", geo);
		System.out.println("> \"geo\" stored in session: " + geo);
	}

	private GeoObjectTopic getGeoObject(Session session) {
		return (GeoObjectTopic) as.getLiveTopic((BaseTopic) session.getAttribute("geo"));
	}

	// ---

	// ### compare to class Institution
	private void updateImagefile(Session session) {
		String imageURL = null;
		BaseTopic image = getGeoObject(session).getImage();
		if (image != null) {
			String imagefile = as.getTopicProperty(image, PROPERTY_FILE);
			if (imagefile.length() > 0) {
				imageURL = as.getCorporateWebBaseURL() + FILESERVER_IMAGES_PATH + imagefile;
			}
		}
		session.setAttribute("imagefile", imageURL);
		System.out.println("> \"imagefile\" stored in session: " + imageURL);
	}
}
