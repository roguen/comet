//Copyright (c) 2015 Hitachi Data Systems, Inc.
//All Rights Reserved.
//
//   Licensed under the Apache License, Version 2.0 (the "License"); you may
//   not use this file except in compliance with the License. You may obtain
//   a copy of the License at
//
//         http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
//   WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
//   License for the specific language governing permissions and limitations
//   under the License.
//
//Package: COMET Web Application
//Author: Chris Delezenski <chris.delezenski@hdsfed.com>
//Compilation Date: 2015-05-06
//License: Apache License, Version 2.0
//Version: 1.21.0
//(RPM) Release: 1
//SVN: r554


if(typeof google === 'undefined') {
	global_properties.geo_enabled=false;
	geo_available=false;
} else {
	google.load("earth","1");
}	
var flip_back_to=0;
function CometGeo_driver_GE_Enabled() {
	return global_properties.geo_enabled && ge;
}

function CometGeo_driver_GE_Init() {
	if(typeof google === 'undefined') {
		global_properties.geo_enabled=false;
		geo_available=false;
		console.log("Unable to load Google Earth to canvas...");
		return;
	} else {
		console.log("Loading Google Earth to canvas...");
	}

	var request=cTabLoad("index_geoviz.html","geoviz_page");
	request.onreadystatechange = function() {
		if ((request.readyState == 4) && (request.status == 200)) {
			CometControlsLog("loading "+request.html_page+" content to object "+request.tgt);
			document.getElementById(request.tgt).innerHTML=request.responseText;
			if(document.getElementById('map3d')) document.getElementById('map3d').innerHTML = '';
			document.getElementById('installed-plugin-version').innerHTML='';
			//google.load("earth","1");
	
			setTimeout(function() { 
			
				console.log("creating google earth instance...");
				if(!google.earth) {
				
					console.log("ge not available...");
				
				}
				
				flip_back_to=cTabGetCurrentPage("RHS");

				cTabFliptoPage( "RHS", page_number_geoviz );
	

			google.earth.createInstance('map3d', CometGeo_driver_GE_InitCallback, CometGeo_driver_GE_FailureCallback); 
			
			
			}, 5000);
			CometStopAJAXAnimation();
		}
	}
	return true;
}

function CometGeo_driver_GE_InitCallback(instance) {
	ge = instance;
	
	if(!GoogleExistanceCheck(ge)) {
		console.log("Failed to load GE, trying again...");
		delete ge;
		delete google.earth;
		google.load("earth","1");
		console.log("Reinitialize GE in 5 seconds...");
		setTimeout(function() { CometGeo_driver_GE_Init(); }, 5000);
		return;
	
	}
	ge.getWindow().setVisibility(true);
	// add a navigation control
	ge.getNavigationControl().setVisibility(ge.VISIBILITY_AUTO);
	// add some layers
	ge.getLayerRoot().enableLayerById(ge.LAYER_BORDERS, true);
	ge.getLayerRoot().enableLayerById(ge.LAYER_ROADS, true);
	ge.getLayerRoot().enableLayerById(ge.LAYER_TERRAIN, true);
	ge.getLayerRoot().enableLayerById(ge.LAYER_BUILDINGS, true);
	ge.getOptions().setFlyToSpeed(ge.SPEED_TELEPORT); 
	ge.getOptions().setStatusBarVisibility(true);
	ge.getOptions().setOverviewMapVisibility(true);
//        google.earth.addEventListener(ge, "frameend", rotateEarth); 
//	google.earth.addEventListener(ge.getView(), 'viewchange', CometGeo_driver_GE_getCoordinates);
	google.earth.addEventListener(ge.getView(), 'viewchange', CometControlsCoordinatesCB );
	// CometGeo_ViewChange_Callback );
	defaultLookAt =  ge.getView().copyAsLookAt(ge.ALTITUDE_RELATIVE_TO_GROUND)
	defaultCamera = ge.getView().copyAsCamera(ge.ALTITUDE_RELATIVE_TO_GROUND);
	document.getElementById('installed-plugin-version').innerHTML = ge.getPluginVersion().toString();
	
	ge.getOptions().setFlyToSpeed(0.5);
	
	console.log("GE loading complete");
	cTabFliptoPage( "RHS", flip_back_to );
	//finish the load
	CometGeo_LoadComplete();

	
}

// function CometGeo_driver_GE_getCoordinates() {
// 	console.log("updating coordinates -- never see this!");
// 	var lookAt = ge.getView().copyAsLookAt(ge.ALTITUDE_RELATIVE_TO_GROUND);
// 	
// 	coordinates.latitude=lookAt.getLatitude().toFixed(6);
// 	coordinates.longitude=lookAt.getLongitude().toFixed(6);
// 
// 	var hitTestNW = ge.getView().hitTest(0, ge.UNITS_FRACTION, 0, ge.UNITS_FRACTION, ge.HIT_TEST_GLOBE);
// 	var hitTestNE = ge.getView().hitTest(1, ge.UNITS_FRACTION, 0, ge.UNITS_FRACTION, ge.HIT_TEST_GLOBE);
// 	var hitTestSE = ge.getView().hitTest(1, ge.UNITS_FRACTION, 1, ge.UNITS_FRACTION, ge.HIT_TEST_GLOBE);
// 	var hitTestSW = ge.getView().hitTest(0, ge.UNITS_FRACTION, 1, ge.UNITS_FRACTION, ge.HIT_TEST_GLOBE);
//     
//     	if(hitTestNW) {
// 		coordinates.nw_latitude=hitTestNW.getLatitude().toFixed(6);
// 		coordinates.nw_longitude=hitTestNW.getLongitude().toFixed(6);
// 	} else {
// 		coordinates.nw_latitude=0;
// 		coordinates.nw_longitude=0;
// 	}
// 
//     	if(hitTestNE) {
// 		coordinates.ne_latitude=hitTestNE.getLatitude().toFixed(6);
// 		coordinates.ne_longitude=hitTestNE.getLongitude().toFixed(6);
// 	} else {
// 		coordinates.ne_latitude=0;
// 		coordinates.ne_longitude=0;
// 	}
// 
//     	if(hitTestSW) {
// 		coordinates.sw_latitude=hitTestSW.getLatitude().toFixed(6);
// 		coordinates.sw_longitude=hitTestSW.getLongitude().toFixed(6);
// 	} else {
// 		coordinates.sw_latitude=0;
// 		coordinates.sw_longitude=0;
// 	}
// 
//     	if(hitTestSE) {
// 		coordinates.se_latitude=hitTestSE.getLatitude().toFixed(6);
// 		coordinates.se_longitude=hitTestSE.getLongitude().toFixed(6);
// 	} else {
// 		coordinates.se_latitude=0;
// 		coordinates.se_longitude=0;
// 	}
// 	
//  	console.log(coordinates.latitude+", "+coordinates.longitude);
// }

