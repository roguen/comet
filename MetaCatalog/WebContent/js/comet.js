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


try {
	console.log("Loading script");
} catch (e) {
	console = {
		log : function() {
		}
	};
}

if (typeof String.prototype.trim != 'function') { // detect native implementation
  String.prototype.trim = function () {
      return this.replace(/^\s+/, '').replace(/\s+$/, '');
  };
}

if (typeof String.prototype.contains === 'undefined') { String.prototype.contains = function(it) { 
	
	var temp=this;
//	CometControlsLog("does this="+temp+" contain "+it+"?"); 


	//return temp.toLower().indexOf(it.toLower()) != -1;
	return this.indexOf(it) != -1; 
}; }

if (typeof String.prototype.startsWith != 'function') {
  String.prototype.startsWith = function (str){
    return this.slice(0, str.length) == str;
  };
}

if (typeof String.prototype.endsWith != 'function') {
  String.prototype.endsWith = function (str){
    return this.slice(-str.length) == str;
  };
}

////////////// Generic Functions ///////////////////////////////

//converts an int to string
function itoa(i)
{ 
   return String.fromCharCode(i);
}

// Converts a char into to an integer (unicode value)
function atoi(a)
{ 
   return a.charCodeAt();
}

//Get the browser-specific request object, either for
//Firefox, Safari, Opera, Mozilla, Netscape, IE 8, or IE 7 (top entry);
//or for Internet Explorer 5 and 6 (bottom entry). 
function getRequestObject() {
	if (window.XMLHttpRequest) {
		return (new XMLHttpRequest());
	} else if (window.ActiveXObject) {
		return (new ActiveXObject("Microsoft.XMLHTTP"));
	} else {
		// Don't throw Error: this part is for very old browsers,
		// and Error was implemented starting in JavaScript 1.5.
		return (null);
	}
}

function MIN(a,b) {
	if(a<b) return a;
	return b;
}

function isNumber(n) {
  return !isNaN(parseFloat(n)) && isFinite(n);
}

function checkAll(checkname, bx) {
	for (i = 0; i < checkname.length; i++){
        	checkname[i].checked = bx.checked? true:false;
	}
}

function checkPage(bx){

CometControlsLog("getting elements by id=dataTable");

CometControlsLog("this check box is: "+bx.checked);

  CometControlsLog("all hits = "+viewable_objects);
  for(var i=0; i<viewable_objects; i++) {
  	document.getElementById('download_cb_'+i).checked=bx.checked;
  }

}



////////////// Global Variables ///////////////////////////////

var rolelist=new Array();
var selected_role=null;
var filesToDownload=new Array();
var ge;
var placemarkList=new Array();
var counter = 0;
var oldcounter = 0;
var speed =  15;  // degrees per second 
var lastMillis = (new Date()).getTime(); 
var defaultCamera;
var defaultLookAt;
var rotating=false;

var showLogin=true;
//var fileTypeArray = ["txt", "ppt", "doc"];
//var fileGroup="doc";
var login = "true";

var login_complete=false;


var askmlpath=false;

var global_annotations_num=0;
var global_annotations_list=new Array();	
			

var loadables=new Object();

loadables.geo=false;
loadables.wc=true
loadables.ingest=false;
loadables.admin=false;
loadables.ext=false;


var numscenes=0;
var scenetablebuilt=false;
var wctablebuilt=false;
var sceneallowed=false;

var dragInfo = null;

var pageflip_required=true;


var automatic_search_delay=10;
var automatic_next_delay=10;

var listsep=";;;";
var nvp_sep="===";

var taglist_sep=",,,";
var taglist_sep2=":::";

//activate and deactive draggable placemark listener for path editor
var path_editor_listener_active=false;	

var num_of_tags=0;

var properties_loaded=false;

//if(typeof google != 'undefined') google_earth_available=true;
//else google_earth_available=false;
		
//if(global_properties.google_earth_enabled) google.load("earth","1");
//else console.log("Google Earth not available");
	
//delay 3.5 seconds when triggering a "fly there" or "fly over" before opening the balloon		
var fly_balloon_delay=3500;

var viewable_objects=0;

var showTooltips=true;
var search_locked=false;

var busy_animate=0;

var quit_animating=false;


var uploader = new plupload.Uploader({
	runtimes : 'html5',
	browse_button : 'uploader_pickfiles',
	container: 'uploader_container',
	max_file_size : '4096mb',
	url : 'Upload',
	resize : {width : 320, height : 240, quality : 90},
	//	filters : [
	//		{title : "Image files", extensions : "jpg,gif,png"},
	//		{title : "Zip files", extensions : "zip"}
	//		
	//	]
	});




//////////******** BEGIN INGEST





var root_ingest_dir='/opt/COMETDist/InputDir/';
var root_ingest_dir_orig=root_ingest_dir;
var mins = 1;  //Set the number of minutes you need
var secs = mins * 60;
var currentSeconds = 0;
var currentMinutes = 0;


var rds_cur_pos=0;
var url="";
	
var wc_history=new Array;
var wc_history_index=0;	
var wc_clicks=0;	
var wc_load_in_progress=false;


var visualizer="nada";

var show_labels=false;


var log_iteration=50;
var log_read=0;
var log_locked=false;
var log_timer;
//////////******** END INGEST




var starting_point_hint="";
var page_number_geoviz=1;
var page_number_vidcat=page_number_geoviz+1; //2 
var page_number_wordcloud=page_number_vidcat+1; //3
var page_number_editor=page_number_wordcloud+1 //4
var page_number_notes=page_number_editor+1; //5
var page_number_ingest=page_number_notes+1; //6
var page_number_admin=page_number_ingest+1; //7


	
var ingest_cur_pos=100;


var coordinate_closed=true;

var calendar_shown=false;
var coordinates_shown=false;
var global_tag="";
var current_tag="";
var global_action="";


var nowplaying="";
var ondisplay_tag="";

var namespace="";
var tenant="";
var hcp_name="";
var hcp_domain="";


var current_title="";
var xml_content;

var path_mark_prefix="posmark";

var cur_longitude_id="pe_longitude";
var cur_latitude_id="pe_latitude";
var cur_datetime_id="pe_startdatetime";

//change to xml reference in the future
var paddle_icon='http://maps.google.com/mapfiles/kml/paddle/red-circle.png';


var global_properties=new Object();
global_properties.verbose=false;

var busyAnimRef=0;


var shouldTrackIngest=false;

global_properties.webapp="/";

var tabset=new Object();

////////////// dataTables Functions ///////////////////////////////


$.extend( $.fn.dataTableExt.oStdClasses, {
	"sWrapper": "dataTables_wrapper form-inline"
} );

// API method to get paging information
$.fn.dataTableExt.oApi.fnPagingInfo = function ( oSettings )
{
	return {
		"iStart":         oSettings._iDisplayStart,
		"iEnd":           oSettings.fnDisplayEnd(),
		"iLength":        oSettings._iDisplayLength,
		"iTotal":         oSettings.fnRecordsTotal(),
		"iFilteredTotal": oSettings.fnRecordsDisplay(),
		"iPage":          Math.ceil( oSettings._iDisplayStart / oSettings._iDisplayLength ),
		"iTotalPages":    Math.ceil( oSettings.fnRecordsDisplay() / oSettings._iDisplayLength )
	};
}

// Bootstrap style pagination control 
$.extend( $.fn.dataTableExt.oPagination, {
	"bootstrap": {
		"fnInit": function( oSettings, nPaging, fnDraw ) {
			var oLang = oSettings.oLanguage.oPaginate;
			var fnClickHandler = function ( e ) {
				e.preventDefault();
				if ( oSettings.oApi._fnPageChange(oSettings, e.data.action) ) {
					fnDraw( oSettings );
				}
			};

			$(nPaging).addClass('pagination').append(
				'<ul>'+
					'<li class="prev disabled"><a href="#">&larr; '+oLang.sPrevious+'</a></li>'+
					'<li class="next disabled"><a href="#">'+oLang.sNext+' &rarr; </a></li>'+
				'</ul>'
			);
			var els = $('a', nPaging);
			$(els[0]).bind( 'click.DT', { action: "previous" }, fnClickHandler );
			$(els[1]).bind( 'click.DT', { action: "next" }, fnClickHandler );
		},

		"fnUpdate": function ( oSettings, fnDraw ) {
			var iListLength = 5;
			var oPaging = oSettings.oInstance.fnPagingInfo();
			var an = oSettings.aanFeatures.p;
			var i, j, sClass, iStart, iEnd, iHalf=Math.floor(iListLength/2);

			if ( oPaging.iTotalPages < iListLength) {
				iStart = 1;
				iEnd = oPaging.iTotalPages;
			}
			else if ( oPaging.iPage <= iHalf ) {
				iStart = 1;
				iEnd = iListLength;
			} else if ( oPaging.iPage >= (oPaging.iTotalPages-iHalf) ) {
				iStart = oPaging.iTotalPages - iListLength + 1;
				iEnd = oPaging.iTotalPages;
			} else {
				iStart = oPaging.iPage - iHalf + 1;
				iEnd = iStart + iListLength - 1;
			}
			for ( i=0, iLen=an.length ; i<iLen ; i++ ) {
				// Remove the middle elements
				$('li:gt(0)', an[i]).filter(':not(:last)').remove();

				// Add the new list items and their event handlers
				for ( j=iStart ; j<=iEnd ; j++ ) {
					sClass = (j==oPaging.iPage+1) ? 'class="active"' : '';
					$('<li '+sClass+'><a href="#">'+j+'</a></li>')
						.insertBefore( $('li:last', an[i])[0] )
						.bind('click', function (e) {
							e.preventDefault();
							oSettings._iDisplayStart = (parseInt($('a', this).text(),10)-1) * oPaging.iLength;
							fnDraw( oSettings );
						} );
				}
				// Add / remove disabled classes from the static elements
				if ( oPaging.iPage === 0 ) {
					$('li:first', an[i]).addClass('disabled');
				} else {
					$('li:first', an[i]).removeClass('disabled');
				}
				if ( oPaging.iPage === oPaging.iTotalPages-1 || oPaging.iTotalPages === 0 ) {
					$('li:last', an[i]).addClass('disabled');
				} else {
					$('li:last', an[i]).removeClass('disabled');
				}
			}
		}
	}
} );

// Table initialisation
$(document).ready(function() {
	$('#dataTable').dataTable( {
		

		"sDom": "<'row'<'span6'l><'span6'f>r>t<'row'<'span6'i><'span6'p>> ",
		
		"sPaginationType": "bootstrap",
		"oLanguage": {
			"sLengthMenu": ""
//			"sLengthMenu": "_MENU_ records per page"
		}
	} );
} );

////////////// GoogleControl Functions ///////////////////////////////



////////////// CometToolTipBuilder Functions ///////////////////////////////

//internal only
function CometToolTipBuilderCaveatColor(cav) {
	if(cav=="TOP BANANA" || cav=="TOP BANANA//SCI" || cav=="TOP SECRET" || cav=="TOP SECRET//SCI") {
		return "#AAAA00";
	}
	if(cav=="BANANA" || cav=="BANANA RELEASABLE" || cav=="SECRET" || cav=="SECRET RELEASABLE"  || cav=="SECRET NO FORN") {
		return "#FF0000";
	}
	//unclassified
	return "#00AA00";
}

function CometToolTipBuilderCaveatColorForeground(bgcolor) {
	if(bgcolor=="#AAAA00") return "#000000";
	return "#FFFFFF";
}

function CometToolTipBuilderGetThumbnail(tag) {
	return global_properties.webapp+"Relay?path="+placemarkList[tag].mdm.urlName+"&type=thumbnail&stream";
}

//internal only
function CometToolTipBuilderUrl2Path(_url) {
	CometControlsLog("----->WARNING: may not want to be using this function CometToolTipBuilderUrl2Path <-----");
	if(!_url.contains("http")) return _url;


	var splits=_url.split('/');
	var newurl="";
	for(var i=4; i<splits.length; i++) {
		newurl=newurl+"/"+splits[i];
	}
	return newurl;
}

function CometToolTipBuilderMetadataMapToPlacemarkObject(mdm) {
	CometControlsLog("*********** BEGIN CometToolTipBuilderMetadataMapToPlacemarkObject("+mdm.tag+","+mdm.utf8name+") ******************");
	placemarkList[mdm.tag]=mdm;
	
	//hack to get around naming conventions
	placemarkList[mdm.tag].mdm=mdm;
	
	placemarkList[mdm.tag].flownin=true;

	var temp_contents="";
	
	
	if(Number(mdm.annotation_size)!=0) {
		CometControlsLog("setting tooltip_contents to iframe");
		placemarkList[mdm.tag].tooltip_contents='<iframe width=320 height=420 src="'+global_properties.webapp+'Relay?path='+placemarkList[mdm.tag].mdm.urlName+'&type=generated&annotation=tooltips&stream&astext&size=300x400&skipaction&tag='+(Number(mdm.tag)+1)+'" ></iframe>';
	
		CometControlsLog("tooltip url = "+placemarkList[mdm.tag].tooltip_contents);
	}
	placemarkList[mdm.tag].active=true;
	CometControlsLog("*********** END CometToolTipBuilderMetadataMapToPlacemarkObject("+mdm.tag+","+mdm.utf8name+") ******************");
}

function CometToolTipBuilderGenerateToolTip(tag) {
	return placemarkList[tag].tooltip_contents;
}

function CometToolTipBuilderReloadSingleMetadata(tag) {
	CometControlsLog("CometToolTipBuilderReloadSingleMetadata("+tag+")::: BEGIN");
	//create request
	var request=getRequestObject();
	CometStartAJAXAnimation();

	//catch request and do something with it
	request.onreadystatechange = function() {	
		if ((request.readyState == 4) && (request.status == 200)) {
		
			CometControlsLog(request.responseText);
			var mdm=JSON.parse(request.responseText);
			
	
			CometToolTipBuilderMetadataMapToPlacemarkObject(mdm);
			
			if(mdm.tag) {
				CometControlsLog("------>>>>tooltip is now "+placemarkList[mdm.tag].tooltip_contents);
				
				placemarkList[mdm.tag].loading=false;
			}
			CometControlsReloadVidCat(tag);
			cTabFliptoPage( "RHS", page_number_vidcat );
	
			CometStopAJAXAnimation();
		}
	};
	
	placemarkList[tag].loading=true;
	//send request to serverlet
	request.open("GET", 'Relay?path='+placemarkList[tag].mdm.urlName+'&type=json&tag='+tag, true);
	CometControlsLog("CometToolTipBuilderReloadSingleMetadata("+tag+")::: END");
	request.send(null);
}

function CometGetIconPath(tag) {
	return global_properties.imageprefix+"images/icons/"+placemarkList[tag].mdm.fileTypeIcon+".png";
}


////////////// CometSearchResult Functions ///////////////////////////////
function CometSearchResultsColumn1Gen(tag) {
	if(placemarkList[tag].mdm.fileType=="directory") return '';
	return '<input type="checkbox" name="selection" id="download_cb_'+tag+'" onclick="CometControlsFileDownloadArray(\''+tag+'\')"></input>'

}

//click on icon - reveals action bar somehow

function CometSearchResultsBalloonContent(tag) {
//	CometControlsLog("CSRBC::: annotation size as a number: "+Number(placemarkList[tag].mdm.annotation_size));
//	if(Number(placemarkList[tag].mdm.annotation_size)==0) return '<span data-notifications="'+(+tag+1)+'"><center><img style="cursor:pointer;" src="'+CometGetIconPath(tag)+'" width=45 /><center></span>';
	return '<span data-notifications="'+(+tag+1)+'"><center><img onmouseover="CometControlsOnBalloonOver(\''+tag+'\')" onmouseout="CometControlsOnObjectMouseOut(\''+tag+'\')"'+
'  style="cursor:pointer;" src="'+CometGetIconPath(tag)+'" width=45 /><center></span>';

}

function CometControlsOnBalloonOver(tag) {
	CometControlsChangeIcon(tag, CometProperties.getInstance().getWebAppPath()+"Relay?path="+placemarkList[tag].mdm.urlName+"&type=icon&stream&size=32x32",2.0);
	
	if(Number(placemarkList[tag].mdm.annotation_size)!=0) CometControlsTip(CometToolTipBuilderGenerateToolTip(tag));
	else CometControlsTip("Object has no metadata");

}

function CometSearchResultsColumn2Gen_ORIG(tag) {
	return '<span data-notifications="'+(+tag+1)+'"><img '+
	' onmouseover="CometControlsOnObjectMouseOver(\''+tag+'\')"'+
	' onclick="CometControlsOnObjectMouseDown(\''+tag+'\')"'+
	' onmouseout="CometControlsOnObjectMouseOut(\''+tag+'\')"'+
	'  style="cursor:pointer;" src="'+CometGetIconPath(tag)+'" width=45 /></span>';
}

function CometSearchResultsColumn2Gen(tag) {
	
	var content="<TABLE>";
	content+="<TR><TD width=10%>"+CometSearchResultsColumn2Gen_ORIG(tag)+"</TD>";
	
	if(placemarkList[tag].geotagged) cols=9;
	else cols=4;
	
	content+="<TD class=metadata>"+CometControlsDisplayPath(placemarkList[tag].mdm.utf8name,15)+"</TD></TR>";
	content+="<TR><TD colspan=2 ><TABLE border=0 style=\"display:none\" id=\"hidden_row_"+tag+"\">";
	content+="<TR >";
	
	//balloon
	if(placemarkList[tag].geotagged && global_properties.geo_enabled) content+="<TD>"+CometSearchResultsColumn3Gen(tag)+"</td>";
	
	//fly in
//	if(placemarkList[tag].geotagged && global_properties.geo_enabled) content+="<TD>"+CometSearchResultsColumn4Gen(tag)+"</td>";
	
	//fly out
	if(placemarkList[tag].geotagged && global_properties.geo_enabled) content+="<TD>"+CometSearchResultsColumn5Gen(tag)+"</td>";
	
	//action, edit and view
	if(CometHasContent(placemarkList[tag].mdm.link_action) || CometHasContent(placemarkList[tag].mdm.actionFunc)) {
		CometControlsLog("link_action="+placemarkList[tag].mdm.link_action);
		CometControlsLog("actionFunc="+placemarkList[tag].mdm.link_label);
		
		
		content+="<TD>"+CometSearchResultsColumn6Gen(tag)+"</td>";
	} else content+="<TD></TD>";
	content+="<TD>"+CometSearchResultsColumn7Gen(tag)+"</td>";
	content+="<TD>"+CometSearchResultsColumn8Gen(tag)+"</td>";


	//load KML_path into google earth
	if(placemarkList[tag].geotagged && global_properties.geo_enabled) {
		if(global_properties.allow_kml_load) content+="<TD>"+CometSearchResultsColumn9Gen(tag)+"</td>";
		else content+="<TD></TD>";
	}

	//download KML_path
//	if(placemarkList[tag].geotagged) content+="<TD>"+CometSearchResultsColumn10Gen(tag)+"</td>";

	if(placemarkList[tag].geotagged && global_properties.geo_enabled) {
		if(global_properties.allow_path_edit) content+="<TD>"+CometSearchResultsColumn11Gen(tag)+"</td>";
		else content+="<TD></TD>";
	}
	
	if(global_properties.geo_enabled) {
		if(global_properties.allow_geo_edit) content+="<TD>"+CometSearchResultsColumn12Gen(tag)+"</td>";
		else content+="<TD></TD>";
	}
	
	if(global_properties.allow_delete) {
		content+="<TD>"+CometSearchResultsColumn13Gen(tag)+"</td>";
	} else {
		content+="<TD></TD>";
	}
	if(global_properties.allow_notes && placemarkList[tag].mdm.note_urlName) {
		content+="<TD>"+CometSearchResultsColumn14Gen(tag)+"</td>";
	} else {
		content+="<TD></TD>";
	}
	content+="</TR></TABLE></td></tr></TABLE>";
	return content;
}




//show geospatial balloon
function CometSearchResultsColumn3Gen(tag) {

	if(placemarkList[tag].geotagged) return '<span><center><img onmouseover="CometControlsTip(\'Click to show/hide geospatial balloon\')"  onmouseout="CometControlsUnTip()" onmousedown="CometGeo_ToggleBalloon(\''+tag+'\')" src="images/icons/geoballoon-icon.png" width=32 height=32></center></span>';
	
	return '<span><center><img onmouseover="CometControlsTip(\'Not geotagged\')"  onmouseout="CometControlsUnTip()" src="images/icons/geoballoon-icon.png" width=32 height=32></center></span>';
}

//fly in

