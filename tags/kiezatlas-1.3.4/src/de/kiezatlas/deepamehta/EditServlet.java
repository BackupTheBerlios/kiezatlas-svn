package de.kiezatlas.deepamehta;

import de.kiezatlas.deepamehta.topics.InstitutionTopic;
//
import de.deepamehta.BaseTopic;
import de.deepamehta.DeepaMehtaException;
import de.deepamehta.service.Session;
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
 * Kiez-Atlas 1.3.1.<br>
 * Requires DeepaMehta 2.0b6.
 * <p>
 * Last functional change: 17.4.2006<br>
 * J&ouml;rg Richter<br>
 * jri@freenet.de
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
				setInstitution(InstitutionTopic.lookupInstitution(alias, as), session);
				return PAGE_INSTITUTION_LOGIN;
			} catch (DeepaMehtaException e) {
				System.out.println("*** EditServlet.performAction(): " + e);
				session.setAttribute("error", e.getMessage());
				return PAGE_ERROR;
			}
		}
		// session timeout?
		if (getInstitution(session) == null) {
			System.out.println("*** Session Expired ***");
			session.setAttribute("error", "Timeout: Kiez-Atlas wurde mehr als " +
				((WebSession) session).session.getMaxInactiveInterval() / 60 + " Minuten nicht benutzt");
			return PAGE_ERROR;
		}
		//
		if (action.equals(ACTION_TRY_LOGIN)) {
			String password = params.getValue(PROPERTY_PASSWORD);
			String instPw = as.getTopicProperty(getInstitution(session), PROPERTY_PASSWORD);
			return password.equals(instPw) ? PAGE_INSTITUTION_HOME : PAGE_INSTITUTION_LOGIN;
			//
		} else if (action.equals(ACTION_SHOW_INSTITUTION_FORM)) {
			return PAGE_INSTITUTION_FORM;
			//
		} else if (action.equals(ACTION_UPDATE_INSTITUTION)) {
			updateTopic(getInstitution(session).getType(), params, session);
			writeImage(params.getUploads());
			return PAGE_INSTITUTION_HOME;
			//
		} else if (action.equals(ACTION_SHOW_FORUM_ADMINISTRATION)) {
			return PAGE_FORUM_ADMINISTRATION;
			//
		} else if (action.equals(ACTION_ACTIVATE_FORUM)) {
			InstitutionTopic inst = getInstitution(session);
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
			BaseTopic forum = getInstitution(session).getForum();
			cm.setTopicData(forum.getID(), 1, PROPERTY_FORUM_ACTIVITION, SWITCH_OFF);
			return PAGE_FORUM_ADMINISTRATION;
			//
		} else if (action.equals(ACTION_DELETE_COMMENT)) {
			String commentID = params.getValue("commentID");
			deleteTopic(commentID);
			return PAGE_FORUM_ADMINISTRATION;
			//
		} else if (action.equals(ACTION_GO_HOME)) {
			return PAGE_INSTITUTION_HOME;
			//
		} else {
			return super.performAction(action, params, session);
		}
	}

	protected void preparePage(String page, RequestParameter params, Session session) {
		if (page.equals(PAGE_INSTITUTION_HOME)) {
			updateImagefile(session);
		} else if (page.equals(PAGE_FORUM_ADMINISTRATION)) {
			InstitutionTopic inst = getInstitution(session);
			boolean isForumActivated = inst.isForumActivated();
			session.setAttribute("activition", isForumActivated ? SWITCH_ON : SWITCH_OFF);
			if (isForumActivated) {
				session.setAttribute("comments", inst.getCommentBeans());
			}
		}
	}



	// *****************
	// *** Utilities ***
	// *****************



	private void writeImage(Vector fileItems) {
		System.out.println(">>> EditServlet.writeImage(): " + fileItems.size() + " files uploaded");
		try {
			Enumeration e = fileItems.elements();
			while (e.hasMoreElements()) {
				FileItem item = (FileItem) e.nextElement();
				String filename = getFilename(item.getName());	// ### explorer sends path
				System.out.println("  > EditServlet.writeImage(): \"" + filename + "\"");
				item.write(new File("/home/jrichter/deepamehta/install/client/images/" + filename));	// ###
				// ### item.write(new File(as.getCorporateWebBaseURL().substring(5) + "images/" + filename));
			}
		} catch (Exception e) {
			System.out.println("*** EditServlet.writeImage(): " + e);
		}
	}

	// ###
	String getFilename(String path) {
		int pos = path.lastIndexOf('\\');
		return pos != -1 ? path.substring(pos + 1) : path;
	}



	// *************************
	// *** Session Utilities ***
	// *************************



	private void setInstitution(BaseTopic inst, Session session) {
		session.setAttribute("inst", inst);
		System.out.println("> \"inst\" stored in session: " + inst);
	}

	private InstitutionTopic getInstitution(Session session) {
		return (InstitutionTopic) as.getLiveTopic((BaseTopic) session.getAttribute("inst"));
	}

	// ---

	// ### compare to class Institution
	private void updateImagefile(Session session) {
		String imageURL = null;
		BaseTopic image = getInstitution(session).getImage();
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