function CometGeo_driver_GE_FailureCallback(errorCode) {
	//finish the load
	CometGeo_LoadComplete();
}


function CometGeo_driver_GE_HideMarker(_tag) {
	if(!GoogleExistanceCheck(ge)) return;

	var style = ge.createStyle('');
	style.getIconStyle().setScale(0.0);
	var styleMap = ge.createStyleMap('');
	styleMap.setNormalStyle(style);
        styleMap.setHighlightStyle(style);

	if(!placemarkList[_tag]) return;
	
	if(!placemarkList[_tag].placemark) return;
	
	if(!placemarkList[_tag].geotagged) return;
	placemarkList[_tag].placemark.setStyleSelector(styleMap);
	
	placemarkList[_tag].placemark.setName('');
}


function CometGeo_driver_GE_HideAllMarkers() {
	for(var i=0; i<placemarkList.length; i++) {
		CometGeo_driver_GE_HideMarker(i);
	}
}

function CometGeo_driver_GE_DestroyPlacemark(tag) {
	var this_pm=ge.getElementById('placemark'+tag);
	ge.getFeatures().removeChild(this_pm);
	this_pm.release();
}


function CometGeo_driver_GE_ExistsPlacemark(tag) {
	if(!placemarkList[tag].geotagged) return false;

	var this_pm=ge.getElementById('placemark'+tag);
	if(this_pm) {
		return true;
	}
	return false;

}
function CometGeo_driver_GE_CreatePlacemark(tag) {
 	if(!global_properties.geo_enabled) return;
 	if(!placemarkList[tag].geotagged) return;

	placemarkList[tag].balloon=ge.createHtmlStringBalloon('');
	
	//if there exists a pm already, release it.
	
	
	placemarkList[tag].placemark = ge.createPlacemark('placemark'+tag);

	ge.getFeatures().appendChild(placemarkList[tag].placemark);
	

	placemarkList[tag].placemark.setName(placemarkList[tag].desc);

 	var styleMap = ge.createStyleMap('');
// 	// Create style map for placemark
 	var icon = ge.createIcon('');
 	icon.setHref(paddle_icon);
 	var style = ge.createStyle('');
 	style.getIconStyle().setIcon(icon);
 	placemarkList[tag].placemark.setStyleSelector(style);
 
 	var highlightStyle = ge.createStyle('');
        var highlightIcon = ge.createIcon('');
 	
        highlightIcon.setHref(paddle_icon);
        highlightStyle.getIconStyle().setIcon(highlightIcon);
        highlightStyle.getIconStyle().setScale(2.0);
 
 	styleMap.setNormalStyle(style);
        styleMap.setHighlightStyle(highlightStyle);
// 
// 	// Apply stylemap to a placemark.
        placemarkList[tag].placemark.setStyleSelector(styleMap);
 	var point = ge.createPoint('');
 	//CometControlsLog("center = "+placemarkList[_tag].mdm.longitude+" ,"+ placemarkList[_tag].mdm.latitude);
 	point.setLatitude(parseFloat(placemarkList[tag].latitude));
 	point.setLongitude(parseFloat(placemarkList[tag].longitude));
 	placemarkList[tag].placemark.setGeometry(point);


	if(global_properties.balloon_click) {
		google.earth.addEventListener(placemarkList[tag].placemark, 'click', function(event) {
			// prevent the default balloon from popping up
			var thistag=tag;
			event.preventDefault();
			var placemark = event.getTarget();
			CometControlsLog("id is ="+placemark.getId());
			var tagList=placemark.getId().split('k');
			var tag=tagList[1];
			CometGeo_GE_ToggleBalloon(tag);
		});
	}
}

function CometGeo_driver_GE_ToggleBalloon_Launch(tag) {
	if(!global_properties.geo_enabled) return;
	CometControlsLog("launch balloon "+tag);
	var request=getRequestObject();	
	global_tag=tag;
	request.onreadystatechange = function() {
		if(request.responseText!="" && request.readyState == 4 && request.status == 200) {
			tag=global_tag;
			
			if(!placemarkList[tag]) return;
			if(!placemarkList[tag].geotagged) return;
			if(!placemarkList[tag].placemark) return;
			if(!placemarkList[tag].balloon) return;
		
			placemarkList[tag].balloon.setFeature(placemarkList[tag].placemark); // optional
			placemarkList[tag].balloon.setMinWidth(510);
			placemarkList[tag].balloon.setMaxWidth(510);
			placemarkList[tag].balloon.setMinHeight(300);
			placemarkList[tag].balloon.setMaxHeight(410);
			//CometControlsLog(request.responsetext);
			placemarkList[tag].balloon.setContentString(request.responseText);
			placemarkList[tag].balloon.setMaxWidth(300);
			placemarkList[tag].balloon.setMaxHeight(500);
			ge.setBalloon(placemarkList[tag].balloon);
			placemarkList[tag].showBalloon=true;
			//CometControlsLog("wrote balloon for "+tag);
		}
	}	
	
	var entire_uri=global_properties.imageprefix+'Relay?path='+placemarkList[tag].mdm.urlName+'&type=generated&annotation=description&stream&size=180x400&tag='+(Number(tag)+1);
//	CometControlsLog("entire uri=\""+entire_uri+"\"");
	request.open("GET", entire_uri, true);
	request.send(null);
	CometControlsLog("****GoogleControlsToggleBalloon_Launch::: end processing******");
}

function CometGeo_driver_GE_ToggleBalloon(tag) {
	if(!global_properties.geo_enabled || !placemarkList[tag].geotagged) return;
	CometControlsLog('toggleBalloon('+tag+')');
	var content="";
	if(!placemarkList[tag].showBalloon || ge.getBalloon()==null) {
		CometGeo_driver_GE_ToggleBalloon_Launch(tag);
	} else {
		placemarkList[tag].showBalloon=false;
		placemarkList[tag].balloon.setContentString('');
		ge.setBalloon(null);
	}
}

function CometGeo_driver_GE_HideBalloon(tag) {
	placemarkList[tag].showBalloon=false;
	if(placemarkList[tag].balloon && placemarkList[tag].balloon!="nill") {
		placemarkList[tag].balloon.setContentString('');
	}

}

