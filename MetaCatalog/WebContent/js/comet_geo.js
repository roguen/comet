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
var geo_driver;

// var coordinates=new Object();
// 
// coordinates.nw_longitude=0;
// coordinates.nw_latitude=0;
// 
// 
// coordinates.ne_longitude=0;
// coordinates.ne_latitude=0;
// 
// coordinates.sw_longitude=0;
// coordinates.sw_latitude=0;
// 
// coordinates.se_longitude=0;
// coordinates.se_latitude=0;
// 
// coordinates.longitude=0;
// coordinates.latitude=0;


function CometGeo_Init(driver) {
	geo_driver=driver;
	var r=false;
	switch (driver) {
		case "google_earth":
			CometLoading("Initializing Geo Visualization...");
			r=CometGeo_driver_GE_Init();
		break;
		case "google_maps":
		CometControlsLog("Google Maps support coming soon");
		global_properties.geo_enabled=false;
		break;
		case "dummy":
		CometControlsLog("dummy support coming soon");
		global_properties.geo_enabled=false;
		break;
		case "cesium":
		CometControlsLog("cesium support coming soon");
		global_properties.geo_enabled=false;

		break;
		case "disabled":
		global_properties.geo_enabled=false;
		break;
		default:
		CometControlsLog("ERROR: unsupported driver: "+driver);
		global_properties.geo_enabled=false;
	}
	
	return r;
}

// function CometGeo_getCoordinates_DONOTUSE() {
// 	CometControlsLog("getting coordinates from geo abstraction layer");
// 
//  	switch (driver) {
//  		case "google_earth":
// 			CometGeo_driver_GE_getCoordinates();
// 			CometControlsLog("google earth!!");
// 		break;
// 		case "google_maps":
// 		CometControlsLog("Google Maps support coming soon");
// 		break;
// 		case "dummy":
// 		CometControlsLog("dummy support coming soon");
// 		break;
// 		case "cesium":
// 		CometControlsLog("cesium support coming soon");
// 		
// 		break;
//  		default:
//  		CometControlsLog("ERROR: unsupported driver: "+driver);
//  	}
// 	
// }


function CometGeo_HideAllMarkers() {
	CometControlsLog("inside CometGeo_HideAllMarkers()");
	switch (geo_driver) {
		case "google_earth":
			CometGeo_driver_GE_HideAllMarkers();
		break;
		case "google_maps":
		CometControlsLog("Google Maps support coming soon");
		break;
		case "dummy":
		CometControlsLog("dummy support coming soon");
		break;
		case "cesium":
		CometControlsLog("cesium support coming soon");
		
		break;
		default:
		CometControlsLog("ERROR: unsupported driver: "+geo_driver);
	}
}

function CometGeo_ExistsPlacemark(tag) {
	switch (geo_driver) {
		case "google_earth":
			return CometGeo_driver_GE_ExistsPlacemark(tag);
		break;
		case "google_maps":
		CometControlsLog("Google Maps support coming soon");
		break;
		case "dummy":
		CometControlsLog("dummy support coming soon");
		break;
		case "cesium":
		CometControlsLog("cesium support coming soon");
		
		break;
		default:
		CometControlsLog("ERROR: unsupported driver: "+geo_driver);
	}
	return false;
}

function CometGeo_DestroyPlacemark(tag) {
	CometControlsLog("inside CometGeo_DestroyPlacemark()");
	switch (geo_driver) {
		case "google_earth":
			CometGeo_driver_GE_DestroyPlacemark(tag);
		break;
		case "google_maps":
		CometControlsLog("Google Maps support coming soon");
		break;
		case "dummy":
		CometControlsLog("dummy support coming soon");
		break;
		case "cesium":
		CometControlsLog("cesium support coming soon");
		
		break;
		default:
		CometControlsLog("ERROR: unsupported driver: "+geo_driver);
	}
}


