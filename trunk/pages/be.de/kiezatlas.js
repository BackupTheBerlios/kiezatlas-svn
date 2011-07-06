
  /** 
   * This is the JavaScript Kiezatlas Citymap Client
   * 
   * @author Malte Rei&szlig;ig (malte@deepamehta.org)
   * @license GPL v3
   * 
   * @modified 16.May 2011
   *
   * @requires OpenLayers.js (2.9), jQuery(1.3.2 - 1.5.2).js
   * 
   * TODO: code cleanup, improving some wording on method signatures, some error handling
   * TODO: relies on some icons and scripts at http://www.kiezatlas.de/maps/embed/ ### if (onBerlinDe)
   * INFO: getters/checkers return "null" as a negative result/ error
   */



  // --
  // --- Settings helping you to configure this script
  // --

  var SERVICE_URL = "http://www.kiezatlas.de/rpc/"; // to be used by the jquery ajax methods
  var TEST_SERVICE_URL = "http://localhost:8080/kiezatlas/rpc/"; // to be used by the jquery ajax methods
  var ICONS_URL = "http://www.kiezatlas.de/client/icons/"; // to be used by all icons if not relative to this folder
  //
  // var IMAGES_URL = "http://www.kiezatlas.de/images/"; // bo be used by all images in the sidebar
  var IMAGES_URL = "http://www.kiezatlas.de:8080/client/images/"
  var LEVEL_OF_DETAIL_ZOOM = 15; // the map focus when a mapinternal infoWindow is rendered
  var LEVEL_OF_DISTRICT_ZOOM = 12;
  var LEVEL_OF_CITY_ZOOM = 11;
  //
  var sideBarToggle = true;
  var autocomplete_item = 0;
  var alternative_items = [];
  var lastStreetName = "";
  var debugUI = false;
  // deployment default settings
  var onBerlinDe = false;
  var fullWindow = false;
  var headerGap = 0;
  var kiezKey = "ABQIAAAAyg-5-YjVJ1InfpWX9gsTuxRa7xhKv6UmZ1sBua05bF3F2fwOehRUiEzUjBmCh76NaeOoCu841j1qnQ";
  var berlinKey = "ABQIAAAADev2ctFkze28KEcta5b4WBSQDgFJvORzMhuwLQZ9zEDMQLdVUhTWXHB2vS0W0TdlEbDiH_qzhBEZ5A";
  var propFooterMessage = "";
  var helpLink = "";
  var baseUrl = "http://www.kiezatlas.de/map/";
  var permaLink = "";
  var linkParams = [];



  //
  // --- Init & Layout
  //   
    
  function openLayersInit(openBounds) {
    // updatePermaLink(baseUrl+mapAlias);
    // Map Options
    var options = {
      projection: new OpenLayers.Projection("EPSG:900913"),
      displayProjection: new OpenLayers.Projection("EPSG:4326"), units: "m",
      maxResolution: 156543.0339, numZoomLevels: 25
      // maxExtent: openBounds // an internal error occurs when using OpenStreetMap BaseLayer togegher with maxExtent
    };
    map = new OpenLayers.Map('map', options);
    //
    // BaseLayer
    var mapnik = new OpenLayers.Layer.TMS("OpenStreetMap", "http://tile.openstreetmap.org/", {
      type: 'png', getURL: osm_getTileURL, displayOutsideMaxExtent: false,
      attribution: '<a href="http://www.openstreetmap.org/">OpenStreetMap</a>',
      maxExtent: new OpenLayers.Bounds(-20037508.34, -20037508.34, 20037508.34, 20037508.34)
      // note: maxExtent bug, this bounds cannot be smaller than the whole world, otherwise projection error occurs
    });
    var googleBaseLayer = new OpenLayers.Layer.Google("Google Maps", { 
      sphericalMercator:true, maxExtent: openBounds
      // termsOfUse: jQuery("#kafooter").get(0), poweredBy: jQuery("#kafooter").get(0)
    });
    if (baseLayer == "osm") {
      map.addLayers([ mapnik, googleBaseLayer]); // markerLayer
    } else {
      map.addLayers([ googleBaseLayer, mapnik ]); // googleBaseLayer
    }
    // 
    // MapControl Setup
    nav = new OpenLayers.Control.NavigationHistory();
    myLayerSwitcher = OpenLayers.Control.CustomLayerSwitcher = 
    OpenLayers.Class(OpenLayers.Control.LayerSwitcher, {
      CLASS_NAME: "OpenLayers.Control.CustomLayerSwitcher"
    });
    // a parental control must be added to the map
    map.addControl(nav);
    //
    panel = new OpenLayers.Control.Panel( {div: document.getElementById("navPanel")});
    myLayerSwitcher = new OpenLayers.Control.LayerSwitcher({
      'div':OpenLayers.Util.getElement('mapSwitcher'), activeColor: "white"
    });
    map.addControl(myLayerSwitcher);
    // event binding for the new hover controlMenu ### ToDo: find a better place for this
    jQuery("#mapControl").bind("mouseover", overMapControl);
    jQuery("#mapControl").bind("mouseout", outMapControl);
    //  special: left sided forth / back navigation menu on kiezatlas.de/map/*
    if (!onBerlinDe) panel.addControls([nav.next, nav.previous]);
    map.addControl(panel);
    // layerSwitcher, NavigationHistory, Panel
    if (debug) log('mapBounds will be set to: ' + openBounds);
    map.zoomToExtent(openBounds.transform(map.displayProjection, map.projection));
    // if (onBerlinDe) { map.zoomTo(LEVEL_OF_CITY_ZOOM); }
    reSetMarkers();
  }

  /**
   * craziest method ever - if (theres not something) else like that out there
   * notes: does handle our basic html-layout-wireframe in 3 variations, plus all for internet explorer
   **/
  function setLayout(fullH, fullW) {
    // onBerlinDe: fixed Width, downed under from Top, adjusted sideBarHeight, 
    var sideW = 320;
    if (onBerlinDe && jQuery.browser.msie && !fullWindow) {
      // jQuery("#kiezatlas").css("top", "153px");
      jQuery("#kaheader").css("top", 153);
      // jQuery("#kafooter").css("bottom", 2);
      jQuery("#focusAlternatives").css("top", 166);
      jQuery("#permaLink").css("top", 182);
      jQuery("#sideBar").css("width", sideW + 2);
      jQuery("#bo_header #main_navigation").css("width", 1036);
    } else if (jQuery.browser.msie && !onBerlinDe) { // maps-labs on ie
      jQuery("#focusInput").css("top", 9);
      jQuery("#focusInput input textarea").css("padding-top", 2);
      jQuery("#searchInput").css("top", 9);
      jQuery("#searchInput input textarea").css("padding-top", 2);
    }
    var topHeight = jQuery("#kaheader").css("height");
    var startHeight = jQuery("#kaheader").css("top");
    topHeight = parseInt(topHeight.substr(0, topHeight.length-2));
    startHeight = parseInt(startHeight.substr(0, startHeight.length-2));
    jQuery("#kiezatlas").css("visibility", "visible");
    //
    if (onBerlinDe && !fullWindow) {
      fullW = 1037 - 1; // else fullW = fullW - 7;
      jQuery("#kiezatlas").css("width", fullW);
      jQuery("#kiezatlas").css("height", mapH + 153);
    } else if (onBerlinDe && fullWindow) {
      fullW = fullW - 1;
      mapH = mapH - 5;
      jQuery("#kiezatlas").css("width", fullW);
    }
    var mapW = mapW = fullW - sideW - 6; // border fullW - sideW - 7;
    var mapH = fullH - topHeight - startHeight - 1; // current labs headerHeight
    jQuery("#kaheader").css("width", fullW);
    // 
    jQuery("#map").css("top", startHeight + topHeight + 1);
    jQuery("#map").css("width", mapW);
    jQuery("#map").css("height", mapH);
    //
    jQuery("#mapControl").css("left", mapW - 129);
    jQuery("#mapControl").css("top", startHeight + topHeight + 8);
    if (onBerlinDe && !fullWindow) {
      jQuery("#focusInput").css("left", 240);
      jQuery("#focusAlternatives").css("top", 166);
      jQuery("#focusAlternatives").css("left", 310);
      jQuery("#permaLink").css("left", 275);
      jQuery("#headerButtons").css("left", fullW - 46);
    } else if (onBerlinDe && fullWindow) {
      //
      jQuery("#headerButtons").css("left", fullW - 46);
      jQuery("#focusAlternatives").css("top", topHeight);
      jQuery("#permaLink").css("top", topHeight + 8);
      // jQuery("#permaLink").css("left", window);
    } else {
      jQuery("#focusInput").css("left", 345);
      jQuery("#focusAlternatives").css("left", 420);
      jQuery("#headerButtons").css("left", fullW - 30);
    }
    jQuery("#searchInput").css("left", fullW - sideW + 10);
    // sidebarControl is 5px fat
    jQuery("#sideBarControl").css("left", mapW);
    jQuery("#sideBarControl").css("height", mapH);
    jQuery("#sideBarControl").css("top", topHeight + startHeight + 1);
    jQuery("#sideBarControl").css("width", 5);
    //
    jQuery("#sideBar").css("top", topHeight + startHeight + 1);
    jQuery("#sideBar").css("height", mapH - 1);
    jQuery("#sideBar").css("left", mapW + 4);
    // set width and perform a jquery show('fast')
    setSideBarWidth(sideW);
    jQuery("#sideBarCriterias").css("width", sideW - 5);
    jQuery("#resizeButton").attr("height", jQuery("#kaheader").height()-4);
    jQuery("#resizeButton").attr("width", jQuery("#kaheader").height()-4);
    //
    var critHeight = jQuery("#sideBarCriterias").height();
    if (critHeight == 0 || isNaN(critHeight)) {
      critHeight = 110;
    } else {
      critHeight = critHeight + 5;
    }
    var footerHeight = 35; // ### FIXME: to be removed
    var sideBarHeight = mapH - critHeight - footerHeight;
    jQuery("#sideBarCategories").css("height", sideBarHeight);
    jQuery("#sideBarCategories").css("width", sideW - 7);
    // get the position for our footer-div, which is placed outside #sideBar at labs-maps and inside at berlin.de
    var fWidth = sideW - 6;
    var fOrientation = mapW - 3;
    jQuery("#kafooter").css("width", fWidth - 15);
    if (!onBerlinDe) {
      jQuery("#kafooter").css("left", fOrientation + 10);
    } else {
      jQuery("#kafooter").css("left", 5);
    }
    jQuery("#layersDiv").css("left", -25);
    if (!sideBarToggle && onBerlinDe) {
      // if sidebar is toggled away and user wants to get into fullscreen mode
      handleSideBar();
    }
  }

  // ### FIXME: refactor: the width to restore should not be of interest for hiding or showing sideBar
  function handleSideBar() { // e
    var breitSeite; // complete content-window-width
    if(sideBarToggle) {
      // jQuery("#sideBarControl").css("cursor", "w-resize");
      if (onBerlinDe && !fullWindow) breitSeite = 1036 - 5; // new layout
      else breitSeite = windowWidth() - 5; // 1339;//
      // breitSeite = windowWidth() - 5; // 1317;//
      jQuery("#sideBarControl").css("left", breitSeite);
      jQuery("#map").css("width", breitSeite);
      jQuery("#sideBar").hide("fast");
      jQuery("#helpFont").hide("fast");
      // jQuery("#kafooter").css("opacity", "0.4");
      jQuery("#kafooter").css("background", "transparent");
      jQuery("#kafooter").css("bottom", 30);
      // jQuery("#kafooter").show();
      jQuery("#resizeButton").attr("src", "http://www.kiezatlas.de/maps/embed/img/go-first.png");
      jQuery("#resizeButton").attr("height", parseInt(jQuery("#kaheader").css("height"))-4);
      jQuery("#resizeButton").attr("width", parseInt(jQuery("#kaheader").css("height"))-4);
      jQuery("#resizeButton").attr("title", "Seitenleiste einblenden");
      sideBarToggle = false;
    } else {
      // jQuery("#kafooter").css("opacity", "1.0");
      jQuery("#kafooter").css("background", "#fff");
      if (onBerlinDe && !fullWindow) breitSeite = 1036; // new layout
      else breitSeite = windowWidth(); // 1339;//
      // jQuery("#sideBar").show("fast");
      // jQuery("#sideBarControl").attr("onclick", "javascript:handleSideBar();");
      sideBarToggle = true;
  	  jQuery("#helpFont").show("fast");
      jQuery("#kafooter").css("background", "white");
      // if (onBerlinDe && !fullWindow) jQuery("#kafooter").css("bottom", 0);
      //else
      jQuery("#kafooter").css("bottom", 3);
      // jQuery("#headerButtons").html(imgTag);
      jQuery("#resizeButton").attr("src", "http://www.kiezatlas.de/maps/embed/img/go-last.png");
      jQuery("#resizeButton").attr("height", parseInt(jQuery("#kaheader").css("height"))-4);
      jQuery("#resizeButton").attr("width", parseInt(jQuery("#kaheader").css("height"))-4);
      jQuery("#resizeButton").attr("title", "Seitenleiste ausblenden");
  	  // jQuery("#kafooter").css("width", );
      handleResize(breitSeite);
    }
    if (debug) log('[DEBUG] handleSidebar got: ' + e.type + ' at '+ posx+':'+posy + '');
  }

  function toggleFullWindow() {
    // special feature for berlin.de
    if (fullWindow) {
      jQuery("#kiezatlas").css("z-index", "0");
      if (onBerlinDe && jQuery.browser.msie) {
        jQuery("#kaheader").css("top", "153px");
      } else if (onBerlinDe) {
        jQuery("#kaheader").css("top", "143px");
      }
      fullWindow = false;
      handleResize(); // substract the top-height from overall height..
    } else {
      jQuery("#kiezatlas").css("z-index", "1");
      jQuery("#kaheader").css("top", "0px");
      fullWindow = true;
      handleResize(windowWidth());
    }
  }

  function showDialog(renderFlag, title, message) {
    if (!renderFlag) {
      jQuery("#dialogMessage").css("visibility", "hidden");
    } else if (jQuery("#dialogMessage").css("visibility") == "hidden") {
      jQuery("#modalTitle").html(title);
      jQuery("#modalMessage").html(message);
      jQuery("#dialogMessage").css("visibility", "visible");
    } else {
      // jQuery("#dialogMessage").css("visibility", "hidden");
      // jQuery("#permaLinkLabel").css("visibility", "hidden");
    }
  }

  function showPermaLink() {
    // window.location.replace(permaLink);
    if (jQuery("#permaLink").css("visibility") == "hidden") {
      jQuery("#permaLink").css("visibility", "visible");
      // jQuery("#permaLinkLabel").css("visibility", "visible");
    } else {
      jQuery("#permaLink").css("visibility", "hidden");
      // jQuery("#permaLinkLabel").css("visibility", "hidden");
    }
  }

  function updatePermaLink(newLink) {
    if (map.baseLayer != undefined && map.baseLayer.name == "OpenStreetMap") {
      // OpenStreetMap
      if (newLink.indexOf("baseLayer=osm") != -1) {
        //  is already part of the permalink..
        // alert("newLink has BaseLayerOSM already inclduede!!");
      } else if (permaLink.indexOf("?") != -1) {
        // updatePermaLink(permaLink + "&baseLayer=osm");
        newLink = newLink + "&baseLayer=osm";
      } else {
        // updatePermaLink(permaLink + "?baseLayer=osm");
        newLink = newLink + "?baseLayer=osm";
      }
    } else if (map.baseLayer != undefined && map.baseLayer.name == "Google Maps") {
      newLink = newLink.replace("&baseLayer=osm", "");
      newLink = newLink.replace("?baseLayer=osm", "");
      // GMaps
      // updatePermaLink(permaLink);
    }
    permaLink = newLink;
    jQuery("#permaInputLink").val(permaLink);
  }

  function selectPermalink() {
    jQuery("#permaInputLink").focus(function(){
      this.select();
    });
  }


  // 
  // --- Client Side Requests (to proxy scripts)
  // 

  /** a get and show method implemented as
   *  an asynchronous call which renders the html directly into the sidebar when the result has arrived **/
  function getGeoObjectInfo(itemId, resultHandler) {
    // log('requesting info for: ' + topicId);
    if (resultHandler == 'abc') { // ### abc?
      resultHandler = jQuery("#sideBarCategories");
    }
    var url = SERVICE_URL;
    var body = '{"method": "getGeoObjectInfo", "params": ["' + itemId + '"]}';// '{' + urlencode(streetFocus) + '}';
    jQuery.ajax({
	    type: "POST",
	    url: url,
	    data: body,
      beforeSend: function(xhr) {xhr.setRequestHeader("Content-Type", "application/json")},
      dataType: 'json',
	    success: function(obj) {
	      var topic = obj.result;
        showGeoObjectInfo(topic, resultHandler);
	    }, // end of success handler
	    error: function(x, s, e){ 
	      if (debug) log('Error@GeoObjectInfo Request' + x.statusText);
	      resultHandler.empty();
	      resultHandler.append('&nbsp;&nbsp;<b>Projektbezogener &Uuml;bertragungsfehler</b><p/>');
	      resultHandler.append('<table width="100%" cellpadding="2" cellspacing="0" id="sideBarCategoriesTable"><tr>'
	        + '<td class="propertyLabel">&nbsp;&nbsp;&nbsp;&nbsp;Wir arbeiten noch daran.</td></tr></table>');
	      // return resultObj = new String('XHRError');
        hideProgressFromSideBar();
	    }
    });
  }

  /** ask my kiezatlas.de proxy for geoObjects */
  function searchRequest(query) {
    showDialog(false);
    var queryString = "";
    if (typeof query == "undefined") {
      queryString = jQuery("#searchInputField").attr("value");
    } else {
      queryString = query;
      jQuery("#searchInputField").attr("value", query);
    }
    if(debug) log('start searching for ' + queryString);
    queryString = urlencode(queryString);
    // + 'searchGeoObjects.php?query=' + queryString + '&topicmapId=' + topicId + '&workspaceId=' + workspaceId;
    var body = '{"method": "searchGeoObjects", "params": ["'+queryString+'", "'+topicId+'", "'+workspaceId+'"]}';
    showProgressInSideBar("Suchanfrage");
    jQuery.ajax({
      type: "POST",
      url: SERVICE_URL,
      data: body,
      beforeSend: function(xhr) {xhr.setRequestHeader("Content-Type", "application/json")},
      dataType: 'json',
      async: true,
      success: function(obj){
		    initResultList(obj.result, queryString);
        updatePermaLink(baseUrl+mapAlias+"?search="+queryString);
      },
      error: function(x, s, e){
        hideProgressFromSideBar();
        jQuery("#sideBarCategories").empty();
        jQuery("#sideBarCategories").append('&nbsp;&nbsp;<b class="redTitle">Ihre Anfrage lieferte keine Ergebnisse</b><p/>');
	      jQuery("#sideBarCategories").append('<table width="100%" cellpadding="2" cellspacing="0"'
	        + ' id="#sideBarCategoriesTable"></table>');
	      if (debug) log('Fehler beim Suchen aufgetreten' + x.statusText);
      }
    });
  }

  function showGeoObjectInfo(givenTopic, resultHandler) {
    hideProgressFromSideBar();
    resultHandler.empty();
    var imgSrc = getImageSource(givenTopic);
    if (imgSrc != "undefined") {
      imgSrc = IMAGES_URL + imgSrc;
      // var imgWidth = jQuery("#sideBar").css("width");
      resultHandler.append('<img src="'+imgSrc+'"/><br/>');
    }
    resultHandler.append('<b>'+givenTopic.name+'</b><br/>');
    // address related stuff follows
    var cityName = getTopicCity(givenTopic);
    var street = getTopicAddress(givenTopic);
    if (cityName == " Berlin" || cityName == "Berlin" || onBerlinDe) { // ### FIXME sloppy
      var publicTransportURL = 'http://www.fahrinfo-berlin.de/Stadtplan/index?query=' + street +
        '&search=Suchen&formquery=&address=true';
      var imageLink = '<a href="'+ publicTransportURL + '" target="_blank">'
        + '<img src=\"'+IMAGES_URL+'fahrinfo.gif" border="0" hspace="20"/></a>';
      if (onBerlinDe || topicId == "t-331302") { // ehrenamt map datasets have no city property
        resultHandler.append(''+getTopicPostalCode(givenTopic) + ' Berlin<br/>');
      } else {
        resultHandler.append(''+getTopicPostalCode(givenTopic) + ' ' + cityName + '<br/>');
      }
      resultHandler.append('' + street + '&nbsp;' + imageLink + '<p/>');
    } else {
      if (topicId == "t-331302") { // ehrenamt map on datasets have no city property
        resultHandler.append(''+getTopicPostalCode(givenTopic) + ' Berlin<br/>');
      } else {
        resultHandler.append(''+getTopicPostalCode(givenTopic) + ' ' + cityName + '<br/>');
      }
      resultHandler.append('' + street + '<p/>');
    }
    // stripping unwanted fields of the data container
    givenTopic = stripFieldsContaining(givenTopic, "LAT");
    givenTopic = stripFieldsContaining(givenTopic, "LONG");
    givenTopic = stripFieldsContaining(givenTopic, "Locked Geometry");
    givenTopic = stripFieldsContaining(givenTopic, "Forum / Aktivierung");
    givenTopic = stripFieldsContaining(givenTopic, "Image");
    givenTopic = stripFieldsContaining(givenTopic, "Icon");
    givenTopic = stripFieldsContaining(givenTopic, "YADE");
    givenTopic = stripFieldsContaining(givenTopic, "Stadt");
    givenTopic = stripFieldsContaining(givenTopic, "Address");
    givenTopic = stripFieldsContaining(givenTopic, "Name");
    givenTopic = stripFieldsContaining(givenTopic, "Description");
    givenTopic = stripFieldsContaining(givenTopic, "Timestamp");
    givenTopic = stripFieldsContaining(givenTopic, "OriginId");
    var propertyList = '<p>'; //<table width="100%" cellpadding="2" border="0"><tbody>';
    for (var i=0; i < givenTopic.properties.length; i++) {
      // propertyList += '<tr>';
      if (givenTopic.properties[i].label.indexOf("Sonstiges") != -1) {
        propertyList += '<p class="additionalInfoWhite">';
      } else if (givenTopic.properties[i].label.indexOf("Administrator") != -1) {
        propertyList += '<p class="additionalInfo">';
      } else if (givenTopic.properties[i].label == "Barrierefrei" && givenTopic.properties[i].value == "") {
        // skip rendering Barrierefrei-Field cause value was not set yet
      } else {
        propertyList += '<p><span class="propertyLabel">'+givenTopic.properties[i].label+':&nbsp;</span>';
      }
      if (givenTopic.properties[i].type == 0) {
        if (givenTopic.properties[i].label.indexOf("Barrierefrei") == -1) {
          // ordinary rendering for DM Property Type Single Value
          propertyList += '<span class="propertyField">'+givenTopic.properties[i].value+'</span></p>';
        } else {
          // special rendering for the "BARRIERFREE_ACCESS"-Property
          if (givenTopic.properties[i].value == "") {
            // skip rendering Barrierefrei-Field cause value was not set yet
          } else if (givenTopic.properties[i].value == "Ja") {
            propertyList += '<img src="'+ICONS_URL+'green-light.png"/ width="15" height="15" border="0" style="position: relative; top: 2px;" title="Ja, barrierefreier Zugang" alt="Ja, barrierefreier Zugang"></p>';
          } else if (givenTopic.properties[i].value.indexOf("Eingeschr") != -1) {
            propertyList += '<img src="'+ICONS_URL+'orange-light.png"/ width="15" height="15" border="0" style="position: relative; top: 2px;" title="Eingeschr&auml;nkter, barrierefreier Zugang" alt="Eingeschr&auml;nkter, barrierefreier Zugang"></p>';
          } else if (givenTopic.properties[i].value == "Nein") {
            propertyList += '<img src="'+ICONS_URL+'red-light.png"/ width="15" height="15" border="0" style="position: relative; top: 2px;" title="Nein, kein barrierefreier Zugang" alt="Nein, kein barrierefreier Zugang"></p>';
          }
        }
      } else {
        // DM Property Type Multi Value
        propertyList += '<span class="propertyField">';
        for ( var k=0; k < givenTopic.properties[i].values.length; k++ ) {
          stringValue = givenTopic.properties[i].values[k].name;
          var htmlValue = "";
          if (stringValue.startsWith("http://")) {
            htmlValue = makeWebpageLink(stringValue, stringValue);
          } else if (stringValue.indexOf("@") != -1) {
            htmlValue = makeEmailLink(stringValue, stringValue);
          } else {
            htmlValue = stringValue;
          }
          propertyList += '<br/><img style="border-style: none; vertical-align: middle;" '
            + ' src="'+ICONS_URL+''+givenTopic.properties[i].values[k].icon+'"/>&nbsp;' + htmlValue;
        }
        propertyList += '</span></p>';
      }
      propertyList += '</p>';
    }
    resultHandler.append(propertyList);
    //
    updatePermaLink(baseUrl+mapAlias+"?topicId="+givenTopic.id);
  }
  
  /**
   * sends an ajax request to the google geocoder through a proxy script
   * and moves the center / focuse of the mapTiles to the first result of coordinates
   * 
   * ### TODO: improve the dynamic localization of the viewPortBias, try again to make use of mapBounds
   **/
  function focusRequest() {
    if (debug) log('focusRqeust for ' + streetFocus);
    var streetFocus = jQuery("#streetNameField").val();
    var locale = ""; // default set to de if empty by proxy-servlet
    // var swBerlin = "6881778.529613,1467590.9428711";
    // var nwBerlin = "6920608.5399765,1518650.8777585";
    var viewPortBias = "&bounds="; // +swBerlin+"|"+nwBerlin;
    //if berlinde, or no ifs or no "de" map
    if (onBerlinDe | mapTitle.indexOf("international") == -1 | mapTitle.indexOf("Deut") != -1) {
      streetFocus = jQuery("#streetNameField").val() + ' Berlin';
    } else { // ### unused
      var bounds = calculateInitialBounds();
      viewPortBias += ""+bounds.left+","+bounds.bottom+"|"+bounds.right+","+bounds.top;
    }
    // var url = PROXY_SERVLET_URL + urlencode(streetFocus) + viewPortBias + '&output=json&oe=utf8&sensotr=false&key=';
    var key = kiezKey;
    if (onBerlinDe) {
      key = berlinKey;
      locale = "de";
    }
    var body = '{"method": "oldGeoCode", "params": ["'+streetFocus+'", "'+key+'", "'+locale+'"]}';
    // var viewPortURL = "1338106.6169795,6831635.8390675|1614197.1630961,6955769.5729803";
    jQuery.ajax({
      type: "POST",
      url: SERVICE_URL,
      data: body,
      beforeSend: function(xhr) {
        xhr.setRequestHeader("Content-Type", "application/json")
        xhr.setRequestHeader("Charset", "UTF-8")
      },
      dataType: 'json',
      success: function(obj) {
        // alternative_items = null;
        autocomplete_item = 0;
        alternative_items = obj.Placemark;
        if (alternative_items.length == 1) {
          // alert("Exact 1 Reuslt for: " + alternativ_items[0].address);
          select_current_item();
          focus_current_item();
          // 
        } else if (alternative_items.length > 1) {
          // alert("More than 1 Reuslt => " + alternativ_items.length);
          show_alternatives_list(jQuery("#focusAlternatives"));
          // select_alternative_item(autocomplete_item);
          select_current_item();
          focus_current_item();
        }
      },
      error: function(x, s, e) {
        showDialog(true, "ERROR", "x: " + x + " s: " + s + " e: " + e);
        if (debug) log('.. wrong street ?! (' + x.statusText+')');
      }
    });
    if (!checkIfAllCategoriesSelected()) showAllMarker();
  }



  //
  // --- Handling of alternatives after a focusRequest() 
  //
  
  /**
   * Alternatives List for focusRequest and mapNavigation
   * inspired by the plain_document.js implementation of the deepamehta3-client
   **/
  function show_alternatives_list(input_element) {
      var innerHtml = "<i>Es wurden &auml;hnliche Orte gefunden</i><br/>";
      for (r = 0; r < alternative_items.length; r++) {
        var item = alternative_items[r];
        // var deIndex = item.address.indexOf(", Germany");
        // if (deIndex != -1) {
          // item.address = item.address.substring(0, deIndex);
        // }
        // innerHtml += '<a id="resultItem_' + r + '" href=javascript:focus_current_item(' + r + ')>' + item.formatted_address + '</a><br/>';
        innerHtml += '<a id="resultItem_' + r + '" href=javascript:focus_current_item(' + r + ')>' + item.address + '</a><br/>';
      }
      innerHtml += "<i>Pfeilsteuerung + ENTER<br/> oder ESC zum Abbrechen</i><br/>";
      // + "<b>zur Auswahl: &#8595; , &#8593; + &#8629; bzw. ESC</b><br/>";
      jQuery("#focusAlternatives").empty()
      jQuery("#focusAlternatives").css("visibility", "visible");
      jQuery("#focusAlternatives").html(innerHtml)
      jQuery("#focusAlternatives").show();
      jQuery("#focusInput").unbind();
      jQuery("#focusInput").keyup(function(event) {
        if (jQuery("#focusAlternatives").css("visibility") == "visible") {
          if (autocomplete_item < 0 || autocomplete_item > alternative_items.length) {
            jQuery("#autoLocateInputField").attr("action", "javascript:focusRequest()");
            // jQuery("#focusInput").attr("disabled", "enabled");
            if (debug) log("REQUEST" + event.keyCode);
          }else {
            jQuery("#autoLocateInputField").attr("action", "javascript:focus_current_item()");
            // jQuery("#focusInput").attr("disabled", "disabled");
            if (debug) log("FOCUS" + event.keyCode);
          }
          if (event.keyCode == 38) { // UP
            select_previous_item();
          } else if (event.keyCode == 40) { // DOWN
            if (jQuery("#focusAlternatives").css("visibility") == "hidden") {
              jQuery("#focusAlternatives").css("visibility", "visible");
            }
            select_next_item();
          } else if (event.keyCode == 27) { // ESC
            jQuery("#focusAlternatives").css("visibility", "hidden");
            autocomplete_item = 0;
            alternative_items = null;
          } else if (event.keyCode == 13) { // ENTER
            jQuery("#focusInput").submit();
            if (debug) log("ENTER submitting  but resetting ? " + autocomplete_item);
          }
        } else {
          jQuery("#autoLocateInputField").attr("action", "javascript:focusRequest()");
        }
      });
      if (debug) log("visibAlternatives:" + jQuery("#focusAlternatives").css("visibility"))
  }
  
  function select_current_item() {
      jQuery("#resultItem_" + autocomplete_item).css("background-color", "#999999");
      // autocomplete_item = atPos;
  }

  function focus_current_item(itemNumber)  {
      var item;
      if (alternative_items != null) {
        item = alternative_items[autocomplete_item]; // autoselection
        if (itemNumber != null) {
          item = alternative_items[itemNumber];
          select_next_item(itemNumber);
        }
      }
      // TODO: check if inputField.action with jQuery (line 293)
      // was later changed than inputField.keyUp is received (line 314)
      if (item != undefined) {
        // NOTE: has to be inside the map.maxExtend-Viewport otherwise request will be silently ignored by OL
        // var toLonLat = new OpenLayers.LonLat(item.geometry.location.lng, item.geometry.location.lat);
        var toLonLat = new OpenLayers.LonLat(item.Point.coordinates[0], item.Point.coordinates[1]);
        if (debug) log("alternativeFocus to Point: " + toLonLat);  
        if (!map.getMaxExtent().containsLonLat(toLonLat.transform(map.displayProjection, map.projection))) {
          var errorMessage = "Die von ihnen ausgew&auml;hlte Stra&szlig;e ist nicht Teil dieses Stadtplans.\n"
            + "Bitte geben sie zus&auml;tzliche Angaben, wie z.B. PLZ ein, oder w&auml;hlen sie eine Alternative aus.";
          showDialog(true, "Die Umkreissuche meldet:", errorMessage);
          //+ "<br/>(GPS: " + toLonLat.transform(map.displayProjection, map.projection).toString() + ").");
        } else {
          // var toLonLat= new OpenLayers.LonLat(item.Point.coordinates[0], item.Point.coordinates[1]);
          // map.setCenter(toLonLat.transform(map.displayProjection, map.projection), LEVEL_OF_DETAIL_ZOOM);
          map.panTo(toLonLat);
          map.zoomTo(LEVEL_OF_DETAIL_ZOOM); // level of detail
          // map.setCenter(toLonLat.transform(map.displayProjection, map.projection), LEVEL_OF_DETAIL_ZOOM);
          // showDialog(true, "Die Umkreissuche meldet:", "OK => (GPS: " + toLonLat.transform(map.displayProjection, map.projection).toString() + ").");
        }
      }
  }
  
  function select_next_item(num) {
      jQuery("#resultItem_" + autocomplete_item).css("background-color", "#FFFFFF");
      if (num != null) {
        autocomplete_item = num;
      } else {
        autocomplete_item++;
      }
      jQuery("#resultItem_" + autocomplete_item).css("background-color", "#999999");
      if (debug) log("selectingNextItem:" + autocomplete_item);
  }
  
  function select_previous_item() {
      jQuery("#resultItem_" + autocomplete_item).css("background-color", "#FFFFFF");
      autocomplete_item--;
      jQuery("#resultItem_" + autocomplete_item).css("background-color", "#999999");
      log("selectingPrevItem:" + autocomplete_item);
  }



  // 
  // --- Sidebar Specific GUI Code
  // 
  
  function initResultList(resultObjects, lastQuery) {
    hideProgressFromSideBar();
    var topicIdsToShow = new Array();
    jQuery("#sideBarCategories").empty();
    jQuery("#sideBarCategories").append('&nbsp;<b class=\"redTitle\">' + resultObjects.length + '</b> ');
    jQuery("#sideBarCategories").append('Suchergebnisse f&uuml;r <b class=\"redTitle\">\"' + lastQuery + '\"</b><p/>');
    jQuery("#sideBarCategories").append('<table width="100%" cellpadding="2" cellspacing="0" ' +
      'id="sideBarCategoriesTable"></table>');
    for (var i=0; i < resultObjects.length; i++) {
      var resultBaseTopic = resultObjects[i];
      if (resultBaseTopic.lat == 0.0 || resultBaseTopic.lon == 0.0) {
        if (debug) log('..initResultList - skipping ' + resultBaseTopic.name );
      } else {
        jQuery("#sideBarCategoriesTable").append('<tr id="topicRow-'+resultBaseTopic.id+'" width="100%" '
          + ' class="topicRowDeselected"><td width="20px" valign="center" align="center"><b>'+(i+1)+'. </b></td>'
          + '<td valign="center"><a href="#" id="topicRowHref-'+resultBaseTopic.id+'">' + resultBaseTopic.name + '</a></td></tr>');
        jQuery("#topicRow-"+resultBaseTopic.id).attr('onclick', 'javascript:showTopicInMap("' + resultBaseTopic.id + '");');
        jQuery("#topicRowHref-"+resultBaseTopic.id).attr('href', 'javascript:showTopicInMap("' + resultBaseTopic.id + '");');
        topicIdsToShow.push(resultBaseTopic.id);
      }
    }
    // showTopicsInMap(resultObjects);
    showTopicFeatures(topicIdsToShow, "");
  }
  
  /**
   * depends on an available #sideBarCriterias div-Container
   **/
  function initCriteriaList() {
    jQuery("#progContainer").hide();
    var critListElement = jQuery("#sideBarCriterias");
    var onEventMap = (mapTitle.indexOf("Veranstaltungen Ehrenamt Berlin") != -1) ? true : false;
    var onProjectMap = (mapTitle.indexOf("Ehrenamt Berlin") != -1) ? true : false;
    var tabsHtml = "";
    if (onBerlinDe && onEventMap) {
      tabsHtml = '<div id="navigation-helper" '
          + 'style="border-bottom: 1px dashed #e8e8e8; padding-left: 4px; padding-bottom: 3px; padding-top:0px; padding-right: 4px;">'
          + '<a href="'+ baseUrl +'ehrenamt" title="Zum Einsatzstadtplan wechseln">Einsatzorte</a>&nbsp;|&nbsp;'
          + 'Veranstaltungen</div>';
    } else if (onBerlinDe && onProjectMap) {
      tabsHtml = '<div id="navigation-helper" '
          + 'style="border-bottom: 1px dashed #e8e8e8; padding-left: 4px; padding-bottom: 3px; padding-top:0px; padding-right: 4px;">'
          + 'Einsatzorte&nbsp;|&nbsp;'
          + '<a href="'+ baseUrl +'veranstaltungen-ehrenamt" title="Zum Veranstaltungsstadtplan wechseln">Veranstaltungen</a></div>';
    }
    var critLinkList = '';
    if (onBerlinDe && (onEventMap || onProjectMap)) {
      critLinkList += tabsHtml; // render special tab selection for inner ehrenamtsnetz navigation
    }
    critLinkList += '<table width="95%" cellpadding="0" border="0"><tbody>';
    critLinkList += '<tr valign="top">'; // TODO: onclick
    critLinkList += '<td rowspan="'+workspaceCriterias.result.length+1+'" align="left">';
    // rebuild upper part of the sideBar stub
    critLinkList += '<a id="sideBarLogoLink" href="http://www.kiezatlas.de">'
      + ' <img id="sideBarLogo" src="'+IMAGES_URL +'kiezatlas-logo.png" alt="Das Kiezatlas Logo" '
      + ' border="0" text="Das Kiezatlas Logo"/></a></td>';
    critLinkList += '<td></td><td></td>';
    critLinkList += '</tr>';
    for (var i = 0; i < workspaceCriterias.result.length; i++) {  
      var critName = [workspaceCriterias.result[i].critName];
      if ( i == 0 && workspaceCriterias.result.length == 2) {
        critLinkList += '<tr valign="center">';
      } else {
        critLinkList += '<tr valign="top">';
      }
      critLinkList += '<td onclick="javascript:updateCategoryList(' + i + ');" align="right" class="critLinkNormal">'
        + critName + '</td>';
      if (crtCritIndex == i) {
        critLinkList += '<td align="left">&#8226;</td></tr>';
      } else {
        critLinkList += '<td></td></tr>';
      }
    }
    critLinkList += '</tbody>';
    critLinkList += '</table>';
    // do append the concatenated html
    critListElement.html(critLinkList);
    if (!onBerlinDe) {
      var breadCrumpHtml = '<div id="navigation-helper">'
          + '<a href="http://www.kiezatlas.de/browse/'+mapAlias+'" title="Zur klassichen Stadtplanoberfl&auml;che wechseln">Zur klassischen Ansicht</a></div>';
      critListElement.append(breadCrumpHtml);
    }
    // set the correct images
    // if (workspaceInfos != null) setCustomWorkspaceInfos(); else setDefaultWorkspaceInfos();
    setWorkspaceInfos();
  }

  /**
   * renders a listing of slim-items displaying all geo-objects associated with one given category
   * note: caches categoryId in our little helper list of markerGroupIds for later re-selection
   **/
  function showCatInSideBar(catId, catName) {
    reSetMarkers(); // clean up the category and map state
    showDialog(false); // hide our info-dialog, if necessary
    //
    var topicsToShow = getAllTopicsInCat(catId);
    topicsToShow.sort(topicSort); // alphabetical ascending
    var topicIdsToShow = new Array();
    //
    var sideBarCategories = jQuery("#sideBarCategories");
    sideBarCategories.empty();
    sideBarCategories.append('&nbsp;<b class="redTitle"><a href="javascript:renderCritCatListing('+ crtCritIndex+')" title="Zur&uuml;ck">' +catName + '</a></b><br/>&nbsp;&nbsp;');
    sideBarCategories.append('<small>('+topicsToShow.length+ ' Objekte)</small><p/>');
    sideBarCategories.append('<table width="100%" cellpadding="2" cellspacing="0" id="sideBarCategoriesTable"></table>');
    for (var i = 0; i < topicsToShow.length; i++) {
      jQuery("#sideBarCategoriesTable").append('<tr width="100%" class="topicRowDeselected">' 
        + '<td width="20px" class="iconCell" valign="center" align="center">'
          + '<img src="http://www.berlin.de/imperia/md/images/system/icon_punkt_rot.gif"/></td>'
        +' <td><a href="#" id="topicRowHref-'+topicsToShow[i].id+'">'+topicsToShow[i].name+'</a></td></tr>');
      jQuery("#topicRow-"+topicsToShow[i].id).attr('onclick', 'javascript:showTopicInMap("'+topicsToShow[i].id+'");');
      jQuery("#topicRowHref-"+topicsToShow[i].id).attr('href', 'javascript:showTopicInMap("'+topicsToShow[i].id+'");');
      topicIdsToShow.push(topicsToShow[i].id);
    }
    // mark category as currently selected, visible
    markerGroupIds.push(catId);
    // showTopicsInMap(topicsToShow);
    showTopicFeatures(topicIdsToShow, catId);
    // calculateNewBounds if its a "District" criteria
    if (onBerlinDe) { // ### fixed hack
      for (i = 0; i < districtNames.length; i++) {
        if (districtNames[i].catName == catName) {
          var districtBounds = getBoundsOfCurrentVisibleFeatures(); // out features inside
          updateVisibleBounds(districtBounds, false, LEVEL_OF_DISTRICT_ZOOM);
        }
      }
    }
  }

  /** resetsMarkers, removePopups, and renders the current criteria and category list */
  function updateCategoryList(criteriaIndex) {
    // clear`s catList and hides all visible marker
    reSetMarkers();
    hideAllInfoWindows();
    renderCritCatListing(criteriaIndex);
  }

  /**
   * a) (@see updateCategoryList) initializes the upper list of criterias + lower list of categories based on given idx
   * b) (@see showCatInSideBar-Href-Back-Trigger) initializes the upper list of criterias + renders the list of
   *    categories but the formerly selected one as selected
   * info: displays markerGroupIds[catId] as selected
   ***/
  function renderCritCatListing(criteria) {
    crtCritIndex = criteria; // update the in memory criteria index/pointer
    updatePermaLink(baseUrl+mapAlias+"?critId="+(parseInt(criteria)+1)); // permalink: cause users start counting from 1
    // 
    initCriteriaList(); // based on crtCritIndex, also sets the WorkspaceInfos
    var sideBarCategories = jQuery("#sideBarCategories");
    sideBarCategories.empty();
    sideBarCategories.append('<p/>'); // formerly: &nbsp;&nbsp;<b class="redTitle">Informationsebenen sind: </b></p>
    sideBarCategories.append('<table width="97%" cellpadding="2" cellspacing="0" id="sideBarCategoriesTable"></table>');
    // sideBarCategories.html('<table width="95%">');
    // var contentWidth = jQuery("#sideBar").css("width").substr(0,jQuery("#sideBar").css("width").length-2);
    if (workspaceCriterias.result.length <= 0) {
      // ### TODO: Exception Handling
      alert("Sorry for that inconvenience. Probably an error occured while loading the criterias.");
    } else {
      for (var i = 0; i < workspaceCriterias.result[crtCritIndex].categories.length; i++) {
        // Schleife über alle Kategorien eines Kriteriums
        var catIcon = [workspaceCriterias.result['' + crtCritIndex + ''].categories[i].catIcon];
        var catName = [workspaceCriterias.result['' + crtCritIndex + ''].categories[i].catName];
        var catId = new String([workspaceCriterias.result['' + crtCritIndex + ''].categories[i].catId]);
        var catCSS = "catRowDeselected";
        if (isCategoryVisible(catId)) {
          catCSS = "catRowSelected";
        }
        //
        var html = '<tr id="catRow-'+catId+'" width="100%" class="'+catCSS+'">'
          + ' <td width="25px" class="iconCell" valign="center" align="center"><a href="" id="toggleHref-'+catId+'">'
            + '<img src="'+ICONS_URL+''+catIcon+'" border="0" id="catIconRow-'+catId+'" alt="'+catName+'-Icon" '
            + 'text="Klicken zum Ein- und Ausblenden"/></a></td>'
          + '<td valign="center"><a href="" id="catHref-'+catId+'">'+catName+'</a></td></tr>';
        jQuery("#sideBarCategoriesTable").append(html); // injectin into the table structure^^
        //
        // mozilla alows onclick on a tableRow while webkit and others do neither allow onmouseclick nor onclick
        jQuery("#toggleHref-"+catId).attr('href', 'javascript:toggleMarkerGroups("' + catId + '");');
        jQuery("#catHref-"+catId).attr('href', 'javascript:showCatInSideBar("' + catId + '", "'+catName+'");');
        // registering ui effects
        // jQuery("#catRow-"+catId).attr('onmouseover', 'hoverCatButton("' + catId + '")');
        // jQuery("#catRow-"+catId).attr('onmouseout', 'outCatButton("' + catId + '")');
      }
    }
  }

  /** sets imprint, homepage and logo link associated with the current workspaceInfos */
  function setWorkspaceInfos() {
    var footerMessage = "";
    if (onBerlinDe) {
      footerMessage = '<b><a href="http://www.kiezatlas.de">Kiezatlas</a></b> '
	      + 'is powered by <a href="http://www.deepamehta.de">DeepaMehta</a>';
    } else {
      footerMessage = '<b><a href="http://www.kiezatlas.de">Kiezatlas</a></b> '
	      + 'is powered by <a href="http://www.deepamehta.de">DeepaMehta</a>';
    }
    var footer = '<span id="footerImprint"><a href="'+workspaceImprint+'">Impressum / Haftungshinweise</a></span>';
    footer += '<span id="footerPoweredBy">'+footerMessage+'</span>';
    jQuery("#kafooter").html(footer);
    jQuery("#sideBarLogo").attr('src', '' + IMAGES_URL + workspaceLogo);
    jQuery("#sideBarLogo").attr('title', 'Das KiezAtlas Logo (Version 1.6.8.2)');
    jQuery("#sideBarLogo").attr('alt', mapTitle + ' Logo');
    jQuery("#sideBarLogoLink").attr('href', workspaceHomepage);
    if (debug) log("set sideBar to: " + jQuery("#sideBarLogo").attr('src') + " and logo is: " + jQuery("#sideBarLogo").attr('title'));
  }

  function isCategoryVisible(myCatId) {
    // log('howManyCatsVisible: ' + markerGroupIds.length);
    for (var i = 0; i < markerGroupIds.length; i++) {
      if (markerGroupIds[i] == myCatId) {
        return true;
      }
    }
    return false;
  }

  // compare "a" and "b" in some fashion, and return -1, 0, or 1
  function topicSort(a, b) {
    var nameA = a.name.toLowerCase();
    var nameB = b.name.toLowerCase();
    if (nameA < nameB) // sort string ascending
      return -1
    if (nameA > nameB)
      return 1
    return 0 //default return value (no sorting)
  }

  /** convenient method to render listing of categories according to the latest chosen criteria */
  function showCritCatList() {
    // reSets all Markers, removes all Popups and renders criteria list
    updateCategoryList(crtCritIndex);
  }

  /** produces a very clear markerGroupIds situation*/
  function selectAllCategories() {
    markerGroupIds = []; // clear all, then put every catId inside
    //for (var i = 0; i < workspaceCriterias.result.length; i++) {
    for (var j = 0; j < workspaceCriterias.result[crtCritIndex].categories.length; j++) {
      var catId = workspaceCriterias.result[crtCritIndex].categories[j].catId;
      markerGroupIds.push(catId);
      jQuery("#catRow-"+catId).attr("class", "catRowSelected");
    }
    log("selected All markerGroups: " + markerGroupIds);
  }

  function checkIfAllCategoriesSelected() {
    if (markerGroupIds.length == workspaceCriterias.result[crtCritIndex].categories.length) return true; else return false;
  }

  /** produces a very clear markerGroupIds state */
  function deSelectAllCategories() {
    markerGroupIds = [];
    if (workspaceCriterias.result.length <= 0) {
      // ### ToDo: Exception Handling
      alert("Sorry for that inconvenience. Probably an error occured while loading the criterias.");
    } else {
      for (var j = 0; j < workspaceCriterias.result[crtCritIndex].categories.length; j++) {
        var catId = workspaceCriterias.result[crtCritIndex].categories[j].catId;
        // remove catId from groups
        jQuery("#catRow-"+catId).attr("class", "catRowDeselected");
      }
    }
    if(debug) log("deSelected All markerGroups to: " + markerGroupIds);
  }



  // --
  // --- Map specific GUI Code
  // --

  /**
    * renders numerous marker on the vector layer with their "default"-style
    * (@see initResultList, showCatInSideBar)
    * info: is used to display search or catSearch results _ontop or independent of already visible categories_
    */
  function showTopicsInMap(topicsToShow) {
    for (var i = 0; i < topicsToShow.length; i++) {
      var topic = topicsToShow[i];
      var featureToToggle = checkFeatureById(topic.id);
      if (featureToToggle != null) {
        featureToToggle.renderIntent = "default";
      } else {
        log("[ERROR] no feature found for " + topic.id );
      }
    }
    // rerender
    myNewLayer.redraw();
  }

  /**
    * this method is used widely for selecting a normal or clustered feature which corresponds to a topicId
    * it shows the name of the topic in an map internal infowindow
    */
  function showTopicInMap(topic) {
    // var featureToToggle = checkFeatureById(topic);
    var featureToToggle = checkDrawnFeaturesForTopicId(topic); // also returns a feature if this id is part of a cluster
    if (featureToToggle != null) {
      // the feature is currently drawn
      var position = new OpenLayers.LonLat(featureToToggle.data.lon, featureToToggle.data.lat);
      map.panTo(position);
      map.zoomTo(LEVEL_OF_DETAIL_ZOOM); // level of detail
      // log("[INFO]: panning to: " + position);
      if (featureToToggle.data.topicId != topic) { // the topic is drawn in a cluster..
        showInfoWindowForMarker(featureToToggle.data, topic);
        if (debug) log("showTopicInCluster: " +topic + "data:" + featureToToggle.data.cluster);
      } else {
        showInfoWindowForMarker(featureToToggle.data);
      }
      featureToToggle.renderIntent = "select";
      myNewLayer.redraw();
    } else {
      // the feature is currently not drawn
      featureToToggle = checkFeatureByTopicId(topic);
      // featureToToggle.draw();
      featureToToggle.renderIntent = "select";
      myNewLayer.redraw();
      // move map to poi
      var position = new OpenLayers.LonLat(featureToToggle.data.lon, featureToToggle.data.lat);
      map.panTo(position);
      map.zoomTo(LEVEL_OF_DETAIL_ZOOM); // level of detail
      showInfoWindowForMarker(featureToToggle.data);
    }
  }

  /**
   * called when single topics are linked in, addressed from outside
   * @see jQuery.load(...)
   **/
  function selectAndShowInMap(originId, isTopicId) {
    var feature = null;
    if (isTopicId) {
      feature = checkFeatureByTopicId(originId);
    } else {
      feature = checkFeatureByOriginId(originId);
    }
    if (feature == null) {
      // project could not be associated with a correct address, though is not published
        var helpHtmlOne = '<br/><b class="redTitle">Entschuldigen sie bitte, die Projektadresse ist unbekannt.</b><p/> '
          + 'F&uuml;r diese <i>Einsatzm&ouml;glichkeit</i> ist die Adresse des Einsatzortes nicht bekannt bzw. '
          + 'fehlerhaft und daher k&ouml;nnen wir ihnen an dieser Stelle keine zus&auml;tzlichen Informationen anzeigen. <p/>'
          + ' Die Kontaktinformationen zu dieser <i>Einsatzm&ouml;glichkeit</i> erhalten sie auf der '
          + ' <a href="http://www.berlin.de/buergeraktiv/ehrenamtsnetz/angebote/'
          + 'index.cfm?dateiname=ea_projekt_beschreibung.cfm&cfide=0.304475484697&&anwender_id=5&id=0&ehrenamt_id=0&projekt_id='
          + originId +'&seite=1&organisation_id=0">vorherigen Seite.</a><br/>';
          helpHtmlOne += '<br/> Sie k&ouml;nnen nat&uuml;rlich auch in dieser Ansicht weiter nach <a href="javascript:updateCategoryList(1);">Einsatzm&ouml;glichkeiten</a> in ihrer Umgebung navigieren.';
        jQuery("#sideBarCategories").html(helpHtmlOne);
    } else {
      // log("linking in and showing " + feature.data.topicName);
      // TODO: select some categories when user got linked in..
      // TODO: createSlimGeoObject(has to assemble the crtierias with the help of CityMpa.getSeachCriteria() 
      // to mach always to the first criteria);
      // NOTE: just works for the herewith fixed default 2
      // var topCatId = getTopicById(feature.data.topicId).criterias[1].categories[0];
      // if (topCatId  != undefined) toggleMarkerGroups(topCatId);
      //
      showTopicInMap(feature.data.topicId);
      //
      showTopicInSideBar(feature.data.topicId);
    }
  }

  /**
   * NOTE: topic must be already rednered in map, otherwise method will fail, just call showTopicInMap(topicId) in before
   * Focus and show infos for a drawn topic in map and load the corresponding data container
   **/
  function showTopicInSideBar(topicId) {
    // sideBar related stuff
    //
    var handler = jQuery("#sideBarCategories");
    handler.empty();
    jQuery("#progContainer").show("fast");
    getGeoObjectInfo(topicId, handler);
    //
    /** var topicFeature = checkDrawnFeaturesForTopicId(topicId);
    if ( topicFeature != null ) {
      topicFeature.renderIntent = "select";
      myNewLayer.redraw();
      // topi undrawn but infowMarker is still there, which is OK
      showInfoWindowForMarker(topicFeature.data);
    } */
  }

  function getAllTopicsInCat(catId) {
    var topics = new Array();
    //function get
    for (var i = 0; i < mapTopics.result.topics.length; i++) {
      for (var j = 0; j < mapTopics.result.topics[i].criterias.length; j++) {
        for (var k = 0; k < mapTopics.result.topics[i].criterias[j].categories.length; k++) {
          if (catId == mapTopics.result.topics[i].criterias[j].categories[k]) {
              // log("hiding topicId:"+mapTopics.result.topics[i].id);
              topics.push(mapTopics.result.topics[i]);
          }
        }
      }
    }
    return topics;
  }  

  function toggleMarkerGroups(category) {
    showDialog(false);
    //var catId = "t-"+category;
    var catSelected = jQuery("#catRow-"+category).attr("class");
    log('toggleMarkerGroup ' +category+ ' is ' + catSelected);
    if (catSelected == "catRowDeselected") {
      // catId was not selected, but is now
      // add catId to our little helper list
      markerGroupIds.push(category);
      var topics = showMarkerGroup(category);
      showTopicFeatures(topics, category);
      log('<b>MarkerGroupIds before showing: ' + markerGroupIds.toString() + ' in which are: '+topics.length+'</b>');
    } else {
      // remove catId from our little helper List
      for (var m = 0; m < markerGroupIds.length; m++) {
        if (category == markerGroupIds[m]) {
          markerGroupIds.splice(m,1); // = null; // delete catId from the list of currently visible categories
          // printOut('cleaned up List.. ' + markerGroupIds.length + '.. removed ' + category);
        }
      }
      var topicsToToggle = hideMarkerGroup(category);
      hideTopicFeatures(topicsToToggle);
      if (debug) log('.toggleMarkerGroups.before hiding: ' + markerGroupIds.toString() + ', '+topics.length+'</b>');
    }
  }

  /** ### FIXME: cleanup **/
  function hideTopicFeatures(topicListToHide) {
    // log("..starting to Hide "+topicListToHide.length+"Features");
    for (m=0; m<topicListToHide.length; m++) {
      var id = topicListToHide[m];
      // visible, but just maybe to hide
      var catIds = getMarkerCategories(id);
      var showMarker = false;
      // if _any category is visible, topic/mark it to be shown
      for (var j = 0; j < catIds.length; j++) {
        if (isCategoryVisible(catIds[j])) {
          showMarker = true;
          // log("> leaving topic right in map " + topicId + ' cause ' + catIds[j] + ' is in ' + markerGroupIds);
          // do nothing more, for this topic its decided
        }
      }
      var featureToHide = checkDrawnFeaturesForTopicId(id);
      if (featureToHide != null) {
        if (featureToHide.data.topicId == id) {
          // hide ?? 
          if (!showMarker) {
            // feature ausblenden
            featureToHide.renderIntent = "delete";
          }
        } else if (featureToHide.data.cluster != null) {
          // or if this is a cluster then check if topic is indirect visible in here
          if (!showMarker) {
            for(j=0; j<featureToHide.data.cluster.length; j++) {
              if (featureToHide.data.cluster[j].topicId == topicId) {
                // remove it from cluster ??
                featureToHide.data.cluster.splice(j,1);
                if (featureToHide.data.cluster.length == 1) {
                  featureToHide.attributes.marker = "normal"; // clusterFeature, not
                  featureToHide.data.cluster = null;
                  log("[INFO] transforming a former cluster into a normal marker..");
                }
              }
            }
          }
        }
      } else {
        if (debug) log("hiding Feature which was not drawn: " + topicId);
      }
    }
    if (myNewLayer != null) myNewLayer.redraw();
  }

  function showTopicFeatures(topicListToShow, catIdToShow) {
    // log("..starting to show "+topicListToShow.length+"Features");
    // var boundingFeatures = new Array();
    var catIconURL = "";
    if(catIdToShow != "") {
      catIconURL = getCatIconURL(catIdToShow);
    }
    if (debug) log("catIconToShow is "+catIconURL);
    for ( m = 0; m < topicListToShow.length; m++ ) {
      var id = topicListToShow[m];
      var featureToShow = checkDrawnFeaturesForTopicId(id);
      if ( featureToShow != null ) { // is wether already drawn in cluster or normal, no need for handling it again
        // skipping
      } else {
        featureToShow = checkFeatureById(id);
        if ( featureToShow != null ) {
          // there was a feature for this topic initialized
          var pos = new OpenLayers.LonLat(featureToShow.data.lon, featureToShow.data.lat);
          // for a visible feature on that position which is not the same topic id
          // // (occurs because topics are in multiple categories)
          var clusterFeature = checkLayerForVisibleFeatureOnPosition(pos, id);
          if ( clusterFeature != null ) {
            // on this position there is already a feature drawn, make it a cluster or at least append it
            // clusterFeature.data.cluster.push(featureToShow.data);
            if (featureToShow.data.cluster == null) { // is a new cluster
              var newCluster = new Array();
              // check if data.cluster is an Array of feature.data objects ???
              if ( clusterFeature.data.cluster == null ) { // starting new cluster
                newCluster.push(clusterFeature.data);
                newCluster.push(featureToShow.data);
                if (debug) log("> starting a cluster at " + pos + " with " + clusterFeature.data.topicName + " / " + featureToShow.data.topicName);
              } else {
                for(j=0;j<clusterFeature.data.cluster.length;j++) {
                 newCluster.push(clusterFeature.data.cluster[j]);
                } // building new cluster through appending
                newCluster.push(featureToShow.data);
                // log("> extending a cluster at " + pos + " with " + featureToShow.data.topicName);
              }
            } else {
              // is a cluster which is already there but probably wants to have some new topics in its content
              // TODO:
            }
            // rendering features
            clusterFeature.data.cluster = newCluster;
            if ( catIconURL == "" || catIconURL == "blackdot.gif" ) { // http://www.kiezatlas.de/client/icons/
              // paint a circle instead
              // clusterFeature.attributes.renderer = "circle";
              // clusterFeature.attributes.renderer = "icon";
              clusterFeature.attributes.marker = "hotspot";
              clusterFeature.attributes.size = "20";
              if (onBerlinDe) {
                clusterFeature.attributes.label = "mehrere Einsatzmöglichkeiten";
              } else {
                clusterFeature.attributes.label = "Hotspot";
              }
              clusterFeature.attributes.iconUrl = ICONS_URL+"FreiwilligenAgentur.png";
              clusterFeature.renderIntent = "default";
            } else {
              clusterFeature.attributes.marker = "hotspot";
              // clusterFeature.attributes.renderer = "icon";
              clusterFeature.attributes.size = "20";
              clusterFeature.attributes.iconUrl = ICONS_URL+catIconURL;
              clusterFeature.renderIntent = "default";
            }
            // boundingFeatures.push(clusterFeature);
          } else {
            // normal show
            featureToShow = checkFeatureById(id);
            //featureToShow.attributes.renderer = "icon";
            if ( catIconURL == "" || catIconURL == "blackdot.gif" ) { // http://www.kiezatlas.de/client/icons/
              featureToShow.attributes.size = "15";
              featureToShow.attributes.iconUrl = ICONS_URL+"FreiwilligenAgentur.png";
              featureToShow.renderIntent = "default";
            } else {
              featureToShow.attributes.renderer = "icon";
              featureToShow.attributes.size = "15";
              featureToShow.attributes.iconUrl = ICONS_URL+catIconURL;
              featureToShow.renderIntent = "default";
            }
            // boundingFeatures.push(featureToShow);
          }
        }
      }
    }
    if (myNewLayer != null) myNewLayer.redraw();
  }

  /** operates on the original mapTopics result from the mapservice */
  function hideMarkerGroup(category) {
    var topicsToHide = new Array();
    for (var i = 0; i < mapTopics.result.topics.length; i++) {
      for (var j = 0; j < mapTopics.result.topics[i].criterias.length; j++) {
        for (var k = 0; k < mapTopics.result.topics[i].criterias[j].categories.length; k++) {
          if (category == mapTopics.result.topics[i].criterias[j].categories[k]) {
            // log("hiding topicId:"+mapTopics.result.topics[i].id);
            // toggleMarkerById(mapTopics.result.topics[i].id);
            topicsToHide.push(mapTopics.result.topics[i].id);
          }
        }
      }
    }
    // printOut("toHide: " + category);
    jQuery("#catRow-"+category).attr("class", "catRowDeselected");
    // var catHover = "catRowDeselected";
    log('hidingMarkerGroup and catIdRow is now: ' + jQuery("#catRow-"+category).attr("class"));
    //TODO: If there's any infoWindow still open, close it .. map.closeInfoWindow();
    return topicsToHide;
  }

  /** operates on the original mapTopics result from the mapservice */
  function showMarkerGroup(category) {
    var topicsToShow = new Array();
    for (var i = 0; i < mapTopics.result.topics.length; i++) {
      for (var j = 0; j < mapTopics.result.topics[i].criterias.length; j++) {
        for (var k = 0; k < mapTopics.result.topics[i].criterias[j].categories.length; k++) {
          if (category == mapTopics.result.topics[i].criterias[j].categories[k]) {
            // log("reveal topicId:"+mapTopics.result.topics[i].id);
            // toggleMarkerById(mapTopics.result.topics[i].id);
            topicsToShow.push(mapTopics.result.topics[i].id);
          }
        }
      }
    }
    // printOut(' / adding'  + category  + ' / ');
    jQuery("#catRow-"+category).attr("class", "catRowSelected");
    log('showingMarkerGroup and catIdRow is now: ' + jQuery("#catRow-"+category).attr("class"));
    return topicsToShow;
  }
  
  /** toggles a OpenLayersMarker for a given topicId 
    * toggles a OpenLayersFeature on myNewLayer for a given topicId (it`s definitely already on the layer, just hide or show it)
    *
    * addingNote: 
    * TODO: use, react and maintain the hotspot list
    */
  function toggleMarkerById(topicId) {
    // myNewLayer
    var featureToToggle = checkFeatureById(topicId);
    if (featureToToggle != null) {
      // console.log("featureObject: is ", featureToToggle);
      var featureRenderState = featureToToggle.renderIntent;
      // log("featureToToggle is available and visible: " + featureRenderState);
      // console.log("featureRenderState", featureRenderState);
      if (featureRenderState == "delete") {
        // not visible, definitely show
        featureToToggle.renderIntent = "default";
        myNewLayer.redraw();
        // featureToToggle.toState("default");
      } else {
        // visible, but maybe delete
        var catIds = getMarkerCategories(topicId);
        var showMarker = false;
        // if _any category is visible, topic/mark it to be shown
        for (var j = 0; j < catIds.length; j++) {
          if (isCategoryVisible(catIds[j])) {
          showMarker = true;
          // log("> leaving topic right in map " + topicId + ' cause ' + catIds[j] + ' is in ' + markerGroupIds);
          // do nothing more, for this topic its decided
          }
        }
        if (!showMarker) {
          // feature ausblenden
          featureToToggle.renderIntent = "delete";
          // console.log("featureIntent:", featureToToggle);
          myNewLayer.redraw();
        }
      }
    }
  }

  function getMarkerCategories(topicId) {
    var topic = getTopicById(topicId);
    var cats = new Array();
    if (topic == null) {
      log('Error for ' + topicId + '  ');
    } else {
      for (var i = 0; i < topic.criterias.length; i++) {
        for (var j = 0; j < topic.criterias[i].categories.length; j++) {
          cats.push(topic.criterias[i].categories[j]);
        }
      }
    }
    return cats;
  }

  /** TODO: cleanup... **/
  function initBerlinDistrictsLayer() {
    var dStyle = new OpenLayers.Style( { 
      strokeColor: "#4170D4", strokeWidth: 1.5, fill: 0
      // fontWeight: "bold", labelAlign: "${align}",
      // labelXOffset: "${xOffset}", labelYOffset: "${yOffset}"
    });
    var defaultStyle = OpenLayers.Util.applyDefaults( dStyle, OpenLayers.Feature.Vector.style["default"]);
    var dStyleMap = new OpenLayers.StyleMap({
      "default": defaultStyle, 
      "select": {strokeColor:"#B60033", strokeWidth: 2, fill: 0,
        label : "${name}", fontSize: "12px", fontStyle: "bold",
        fontFamily: "Arial,Helvetica,sans-serif", fontColor: "#B60033"}
    });
    var districtLayer = new OpenLayers.Layer.Vector("Bezirksgrenzen", {
      styleMap: dStyleMap, zIndexing: true,
      projection: map.displayProjection,
      strategies: [new OpenLayers.Strategy.Fixed()],
      protocol: new OpenLayers.Protocol.HTTP({
        url: "http://www.kiezatlas.de/maps/embed/img/districts.kml",
        format: new OpenLayers.Format.KML({
          extractStyles: false,
          extractAttributes: true
        })
      })
    });
    //
    districtLayer.setVisibility(debug);
    map.addLayer(districtLayer);
    // alert("districtLayoutIndex: " + map.getLayerIndex(districtLayer));
    if (debug) log('districtLayer loaded with overall: ' + districtLayer.features.length + ' polygons');
    var districtSelect = new OpenLayers.Control.SelectFeature(districtLayer, {highlightOnly: true});
    //selecting kml based Features in the new layer disabled for now
    /** districtLayer.onSelect = function (feature) {
      // "featureselected": function(evt) { log("districtLayer clicked " + evt); },
      // "featurehighlighted": function(evt) { log("districtLayer highlight " + evt); }
      feature.graphicZIndex = 100;
      feature.zIndex = 100;
      feature.zIndex = 100;
      feature.zIdx = 100;
      // feature.renderIntent = "select";
    }
    districtLayer.onUnselect = function (feature) {
      // "featureselected": function(evt) { log("districtLayer clicked " + evt); },
      // "featurehighlighted": function(evt) { log("districtLayer highlight " + evt); }
      feature.graphicZIndex = 0;
      feature.zIndex = 0;
      feature.zIndex = 0;
      feature.zIdx = 0;
      // feature.renderIntent = "select";
    }
    */
    map.addControl(districtSelect);
    districtSelect.activate();
    //
  }

  /**
   * initializes myNewLayer and allFeatures
   * TOOD: cleanup..
   **/
  function initLayerAllFeatures(points, mainMap) {
    var context = function(feature) {return feature;} // a magic line from somewhere..
    var myStyle = new OpenLayers.Style( { 
      graphicName: "circle", fillOpacity: "1", fillColor: "#378fe0", strokeColor: "blue", pointRadius: 5, 
      graphicTitle: "${label}", labelYOffset: "7px", externalGraphic: "${iconUrl}", graphicWidth: "${size}", 
      fontSize: "10px", fontFamily: "Verdana, Arial", fontColor: "#ffffff"} );
    var symbolizer = OpenLayers.Util.applyDefaults( myStyle, OpenLayers.Feature.Vector.style["default"]);
    var myStyleMap = new OpenLayers.StyleMap({"default": symbolizer,
      "select": {strokeColor:"red", fillOpacity: "1", fillColor:"white", strokeWidth: 2 , graphicWidth: 21},
      "temporary": {strokeColor:"white", fillOpacity: "1", fillColor: "blue", strokeWidth: 2, graphicWidth: 21}
    });
    //"hotspot": {pointRadius: 8}});
    var lookup = {
      "normal": {pointRadius: 5}, // normal
      "hotspot": {pointRadius: 7} // hotspot / cluster
    };
    myStyleMap.addUniqueValueRules("default", "marker", lookup);
    // myStyleMap.addUniqueValueRules("temporary", "label", labelook);
    myNewLayer = new OpenLayers.Layer.Vector('Kiezatlas Marker', {
      styleMap: myStyleMap, displayInLayerSwitcher: false
      // strategies: [ new OpenLayers.Strategy.Cluster() ]
    });
    var selectFeatureHandler = new OpenLayers.Control.SelectFeature(myNewLayer, {
      multiple: false, clickout: false, toggle: false,
      hover: false, highlightOnly: false, renderIntent: "select",
      onSelect: function() {
        // jQuery("#memu").css("visibility", "hidden");
      }
    });
    mainMap.addControl(selectFeatureHandler);
    selectFeatureHandler.activate();
    //
    var featureHandler = new OpenLayers.Handler.Feature(selectFeatureHandler, myNewLayer, {
      //stopClick: true,
      stopUp: true, stopDown: true,
      click: function(feat) {
        for ( i = 0; i < myNewLayer.selectedFeatures.length; i++) {
          selectFeatureHandler.unselect(myNewLayer.selectedFeatures[i]);
        }
        showInfoWindowForMarker(feat.data);
        selectFeatureHandler.select(feat);
      }, // clickFunction
      clickout: function (feat) {
        selectFeatureHandler.unselect(feat);
        hideAllInfoWindows();
      }
    }); // end FeatureHandlerInit
      /* commented out the mouseover cluster menu  */
    var highlightCtrl = new OpenLayers.Control.SelectFeature(myNewLayer, {
      hover: true, highlightOnly: true,
      renderIntent: "temporary",
      eventListeners: { // makes use of the global propertyMap for eventListeners
        beforefeaturehighlighted: function(e) {
          e.feature.attributes.label = e.feature.data.topicName;
          // no menu just label
          var marker = e.feature.attributes.marker;
          if (marker == "hotspot") {
            e.feature.attributes.label = "mehrere Einsatzmoeglichkeiten";
          }
        },
        // ### ToDo: mostly unused and to be removed
        /* featurehighlighted: function(e) {
          var marker = e.feature.attributes.marker;
          if (marker == "hotspot") {
            //log("hotSpotFeature highlght, to show contextMenu at l:" + e.feature.geometry.bounds.getCenterPixel()); // + "b:"+ e.feature.geometry.bounds.bottom);
            var centerPoint = myNewLayer.getViewPortPxFromLonLat(e.feature.geometry.bounds.getCenterLonLat());
            var htmlString = "";
            if ( e.feature.data.cluster != null && e.feature.data.cluster != undefined ) {
                /* for ( i = 0; i < e.feature.data.cluster.length; i++) {
                // htmlString += '<a href=javascript:showInfoWindowForTopicId("'
                  // + e.feature.data.cluster[i].topicId+'");>'+e.feature.data.cluster[i].topicName+'</a><br/>';
                }
                // jQuery("#memu").html(htmlString);
                // jQuery("#memu").css("visibility", "visible");
                // jQuery("#memu").css("left", centerPoint.x);
                // jQuery("#memu").css("top", centerPoint.y + headerGap + 27); // ### headergap seems unneccessary
            }
          } else {
            // log("normalFeature just highlight");
            // e.feature.attributes.label = "";
          }
        }, */
        featureunhighlighted: function(e) {
            // TODO: is wrong one, if one is already selected and the user wants to deal with a cluster
            // log("feature" + e.feature.data.topicId + " unhighlighted");
            var marker = e.feature.attributes.marker;
            if (marker == "hotspot") {
              jQuery("#memu").css("visibility", "hidden");
              // var testXY = e.feature.geometry.clone().transform(map.projection, map.displayProjection);
              // log("hotSpotFeature highlght, to hide contextMenu at l:" + myNewLayer.getViewPortPxFromLonLat(testXY));
              // + "t:"+ e.feature.geometry.bounds.top);
            } else {
              // e.feature.attributes.label = " ";
            }
        }
      } // eventListeners end
    });
    mainMap.addControl(highlightCtrl);
    highlightCtrl.activate();
    featureHandler.activate();
    allFeatures = [points.length];
    for ( var i = 0; i < points.length; i++ ) {
      allFeatures[i] = new OpenLayers.Feature.Vector (
        new OpenLayers.Geometry.Point(points[i].lonlat.lon, points[i].lonlat.lat), {"marker": "normal", "label": ""}
      );
      allFeatures[i].data = {
        topicName: points[i].topicName, topicId: points[i].topicId, defaultIcon: points[i].defaultIcon, 
        lon:points[i].lonlat.lon, lat:points[i].lonlat.lat, originId: points[i].originId 
      };
      allFeatures[i].cluster = null;
      allFeatures[i].attributes.iconUrl = ""; // not to show feature after initializing
      // allFeatures[i].attributes.renderer = "circle"; // = "blackdot.gif"; // not to show feature after initializing
      allFeatures[i].attributes.size = "15";
      allFeatures[i].attributes.label = points[i].topicName;
      allFeatures[i].renderIntent = "default"; // not to show feature after initializing
      // add new feature
      myNewLayer.addFeatures(allFeatures[i]);
    }
    if (debug) log('newLayer with overall: ' + myNewLayer.features.length + ' features');
    map.addLayer(myNewLayer);
  }

  /**
   * note: we may remove dependency to OpenLayers.Marker and switch to OpenLayers.Features only (solely SVG-rendering)
   * ### FIXME: REFACTOR gMarkers + .long
   **/
  function setupOpenMarkers() {
    // var bounds = new GLatLngBounds();
    gMarkers = new Array();
    var size = new OpenLayers.Size(10,10);
    var offset = new OpenLayers.Pixel(-(size.w/2), -size.h);
    var icon = new OpenLayers.Icon('http://www.kiezatlas.de/client/icons/redball-middle.png', size, offset);
    //
    /* if ( mapTopics.result.topics.length == 0) {// display to the user that the import failed and there is currently no data available
      jQuery("#map").html('<div style="position: relative; top: 150px; left 30%; background-color:#999999;"></div>'); 
    };*/
    for (var i = 0; i < mapTopics.result.topics.length; i++) {
      var lng = [mapTopics.result.topics[i].long];
      var lat = [mapTopics.result.topics[i].lat];
      var skip = false;
      if (lat == 0.0) {
        skip = true;
      }
      if (lng == 0.0) {
        skip = true;
      }
      if (!skip) {
        var point = new OpenLayers.LonLat(parseFloat(lng), parseFloat(lat));
        var name = [mapTopics.result.topics[i].name];  
        var id = [mapTopics.result.topics[i].id];
        var originId = [mapTopics.result.topics[i].originId];
        //var iconFile = [mapTopics.result.topics[i].icon]
        var marker = createOpenMarker(point, name, id, icon, originId); // , categoryId
        gMarkers.push(marker);
      }
    }
    // log('setupMarkers ' + gMarkers.length);
    return gMarkers;
  }

  /** there is a default marker in stylmap */
  function createOpenMarker(point, topicName, id, icon, originId) {
    var marker = new OpenLayers.Marker(point.transform(map.displayProjection, map.projection), icon.clone());
    marker.defaultIcon = icon.clone();
    marker.topicId = id;
    marker.originId = originId;
    marker.topicName = topicName;
    marker.events.register('click', marker, function(event) {
      showInfoWindowForMarker(marker);
      // log("createdPopUpWindow for Marker " + id);
      showTopicInSideBar(id);
    });
    // log("createdOpenMarker for " + marker.topicId + "olId"+marker.id+" which isDrawn" + marker.isDrawn()+ " or isDisplayed: " + marker.display());
    return marker;
  }

  function makeHotspot(poi, allMarkers){
    var cluster = new Array();
    for(i=0; i<allMarkers.length; i++) {
      if (parseFloat(poi.lonlat.lon) == parseFloat(allMarkers[i].lonlat.lon) 
          && parseFloat(poi.lonlat.lat) == parseFloat(allMarkers[i].lonlat.lat)) {
        cluster.push(allMarkers[i]);
      } else {
        if (debug) log("*** mischeck: "+ poi.lonlat + "!=" + allMarkers[i].lonlat);
      }
    }
    if (cluster.length >= 1) {cluster.push(poi);} else {log("nothing found equal: " + poi.lonlat.lon +"=="+ poi.lonlat.lat);}
    // after one match we`re a cluster
    return cluster;
  }
  
  /** yet unused method */
  function checkLayerForFeatureOnPosition(lonlat) {
    for(i=0; i<myNewLayer.features.length; i++) {
      if (parseFloat(myNewLayer.features[i].data.lon) == parseFloat(lonlat.lon) 
          && parseFloat(myNewLayer.features[i].data.lat) == parseFloat(lonlat.lat)) {
          // log("> clusterCheck found a featured position.. ");
        return myNewLayer.features[i];
      }
    }
    return null;
  }
  
  /** used for creating cluster*/
  function checkLayerForVisibleFeatureOnPosition(lonlat, topicId) {
    for ( i = 0; i < myNewLayer.features.length; i++) {
      if (myNewLayer.features[i].data.topicId == topicId) {
        // alert("skipped" + topicId + " caused by multiple categories ")
        // we don't want to cluster an item with itself cause it may be assigned to multiple categories'
        return null;
      }
      if (myNewLayer.features[i].renderIntent != "delete") {
        if (parseFloat(myNewLayer.features[i].data.lon) == parseFloat(lonlat.lon) 
            && parseFloat(myNewLayer.features[i].data.lat) == parseFloat(lonlat.lat)) {
          return myNewLayer.features[i];
        }
      }
    }
    return null;
  }
  
  /** checks all features on a layer, if they are a cluster and if they contain already a specific topic */
  function checkIfAlreadyInCluster(topicId) {
    for(i=0; i<myNewLayer.features.length; i++) {
      // look in every feature on the layer which is a cluster ?? and 
      // which is currently visible (there`ll never be an invisible cluster)?? 
      if(myNewLayer.features[i].cluster != null) { 
        // parseFloat(lonlat.lon) && parseFloat(myNewLayer.features[i].data.lat) == parseFloat(lonlat.lat)) {
        for(k=0; k<myNewLayer.features[i].cluster.length; k++) {
          if(topicId == myNewLayer.features[i].cluster[k].topicId) return true;
        }
        // return myNewLayer.features[i];
        // log("clusterCheck found a featured position.. ");
      }
    }
    if (debug) log("> topicId is not in any cluster yet");
    return false;
  }
  
  /** getFeature from Layer by topicId */
  function checkFeatureById(topicId) {
    if ( myNewLayer != null ) {
      for( i = 0; i < myNewLayer.features.length; i++ ) {
        if( myNewLayer.features[i].data.topicId == topicId ) {
          // log("found a feature "+ myNewLayer.features[i].id + " on myNewLayer.. displayStyle is:"
          //  + myNewLayer.features[i].attributes);
          return myNewLayer.features[i];
        }
      }
    }
    return null;
  }
  
  /** getFeature from Layer by topicId */
  function checkFeatureByOriginId(givenId) {
    for ( i=0; i < myNewLayer.features.length; i++ ) {
      if ( myNewLayer.features[i].data.originId == givenId ) {
       return myNewLayer.features[i];
      }
    }
    if (debug) log("[ERROR] found no feature on myNewLayer for " + givenId);
    return null;
  }

  /** getFeature from Layer by topicId */
  function checkFeatureByTopicId(givenId) {
    for ( i=0; i<myNewLayer.features.length; i++ ) {
      if ( myNewLayer.features[i].data.topicId == givenId ) {
       return myNewLayer.features[i];
      }
    }
    if (debug) log("[ERROR] found no feature on myNewLayer for " + givenId);
    return null;
  }

  /** check all Drawn Features if they are a topicId or contain a topicId in their cluster*/
  function checkDrawnFeaturesForTopicId(topicId) {
    if ( myNewLayer != null ) {
      for( i=0; i < myNewLayer.features.length; i++ ) {
        if ( myNewLayer.features[i].renderIntent != "delete" ) {
          // just go on with the check if feature is currently visible
          if ( myNewLayer.features[i].data.topicId == topicId ) {
            // check for direct topicId match
            // log("found a feature "+ myNewLayer.features[i].id + " on myNewLayer.. displayStyle is:"
            //   + myNewLayer.features[i].attributes);
            return myNewLayer.features[i];
          } else if ( myNewLayer.features[i].data.cluster != null ) {
            // or if this is a cluster then check if topic is indirect visible in here
            for( j=0; j < myNewLayer.features[i].data.cluster.length; j++ ) {
              if ( myNewLayer.features[i].data.cluster[j].topicId == topicId ) {
                log("[INFO] " + myNewLayer.features[i].data.cluster[j].topicName + " is <i>visibleInCluster</i>");
                return myNewLayer.features[i];
              };
            }
          }
        }
      }
    }
    return null;
  }
  
  /** check all Drawn Features if they are a topicId or contain a topicId in their cluster*/
  function checkDrawnFeaturesForOriginId(originId) {
    //
    for(i=0; i<myNewLayer.features.length; i++) {
      if (myNewLayer.features[i].renderIntent != "delete") {
        // just go on with the check if feature is currently visible
        if (myNewLayer.features[i].data.originId == originId) {
          // check for direct topicId match
          // log("found a feature "+ myNewLayer.features[i].id +" on myNewLayer.. displayStyle is:" 
          //   + myNewLayer.features[i].attributes);
          return myNewLayer.features[i];
        } else if (myNewLayer.features[i].data.cluster != null) {
          // or if this is a cluster then check if topic is indirect visible in here
          for(j=0; j<myNewLayer.features[i].data.cluster.length; j++) {
            if (myNewLayer.features[i].data.cluster[j].originId == originId) { 
              log("[INFO] " + myNewLayer.features[i].data.cluster[j].topicName + " is <i>visibleInCluster</i>");
              return myNewLayer.features[i]; 
            };
          }
      }
      }
    }
    return null;
  }

  /** create and show popuop and render marker as selected */
  function showInfoWindowForMarker(featureData, clusteredId) {
    var idString = ""+ featureData.topicId + "";
    var htmlString = '<b>' + featureData.topicName + '</b><br/><a href=javascript:showTopicInSideBar("'
      + idString+'")>weitere Details</a>';
    // if ( clusteredId != null ) {
    if ( featureData.cluster != null ) {
      htmlString = "<b class=\"redTitle\">Es gibt hier mehrere M&ouml;glichkeiten an einem Ort:</b><p/>";
      for (i=0; i < featureData.cluster.length; i++) {
        var clusterTopicId = featureData.cluster[i].topicId;
        var clusterTopic = getTopicById(clusterTopicId);
        htmlString += '<b>' + clusterTopic.name + '</b>&nbsp; - <a href=javascript:showTopicInSideBar("'
          + clusterTopicId + '")>weitere Details</a><br/>';
      }
      // htmlString += "</li>";
    }
    // just make sure that there is not more than 1active PopUpWindow
    hideAllInfoWindows();
    // var htmlString = '<b>' + featureData.topicName 
    // + '</b><br/><a href=javascript:showTopicInSideBar("'+idString+'")>weitere Details</a>';
    var lonlat = new OpenLayers.LonLat(featureData.lon, featureData.lat);
    var popup = new OpenLayers.Popup.FramedCloud(
      "infoPoop-"+featureData.topicId,
      lonlat, new OpenLayers.Size(250, 200),
      htmlString, null,
      false);
    // popup.keepInMap = true;
    popup.autoSize = true;
    popup.panMapIfOutOfView = false;
    /*popup.addCloseBox(function(){
      var feat = checkFeatureById(idString);
      feat.renderIntent = "default";
      myNewLayer.redraw();
    }); **/
    map.addPopup(popup);
  }

  /** create and show popuop and render marker as selected */
  function showInfoWindowForTopicId(topicId) {
    // just make sure that there is not more than 1active PopUpWindow
    hideAllInfoWindows();
    //var topicInfo = getTopicById(data.topicId);
    var idString = ""+ topicId + "";
    var featureData = checkFeatureById(topicId).data;
    var htmlString = '<b>' + featureData.topicName + '</b><br/><a href=javascript:showTopicInSideBar("'
      + idString+'")>weitere Details</a>';
    var lonlat = new OpenLayers.LonLat(featureData.lon, featureData.lat);
    // 
    var popup = new OpenLayers.Popup.FramedCloud(
      "infoPoop-"+featureData.topicId, 
      lonlat, new OpenLayers.Size(250, 100), 
      htmlString, null, false);
    popup.keepInMap = true;
    // popup.panMapIfOutOfView = false;
    popup.autoSize = true;
    map.addPopup(popup);
  }
  
  /** function deselectAllFeatures(callback) {
    for (i=0; i<myNewLayer.selectedFeatures.length; i++) { 
      selectFeatureHandler.unselect(myNewLayer.selectedFeatures[i]);
    }
  } */

  /** search _all_ clientside known markers for a double by coordinates 
    * useful to identify hotspots
    * TODO: find out if a markerLayer can check if a position is already used by marker
    */
  function getMarkerByLatLng(latLng) {
    //
    for (var i = 0; i < gMarkers.length; i++) {
      // map.addOverlay(gMarkers[i]);
      var marker = gMarkers[i];
      if (marker.lonlat == latLng) {return marker;}
    }
    return null;
  }
  
  /** it may be the case that a given topicId is part of a category*/
  function getTopicById(topicId) {
    // printOut("searching through " + mapTopics.result.topics.length);
    for (var m = 0; m < mapTopics.result.topics.length; m++){
      //printOut(' searching through ' + mapTopics.result.topics.length + ' mapTopics');
      // printOut("|..checking " + mapTopics.result.topics[m].id + '==' + topicId + ' |');
      if (topicId == mapTopics.result.topics[m].id) {
        // printOut(' foundTopicById: ' + topicId);
        return mapTopics.result.topics[m];
      }
    }
    return null;
  }
  
  /** it may be the case that a given topicId is part of a category*/
  function getTopicByOriginId(topicId) {
    // printOut("searching through " + mapTopics.result.topics.length);
    for (var m = 0; m < mapTopics.result.topics.length; m++){
      //printOut(' searching through ' + mapTopics.result.topics.length + ' mapTopics');
      // printOut("|..checking " + mapTopics.result.topics[m].id + '==' + topicId + ' |');
      if (topicId == mapTopics.result.topics[m].originId) {
        // printOut(' foundTopicById: ' + topicId);
        return mapTopics.result.topics[m];
      }
    }
    return null;
  }
  
  //
  // --- Utility Methods for Style and Layout
  //

  function getCatIconURL(categoryId) {
    for (var j = 0; j < workspaceCriterias.result[crtCritIndex].categories.length; j++) {
      if(workspaceCriterias.result[crtCritIndex].categories[j].catId == categoryId) {
        log("<b>catIcon is: </b>" + workspaceCriterias.result[crtCritIndex].categories[j].catIcon);
        return workspaceCriterias.result[crtCritIndex].categories[j].catIcon;
      }
      // var catToShow = mapTopics.result.topics[i].criterias[j].categories
    }
    return null;
  }

  /** TODO: fix **/
  function inputFieldBehaviour() {
    // focusInputFinputield
    // jQuery("#focusInput").addClass("idle");
    jQuery("#focusInput").focus(function() {
      // jQuery(this).addClass("activeField").removeClass("idle");
      // set activeField
      // jQuery(this).width(150);
      jQuery(this).animate({width: 145}, 500);
    });
    jQuery("#focusInput").blur(function() {
      // jQuery(this).removeClass("activeField").addClass("idle");
      // jQuery(this).width(75);
      // set idleField
      jQuery(this).animate({width: 115}, 500);
    });
  }
   
  function toggleWidth(e) {
      if (slimWidth) {
        slimWidth = false;
      } else {
        slimWidth = true;
      }
      var fHeight = windowHeight();
      var fWidth = windowWidth(); // ### formerly 1339;// 
      setLayout(fHeight, fWidth);
      // map.redraw();
  }
  
  function setSideBarWidth(sideBarWidth) {
    // jQuery("#sideBarControl").attr('onclick','javascript:handleSideBar();');
    // jQuery("#sideBarControl").css("cursor", "e-resize");
    jQuery("#sideBar").show("fast");
    jQuery("#sideBar").css("width", sideBarWidth);
  }

  function windowHeight() {
    if (self.innerHeight)
    // log('innerHeight is' + self.innerHeight);
    return self.innerHeight;
    if (document.documentElement && document.documentElement.clientHeight)
    //log('clientHeight is' + $.clientHeight);
    return jQuery.clientHeight;
    if (document.body)
    //log('bodyHeight is' + document.body.clientHeight);
    return document.body.clientHeight;
    return 0;
  }
  
  function windowWidth() {
    if (self.innerWidth) {
      //log('innerWidth is' + self.innerWidth);
      return self.innerWidth;
    }
    if (document.documentElement && document.documentElement.clientWidth) {
      //log('clientWidth is' + $.clientWidth);
      return jQuery.clientWidth;
    }
    if (document.body) {
      //log('bodyWidth is' + document.body.clientWidth);
      return document.body.clientWidth;
    }
    return 0;
  }

  function handleResize(width) {
    var fHeight = windowHeight();
    var fWidth = windowWidth();
    var crtHeightFromTop = parseInt(jQuery("#kiezatlas").css("top"));
    if (crtHeightFromTop > 0) { // leaving fullWindow onBerlinDe-Hack
      fHeight = fHeight - crtHeightFromTop;
    }
    if ( width != null) {
      fWidth = width;
    }
    if (debugUI) log('handlingResize browser Window is ' + fHeight +':'+ fWidth );
    setLayout(fHeight, fWidth);
  }
  
  /** TODO: to show progress always in center and with a label .. */
  function showProgressInSideBar(progressVal) {
    // sideBarControl
    /* showSideBar (305); */
    // jQuery("#sideBarCategoriesTable").empty(); // '<a href="javascript:clearCriteriaSelection();">Auswahl aufheben</a>');
    // sideContent
    jQuery("#progContainer").html('<b>' + progressVal + '</b><br/><img src="http://www.kiezatlas.de/maps/embed/img/aLoading.gif" alt="Loading"/>');
    jQuery("#progContainer").show('fast');
  }

  function hideProgressFromSideBar() {
    jQuery("#progContainer").hide("fast");
  }


  
  //
  // --- Topic Data Container Utilities
  //
  
  /** TopicBeanField Util */
  function stripFieldsContaining(topic, fieldName) {
    var newProps = new Array();
    for (var it=0; it < topic.properties.length; it++) {
      // resultHandler.append('<tr><td>'+topic.properties[i].label+'</td><td>'+topic.properties[i].value+'</td></tr>');
      if (topic.properties[it].name.indexOf(fieldName) == -1) {
        // log('fieldStrippin: ' + it);
        newProps.push(topic.properties[it]);
      } else if (topic.properties[it].name.indexOf("Email") != -1) {
        // save Email Address Property being stripped by a command called "Address""
        newProps.push(topic.properties[it]);
      } else {
        // flog('stripping Field ' + topic.properties[it].name);
      }
    }
    topic.properties = newProps;
    return topic;
  }
  
  function getTopicAddress(topic) {
    for (var i=0; i < topic.properties.length; i++) {
      if (topic.properties[i].name == "Address / Street" && topic.properties[i].value != "") {
      // via related Address Topic
      return topic.properties[i].value;
      } else if (topic.properties[i].name == "Straße" && topic.properties[i].value != "") {
      // via related Street PropertyField
      return topic.properties[i].value;
      }
    }
    return "";
  }
  
  function getImageSource(topic) {
    for (var i=0; i < topic.properties.length; i++) {
      if (topic.properties[i].name == "Image / File" && topic.properties[i].value != "") {
        return topic.properties[i].value;
      }
    }
    return "undefined";
  }

  function getTopicPostalCode(topic) {
    for (var i=0; i < topic.properties.length; i++) {
      if (topic.properties[i].name == "Address / Postal Code") {
        return topic.properties[i].value; // + ' Berlin<br/>';
      }
    }
    return "";
  }

  function getTopicCity(topic) {
    for (var at=0; at < topic.properties.length; at++) {
      // resultHandler.append('<tr><td>'+topic.properties[i].label+'</td><td>'+topic.properties[i].value+'</td></tr>');
      if (topic.properties[at].name == "Address / City") {
        return topic.properties[at].value; // + ' Berlin<br/>';
      } else if (topic.properties[at].name == "Stadt") {
        return topic.properties[at].value;
      }
    }
    return null;
  }



  // 
  // --- High Level CiyMap Functions 
  // 
  
  function calculateInitialBounds() {
    var bounds = new OpenLayers.Bounds();
    for (var i = 0; i < mapTopics.result.topics.length; i++) {
      var lng = [mapTopics.result.topics[i].long];
      var lat = [mapTopics.result.topics[i].lat];
      var skip = false;
      if (lat == 0.0 || lng == 0.0) {
        skip = true;
      } else if (lng < 0.0 || lng > 360.0) {
        skip = true;
      } else if (lat < -90.0 || lat > 90.0) {
        skip = true;
      }
      if (!skip) {
        var point = new OpenLayers.LonLat(parseFloat(lng), parseFloat(lat));
        bounds.extend(point);
      }
    }
    return bounds;
  }

  function getBoundsOfFeatures(pois) {
    var bounds = new OpenLayers.Bounds();
    for (var i = 0; i < pois.length; i++) {
      var lng = pois[i].data.lon;
      var lat = pois[i].data.lat;
      var skip = false;
      if (lat == 0.0 || lng == 0.0) {
        skip = true;
      }
      if (!skip) {
        var point = new OpenLayers.LonLat(parseFloat(lng), parseFloat(lat));
        bounds.extend(point);
      }
    }
    return bounds;
  }
  
  function getBoundsOfCurrentVisibleFeatures() {
    var bounds = new OpenLayers.Bounds();
    var counter = 0;
    for (var i = 0; i < myNewLayer.features.length; i++) {
     if ( myNewLayer.features[i].renderIntent != "delete" ) {
        //var lng = myNewLayer.features[i].data.lon;
        //var lat = myNewLayer.features[i].data.lat;
        var point = myNewLayer.features[i].geometry.getBounds().getCenterLonLat();
        var skip = false;
        if (point.lat == 0.0 || point.lon == 0.0) {
          skip = true;
        }
        if (!skip) {
          counter++;
          // var point = new OpenLayers.LonLat(parseFloat(lng), parseFloat(lat));
          bounds.extend(myNewLayer.features[i].geometry.getBounds().getCenterLonLat());
          // if (debug) log("extending DistricBounds about: " + myNewLayer.features[i].geometry.getBounds().getCenterLonLat());
        }
      }
    }
    if (debug) log("setDistrictBounds: of " + counter + " elements to " + bounds );
    return bounds;
  }
  
  /** enables to set the maps focus */
  function updateVisibleBounds(newBounds, resetMarkers, zoomLevel, resetSearch) {
    if (newBounds == null) {
      map.zoomToExtent(calculateInitialBounds().transform(map.displayProjection, map.projection));
      // map.zoomTo(11);
    } else {
      // map.zoomToExtent(newBounds.transform(map.displayProjection, map.projection));
      map.panTo(newBounds.getCenterLonLat());
      map.zoomTo(zoomLevel);
    }
    // render all markers as "delete" and deselect all categories
    if (resetMarkers == true) {
      reSetMarkers();
    }
    // reset the sidebar to the latest critIndex
    if (resetSearch) updateCategoryList(crtCritIndex);
  }
  
  /** called by the CustomLayerSwitcher.onInputClick */
  function clickInfoForMapControlMenu() {
    updatePermaLink(permaLink);
    outMapControl();
  }
  
  function overMapControl() {
    if (onBerlinDe) {
      jQuery("#mapControl").css("height", 105);
    } else {
      jQuery("#mapControl").css("height", 145);
    }
    toggleMapControl();
  }
  
  function outMapControl() {
    jQuery("#mapControl").css("height", 20);
    toggleMapControl();  
  }
  
  function toggleMapControl() {
    if (jQuery("#mapSwitcher").css("visibility") == "hidden") {
      jQuery("#mapSwitcher").css("visibility", "visible");
    } else {
      jQuery("#mapSwitcher").css("visibility", "hidden");
    }
  }
  
  /** show all topics as Features in myNewLayer and select all Categories in Sidebar */
  function showAllMarker() {
    if (!checkIfAllCategoriesSelected()) {
      var els = new Array();
      for (var i = 0; i < mapTopics.result.topics.length; i++) {
        els.push(mapTopics.result.topics[i].id);
      }
      showTopicFeatures(els, "");
      //
      selectAllCategories();
    }
  }
  
  /** TODO: nearly redundant code to hideAllKiezatlasFeatures  */
  function reSetMarkers() {
    // if (myNewLayer != null) { // check for missing baseLayers
    hideAllInfoWindows();
    hideAllKiezatlasFeatures(); // and popups
    deSelectAllCategories();
    // }
  }

  /** TODO: redundant code to reSetMarkers  
   * should be refactorde to hideAllMarker
   */
  function hideAllKiezatlasFeatures() {
    // showProgressInSideBar("Platzieren der Markierer");
    /* for (var i = 0; i < gMarkers.length; i++) {
      markerLayer.removeMarker(gMarkers[i]);
      gMarkers[i].erase(); // = false;
    }*/
    //
    if (myNewLayer != undefined) {
      for (var i = 0; i < myNewLayer.features.length; i++) {
        var featureToToggle = myNewLayer.features[i];
        // gMarkers[i].erase(); // = false;
        featureToToggle.renderIntent = "delete";
      }
      myNewLayer.redraw();
    }
  }



  //
  // --- Other Little Helpers
  //
  
  /** mapTile Function, not exactly clear what it does */
  function osm_getTileURL(bounds) {
    var res = this.map.getResolution();
    var x = Math.round((bounds.left - this.maxExtent.left) / (res * this.tileSize.w));
    var y = Math.round((this.maxExtent.top - bounds.top) / (res * this.tileSize.h));
    var z = this.map.getZoom();
    var limit = Math.pow(2, z);
    //
    if (y < 0 || y >= limit) {
     return OpenLayers.Util.getImagesLocation() + "404.png";
    } else {
      x = ((x % limit) + limit) % limit;
      return this.url + z + "/" + x + "/" + y + "." + this.type;
    }
  }

  function redrawAfterZoomOperation () {
    // markerLayer.setVisibility(true);
    myNewLayer.setVisibility(true);
  }
  
  function log(text) {
    if (debug) {
      // Note: the debug window might be closed meanwhile
      if (debug_window.document) {
        debug_window.document.writeln(render_text(text) + "<br>")
      }
    }
  }

  function setCityMapName(title) {
    jQuery("#mapName").html('<b>Stadtplan: </b> ' + title);
  }

  /** TODO: check what they actually need todo 
   *  helper for the inputFields to send Requests to our Proxyscript with encoding the blanks
   */
  function urlencode(query) {
    // var street = ""+query+"";
    var result = new String(query).replace(/ /g, "%20");
    return result
  }
  
  /** helper functions to produce html links */
  function htmlReplace(val) {
    if(val.indexOf("http://") != -1) {
      return '<a href="'+val+'">'+val+'</a>';
    } else if (val.indexOf("@") != -1) {
      return '<a href="mailto:'+val+'">'+val+'</a>';
    }
    return val;
  }
  
  function render_text(text) {
    return text.replace(/\n/g, "<br>")
  }
  
  function makeWebpageLink(url, label) {
    if (onBerlinDe) urlMarkup = '<a href="'+url+'" target="_blank">Link zur T&auml;tigkeitsbeschreibung'
      + '<img src="/.img/ml/link_extern.gif" class="c7" alt="(externer Link)" border="0" height="11" width="12"/></a>';
    else urlMarkup = '<a href="'+url+'" target="_blank">'+label+'</a>';
    return urlMarkup
  }

  function makeEmailLink(url, label) {
    urlMarkup = '<a href="mailto:'+url+'" target="_blank">'+label+'</a>';
    return urlMarkup
  }
  
  function hideAllInfoWindows() {
    if (map != undefined && map.popups != undefined) {
      for (var i=0; i < map.popups.length; i++) {
        // map.removePopup(map.popups[i]);
        map.popups[i].destroy();
      }
    }
  }
  
  function help() {
    if(!helpVisible) {
      var helpHtmlOne = '<img src="http://www.kiezatlas.de/maps/embed/img/sideBarHelper.png" id="helpPageOne" width="320" alt="Hilfetext Seite 1" '
        + 'text="Hilfetext Seite 1"/><p/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="cityMapNavigation.html">'
        + 'Hier geht`s zur ausf&uuml;hrlichen Hilfe.</a>';
      jQuery("#sideBar").html(helpHtmlOne);
      helpVisible = true;
    } else {
      jQuery("#helpPageOne").remove();
      // updateCategoryList(crtCritIndex);
      var html = '<div id="sideBarCriterias"></div><div id="sideBarCategories">'
        + '<table width="100%" cellpadding="2" cellspacing="0" id="sideBarCategoriesTable"></table>';
      jQuery("#sideBar").html(html);
      showCritCatList();
      // updateCategoryList(crtCritIndex, false); // notResetAllFeatures and restore Categories
      handleResize();
      // setWorkspaceInfos();
      helpVisible = false;
    }
  }
  