function CometGeo_driver_GE_HideAllBalloons() {
	for(var i=0; i<placemarkList.length; i++) {
		CometGeo_driver_GE_HideBalloon(i);
	}
	ge.setBalloon(null);
}


function CometGeo_driver_GE_Fly(tag, hide_them) {
	if(hide_them) CometGeo_driver_GE_HideAllBalloons();
	if(!global_properties.geo_enabled || !placemarkList[tag] || !placemarkList[tag].geotagged) return;

	CometControlsLog("BEGIN GoogleControlsFly("+tag+") with flownin="+placemarkList[tag].flownin);
	if(placemarkList[tag].flownin) {
		GoogleControlsFlyOverLatLong_INTERNAL(placemarkList[tag].mdm.latitude,placemarkList[tag].mdm.longitude);
	} else {
		GoogleControlsFlyThereLatLong_INTERNAL(placemarkList[tag].mdm.latitude,placemarkList[tag].mdm.longitude);
	}
	placemarkList[tag].flownin=!placemarkList[tag].flownin;
	CometControlsLog("END GoogleControlsFly("+tag+") with flownin="+placemarkList[tag].flownin);
}

function CometGeo_driver_GE_FlyThereLatLong_INTERNAL(lat,long) {
	var la = ge.createLookAt('');
	CometControlsLog("BEGIN GoogleControlsFlyThereLatLong_INTERNAL("+lat+","+long+")");
	//cTabFliptoPage("RHS",page_number_geoviz);
	
	la.set(Number(lat), Number(long), 0, ge.ALTITUDE_RELATIVE_TO_GROUND, 0, 0, 5000 );
	ge.getView().setAbstractView(la);
	var la = ge.createLookAt('');
	la.set(Number(lat), Number(long), 0, ge.ALTITUDE_RELATIVE_TO_GROUND, 0, 80, 100 );
	ge.getView().setAbstractView(la);

//	GoogleControlsHideAllBalloons();
	CometControlsLog("END GoogleControlsFlyThereLatLong_INTERNAL("+lat+","+long+")");
}

//Internal
function CometGeo_driver_GE_FlyOverLatLong_INTERNAL(lat,long) {
	CometControlsLog("BEGIN GoogleControlsFlyOverLatLong_INTERNAL("+lat+","+long+")");
	//cTabFliptoPage("RHS",page_number_geoviz);
	var la = ge.createLookAt('');
	CometControlsLog("lat="+lat+", long="+long);
	
	la.set( Number(lat), Number(long), 0, ge.ALTITUDE_RELATIVE_TO_GROUND, 0, 0, 1000000 );
	ge.getView().setAbstractView(la);
//	GoogleControlsHideAllBalloons();
	CometControlsLog("END GoogleControlsFlyOverLatLong_INTERNAL("+lat+","+long+")");
}

function CometGeo_driver_GE_FlytoGeoLocation(geocodeLocation) {
  var geocoder = new google.maps.ClientGeocoder();
  geocoder.getLatLng(geocodeLocation, function(point) {
    if (point) {
      var lookAt = ge.createLookAt('');
      lookAt.set(point.y, point.x, 10, ge.ALTITUDE_RELATIVE_TO_GROUND,
                 0, 60, 20000);
      ge.getView().setAbstractView(lookAt);
    }
  });
}

// function CometControlsLoadKMLPath(tag) {
// 	CometControlsLog("****BEGIN CometControlsKMLPath("+tag+")*****");
// 	cTabFliptoPage("RHS",page_number_geoviz);
// 	if(placemarkList[tag].playing) {
// //		placemarkList[tag].playing=false;
// 		GoogleControlsUnLoadKMLviaNetworkLink(tag);
// 		return;
// 	}
// 	var link = ge.createLink('');
// 	var url=global_properties.imageprefix+"Relay?path="+placemarkList[tag].mdm.urlName+
// 			"&type=generate&annotation=kml_path&stream&astext";
// 	link.setHref(url);
// 	placemarkList[tag].networkLink = ge.createNetworkLink('');
// 	placemarkList[tag].networkLink.set(link, true, true); // Sets the link, refreshVisibility, and flyToView
// 	ge.getFeatures().appendChild(placemarkList[tag].networkLink);
// 	placemarkList[tag].playing=true;
// 	if(!ge.getTime().getControl().getVisibility()) {
// 		ge.getTime().getControl().setVisibility(true);
// 	}
// 	CometControlsLog("****END CometControlsKMLPath("+tag+")****");
// }


function CometGeo_driver_GE_LoadKML(tag) {
	CometControlsLog("****BEGIN CometControlsKMLPath("+tag+")*****");
	var link = ge.createLink('');
	var url=global_properties.imageprefix+"Relay?path="+placemarkList[tag].mdm.urlName+
			"&type=generate&annotation=kml_path&stream&astext";
			
	CometControlsLog("trying url="+url);		
	link.setHref(url);
	placemarkList[tag].networkLink = ge.createNetworkLink('');
	placemarkList[tag].networkLink.set(link, true, true); // Sets the link, refreshVisibility, and flyToView
	ge.getFeatures().appendChild(placemarkList[tag].networkLink);
	placemarkList[tag].playing=true;
	if(!ge.getTime().getControl().getVisibility()) {
		ge.getTime().getControl().setVisibility(true);
	}
	CometControlsLog("****END CometControlsKMLPath("+tag+")****");
}

function CometGeo_driver_GE_UnLoadKML(tag) {
	//CometControlsLog("detach KML (network link) from google earth for tag "+tag);
	//if(placemarkList[tag] && placemarkList[tag].networkLink && placemarkList[tag].geotagged && placemarkList[tag].playing) {
	ge.getFeatures().removeChild(placemarkList[tag].networkLink);
	//}
	placemarkList[tag].playing=false;

}

function CometGeo_driver_GE_ClearAll() {
	var features = ge.getFeatures(); 
	while (features.getFirstChild()) { 
		var this_feature=features.getFirstChild();
		features.removeChild(this_feature); 
		this_feature.release();
	} 
	
	if(ge.getTime().getControl().getVisibility()) {
		ge.getTime().getControl().setVisibility(false);
	}
}

function CometGeo_driver_GE_CreateAllPlacemarks() {
	for(var i=0; i<placemarkList.length; i++) {
		CometGeo_driver_GE_CreatePlacemark(i);
	}
}


