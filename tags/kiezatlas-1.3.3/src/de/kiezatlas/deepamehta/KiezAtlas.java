package de.kiezatlas.deepamehta;

import de.deepamehta.DeepaMehtaConstants;



/**
 * Kiez-Atlas 1.3.3<br>
 * Requires DeepaMehta 2.0b6-post3
 * <p>
 * Last change: 28.5.2006<br>
 * J&ouml;rg Richter<br>
 * jri@freenet.de
 */
public interface KiezAtlas extends DeepaMehtaConstants {



	// *****************
	// *** Constants ***
	// *****************



	// -----------------
	// --- Workspace ---
	// -----------------



	static final String WORKSPACE_KIEZATLAS = "t-ka-workspace";



	// -------------------
	// --- Topic Types ---
	// -------------------



	static final String TOPICTYPE_CITYMAP = "tt-ka-stadtplan";
	static final String TOPICTYPE_KIEZ_INSTITUTION = "tt-ka-einrichtung";
	static final String TOPICTYPE_KIEZ_INSTITUTION_SEARCH = "tt-ka-einrichtungsuche";
	static final String TOPICTYPE_AGENCY = "tt-ka-traeger";
	static final String TOPICTYPE_CRITERIA = "tt-ka-kriterium";
	static final String TOPICTYPE_YADE_POINT = "tt-ka-yadepoint";
	static final String TOPICTYPE_FORUM = "tt-ka-forum";
	static final String TOPICTYPE_COMMENT = "tt-ka-kommentar";
	static final String TOPICTYPE_OUTLINE_POINT = "tt-ka-outlinepoint";
	static final String TOPICTYPE_COLOR = "tt-ka-color";



	// -------------------------
	// --- Association Types ---
	// -------------------------



	static final String ASSOCTYPE_OUTLINE = "at-ka-outline";



	// -------------------------------------
	// --- Semantic of Association Types ---
	// -------------------------------------



	// direction is from workspace to sub-workspace
	static final String SEMANTIC_SUB_WORKSPACE = ASSOCTYPE_COMPOSITION;

	// direction is from institution to forum
	static final String SEMANTIC_INSTITUTION_FORUM = ASSOCTYPE_ASSOCIATION;

	// direction is from forum to comment
	static final String SEMANTIC_FORUM_COMMENTS = ASSOCTYPE_ASSOCIATION;

	// direction is arbitrary
	static final String SEMANTIC_SHAPE_OUTLINE = ASSOCTYPE_OUTLINE;



	// ------------------
	// --- Properties ---
	// ------------------



	static final String PROPERTY_CITY = "Stadt";
	static final String PROPERTY_OEFFNUNGSZEITEN = "…ffnungszeiten";
	static final String PROPERTY_SONSTIGES = "Sonstiges";
	static final String PROPERTY_AGENCY_KIND = "Art";
	static final String PROPERTY_YADE_X = "YADE x";
	static final String PROPERTY_YADE_Y = "YADE y";
	//
	static final String PROPERTY_FORUM_ACTIVITION = "Aktivierung";
	static final String PROPERTY_COMMENT_AUTHOR = "Autor";
	static final String PROPERTY_COMMENT_DATE = "Datum";
	static final String PROPERTY_COMMENT_TIME = "Uhrzeit";
	//
	static final String PROPERTY_ORIGINAL_BACKGROUND_IMAGE = "Original Background Image";



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
	//
    static final String ITEM_FILL_SHAPE = "Fill";
    static final String  CMD_FILL_SHAPE = "fillShape";
	//
    static final String ITEM_CHOOSE_FILL_COLOR = "Choose Color";
    static final String  CMD_CHOOSE_FILL_COLOR = "chooseFillColor";
	//
    static final String ITEM_UNDO_ALL_FILLINGS = "Undo all Fillings";
    static final String  CMD_UNDO_ALL_FILLINGS = "undoAllFillings";



	// *********************
	// *** Web Constants ***
	// *********************



	// -------------
	// --- Icons ---
	// -------------



	public static final String ICON_HOTSPOT = "redball-middle.gif";



	// ---------------
	// --- Actions ---
	// ---------------



	// browse servlet
	public static final String ACTION_INIT_FRAME = "initFrame";
	public static final String ACTION_SHOW_CATEGORIES = "showCategories";
	public static final String ACTION_SHOW_INSTITUTION_INFO = "showInstInfo";
	public static final String ACTION_SEARCH = "search";
	public static final String ACTION_SEARCH_BY_CATEGORY = "searchByCategory";
	public static final String ACTION_SELECT_CATEGORY = "selectCategory";
	public static final String ACTION_SHOW_INSTITUTION_FORUM = "showInstForum";
	public static final String ACTION_SHOW_COMMENT_FORM = "showCommentForm";
	public static final String ACTION_CREATE_COMMENT = "createComment";
	// edit servlet
	public static final String ACTION_TRY_LOGIN = "tryLogin";		// also used for list servlet
	public static final String ACTION_SHOW_INSTITUTION_FORM = "showInstForm";
	public static final String ACTION_UPDATE_INSTITUTION = "updateInst";
	public static final String ACTION_SHOW_FORUM_ADMINISTRATION = "showForumAdmin";
	public static final String ACTION_ACTIVATE_FORUM = "activateForum";
	public static final String ACTION_DEACTIVATE_FORUM = "deactivateForum";
	public static final String ACTION_DELETE_COMMENT = "deleteComment";
	public static final String ACTION_GO_HOME = "goHome";
	// list servlet
	public static final String ACTION_SHOW_INSTITUTIONS = "showInstitutions";



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
    static final String PAGE_INSTITUTION_LIST = "InstitutionList";
    static final String PAGE_INSTITUTION_INFO = "InstitutionInfo";
    static final String PAGE_INSTITUTION_FORUM = "InstitutionForum";
    static final String PAGE_COMMENT_FORM = "CommentForm";
    // edit
    static final String PAGE_INSTITUTION_LOGIN = "InstitutionLogin";
    static final String PAGE_INSTITUTION_HOME = "InstitutionHome";
    static final String PAGE_INSTITUTION_FORM = "InstitutionForm";
    static final String PAGE_FORUM_ADMINISTRATION = "ForumAdministration";
    // list
    static final String PAGE_LIST_LOGIN = "ListLogin";
    static final String PAGE_LIST_HOME = "ListHome";
    static final String PAGE_LIST = "List";
	// error
    static final String PAGE_ERROR = "error";
}
