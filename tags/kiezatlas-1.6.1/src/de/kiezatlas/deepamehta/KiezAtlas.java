package de.kiezatlas.deepamehta;

import de.deepamehta.DeepaMehtaConstants;



/**
 * Kiezatlas 1.6.1<br>
 * Requires DeepaMehta 2.0b8.
 * <p>
 * Last change: 11.8.2008<br>
 * J&ouml;rg Richter<br>
 * jri@deepamehta.de
 */
public interface KiezAtlas extends DeepaMehtaConstants {



	// *****************
	// *** Constants ***
	// *****************



	// -------------------
	// --- Preferences ---
	// -------------------



	static final int SHAPE_ALPHA = 128;			// 0-transparent ... 255-opaque
	static final int SHAPE_LEGEND_HEIGHT = 17;	// in pixel



	// -----------------
	// --- Workspace ---
	// -----------------



	static final String WORKSPACE_KIEZATLAS = "t-ka-workspace";



	// -------------------
	// --- Topic Types ---
	// -------------------



	static final String TOPICTYPE_CITYMAP = "tt-ka-stadtplan";
	static final String TOPICTYPE_KIEZ_GEO = "tt-ka-geoobject";
	static final String TOPICTYPE_KIEZ_GEO_SEARCH = "tt-ka-geoobject-search";
	static final String TOPICTYPE_AGENCY = "tt-ka-traeger";
	static final String TOPICTYPE_CRITERIA = "tt-ka-kriterium";
	static final String TOPICTYPE_YADE_POINT = "tt-ka-yadepoint";
	static final String TOPICTYPE_FORUM = "tt-ka-forum";
	static final String TOPICTYPE_COMMENT = "tt-ka-kommentar";
	static final String TOPICTYPE_OUTLINE_POINT = "tt-ka-outlinepoint";
	static final String TOPICTYPE_SHAPE = "tt-ka-shape";
	static final String TOPICTYPE_STYLESHEET = "tt-ka-stylesheet";



	// -------------------------
	// --- Association Types ---
	// -------------------------



	static final String ASSOCTYPE_OUTLINE = "at-ka-outline";
	static final String ASSOCTYPE_HOMEPAGE_LINK = "at-ka-homepage-link";
	static final String ASSOCTYPE_IMPRESSUM_LINK = "at-ka-impressum-link";



	// -------------------------------------
	// --- Semantic of Association Types ---
	// -------------------------------------



	// direction is from workspace to sub-workspace
	static final String SEMANTIC_SUB_WORKSPACE = ASSOCTYPE_COMPOSITION;

	// direction is from institution to forum
	static final String SEMANTIC_INSTITUTION_FORUM = ASSOCTYPE_ASSOCIATION;

	// direction is from forum to comment
	static final String SEMANTIC_FORUM_COMMENTS = ASSOCTYPE_ASSOCIATION;

	// direction is from workspace to shape-subtype
	static final String SEMANTIC_WORKSPACE_SHAPETYPE = ASSOCTYPE_ASSOCIATION;

	// direction is from workspace to stylesheet
	static final String SEMANTIC_WORKSPACE_STYLESHEET = ASSOCTYPE_ASSOCIATION;

	// direction is from workspace to image
	static final String SEMANTIC_WORKSPACE_SITELOGO = ASSOCTYPE_ASSOCIATION;

	// direction is from workspace to webpage
	static final String SEMANTIC_WORKSPACE_HOMEPAGELINK = ASSOCTYPE_HOMEPAGE_LINK;

	// direction is from workspace to webpage
	static final String SEMANTIC_WORKSPACE_IMPRESSUMLINK = ASSOCTYPE_IMPRESSUM_LINK;

	// direction is arbitrary
	static final String SEMANTIC_SHAPE_OUTLINE = ASSOCTYPE_OUTLINE;



	// ------------------
	// --- Properties ---
	// ------------------



	static final String PROPERTY_CITY = "Stadt";
	static final String PROPERTY_OEFFNUNGSZEITEN = "Öffnungszeiten";
	static final String PROPERTY_SONSTIGES = "Sonstiges";
	static final String PROPERTY_ADMINISTRATION_INFO = "Administrator Infos";
	static final String PROPERTY_AGENCY_KIND = "Art";
	static final String PROPERTY_YADE_X = "YADE x";
	static final String PROPERTY_YADE_Y = "YADE y";
	static final String PROPERTY_LAST_MODIFIED = "Zuletzt geändert";
	//
	static final String PROPERTY_FORUM_ACTIVITION = "Aktivierung";
	static final String PROPERTY_COMMENT_AUTHOR = "Autor";
	static final String PROPERTY_COMMENT_DATE = "Datum";
	static final String PROPERTY_COMMENT_TIME = "Uhrzeit";
	//
	static final String PROPERTY_TARGET_WEBALIAS = "Target Web Alias";
	//
	static final String PROPERTY_CSS = "CSS";



	// -----------------------
	// --- Property Values ---
	// -----------------------