function CometGeo_driver_GE_OverheadZoom(latitude, longitude) {
	var la = ge.createLookAt('');
	la.set(Number(latitude), Number(longitude), 0, ge.ALTITUDE_RELATIVE_TO_GROUND, 0, 0, 5000 );
	ge.getView().setAbstractView(la);
}

function CometGeo_driver_GE_CreateScreenOverlay(canvas, label, image_url, image_width, image_height) {
	if(!ge) return;
	if(ge.getElementById(label)!=null) {
		ge.getElementById(label).setVisibility(true);
		return;
	}
	
	// Create the ScreenOverlay.
        var screenOverlay = ge.createScreenOverlay(label);

        // Specify a path to the image and set as the icon.
        var icon = ge.createIcon('');
        icon.setHref(image_url);
	screenOverlay.setIcon(icon);

        // Important note: due to a bug in the API, screenXY and overlayXY
        // have opposite meanings than their KML counterparts. This means
        // that in the API, screenXY defines the point on the overlay image
        // that is mapped to a point on the screen, defined by overlayXY.

        // This bug will not be fixed until the next major revision of the
        // Earth API, so that applications built upon version 1.x will
        // not break.

	// Set the ScreenOverlay's position in the window.
        screenOverlay.getOverlayXY().setXUnits(ge.UNITS_PIXELS);
	screenOverlay.getOverlayXY().setYUnits(ge.UNITS_PIXELS);
	screenOverlay.getOverlayXY().setX( $(canvas).width()/2-Number(image_width)/2);
	screenOverlay.getOverlayXY().setY( $(canvas).height()/2-Number(image_height)/2);

	//doesn't work
	screenOverlay.getOverlayXY().setXUnits(ge.UNITS_FRACTION);
        screenOverlay.getOverlayXY().setYUnits(ge.UNITS_FRACTION);
        screenOverlay.getOverlayXY().setX(0.5);
        screenOverlay.getOverlayXY().setY(0.5);
	
	// Set the overlay's size in pixels.
        screenOverlay.getSize().setXUnits(ge.UNITS_PIXELS);
        screenOverlay.getSize().setYUnits(ge.UNITS_PIXELS);
        screenOverlay.getSize().setX(image_width);
	screenOverlay.getSize().setY(image_height);

	// Rotate the overlay.
	//  screenOverlay.setRotation(25);

	// Add the ScreenOverlay to Earth.
	ge.getFeatures().appendChild(screenOverlay);
}

function CometGeo_driver_GE_HideScreenOverlay(label) {
	if(!ge) return;
	
	if(ge.getElementById(label)!=null) {
		ge.getElementById(label).setVisibility(false);
	}
}

function CometGeo_driver_GE_movePlacemark(tag, latitude, longitude) {
	var this_pm=ge.getElementById(tag);
	var point = ge.createPoint('');
	point.setLatitude(latitude);
  	point.setLongitude(longitude);
  	this_pm.setGeometry(point);
}

//create a simple (and boring) placemark with a single icon at 1.0 scale
function CometGeo_driver_GE_createSimplePlacemark(tag, title, iconurl, latitude, longitude) { //duplicate of createplacemark or very similar
	var placemark = ge.createPlacemark(tag);
	ge.getFeatures().appendChild(placemark);

	if(title!="" && title!=undefined) placemark.setName(title);
	CometGeo_driver_GE_changeIconTo(tag, iconurl, 1.0);
	CometGeo_driver_GE_movePlacemark(tag, latitude, longitude);
}

//create a simple placemark using the current view
function CometGeo_driver_GE_createPlacemarkHere(tag, title, iconurl) { //duplicate of createplacemark or very similar
	var la = ge.getView().copyAsLookAt(ge.ALTITUDE_RELATIVE_TO_GROUND);
	CometGeo_driver_GE_createSimplePlacemark(tag, title, iconurl, la.getLatitude(), la.getLongitude());
}

//permanently, remove the placemark from the DOM
function CometGeo_driver_GE_removePlacemark(tag) { //duplicate of destroyplacemark, or very similar
	var this_pm=ge.getElementById(tag);
	ge.getFeatures().removeChild(this_pm);
	this_pm.release();
}

//change the icon
function CometGeo_driver_GE_changeIconTo(tag, new_icon_url, scale) {
	var placemark = ge.getElementById(tag);
	var icon = ge.createIcon('');
	icon.setHref(new_icon_url);
	var style = ge.createStyle('');
	style.getIconStyle().setIcon(icon);

	if(!scale) scale=1.0;
	style.getIconStyle().setScale(scale);

	placemark.setStyleSelector(style);
}

//Determine if two points are within "spitting distance" of each other
function CometGeo_driver_GE_arePointsClose(point1, point2, prec) {
	if(!prec) prec=4;

	//precision, higher is better
	return (Number(point1.getLatitude().toFixed(prec))==Number(point2.getLatitude().toFixed(prec)) && Number(point1.getLongitude().toFixed(prec))==Number(point2.getLongitude().toFixed(prec)));
}

//fly over and then "in" to a point represented by la (look at)
function CometGeo_driver_GE_flyToPM_in(pm,la) {
	la.set( pm.getGeometry().getLatitude(), pm.getGeometry().getLongitude(), 0, ge.ALTITUDE_RELATIVE_TO_GROUND, 0, 0, 5000 );
	ge.getView().setAbstractView(la);
	la.set( pm.getGeometry().getLatitude(), pm.getGeometry().getLongitude(), 0, ge.ALTITUDE_RELATIVE_TO_GROUND, 0, 80, 100 );
	ge.getView().setAbstractView(la);
}

//return true if the tilt is very close to "flown in"
function CometGeo_driver_GE_isTiltIn(la) {
	return (Number(la.getTilt())>78 && Number(la.getTilt())<81);
}

//fly over
function CometGeo_driver_GE_flyToPM_over(pm,la) {
	la.set( pm.getGeometry().getLatitude(), pm.getGeometry().getLongitude(), 0, ge.ALTITUDE_RELATIVE_TO_GROUND, 0, 0, 1000000 );
	ge.getView().setAbstractView(la);
}

//toggle between fly-in/fly-over views
function CometGeo_driver_GE_flyToPMToggle(tag) {
	var placemark = ge.getElementById(tag);
	if(!placemark) {
		return;
	}
	var lookAt = ge.getView().copyAsLookAt(ge.ALTITUDE_RELATIVE_TO_GROUND);
	//if we're way off, or we're already flown in, do a fly-over
	if(GE_arePointsClose(placemark.getGeometry(), lookAt,4) && !GE_isTiltIn(lookAt)) {
		GE_flyToPM_in(placemark,lookAt);
	} else {
		GE_flyToPM_over(placemark,lookAt);
	}
}