function CometGeo_CreatePlacemark(tag) {
	CometControlsLog("inside CometGeo_CreatePlacemark()");
	
	if(CometGeo_ExistsPlacemark(tag)) CometGeo_DestroyPlacemark(tag);

	switch (geo_driver) {
		case "google_earth":
			CometGeo_driver_GE_CreatePlacemark(tag);
		break;
		case "google_maps":
		CometControlsLog("Google Maps support coming soon");
		break;
		case "dummy":
		CometControlsLog("dummy support coming soon");
		break;
		case "cesium":
		CometControlsLog("cesium support coming soon");
		
		break;
		default:
		CometControlsLog("ERROR: unsupported driver: "+geo_driver);
	}
}

function CometGeo_createSimplePlacemark(label, title, iconurl, latitude, longitude) { //duplicate of createplacemark or very similar
	CometControlsLog("inside CometGeo_CreatePlacemark()");
	
	//if(CometGeo_ExistsPlacemark(tag)) CometGeo_DestroyPlacemark(tag);

	switch (geo_driver) {
		case "google_earth":
			CometGeo_driver_GE_createSimplePlacemark(label, title, iconurl, latitude, longitude);
		break;
		case "google_maps":
		CometControlsLog("Google Maps support coming soon");
		break;
		case "dummy":
		CometControlsLog("dummy support coming soon");
		break;
		case "cesium":
		CometControlsLog("cesium support coming soon");
		
		break;
		default:
		CometControlsLog("ERROR: unsupported driver: "+geo_driver);
	}
}


function CometGeo_HideBalloon(tag) {
	CometControlsLog("inside CometGeo_HideBalloon()");
	switch (geo_driver) {
		case "google_earth":
			CometGeo_driver_GE_HideBalloon(tag);
		break;
		case "google_maps":
		CometControlsLog("Google Maps support coming soon");
		break;
		case "dummy":
		CometControlsLog("dummy support coming soon");
		break;
		case "cesium":
		CometControlsLog("cesium support coming soon");
		
		break;
		default:
		CometControlsLog("ERROR: unsupported driver: "+geo_driver);
	}
}
function CometGeo_HideAllBalloons() {
	CometControlsLog("inside CometGeo_HideAllBalloons()");
	switch (geo_driver) {
		case "google_earth":
			CometGeo_driver_GE_HideAllBalloons();
		break;
		case "google_maps":
		CometControlsLog("Google Maps support coming soon");
		break;
		case "dummy":
		CometControlsLog("dummy support coming soon");
		break;
		case "cesium":
		CometControlsLog("cesium support coming soon");
		
		break;
		default:
		CometControlsLog("ERROR: unsupported driver: "+geo_driver);
	}
}

function CometGeo_Fly(tag, hide_them) {
	if(hide_them) CometGeo_HideAllBalloons();
	if(!placemarkList[tag] || !placemarkList[tag].geotagged) return;

	if(placemarkList[tag].flownin) {
		CometGeo_FlyOverLatLong_INTERNAL(placemarkList[tag].latitude,placemarkList[tag].longitude);
	} else {
		CometGeo_FlyThereLatLong_INTERNAL(placemarkList[tag].latitude,placemarkList[tag].longitude);
	}
	placemarkList[tag].flownin=!placemarkList[tag].flownin;
}

function CometGeo_FlyThereLatLong_INTERNAL(lat,long) {
	CometControlsLog("inside CometGeo_FlyThereLatLong_INTERNAL()");
	switch (geo_driver) {
		case "google_earth":
			CometGeo_driver_GE_FlyThereLatLong_INTERNAL(lat,long);
		break;
		case "google_maps":
		CometControlsLog("Google Maps support coming soon");
		break;
		case "dummy":
		CometControlsLog("dummy support coming soon");
		break;
		case "cesium":
		CometControlsLog("cesium support coming soon");
		
		break;
		default:
		CometControlsLog("ERROR: unsupported driver: "+geo_driver);
	}
}