function CometSearchResultsColumn5Gen(tag) {
	if(placemarkList[tag].geotagged) return '<span><center><img onmouseover="CometControlsTip(\'Click to fly to\')"  onmouseout="CometControlsUnTip()" onmousedown="CometGeo_Fly('+tag+',true)" src="images/icons/flyin-icon.png" width=32 height=32></center></span>';
	return '<span><center><img onmouseover="CometControlsTip(\'Not geotagged\')"  onmouseout="CometControlsUnTip()" src="images/icons/flyin-icon.png" width=32 height=32></center></span>';
}

//action button
function CometSearchResultsColumn6Gen(tag) {
	return '<span id=action_tag_'+tag+'><center><img onmouseover="CometControlsTip(\''+placemarkList[tag].mdm.link_label+'\')" onmouseout="CometControlsUnTip()" onmousedown="'+placemarkList[tag].mdm.link_action+'" src="images/icons/play-icon.png" width=32 height=32></center></span>';
}

//edit metadata
function CometSearchResultsColumn7Gen(tag) {
	return '<span><center><img onmouseover="CometControlsTip(\'Click to edit metadata\')" onmouseout="CometControlsUnTip()" onmousedown="CometControlsEditMetadata(\''+tag+'\')" src="images/icons/edit-icon.png" width=32 height=32></center></span>';
}

//view in vidcat
function CometSearchResultsColumn8Gen(tag) {
	return '<span><center><img onmouseover="CometControlsTip(\'Click to view object\')" onmouseout="CometControlsUnTip()" onmousedown="CometControlsViewInVidCat(\''+tag+'\')" src="images/icons/view-icon.png" width=32 height=32></center></span>';
}

function CometSearchResultsColumn9Gen(tag) {
	if(placemarkList[tag].geotagged) return '<span><center><img onmouseover="CometControlsTip(\'Click to load KML Path\')"  onmouseout="CometControlsUnTip()" onmousedown="CometGeo_LoadKML('+tag+')" src="images/icons/path-icon.png" width=32 height=32></span></center>';
	return '<span><center><img onmouseover="CometControlsTip(\'Not geotagged or no kml path found\')"  onmouseout="CometControlsUnTip()" src="images/icons/path-icon.png" width=32 height=32></center></span>';

}

function CometSearchResultsColumn11Gen(tag) {
	return '<span><center><img onmouseover="CometControlsTip(\'Click to edit KML Path\')"  onmouseout="CometControlsUnTip()" onmousedown="CometControlsPathEdit('+tag+')" src="images/icons/build-icon.png" width=32 height=32></center></span>';
	
}

function CometSearchResultsColumn12Gen(tag) {
	return '<span><center><img onmouseover="CometControlsTip(\'Click to edit GEO Tag\')"  onmouseout="CometControlsUnTip()" onmousedown="CometControlsGeoEdit('+tag+')" src="images/icons/geotag-icon.png" width=32 height=32></center></span>';
}

function CometSearchResultsColumn13Gen(tag) {
	return '<span><center><img onmouseover="CometControlsTip(\'Click to Delete Object\')" onmouseout="CometControlsUnTip()" onmousedown="CometControlsDeleteObject('+tag+')" src="images/icons/trash-icon.png" width=32 height=32></center></span>';
}

function CometSearchResultsColumn14Gen(tag) {
	return '<span><center><img onmouseover="CometControlsTip(\'Click to View Note\')" onmouseout="CometControlsUnTip()" onmousedown="CometControlsViewNoteObject('+tag+')" src="images/icons/notes-icon.png" width=32 height=32></center></span>';
}

function CometSearchResultsAddRow_inner(mdm) {
	CometControlsLog("begin of addrow_inner("+mdm.tag+")");
	CometToolTipBuilderMetadataMapToPlacemarkObject(mdm);

	CometControlsLog("creating placemark for tag: "+mdm.tag+" and show marker");
	CometGeo_CreatePlacemark(mdm.tag);
//	GoogleControlsShowMarker(mdm.tag);
		//try for 3 columns
$('#dataTable').dataTable().fnAddData([
CometSearchResultsColumn1Gen(mdm.tag),
placemarkList[mdm.tag].mdm.desc,
CometSearchResultsColumn2Gen(mdm.tag)]);
	CometControlsLog("end of addrow_inner("+mdm.tag+")");
}
//INTERNAL

function CometSearchResultsAddRows(resultRegion) {
	CometControlsLog("======CometSearchResultsAddRows(responseText, "+resultRegion+")============");
	CometStartAJAXAnimation();
//	if ((request.readyState == 4) && (request.status == 200)) {
		CometControlsLog("CometSearchResultsAddRows (ready) (request, "+resultRegion+") - request ready");
		//probably needs validation
		//var new_search_results=global_properties.cached_search_results; 

		
		CometControlsLog("==========================");
		CometControlsLog(" BEGIN SEARCH RESULTS ");
		CometControlsLog("==========================");
		//for ( var i = 0; i< new_search_results.length; i++) {
		//	CometControlsLog("\tpath="+new_search_results[i].path);
		for ( var i = 0; i< placemarkList.length; i++) {
			CometControlsLog("\tpath="+placemarkList[i].path);
		}
		CometControlsLog("==========================");
		CometControlsLog("search results length="+placemarkList.length);
		//var splitstring = request.responseText.split("~");
		var k=0;
		var listing=new Array();
		var searchstring=document.getElementById("search").value
		//viewable_objects=MIN(splitstring.length,num_of_tags)-1;
		viewable_objects=MIN(placemarkList.length,num_of_tags);
		for ( var i = 0; i< placemarkList.length; i++) {
			CometControlsLog("begin loop for i="+i+" of "+placemarkList.length);
			var mdm=placemarkList[i];

			if(mdm) {

// 				if(mdm.content_utf8name) {
// 					mdm.utf8name=mdm.content_utf8name
// 				}	
// 
// 				if(mdm.content_urlName) {
// 					mdm.urlName=mdm.content_urlName
// 				}	



			CometControlsLog("looking at path="+mdm.utf8name);
			if((!mdm.utf8name.contains("datasource_") && !mdm.utf8name.contains("thumb.png") && !mdm.utf8name.contains("thumb.jpg")) && (searchstring.length==0 || (searchstring!="" && mdm.utf8name.contains(searchstring)))) {
				CometControlsLog("\tadding ============>"+mdm.utf8name);
				mdm.tag=k;
				CometSearchResultsAddRow_inner(mdm);
				k=k+1;
			} else {
				
				CometControlsLog("rejected result: "+mdm.utf8name);
				CometControlsLog("\tbut why? ");
				CometControlsLog("\tsearchstringlength="+searchstring.length);
				CometControlsLog("\tsearchstring="+searchstring);
			}
			
			CometControlsLog("\trow successfully added: "+k+" working on i="+i);
			}
		}
		CometStopAJAXAnimation();
//	}
}

function CometPathEdit_EnableListeners() {
	if(CometGeo_Disabled()) return;
	
	CometGeo_enableDraggableListeners();
	CometGeo_addEventListener('click',CometControlsPathEdit_onMouseClick);
	path_editor_listener_active=true;
}

function CometPathEdit_DisableListeners() {
	if(CometGeo_Disabled()) return;

	path_editor_listener_active=false;

	CometGeo_disableDraggableListeners();
	CometGeo_removeEventListener('click', CometControlsPathEdit_onMouseClick);
				
}


////////////// cTab Functions ///////////////////////////////

//called as a result of searching
//ctabpageflip
//flippage
//tabflip
function cTabFliptoPage( prefix, n ){
	if(n==-1 ) return;
	var container = document.getElementById(prefix+"_tabContainer");
	var pages = container.querySelectorAll(".tabpage");
	var navitem = container.querySelector(".tabs ul li");
	var current = navitem.parentNode.getAttribute("data-current");
	
	if(prefix=="LHS") {
		CometControlsLog("switching to something on LHS");		
		if(n==3) {
			if(global_properties.allow_geo_edit) CometControlsGeoEdit_CreateScreenOverlay();
			CometControlsLog("switching to path editor on LHS");
			if(path_editor_listener_active) {
				CometControlsLog("but path listener already active");
			} else {
				CometControlsLog("path listener is not active.. activating listener");
				CometPathEdit_EnableListeners();
				
			}
		} else {
			if(global_properties.allow_geo_edit) CometControlsGeoEdit_RemoveScreenOverlay();
			CometControlsLog("switching to a different tab");
			if(path_editor_listener_active) {
				CometPathEdit_DisableListeners();
				
			} else {
				CometControlsLog("path listener was not active, nothing to do");
			}
		}			
	} else if(prefix=="RHS") {
		//if current is ingest and shouldtrack, stop tracking
		
		if(n==page_number_ingest && shouldTrackIngest) CometIngestor_StartLoop(); // CometControlsStartIngestorLoop();
		else CometIngestor_StopLoop(); // CometControlsStopIngestorLoop();
		
	} else if(prefix=="INGEST") {
		
		//if n is ingest and shouldtrack, start tracking
		if(n==2 && shouldTrackIngest) CometIngestor_StartLoop(); // CometControlsStartIngestorLoop();
		else CometIngestor_StopLoop(); //CometControlsStopIngestorLoop();
		
	
	
	} else if(prefix=="ADMIN") {
		if(n==3 && document.getElementById("configuration_file").innerHTML=="") CometControlsLoadConfigurationFile();
		if(n==4 && document.getElementById("roles_file").innerHTML=="") CometControlsLoadRolesFile();
		CometControlsLog("on admin tab");
	} else if(prefix=="LOGS") {
	
		CometRefreshLogs();
		
	
	} else {
		CometControlsLog("switching to something on other tab set");
	}


	document.getElementById(prefix+"_tabHeader_" + current).removeAttribute("class");
	document.getElementById(prefix+"_tabpage_" + current).style.display="none";
	//CometControlsLog("want to enable page "+n);
	//add class of activetabheader to new active tab and show contents
	document.getElementById(prefix+"_tabHeader_"+n).setAttribute("class","tabActiveHeader");
	document.getElementById(prefix+"_tabpage_" + n).style.display="block";
	navitem.parentNode.setAttribute("data-current",n);
	
//	if(n==0 && prefix=="RHS") {
//		document.getElementById("map3d").style.display="block";
//	}
	
	CometControlsLog("***tab page "+prefix+"_tabpage_" + current+" = "+document.getElementById(prefix+"_tabpage_" + current).style.display);
	
	
	
}

//assigned to each tab on init
function cTabDisplayPageOnClick() {
	var prefix=this.id.split("_")[0];
	var ident=this.id.split("_")[2];
	CometControlsLog("tab clicked, switch to cTabFliptoPage("+prefix+","+ident+")");
	cTabFliptoPage(prefix, ident);
}

function cTabGetCurrentPage(prefix) {
	var container = document.getElementById(prefix+"_tabContainer");
	var pages = container.querySelectorAll(".tabpage");
	var navitem = container.querySelector(".tabs ul li");
	return navitem.parentNode.getAttribute("data-current");
}
//formerly: function CometControlsLoadThisTab(html_page,tgt) {
function cTabLoad(html_page,tgt) {
	CometControlsLog("LoadThisTab("+html_page+","+tgt+")");
	var request=CometControlsHttpGet(html_page);
	request.tgt=tgt;
	request.html_page=html_page;
	return request;
}

//formerly: function CometControlsLoadTabWhenReady(request) {
function cTabLoadReady(request) {
	request.onreadystatechange = function() {
		if ((request.readyState == 4) && (request.status == 200)) {
			CometControlsLog("loading "+request.html_page+" content to object "+request.tgt);
			document.getElementById(request.tgt).innerHTML=request.responseText;
			CometStopAJAXAnimation();
		}
	}
}
//formerly: CometControlsLoadTabs()
function cTabLoadAll() {
	CometControlsLog("------> GET HERE <-----");
	//load all the tabs
	
//	tabset.rhs0_bannerreq=cTabLoad("http://"+cometdev.domain.com"+CometProperties.getInstance().getWebAppPath()+"Relay?path=/opt/COMETDist/banner.html&type=editfile&stream&astext","banner_page");
	tabset.rhs0_bannerreq=cTabLoad(document.location.origin+global_properties.webapp+"Relay?path=/opt/COMETDist/banner.html&type=editfile&stream&astext","banner_page");
	cTabLoadReady(tabset.rhs0_bannerreq);
	
//	tabset.rhs1_geovizreq=cTabLoad("index_geoviz.html","geoviz_page");
//	cTabLoadReady(tabset.rhs1_geovizreq);
	tabset.rhs2_vidcatreq=cTabLoad("index_vidcat.html","vidcat_page");
	cTabLoadReady(tabset.rhs2_vidcatreq);
//	tabset.rhs3_wcreq=cTabLoad("index_wordcloud.html","wordcloud_page");
	//cTabLoadReady(tabset.rhs3_wcreq);
	tabset.rhs4_mdereq=cTabLoad("index_metadataeditor.html","metadataeditor_page");
	cTabLoadReady(tabset.rhs4_mdereq);
	tabset.rhs5_notesreq=cTabLoad("index_notes.html","notes_page");
	cTabLoadReady(tabset.rhs5_notesreq);

	
}

////////////// CometControls Functions ///////////////////////////////


//complete rehash of query properties
function CometControlsQueryProperties() {
	setTimeout(function() { CometControls_CheckLoadables(); }, 5000);

	cTabLoadAll();
	global_properties.verbose=true;	
	if(testing) {
		CometControlsLog("!!! WARNING: running in testing mode !!!");
	}
	var request=getRequestObject();
	//var isiPad = navigator.userAgent.match(/iPad/i) != null;
	var isMac = navigator.platform.toUpperCase().indexOf('MAC')>=0;
	var isWindows =  navigator.platform.toUpperCase().indexOf('WIN')>=0;
	
	//var isChrome = navigator.userAgent.toUpperCase().indexOf('CHROME')>=0;
	
	
	//console.log("**************isChrome="+isChrome);
	
	//var isChromeGoodVersion = (navigator.userAgent.toUpperCase().indexOf('CHROME/38')>=0);
	//console.log("**************is good chrome="+isChromeGoodVersion);
	
	var isInternetExploder = isWindows && (navigator.userAgent.toUpperCase().indexOf('RV:11.0')>0);
	
	
	request.force_geodriver_disabled=false;
	
	
	
	if((!isMac && !isWindows) || isInternetExploder) { 
	//(isChrome && !isChromeGoodVersion) ||isInternetExploder) { 
		request.force_geodriver_disabled=true;
	}
	
//	if(isChrome && isChromeBadVersion && global_properties.geo_enabled) {
//		console.log("WARNING: Google Earth Plugin has been deprecated on this browser");
//		 alert('WARNING: Google Earth plugin has been deprecated on this browser');
//	}
	console.log("**************platform="+navigator.appVersion);
	
	
	
	// ask java-land for the content of property
	
	request.onreadystatechange = function() {
		//only care if there is a response
		if ((request.readyState == 4) && (request.status == 200) && (request.responseText!="") && (!properties_loaded)) {
			global_properties=JSON.parse(request.responseText);
			
			document.getElementById("installed-comet-version").innerHTML=global_properties.comet_version;

			global_properties.imageprefix=document.location.origin+global_properties.webapp;
		
			num_of_tags=Number(global_properties.maxsearchresults)+1;


			root_ingest_dir=global_properties.ingestor_source+'/';
			root_ingest_dir_orig=global_properties.ingestor_source+'/';
			//load google earth
//			if(CometControlsQueryPropertiesLoadGE()) {
			if(request.force_geodriver_disabled) {
				global_properties.geodriver="disabled";
				global_properties.geo_enabled=false;
			}


			if(!CometGeo_Init(global_properties.geodriver)) {
				CometControlsLog("geo driver disabled for one reason or another");
//				document.getElementById("RHS_tabpage_0").style.display="block";
				document.getElementById('installed-plugin-version').innerHTML = "N/A"
//				GoogleControlsInitPlacemarkList();
				if(!testing) {
					document.getElementById("coordinate_drop").style.display="none";
//					document.getElementById("google_cb_row1").style.display="none";
//					document.getElementById("google_cb_row2").style.display="none";
//					document.getElementById("google_cb_row3").style.display="none";
//					document.getElementById("google_cb_row4").style.display="none";
//					document.getElementById("google_cb_row5").style.display="none";
					document.getElementById("google_plugin").style.display="none";
					document.getElementById("RHS_tabHeader_1").style.display="none";
					cTabFliptoPage("RHS",2);
				}
				//geo is disabled, but we still have to release the loadable
				CometGeo_LoadComplete();
			}
		
			// Get the DataTables object again - this is not a recreation, just a get of the object 
			var oTable = $('#dataTable').dataTable();
			
			if(!testing) oTable.fnSetColumnVis(1, false);
		
			if(global_properties.ingestor_enabled) {
			
//				if(global_properties.allow_exp) {
			
					CometIngestor_Init();
// 				} else {
// 
// 				tabset.rhs6_ingestreq=cTabLoad("index_ingestor.html","ingestor_page");
// 				tabset.rhs6_ingestreq.onreadystatechange = function() {
// 					if ((tabset.rhs6_ingestreq.readyState == 4) && (tabset.rhs6_ingestreq.status == 200)) {
// 						CometControlsLog("loading "+tabset.rhs6_ingestreq.html_page+" content to object "+tabset.rhs6_ingestreq.tgt);
// 						document.getElementById(tabset.rhs6_ingestreq.tgt).innerHTML=tabset.rhs6_ingestreq.responseText;
// 						CometStopAJAXAnimation();
// 						
// 						InitTabs("INGEST");
// 					
// 						//upload not supported when using multiple sources
// 						if(global_properties.ingestor_multisource) {
// 							CometControlsLog("====>ingestor is multisource, hide upload tab");
// 							document.getElementById("INGEST_tabHeader_1").style.display="none";
// 							cTabFliptoPage( "INGEST", 2 );
// 						} else {
// 							CometControlsLog("====>ingestor is single source, allow upload tab");
// 							cTabFliptoPage( "INGEST", 1 );
// 							CometControlsInitializeUploader();
// 							CometControlsCHDir(root_ingest_dir_orig);
// 						}
// 	
// 				
// 						//load ingestor
// 						if(global_properties.ingestor_continuous) {
// 							CometControlsLog("Ingestor is in continuous mode");
// 							if(!testing) {
// 								
// 								setTimeout(function() { CometIngestor_StartLoop(); CometIngestor_StopLoop();}, 5000);
// 							}
// 						} else {
// 							if(!testing) document.getElementById("ingest_button_span").innerHTML='<button id="ingest_button" onmousedown="CometIngestor_StartLoop()" value="startcountdown">Begin Ingest</button>'
// 						}
// 						
// 						document.getElementById("migration_src").innerHTML=global_properties.migration_src;
// 						document.getElementById("migration_dest").innerHTML=global_properties.migration_dest;
// 						
// 						document.getElementById("RHS_tabHeader_"+page_number_ingest).style.display="none";
// 
// 					}
// 				}
//				}
			} 
			
			
			if(global_properties.extensions_enabled) {
			
				if(global_properties.allow_exp) {
			
				CometControlsLog("loading extensions");
				CometExt_Init();
				}
			} else {
				CometControlsLog("not loading extensions");
				loadables.ext=true;
			}
			
			if(!testing) CometControlsExternalConfigButtons(global_properties.destination_rootPath);
		
		
				
			if(!document.getElementById("RHS_tabHeader_"+page_number_wordcloud)) {
				CometControlsLog("word cloud is disabled");
			} else {				
				
				CometControlsLog("want to load wordcloud...");
				CometLoading("Initializing Word Cloud UI...");
				tabset.rhs3_wcreq=cTabLoad("index_wordcloud.html","wordcloud_page");

//				CometControlsLog("readyState="+tabset.rhs3_wcreq.readyState);
//				CometControlsLog("readyState="+tabset.rhs3_wcreq.status);
				
				tabset.rhs3_wcreq.onreadystatechange = function() {
					CometControlsLog("wc state change!");
				
					if ((tabset.rhs3_wcreq.readyState == 4) && (tabset.rhs3_wcreq.status == 200)) {
						document.getElementById(tabset.rhs3_wcreq.tgt).innerHTML=tabset.rhs3_wcreq.responseText;
						CometStopAJAXAnimation();
						
						//searchme4
						
						CometControlsLoadWordCloudByPathAutoLoad(); 
						 if(!$('#myCanvas').tagcanvas({
        						  textColour: null,
							   textFont: null,
						          outlineColour: '#ff0000',
							          reverse: true,
						          depth: 0.8,
						          maxSpeed: 0.01,
								  weight: true
						        },'tags')) {
							
								CometControlsLog("wc broken!?!?!?");
							
					          // something went wrong, hide the canvas container
						          $('#myCanvasContainer').hide();
						        }
						// });
						
						//document.getElementById("RHS_tabHeader_"+page_number_wordcloud).style.display="none";
						
						//document.getElementById("RHS_tabpage_"+page_number_wordcloud).style.display="none";
						wctablebuilt=true;
						
						if(login_complete) document.getElementById("RHS_tabHeader_"+page_number_wordcloud).style.display="block";
						CometLoading("Word Cloud UI Complete");
					}
				}
			}	 
			
			CometControlsLog("**********done with properties file ****************");
			properties_loaded=true;
			
			if(document.getElementById("hide_iframe")) document.getElementById("hide_iframe").style.display="none";
			if(document.getElementById("iframe")) document.getElementById("iframe").style.display="none";
			$('audio,video').mediaelementplayer({audioWidth: 300});
			
			CometControlsSetCaveats();

			
			if(!testing) document.getElementById("RHS_tabHeader_6").style.display="none";

			
			CometControlsCheckHCP();
			CometControlsShowLoginPane();
			LockSearch();
			
			CometAdmin_Init();
			
			if(global_properties.autologin_enabled) {
				setTimeout(function() { 
					CometControlsLog("----->autologin enabled");
					CometControlsLog("----->logging in as user "+global_properties.autologin_user);
					CometControlsLog("----->logging in as role "+global_properties.autologin_role);
			
					//do log in type stuff	
			
			
					var login_props=new Object();
					login_props.authorized=true;
					login_props.username=global_properties.autologin_user;
					login_props.role=global_properties.autologin_role;	
	
					//if(login_props.role=="admin") login_props.admin_user=true;	
					login_props.admin_user=false;			
					CometControlsLoginBTN_push(login_props.username, '', login_props.role);

					//CometControlsLoginBTN_handler(login_props);
				
				 },2000);
			} else if(global_properties.session_auth) {
				setTimeout(function() { 
					CometControlsLog("----->session-based autologin enabled");
					CometControlsLog("----->logging in as user "+global_properties.session_user);
					CometControlsLog("----->logging in as role "+global_properties.session_role);
			
					//do log in type stuff	
			
			
					var login_props=new Object();
					login_props.authorized=true;
					login_props.username=global_properties.session_user;
					login_props.role=global_properties.session_role;	
	
					//if(login_props.role=="admin") login_props.admin_user=true;	
					login_props.admin_user=false;			
					CometControlsLoginBTN_push(login_props.username, '', login_props.role);

					//CometControlsLoginBTN_handler(login_props);
				
				 },2000);
			
			
			
			} else {
				if(!global_properties.geo_enabled) {
				
					CometControlsLog("switch to tab 0 and hide the tab");
				
					cTabFliptoPage( "RHS", 0 );	
					document.getElementById("RHS_tabHeader_0").style.display="none";
				} else {
					CometControlsLog("not switching to tab 0 and hiding the tab");
				}
				
				
			
			}
		
			setTimeout(function() { 
				//cheat until we can fix ajax timing bug
				busyAnimRef=1;
				CometStopAJAXAnimation();
			
			},5000);
			
			
			if(global_properties.extensions_enabled) {
				setTimeout(function() { CometExt_LoadComplete(); },10000);
			}
			
			CometControlsLog("---at end of properties load---");
				
		} //request is valid
	} //anon function: ready state change
	
	var params="";
	
	if(document.getElementById("loggedin_user") && document.getElementById("loggedin_user").innerHTML!="") {
		params="?userName="+document.getElementById("loggedin_user").innerHTML;
		if(document.getElementById("loggedin_role").innerHTML!="")
		 params+="&userRole="+document.getElementById("loggedin_role").innerHTML;
	}
	request.open("GET", "get-properties"+params, true);
	
 	//request.send(params);
	request.send(null);
}