//Debugging functions
function CometGeo_driver_GE_inspectPlacemark(placemark) {
	console.log("lat="+placemark.getGeometry().getLatitude());
	console.log("long="+ placemark.getGeometry().getLongitude());
//	console.log("range="+ placemark.getGeometry().getRange());
}
function CometGeo_driver_GE_inspectLookAt(lookAt) {
	console.log("(lookAt) lat="+lookAt.getLatitude());
	console.log("(lookAt) long="+lookAt.getLongitude());
	console.log("(lookAt) range="+lookAt.getRange());
	console.log("(lookAt) tilt="+lookAt.getTilt());
	console.log("(lookAt) alt="+lookAt.getAltitude());
	//console.log("(lookAt) roll="+lookAt.getRoll()); //roll not part of 'lookAt'
}
function CometGeo_driver_GE_inspectCamera(camera) {
	console.log("(camera) lat="+camera.getLatitude());
	console.log("(camera) long="+camera.getLongitude());
	//console.log("(camera) range="+camera.getRange()); // range is not part of camera
	console.log("(camera) tilt="+camera.getTilt());
	console.log("(camera) alt="+camera.getAltitude());
	console.log("(camera) roll="+camera.getRoll());
}

//toggle visibility for a placemark
function CometGeo_driver_GE_hideShowPlacemarkID(tag,visible) {
	var placemark = ge.getElementByID(tag);
	if (placemark) {
		placemark.setVisibility(visible);                
	}                               
}

//optional call back for draggable placemarks on mouse move
function CometGeo_driver_GE_onMouseMoveDraggable(event) {
	if (dragInfo) {
    	event.preventDefault();
		var point = dragInfo.placemark.getGeometry();
		point.setLatitude(event.getLatitude());
		point.setLongitude(event.getLongitude());
		
		if(cur_latitude_id && document.getElementById(cur_latitude_id)) document.getElementById(cur_latitude_id).value=event.getLatitude().toFixed(6);
		if(cur_longitude_id && document.getElementById(cur_longitude_id)) document.getElementById(cur_longitude_id).value=event.getLongitude().toFixed(6);

		
		//console.log("(mouse move) placemark id="+event.getTarget().getId());
		dragInfo.dragged = true;
	}
}

//optional call back for draggable placemarks on mouse up
function CometGeo_driver_GE_onMouseUpDraggable(event) {
	if (dragInfo) {
		if (dragInfo.dragged) {
			// if the placemark was dragged, prevent balloons from popping up
			event.preventDefault();
			//console.log("(mouse up) placemark id="+event.getTarget().getId());
			
			if(cur_latitude_id && document.getElementById(cur_latitude_id)) document.getElementById(cur_latitude_id).value=event.getLatitude().toFixed(6);
			if(cur_longitude_id && document.getElementById(cur_longitude_id)) document.getElementById(cur_longitude_id).value=event.getLongitude().toFixed(6);
			
			
		}
		dragInfo = null;
    }
}

//optional call back for draggable placemarks on mouse down
function CometGeo_driver_GE_onMouseDownDraggable(event) {
	if (event.getTarget().getType() == 'KmlPlacemark' && event.getTarget().getGeometry().getType() == 'KmlPoint') {
		event.preventDefault();
		var placemark = event.getTarget();
		//console.log("(mouse down) placemark id="+event.getTarget().getId());
		dragInfo = {
        	placemark: event.getTarget(),
        	dragged: false
		};
	}
}

function CometGeo_driver_GE_enableDraggableListeners() {
	// listen for mousedown on the window (look specifically for point placemarks)
	google.earth.addEventListener(ge.getWindow(), 'mousedown', CometGeo_driver_GE_onMouseDownDraggable );

	// listen for mousemove on the globe
	google.earth.addEventListener(ge.getGlobe(), 'mousemove', CometGeo_driver_GE_onMouseMoveDraggable );
  
	// listen for mouseup on the window
	google.earth.addEventListener(ge.getWindow(), 'mouseup', CometGeo_driver_GE_onMouseUpDraggable);
}

function CometGeo_driver_GE_disableDraggableListeners() {
	google.earth.removeEventListener(ge.getWindow(), 'mousedown', CometGeo_driver_GE_onMouseDownDraggable);
	google.earth.removeEventListener(ge.getGlobe(), 'mousemove', CometGeo_driver_GE_onMouseMoveDraggable);
 	google.earth.removeEventListener(ge.getWindow(), 'mouseup', CometGeo_driver_GE_onMouseUpDraggable);
}

function CometGeo_driver_GE_clearAll_DUPLICATE() {
	var features = ge.getFeatures(); 
	while (features.getFirstChild()) { 
		var this_feature=features.getFirstChild();
		features.removeChild(this_feature); 
		this_feature.release();
	} 
	
	if(ge.getTime().getControl().getVisibility()) {
		ge.getTime().getControl().setVisibility(false);
	}

}


//using parts of GoogleControlsShowMarker(...) and InitPlacemarks, create a CreatePlacemark function for GE
//********************************** START HERE ***********************************************//

// function GoogleControlsShowMarker_DONOTUSE(_tag) {
// 	CometControlsLog("BEGIN GoogleControlsShowMarker("+_tag+")");
// 	CometControlsLog("geotagged="+placemarkList[_tag].geotagged);
// 	placemarkList[_tag].placemark.setName(placemarkList[_tag].desc);
// 	ge.getFeatures().appendChild(placemarkList[_tag].placemark);
// 	var styleMap = ge.createStyleMap('');
// 	// Create style map for placemark
// 	var icon = ge.createIcon('');
// 	icon.setHref(paddle_icon);
// 	var style = ge.createStyle('');
// 	style.getIconStyle().setIcon(icon);
// 	placemarkList[_tag].placemark.setStyleSelector(style);
// 
// 	var highlightStyle = ge.createStyle('');
//         var highlightIcon = ge.createIcon('');
// 	
//         highlightIcon.setHref(paddle_icon);
//         highlightStyle.getIconStyle().setIcon(highlightIcon);
//         highlightStyle.getIconStyle().setScale(2.0);
// 
// 	styleMap.setNormalStyle(style);
//         styleMap.setHighlightStyle(highlightStyle);
// 
// 	// Apply stylemap to a placemark.
//         placemarkList[_tag].placemark.setStyleSelector(styleMap);
// 	var point = ge.createPoint('');
// 	//CometControlsLog("center = "+placemarkList[_tag].mdm.longitude+" ,"+ placemarkList[_tag].mdm.latitude);
// 	point.setLatitude(parseFloat(placemarkList[_tag].mdm.latitude));
// 	point.setLongitude(parseFloat(placemarkList[_tag].mdm.longitude));
// 	placemarkList[_tag].placemark.setGeometry(point);
// }