	static final String AGENCY_KIND_KOMMUNAL = "kommunal";
	static final String AGENCY_KIND_FREI = "frei";



	// ----------------
	// --- Commands ---
	// ----------------



    static final String ITEM_LOCK_GEOMETRY = "Lock";
    static final String  CMD_LOCK_GEOMETRY = "lockGeometry";
	//
    static final String ITEM_UNLOCK_GEOMETRY = "Unlock";
    static final String  CMD_UNLOCK_GEOMETRY = "unlockGeometry";
	//
    static final String ITEM_REPOSITION_ALL = "Reposition all";
    static final String  CMD_REPOSITION_ALL = "repositionAll";
    static final String ICON_REPOSITION_ALL = "location.png";
	//
    static final String ITEM_MAKE_SHAPE = "Make Shape";
    static final String  CMD_MAKE_SHAPE = "makeShape";



	// *********************
	// *** Web Constants ***
	// *********************



	// ----------------
	// --- Servlets ---
	// ----------------



	public static final int SERVLET_BROWSE = 1;
	public static final int SERVLET_EDIT = 2;
	public static final int SERVLET_LIST = 3;
	public static final int SERVLET_UPLOAD = 4;



	// -------------
	// --- Icons ---
	// -------------



	public static final String ICON_HOTSPOT = "redball-middle.gif";
	public static final String ICON_CLUSTER = "redball-bigger.gif";



	// ---------------
	// --- Actions ---
	// ---------------



	// browse servlet
	public static final String ACTION_INIT_FRAME = "initFrame";
	public static final String ACTION_SHOW_CATEGORIES = "showCategories";
	public static final String ACTION_SHOW_INFO_EXTERNAL = "showInfo";
	public static final String ACTION_SHOW_GEO_INFO = "showGeoObjectInfo";
	public static final String ACTION_SEARCH = "search";		
	public static final String ACTION_SEARCH_BY_CATEGORY = "searchByCategory";
	public static final String ACTION_SELECT_CATEGORY = "selectCategory";
	public static final String ACTION_SHOW_GEO_FORUM = "showGeoObjectForum";
	public static final String ACTION_SHOW_COMMENT_FORM = "showCommentForm";
	public static final String ACTION_CREATE_COMMENT = "createComment";
	public static final String ACTION_TOGGLE_SHAPE_DISPLAY = "toggleShapeDisplay";
	// edit servlet
	public static final String ACTION_TRY_LOGIN = "tryLogin";				// also used for list servlet
	public static final String ACTION_SHOW_GEO_FORM = "showGeoObjectForm";	// also used for list servlet
	public static final String ACTION_UPDATE_GEO = "updateGeo";				// also used for list servlet
	public static final String ACTION_GO_HOME = "goHome";					// also used for list servlet
	public static final String ACTION_SHOW_FORUM_ADMINISTRATION = "showForumAdmin";
	public static final String ACTION_ACTIVATE_FORUM = "activateForum";
	public static final String ACTION_DEACTIVATE_FORUM = "deactivateForum";
	public static final String ACTION_DELETE_COMMENT = "deleteComment";
	// list servlet
	public static final String ACTION_SHOW_INSTITUTIONS = "showInstitutions";
	public static final String ACTION_SHOW_EMPTY_GEO_FORM = "showEmptyGeoObjectForm";
	public static final String ACTION_CREATE_GEO = "createGeo";



	// --------------------
	// --- Search Modes ---
	// --------------------



    static final String SEARCHMODE_BY_NAME = "byName";
    // Note: other search modes are generated dynamically: "0", "1", "2", ....



	// --------------
	// --- Frames ---
	// --------------



    static final String FRAME_LEFT = "left";
    static final String FRAME_RIGHT = "right";



	// -------------
	// --- Pages ---
	// -------------



	// browse
    static final String PAGE_FRAMESET = "frameset";
    static final String PAGE_CITY_MAP = "CityMap";
    static final String PAGE_CATEGORY_LIST = "CategoryList";
    static final String PAGE_GEO_LIST = "GeoObjectList";
    static final String PAGE_GEO_INFO = "GeoObjectInfo";
    static final String PAGE_GEO_FORUM = "GeoObjectForum";
    static final String PAGE_COMMENT_FORM = "CommentForm";
    // edit
    static final String PAGE_GEO_LOGIN = "GeoObjectLogin";
    static final String PAGE_GEO_HOME = "GeoObjectHome";
    static final String PAGE_GEO_FORM = "GeoObjectForm";
    static final String PAGE_FORUM_ADMINISTRATION = "ForumAdministration";
    // list
    static final String PAGE_LIST_LOGIN = "ListLogin";
    static final String PAGE_LIST_HOME = "ListHome";
    static final String PAGE_LIST = "List";
    static final String PAGE_GEO_ADMIN_FORM = "GeoObjectAdminForm";
    static final String PAGE_GEO_EMPTY_FORM = "GeoObjectEmptyForm";
	// error
    static final String PAGE_ERROR = "error";
}