// using this when file list has radio button to allow only 
// download of single file
//internal only
function CometControlsFileDownloadArray(tag) {
	var file=placemarkList[tag].mdm.urlName;
	var found = false;
	for(var i=0; i<filesToDownload.length; i++) {
		if(file == filesToDownload[i].toString()) {
		
			CometControlsLog("--->skipping file: "+file+" for some reason");
			filesToDownload.splice(i,i+1);
			found = true;
		}
	}
	if(!found) {
		
		CometControlsLog("--->pushed file onto stack:"+file);
		filesToDownload.push(file);
	}
}

//This is the validation function:
function CometControlsValidateLogon() {
    // Start validation:
    $.validity.start();
    $.validity.setup({ outputMode:"modal" });
    $(document.getElementById('userName')).require();
    // End the validation session:
    var result = $.validity.end();
    // Return whether it's okay to proceed with the Ajax:
    return result.valid;
}

function CometControls_ViewChangeCB() {

	CometControlsLog("get here in viewchangeCB");


	document.getElementById('latitude').value=coordinates.latitude;
	document.getElementById('longitude').value=coordinates.longitude;

	if(global_properties.allow_geo_edit) {
		document.getElementById('geo_latitude').value=coordinates.latitude;
		document.getElementById('geo_longitude').value=coordinates.longitude;;
	}	


       	document.getElementById('nwlat').value=coordinates.nw_latitude;
       	document.getElementById('nwlong').value=coordinates.nw_longitude;
	
	document.getElementById('nelat').value=coordinates.ne_latitude;
       	document.getElementById('nelong').value=coordinates.ne_latitude;

       	document.getElementById('swlat').value=coordinates.sw_latitude;
       	document.getElementById('swlong').value=coordinates.sw_longitude;
	
	document.getElementById('selat').value=coordinates.se_latitude;
       	document.getElementById('selong').value=coordinates.se_latitude;

	if(global_properties.allow_geo_edit) {
	       	document.getElementById('geo_nwlat').value=coordinates.nw_latitude;
	       	document.getElementById('geo_nwlong').value=coordinates.nw_longitude;
	
		document.getElementById('geo_nelat').value=coordinates.ne_latitude;
	       	document.getElementById('geo_nelong').value=coordinates.ne_latitude;

	       	document.getElementById('geo_swlat').value=coordinates.sw_latitude;
	       	document.getElementById('geo_swlong').value=coordinates.sw_longitude;
		
		document.getElementById('geo_selat').value=coordinates.se_latitude;
	       	document.getElementById('geo_selong').value=coordinates.se_latitude;
	}

}

function CometControlsCoordinatesCB() {
	var lookAt = ge.getView().copyAsLookAt(ge.ALTITUDE_RELATIVE_TO_GROUND);
	
	// Set new latitude and longitude values.
	//CometControlsLog("center: "+lookAt.getLatitude()+","+lookAt.getLongitude());
	
	document.getElementById('latitude').value=lookAt.getLatitude().toFixed(6);
	document.getElementById('longitude').value=lookAt.getLongitude().toFixed(6);

	if(global_properties.allow_geo_edit) {
		document.getElementById('geo_latitude').value=lookAt.getLatitude().toFixed(6);
		document.getElementById('geo_longitude').value=lookAt.getLongitude().toFixed(6);
	}	
//	document.getElementById('clat_formxml').value=lookAt.getLatitude().toFixed(6);
//	document.getElementById('clong_formxml').value=lookAt.getLongitude().toFixed(6);
	
	

	var hitTestTL = ge.getView().hitTest(0, ge.UNITS_FRACTION, 0, ge.UNITS_FRACTION, ge.HIT_TEST_GLOBE);
	var hitTestTR = ge.getView().hitTest(1, ge.UNITS_FRACTION, 0, ge.UNITS_FRACTION, ge.HIT_TEST_GLOBE);
	var hitTestBR = ge.getView().hitTest(1, ge.UNITS_FRACTION, 1, ge.UNITS_FRACTION, ge.HIT_TEST_GLOBE);
	var hitTestBL = ge.getView().hitTest(0, ge.UNITS_FRACTION, 1, ge.UNITS_FRACTION, ge.HIT_TEST_GLOBE);
    
    	if(hitTestTL) {
		document.getElementById('nwlat').value=hitTestTL.getLatitude().toFixed(6);
		document.getElementById('nwlong').value=hitTestTL.getLongitude().toFixed(6);

		//shared
		document.getElementById('nelat').value=hitTestTL.getLatitude().toFixed(6);
		document.getElementById('swlong').value=hitTestTL.getLongitude().toFixed(6);


		if(global_properties.allow_geo_edit) {
			document.getElementById('geo_nwlat').value=hitTestTL.getLatitude().toFixed(6);
			document.getElementById('geo_nwlong').value=hitTestTL.getLongitude().toFixed(6);

		//shared
			document.getElementById('geo_nelat').value=hitTestTL.getLatitude().toFixed(6);
			document.getElementById('geo_swlong').value=hitTestTL.getLongitude().toFixed(6);
		}


	} else {
        	document.getElementById('nwlat').value=0;
        	document.getElementById('nwlong').value=0;
        	document.getElementById('nelat').value=0;
        	document.getElementById('swlong').value=0;

		if(global_properties.allow_geo_edit) {
        		document.getElementById('geo_nwlat').value=0;
        		document.getElementById('geo_nwlong').value=0;
        		document.getElementById('geo_nelat').value=0;
        		document.getElementById('geo_swlong').value=0;
		}
   	}

	if(hitTestBR) {
        	document.getElementById('selat').value=hitTestBR.getLatitude().toFixed(6);
        	document.getElementById('selong').value=hitTestBR.getLongitude().toFixed(6);

		//shared
        	document.getElementById('swlat').value=hitTestBR.getLatitude().toFixed(6);
        	document.getElementById('nelong').value=hitTestBR.getLongitude().toFixed(6);
		
		if(global_properties.allow_geo_edit) {
	        	document.getElementById('geo_selat').value=hitTestBR.getLatitude().toFixed(6);
	        	document.getElementById('geo_selong').value=hitTestBR.getLongitude().toFixed(6);

			//shared
	        	document.getElementById('geo_swlat').value=hitTestBR.getLatitude().toFixed(6);
        		document.getElementById('geo_nelong').value=hitTestBR.getLongitude().toFixed(6);
		}

	} else {
        	document.getElementById('selat').value=0;
        	document.getElementById('selong').value=0;

        	document.getElementById('swlat').value=0;
        	document.getElementById('nelong').value=0;

		if(global_properties.allow_geo_edit) {
        		document.getElementById('geo_selat').value=0;
	        	document.getElementById('geo_selong').value=0;

        		document.getElementById('geo_swlat').value=0;
        		document.getElementById('geo_nelong').value=0;
		}

	}
}

function CometControlsLoginBTN_handler(login_props) {
	if(login_props.authorized) {
		login = 'true';
		document.getElementById("logoutBtn").style.display = "block";
		//document.getElementById("loginContent").style.display = "none";
				
		//document.getElementById("LHS_tabContainer").style.display="block";
		//document.getElementById("RHS_tabContainer").style.display="block";
	
		document.getElementById("LHS_tabHeader_1").style.display="block";
		document.getElementById("LHS_tabHeader_2").style.display="block";
		document.getElementById("LHS_tabHeader_3").style.display="block";
		document.getElementById("LHS_tabHeader_4").style.display="block";
		document.getElementById("LHS_tabHeader_5").style.display="none";
		cTabFliptoPage( "LHS", 1 );
	
		//document.getElementById("mapContent").style.display="block";
		//document.getElementById("wrapper").style.display="block";
		document.getElementById("loggedin_user").innerHTML = login_props.username;
		document.getElementById("loggedin_role").innerHTML = login_props.role;
		//CometControlsValidateLogon();
		//reload properties from alternative file
			
		document.getElementById("icon_user_image").style.display = "block";
		document.getElementById("multi_user_div").style.display = "block";
		document.getElementById("multi_user_div2").style.display = "block";
			
		CometControlsLog("\tinside login, time to unlock search");
		UnLockSearch();
		IsSearchLocked();


		CometGeo_ShowTab();

		document.getElementById("RHS_tabHeader_"+page_number_vidcat).style.display="block";
		document.getElementById("RHS_tabHeader_"+page_number_editor).style.display="block";

				
			
		//document.getElementById("RHS_tabpage_"+page_number_wordcloud).style.display="block";


		CometControlsRoleSwitch(login_props.admin_user,login_props.role);
	} else {
		UpdateStatusMessage("Username and/or password was incorrect.");
	}	
}


function CometControlsLoginBTN_push(username, password, role) { 
	CometStartAJAXAnimation();
	
	var request = getRequestObject();
	var params = "username="+username+"&password="+password+"&role="+role;

	request.onreadystatechange = function() {
		if ((request.readyState == 4) && (request.status == 200) && (request.responseText!="")) {
			CometControlsLog("==========authorize results received============");	
			
			CometControlsLog("=====");
			//CometControlsLog(request.responseText);
			CometControlsLog("=====");
			
			CometStopAJAXAnimation();
			
			CometControlsLoginBTN_handler(JSON.parse(request.responseText));
			
			
	
		}
	};
	//if(params!="") params="?"+params
//	request.open("GET", "do-Query" + params, true);
	request.open("POST", "authorize" , true);
	request.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
//	request.send(null);
	request.send(params);



}


function CometControlsLoginBTN(){
	CometControlsLoginBTN_push(document.getElementById("login_username").value, document.getElementById("login_password").value, document.getElementById("role_selector").value);
}

function RoleToId(tgt_role) {
	for(var i=0; i<global_properties.rolelist.length; i++) {
		if(global_properties.rolelist[i].role==tgt_role) return i;
	}
	return -1;
}

function CometControlsRoleSwitch(admin_user, role) {
	CometControlsLog("=== role switch to role: "+role+" ====");
	var id=RoleToId(role);
	
	//invalid role or roles are unsupported
	if(id==-1) return;
	global_properties.classification=global_properties.rolelist[id].classification;
	global_properties.showCaveats=global_properties.rolelist[id].show_caveats;
	
	if(global_properties.rolelist[id].show_caveats) {
		CometControlsLog("\t>>>>role does not support caveats");
	} else {
		CometControlsLog("\t>>>>role does support caveats");
	
	}
	
	
	//login_props.role=global_properties.rolelist[id].role;
	role=global_properties.rolelist[id].role;
	document.getElementById("loggedin_role").innerHTML = role;

	if(global_properties.rolelist[id].admin) {
		global_properties.admin_user_authorized=admin_user;
		if(!admin_user) {
			CometControlsLog("CometControlsRoleSwitch::attempted to log in as admin, but user is not authorized, fall back on system monitor role");
			CometControlsLog("\tadmin_user="+admin_user+" prop_role_admin="+global_properties.rolelist[id].admin);
			if(global_properties.rolelist[id].fallback!="") return CometControlsRoleSwitch(false, global_properties.rolelist[id].fallback);
			
			ApplySystemMonitorRole();
			
		}
		document.getElementById("RHS_tabHeader_"+page_number_admin).style.display="block";
	
	
	
	
	} else {
		CometControlsLog("CometControlsRoleSwitch::admin page should be invisible now");
		CometControlsLog("\tadmin_user="+admin_user+" prop_role_admin="+global_properties.rolelist[id].admin);
		document.getElementById("RHS_tabHeader_"+page_number_admin).style.display="none";
	}


	if(global_properties.ingestor_enabled && global_properties.rolelist[id].ingest) {
		document.getElementById("RHS_tabHeader_"+page_number_ingest).style.display="block";
	} else {
		document.getElementById("RHS_tabHeader_"+page_number_ingest).style.display="none";
	}
	if(global_properties.rolelist[id].notes) {
		CometControlsLog("CometControlsRoleSwitch::notes page should be visible now");
		CometControlsLog("\tprop_role_notes="+global_properties.rolelist[id].notes);
		document.getElementById("RHS_tabHeader_"+page_number_notes).style.display="block";
	} else {
		CometControlsLog("CometControlsRoleSwitch::notes page should be invisible now");
		CometControlsLog("\tprop_role_notes="+global_properties.rolelist[id].notes);
		document.getElementById("RHS_tabHeader_"+page_number_notes).style.display="none";
	}
	

	if(wctablebuilt) {
		CometControlsLog("\t\tWord cloud is available.. make it block");	
		document.getElementById("RHS_tabHeader_"+page_number_wordcloud).style.display="block";
	} else {
		CometControlsLog("\t\tWord cloud is not available.. do nothing");	
	}


	if(global_properties.geo_enabled) document.getElementById("RHS_tabHeader_"+page_number_geoviz).style.display="block";

	cTabFliptoPage( "LHS", global_properties.rolelist[id].lhspf );
	cTabFliptoPage( "RHS", global_properties.rolelist[id].rhspf );	

	CometControlsSetCaveats();
	login_complete=true;
}


//referenced by html
function CometControlsLogoutBTN() {
	properties_loaded=false;
	document.getElementById("loggedin_user").innerHTML="";
	document.getElementById("loggedin_role").innerHTML="";
//	CometControlsQueryProperties();

	CometStartAJAXAnimation();

	
	var request=getRequestObject();
	request.onreadystatechange = function() {
		if ((request.readyState == 4) && (request.status == 200) && (request.responseText!="")) {
			
			CometStopAJAXAnimation();

			window.location.href="/";
			
			
		} //request is valid
	}; //anon function: ready state change
	
	request.open("GET", "authorize?logout", true);
	
 	//request.send(params);
	request.send(null);
}




//referenced by index.html
function CometControlsBoundingBoxToggleRB() {
	if(global_properties.verbose) CometControlsLog("turnon bounding box");
	
	if(document.getElementById('range_bbox').checked) {
		CometControlsLog("bounding box is checked");
	} else {
		CometControlsLog("bounding box is not checked");
	}
	
}

function CometControlsBusyAnimate(targeted_region, timer) {
	var tag=-1;
	if(busy_animate>animated_array.length) busy_animate=0;
//	CometControlsLog("want to animate region: "+targeted_region);
	document.getElementById(targeted_region).innerHTML="Searching...." + animated_array[busy_animate];
	busy_animate++;
	if(!quit_animating) setTimeout(function() { CometControlsBusyAnimate(targeted_region, timer); }, timer);
	else {
		CometControlsLog("search animation complete, therefore hit first target");
		
		if(starting_point_hint!="") {
			CometControlsLog("starting_point_hint was set to "+starting_point_hint);
			tag=CometControlsRLU(starting_point_hint);
			CometControlsLog("which is tag #"+tag);
		}
		
		if(tag!=-1)  CometControlsOnSearchComplete(tag);
		quit_animating=false;
	}
}

function CometControlsReformatDate(tgt) {
	CometControlsLog("getting date from "+tgt+"...");
	var collection=document.getElementById(tgt).value.split('/');
	CometControlsLog("month="+collection[0]);
	CometControlsLog("day="+collection[1]);
	CometControlsLog("year="+collection[2]);
	CometControlsLog("END getting date from "+tgt+"...");
	return collection[2]+"-"+collection[0]+"-"+collection[1];
}