/*
function CometControlsQueryPropertiesLoadGE() {
	
	tabset.rhs1_geovizreq=cTabLoad("index_geoviz.html","geoviz_page");
	
	tabset.rhs1_geovizreq.onreadystatechange = function() {
		if ((tabset.rhs1_geovizreq.readyState == 4) && (tabset.rhs1_geovizreq.status == 200)) {
			CometControlsLog("loading "+tabset.rhs1_geovizreq.html_page+" content to object "+tabset.rhs1_geovizreq.tgt);
			document.getElementById(tabset.rhs1_geovizreq.tgt).innerHTML=tabset.rhs1_geovizreq.responseText;
			google.earth.createInstance('map3d', GoogleControlsInitCallback, GoogleControlsFailureCallback);
			CometStopAJAXAnimation();
			CometControlsLog("Google Plugin should be finished loading now");
		}
	}
	return true;			
}




////////////// GoogleControl Functions ///////////////////////////////

function GoogleExistanceCheck(ge) {
	if(typeof ge == 'undefined') {
		
		console.log("google earth not supported.. turning off google earth plugin support");
		
		global_properties.google_earth_enabled=false;
	}
	return global_properties.google_earth_enabled;
}	


 //INTERNAL only 
function GoogleControlsInitPlacemarkList() {
	CometControlsLog("initialize placemarklist");
	//	var listing="ABCDEFGHIJKLMNOPQRSTUVWXYZ*";
	//create extra placemark for generating XML
	for(var i=0; i<num_of_tags+1; i++) {
		//console.log("loop "+i+" of "+num_of_tags);
		var o=new Object();
		
		o.tag=i;
		if(global_properties.google_earth_enabled) {
			o.balloon=ge.createHtmlStringBalloon('');
			o.placemark = ge.createPlacemark('placemark'+o.tag);
			//o.placemark.setName('Unknown');
			ge.getFeatures().appendChild(o.placemark);
			var styleMap = ge.createStyleMap('');
			// Create style map for placemark
			var icon = ge.createIcon('');
			icon.setHref(paddle_icon);
			var style = ge.createStyle('');
			style.getIconStyle().setIcon(icon);
			style.getIconStyle().setScale(0.0);
		
			var highlightStyle = ge.createStyle('');
			var highlightIcon = ge.createIcon('');
		
	        	highlightIcon.setHref(paddle_icon);
			highlightStyle.getIconStyle().setIcon(highlightIcon);
			highlightStyle.getIconStyle().setScale(0.0);
			styleMap.setNormalStyle(style);
        		styleMap.setHighlightStyle(highlightStyle);

			// Apply stylemap to a placemark.
	        	o.placemark.setStyleSelector(styleMap);
		}
		o.showBalloon=false;
		o.active=false;
		o.desc="";
		o.geotagged=false;
		o.dont_autoplay=false;
		o.loading=false;
		o.playing=false;
		o.flownin=true;
		placemarkList[o.tag]=o;
		if(global_properties.google_earth_enabled && global_properties.balloon_click) {
			google.earth.addEventListener(placemarkList[o.tag].placemark, 'click', function(event) {
				// prevent the default balloon from popping up
				var thistag=o.tag;
	  			event.preventDefault();
				var placemark = event.getTarget();
				CometControlsLog("id is ="+placemark.getId());
				var tagList=placemark.getId().split('k');
				var tag=tagList[1];
				GoogleControlsToggleBalloon(tag);
			});
		}
	}
}    

//INTERNAL Only
function GoogleControlsInitCallback(instance) {
	ge = instance;
	
	if(!GoogleExistanceCheck(ge)) return;
	
	ge.getWindow().setVisibility(true);
	// add a navigation control
	ge.getNavigationControl().setVisibility(ge.VISIBILITY_AUTO);
	// add some layers
	ge.getLayerRoot().enableLayerById(ge.LAYER_BORDERS, true);
	ge.getLayerRoot().enableLayerById(ge.LAYER_ROADS, true);
	ge.getLayerRoot().enableLayerById(ge.LAYER_TERRAIN, true);
	ge.getLayerRoot().enableLayerById(ge.LAYER_BUILDINGS, true);
	ge.getOptions().setFlyToSpeed(ge.SPEED_TELEPORT); 
	ge.getOptions().setStatusBarVisibility(true);
	ge.getOptions().setOverviewMapVisibility(true);
//        google.earth.addEventListener(ge, "frameend", rotateEarth); 
	google.earth.addEventListener(ge.getView(), 'viewchange', CometControlsCoordinatesCB );
	defaultLookAt =  ge.getView().copyAsLookAt(ge.ALTITUDE_RELATIVE_TO_GROUND)
	defaultCamera = ge.getView().copyAsCamera(ge.ALTITUDE_RELATIVE_TO_GROUND);
	document.getElementById('installed-plugin-version').innerHTML = ge.getPluginVersion().toString();
	GoogleControlsInitPlacemarkList();
	
	//TEMPORARILY disabled login/logout
	//if(showLogin) CometControlsLogoutBTN()
	ge.getOptions().setFlyToSpeed(0.5);
	
}

function GoogleControlsReloadGoogleEarth() {
	document.getElementById('map3d').innerHTML = '';
	document.getElementById('installed-plugin-version').innerHTML='';
	google.load("earth","1");
	google.earth.createInstance('map3d', GoogleControlsInitCallback, GoogleControlsFailureCallback);
}

 
//INTERNAL Only    
function GoogleControlsFailureCallback(errorCode) {
}


function GoogleControlsHideAllBalloons() {
	CometControlsLog("hideallballoons.... should never get here");
	for(var i=0; i<num_of_tags; i++) {
		placemarkList[i].showBalloon=false;
		placemarkList[i].balloon.setContentString('');
	}
	ge.setBalloon(null);
}

function GoogleControlsFly(tag, hide_them) {
	if(hide_them) GoogleControlsHideAllBalloons();
	if(!global_properties.google_earth_enabled || !placemarkList[tag] || !placemarkList[tag].geotagged) return;

	CometControlsLog("BEGIN GoogleControlsFly("+tag+") with flownin="+placemarkList[tag].flownin);
	if(placemarkList[tag].flownin) {
		GoogleControlsFlyOverLatLong_INTERNAL(placemarkList[tag].mdm.latitude,placemarkList[tag].mdm.longitude);
	} else {
		GoogleControlsFlyThereLatLong_INTERNAL(placemarkList[tag].mdm.latitude,placemarkList[tag].mdm.longitude);
	}
	placemarkList[tag].flownin=!placemarkList[tag].flownin;
	CometControlsLog("END GoogleControlsFly("+tag+") with flownin="+placemarkList[tag].flownin);
}

function GoogleControlsFlyThereLatLong_INTERNAL(lat,long) {
	var la = ge.createLookAt('');
	CometControlsLog("BEGIN GoogleControlsFlyThereLatLong_INTERNAL("+lat+","+long+")");
	cTabFliptoPage("RHS",page_number_geoviz);
	
	la.set(Number(lat), Number(long), 0, ge.ALTITUDE_RELATIVE_TO_GROUND, 0, 0, 5000 );
	ge.getView().setAbstractView(la);
	var la = ge.createLookAt('');
	la.set(Number(lat), Number(long), 0, ge.ALTITUDE_RELATIVE_TO_GROUND, 0, 80, 100 );
	ge.getView().setAbstractView(la);

//	GoogleControlsHideAllBalloons();
	CometControlsLog("END GoogleControlsFlyThereLatLong_INTERNAL("+lat+","+long+")");
}

//Internal
function GoogleControlsFlyOverLatLong_INTERNAL(lat,long) {
	CometControlsLog("BEGIN GoogleControlsFlyOverLatLong_INTERNAL("+lat+","+long+")");
	cTabFliptoPage("RHS",page_number_geoviz);
	var la = ge.createLookAt('');
	CometControlsLog("lat="+lat+", long="+long);
	
	la.set( Number(lat), Number(long), 0, ge.ALTITUDE_RELATIVE_TO_GROUND, 0, 0, 1000000 );
	ge.getView().setAbstractView(la);
//	GoogleControlsHideAllBalloons();
	CometControlsLog("END GoogleControlsFlyOverLatLong_INTERNAL("+lat+","+long+")");
}

function GoogleControlsHideMarker(_tag) {
	if(!GoogleExistanceCheck(ge)) return;

	var style = ge.createStyle('');
	style.getIconStyle().setScale(0.0);
	var styleMap = ge.createStyleMap('');
	styleMap.setNormalStyle(style);
        styleMap.setHighlightStyle(style);

	if(!placemarkList[_tag]) return;
	
	if(!placemarkList[_tag].placemark) return;
	
	if(!placemarkList[_tag].geotagged) return;
	placemarkList[_tag].placemark.setStyleSelector(styleMap);
	
	placemarkList[_tag].placemark.setName('');
}

function GoogleControlsHideAllMarkers() {
	for(var i=0; i<num_of_tags; i++) {
		GoogleControlsHideMarker(i);
	}
}

function GoogleControlsShowMarker(_tag) {
	if(!global_properties.google_earth_enabled) return;
	if(!placemarkList[_tag].geotagged) return;
	CometControlsLog("BEGIN GoogleControlsShowMarker("+_tag+")");
	CometControlsLog("geotagged="+placemarkList[_tag].geotagged);
	placemarkList[_tag].placemark.setName(placemarkList[_tag].desc);
	ge.getFeatures().appendChild(placemarkList[_tag].placemark);
	var styleMap = ge.createStyleMap('');
	// Create style map for placemark
	var icon = ge.createIcon('');
	icon.setHref(paddle_icon);
	var style = ge.createStyle('');
	style.getIconStyle().setIcon(icon);
	placemarkList[_tag].placemark.setStyleSelector(style);

	var highlightStyle = ge.createStyle('');
        var highlightIcon = ge.createIcon('');
	
        highlightIcon.setHref(paddle_icon);
        highlightStyle.getIconStyle().setIcon(highlightIcon);
        highlightStyle.getIconStyle().setScale(2.0);

	styleMap.setNormalStyle(style);
        styleMap.setHighlightStyle(highlightStyle);

	// Apply stylemap to a placemark.
        placemarkList[_tag].placemark.setStyleSelector(styleMap);
	var point = ge.createPoint('');
	//CometControlsLog("center = "+placemarkList[_tag].mdm.longitude+" ,"+ placemarkList[_tag].mdm.latitude);
	point.setLatitude(parseFloat(placemarkList[_tag].mdm.latitude));
	point.setLongitude(parseFloat(placemarkList[_tag].mdm.longitude));
	placemarkList[_tag].placemark.setGeometry(point);
}

function GoogleControlsMakeVisibleBTN() {
	cTabFliptoPage("RHS",page_number_geoviz);
}

function GoogleControlFlytoGeoLocation(geocodeLocation) {
  var geocoder = new google.maps.ClientGeocoder();
  geocoder.getLatLng(geocodeLocation, function(point) {
    if (point) {
      var lookAt = ge.createLookAt('');
      lookAt.set(point.y, point.x, 10, ge.ALTITUDE_RELATIVE_TO_GROUND,
                 0, 60, 20000);
      ge.getView().setAbstractView(lookAt);
    }
  });
}

function GoogleControlsUnLoadKML(tag) {
	CometControlsLog("detach KML from google earth for tag "+tag);
	placemarkList[tag].playing=false;
	ge.getFeatures().removeChild(placemarkList[tag].kml);
}

function GoogleControlsUnLoadKMLviaNetworkLink(tag) {
	CometControlsLog("detach KML (network link) from google earth for tag "+tag);
	if(placemarkList[tag] && placemarkList[tag].networkLink && placemarkList[tag].geotagged && placemarkList[tag].playing) ge.getFeatures().removeChild(placemarkList[tag].networkLink);
	placemarkList[tag].playing=false;

}
function CometControlsUnLoadKMLPath(tag) {
	placemarkList[tag].playing=false;
//	if(placemarkList[tag].networkLink.parent) //dplacemarkList[tag].networkLink.parent.removeChild(placemarkList[tag].networkLink);
	ge.getFeatures().removeChild(placemarkList[tag].networkLink);
}
function CometControlsLoadKMLPath(tag) {
	CometControlsLog("****BEGIN CometControlsKMLPath("+tag+")*****");
	cTabFliptoPage("RHS",page_number_geoviz);
	if(placemarkList[tag].playing) {
//		placemarkList[tag].playing=false;
		GoogleControlsUnLoadKMLviaNetworkLink(tag);
		return;
	}
	var link = ge.createLink('');
	var url=global_properties.imageprefix+"Relay?path="+placemarkList[tag].mdm.urlName+
			"&type=generate&annotation=kml_path&stream&astext";
	link.setHref(url);
	placemarkList[tag].networkLink = ge.createNetworkLink('');
	placemarkList[tag].networkLink.set(link, true, true); // Sets the link, refreshVisibility, and flyToView
	ge.getFeatures().appendChild(placemarkList[tag].networkLink);
	placemarkList[tag].playing=true;
	if(!ge.getTime().getControl().getVisibility()) {
		ge.getTime().getControl().setVisibility(true);
	}
	CometControlsLog("****END CometControlsKMLPath("+tag+")****");
}

function GoogleControlsLoadKMLviaNetworkLinkByRLU(rlu) {
	var tag=CometControlsRLU(rlu);
	GoogleControlsLoadKMLviaNetworkLinkByTag(tag);
}
	
function GoogleControlsLoadKMLviaNetworkLinkByTag(tag) {
	if(placemarkList[tag].playing) return;
	var link = ge.createLink('');
	link.setHref(global_properties.imageprefix+"Relay?path="+placemarkList[tag].mdm.urlName+"&type=object&stream");
	placemarkList[tag].networkLink = ge.createNetworkLink('');

	placemarkList[tag].networkLink.set(link, true, true); // Sets the link, refreshVisibility, and flyToView
	ge.getFeatures().appendChild(placemarkList[tag].networkLink);
	
//	document.getElementById("action_tag_"+tag).innerHTML='<center><img onmouseover="CometControlsTip(\'Unload KML\')" onmouseout="CometControlsUnTip()" onmousedown="javascript:GoogleControlsUnLoadKMLviaNetworkLink(\''+tag+'\')" src="images/icons/stop-icon.png" width=32 height=32></center></span>';
	if(!ge.getTime().getControl().getVisibility()) {
		ge.getTime().getControl().setVisibility(true);
	}
}

function GoogleControlsLoadKML(rlu) {
	CometControlsLog("****GoogleControlsLoadKML::: started processing****");
	var tag=CometControlsRLU(rlu);
	global_tag=tag;
	document.getElementById("action_tag_"+tag).innerHTML="Loading KML";
	
//	CometControlsLog("looking at url="+url);
	var request=getRequestObject();	
	if(placemarkList[tag].playing) return;
	 
	request.onreadystatechange = function() {
		if(request.responseText!="" && !placemarkList[global_tag].playing) {
			placemarkList[global_tag].playing=true;
			CometControlsLog("====GoogleControlsLoadKML::: start onreadystatechange===");
			CometControlsLog("loading response = \""+request.responseText+"\"");
			CometControlsLog("tag = \""+global_tag+"\"");
			placemarkList[global_tag].kml=ge.parseKml(request.responseText);
			ge.getFeatures().appendChild(placemarkList[global_tag].kml);	
			CometControlsLog("===GoogleControlsLoadKML::: end onreadystatechange===");
			if(!ge.getTime().getControl().getVisibility()) {
				ge.getTime().getControl().setVisibility(true);
			}
		}
	}
	var params="?path="+placemarkList[tag].mdm.urlName+"&type=object&stream";
	CometControlsLog("params = \""+params+"\"");
	request.open("GET", "Relay"+params, true);
	request.send(null);
	CometControlsLog("****GoogleControlsLoadKML::: end processing******");
}

function GoogleControlsShowSky() {
  ge.getOptions().setMapType(ge.MAP_TYPE_SKY);
}

function GoogleControlsShowEarth() {
	ge.getOptions().setMapType(ge.MAP_TYPE_EARTH);
}

function GoogleControlsShowMoon() {
	document.getElementById('map3d').innerHTML = '';
	//GoogleControlsInitCallback, GoogleControlsFailureCallback


	google.earth.createInstance('map3d', null, GoogleControlsFailureCallback, { database: 'http://khmdb.google.com/?db=moon' });
}

function GoogleControlsShowMars() {
	document.getElementById('map3d').innerHTML = '';
	google.earth.createInstance('map3d', null, GoogleControlsFailureCallback, { database: 'http://khmdb.google.com/?db=mars' });
}

*/



