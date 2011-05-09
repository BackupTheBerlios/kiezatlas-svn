<%@ include file="../KiezAtlas.jsp" %>
<%
	BaseTopic map = (BaseTopic) session.getAttribute("map");
	BaseTopic workspace = (BaseTopic) session.getAttribute("workspace");
	String mapTopics = (String) session.getAttribute("mapTopics");
  String workspaceCriterias = (String) session.getAttribute("workspaceCriterias");
  String workspaceImprint = (String) session.getAttribute("workspaceImprint");
  String workspaceLogo = (String) session.getAttribute("workspaceLogo");
  String mapAlias = (String) session.getAttribute("mapAlias");
  String workspaceHomepage = (String) session.getAttribute("workspaceHomepage");
	String searchTerm = (String) session.getAttribute("searchTerm");
	String originId = (String) session.getAttribute("originId");
	String topicId = (String) session.getAttribute("topicId");
  String baseLayer = (String) session.getAttribute("baseLayer");
	Integer critIndex = (Integer) session.getAttribute("critIndex");
  //
  String title = "Kiezatlas Stadtplan - " + map.getName();

%>
<% startMaps(session, out); %>
<head>
  <meta http-equiv="content-type" content="text/html charset=UTF-8"/>
  <title> <%= title %> </title>
  <link rel="stylesheet" type="text/css" href="../pages/be.de/maps.css"/>
  <script type="text/javascript" src="../pages/be.de/kiezatlas.js"></script>
  <!-- http://openlayers.org/api/2.9/OpenLayers.js // 2.9 or 2.9.1? -->
  <script type="text/javascript" src="../pages/be.de/OpenLayers.js"></script>
  <script type="text/javascript" src="../pages/be.de/CustomLayerSwitcher.js"></script>
  <script type="text/javascript" src="../pages/be.de/jquery.min.js"></script>
  <!-- <script type="text/javascript" src="../pages/be.de/jquery-1.3.2.js"></script>-->
  <script type="text/javascript" src="http://maps.google.com/maps?file=api&amp;v=2&amp;key=ABQIAAAAyg-5-YjVJ1InfpWX9gsTuxRa7xhKv6UmZ1sBua05bF3F2fwOehRUiEzUjBmCh76NaeOoCu841j1qnQ"></script>
  <script type="text/javascript">
    var mapTitle = '<%= map.getName() %>';
    var mapAlias = '<%= mapAlias %>';
    var topicId = '<%= map.getID() %>'; // unlike the topicId param from the requestURL
    var baseLayer = '<%= baseLayer %>';
    var workspaceId = '<%= workspace.getID() %>';
    // options
    var crtCritIndex = <%= critIndex %>;
    var searchTerm = '<%= searchTerm %>';
    var linkToTopicId = '<%= topicId %>';
    var linkTo = '<%= originId %>';
    // body
    var mapTopics = <%= mapTopics %>; // null; //eval('(' + <?php echo json_encode($mapTopics) ?> + ')');
    var workspaceCriterias = <%= workspaceCriterias %>; // eval('(' + <?php echo json_encode($workspaceCriterias) ?> + ')');
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
    var debug = false;
    if (debug) {
      var debug_window = window.open('','','width=400,height=600,scrollbars=1');
    }
    var gKey = 'ABQIAAAAyg-5-YjVJ1InfpWX9gsTuxRa7xhKv6UmZ1sBua05bF3F2fwOehRUiEzUjBmCh76NaeOoCu841j1qnQ';
    //
    jQuery(document).ready(function() {
      // register resize
      jQuery(window).resize(function() {
        handleResize();
      });
      if (window.location.toString().indexOf("berlin.de") != -1) {
        onBerlinDe = true;
      } else {
        onBerlinDe = false;
      }
      if (onBerlinDe & workspaceCriterias.result.length > 4) districtNames = workspaceCriterias.result[4].categories;
      setWorkspaceInfos();
      setCityMapName('<%= map.getName() %>'); // fetch and set CityMapName
      // check if a special criteria was set through an entry url
      if (crtCritIndex >= workspaceCriterias.result.length) {
        crtCritIndex = 0;// workspaceCriterias.result.length;
      }
      // cityMap setup
      bounds = calculateInitialBounds();
      // after the dom is loaded we can init our parts of the app
      jQuery(window).load(function() {
        // if (console != undefined) console.log("testLog...");
        document.namespaces; // some curios ie workaround for openlayers
        handleResize(); // do the layout
        // setup mapObject and layers
        openLayersInit(bounds);
        // ...
        renderCritCatListing(crtCritIndex);
        // create an array of OpenLayoers.Marker based on mapTopics
        gMarkers = setupOpenMarkers();
        // initialize Features and their control
        initLayerAllFeatures(gMarkers, map);
        initBerlinDistrictsLayer();
        reSetMarkers(myNewLayer);
        inputFieldBehaviour();
        // check if a special projectId was given through the entry url
        if (linkTo != 'null') {
          selectAndShowInMap(linkTo, false);
        } else if (linkToTopicId != 'null') {
          selectAndShowInMap(linkToTopicId, true);
        }
        if (searchTerm != 'null') {
          searchRequest(searchTerm);
        }
        map.events.register("zoomend", map, redrawAfterZoomOperation);
        // map.maxExtent = bounds;
        map.raiseLayer(myNewLayer);
        if (jQuery.browser.msie) handleResize(); // fix the layout for ie..
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
		      <!-- <div id="mapMarkerButtons" style="position: relative; top: 5px; left: 70px; visibility: visible;">-->
			    <img id="resizeButton" title="Seitenleiste ausblenden" onclick="javascript:handleSideBar()" src="../pages/be.de/img/go-last.png" alt="Seitenleiste ausblenden">
		    <!-- </div> -->
		    </div>
      </div>
      <div id="map"></div>
	    <div id="focusAlternatives"></div>
      <div id="permaLink" style="visibility: hidden;" onclick="javascript:selectPermalink()">
          <input id="permaInputLink" type="text" value=""/>
      </div>
      <!-- <div id="permaLinkLabel" style="visibility: hidden;" onclick="javascript:selectPermalink()">Copy & Paste URL:</div> -->
      <div id="mapControl">&nbsp;
        <a href="javascript:showPermaLink();" id="permaLinkHref">
		      <img border="0" src="../pages/be.de/img/gnome_permalink.png" title="Permalink anzeigen" alt="Permalink-Symbol" width="16" height="16">
        </a>
        <a href="javascript:showAllMarker();" id="toggleMarkerHref">
          <img border="0" src="../pages/be.de/img/FreiwilligenAgentur.png" title="Alle Markierer einblenden" alt="Markierer-Symbol" width="15" height="15">
        </a>
        <!-- <img border="0" id="divider" src="img/division.png" title="" width="1" height="10"> -->
		      <!-- <a href="javascript:removeAllMarker();" style="text-decoration: none;">> Alle ausblenden</a> <br/>-->
		    <a href="javascript:updateVisibleBounds(null, true, null, true);" id="resetMarkerHref">
		      <img border="0" src="../pages/be.de/img/Stop.png" title="zurücksetzen der Kartenansicht und Informationsebenen" alt="Reset-Symbol" width="15" height="15">
        </a>
        <!-- <img border="0"atlas id="divider" src="img/division.png" title="" width="1" height="10"> -->
        <span id="moreLabel">&nbsp;Mehr..</span>
        <div id="mapSwitcher" style="position: absolute; visibility: hidden;"></div>
      </div>
      <div id="memu" style="visibility:hidden;"></div>
      <div id="navPanel"></div>
      <div id="sideBar">
		    <div id="sideBarCriterias"></div>
		    <div id="sideBarCategories"></div>
        <div id="progContainer"></div>
      </div>
      <div id="kafooter">
        <a href="http://www.berlin.de/buergeraktiv/">Impressum</a> und <a href="http://ehrenamt.index.de">Haftungshinweise</a><br/><b> powered by <a href="http://www.kiezatlas.de">Kiezatlas</a></b>
      </div>
      <div id="sideBarControl"></div> <!-- onclick="javascript:handleSideBar();" onclick="javascript:showDialog(false)"  -->
      <div id="dialogMessage" style="visibility: hidden;" title="Dialog schlie&szlig;en">
          <div id="closeDialog" onclick="javascript:showDialog(false)">(Dialog schlie&szlig;en)</div>
          <b id="modalTitle" class="redTitle"></b>
          <p id="modalMessage"></p>
      </div>
    </div>
  </body>
</html>