function CometControlsSearchBTN(se) {
	CometControlsLog("====begin CometControlsSearchBTN("+se+")=======");
	
	if(IsSearchLocked()) {
		CometControlsLog("=== search already in progress, please wait ===");
		return;
	}
	
	LockSearch();
	UpdateStatusMessage("Searching...");
	
	//new imageLoader(cImageSrc, 'startAnimation()');
	quit_animating=false;
	
	CometStartAJAXAnimation();
	
	CometControlsLog("clear datatable");
	$('#dataTable').dataTable().fnClearTable();
	
	CometControlsLog("clear download array");
	filesToDownload = [];
	
	CometGeo_ClearAll();
	//if(global_properties.geo_enabled) GoogleControlsHideAllMarkers();
	
	//clear vidcat
	document.getElementById("catalog").innerHTML="";
	
	
	//reset metadata editor
	document.getElementById("xml_file_path").value="";
	document.getElementById("xml_tag").value="-1";
	document.getElementById("editor_body").innerHTML="Click the edit icon for the object to edit metadata.";
	document.getElementById("editor_back_field").style.backgroundColor="white";
	
	//reset objects
// 	for(var i=0; i<num_of_tags+1; i++) {
// 		placemarkList[i].showBalloon=false;
// 		placemarkList[i].active=false;
// 		placemarkList[i].desc="";
// 		placemarkList[i].geotagged=false;
// 		placemarkList[i].loading=false;
// 		placemarkList[i].flownin=true;
// 		placemarkList[i].mdm=new Object();
// 		
// 	}
	
	var request = getRequestObject();
	var params = "searchEngine="+se; //"searchString=" + search;
	var params2="";
	
	var query="";
	var object="";

	if(se=="MQE") {
		object='xpath_mqe';
	} else if(se=="NEO") {
		object='xpath_neo';
	} else {
		CometControlsLog("missing object name!");
	}
	
	CometControlsLog("object="+object);

	if(document.getElementById(object).value===undefined) {
		document.getElementById(object).value="";
	}
	query=document.getElementById(object).value;


	if(global_properties.geo_enabled && coordinates_shown) {
		//params="?searchEngine=MQE";
		params += "&" + "north=" + document.getElementById('nwlat').value;
		params += "&" + "east=" + document.getElementById('selong').value;
		params += "&" + "south=" + document.getElementById('selat').value;
		params += "&" + "west=" + document.getElementById('nwlong').value;
		params += "&searchType=quadrant";
	} else {
		params = "&searchEngine="+se+"&searchType=everything";
	}


	if(query!="") {
		params="&searchEngine="+se+"&searchType=xpath&query="+encodeURIComponent(query);
	} 
	if(document.getElementById('search_kml').checked) {
		params+="&only_geo=true"
	}
	
	if(calendar_shown) {
		if(document.getElementById("start_date").value!="") {
			params+="&start_date="+CometControlsReformatDate("start_date");
		}
		if(document.getElementById("end_date").value!="") {
			params+="&end_date="+CometControlsReformatDate("end_date");
		}
	}

	CometControlsLog("\tsearching as role: ");
	CometControlsLog("\t---->: "+document.getElementById("loggedin_role").innerHTML);
	params+="&role="+document.getElementById("loggedin_role").innerHTML;

	request.onreadystatechange = function() {
		if ((request.readyState == 4) && (request.status == 200) && (request.responseText!="")) {
			CometStopAJAXAnimation();
			UnLockSearch();
			UpdateStatusMessage("Search Completed");
			CometControlsLog("==========search results received============");	
			
			CometControlsLog("=====");
			CometControlsLog(request.responseText);
			CometControlsLog("=====");
			
			
			//global_properties.cached_search_results
			placemarkList=JSON.parse(request.responseText)  
			
			CometSearchResultsAddRows("dataTable.tableBody");
			cTabFliptoPage("LHS",4);
			
			
			
			
			
			
			//go to the designated "first hit" in the series
			if(starting_point_hint!="") {
				CometControlsLog("going to first hit: "+starting_point_hint);
			
				CometControlsStartScene3(starting_point_hint);
				starting_point_hint="";
			}


		}
	};
	//if(params!="") params="?"+params
//	request.open("GET", "do-Query" + params, true);
	request.open("POST", "query" , true);
	request.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
//	request.send(null);
	request.send(params);
	CometControlsLog("=====search request sent=====");	
	
	//deprecated
// 	if(document.getElementById("automatic_search").checked) {
// 		CometControlsLog("automatically enacting search every "+automatic_search_delay+" seconds");	
// 		setTimeout(function() { CometControlsSearchBTN(se)}, automatic_search_delay * 1000);
// 	}
}

//ref by index.html
function CometControlsDownloadBTN() {
    	var filename = "";
    	if (filesToDownload.length == 1) {
		filename = filesToDownload[0].toString();	
	} else {
		filename = filesToDownload.toString();	
	}
	var form = document.createElement('form');
	document.body.appendChild(form);
	var input = document.createElement('input');
	input.setAttribute('type', 'hidden');
	input.setAttribute('name', 'filename');
	input.setAttribute('value', filename);
	form.appendChild( input );
	
	if(askmlpath) {
		CometControlsLog("askmlpath is checked");
		form.setAttribute('action', 'Download?askml_path=true');
		askml_path=false;
	} else if(document.getElementById("askml").checked) {
		CometControlsLog("askml is checked");
		form.setAttribute('action', 'Download?askml=true');
	} else {
		CometControlsLog("askml is not checked");
		form.setAttribute('action', 'Download');
	}
	form.setAttribute('method', 'post');
	form.submit();
	UpdateStatusMessage("Download Requested");
}


function CometControlsDownloadAllBTN() {
	//now clear out the array
	filesToDownload=new Array();
	
	//loop through, add all urls to the listing
	
	CometControlsLog("viewable objects="+viewable_objects);
	CometControlsLog("maximum tag="+num_of_tags);
	var count=0;
	for(var tag=0; tag<num_of_tags; tag++) {
		if(placemarkList[tag].active) count++;
	}
	CometControlsLog("count="+count);
	
	
	for(var tag=0; tag<viewable_objects; tag++) {
		CometControlsLog("---->adding (tag="+tag+") "+placemarkList[tag].mdm.utf8name);
		CometControlsLog("---->adding (tag="+tag+") "+placemarkList[tag].mdm);
		CometControlsLog("---->adding (tag="+tag+") "+placemarkList[tag]);
		if(placemarkList[tag].mdm.utf8name) CometControlsFileDownloadArray(tag);
	}
	//hit download
	CometControlsDownloadBTN();

	//got to figure out how to do posts and catch a success
	setTimeout(function() { 
		CometControlsLog("enter annon function....");
		//clear it out
		filesToDownload= [];

		filesToDownload= new Array();
	
			CometControlsLog("looping through viewable objects: "+viewable_objects+" looking for checks");
		//if the item is checked, re-add it
			for(var i=0; i<viewable_objects; i++) {
			
				CometControlsLog("is download_db_"+i+" is checked?");
			
  				if(document.getElementById('download_cb_'+i) && document.getElementById('download_cb_'+i).checked) {
					CometControlsLog("download_db_"+i+" is checked, add it");
				
					CometControlsFileDownloadArray(i);	
				} //end if
  			} //end for
		} //end annon function
		,5000);
	

}

function CometControlsSetCaveats() {
	CometControlsLog(">>>>>> CometControlsSetCaveats() BEGIN");
	var rows = document.getElementById("main_table").getElementsByTagName("tr");
	var n = rows.length;
	
	
	if(global_properties.showCaveats) {

		CometControlsLog("\tcaveats enabled, setting classifications");

		CometControlsLog("row[0] style = "+rows[0].style.display);
		rows[0].style.display="";
		rows[n-1].style.display="";

		rows[0].style.backgroundColor=CometToolTipBuilderCaveatColor(global_properties.classification);
		rows[n-1].style.backgroundColor=CometToolTipBuilderCaveatColor(global_properties.classification);

		document.getElementById("caveat_top").innerHTML=global_properties.classification + " (simulated)";
		document.getElementById("caveat_top").style.color=CometToolTipBuilderCaveatColorForeground(CometToolTipBuilderCaveatColor(global_properties.classification));
		document.getElementById("caveat_bottom").innerHTML=global_properties.classification + " (simulated)";
		document.getElementById("caveat_bottom").style.color=CometToolTipBuilderCaveatColorForeground(CometToolTipBuilderCaveatColor(global_properties.classification));
	
		if(document.getElementById('catalog')) {
			if(document.getElementById('catalog').innerHTML!="") {
				CometControlsReloadVidCat(document.getElementById("xml_tag").value);
			}
		}		
		
	} else {
	
	
	
		CometControlsLog("\tcaveats disabled, hiding classifications");
		rows[0].style.display="none";
		rows[n-1].style.display="none";
		if(document.getElementById('catalog')) {
			if(document.getElementById('catalog').innerHTML!="") {
				CometControlsReloadVidCat(document.getElementById("xml_tag").value);
			}
		}
	}
	CometControlsLog(">>>>>> CometControlsSetCaveats() END");
}

//index.html
function CometControlsDrawSceneRowBTN(target, desc, fname, searchterm) {
	var temp_content='<TR><TD>'+desc+'</td><TD><button class="btn" onmouseover="CometControlsTip(\'Click to begin scenario\')" onmouseout="CometControlsUnTip()" onclick="javascript:CometControlsStartScene(\''+fname+'\',\''+searchterm+'\')">Begin</button></td></tr>';
	document.getElementById(target).innerHTML+=temp_content;
}

function CometControlsDrawWordCloudRowBTN(target, desc, path) {
	var temp_content='<TR><TD>'+desc+'</td><TD><button class="btn" onmouseover="CometControlsTip(\'Click to load word cloud\')" onmouseout="CometControlsUnTip()" onclick="javascript:CometControlsLoadWordCloudByPath(\''+path+'\')">Begin</button></td></tr>';
	document.getElementById(target).innerHTML+=temp_content;
}

function CometControlsStartSceneMQE(fname, searchterm, xpath) {
	CometControlsLog("=========>StartScene (MQE): fname="+fname+" directory filter="+searchterm+" xpath query="+xpath);
	document.getElementById("search").value=searchterm;
	document.getElementById("xpath_mqe").value=xpath;
	CometControlsSearchBTN('MQE');
	
	starting_point_hint=fname;
	
	//go to the designated "first hit" in the series
	if(CometGeo_Enabled() && starting_point_hint!=null) cTabFliptoPage("RHS",1);
	else cTabFliptoPage("RHS",2);
	CometControlsLog("End Scene<=========>StartScene (MQE): fname="+fname+" directory filter="+searchterm+" xpath query="+xpath);
	
}

function CometControlsStartSceneNEO(query) {
	CometControlsLog("=========>StartScene (NEO): query="+query);
	document.getElementById("xpath_neo").value=query;
	CometControlsSearchBTN('NEO');
	CometControlsLog("End Scene<=========>StartScene (NEO): query="+query);
}

function CometControlsStartScene(fname, searchterm, xpath) {
	CometControlsLog("\n\nDEPRECATED.. you want CometControlsStartSceneMQE or CometControlsStartSceneNEO\n\n");
	CometControlsStartSceneMQE(fname, searchterm, xpath);
}

function CometControlsLog(text) {
	if(global_properties.verbose) console.log(text);
}

//returns tag based on image name
function CometControlsRLU(image) {
	var tag="";
	for(var i=0; i<num_of_tags; i++) {
		tag=i;
		if(placemarkList[tag].active) {
			if(placemarkList[tag].mdm.utf8name.indexOf(image)!=-1) {
				return tag;
			}	
		}
	}
	return "-1";
}

function CometControlsFlyThereRLU(image) {
	cTabFliptoPage("RHS",page_number_geoviz);
	var tag=CometControlsRLU(image);
	if(tag=="zz") {
		return;
	}
	placemarkList[tag].flownin=true;
	CometGeo_Fly(tag,true);
	setTimeout(function() { 
		CometGeo_Fly(tag); 
		CometGeo_ToggleBalloon(tag);
	
	}, fly_balloon_delay);
}

function CometControlsFlyOverRLU(image) {
	cTabFliptoPage("RHS",page_number_geoviz);
	var tag=CometControlsRLU(image);
	if(tag=="zz") {
		return;
	}
	// hide all balloons
	placemarkList[tag].flownin=true;
	CometGeo_Fly(tag);
	setTimeout(function() { CometGeo_ToggleBalloon(tag); }, fly_balloon_delay * 2);
}

function CometControlsLoadAnnotationsByTag(tag) {
//	CometControlsLoadMetadata_internal(tag,CometToolTipBuilderUrl2Path(placemarkList[tag].url),placemarkList[tag].active_annotation);
	CometControlsLoadAnnotations_internal(tag);
}


function CometControlsEditMetadata(tag) {
	document.getElementById("editor_back_field").style.backgroundColor="black";
	global_tag=tag;
	CometControlsLoadAnnotationsByTag(tag);
	cTabFliptoPage( "RHS", page_number_editor );
}

function DEPRECATED_CometControlsReloadVidCatLoop(tag) {
	CometControlsLog("CometControlsReloadVidCatLoop("+tag+") BEGIN ");
	if(placemarkList[tag].loading) {
		CometControlsLog("still loading...");
		setTimeout(function() { CometControlsReloadVidCatLoop(tag); }, 200);
	} else {
		CometControlsLog("vidcat done loading");
		CometControlsReloadVidCat(tag);
		cTabFliptoPage( "RHS", page_number_vidcat );
	
		CometStopAJAXAnimation();
	}
	CometControlsLog("CometControlsReloadVidCatLoop("+tag+") END");
}

function CometControlsPushToHCPBTN() {
	var tag=document.getElementById("xml_tag").value;
	CometControlsLog("==== CometControlsPushToHCPBTN("+tag+" BEGIN ====");
	
	//start animation
	CometStartAJAXAnimation();


	//post
	var form = document.createElement('form');
	form.setAttribute('id', 'pushForm');
	form.setAttribute('name', 'pushForm');
	
	
	document.body.appendChild(form);
	var input;
	
	
	input = document.createElement('input');
	input.setAttribute('type', 'hidden');
	input.setAttribute('name', 'filename');
//	input.setAttribute('value',  document.getElementById("xml_file_path").value );
	input.setAttribute('value',  placemarkList[tag].mdm.urlName);
	form.appendChild( input );

	input = document.createElement('input');
	input.setAttribute('type', 'hidden');
	input.setAttribute('name', 'xml_content');
	input.setAttribute('value', document.getElementById("xmlcontent").value);
	form.appendChild( input );

	form.setAttribute('action', 'MetadataModifier');
	form.setAttribute('method', 'post');
	form.setAttribute('target', 'iframe');
	form.submit();

	
	//CometControlsLog("tag="+tag);
	setTimeout(function() { CometControlsPushToHCPBTN_internal(tag); },2000); 
	CometControlsLog("==== CometControlsPushToHCPBTN("+tag+" END ====");
}

function CometControlsOnObjectMouseOver(tag) {
	CometControlsTip(placemarkList[tag].tooltip_contents);
	CometControlsChangeIcon(tag, CometGetIconPath(tag),2.0);
}

//change to OnObjectMouseDown to trigger sticky note
//function CometControlsOnObjectMouseOver(tag) {
function CometControlsOnObjectMouseDown(tag) {
	CometControlsLog("CometControlsOnObjectMouseOver("+tag+")");
	CometControlsLog("annotation_size="+placemarkList[tag].mdm.annotation_size);
	CometControlsLog("fileType="+placemarkList[tag].mdm.fileType);

	if(placemarkList[tag].mdm.fileType=="directory") {
		document.getElementById("xpath_mqe").value=placemarkList[tag].mdm.utf8name;
		CometControlsSearchBTN('MQE');
	}

	if(!document.getElementById("hidden_row_"+tag)) {
		CometControlsLog("tag was invalid");
		ondisplay_tag="";
		return;
	}
	
	if(document.getElementById("hidden_row_"+tag).style.display=="none") {
		if(CometHasContent(ondisplay_tag) && document.getElementById("hidden_row_"+ondisplay_tag)) document.getElementById("hidden_row_"+ondisplay_tag).style.display="none";
		document.getElementById("hidden_row_"+tag).style.display="block" 
		ondisplay_tag=tag;
		
		//check to see if we're on page RHS2
		
		var page=cTabGetCurrentPage("RHS");
		switch(Number(page)) {
			case page_number_geoviz:
				//click balloon icon for tag
				CometGeo_ToggleBalloon(tag);
			
			break;
			
			case page_number_vidcat:
				//click vidcat for tag
				CometControlsViewInVidCat(tag);
			break;
			case page_number_editor:
				//click editor
				CometControlsEditMetadata(tag);
			break;
			default:
				CometControlsLog("do nothing for this page "+page);
			break;
		}
	} else { 
		document.getElementById("hidden_row_"+tag).style.display="none" 
		ondisplay_tag="";
	}
}

function CometControlsShowMeRLU(filename) {
	var tag=CometControlsRLU(fname);
	CometControlsShowMe(tag);

}

function CometControlsShowMe(tag) {
	if(global_properties.geo_enabled) {
		placemarkList[tag].flownin=true;
		
		CometGeo_Fly(tag, true);
		//GoogleControlsFly(tag);
//		setTimeout(function() { GoogleControlsFly(tag); }, fly_balloon_delay);
		setTimeout(function() { CometGeo_Fly(tag, true); }, fly_balloon_delay);
		
		setTimeout(function() { CometGeo_ToggleBallon(tag); }, fly_balloon_delay * 2);
//		GoogleControlsToggleBalloonDelayed(tag, fly_balloon_delay * 2);
	} else CometControlsOnObjectMouseDown(tag);
}

//fire after search is completed
function CometControlsOnSearchComplete(tag) {
	CometControlsShowMe(tag);
}

function CometControlsReloadVidCat(tag) {
	CometControlsLog(">>>>>>>>>>>>>>>CometControlsReloadVidCat("+tag+") BEGIN");
	
	if(document.getElementById("action_tag_"+tag)) {
			document.getElementById("action_tag_"+tag).innerHTML='<center><img onmouseover="CometControlsTip(\''+placemarkList[tag].link_label+'\')" onmouseout="CometControlsUnTip()" onmousedown="'+placemarkList[tag].link_action+'" src="images/icons/play-icon.png" width=32 height=32></center>';
	}
	
	//var url=CometToolTipBuilderGetThumbnail(tag);
	document.getElementById('catalog').innerHTML='<div id="video_target" style="display:none;"></div><div id="thumbnail_target"><table width=790px height=670px><tbody id=object_info></tbody></table></div>';

		
	//added to use new description model	
	LoadFromDescription(tag);	

	CometControlsLog("CometControlsReloadVidCat("+tag+") END <<<<<<<<<<<<<<<<");
}

function CometControlsPlayAction(tag) {
	CometControlsLog("BEGIN CometControlsPlayAction("+tag+")");
	if(!CometHasContent(placemarkList[tag].mdm.link_action)) {
		CometControlsLog("attempted to run link action without content for tag="+tag);
		return;
	}
	
	if(placemarkList[tag].mdm.link_action.contains("CometControlsPlayAudioByTag")) CometControlsPlayAudioByTag(tag);
	else if(placemarkList[tag].mdm.link_action.contains("CometControlsPlayMPEGVideoByTag")) CometControlsPlayMPEGVideoByTag(tag); 
	else if(placemarkList[tag].mdm.link_action.contains("CometControlsPlayYouTubeVideoByTag")) CometControlsPlayYouTubeVideoByTag(tag); 
//	else if(placemarkList[tag].mdm.link_action.contains("GoogleControlsLoadKMLviaNetworkLinkByTag")) GoogleControlsLoadKMLviaNetworkLinkByTag(tag); 
	else if(placemarkList[tag].mdm.link_action.contains("LoadKML")) CometGeo_LoadKML(tag); 
	
	CometControlsLog("END CometControlsPlayAction("+tag+")");
}

//formerly onobjectmousedown
//function CometControlsOnObjectMouseDown(tag) {
function CometControlsViewInVidCat(tag) {
	CometControlsLog("BEGIN::: ViewInVidCat("+tag+")");

	if(tag>num_of_tags || !placemarkList[tag].active) {
		CometControlsLog("tag is already 0, just return");
		if(tag==0) return;
		CometControlsLog("tag out of range, resetting to 0");
		tag=0;
//		CometControlsOnObjectMouseDown(tag);
		CometControlsViewInVidCat(tag);
		return;
	}

	global_tag=tag;
	//CometControlsLoadMetadataByTag(tag);
	CometControlsLoadAnnotationsByTag(tag);
	cTabFliptoPage("RHS",page_number_vidcat);
	CometControlsLog("\tcontinue mousdown on vidcat("+tag+") === should stop action of "+current_tag+" ======");
	current_tag=tag;
	CometControlsReloadVidCat(tag);
// 	if(document.getElementById("automatic_playnext").checked) {
// 		tag++;
// 		setTimeout(function() { CometControlsViewInVidCat(tag); }, automatic_next_delay*1000);
// 	}
	
	CometControlsLog("END::: ViewInVidCat("+tag+")");
}

function CometControlsOnObjectMouseOut(tag) {
	CometControlsUnTip();
	
	//change placemark icon back
	CometControlsChangeIcon(tag, paddle_icon, 1.0);
	
}

function CometControlsChangeIcon(tag, to_icon, scale) {
//	CometControlsLog("Change icon to="+to_icon);
//	if(!global_properties.geo_enabled || !placemarkList[tag] || !placemarkList[tag].geotagged) return;
	if(placemarkList[tag].geotagged) CometGeo_changeIconTo("placemark"+tag, to_icon, scale);
	
// 	var icon = ge.createIcon('');
// 	icon.setHref(to_icon);
// 	var style = ge.createStyle('');
// 	style.getIconStyle().setIcon(icon);
// 	style.getIconStyle().setScale(scale);
// 
// 	if(!placemarkList[tag].placemark) return;
// 
// 	placemarkList[tag].placemark.setStyleSelector(style);

}

function CometControlsFlyToLocationBTN() {
//	CometControlsLog("pressed CometControlsFlyToLocationBTN()");
//	GoogleControlFlytoGeoLocation(document.getElementById("geolocation").value);
	
	CometGeo_FlytoGeoLocation(document.getElementById("geolocation").value);
}

// function CometControlsRecenterBTN() {
// 	GoogleControlsFlyOverLatLong_INTERNAL(Number(document.getElementById("clat_formxml").value),Number(document.getElementById("clong_formxml").value));
// }

function CometControlsTip(content) {
	if(showTooltips) Tip(content);
	//else CometControlsLog("ignoring tip request");
}
function CometControlsUnTip() {
	if(showTooltips) UnTip();
	//else CometControlsLog("ignoring untip request");
}

