package de.kiezatlas.deepamehta;

import de.deepamehta.DeepaMehtaUtils;
import de.deepamehta.service.ApplicationService;
//
import java.io.Serializable;



/**
 * A bean-like data container for passing data from the front-controler (servlet)
 * to the presentation layer (JSP engine).
 * <p>
 * Kiez-Atlas 1.3.2<br>
 * Requires DeepaMehta 2.0b6
 * <p>
 * Last change: 18.5.2006<br>
 * J&ouml;rg Richter<br>
 * jri@freenet.de
 */
public class Comment implements KiezAtlas, Serializable {

	public String id, text, author, email, date, time;

	public Comment(String commentID, ApplicationService as) {
		id = commentID;
		text = as.getTopicProperty(commentID, 1, PROPERTY_TEXT);
		text = DeepaMehtaUtils.replace(text, '\r', "<br>");		// needed for "Multiline Input Field"
		author = as.getTopicProperty(commentID, 1, PROPERTY_COMMENT_AUTHOR);
		email = as.getTopicProperty(commentID, 1, PROPERTY_EMAIL_ADDRESS);
		date = as.getTopicProperty(commentID, 1, PROPERTY_COMMENT_DATE);
		time = as.getTopicProperty(commentID, 1, PROPERTY_COMMENT_TIME);
	}
}