function CometGeo_FlyOverLatLong_INTERNAL(lat,long) {
	CometControlsLog("inside CometGeo_FlyOverLatLong_INTERNAL()");
	switch (geo_driver) {
		case "google_earth":
			CometGeo_driver_GE_FlyOverLatLong_INTERNAL(lat,long);
		break;
		case "google_maps":
		CometControlsLog("Google Maps support coming soon");
		break;
		case "dummy":
		CometControlsLog("dummy support coming soon");
		break;
		case "cesium":
		CometControlsLog("cesium support coming soon");
		
		break;
		default:
		CometControlsLog("ERROR: unsupported driver: "+geo_driver);
	}
}

function CometGeo_ToggleBalloon(tag) {
	CometControlsLog("inside CometGeo_ToggleBalloon()");
	switch (geo_driver) {
		case "google_earth":
			CometGeo_driver_GE_ToggleBalloon(tag);
		break;
		case "google_maps":
		CometControlsLog("Google Maps support coming soon");
		break;
		case "dummy":
		CometControlsLog("dummy support coming soon");
		break;
		case "cesium":
		CometControlsLog("cesium support coming soon");
		
		break;
		default:
		CometControlsLog("ERROR: unsupported driver: "+geo_driver);
	}
}
function CometGeo_FlytoGeoLocation(geocodeLocation) {
	switch (geo_driver) {
		case "google_earth":
			CometGeo_driver_GE_FlytoGeoLocation(geocodeLocation);
		break;
		case "google_maps":
		CometControlsLog("Google Maps support coming soon");
		break;
		case "dummy":
		CometControlsLog("dummy support coming soon");
		break;
		case "cesium":
		CometControlsLog("cesium support coming soon");
		
		break;
		default:
		CometControlsLog("ERROR: unsupported driver: "+geo_driver);
	}
}
function CometGeo_LoadKML(tag) {
	cTabFliptoPage("RHS",page_number_geoviz);
	if(placemarkList[tag].playing) {
		CometControlsLog("already playing KML for tag "+tag+", redirecting to unload kml");
		CometGeo_UnLoadKML(tag);
		return;
	}
	switch (geo_driver) {
		case "google_earth":
			CometControlsLog("about to load KML for tag: "+tag);
			CometGeo_driver_GE_LoadKML(tag);
		break;
		case "google_maps":
		CometControlsLog("Google Maps support coming soon");
		break;
		case "dummy":
		CometControlsLog("dummy support coming soon");
		break;
		case "cesium":
		CometControlsLog("cesium support coming soon");
		
		break;
		default:
		CometControlsLog("ERROR: unsupported driver: "+geo_driver);
	}
}
function CometGeo_UnLoadKML(tag) {
	placemarkList[tag].playing=false;
	switch (geo_driver) {
		case "google_earth":
			CometGeo_driver_GE_UnLoadKML(tag);
		break;
		case "google_maps":
		CometControlsLog("Google Maps support coming soon");
		break;
		case "dummy":
		CometControlsLog("dummy support coming soon");
		break;
		case "cesium":
		CometControlsLog("cesium support coming soon");
		
		break;
		default:
		CometControlsLog("ERROR: unsupported driver: "+geo_driver);
	}
}

function CometGeo_ClearAll() {
	switch (geo_driver) {
		case "google_earth":
			CometGeo_driver_GE_ClearAll();
		break;
		case "google_maps":
		CometControlsLog("Google Maps support coming soon");
		break;
		case "dummy":
		CometControlsLog("dummy support coming soon");
		break;
		case "cesium":
		CometControlsLog("cesium support coming soon");
		
		break;
		default:
		CometControlsLog("ERROR: unsupported driver: "+geo_driver);
	}
}
function CometGeo_CreateAllPlacemarks() {
	switch (geo_driver) {
		case "google_earth":
			CometGeo_driver_GE_CreateAllPlacemarks();
		break;
		case "google_maps":
		CometControlsLog("Google Maps support coming soon");
		break;
		case "dummy":
		CometControlsLog("dummy support coming soon");
		break;
		case "cesium":
		CometControlsLog("cesium support coming soon");
		
		break;
		default:
		CometControlsLog("ERROR: unsupported driver: "+geo_driver);
	}
}