function CometControlsExternalConfigButtons(url) {
	var splits=new Array();
	splits=url.split('/');
	var parts=new Array();
	parts=splits[2].split('.');
	namespace=parts[0];
	tenant=parts[1];
	hcp_name=parts[2];
	
	for(var i=3; i<parts.length; i++) {
		hcp_domain+=parts[i];
		if(i+1!=parts.length) hcp_domain+=".";
	}
//	hcp_domain=parts[3]+"."+parts[4];
	if(testing) return;
	var hdds_name="hdds";
	CometControlsLog("namespace="+namespace+" hcp_domain="+hcp_domain);
	var content="<table border=1><tr>";
	content+="<td><input type=button id=ta onclick=window.open('https://"+namespace+"."+tenant+"."+hcp_name+"."+hcp_domain+"') target=_blank value=\"HCP (Data) Namespace Browser\" /></td>";
	content+="</tr><tr>";
	content+="<td><input type=button id=ta onclick=window.open('https://"+global_properties.config_ns+"."+tenant+"."+hcp_name+"."+hcp_domain+"') target=_blank value=\"HCP (Config) Namespace Browser\" /></td>";

	if(global_properties.hdds_enabled) {
		content+="</tr><tr>";
		content+="<td><input type=button id=ta onclick=window.open('https://"+global_properties.hdds_name+":8443/hdds/service/search') target=_blank value=\"HDDS Search Console\" /></td>";
		content+="</tr><tr>";
		content+="<td><input type=button id=ta onclick=window.open('https://"+global_properties.hdds_name+":8443/hdds/admin') target=_blank value=\"HDDS Admin Interface\" /></td>";
	}
	content+="</tr><tr>";
	content+="<td><input type=button id=ta onclick=window.open('https://"+tenant+"."+hcp_name+"."+hcp_domain+":8000') target=_blank value=\"HCP Tenant Admin\" /></td>";
	content+="</tr><tr>";
	content+="<td><input type=button id=ta onclick=window.open('https://admin."+hcp_name+"."+hcp_domain+":8000') target=_blank value=\"HCP Admin\" /></td>";
	content+="</tr><tr>";
	content+="<td><input type=button id=ta onclick=window.open('https://"+tenant+"."+hcp_name+"."+hcp_domain+":8888') target=_blank value=\"HCP Search Console\" /></td>";
	content+="</tr><tr></table>";
	document.getElementById("ext_buttons").innerHTML=content;
}

/************************ FROM INGEST *****************************/



function CometControlsInitializeUploader() {
	console.log("--->> CometControlsInitializeUploader() BEGIN");
	//console.log("--->> CometControlsInitializeUploader() BEGIN");
	uploader.bind('Init', function(up, params) {
		document.getElementById('uploader_filelist').innerHTML = '<div style="display:none">Current runtime: ' + params.runtime + '</div>';
		
		//if(params.runtime!="html5") CometControlsLog("Runtime failed to load for uploader");
		console.log("params="+params);
	});
	
	uploader.init();
	
	uploader.bind('FilesAdded', function(up, files) {
		for (var i in files) {
			document.getElementById('uploader_filelist').innerHTML += '<div id="' + files[i].id + '">' + files[i].name + ' (' + plupload.formatSize(files[i].size) + ') <b></b></div>';
			
		}
		//document.getElementById('uploader_upfiles').enabled=true;
		document.getElementById('uploader_upfiles').click();
	});
	
	uploader.bind('UploadProgress', function(up, file) {
		document.getElementById(file.id).getElementsByTagName('b')[0].innerHTML = '<span>' + file.percent + "%</span>";
	});
	
	uploader.bind('UploadComplete', function(up, files) {
		CometControlsReloadFileTree();
		UpdateStatusMessage("Upload complete.");		

		setTimeout(function() { document.getElementById('uploader_filelist').innerHTML = '<div style="display:none">Current runtime: html5</div>'; }, 5000);
		
	});
	
	document.getElementById('uploader_upfiles').onclick = function() {
		console.log("current upload url is: "+uploader.settings.url);
		CometControlsLog("root_ingest_dir="+root_ingest_dir);
		CometControlsLog("root_ingest_dir_orig="+root_ingest_dir_orig);
		
		uploader.settings.url=global_properties.webapp+'Upload?uploaddir='+root_ingest_dir.substring(root_ingest_dir_orig.length-1,root_ingest_dir.length);
		console.log("now, upload url is: "+uploader.settings.url);
//		console.log("current upload url is: "+uploader.getUrl());
		uploader.start();
		return false;
	};
	console.log("--->> CometControlsInitializeUploader() END");
}

function CometControlsReloadFileTree() {
	CometControlsLog("CometControlsReloadFileTree() :::: BEGIN");
				$('#fileTree').fileTree({ root: root_ingest_dir, script: 'jqueryFileTree.jsp' }, function(file) { 
				
					document.getElementById("remove_item").value=file.substring(root_ingest_dir_orig.length-1,file.length);
				
					});
}
		
function InitTabs(prefix) {
	CometControlsLog("initialize tabs prefix="+prefix);
	console.log("initialize tabs prefix="+prefix);
	var container = document.getElementById(prefix+"_tabContainer");
		
	if(container==null) console.log("container is null?");
	// set current tab
	var navitem = container.querySelector(".tabs ul li");
	//store which tab we are on
	var ident = navitem.id.split("_")[2];
	navitem.parentNode.setAttribute("data-current",ident);
		//set current tab with class of activetabheader
	navitem.setAttribute("class","tabActiveHeader");
		//hide all tab contents we don't need
	var pages = container.querySelectorAll(".tabpage");
	for (var i = 1; i < pages.length; i++) {
		pages[i].style.display="none";
	}

	//this adds click event to tabs
	var tabs = container.querySelectorAll(".tabs ul li");
	for (var i = 0; i < tabs.length; i++) {
//		console.log("assigning onclick attribute for tab #"+i+" with prefix "+prefix);
	      tabs[i].onclick=cTabDisplayPageOnClick;
	}
}

function CometControlsCreateDir() {
	CometControlsLog("clicked create dir");
	
	if(document.getElementById("makeDir").style.display=='none') {
		document.getElementById("makeDir").style.display='block';
	
	
	} else {
	
		CometControlsLog("about to create a directory called "+document.getElementById("selected_dir_name").innerHTML+document.getElementById("newdir_input").value);
	
		CometStartAJAXAnimation();

		
		var request=getRequestObject();	

		request.onreadystatechange = function() {	
			if ((request.readyState == 4) && (request.status == 200)) {
				CometControlsLog("created directory called "+document.getElementById("selected_dir_name").innerHTML+document.getElementById("newdir_input").value);
			
				CometControlsCHDir(document.getElementById("selected_dir_name").innerHTML+document.getElementById("newdir_input").value);
			
			
				document.getElementById("makeDir").style.display='none';
				document.getElementById("newdir_input").value="";
				CometStopAJAXAnimation();
			}
		}
		var params="?dir="+document.getElementById("selected_dir_name").innerHTML.substring(root_ingest_dir_orig.length-1,document.getElementById("selected_dir_name").innerHTML.length)+document.getElementById("newdir_input").value;
		request.open("GET", "MKdir" + params, true);
		request.send(null);
		CometControlsLog("params="+params);
		
	}	
}
	
function ChopRt(s, delim) {
	var parts=new Array();
	var parts=s.split(delim);
	var offset=1;
	if(s[s.length-1]==delim) offset=2;
	var retval=s.substring(0,s.length-parts[parts.length-offset].length-1);
	return retval;
}
function CometControlsRemoveDir() {
	CometControlsLog("clicked remove file/dir");
	
	if(document.getElementById("removeDir").style.display=='none') {
		document.getElementById("removeDir1").setAttribute('disabled',true);
		document.getElementById("removeDir").style.display='block';
	
	
	} else {
	
		
		var deleteme="";
		if(document.getElementById("remove_item").value=="") {
			document.getElementById("remove_item").value=document.getElementById("selected_dir_name").innerHTML.substring(root_ingest_dir_orig.length-1,document.getElementById("selected_dir_name").innerHTML.length);
		}
		
		
		//CometControlsLog("about to remove a file/directory called "+document.getElementById("selected_dir_name").innerHTML+document.getElementById("newdir_input").value);
		CometStartAJAXAnimation();
	
		
		var request=getRequestObject();	

		request.onreadystatechange = function() {	
			if ((request.readyState == 4) && (request.status == 200)) {
				if(document.getElementById("selected_dir_name").innerHTML.substring(document.getElementById("selected_dir_name").innerHTML.length-1,document.getElementById("selected_dir_name").innerHTML.length)=="/") CometControlsUpDir()
				//document.getElementById("removeDir").style.display='none';
				document.getElementById("removeDirBTN").setAttribute('disabled',false);
				document.getElementById("removeDir1").setAttribute('disabled',false);
				//document.getElementById("newdir_input").value="";

				CometStopAJAXAnimation();
			}
		}
		var params="?dir="+document.getElementById("selected_dir_name").innerHTML.substring(root_ingest_dir_orig.length-1,document.getElementById("selected_dir_name").innerHTML.length);
		request.open("GET", "RMdir" + params, true);
		request.send(null);
		CometControlsLog("params="+params);
		
	}	
}
function CometControlsCHDir(newdir) {
	CometControlsLog("cwd: "+root_ingest_dir);
	CometControlsLog("desired dir: "+newdir);
	root_ingest_dir=newdir;
	document.getElementById("selected_dir_name").innerHTML=newdir;
	document.getElementById("remove_item").value=newdir.substring(root_ingest_dir_orig.length-1,newdir.length);
	CometControlsReloadFileTree();

}
function CometControlsUpDir() {
	CometControlsLog("clicked up dir");
	CometControlsLog("orig="+root_ingest_dir_orig);
	if(root_ingest_dir==root_ingest_dir_orig) {
		CometControlsLog("already at top level");
		
		CometControlsCHDir(root_ingest_dir);
	} else {
		CometControlsLog("go up one level");
		CometControlsCHDir(ChopRt(root_ingest_dir,"/"));
	}
}
function CometControlsDownDir() {
	CometControlsLog("clicked down dir");
	CometControlsCHDir(document.getElementById("selected_dir_name").innerHTML);
}

function CheckRemoveBtn() {

	if(document.getElementById("remove_item").value=="/") {
	
		CometControlsLog("new item ="+document.getElementById("remove_item").value+" therefore, disable btn");
	
	
		document.getElementById("removeDirBTN").setAttribute('disabled',true);
	} else {
	
		CometControlsLog("new item ="+document.getElementById("remove_item").value+" therefore, enable btn");
	
		document.getElementById("removeDirBTN").setAttribute('disabled',false);
	}
	
}

function CometControlsPlayYouTubeVideoByRLU(rlu, vid) {
	var tag=CometControlsRLU(rlu);
	CometControlsOnObjectMouseDown(tag);
	CometControlsPlayYouTubeVideoByTag(tag, vid);
}
	
function CometControlsPlayYouTubeVideoByTag(tag, vid) {
	document.getElementById('video_target').innerHTML='<iframe width="640" height="360" src="http://www.youtube.com/embed/'+vid+'?feature=player_embedded" frameborder="0" allowfullscreen></iframe>';

	document.getElementById('video_target').style.display="block";
	document.getElementById('thumbnail_target').style.display="none";
	
	document.getElementById("action_tag_"+tag).innerHTML='<center><img onmouseover="CometControlsTip(\'Stop Video\')" onmouseout="CometControlsUnTip()" onmousedown="javascript:CometControlsStopVideo(\''+tag+'\')" src="images/icons/stop-icon.png" width=32 height=32></center>';

	

}

function CometControlsStopVideo(tag) {

	document.getElementById('video_target').style.display="none";
	document.getElementById('thumbnail_target').style.display="block";
	
	document.getElementById("action_tag_"+tag).innerHTML='<center><img onmouseover="CometControlsTip(\''+placemarkList[tag].link_label+'\')" onmouseout="CometControlsUnTip()" onmousedown="'+placemarkList[tag].link_action+'" src="images/icons/play-icon.png" width=32 height=32></center>';



}

function CometControlsPlayAudio(rlu) {
	CometControlsLog("CometControlsPlayAudio() is deprecated, you probably wanted CometControlsPlayAudioRLU");
	
	CometControlsPlayAudioByRLU(rlu);
}
function CometControlsPlayAudioByRLU(rlu) {
	var tag=CometControlsRLU(rlu);
	CometControlsOnObjectMouseDown(tag);
	CometControlsPlayAudioByTag(tag);
}

function CometControlsPlayAudioByTag(tag) {	
	CometControlsLog("\n\nBEGIN CometControlsPlayAudioByTag("+tag+")");
	if(nowplaying!="") {
		CometControlsLog("already playing tag="+nowplaying+"... stoppping music");
		CometControlsStopAudio(nowplaying);
	}
	

	//need to change this target someday
	
	var object_url=global_properties.imageprefix+"Relay?path="+placemarkList[tag].mdm.urlName+"&type=object&stream";
	//var object_url="https://"+namespace+"."+tenant+"."+hcp_name+"."+hcp_domain+"/rest"+placemarkList[tag].mdm.urlName;

	var content='<audio id="now_playing" src="'+object_url+'" type="audio/mp3" controls="controls"></audio>';
//	var content='<audio id="now_playing" src="'+placemarkList[tag].url+'" controls="controls"></audio>';
	document.getElementById('mp3_player').innerHTML=content;


	$('audio,video').mediaelementplayer({audioWidth: 300});

	document.getElementById('mp3_player').style.display="block";
	//document.getElementById('thumbnail_target').style.display="none";
	
	document.getElementById("action_tag_"+tag).innerHTML='<center><img onmouseover="CometControlsTip(\'Stop Audio\')" onmouseout="CometControlsUnTip()" onmousedown="javascript:CometControlsStopAudio(\''+tag+'\')" src="images/icons/stop-icon.png" width=32 height=32></center></span>';
	nowplaying=tag;
	
	CometControlsLog("END CometControlsPlayAudioByTag("+tag+")");
}

//TODO: http://stackoverflow.com/questions/11867779/mediaelement-js-and-custom-playlist
//doc: http://mediaelementjs.com/#api




function CometControlsStopAudio(tag) {
	nowplaying="";
	document.getElementById('mp3_player').style.display="none";
	document.getElementById('thumbnail_target').style.display="block";
	document.getElementById("action_tag_"+tag).innerHTML='<center><img onmouseover="CometControlsTip(\''+placemarkList[tag].mdm.link_label+'\')" onmouseout="CometControlsUnTip()" onmousedown="'+CometControlsLinkAction(tag)+'" src="images/icons/play-icon.png" width=32 height=32></center>';
	placemarkList[tag].playing=false;
}



function CometControlsPlayMPEGVideo(rlu) {

	CometControlsLog("function deprecated, you probably want CometControlsPlayMPEGVideoByRLU");
	CometControlsPlayMPEGVideoByRLU(rlu);
}

function CometControlsPlayMPEGVideoByRLU(rlu) {
	var tag=CometControlsRLU(rlu);
	CometControlsOnObjectMouseDown(tag);
	CometControlsPlayMPEGVideoByRLU(tag);
}


function CometControlsPlayMPEGVideoByTag(tag) {
	//for now, skip wav support
	//if(placemarkList[tag].url.ends_with('.mp3')) ....
	if(nowplaying!="") {
		CometControlsLog("already playing tag="+nowplaying+"... stoppping music");
		CometControlsStopMPEGVideo(nowplaying);
	}


	//need to change this target someday
	//var content='<audio id="now_playing" src="'+placemarkList[tag].url+'" type="audio/mp3" controls="controls"></audio>';
	document.getElementById('thumbnail_target').style.display="none";
	
	//pointing to COMET
	var object_url=global_properties.imageprefix+"Relay?path="+placemarkList[tag].mdm.urlName+"&type=object&cached";
	
	//original, pointing back to HCP directly
	//var object_url=global_properties.destination_rootPath+placemarkList[tag].mdm.urlName;
	
	
	var content='<video id="video_now_playing" width=720 controls>';
	content+='<source src="'+object_url+'" width=720 controls type="video/x-m4v"></source>';
//	content+='<source src="'+object_url.substring(0,object_url.length - 3)+'webm" type="video/webm"></source>';
	content+='<source src="'+object_url+'&webm"  width=720 controls type="video/webm"></source>';
	content+='</video>';
//	var content='<video id="video_now_playing" preload="none" width=720 controls></video>';
	
	document.getElementById('video_target').innerHTML=content;

	document.getElementById('video_target').style.display="block";
	//document.getElementById('thumbnail_target').style.display="none";
	
	if(!testing)
	document.getElementById("action_tag_"+tag).innerHTML='<center><img onmouseover="CometControlsTip(\'Stop Video\')" onmouseout="CometControlsUnTip()" onmousedown="javascript:CometControlsStopMPEGVideo(\''+tag+'\')" src="images/icons/stop-icon.png" width=32 height=32></center></span>';

	nowplaying=tag;
}

//TODO: http://stackoverflow.com/questions/11867779/mediaelement-js-and-custom-playlist
//doc: http://mediaelementjs.com/#api




function CometControlsStopMPEGVideo(tag) {
	nowplaying="";
	document.getElementById('video_target').style.display="none";
	document.getElementById('thumbnail_target').style.display="block";
	
	if(document.getElementById("action_tag_"+tag)!=undefined) document.getElementById("action_tag_"+tag).innerHTML='<center><img onmouseover="CometControlsTip(\''+placemarkList[tag].link_label+'\')" onmouseout="CometControlsUnTip()" onmousedown="'+placemarkList[tag].link_action+'" src="images/icons/play-icon.png" width=32 height=32></center>';

	placemarkList[tag].playing=false;


}

function CometControlsWordCloudUpBTN() {
	if(wc_history_index<2) return;


	CometControlsLog("Current wc: "+document.getElementById("wc_file_path").value);
	CometControlsLog("which is the same as: "+wc_history[wc_history_index-1]);
	CometControlsLog("want to load one prior to that: "+wc_history[wc_history_index-2]);


	//document.getElementById("wc_file_path").value=wc_history[wc_history_index];
	var temp=wc_history[wc_history_index-2];
	wc_history_index-=2;

	CometControlsLoadWordCloudByPath(temp);

}




function CometControlsLoadWordCloudByPath(urlName) {
	CometControlsLog("\tBEGIN CometControlsLoadWordCloudByPath("+urlName+") - action");
	
	if(wc_load_in_progress) {
		CometControlsLog("wc load already in progress or prior load crashed");
		return;
	}
	
//	if(path==document.getElementById("wc_file_path").value) {
//		cTabFliptoPage("RHS",page_number_wordcloud);
//		CometControlsLog("rejecting reload of duplicate path: "+path);
//		return;
//	}

	CometControlsLog("history::: (before)");
	for(var i=0; i<wc_history_index; i++) {
		CometControlsLog("\thistory["+i+"]="+wc_history[i]);
	}
	
	
	//document.getElementById("wc_file_path").value=path;
	//document.getElementById("xml_tag").value=tag;
	
	var request = getRequestObject();
	var params="path="+urlName; 
	wc_clicks++;
	//global_tag=tag;
	request.urlName=urlName
	request.onreadystatechange = function() {
		if ((request.readyState == 4) && (request.status == 200) && request.responseText!="") {
			CometControlsLog("CometControlsLoadWordCloudByPath("+request.urlName+") = received response");
			//cTabFliptoPage("RHS",page_number_wordcloud);
			
			
			//$('#myCanvas').tagcanvas("pause");
			document.getElementById("wc_file_path").value=request.urlName;
			
			
			if(wc_history_index!=0 && wc_history[wc_history_index-1]==document.getElementById("wc_file_path").value) {
				
				//wc_history_index--;
				return;
			}
			
			wc_history[wc_history_index]=document.getElementById("wc_file_path").value;
			wc_history_index++;
			CometControlsLog("history::: (after)");
			for(var i=0; i<wc_history_index; i++) {
				CometControlsLog("\thistory["+i+"]="+wc_history[i]);
			}
	
			document.getElementById("tags").innerHTML=request.responseText;
			
			CometControlsLog("sending update to myCanvas...");
			$('#myCanvas').tagcanvas("reload");
			$('#myCanvas').tagcanvas("update");
			CometControlsLog("wc_clicks="+wc_clicks);
			wc_load_in_progress=false;
		}
	};
	
	if(params!="") params="?"+params;

	request.open("get", 'Relay'+ params+"&type=object&stream", true);
	request.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
	request.send( null );
	CometControlsLog("END CometControlsLoadWordCloudByPath("+urlName+")");

}

