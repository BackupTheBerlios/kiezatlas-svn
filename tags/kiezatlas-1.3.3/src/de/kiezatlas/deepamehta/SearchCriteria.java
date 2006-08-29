package de.kiezatlas.deepamehta;

import de.deepamehta.topics.TypeTopic;
//
import java.io.Serializable;
import java.util.Vector;



/**
 * Kiez-Atlas 1.2.3.<br>
 * Requires DeepaMehta 2.0b4.
 * <p>
 * Last functional change: 6.4.2005<br>
 * J&ouml;rg Richter<br>
 * jri@freenet.de
 */
public class SearchCriteria implements Serializable {
	
	public TypeTopic criteria;
	Vector selectedCategoryIDs;

	SearchCriteria(TypeTopic criteria, Vector selectedCategoryIDs) {
		this.criteria = criteria;
		this.selectedCategoryIDs = selectedCategoryIDs;
	}
}