/******* Test functions **********/
/*
var ge;

var counter = -1;

google.load("earth", "1");

function init() {
	google.earth.createInstance('map3d', GE_testInitCallback, GE_testFailureCallback);

	addSampleButton('Create a Placemark!', GE_testAddPM);
	addSampleButton('Remove a Placemark!', GE_testRemovePM);
	addSampleButton('Change icon!', GE_testChangePMIcon);
	addSampleButton('fly test', GE_testFly);
}

function GE_testInitCallback(instance) {
	ge = instance;
	ge.getWindow().setVisibility(true);

	// add a navigation control
	ge.getNavigationControl().setVisibility(ge.VISIBILITY_AUTO);

	// add some layers
	ge.getLayerRoot().enableLayerById(ge.LAYER_BORDERS, true);
	ge.getLayerRoot().enableLayerById(ge.LAYER_ROADS, true);

	var la = ge.getView().copyAsLookAt(ge.ALTITUDE_RELATIVE_TO_GROUND);
	la.setRange(100000);
	ge.getView().setAbstractView(la);

	document.getElementById('installed-plugin-version').innerHTML =
	ge.getPluginVersion().toString();

	//add first one automatically
	GE_testAddPM();
	
}

function GE_testFailureCallback(errorCode) {
}

function GE_testChangePMIcon() {
	GE_changeIconTo("placemark" + counter,'http://maps.google.com/mapfiles/kml/paddle/blu-blank.png');
}

function GE_testFly() {
	GE_flyToPMToggle("placemark" + counter);
}

function GE_testAddPM() {
	if(counter==200) return;
	
	if(counter==-1) GE_enableDraggableListeners();
	
	counter++;
	GE_createSimplePlacemarkHere("placemark" + counter,"placemark" + counter,'http://maps.google.com/mapfiles/kml/paddle/red-circle.png');
}

function GE_testRemovePM() {
  if(counter==-1) return;
  GE_removePlacemark("placemark" + counter);
  counter--;
  if(counter==-1) GE_disableDraggableListeners();
}


*/