function CometControlsLoadWordCloudByPathAutoLoad() {
	CometControlsLog("BEGIN WordCloud autoload ---- wc="+global_properties.wordCloud);

	var request = getRequestObject();

	CometStartAJAXAnimation();

	request.onreadystatechange = function() {
//		CometControlsLog("\treadyState="+request.readyState);
//		CometControlsLog("\tstatus="+request.status);
		

		//only care if there is a response
		if ((request.readyState == 4) && (request.status == 200)) {
			CometControlsLog("CCLWCPA:: received response"+request.responseText);
	
	
			CometStopAJAXAnimation();
			var file_props=JSON.parse(request.responseText);
			if(file_props.exists) {
				CometControlsLog("word cloud exists! -- continue with load");
				CometControlsLoadWordCloudByPath(global_properties.wordCloud);
			} else {	
				CometControlsLog("path "+file_props.path+" does not exist");
				CometControlsLog("\tshould hide word cloud tab");
				document.getElementById("RHS_tabHeader_3").style.display="none";

			}
		} //request is valid
	} //anon function: ready state change
	
	var url="Relay?path="+global_properties.wordCloud+"&type=exists";

	request.open("GET", url, true);

	request.send(null);
	
}

function CometControlsPullWCFromHCPBTN() {
	var path=document.getElementById("wc_file_path").value;
	if(path.contains("http")) {
		CometControlsLog("path shouldn't include http");
		 path=CometToolTipBuilderUrl2Path(path);
	}	 
	
	CometControlsLoadWordCloudByPath(path);
}


// make a light box with username=comet, password=who cares and select box with roles, then store the chosen role in memory

function CometControlsShowLoginPane() {
	CometControlsLog("BEGIN:::: CometControlsInitRoles()");	
	
	//show login pane
//	document.getElementById("loginContent").style.display="block";

	//hide only the search, config, path editor and results tabs on the LHS, page flip to login
	
	document.getElementById("LHS_tabHeader_1").style.display="none";
	document.getElementById("LHS_tabHeader_2").style.display="none";
	document.getElementById("LHS_tabHeader_3").style.display="none";
	document.getElementById("LHS_tabHeader_4").style.display="none";
	document.getElementById("LHS_tabHeader_5").style.display="block";
	
	cTabFliptoPage( "LHS", 5 );

	
//	document.getElementById("LHS_tabContainer").style.display="none";
//	document.getElementById("RHS_tabContainer").style.display="none";
	
	CometStartAJAXAnimation();
	
	var request = getRequestObject();
	request.onreadystatechange = function() {
		if ((request.readyState == 4) && (request.status == 200) && (request.responseText!="")) {
			CometControlsLog("==========loaded json============");	
			
			CometStopAJAXAnimation();

			global_properties.rolelist=JSON.parse(request.responseText);
			
			//CometControlsLog("rolelist length = "+global_properties.rolelist.length);
			//CometControlsLog("role of object 0: ");
			//CometControlsLog("\t"+global_properties.rolelist[0].role);
			
			var content;
			for(var i=0; i<global_properties.rolelist.length; i++) {
				content+="<option value=\""+global_properties.rolelist[i].role+"\">"+global_properties.rolelist[i].role+"</option>";
			}
			
			document.getElementById("role_selector").innerHTML=content;
		}
	}
	request.open("GET", "Relay?type=logs&annotation=roles", true);
	request.send(null);

}

function CometControlsLoadAnnotations_GenerateTabs(xmlDoc, tag) {
	CometControlsLog("BEGIN Generate Tabs");
	//generate the tabs 

	//<div id="editor_body">Loading...</div>
	content="<div id=\"annotations_tabContainer\" style=\"height: 400px;\">";
	content+="<div class=\"tabs\"><ul>";
	
	content+="<li id=\"annotations_tabHeader_default\">Default</li>";
	
	
	var x=xmlDoc.getElementsByTagName("annotation");
	var num=xmlDoc.getElementsByTagName("number_of_annotations")[0].childNodes[0].nodeValue;

	var real_count=0;
	for(var i=0; i<num; i++) {
		if(x[i] && x[i].getAttribute("name")) {
			real_count++;
			
		content+=
		"<li id=\"annotations_tabHeader_"
		+x[i].getAttribute("name")
		+"\">"
		+x[i].getAttribute("name")
		+"</li>";
		}
		
	}
	if(num!=real_count) CometControlsLog("WARNING: real_count!=num; correcting");
	num=real_count;

	content+="</ul></div> <!-- annotations tabs -->";

	var xml_ta_attributes="style=\"height: 580px; width: 600px \" white-space:nowrap overflow:auto\" wrap=\"off\" rows=\"36\" cols=\"80\"";


//	content+="<div class=\"tabscontent\" style=\"height: 560px; width: 640px \" align=\"center\"><div class=\"tabpage\" id=\"annotations_tabpage_default\">";
	content+="<div class=\"tabscontent\" style=\"height: 590px; width: 640px \" align=\"center\">" +
			"<div class=\"tabpage\" id=\"annotations_tabpage_default\">";
	content+="<textarea id=\"xmlcontent_default\"  readonly name=\"xmlcontent_default\" "+xml_ta_attributes+"></textarea>";
	
		content+='<table>'+
		'<tr width="590" align=left>'+
		'<td><input type="button" id="new_annotation_'+i+'" onclick="CometCreateAnnotation(\''+tag+'\',\'new_annotation_text'+i+'\')" value="New" /><input type="text" value="newannotation" id="new_annotation_text'+i+'" size="20"/>|</td>'+
		
		'<td><input type="button" id="rename_annotation_'+i+'" onclick="CometRenameAnnotation(\''+tag+'\',\'default\',\'rename_annotation_text'+i+'\')" value="Rename" disabled/><input type="text" value="default" id="rename_annotation_text'+i+'" size="20" readonly />|</td>' +
		'<td><input type="button" id="del_annotation_'+i+'" onclick="CometDeleteAnnotation(\''+tag+'\',\'default\')" value="Delete" disabled /></td>'+
		
		
		'</tr>'+
		'</table>';

	content+="</div><!-- end of tab page -->";


	var j=0;
	for(var i=0; i<num; i++) {
		j=i+1;
		content+='<div class="tabpage" id="annotations_tabpage_'+x[i].getAttribute("name")+'">';
		
		content+='<center><textarea id="xmlcontent_'
		+x[i].getAttribute("name")
		+'" name="xmlcontent_'
		+x[i].getAttribute("name")
		+'"  '+xml_ta_attributes+'></textarea></center>';
		
		content+='<table>'+
		'<tr width="590" align=left>'+
		'<td><input type="button" id="new_annotation_'+i+'" onclick="CometCreateAnnotation(\''+tag+'\',\'new_annotation_text'+i+'\')" value="New" /><input type="text" value="newannotation" id="new_annotation_text'+i+'" size="20"/>|</td>'+
		
		'<td>'+
			'<input type="button" id="rename_annotation_'+i+'" onclick="CometRenameAnnotation(\''+tag+'\',\''+x[i].getAttribute("name")+'\',\'rename_annotation_text'+i+'\')" value="Rename" />' +
			'<input type="text" value="'+x[i].getAttribute("name")+'" id="rename_annotation_text'+i+'" size="20"/>|' +
		'</td>' +
		'<td><input type="button" id="del_annotation_'+i+'" onclick="CometDeleteAnnotation(\''+tag+'\',\''+x[i].getAttribute("name")+'\')" value="Delete" /></td>'+

		'</tr>'+
		'</table>';
		
		
		content+='</div><!-- end of tab page -->';
	}

	content+="</div><!--end of tab content -->";
	content+="</div><!--end of tab container -->";

	document.getElementById("editor_body").innerHTML=content;


	CometControlsLog("BEGIN"+content+"END");



	//initialize the tabs
	InitTabs("annotations");
	
	
	CometControlsLog("END Generate Tabs");

}

function CometControlsLoadSpecificAnnotationToTextArea(tag, annotation, text_area_id) {
	CometControlsLog("LSAtotextarea("+placemarkList[tag].mdm.utf8name+","+annotation+")");

	var request = getRequestObject();
	var params="?path="+placemarkList[tag].mdm.urlName+"&type=custom-metadata&annotation="+annotation+"&stream"; 

	CometStartAJAXAnimation();


	request.onreadystatechange = function() {
		if ((request.readyState == 4) && (request.status == 200)) {

			CometControlsLog("received annotation from relay");		
			CometControlsLog(request.response);
			document.getElementById(text_area_id).innerHTML=request.response;
			CometStopAJAXAnimation();
		}
	}	
	//if(params!="") params="?"+params;
//	request.open("get", 'MetadataModifier'+ params, true);


	CometControlsLog("exec: Relay"+ params);

	request.open("get", 'Relay'+ params, true);
	request.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
	request.send( null );
}


function CometControlsLoadAnnotations_internal(tag) {
	CometControlsLog("BEGIN LoadAnnotations_internal("+tag+")");
	
	if(tag!=-1 && placemarkList[tag].mdm.annotation_size==0) {
		CometControlsLog("Object has no metadata");
		
		UpdateStatusMessage("Object has no metadata");		
		var i="def";
		content='<table>'+
		'<tr width="590" align=left>'+
		'<td><input type="button" id="new_annotation_'+i+'" onclick="CometCreateAnnotation(\''+tag+'\',\'new_annotation_text'+i+'\')" value="New" /><input type="text" value="newannotation" id="new_annotation_text'+i+'" size="20"/></td>'+
		
		'<td><input type="button" id="rename_annotation_'+i+'" onclick="CometRenameAnnotation(\''+tag+'\',\'default\',\'rename_annotation_text'+i+'\')" value="Rename" disabled/><input type="text" value="default" id="rename_annotation_text'+i+'" size="20" readonly /></td>' +
		'<td><input type="button" id="del_annotation_'+i+'" onclick="CometDeleteAnnotation(\''+tag+'\',\'default\')" value="Delete" disabled /></td>'+
		
		
		'</tr>'+
		'</table>';
		document.getElementById("editor_back_field").style.backgroundColor="white";
		document.getElementById("editor_body").innerHTML=content;
		return;
	}
	CometControlsLog("\t ==== annotation size="+placemarkList[tag].mdm.annotation_size);
	
//	var xml_file_path="xml_file_path2";
//	var xml_tag="xml_tag2";


	document.getElementById("xml_file_path").value=placemarkList[tag].mdm.utf8name;
	document.getElementById("xml_tag").value=tag;
	
	var request = getRequestObject();
	var params="path="+placemarkList[tag].mdm.urlName+"&type=custom-metadata&annotation=default&stream"; 
	
	global_tag=tag;
	request.onreadystatechange = function() {
		if ((request.readyState == 4) && (request.status == 200)) {
			tag=document.getElementById("xml_tag").value;
			
			CometControlsLog("BEGIN LoadAnnotations_internal("+tag+","+placemarkList[tag].mdm.utf8name+") --- beginning of request");



			var path=placemarkList[tag].mdm.urlName;

//			document.getElementById("xmlcontent").value=request.responseText;
			//CometControlsLog(request.responseText);
			
			
			if(request.responseText=="") {
				CometControlsLog("\tmissing reponse text!");
			}
			//default annotation contains all other annotations, we just need to walk it
			parser=new DOMParser();
 			xmlDoc=parser.parseFromString(request.responseText,"text/xml");
			

			if(xmlDoc==null) {
			
				CometControlsLog("\txmlDoc is null?");
			}
			
			var x=xmlDoc.getElementsByTagName("annotation");
			
			if(x==null) {
				CometControlsLog("\tmissing annotation element");
			} else {
				CometControlsLog("\tx="+x);
			}	
			
/*			CometControlsLog("\tnumber of annotations: "+xmlDoc.getElementsByTagName("number_of_annotations"));
			CometControlsLog("\tnumber of annotations: (length) "+xmlDoc.getElementsByTagName("number_of_annotations").length);
			CometControlsLog("\tnumber of annotations: [0] length"+xmlDoc.getElementsByTagName("number_of_annotations")[0].length);
			CometControlsLog("\tnumber of annotations: [0] childNode"+xmlDoc.getElementsByTagName("number_of_annotations")[0].childNodes);
			CometControlsLog("\tnumber of annotations: [0] childNode length "+xmlDoc.getElementsByTagName("number_of_annotations")[0].childNodes.length);
			CometControlsLog("\tnumber of annotations: [0] childNode [0] "+xmlDoc.getElementsByTagName("number_of_annotations")[0].childNodes[0]);
			CometControlsLog("\tnumber of annotations: [0] childNode [0] length"+xmlDoc.getElementsByTagName("number_of_annotations")[0].childNodes[0].length);
			CometControlsLog("\tnumber of annotations: [0] childNode [0] nodeValue"+xmlDoc.getElementsByTagName("number_of_annotations")[0].childNodes[0].nodeValue);

			CometControlsLog("\tabout to fail");
			
*/			
			var num=xmlDoc.getElementsByTagName("number_of_annotations")[0].childNodes[0].nodeValue;
			
			CometControlsLog("there are "+num+" annotations for this object");
			
			
			delete global_annotations_list;
			
			global_annotations_list=new Array();
			
			for(var i=0; i<num; i++) {
				if(!x[i] || !x[i].getAttribute("name")) global_annotations_num--;
				else {
					CometControlsLog("annotation #"+i+" "+x[i].getAttribute("name"));
					global_annotations_list[i]=x[i].getAttribute("name");	
				}
			}
//			CometControlsLog("(BEFORE) about to generate tabs for "+tag);
			CometControlsLoadAnnotations_GenerateTabs(xmlDoc, tag);
//			CometControlsLog("(AFTER) about to generate tabs for "+tag);
			
			
			document.getElementById("xmlcontent_default").innerHTML=request.responseText;
			
			
			for(var i=0; i<num; i++) {
			CometControlsLog("loading specific annotation ("+i+") "+x[i].getAttribute("name")+" for file "+placemarkList[tag].mdm.utf8name+" to textarea id: "+"xmlcontent_"+x[i].getAttribute("name"));
			CometControlsLoadSpecificAnnotationToTextArea(tag, x[i].getAttribute("name"), "xmlcontent_"+x[i].getAttribute("name"));
			}
		
			
			var splits=new Array();
			
			
			CometControlsLog("(Before infraction) xml_file_path="+xml_file_path);
			
			splits=document.getElementById("xml_file_path").value.split('/');
			var tag=CometControlsRLU(splits[splits.length-1]);
			document.getElementById("xml_tag").value=tag;
			if(tag!=-1) {
				//CometToolTipBuilderReloadSingleMetadata(tag);
				
				
				CometToolTipBuilderMetadataMapToPlacemarkObject(placemarkList[tag].mdm)
				
				//CometControlsLog("=== tool tip should be correct at this point, but it is not ===");
			}
			
			CometControlsLog("END LoadAnnotations_internal("+tag+","+path+") --- end of request");

		}
	};
	if(params!="") params="?"+params;
//	request.open("get", 'MetadataModifier'+ params, true);
	request.open("get", 'Relay'+ params, true);
	request.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
	request.send( null );
	CometControlsLog("END LoadAnnotations_internal("+tag+","+placemarkList[tag].mdm.utf8name+")");

}

function CometControlsLoadAnnotationsByRLU(utf8name) {
	CometControlsLog("BEGIN LoadAnnotationsByRLU("+utf8name+")");
	var splits=new Array();
	splits=utf8name.split('/');
	CometControlsLog("get tag of "+splits[splits.length-1]);
	var tag=CometControlsRLU(splits[splits.length-1]);
	CometControlsLoadAnnotations_internal(tag);
	CometControlsLog("END LoadAnnotationsByRLU("+utf8name+")");
}

function CometControlsLoadAnnotationsFromHCPBTN() {
	//step one, identify, what we're loading
	var path=document.getElementById("xml_file_path").value
	if(path.contains("http")) path=CometToolTipBuilderUrl2Path(path);
	document.getElementById("xml_file_path").value=path;
	CometControlsLoadAnnotationsByRLU(path);
}

function CometControlsSaveAnnotations_internal(tag) {
	
	CometControlsLog("==== CometControlsSaveAnnotations_internal BEGIN ====");
	CometControlsLoadAnnotations_internal(tag);
	if(tag!=-1) {
		CometToolTipBuilderReloadSingleMetadata(tag);
		CometControlsLog("---->>>tooltip should be correct here: "+placemarkList[tag].tooltip_contents+", but it is not");
		//CometControlsReloadVidCatLoop(tag);
	}
	CometControlsLog("==== CometControlsPushToHCPBTN_internal END ====");
}

function CometControlsSaveAnnotationsToHCPBTN() {
	CometControlsLog("BEGIN Save annotations to HCP");
	CometControlsLog("want to save to path = "+document.getElementById("xml_file_path").value);
	CometControlsLog("want to save tag = "+document.getElementById("xml_tag").value);
	//determine annotations list to save to
	var tag=document.getElementById("xml_tag").value;
	CometControlsLog("annotation CSV: "+global_annotations_list.join(","));
	var form = document.createElement('form');
	form.setAttribute('id', 'pushForm');
	form.setAttribute('name', 'pushForm');
	document.body.appendChild(form);
	var input;
	input = document.createElement('input');
	input.setAttribute('type', 'hidden');
	input.setAttribute('name', 'filename');
	input.setAttribute('value',  document.getElementById("xml_file_path").value );
	form.appendChild( input );
	for(var i=0; i<global_annotations_list.length; i++) {
		CometControlsLog("posting annotation: "+global_annotations_list[i]);
		CometControlsLog("\tposting content: "+document.getElementById('xmlcontent_'+global_annotations_list[i]).value);
		input = document.createElement('input');
		input.setAttribute('type', 'hidden');
		input.setAttribute('name', 'xmlcontent_'+global_annotations_list[i]);
		input.setAttribute('value', document.getElementById('xmlcontent_'+global_annotations_list[i]).value);
		form.appendChild( input );
	}
	CometControlsLog("posting annotation: default");
	CometControlsLog("\tposting content: default"+document.getElementById('xmlcontent_default'));
	input = document.createElement('input');
	input.setAttribute('type', 'hidden');
	input.setAttribute('name', 'xmlcontent_default');
	input.setAttribute('value', document.getElementById('xmlcontent_default').innerHTML);
	form.appendChild( input );
	input.setAttribute('type', 'hidden');
	input.setAttribute('name', 'annotation_hint');
	input.setAttribute('value', global_annotations_list.join(","));
	form.appendChild( input );
	form.setAttribute('action', 'MetadataModifier');
	form.setAttribute('method', 'post');
	form.setAttribute('target', 'iframe');
	form.submit();
	global_tag=tag;
	//still needed?
	setTimeout(function() { CometControlsSaveAnnotations_internal(global_tag); },3000); 
	CometControlsLog("END Save annotations to HCP");
	UpdateStatusMessage("Annotations submitted");
}


function CometControlsDisplayPath(path, num) {
	CometControlsLog("displaying path="+path);
	var shorty="";
	if(path.length<33) shorty=path;
	else shorty=path.substring(0,num)+"..."+path.substring(path.length-num,path.length);
	
	return "<span onmouseover=\"CometControlsTip(\'"+path+"\')\" onmouseout=\"CometControlsUnTip()\">"+shorty+"</span>";
}

function CometControlsCoordinateDropDownBtn() {
	CometControlsLog("BEGIN CometControlsCoordinateDropDownBtn()");
	var rc="row_closed";
	var ro="row_open";
	
	if(document.getElementById(rc).style.display!="none") {
		CometControlsLog("coordinates are closed let's change closed to none");
		document.getElementById(rc).style.dipslay="none";
		coordinate_closed=false;
	} else {
		CometControlsLog("coordinates are open let's change closed to block");
		document.getElementById(rc).style.dipslay="block";
		coordinate_closed=true;
	}
	

}

function CometControlsInitCalendar() {
	document.getElementById("cal_drop_content").style.display="none";
	
	$(function() {
		$( "#start_date" ).datepicker({
			defaultDate: "+1w",
			changeMonth: true,
			numberOfMonths: 3,
			onClose: function( selectedDate ) {
				$( "#to" ).datepicker( "option", "minDate", selectedDate );
			}
		});
		$( "#end_date" ).datepicker({
			defaultDate: "+1w",
			changeMonth: true,
			numberOfMonths: 3,
			onClose: function( selectedDate ) {
				$( "#from" ).datepicker( "option", "maxDate", selectedDate );
			}
		});
	});

	$(function() {
	// run the currently selected effect
		function runEffect() {
			calendar_shown=!calendar_shown;
			CometControlsLog("calendar shown="+calendar_shown);
		// run the effect
			$( "#cal_drop_content" ).toggle( "blind", {}, 200 );
		};
		// set effect from select menu value
		$( "#cal_drop" ).click(function() {
			runEffect();
			return false;
		});
	});
	//$( "#cal_drop_content" ).toggle( "blind", {}, 200 );

}

