package de.kiezatlas.deepamehta.topics;

import de.kiezatlas.deepamehta.KiezAtlas;
//
import de.deepamehta.BaseTopic;
import de.deepamehta.DeepaMehtaException;
import de.deepamehta.DeepaMehtaUtils;
import de.deepamehta.PresentableTopic;
import de.deepamehta.PresentableAssociation;
import de.deepamehta.PropertyDefinition;
import de.deepamehta.AmbiguousSemanticException;
import de.deepamehta.service.ApplicationService;
import de.deepamehta.service.CorporateDirectives;
import de.deepamehta.service.CorporateCommands;
import de.deepamehta.service.DeepaMehtaServiceUtils;
import de.deepamehta.service.Session;
import de.deepamehta.topics.LiveTopic;
import de.deepamehta.topics.TypeTopic;
//
import java.io.File;
import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;
import java.awt.geom.Point2D;
import java.awt.*;
import java.util.*;



/**
 * Kiez-Atlas 1.3.3.<br>
 * Requires DeepaMehta 2.0b6-post3.
 * <p>
 * Last functional change: 29.5.2006<br>
 * J&ouml;rg Richter<br>
 * jri@freenet.de
 */
public class ColorTopic extends LiveTopic implements KiezAtlas {



	// *******************
	// *** Constructor ***
	// *******************



	public ColorTopic(BaseTopic topic, ApplicationService as) {
		super(topic, as);
	}



	// **********************
	// *** Defining Hooks ***
	// **********************



	// --------------------------
	// --- Providing Commands ---
	// --------------------------



	// ### Note: the button for the "Color" property is added by superclass LiveTopic



	// ---------------------------
	// --- Handling Properties ---
	// ---------------------------



	public CorporateDirectives propertiesChanged(Hashtable newProps, Hashtable oldProps, String topicmapID,
																					String viewmode, Session session) {
		CorporateDirectives directives = super.propertiesChanged(newProps, oldProps, topicmapID, viewmode, session);
		// --- "Color" ---
		String color = (String) newProps.get(PROPERTY_COLOR);
		if (color != null) {
			System.out.print(">>> \"" + PROPERTY_COLOR + "\" property has changed -- ");
			String iconfile = getIconfile(color);
			File file = new File(FILESERVER_ICONS_PATH + iconfile);
			if (!file.exists()) {
				System.out.println("create new iconfile \"" + iconfile + "\"");
				createIconfile(file, color);
			} else {
				System.out.println("iconfile \"" + iconfile + "\" already exists");
			}
			//
			Hashtable props = new Hashtable();
			props.put(PROPERTY_ICON, iconfile);
			directives.add(DIRECTIVE_SHOW_TOPIC_PROPERTIES, getID(), props, new Integer(1));
		}
		//
		return directives;
	}

	public static Vector hiddenProperties(TypeTopic type) {
		Vector props = new Vector();
		props.addElement(PROPERTY_ICON);
		return props;
	}



	// **********************
	// *** Custom Methods ***
	// **********************



	protected final void createIconfile(File file, String color) {
		// --- create offscreen image ---
		BufferedImage icon = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = icon.createGraphics();
		// ### g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		// --- paint icon ---
		g.setColor(DeepaMehtaUtils.parseHexColor(color));
		g.fillRect(0, 0, IMAGE_SIZE, IMAGE_SIZE);	// ### Mac OS X, Java 1.4: fillRect() following drawing commands are ignored!
		// --- save icon as PNG file ---
		DeepaMehtaServiceUtils.createImageFile(icon, file);
	}

	private String getIconfile(String color) {
		return "color-" + color.substring(1) + ".png";
	}
}
