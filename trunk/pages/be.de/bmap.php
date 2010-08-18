<?php
    require_once "HTTP/Request.php";
	// getMapTopics
    $workspaceId = $_GET['workspaceId'];
    $topicId = $_GET['topicId'];
    $originId = $_GET['linkTo'];
    $body = '{"method": "getMapTopics", "params": ["'.$topicId.'" , "'.$workspaceId.'"]}';
    $req1 =& new HTTP_Request("http://www.kiezatlas.de:8080/rpc/");
    // http://localhost:8080/kiezatlas/rpc/ -- http://www.kiezatlas.de:8080/rpc/
    $req1->addHeader("Content-Type", "application/json");
    // $req2->addHeader("Charset", "utf-8");
    $req1->setBody($body);
    $req1->setMethod(HTTP_REQUEST_METHOD_POST);
    if (!PEAR::isError($req1->sendRequest())) {
	    $resp1 = $req1->getResponseBody();
    } else {
	    $resp1 = "Problems while loading CityMap. Please retry.";
    }
    $mapTopics = utf8_encode($resp1);

    // getWorkspaceCriterias
    $body = '{"method": "getWorkspaceCriterias", "params": ["'.$workspaceId.'", "'.$topicId.'"]}';
    $req2 =& new HTTP_Request("http://www.kiezatlas.de:8080/rpc/");
    $req2->addHeader("Content-Type", "application/json"); 
    // $req2->addHeader("Charset", "utf-8");
    $req2->setBody($body);
    $req2->setMethod(HTTP_REQUEST_METHOD_POST);
    if (!PEAR::isError($req2->sendRequest())) {
	    $resp2 = $req2->getResponseBody();
    } else {
	    $resp2 = "Problems while loading Kiez-Scenario. Please retry.";
    }
    $workspaceCriterias = utf8_encode($resp2);
    //
    $criteriaId = $_GET['critId'];
    if ($criteriaId == null) {
  	  $criteriaId = 0; // per Default, First Criteria will be Selected
    }
    $hazError = "null";
    $errorCheck = strpos($resp2, "Problem");
    if ($errorCheck != false) {
	    $hazError = $resp2;
    }
    $errorCheck = strpos($resp1, "Problem");
    if ($errorCheck != false) {		
	    $hazError = $resp1;
    }