function CometControlsInitCoordinates() {
	document.getElementById("coordinate_drop_content").style.display="none";
	$(function() {
	// run the currently selected effect
		function runEffect() {
			coordinates_shown=!coordinates_shown;
			CometControlsLog("coordinates shown="+coordinates_shown);
		// run the effect
			$( "#coordinate_drop_content" ).toggle( "blind", {}, 200 );
		};
		// set effect from select menu value
		$( "#coordinate_drop" ).click(function() {
			runEffect();
			return false;
		});
	});
	//$( "#cal_drop_content" ).toggle( "blind", {}, 200 );
}

function LoadFromDescription(tag) {
	CometControlsLog("BEGIN LoadFromDescription");

	CometControlsLog("\twant to load description for tag="+tag);
	global_tag=tag;
	var request=getRequestObject();	

	request.onreadystatechange = function() {
		if ((request.readyState == 4) && (request.status == 200) && request.responseText!="") {
			var tag=global_tag;
			CometControlsLog("LoadFromDescription(...) received response");
			var temp_contents=""; //'<tr><td colspan=2 valign=top height=40 ><b>Path: </b><input value="'+CometToolTipBuilderUrl2Path(placemarkList[tag].url)+'" style="width: 700px"/></td></tr>';
			temp_contents+=request.responseText;
			temp_contents+='<span id="mp3_player"></span>';
			temp_contents+='</td></tr>';
			document.getElementById('object_info').innerHTML=temp_contents;
			
			CometControlsLog("running reflect on img#thumbnail_id_"+(Number(tag)+1));
			
			$("img#thumbnail_id_"+(Number(tag)+1)).reflect();
			if(placemarkList[tag].dont_autoplay) {
				CometControlsLog("\ndo not Autoplay object\n");
			} else {
				CometControlsPlayAction(tag);
			}
			CometControlsLog("LoadFromDescription -- Complete");
		}
	};
	var params="?path="+placemarkList[tag].mdm.urlName;

	var classification="";
	if(global_properties.showCaveats) {
		classification="&showclass";
	}
	request.open("get", 'Relay'+ params+"&type=generated&annotation=description&stream&astext&size=300x450&tag="+(Number(tag)+1)+classification, true);
	request.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
	request.send( null );


	CometControlsLog("END LoadFromDescription");
}


//Create a new annotation for object represented by tag using text from the input field newannotation_text_id
function CometCreateAnnotation(tag, newannotation_text_id) {
	CometControlsLog("--->want to create a new annotation for tag="+tag+" and id="+newannotation_text_id+"!");
	
	if(document.getElementById(newannotation_text_id).value.contains("_") ||  document.getElementById(newannotation_text_id).value.contains(" ")) {
		UpdateStatusMessage("Error: Annotation must be alpha numeric only");
		return;
	}
	CometStartAJAXAnimation();

	
	
	
	var params="?path="+placemarkList[tag].mdm.urlName+"&type=create&annotation="+document.getElementById(newannotation_text_id).value+"&stream&recombine";
	CometControlsLog("using params="+params);
	
	global_tag=tag;
	var request = getRequestObject();
	request.onreadystatechange = function() {
		if ((request.readyState == 4) && (request.status == 200)) {
			tag=global_tag;
			
			placemarkList[tag].mdm.annotation_size++;
			
			CometControlsEditMetadata(tag);
			CometStopAJAXAnimation();
			
			
			
		}
	}	
	request.open("get", 'Relay'+params, true);
	request.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
	request.send( null );
}

//Rename an annotation for object represented by tag using text from the input field rename_annotation_text_id
function CometRenameAnnotation(tag, old_annotation, rename_annotation_text_id) {
	CometControlsLog("CometRenameAnnotation("+tag+", "+old_annotation+", "+rename_annotation_text_id+")");
	
	if(document.getElementById(rename_annotation_text_id).value.contains("_") ||  document.getElementById(rename_annotation_text_id).value.contains(" ")) {
		UpdateStatusMessage("Error: Annotation must be alpha numeric only");
		return;
	}


	if(document.getElementById(rename_annotation_text_id).value==old_annotation) {
		UpdateStatusMessage("Error: New annotation must differ from original name");
		return;
	}


	CometStartAJAXAnimation();

	
	CometControlsLog("\t\tannotation="+old_annotation);
	CometControlsLog("\t\t(new)annotation="+document.getElementById(rename_annotation_text_id).value);
	
	var params="?path="+placemarkList[tag].mdm.urlName+"&type=rename&annotation="+old_annotation+"&saveas_annotation="+document.getElementById(rename_annotation_text_id).value+"&stream&recombine";
	var request = getRequestObject();
	request.tag=tag;
	request.onreadystatechange = function() {
		if ((request.readyState == 4) && (request.status == 200)) {
			CometControlsEditMetadata(request.tag);
			CometStopAJAXAnimation();
		}
	}	
	//if(params!="") params="?"+params;
	request.open("get", 'Relay'+params, true);
	request.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
	request.send( null );

}

//Delete the annotation for the object pointed to by this tag
function CometDeleteAnnotation(tag, annotation_to_delete) {
	CometControlsLog("--->want to delete an annotation for tag="+tag+"!");
	var params="?path="+placemarkList[tag].mdm.urlName+"&type=delete&annotation="+annotation_to_delete+"&stream&recombine";
	CometControlsLog("using url="+params);
	
	CometStartAJAXAnimation();

	global_tag=tag;
	var request = getRequestObject();
	request.tag=tag;
	request.onreadystatechange = function() {
		if ((request.readyState == 4) && (request.status == 200)) {
			CometControlsEditMetadata(request.tag);
			CometStopAJAXAnimation();
		}
	}	
	//if(params!="") params="?"+params;
	request.open("get", 'Relay'+params, true);
	request.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
	request.send( null );
}

function CometLoadPage(newpage) {
	window.location.href=global_properties.imageprefix+newpage;
}

function CometHasContent(content) {
	return (content!=undefined && content!="");
}

function CometControlsLinkAction(tag) {
	CometControlsLog("BEGIN CometControlsLinkAction for tag="+tag);
	var result="";
	
	if(!CometHasContent(placemarkList[tag].mdm.link_action)) return result;
	
	if(placemarkList[tag].mdm.link_action.contains("http") || 
			(placemarkList[tag].mdm.link_action.contains("javascript") && placemarkList[tag].mdm.link_action.contains("("))) return placemarkList[tag].mdm.link_action;
	
	CometControlsLog("END CometControlsLinkAction for tag="+tag);
	return placemarkList[tag].mdm.link_action+"('"+tag+"')'";
}

function CometControlsClearAll() {
	CometGeo_ClearAll();
	for(var i=0; i<num_of_tags+1; i++) {
		if(placemarkList[i].geotagged && placemarkList[i].playing) {
			placemarkList[i].playing=false;
		
		}
	}
}

function CometControlsAddToForm(input_name, input_value ) {
	var input = document.createElement('input');
	input.setAttribute('type', 'hidden');
	input.setAttribute('name', input_name);
//	input.setAttribute('value',  encodeURIComponent(input_value));
	input.setAttribute('value', input_value);
	return input;
}

function CometControlsPushForm(form_name) {
	CometStartAJAXAnimation();
	var frm = $(form_name);
	frm.submit(function (ev) {
        	$.ajax({
        		type: frm.attr('method'),
			url: frm.attr('action'),
			data: frm.serialize(),
			success: function (data) {
				CometControlsLog("submitted");
				CometStopAJAXAnimation();
			}
		});
		ev.preventDefault();
	});

	$(form_name).trigger('submit');
}

/************** Path Editor Controls ***********************/

function CometControlsPathEdit_Commit() {
	CometControlsLog("BEGIN CometControlsPathEdit_Commit()=====");
	
	var prior_tag=document.getElementById("posmark_tag").value;




	if(prior_tag!=-1 && isNumber(prior_tag)) {
		$(xml_content.getElementById(prior_tag)).find('starttime').text(document.getElementById(cur_datetime_id).value);
		$(xml_content.getElementById(prior_tag)).find('longitude').text(document.getElementById(cur_longitude_id).value);
		$(xml_content.getElementById(prior_tag)).find('latitude').text(document.getElementById(cur_latitude_id).value);
	}

	var editing_tag=document.getElementById("editing_tag").value

	CometControlsLog("\tprior_tag="+prior_tag);

	CometControlsLog("\tediting_tag="+editing_tag);


	if(!placemarkList[editing_tag].has_kmlpath) {
		CometStartAJAXAnimation();
		//so ugly.. use a GET to create the annotation before committing the annotation
		
		CometControlsLog("\tmissing kml path, creating one");
		
		var requestINIT = getRequestObject();
		requestINIT.tag=editing_tag;
		requestINIT.onreadystatechange = function() {
			if ((requestINIT.readyState == 4) && (requestINIT.status == 200)) {

				CometControlsLog("\tcreated kml_path for tag="+requestINIT.tag+" path="+placemarkList[requestINIT.tag].mdm.utf8name);

				placemarkList[requestINIT.tag].mdm.annotation_size++;
				CometStopAJAXAnimation();

			
				//completed the annotation, try again
				placemarkList[requestINIT.tag].has_kmlpath=true;

				CometControlsLog("\tCalling commit a second time to save our changes to tag="+requestINIT.tag+" path="+placemarkList[requestINIT.tag].mdm.utf8name);

				CometControlsPathEdit_Commit();


			}
		}	
		requestINIT.open("get", "Relay?path="+placemarkList[editing_tag].mdm.urlName+"&type=create&annotation=path&stream&recombine", true);
		requestINIT.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
		requestINIT.send( null );
		
		
	} else {


		CometControlsLog("\tabout to submit commit for tag="+editing_tag);



	//reorder XML
	var counter=0;
	$(xml_content).find('position').each(function(){
		counter++;
		var pos_tag = this.getAttribute('id');
		CometControlsLog("id="+pos_tag);
		CometControlsLog("\t is now "+counter);
		this.setAttribute('id',counter);					
	});	

	//post
	var form = document.createElement('form');
	form.setAttribute('id', 'pathPushForm');
	form.setAttribute('name', 'pathPushForm');
	
	document.body.appendChild(form);
	form.appendChild(CometControlsAddToForm("content", new XMLSerializer().serializeToString(xml_content.documentElement)));
	
	//CometControlsLog("\tsubmitting content with length of "+xml_content.length);
	
	
	
//	form.appendChild(CometControlsAddToForm("path", document.getElementById("pe_path").value));
//	CometControlsLog("\tsubmitting content with path="+document.getElementById("pe_path").value);
	
//	form.appendChild(CometControlsAddToForm("type", "custom-metadata"));
//	CometControlsLog("\tsubmitting content with type=custom-metadata");
//	form.appendChild(CometControlsAddToForm("annotation", "path"));

	console.dirxml(xml_content);
	
	var non_xml_content=new XMLSerializer().serializeToString(xml_content.documentElement);
	CometControlsLog("content="+non_xml_content);
	

	form.setAttribute('action', 'Relay?path='+placemarkList[editing_tag].mdm.urlName+"&type=custom-metadata&annotation=path&content="+non_xml_content);
	
	CometControlsLog("\tmethod=put");
	form.setAttribute('method', 'put');
	CometControlsPushForm('#pathPushForm');
	
	UpdateStatusMessage("Path submitted");
	CometControlsLog("End of CometControlsPathEdit_Commit()");
	
	}
}

function CometControlsPathEdit_Add() {
	//determine tag to append, which is either 1 (xml needs to be built) or lastid+1
	var lastid=1;
	//brand new xml
	if($(xml_content).find('position').length==0) {
		xml_content='<?xml version="1.0" encoding="UTF-8" standalone="no"?><path><position id="1"><latitude>0.0</latitude><longitude>0.0</longitude><starttime>2014-01-01 00:00:00</starttime></position></path>';
		parser=new DOMParser();
		xml_content=parser.parseFromString(xml_content,"text/xml");
	} else {
		lastid=Number($(xml_content.getElementsByTagName('position')[$(xml_content).find('position').length-1]).attr("id"))+1;
		cloneme=xml_content.getElementsByTagName('position')[$(xml_content).find('position').length-1];
		newNode=cloneme.cloneNode(true);
		$(newNode).attr("id",lastid);
		$(newNode).find('starttime').text(CometControlsIncrementTime($(newNode).find('starttime').text(),1));
		xml_content.documentElement.appendChild(newNode);
	}
	CometControlsLog("id for new element is "+lastid);
	//now create new marker wherever the camera is looking
	
	CometGeo_createPlacemarkHere(path_mark_prefix+lastid, current_title+" at position "+lastid, global_properties.imageprefix+"images/gray-triangle.png");

	//now switch to it
	CometControlsPathEdit_onClick(path_mark_prefix+lastid);
	CometControlsPathEdit_onClick("-1");
	CometControlsPathEdit_onClick(path_mark_prefix+lastid);
}

function CometControlsPathEdit_Delete() {
	CometControlsLog("PE_Delete");
	var prior_tag=document.getElementById("posmark_tag").value;
	if($(xml_content).find('position').length==0 || prior_tag==-1) {
		CometControlsLog("nothing to delete, returning");
		return;
	}
	
	$(xml_content.getElementById(prior_tag)).remove();

	CometGeo_removePlacemark(path_mark_prefix+prior_tag);

	document.getElementById(cur_datetime_id).value="";
	document.getElementById(cur_longitude_id).value="";
	document.getElementById(cur_latitude_id).value="";
	document.getElementById("posmark_tag").value=-1;
}

function CometControlsPathEdit_Unload() {
	CometControlsLog("PE_Unload");
	
	$(xml_content).find('position').each(function(){
		counter++;
		var pos_tag = this.getAttribute('id');
		CometControlsLog("id="+pos_tag);
		CometControlsLog("\t is now "+counter);
		//this.setAttribute('id',counter);					
		
		CometGeo_removePlacemark(path_mark_prefix+pos_tag);
		
		//$(xml_content.getElementById(pos_tag)).remove();
	});	
	
	
	
	document.getElementById(cur_datetime_id).value="";
	document.getElementById(cur_longitude_id).value="";
	document.getElementById(cur_latitude_id).value="";
	document.getElementById("posmark_tag").value=-1;

	document.getElementById("pe_add").disabled=true;
	document.getElementById("pe_del").disabled=true;
	document.getElementById("pe_commit").disabled=true;
	document.getElementById("pe_unload").disabled=true;
	
	
	xml_content=[];
	cTabFliptoPage("LHS",4);
}



function CometControlsPathEdit_DateTimeChanged() {
	CometControlsLog("PE_DateTimeChanged");
	var prior_tag=document.getElementById("posmark_tag").value;
	if(prior_tag!=-1) $(xml_content.getElementById(prior_tag)).find('starttime').text(document.getElementById(cur_datetime_id).value);
}

function CometControlsPathEdit_onClick(new_tag) {
	CometControlsLog("===PE_onNewClick===");

	//new_tag goes from postag0 to 0
	
	if(new_tag!=-1) new_tag=Number(new_tag.substring(path_mark_prefix.length,new_tag.length));
	var prior_tag=document.getElementById("posmark_tag").value;

	if(!isNumber(new_tag)) new_tag=-1;

	CometControlsLog("old selection is "+prior_tag);
	CometControlsLog("new selection is "+new_tag);

	if(prior_tag==new_tag) {
		CometControlsLog("determined to be the same icon, no change.");
	} else { // something interesting happens
		CometControlsLog("user changed from "+prior_tag+" to "+new_tag);
		if(prior_tag==-1) {
			CometControlsLog("prior_tag was -1, do not save to xml");
		} else { // > -1, so save in xml and turn gray	
		
			//saved in xml
			$(xml_content.getElementById(prior_tag)).find('starttime').text(document.getElementById(cur_datetime_id).value);
			$(xml_content.getElementById(prior_tag)).find('longitude').text(document.getElementById(cur_longitude_id).value);
			$(xml_content.getElementById(prior_tag)).find('latitude').text(document.getElementById(cur_latitude_id).value);

			//turn gray
			CometControlsLog(path_mark_prefix+prior_tag+","+global_properties.imageprefix+"images/gray-triangle.png");
			CometGeo_changeIconTo(path_mark_prefix+prior_tag, global_properties.imageprefix+"images/gray-triangle.png");
		}
		
		//now that the prior_tag is done, do the opposite for new tag
		if(new_tag==-1) {
			//if new tag is -1, clear out the text boxes
		
			document.getElementById(cur_longitude_id).value=0.0;
			document.getElementById(cur_latitude_id).value=0.0;
			document.getElementById(cur_datetime_id).value="none";
			//disable datetimepicker temporarily
		} else {	
			//populate from xml
			document.getElementById(cur_datetime_id).value=$(xml_content.getElementById(new_tag)).find('starttime').text();
			document.getElementById(cur_longitude_id).value=$(xml_content.getElementById(new_tag)).find('longitude').text();
			document.getElementById(cur_latitude_id).value=$(xml_content.getElementById(new_tag)).find('latitude').text();
		
			CometControlsLog(path_mark_prefix+new_tag+","+global_properties.imageprefix+"images/blue-triangle.png");
			CometGeo_changeIconTo(path_mark_prefix+new_tag, global_properties.imageprefix+"images/blue-triangle.png");
		}
		//prior saved to XML, new settings in place, current points to new_tag
		document.getElementById("posmark_tag").value=new_tag;
		CometControlsLog("transformation complete ("+prior_tag+" to "+new_tag+")");
		//console.dirxml(xml_content);
	}
}

function CometControlsPathEdit(tag) {
	CometControlsLog("BEGIN PathEdit("+tag+")");
	
	document.getElementById("pe_add").disabled=false;
	document.getElementById("pe_del").disabled=false;
	document.getElementById("pe_commit").disabled=false;
	document.getElementById("pe_unload").disabled=false;
	
	
	//step 1, page flip
	cTabFliptoPage("LHS",3);
	
	if(global_properties.geo_enabled) cTabFliptoPage("RHS",page_number_geoviz);
	else return;
		
	//clear out existing arrays
	xml_content="";
	document.getElementById("pe_path").value=placemarkList[tag].mdm.utf8name;
	document.getElementById("editing_tag").value=tag;
	current_title=placemarkList[tag].mdm.desc;
	var url=global_properties.imageprefix+"Relay?path="+placemarkList[tag].mdm.urlName+"&type=custom-metadata&annotation=path&stream";
	CometControlsLog("want to load "+url);
	
	
	if(placemarkList[tag].has_kmlpath) {
		CometControlsLog("has kmlpath... loading");
	$.ajax({
		type: "GET",
		url: url,
		dataType: "xml",
		success: function(xml) {
			xml_content=xml;
			
			//walk the xml dom
			$(xml).find('position').each(function(){
				var pos_tag = this.getAttribute('id');
				CometControlsLog("id="+pos_tag);
				
				//dateList[pos_tag]=$(xml_content.getElementById(pos_tag)).find('starttime').text()
				CometGeo_createSimplePlacemark(path_mark_prefix+pos_tag,
					current_title+" at position "+pos_tag,
					global_properties.imageprefix+"images/gray-triangle.png",
					Number($(xml_content.getElementById(pos_tag)).find('latitude').text()),
					Number($(xml_content.getElementById(pos_tag)).find('longitude').text()));
			}); //end Jquery find
		} //end success function
	}); //end ajax get
	} else {
		CometControlsLog("no initial kml path.. have to make one");
		
		xml_content=jQuery.parseXML('<?xml version="1.0" encoding="UTF-8" standalone="no"?><path></path>');
		
		
		
	}	
	document.getElementById("path_editor_controls").style.display="block";
	document.getElementById("path_editor_message").style.display="none";
	
	$('input[name="pe_startdatetime"]').datetimepicker({
		defaultDate: "",
		changeMonth: true,
		numberOfMonths: 1,
		dateFormat: "yy-mm-dd",
		timeFormat: 'HH:mm:ss',
		stepHour: 1,
		stepMinute: 10,
		stepSecond: 10,
		onClose: function( selectedDate ) {
			$( "#to" ).datepicker( "option", "minDate", selectedDate );
		}
	});
	CometControlsLog("END PathEdit("+tag+")");
}

function CometControlsIncrementTime(time, hours) {
	var datepart=time.split(" ");
	var timepart=datepart[1].split(":");
	timepart[0]=Number(timepart[0])+1;
	if(timepart[0]>23) timepart[0]="0";
	time=datepart[0]+" "+timepart[0]+":"+timepart[1]+":"+timepart[2];
	return time;
}

function CometControlsPathEdit_onMouseClick(event) {
	if(!global_properties.geo_enabled) return;
	if (event.getTarget().getType() == 'KmlPlacemark' && event.getTarget().getGeometry().getType() == 'KmlPoint') {
		event.preventDefault();
		var placemark_id = event.getTarget();
		CometControlsLog("id="+event.getTarget().getId());
		CometControlsLog("click!");
		CometControlsPathEdit_onClick(event.getTarget().getId());
	}
}

