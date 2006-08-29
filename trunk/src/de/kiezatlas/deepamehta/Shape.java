package de.kiezatlas.deepamehta;

import java.io.Serializable;
import java.awt.Point;



/**
 * Kiez-Atlas 1.3.4<br>
 * Requires DeepaMehta 2.0b7-post1
 * <p>
 * Last change: 26.8.2006<br>
 * J&ouml;rg Richter<br>
 * jri@freenet.de
 */
public class Shape implements Serializable {
	
	public String url;
	public Point point;

	Shape(String url, Point point) {
		this.url = url;
		this.point = point;
	}
}
