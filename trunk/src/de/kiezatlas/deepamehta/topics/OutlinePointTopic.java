package de.kiezatlas.deepamehta.topics;

import de.kiezatlas.deepamehta.KiezAtlas;
//
import de.deepamehta.BaseTopic;
import de.deepamehta.BaseAssociation;
import de.deepamehta.Commands;
import de.deepamehta.DeepaMehtaException;
import de.deepamehta.DeepaMehtaUtils;
import de.deepamehta.PresentableTopic;
import de.deepamehta.PropertyDefinition;
import de.deepamehta.service.ApplicationService;
import de.deepamehta.service.CorporateDirectives;
import de.deepamehta.service.CorporateCommands;
import de.deepamehta.service.DeepaMehtaServiceUtils;
import de.deepamehta.service.Session;
import de.deepamehta.topics.LiveTopic;
//
import java.io.File;
import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;
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
public class OutlinePointTopic extends LiveTopic implements KiezAtlas {



	// *******************
	// *** Constructor ***
	// *******************



	public OutlinePointTopic(BaseTopic topic, ApplicationService as) {
		super(topic, as);
	}



	// **********************
	// *** Defining Hooks ***
	// **********************



	// --------------------------
	// --- Providing Commands ---
	// --------------------------



	public CorporateCommands contextCommands(String topicmapID, String viewmode,
								Session session, CorporateDirectives directives) {
		CorporateCommands commands = new CorporateCommands(as);
		int editorContext = as.editorContext(topicmapID);
		//
		commands.addNavigationCommands(this, editorContext, session);
		commands.addSeparator();
		//
		// --- "Fill" ---
		BaseTopic selectedColor = getSelectedColor(topicmapID);
		int cmdState = selectedColor == null ? COMMAND_STATE_DISABLED : COMMAND_STATE_DEFAULT;
		commands.addCommand(ITEM_FILL_SHAPE, CMD_FILL_SHAPE, cmdState);
		// --- "Choose Color" ---
		Vector colors = cm.getTopics(TOPICTYPE_COLOR);
		Commands colorGroup = commands.addCommandGroup(ITEM_CHOOSE_FILL_COLOR, FILESERVER_ICONS_PATH, "color.gif");
		commands.addTopicCommands(colorGroup, colors, CMD_CHOOSE_FILL_COLOR, COMMAND_STATE_RADIOBUTTON,
			selectedColor, null, session, directives);	// title=null
		//
		// --- standard topic commands ---
		commands.addStandardCommands(this, editorContext, viewmode, session, directives);
		//
		return commands;
	}



	// --------------------------
	// --- Executing Commands ---
	// --------------------------



	public CorporateDirectives executeCommand(String command, Session session, String topicmapID, String viewmode) {
		CorporateDirectives directives = new CorporateDirectives();
		StringTokenizer st = new StringTokenizer(command, ":");
		String cmd = st.nextToken();
		//
		if (cmd.equals(CMD_FILL_SHAPE)) {
			fillPolygon(topicmapID, directives);
		} else if (cmd.equals(CMD_CHOOSE_FILL_COLOR)) {
			String colorID = st.nextToken();
			chooseColor(topicmapID, colorID, directives);
		} else {
			return super.executeCommand(command, session, topicmapID, viewmode);
		}
		return directives;
	}



	// -----------------------------
	// --- Handling Associations ---
	// -----------------------------



	public String associationAllowed(String assocTypeID, String relTopicID, CorporateDirectives directives) {
		return SEMANTIC_SHAPE_OUTLINE;
	}



	// **********************
	// *** Custom Methods ***
	// **********************