function CometStartAJAXAnimation() {
	CometControlsLog("--> CometStartAJAX::: "+busyAnimRef+" tasks busy");
	busyAnimRef++;
	document.getElementById('loaderImage').innerHTML="<img src=images/ajax-loader.gif />";
}

function CometStopAJAXAnimation() {
	CometControlsLog("--> CometStopAJAX::: "+busyAnimRef+" tasks busy");
	busyAnimRef--;
	if(busyAnimRef<1) {
		//make the animation block hidden
		document.getElementById('loaderImage').innerHTML="";
		busyAnimRef=0;
	} else {
		CometControlsLog("--> dont stop animating, still have "+busyAnimRef+" tasks working");
	}
}

function CometRefreshLogs() {
	CometRefreshThisLog("gui","gui_log_content",10000);
	CometRefreshThisLog("ingest","ingestor_log_content",10000);
	CometRefreshThisLog("hosts","host_file_content",10000);
	CometRefreshThisLog("syslog","syslog_content",10000);
	CometRefreshThisLog("fstab","fstab_content");
}

function CometControlsAdmin(action,msg_tgt) {
	CometControlsHttpGetMessage("Admin?action="+action,msg_tgt)
}

function CometControlsAdminClear(msg_tgt) {
	document.getElementById(msg_tgt).innerHTML="";
}

function CometControlsLoadConfigurationFile() {
	CometRefreshThisLog("properties","configuration_file");
}


function CometControlsSaveConfigurationFile() {
	var form = document.createElement('form');
	form.setAttribute('id', 'pushForm');
	form.setAttribute('name', 'pushForm');
	document.body.appendChild(form);
	
	var sometext= document.getElementById("configuration_file").value;
	
	CometControlsLog("content of textarea: ");
	
	CometControlsLog(document.getElementById("configuration_file").innerHTML);
	CometControlsLog("========");
	CometControlsLog(document.getElementById("configuration_file").value);
	
	CometControlsLog("========");
	
	CometControlsLog(sometext);
	
	input = document.createElement('input');
	input.setAttribute('type', 'hidden');
	input.setAttribute('name', 'content');
	input.setAttribute('value', sometext);
	form.appendChild(  input );

	input = document.createElement('input');
	input.setAttribute('type', 'hidden');
	input.setAttribute('name', 'annotation');
	input.setAttribute('value', 'props');
	form.appendChild( input );


	input = document.createElement('input');
	input.setAttribute('type', 'hidden');
	input.setAttribute('name', 'type');
	input.setAttribute('value', 'logs');
	form.appendChild( input );

	form.setAttribute('action', 'Relay');
	form.setAttribute('method', 'post');

	form.setAttribute('target', 'iframe');
	form.submit();
	
	UpdateStatusMessage("Configuration file saved");
	
}


function CometControlsLoadRolesFile() {
	CometRefreshThisLog("roles","roles_file");
}

function CometControlsSaveRolesFile() {
	var form = document.createElement('form');
	form.setAttribute('id', 'pushForm');
	form.setAttribute('name', 'pushForm');
	document.body.appendChild(form);
	
	var sometext= document.getElementById("roles_file").value;
	
	CometControlsLog("content of textarea: ");
	
	CometControlsLog(document.getElementById("roles_file").innerHTML);
	CometControlsLog("========");
	CometControlsLog(document.getElementById("roles_file").value);
	
	CometControlsLog("========");
	
	CometControlsLog(sometext);
	
	input = document.createElement('input');
	input.setAttribute('type', 'hidden');
	input.setAttribute('name', 'content');
	input.setAttribute('value', sometext);
	form.appendChild(  input );

	input = document.createElement('input');
	input.setAttribute('type', 'hidden');
	input.setAttribute('name', 'annotation');
	input.setAttribute('value', 'roles');
	form.appendChild( input );


	input = document.createElement('input');
	input.setAttribute('type', 'hidden');
	input.setAttribute('name', 'type');
	input.setAttribute('value', 'logs');
	form.appendChild( input );

	form.setAttribute('action', 'Relay');
	form.setAttribute('method', 'post');

	form.setAttribute('target', 'iframe');
	form.submit();
	UpdateStatusMessage("Role file saved");

}


function CometControlsRequestReady(request) {
	return (request.readyState == 4) && (request.status == 200)
}

function CometControlsHttpGet(url) {
	var request = getRequestObject();
	CometStartAJAXAnimation();
	request.onreadystatechange = function() {
		if (CometControlsRequestReady(request)) {
			CometControlsLog("CometControlsHttpGet()--- request.onreadystatechange:::get here");
			CometStopAJAXAnimation();
		}
	}
	request.open("GET", url, true);
	request.send(null);
	return request;
}

function CometControlsHttpGetMessage(url,msg_tgt) {
	var request = getRequestObject();
	CometStartAJAXAnimation();
	request.tag=-1;
	request.msg_tgt=msg_tgt;
	request.onreadystatechange = function() {
		if (CometControlsRequestReady(request)) {
			//comment out when fixing admin_status_message
//			if(!request.msg_tgt) request.msg_tgt="admin_status_message";
			var oldcontent=document.getElementById(request.msg_tgt).innerHTML;
			
			
			document.getElementById(request.msg_tgt).innerHTML=oldcontent+"\n\n"+request.responseText;
			CometStopAJAXAnimation();
		}
	}
	request.open("GET", url, true);
	request.send(null);
	return request;
}

function CometRefreshThisLog(logname, tgt, tail) {
	url="Relay?type=logs&annotation="+logname;
	if(tail) url+="&tail="+tail;
	url+="&stream";
	CometControlsLog("refreshing log for url="+url);
	var request=CometControlsHttpGet(url);
	request.tgt=tgt;
	request.logname=logname;
	request.onreadystatechange = function() {
		if ((request.readyState == 4) && (request.status == 200)) {
			CometControlsLog("CometRrefreshThisLog:::::loading \""+request.logname+"\" content to text area \""+request.tgt+"\"");
			
			//CometControlsLog("---start-----");
			//CometControlsLog(request.responseText);
			//CometControlsLog("---end-----");
			
			
			document.getElementById(request.tgt).innerHTML=request.responseText;
			document.getElementById(request.tgt).value=request.responseText;
			
			
			
			CometStopAJAXAnimation();
		}
	}
}

function IsSearchLocked() {
	CometControlsLog("checking to see if search is locked..."+search_locked);
	return search_locked;
}

function LockSearch() {
	CometControlsLog("Locking search...");
	search_locked=true;
}

function UnLockSearch() {
	CometControlsLog("Unlocking search...");
	search_locked=false;	
}	

function UpdateStatusMessage(msg) {
	document.getElementById("msg").innerHTML=msg
	setTimeout(function() { document.getElementById("msg").innerHTML=""; },10000);	
}

function CometControlsCheckHCP() {
	var request=getRequestObject();
	CometStartAJAXAnimation();
	request.onreadystatechange = function() {
		//only care if there is a response
		if ((request.readyState == 4) && (request.status == 200) && (request.responseText!="")) {
			hcp_status=JSON.parse(request.responseText);
			document.getElementById("hcp_check").innerHTML=hcp_status.hcp_status;
			CometStopAJAXAnimation();
		}
	}	
	request.open("GET", "Admin?action=hcpcheck", true);
	request.send(null);
}


//run a servlet of the form servlet?type=action&annotation=annotation
//intended for use with raw HTML
function OpenBrowserTab(servlet,action,annotation) {
	window.open(servlet+'?type='+action+'&annotation='+annotation);
}


function CometControlsStartScene3(fname) {
	if(fname==null) return;
	setTimeout(function() {
		CometControlsLog("start CometControlsStartScene3("+fname+")");
		var tag=CometControlsRLU(fname);
		if(placemarkList[tag]==null) return;
		if(placemarkList[tag].geotagged) {
			placemarkList[tag].flownin=true;
			CometGeo_Fly(tag,false);
			
			
			
			CometControlsLog("tag="+tag+" fly_balloon_delay="+fly_balloon_delay);
			//error occurs in one of these next two lines:
			
			
			
			setTimeout(function() { 
				CometGeo_Fly(tag,false); 
				CometGeo_ToggleBalloon(tag);
			}, fly_balloon_delay );
		} else {
			CometControlsLog("unable to start scene for non-geotagged item");
		}
		CometControlsLog("end CometControlsStartScene3("+fname+")");
	},2000);
}

function CometControlsDeleteObject(tag) {
	//want to delete the object pointed to by tag
	var request=getRequestObject();
	CometStartAJAXAnimation();
	request.onreadystatechange = function() {
		//only care if there is a response
		if ((request.readyState == 4) && (request.status == 200) && (request.responseText!="")) {
			UpdateStatusMessage(request.responseText);
			CometStopAJAXAnimation();
			//kick off new search??  delete object from array???
			CometControlsLog("clear datatable");
			$('#dataTable').dataTable().fnClearTable();
	
			CometControlsLog("clear download array");
			filesToDownload = [];
			
			//remove or mark deleted
			

			for(var i=0; i< global_properties.cached_search_results.length; i++) {
				if(global_properties.cached_search_results[i] && global_properties.cached_search_results[i].path==placemarkList[tag].mdm.utf8name) {
					global_properties.cached_search_results[i]=null;
				}
			}
			
			
			CometSearchResultsAddRows("dataTable.tableBody");
			
			
		}
	}	
	request.open("DELETE", "Relay?path="+placemarkList[tag].mdm.urlName, true);
	request.send(null);
}


function CometControls_CheckLoadables() {
	if(loadables.geo && loadables.wc && loadables.ingest && loadables.admin && loadables.ext) {
		document.getElementById("login_username").disabled=false;
		document.getElementById("login_password").disabled=false;
		document.getElementById("role_selector").disabled=false;
		document.getElementById("loginBtn").disabled=false;
		document.getElementById("loading_msg").innerHTML="Loading Complete";
	} else {
		setTimeout(function() { CometControls_CheckLoadables(); }, 5000);
	
	}
}


/**************** BEGIN GEO EDIT and PATH EDIT functions *********************************/

function CometControlsGeoEdit_Save_Btn() {
	CometControlsLog("BEGIN CometControlsGeoEdit_Save()=====");
	

	var tag=document.getElementById("geo_editing_tag").value

	CometControlsLog("\tediting_tag="+tag);


	if(!placemarkList[tag].geotagged) {
		CometStartAJAXAnimation();
		//so ugly.. use a GET to create the annotation before committing the annotation
		
		CometControlsLog("\tmissing geo, creating one");
		
		var requestINIT = getRequestObject();
		requestINIT.tag=tag;
		requestINIT.onreadystatechange = function() {
			if ((requestINIT.readyState == 4) && (requestINIT.status == 200)) {

				CometControlsLog("\tcreated geo for tag="+requestINIT.tag+" path="+placemarkList[requestINIT.tag].mdm.utf8name);

				placemarkList[requestINIT.tag].mdm.annotation_size++;
				CometStopAJAXAnimation();

			
				//completed the annotation, try again
				placemarkList[requestINIT.tag].geotagged=true;

				CometControlsLog("\tCalling commit a second time to save our changes to tag="+requestINIT.tag+" path="+placemarkList[requestINIT.tag].mdm.utf8name);

				CometControlsGeoEdit_Save_Btn();


			}
		}	
		requestINIT.open("get", "Relay?path="+placemarkList[tag].mdm.urlName+"&type=create&annotation=geo&stream&recombine", true);
		requestINIT.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
		requestINIT.send( null );
		
		
	} else {
		CometControlsLog("\tabout to submit commit for tag="+tag);

		//reorder XML
		CometControlsLog("\tUpper Left Y (latitude): "+$(xml_content).find('CornerCoordinates corner[name="Upper Left"] Y').text());
		CometControlsLog("\tUpper Left X (longitude): "+$(xml_content).find('CornerCoordinates corner[name="Upper Left"] X').text());

		CometControlsLog("\tUpper Right Y (latitude): "+$(xml_content).find('CornerCoordinates corner[name="Upper Right"] Y').text());
		CometControlsLog("\tUpper Right X (longitude): "+$(xml_content).find('CornerCoordinates corner[name="Upper Right"] X').text());
			
		CometControlsLog("\tcenter Y (latitude): "+$(xml_content).find('CornerCoordinates corner[name=Center] Y').text());
		CometControlsLog("\tcenter X (longitude): "+$(xml_content).find('CornerCoordinates corner[name=Center] X').text());
			
		CometControlsLog("\tLower Left Y (latitude): "+$(xml_content).find('CornerCoordinates corner[name="Lower Left"] Y').text());
		CometControlsLog("\tLower Left X (longitude): "+$(xml_content).find('CornerCoordinates corner[name="Lower Left"] X').text());

		CometControlsLog("\tLower Right Y (latitude): "+$(xml_content).find('CornerCoordinates corner[name="Lower Right"] Y').text());
		CometControlsLog("\tLower Right X (longitude): "+$(xml_content).find('CornerCoordinates corner[name="Lower Right"] X').text());
	
		CometControlsGeoEdit_SaveXMLValue_Y("Upper Left","geo_nwlat")
		CometControlsGeoEdit_SaveXMLValue_X("Upper Left","geo_nwlong")
			

		CometControlsGeoEdit_SaveXMLValue_Y("Upper Right","geo_nelat")
		CometControlsGeoEdit_SaveXMLValue_X("Upper Right","geo_nelong")
	

		CometControlsGeoEdit_SaveXMLValue_Y("Center","geo_latitude")
		CometControlsGeoEdit_SaveXMLValue_X("Center","geo_longitude")

		CometControlsGeoEdit_SaveXMLValue_Y("Lower Left","geo_swlat")
		CometControlsGeoEdit_SaveXMLValue_X("Lower Left","geo_swlong")
			

		CometControlsGeoEdit_SaveXMLValue_Y("Lower Right","geo_selat")
		CometControlsGeoEdit_SaveXMLValue_X("Lower Right","geo_selong")

		cDao_SaveAnnotation(new XMLSerializer().serializeToString(xml_content.documentElement), placemarkList[tag].mdm.urlName, "geo");
		
		placemarkList[tag].latitude=$(xml_content).find('CornerCoordinates corner[name=Center] Y').text();
		placemarkList[tag].longitude=$(xml_content).find('CornerCoordinates corner[name=Center] X').text();
		
		
		//clear all
		//CometGeo_ClearAll();
		CometGeo_DestroyPlacemark(tag);
		
		
		//update pegs
		//CometGeo_CreateAllPlacemarks();
		CometGeo_CreatePlacemark(tag);		
		
		CometControlsLog("End of CometControlsGeoEdit_Commit()");
	}
}

function CometControlsGeoEdit_SaveXMLValue_Y(corner_name,valueID)  {
	$(xml_content).find('CornerCoordinates corner[name=\"'+corner_name+'\"] Y').text(document.getElementById(valueID).value);
}
function CometControlsGeoEdit_SaveXMLValue_X(corner_name,valueID)  {
	$(xml_content).find('CornerCoordinates corner[name=\"'+corner_name+'\"] X').text(document.getElementById(valueID).value);
}

function CometControlsGeoEdit_Load_Btn() {
	var tag=document.getElementById("geo_editing_tag").value;

	var url=global_properties.imageprefix+"Relay?path="+placemarkList[tag].mdm.urlName+"&type=custom-metadata&annotation=geo&stream";


	CometControlsLog("url="+url);
	$.ajax({
		type: "GET",
		url: url,
		dataType: "xml",
		success: function(xml) {
			//on success, let's move the view to match the xml
			xml_content=xml;
			document.getElementById("geo_latitude").value=$(xml).find('CornerCoordinates corner[name=Center] Y').text();
			document.getElementById("geo_longitude").value=$(xml).find('CornerCoordinates corner[name=Center] X').text();

			CometControlsGeoEdit_Zoom_Btn();

		} //end success function
	}); //end ajax get
}

function CometControlsGeoEdit(tag) {
	if(CometGeo_Disabled()) {
		CometControlsLog("Geo Visualization appears to be disabled...");
	
		return;
	}
	CometControlsLog("BEGIN GeoEdit("+tag+")");
	
//	document.getElementById("geo_load").disabled=false;
	document.getElementById("geo_save_btn").disabled=false;
	document.getElementById("geo_zoom_btn").disabled=false;
	
	
	//step 1, page flip to geo/path editor
	cTabFliptoPage("LHS",3);
	
	//step 2, page flip RHS to visualizer
	cTabFliptoPage("RHS",page_number_geoviz);
		
	//clear out existing arrays
	xml_content="";
	document.getElementById("geo_path").value=placemarkList[tag].mdm.utf8name;
	document.getElementById("geo_editing_tag").value=tag;

	
	if(placemarkList[tag].geotagged) {
		//already know that this object is geotagged, so let's load it and allow it to be reloaded at will
		document.getElementById("geo_load_btn").disabled=false;
		CometControlsGeoEdit_Load_Btn();
		CometControlsLog("has geotags... loading");
	} else {
		CometControlsLog("no initial geo xml.. have to make one");
		xml_content=jQuery.parseXML(CometControlsGeoXML_Default());
		CometControlsGeoEdit_Zoom_Btn();
	}	
	document.getElementById("geo_editor_controls").style.display="block";
	document.getElementById("geo_editor_message").style.display="none";
	CometControlsLog("END GeoEdit("+tag+")");
}

function CometControlsGeoXML_Default() {
	return '<?xml version="1.0" encoding="UTF-8" standalone="no"?><GDALInfo><CornerCoordinates><Corner name="Upper Left"><X dataType="number" /><Y dataType="number" /></Corner><Corner name="Lower Left"><X dataType="number" /><Y dataType="number" /></Corner><Corner name="Upper Right"><X dataType="number" /><Y dataType="number" /></Corner><Corner name="Lower Right"><X dataType="number"/><Y dataType="number" /></Corner><Corner name="Center"><X dataType="number" /><Y dataType="number" /></Corner></CornerCoordinates></GDALInfo>';
}

function CometControlsGeoEdit_Zoom_Btn() {
	CometGeo_OverheadZoom(document.getElementById("geo_latitude").value, document.getElementById("geo_longitude").value);
}

function CometControlsGeoEdit_RemoveScreenOverlay() {
	CometGeo_HideScreenOverlay("overlay");
}

function CometControlsGeoEdit_CreateScreenOverlay() {
	CometGeo_CreateScreenOverlay("#map3d", "overlay", global_properties.imageprefix+'/images/target.gif', 120, 120);
}

function CometControlsViewNoteObject(tag) {
	global_tag=tag;

	//after we implement partner_urlName
	document.getElementById("note_object_path").value=placemarkList[tag].mdm.note_urlName;


	//after we implement partner_object, just use object for testing
//	var url="Relay?path="+document.getElementById("note_object_path").value+"&type=partnerobject";
	var url="Relay?path="+document.getElementById("note_object_path").value+"&type=object";

	//got the path, now load it into the text area


	CometControlsLog("requesting url="+url);
	CometControlsLog("note_object_path="+document.getElementById("note_object_path").value);
	CometControlsLog("tag="+tag);
	
	

	var request=CometControlsHttpGet(url);
	request.tgt="editable_note";
	request.onreadystatechange = function() {
		if ((request.readyState == 4) && (request.status == 200)) {
			CometStopAJAXAnimation();
			document.getElementById(request.tgt).value=request.responseText;
			cTabFliptoPage( "RHS", page_number_notes );
		}
	}
}

function CometControlsSaveFile_inNotesEditor() {
	CometStartAJAXAnimation();
	$.ajax({
   		url: 'Relay?path='+document.getElementById("note_object_path").value+'&type=object',
		type: 'PUT',
		contentType : "text",
		data: document.getElementById("editable_note").value,
		contentLength:  document.getElementById("editable_note").value.length,
		dataType: "text",
		success: function(result) {
			CometStopAJAXAnimation();
			UpdateStatusMessage("File "+document.getElementById("note_object_path").value+" saved.");
		},
		error: function(result) {
			CometStopAJAXAnimation();
			UpdateStatusMessage("Error: unable to save: "+document.getElementById("note_object_path").value);
		}
	});
}

function ApplySystemMonitorRole() {
	
	document.getElementById("stop_comet_service_btn").setAttribute('disabled','true');
	document.getElementById("start_comet_service_btn").setAttribute('disabled','true');
	document.getElementById("save_config_file_btn").setAttribute('disabled','true');
	document.getElementById("save_role_file_btn").setAttribute('disabled','true');


	document.getElementById("ADMIN_tabHeader_5").style.display="none";
	document.getElementById("ADMIN_tabHeader_6").style.display="none";
	document.getElementById("ADMIN_tabHeader_7").style.display="none";
	
	document.getElementById("LHS_tabHeader_3").style.display="none";

}

function CometLoading(msg) {

	var oldcontent=document.getElementById("login_loading").innerHTML;
	document.getElementById("login_loading").innerHTML=oldcontent+'<BR />'+msg;

}






