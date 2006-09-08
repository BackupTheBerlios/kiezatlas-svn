package de.kiezatlas.deepamehta;

import java.io.Serializable;



/**
 * Kiez-Atlas 1.3.4<br>
 * Requires DeepaMehta 2.0b7-post1
 * <p>
 * Last change: 30.8.2006<br>
 * J&ouml;rg Richter<br>
 * jri@freenet.de
 */
public class ShapeType implements Serializable {
	
	public String typeID;
	public String name;
	public String color;
	public boolean isSelected;

	ShapeType(String typeID, String name, String color, boolean isSelected) {
		this.typeID = typeID;
		this.name = name;
		this.color = color;
		this.isSelected = isSelected;
	}
}