	private void fillPolygon(String topicmapID, CorporateDirectives directives) {
		try {
			String mapName = getProperty(topicmapID, 1, PROPERTY_BACKGROUND_IMAGE);
			String mapFile = FILESERVER_BACKGROUNDS_PATH + mapName;
			// Note: ImageIcon is a kluge to make sure the image is fully loaded before we proceed
			Image image = new ImageIcon(Toolkit.getDefaultToolkit().createImage(mapFile)).getImage();
			int width = image.getWidth(null);
			int height = image.getHeight(null);
			System.out.println(">>> OutlinePointTopic.fillPolygon(): image \"" + mapFile + "\" size= " + width + " x " + height);
			// --- generate offscreen image ---
			BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = bufferedImage.createGraphics();
			String colorCode = getProperty(getSelectedColor(topicmapID), PROPERTY_COLOR);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.drawImage(image, 0, 0, null);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
			g.setColor(DeepaMehtaUtils.parseHexColor(colorCode));
			g.fill(createPolygon(topicmapID));
			// --- decide which file to write ---
			File fileToWrite;
			if (isMapColored(topicmapID)) {
				fileToWrite = new File(mapFile);
				//
				directives.add(DIRECTIVE_SET_EDITOR_BGIMAGE, topicmapID, mapName);
			} else {
				String coloredMapName = getColoredMapName(topicmapID);
				fileToWrite = new File(FILESERVER_BACKGROUNDS_PATH + coloredMapName);
				//
				Hashtable props = new Hashtable();
				props.put(PROPERTY_BACKGROUND_IMAGE, coloredMapName);
				props.put(PROPERTY_ORIGINAL_BACKGROUND_IMAGE, mapName);
				directives.add(DIRECTIVE_SHOW_TOPIC_PROPERTIES, topicmapID, props, new Integer(1));
			}
			// --- save image as jpg file ---
			DeepaMehtaServiceUtils.createImageFile(bufferedImage, fileToWrite);
		} catch (DeepaMehtaException e) {
			System.out.println("*** OutlinePointTopic.fillPolygon(): " + e);
			directives.add(DIRECTIVE_SHOW_MESSAGE, "Fehler beim Einfärben: " + e.getMessage(),
				new Integer(NOTIFICATION_WARNING));
		}
	}

	/**
	 * @return	the polygon this outline point is a part of
	 */
	private Polygon createPolygon(String topicmapID) {
		Polygon polygon = new Polygon();
		String currentPoint = getID();
		String behindPoint = null;
		//
		do {
			Vector points = cm.getRelatedViewTopics(topicmapID, 1, currentPoint, SEMANTIC_SHAPE_OUTLINE,
																					TOPICTYPE_OUTLINE_POINT);
			// an outline point must have 2 neighboured outline points
			if (points.size() < 2) {
				throw new DeepaMehtaException("der Umriss ist nicht geschlossen");
			} else if (points.size() > 2) {
				throw new DeepaMehtaException("der Umriss ist nicht eindeutig");
			}
			// decide which point to add
			int i;
			if (behindPoint == null) {
				// traversal starts. Direction is arbitraty.
				i = 0;
			} else {
				if (((PresentableTopic) points.get(0)).getID().equals(behindPoint)) {
					i = 1;
				} else if (((PresentableTopic) points.get(1)).getID().equals(behindPoint)) {
					i = 0;
				} else {
					throw new DeepaMehtaException("internal error while createPolygon()");
				}
			}
			// add point to polygon
			PresentableTopic point = (PresentableTopic) points.get(i);
			Point p = point.getGeometry();
			polygon.addPoint(p.x, p.y);
			// proceed to next point
			behindPoint = currentPoint;
			currentPoint = point.getID();
			//
		} while (!currentPoint.equals(getID()));
		//
		System.out.println("  > polygon has " + polygon.npoints + " vertex points");
		return polygon;
	}

	private void chooseColor(String topicmapID, String colorID, CorporateDirectives directives) {
		// delete assignment, if exists
		BaseTopic selectedColor = getSelectedColor(topicmapID);
		if (selectedColor != null) {
			BaseAssociation assoc = cm.getAssociation(SEMANTIC_PREFERENCE, topicmapID, selectedColor.getID());
			directives.add(DIRECTIVE_HIDE_ASSOCIATION, assoc.getID(), Boolean.TRUE, topicmapID);
		}
		// create new assignment
		cm.createAssociation(as.getNewAssociationID(), 1, SEMANTIC_PREFERENCE, 1, topicmapID, 1, colorID, 1);
	}

	/**
	 * Returns the color selected for the specified city map, or <code>null</code> if no color is selected
	 *
	 * @return	the selected color as a <code>BaseTopic</code> of type <code>TOPICTYPE_COLOR</code>
	 */
	private BaseTopic getSelectedColor(String topicmapID) {
		return as.getRelatedTopic(topicmapID, SEMANTIC_PREFERENCE, TOPICTYPE_COLOR, 2, true);	// emptyAllowed=true
	}

	private boolean isMapColored(String topicmapID) {
		return ((CityMapTopic) as.getLiveTopic(topicmapID, 1)).isMapColored();
	}

	private String getColoredMapName(String topicmapID) {
		String imagefile = getProperty(topicmapID, 1, PROPERTY_BACKGROUND_IMAGE);
		return imagefile.substring(0, imagefile.lastIndexOf('.')) + "-colored.jpg";
	}
}
