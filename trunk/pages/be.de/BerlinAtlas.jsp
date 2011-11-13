<%@ include file="../KiezAtlas.jsp" %>
<%
	BaseTopic map = (BaseTopic) session.getAttribute("map");
	BaseTopic workspace = (BaseTopic) session.getAttribute("workspace");
	String mapTopics = (String) session.getAttribute("mapTopics");
  String workspaceCriterias = (String) session.getAttribute("workspaceCriterias");
  String workspaceHomepage = (String) session.getAttribute("workspaceHomepage");
  String workspaceImprint = (String) session.getAttribute("workspaceImprint");
  String workspaceLogo = (String) session.getAttribute("workspaceLogo");
  String mapAlias = (String) session.getAttribute("mapAlias");
  String searchTerm = (String) session.getAttribute("searchTerm");
	String originId = (String) session.getAttribute("originId");
	String topicId = (String) session.getAttribute("topicId");
  String catIds = (String) session.getAttribute("categories");
	String baseLayer = (String) session.getAttribute("baseLayer");
	Integer critIndex = (Integer) session.getAttribute("critIndex");
  //
  String title = "Kiezatlas Stadtplan - " + map.getName();
  if (map.getName().indexOf("Ehrenamt") != -1) {
    title = "Ehrenamtsatlas - Bürgeraktiv im Kiezatlas";
  }