function CometGeo_LoadComplete() {
	
//	CometGeo_ShowTab();
	loadables.geo=true;
	if(geo_driver!="disabled") CometLoading("Geo Visualization Complete");

}

function CometGeo_Enabled() {
	switch (geo_driver) {
		case "google_earth":
			return CometGeo_driver_GE_Enabled();
		break;
		case "google_maps":
		CometControlsLog("Google Maps support coming soon");
		break;
		case "dummy":
		CometControlsLog("dummy support coming soon");
		break;
		case "cesium":
		CometControlsLog("cesium support coming soon");
		
		break;
		case "disabled":
		default:
		CometControlsLog("ERROR: unsupported driver: "+geo_driver);
		return false;
	}
	return true;
}

function CometGeo_Disabled() {
	return !CometGeo_Enabled();
}

function CometGeo_OverheadZoom(latitude, longitude) {
	switch (geo_driver) {
		case "google_earth":
			CometGeo_driver_GE_OverheadZoom(latitude,longitude);
		break;
		case "google_maps":
		CometControlsLog("Google Maps support coming soon");
		break;
		case "dummy":
		CometControlsLog("dummy support coming soon");
		break;
		case "cesium":
		CometControlsLog("cesium support coming soon");
		
		break;
		case "disabled":
		default:
		CometControlsLog("ERROR: unsupported driver: "+geo_driver);
		
	}
}

function CometGeo_CreateScreenOverlay(canvas, label, image_url, image_width, image_height) {
	switch (geo_driver) {
		case "google_earth":
			CometGeo_driver_GE_CreateScreenOverlay(canvas, label, image_url, image_width, image_height);
		break;
		case "google_maps":
		CometControlsLog("Google Maps support coming soon");
		break;
		case "dummy":
		CometControlsLog("dummy support coming soon");
		break;
		case "cesium":
		CometControlsLog("cesium support coming soon");
		
		break;
		case "disabled":
		default:
		CometControlsLog("ERROR: unsupported driver: "+geo_driver);
		
	}
} 

function CometGeo_HideScreenOverlay(label) {
	switch (geo_driver) {
		case "google_earth":
			CometGeo_driver_GE_HideScreenOverlay(label);
		break;
		case "google_maps":
		CometControlsLog("Google Maps support coming soon");
		break;
		case "dummy":
		CometControlsLog("dummy support coming soon");
		break;
		case "cesium":
		CometControlsLog("cesium support coming soon");
		
		break;
		case "disabled":
		default:
		CometControlsLog("ERROR: unsupported driver: "+geo_driver);
		
	}
}

function CometGeo_removePlacemark(label) {
	switch (geo_driver) {
		case "google_earth":
			CometGeo_driver_GE_removePlacemark(label);
		break;
		case "google_maps":
		CometControlsLog("Google Maps support coming soon");
		break;
		case "dummy":
		CometControlsLog("dummy support coming soon");
		break;
		case "cesium":
		CometControlsLog("cesium support coming soon");
		
		break;
		case "disabled":
		break;
		default:
		CometControlsLog("ERROR: unsupported driver: "+geo_driver);
		
	}
}

function CometGeo_createPlacemarkHere(label, title, icon_url) {
	switch (geo_driver) {
		case "google_earth":
			CometGeo_driver_GE_createPlacemarkHere(label,title,icon_url);
		break;
		case "google_maps":
		CometControlsLog("Google Maps support coming soon");
		break;
		case "dummy":
		CometControlsLog("dummy support coming soon");
		break;
		case "cesium":
		CometControlsLog("cesium support coming soon");
		break;
		case "disabled":
		break;
		default:
		CometControlsLog("ERROR: unsupported driver: "+geo_driver);
		
	}
}