?>
<!-- This comment will put IE 6, 7 and 8 in quirks mode -->
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
  <meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
  <title>Ehrenamtsatlas - Bürgeraktiv im Kiezatlas</title>
    <!-- <link rel="stylesheet" type="text/css" href="land.css">-->
    <link rel="stylesheet" type="text/css" href="landmaps.css">
    <script type="text/javascript" src="kiezatlas.js"></script>
    <script type="text/javascript" src="OpenLayers.2.9.js"></script>
    <script type="text/javascript" src="jquery-1.3.2.js"></script>
    <script type="text/javascript" src="http://maps.google.com/maps?file=api&v=2&oe=utf-8&key=ABQIAAAADev2ctFkze28KEcta5b4WBSQDgFJvORzMhuwLQZ9zEDMQLdVUhTWXHB2vS0W0TdlEbDiH_qzhBEZ5A"></script>
    <script type="text/javascript">
      var topicId = '<?php echo $topicId ?>';
  	  var workspaceId = '<?php echo $workspaceId ?>';
  	  var crtCritIndex = '<?php echo $criteriaId ?>';
  	  var originId = '<?php echo $originId ?>';
	    var hazError = '<?php echo $hazError?>';
	    if (hazError.indexOf("Problem") != -1) {
   	  	log("Please retry accessing the information through reloading the page: The Error Description is: " + hazError);
	    }
      var mapTopics = eval('(' + <?php echo json_encode($mapTopics) ?> + ')');
	    var workspaceCriterias = eval('(' + <?php echo json_encode($workspaceCriterias) ?> + ')');
	    var workspaceInfos = null;
	    var myLayerSwitcher = null;
	    // var loadWorkspaceInfos = null;
	    var districtLayer;
	    var gMarkers = [];
	    var allFeatures = [];
	    var hotspots = [];
	    var districtNames = [];
      var markerGroupIds = new Array();
	    var footerMessage = '<b><a href="http://www.kiezatlas.de">Kiezatlas</a></b> ' 
	      + 'is powered by <a href="http://www.deepamehta.de">DeepaMehta</a>';
    	var helpVisible = false;
	    // var slimWidth = false;
	    // var totalWidth = 1000; // 953
	    var headerGap = 63;
	    var map = "";
	    var bounds = "";
	    var markerLayer = "";
	    // gui elements
	    var sideBarVisible = true;
	    var debug = false;
	    if (debug) {
	      var debug_window = window.open('','','width=400,height=600,scrollbars=1');
	    }
	    var gKey = 'ABQIAAAADev2ctFkze28KEcta5b4WBSQDgFJvORzMhuwLQZ9zEDMQLdVUhTWXHB2vS0W0TdlEbDiH_qzhBEZ5A';
	    //
	    jQuery(document).ready(function(){
	    	// register resize
	      jQuery(window).resize(function() { 
	        handleResize(); 
	      });
	      /**var myMapTopics = null;
	      // do the ajax init is currently #unused
	      loadCityMapTopics(topicId, workspaceId, myMapTopics);
	      var loadedCriterias = null;
	      */
	      // loadWorkspaceCriterias(workspaceId, loadedCriterias);
	      loadWorkspaceInfos(workspaceId);
	      if (debug) log("mapTopics:" + mapTopics.result.topics.length);
	      if (debug) log("workspaceCrits:" + workspaceCriterias.result.length);
	      if (debug) log("districtNames:" + workspaceCriterias.result[4].categories[0].catName);
	      if (workspaceCriterias.result.length >= 4) districtNames = workspaceCriterias.result[4].categories;
	      getCityMapName(topicId); // fetch and set CityMapName
	      handleResize(); // do the layout
	      // check if a special criteria was set through an entry url
	      if (crtCritIndex >= workspaceCriterias.result.length) { 
	        crtCritIndex = 0;// workspaceCriterias.result.length;
	      }
	      // cityMap setup
	      bounds = calculateInitialBounds();
	      // after the dom is loaded we can init our parts of the app
	      jQuery(window).load(function() {
	      	document.namespaces;
	      	openLayersInit(bounds);
				  gMarkers = setupOpenMarkers();
				  // initialize Features and their control
				  // clusters = findInitialClusters(gMarkers);
				  // createVectorizedMarkerLayer(gMarkers, map);
				  initLayerAllFeatures(gMarkers, map);
				  initBerlinDistrictsLayer();
				  showCritCatList();
				  setWorkspaceInfos();
				  reSetMarkers();
				  inputFieldBehaviour();
				  //log("originId is: " + originId);
  	      // check if a special projectId was given through the entry url
				  if (originId != "") {
				    	if (debug) log("log-originLinkToTopicID: " + originId);
				    	selectAndShowInMap(originId);
				    	// ### TODO: über gute-Tat.de
		    	}
				  // drawAllOpenMarkers(gMarkers, map);
				  map.events.register("zoomend", map, redrawAfterZoomOperation);
      	});
      });
  
    </script>
  </head>
  <body>
    <div id="kiezatlas" style="visibility:hidden;">
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
			    <label for="searchInputField">Suchen</label>
			    <input id="searchInputField" type="text" value="Name / Straße" size="15"/>
			    <ul>
			    </ul>
		      </form>
		    </div>
		    <div id="headerButtons">
		      <!-- <div id="mapMarkerButtons" style="position: relative; top: 5px; left: 70px; visibility: visible;">-->
			    <img id="resizeButton" title="Seitenleiste ausblenden" onclick="javascript:handleSideBar()" width="15" height="15" src="img/go-last.png" alt="Seitenleiste ausblenden">
		    <!-- </div> -->
		    </div>
      </div>
      <div id="map"></div>
	    <div id="focusAlternatives"></div>
      <div id="mapControl">
        <a href="javascript:showAllMarker();" id="toggleMarkerHref">
          <img border="0" src="img/FreiwilligenAgentur.png" title="Alle Markierer einblenden" width="15" height="15"></a>
			    <!-- <a href="javascript:removeAllMarker();" style="text-decoration: none;">> Alle ausblenden</a> <br/>-->
			  <a href="javascript:updateVisibleBounds(null, true);" id="resetMarkerHref">
			    <img border="0" src="img/Stop.png" title="zurücksetzen der Kartenansicht und Informationsebenen" width="15" height="15"></a>&nbsp;
		    <a href="javascript:toggleMapControl();" id="mapFunctions">Kartenfunktionalit&auml;t</a>
		    <div id="mapSwitcher" style="position: absolute; visibility: hidden;"></div>
      </div>
      <div id="memu" style="visibility:hidden;"></div>
      <div id="navPanel"></div>
      <div id="sideBar">
		    <div id="sideBarCriterias"></div>
		    <div id="sideBarCategories"><table width="100%" cellpadding="2" cellspacing="0" id="sideBarCategoriesTable"></table></div>
        <div id="progContainer"></div>
      </div>
      <div id="kafooter">
        <a href="http://www.berlin.de/buergeraktiv/">Impressum</a> und <a href="http://ehrenamt.index.de">Haftungshinweise</a><br/><b> powered by <a href="http://www.kiezatlas.de">Kiezatlas</a></b>
      </div>
      <a id="helpFont" alt="Hilfe anzeigen" text="Hilfe anzeigen" href="javascript:help();">zur Hilfe</a>
      <div id="sideBarControl"></div> <!-- onclick="javascript:handleSideBar();" -->
    </div>
  </body>
</html>