%>
<% startMaps(session, out); %>
<head>
  <meta http-equiv="content-type" content="text/html charset=UTF-8"/>
  <title> <%= title %> </title>
  <!--[if gte IE 7]>
    <style type="text/css">@import url(http://www.kiezatlas.de/maps/embed/landmaps-ie.css);</style>
  <![endif]-->
  <link rel="stylesheet" type="text/css" href="http://www.kiezatlas.de/maps/embed/landmaps.css"/>
  <script type="text/javascript" src="http://www.kiezatlas.de/maps/embed/kiezatlas.js"></script>
  <!-- This is a comment, without comment -->
  <!-- <script type="text/javascript" src="../pages/be.de/kiezatlas.js"></script> -->
  <!-- <link rel="stylesheet" type="text/css" href="../pages/be.de/landmaps.css"/> -->
  <!-- http://openlayers.org/api/2.9/OpenLayers.js // 2.9 or 2.9.1? -->
  <script type="text/javascript" src="http://www.kiezatlas.de/maps/embed/OpenLayers.js"></script>
  <script type="text/javascript" src="http://www.kiezatlas.de/maps/embed/CustomLayerSwitcher.js"></script>
  <script type="text/javascript" src="http://www.kiezatlas.de/maps/embed/jquery.min.js"></script>
  <script type="text/javascript" src="http://maps.google.com/maps?file=api&amp;v=2&amp;oe=utf-8&amp;key=ABQIAAAADev2ctFkze28KEcta5b4WBSQDgFJvORzMhuwLQZ9zEDMQLdVUhTWXHB2vS0W0TdlEbDiH_qzhBEZ5A"></script>
  <script type="text/javascript">
    var mapTitle = '<%= map.getName() %>';
    var mapAlias = '<%= mapAlias %>';
    var topicId = '<%= map.getID()%>';
    var baseLayer = '<%= baseLayer %>';
    var workspaceId = '<%= workspace.getID() %>';
    var crtCritIndex = <%= critIndex %>;
    var cats = '<%= catIds %>';
    var catIds = cats.split(",");
    var searchTerm = '<%= searchTerm %>';
    var linkTo = '<%= originId %>';
    var linkToTopicId = '<%= topicId %>';
    var mapTopics = <%= mapTopics %>;
    var workspaceCriterias = <%= workspaceCriterias %>;
    // var workspaceInfos = null;
    var workspaceHomepage = '<%= workspaceHomepage %>';
    var workspaceLogo = '<%= workspaceLogo %>';
    var workspaceImprint = '<%= workspaceImprint %>';
    var myLayerSwitcher = null;
    var myNewLayer = null;
    // var loadWorkspaceInfos = null;
    var districtLayer;
    var gMarkers = [];
    var allFeatures = [];
    var hotspots = [];
    var districtNames = [];
    var markerGroupIds = new Array();
    var helpVisible = false;
    // var slimWidth = false;
    // var totalWidth = 1000; // 953
    var map = "";
    var bounds = "";
    var markerLayer = "";
    // gui elements
    var sideBarVisible = true;
    var fullWindow = false;
    var debug = false;
    if (debug) {
      var debug_window = window.open('','','width=400,height=600,scrollbars=1');
    }
    jQuery.noConflict();
    var gKey = 'ABQIAAAADev2ctFkze28KEcta5b4WBSQDgFJvORzMhuwLQZ9zEDMQLdVUhTWXHB2vS0W0TdlEbDiH_qzhBEZ5A';
    //
    jQuery(document).ready(function() {
      // if (window.location.toString().indexOf("berlin.de") != -1) {
      onBerlinDe = true;
      baseUrl = "http://www.berlin.de/atlas/";
      SERVICE_URL = baseUrl + "rpc/";
      // register resize
      jQuery(window).resize(function() { handleResize(); });
      if (onBerlinDe & workspaceCriterias.result.length > 4) districtNames = workspaceCriterias.result[4].categories;
      setWorkspaceInfos();
      setCityMapName(mapTitle);
      var onEventMap = (mapTitle.indexOf("Veranstaltungen Ehrenamt Berlin") != -1) ? true : false;
      var onProjectMap = (mapTitle.indexOf("Ehrenamt Berlin") != -1) ? true : false;
      // check if a special criteria was set through an entry url
      if (crtCritIndex >= workspaceCriterias.result.length) {
        crtCritIndex = 0;// workspaceCriterias.result.length;
      }
      // cityMap setup
      bounds = calculateInitialBounds();
      // after the dom is loaded we can init our parts of the app
      jQuery(window).load(function() {
        document.namespaces;
        handleResize(); // do the layout
        renderCritCatListing(crtCritIndex);
        // updateCategoryList(crtCritIndex);
        // setup mapObject and layers
        openLayersInit(bounds);
        // create an array of OpenLayoers.Marker based on mapTopics
        gMarkers = setupOpenMarkers();
        // initialize Features and their control
        // clusters = findInitialClusters(gMarkers);
        // createVectorizedMarkerLayer(gMarkers, map);
        initLayerAllFeatures(gMarkers, map);
        // initBerlinDistrictsLayer();
        reSetMarkers(myNewLayer);
        if (onEventMap) showAllMarker();
        // inputFieldBehaviour();
        // check if a special projectId was given through the entry url
        if (linkTo != 'null') {
          selectAndShowInMap(linkTo, false);
        } else if (linkToTopicId != 'null') {
          selectAndShowInMap(linkToTopicId, true);
        } else if (catIds.length > 0) {
          for (var catIdx = 0; catIdx < catIds.length; catIdx++) {
            var catId = catIds[catIdx];
            // catId = catId.replace("%2C", "");
            // alert("catIds: " + catIds + " toggle : \"" + catId + "\"");
            // pre-select the categories encoded in url
            toggleMarkerGroups(catId);
          }
        }
        if (searchTerm != 'null') {
          searchRequest(searchTerm);
        }
        map.events.register("zoomend", map, redrawAfterZoomOperation);
        map.raiseLayer(myNewLayer);
        // if (jQuery.browser.msie)
        handleResize();
      });
    });
  </script>
</head>
  <body>
    <div id="kiezatlas" style="visibility: hidden;">
      <div id="kaheader">
		    <div id="mapName"></div>
		    <div id="focusInput">
		      <form id="autoLocateInputField" action="javascript:focusRequest()">
			    <label for="streetNameField">Umkreissuche</label>
			    <input id="streetNameField" type="text" size="18" value="Stra&szlig;enname / Hnr."/>
		      </form>
		    </div>
		    <div id="searchInput">
		      <form id="searchForm" action="javascript:searchRequest()">
			    <label for="searchInputField">Suche</label>
			    <input id="searchInputField" type="text" value="Einsatzm&#246;glichkeit" size="15"/>
		      </form>
		    </div>
		    <div id="headerButtons">
            <img id="fullWindowButton" title="Volle Fenstergr&ouml;&szlig;e nutzen" onclick="javascript:toggleFullWindow()" width="16" height="16"
              src="http://www.kiezatlas.de/maps/embed/img/fullscreen-16.png" alt="Volle Fenstergr&ouml;&szlig;e nutzen"/>
			    <img id="resizeButton" title="Seitenleiste ausblenden" onclick="javascript:handleSideBar()" width="15" height="15" src="http://www.kiezatlas.de/maps/embed/img/go-last.png" alt="Seitenleiste ausblenden"/>
		    <!-- </div> -->
		    </div>
      </div>
      <div id="map"></div>
	    <div id="focusAlternatives"></div>
      <div id="permaLink" style="visibility: hidden;" onclick="javascript:selectPermalink()">
          <input id="permaInputLink" type="text" value=""/>
      </div>
      <div id="mapControl">&nbsp;
        <a href="javascript:showPermaLink();" id="permaLinkHref">
		      <img border="0" src="http://www.kiezatlas.de/maps/embed/img/gnome_permalink.png" alt="Permalink-Symbol" title="Permalink anzeigen" width="16" height="16"/>
        </a>
        <a href="javascript:showAllMarker();" id="toggleMarkerHref">
          <img border="0" src="http://www.kiezatlas.de/maps/embed/img/FreiwilligenAgentur.png" alt="Markierer-Symbol" title="Alle Markierer einblenden" width="15" height="15"/>
        </a>
        <!-- <img border="0" id="divider" src="img/division.png" title="" width="1" height="10"> -->
		      <!-- <a href="javascript:removeAllMarker();" style="text-decoration: none;">> Alle ausblenden</a> <br/>-->
		    <a href="javascript:updateVisibleBounds(null, true, null, true);" id="resetMarkerHref">
		      <img border="0" src="http://www.kiezatlas.de/maps/embed/img/Stop.png" title="zurücksetzen der Kartenansicht und Informationsebenen" alt="Reset-Symbol" width="15" height="15"/>
        </a>
        <!-- <img border="0" id="divider" src="img/division.png" title="" width="1" height="10"> -->
        <span id="moreLabel">Mehr..</span>
		    <div id="mapSwitcher" style="position: absolute; visibility: hidden;"></div>
      </div>
      <div id="memu" style="visibility:hidden;"></div>
      <div id="navPanel"></div>
      <div id="sideBar">
		    <div id="sideBarCriterias"></div>
		    <div id="sideBarCategories"></div>
        <div id="progContainer"></div>
        <div id="kafooter">
          <a href="http://www.berlin.de/buergeraktiv/">Impressum</a> und 
          <a href="http://ehrenamt.index.de">Haftungshinweise</a><br/><b> powered by
          <a href="http://www.kiezatlas.de">Kiezatlas</a></b>
        </div>
      </div>
      <!-- <a id="helpFont" alt="Hilfe anzeigen" text="Hilfe anzeigen" href="javascript:help();">zur Hilfe</a> -->
      <div id="sideBarControl"></div> <!-- onclick="javascript:handleSideBar();" -->
      <div id="dialogMessage" style="visibility: hidden;" title="Dialog schlie&szlig;en">
          <div id="closeDialog" onclick="javascript:showDialog(false)">(Dialog schlie&szlig;en)</div>
          <b id="modalTitle" class="redTitle"></b>
          <p id="modalMessage"></p>
      </div>
    </div>
  </body>
</html>