function CometGeo_changeIconTo(label, new_icon_url, scale) {
	switch (geo_driver) {
		case "google_earth":
			CometGeo_driver_GE_changeIconTo(label,new_icon_url,scale);
		break;
		case "google_maps":
		CometControlsLog("Google Maps support coming soon");
		break;
		case "dummy":
		CometControlsLog("dummy support coming soon");
		break;
		case "cesium":
		CometControlsLog("cesium support coming soon");
		break;
		case "disabled":
		break;
		default:
		CometControlsLog("ERROR: unsupported driver: "+geo_driver);
		
	}
}


function CometGeo_disableDraggableListeners() {
	switch (geo_driver) {
		case "google_earth":
			CometGeo_driver_GE_disableDraggableListeners();
		break;
		case "google_maps":
		CometControlsLog("Google Maps support coming soon");
		break;
		case "dummy":
		CometControlsLog("dummy support coming soon");
		break;
		case "cesium":
		CometControlsLog("cesium support coming soon");
		break;
		case "disabled":
		break;
		default:
		CometControlsLog("ERROR: unsupported driver: "+geo_driver);
		
	}
}

function CometGeo_enableDraggableListeners() {
	switch (geo_driver) {
		case "google_earth":
			CometGeo_driver_GE_enableDraggableListeners();
		break;
		case "google_maps":
		CometControlsLog("Google Maps support coming soon");
		break;
		case "dummy":
		CometControlsLog("dummy support coming soon");
		break;
		case "cesium":
		CometControlsLog("cesium support coming soon");
		break;
		case "disabled":
		break;
		default:
		CometControlsLog("ERROR: unsupported driver: "+geo_driver);
		
	}
}



function CometGeo_addEventListener(act,func) {
	switch (geo_driver) {
		case "google_earth":
			google.earth.addEventListener(ge.getWindow(), act, func);
		break;
		case "google_maps":
		CometControlsLog("Google Maps support coming soon");
		break;
		case "dummy":
		CometControlsLog("dummy support coming soon");
		break;
		case "cesium":
		CometControlsLog("cesium support coming soon");
		break;
		case "disabled":
		break;
		default:
		CometControlsLog("ERROR: unsupported driver: "+geo_driver);
		
	}
}


function CometGeo_removeEventListener(act,func) {
	switch (geo_driver) {
		case "google_earth":
			google.earth.removeEventListener(ge.getWindow(), act, func);
		break;
		case "google_maps":
		CometControlsLog("Google Maps support coming soon");
		break;
		case "dummy":
		CometControlsLog("dummy support coming soon");
		break;
		case "cesium":
		CometControlsLog("cesium support coming soon");
		break;
		case "disabled":
		break;
		default:
		CometControlsLog("ERROR: unsupported driver: "+geo_driver);
		
	}
}

function CometGeo_ShowTab() {
	if(!CometGeo_Enabled()) return;

	switch (geo_driver) {
		case "google_earth":
			document.getElementById("RHS_tabHeader_"+page_number_geoviz).style.display="block";
		break;
		case "google_maps":
		CometControlsLog("Google Maps support coming soon");
		break;
		case "dummy":
		CometControlsLog("dummy support coming soon");
		break;
		case "cesium":
		CometControlsLog("cesium support coming soon");
		
		break;
		case "disabled":
		break;
		default:
		CometControlsLog("ERROR: unsupported driver: "+driver);
	}
}

//temporary
function GoogleExistanceCheck(ge) {
	if(typeof ge == 'undefined') {
		
		console.log("google earth not supported.. turning off google earth plugin support");
		
		global_properties.geo_enabled=false;
	}
	return global_properties.geo_enabled;
}	

	
