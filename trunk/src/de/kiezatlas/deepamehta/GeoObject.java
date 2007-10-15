package de.kiezatlas.deepamehta;
import java.io.Serializable;
import java.util.Vector;

import de.deepamehta.service.ApplicationService;
import de.kiezatlas.deepamehta.topics.GeoObjectTopic;
import de.kiezatlas.deepamehta.KiezAtlas;


public class GeoObject implements KiezAtlas, Serializable{
	/**
	 *  Value Object/ Bean
	 *  used as Container for the data Transport between Servlet and JSP 
	 */
	private static final long serialVersionUID = 1L;
	public String geoID, name, webAlias;
	public String yadeX, yadeY;
	public Vector categories;
	
	GeoObject(String newID, SearchCriteria[] criterias, ApplicationService as) {
		GeoObjectTopic geo = (GeoObjectTopic) as.getLiveTopic(newID, 1);
		this.geoID = geo.getID();
		this.name = geo.getName();
		this.webAlias = as.getTopicProperty(geo, PROPERTY_WEB_ALIAS);
		this.yadeX = as.getTopicProperty(geo, PROPERTY_YADE_X);
		this.yadeY = as.getTopicProperty(geo, PROPERTY_YADE_Y);
		this.categories = geo.getCategories(KiezAtlas.TOPICTYPE_KIEZ_GEO);
	}
	
	public String toString() {
		return "\"" + name + "\"";
	}
}
