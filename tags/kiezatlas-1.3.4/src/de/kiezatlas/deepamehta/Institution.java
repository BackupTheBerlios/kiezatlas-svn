package de.kiezatlas.deepamehta;

import de.kiezatlas.deepamehta.topics.InstitutionTopic;
//
import de.deepamehta.BaseTopic;
import de.deepamehta.DeepaMehtaUtils;
import de.deepamehta.service.ApplicationService;
//
import java.io.Serializable;
import java.util.*;



/**
 * A bean-like data container for passing data from the front-controler (servlet)
 * to the presentation layer (JSP engine).
 * <p>
 * Kiez-Atlas 1.3<br>
 * Requires DeepaMehta 2.0b6
 * <p>
 * Last change: 19.2.2006<br>
 * J&ouml;rg Richter<br>
 * jri@freenet.de
 */
public class Institution implements KiezAtlas, Serializable {

	public String id, name, webAlias;
	public String street, postalCode, city;
	public String webpageURL, person, phone, fax, email;
	public String traeger, traegerArt;
	public String open, misc;
	public String imageURL;
	public String yadeX, yadeY;
	public Vector[] categories;		// may be null

	Institution(String instID, ApplicationService as) {
		this(instID, null, as);
	}

	/**
	 * @param	criterias	may be null
	 *
	 * @see		BrowseServlet#setSelectedInst
	 */
	Institution(String instID, SearchCriteria[] criterias, ApplicationService as) {
		InstitutionTopic inst = (InstitutionTopic) as.getLiveTopic(instID, 1);
		id = inst.getID();
		name = inst.getName();
		webAlias = as.getTopicProperty(inst, PROPERTY_WEB_ALIAS);
		// address
		BaseTopic address = inst.getAddress();
		if (address != null) {
			street = as.getTopicProperty(address, PROPERTY_STREET);
			postalCode = as.getTopicProperty(address, PROPERTY_POSTAL_CODE);
		} else {
			street = "";
			postalCode = "";
		}
		city = as.getTopicProperty(inst, PROPERTY_CITY);
		// contact
		BaseTopic w = inst.getWebpage();
		BaseTopic p = inst.getPerson();
		BaseTopic ph = inst.getPhone();
		BaseTopic f = inst.getFax();
		BaseTopic e = inst.getEmail();
		webpageURL = w != null ? w.getName() : "";
		person = p != null ? p.getName() : "";
		phone = ph != null ? ph.getName() : "";
		fax = f != null ? f.getName() : "";
		email = e != null ? e.getName() : "";
		// traeger
		BaseTopic t = inst.getTraeger();
		if (t != null) {
			traeger = t.getName();
			traegerArt = as.getTopicProperty(t, PROPERTY_AGENCY_KIND);
		} else {
			traeger = "";
			traegerArt = "";
		}
		//
		open = as.getTopicProperty(inst, PROPERTY_OEFFNUNGSZEITEN);
		misc = as.getTopicProperty(inst, PROPERTY_SONSTIGES);
		open = DeepaMehtaUtils.replace(open, '\r', "<br>");		// needed for "Multiline Input Field"
		misc = DeepaMehtaUtils.replace(misc, '\r', "<br>");		// needed for "Multiline Input Field"		
		// image
		BaseTopic image = inst.getImage();
		if (image != null) {
			String imagefile = as.getTopicProperty(image, PROPERTY_FILE);
			if (imagefile.length() > 0) {
				imageURL = as.getCorporateWebBaseURL() + FILESERVER_IMAGES_PATH + imagefile;
			}
		}
		// yade
		yadeX = as.getTopicProperty(inst, PROPERTY_YADE_X);
		yadeY = as.getTopicProperty(inst, PROPERTY_YADE_Y);
		// categories
		if (criterias != null) {
			categories = new Vector[criterias.length];
			for (int i = 0; i < criterias.length; i++) {
				String critTypeID = criterias[i].criteria.getID();
				categories[i] = inst.getCategories(critTypeID);
			}
		}
	}

	public String toString() {
		return "\"" + name + "\"";
	}
}
