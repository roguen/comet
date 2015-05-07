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

var search_testing=false;

function LoadTest() {
	CometControlsLog("BEGIN LoadTest()");
	CometControlsLog("path="+document.getElementById("tester").value);
	placemarkList=new Array();
	var o=new Object();
	var rawlist="actionFunc===Video;;;annotation_size===7;;;caveat===TOP SECRET;;;latitude===38.5000000;;;longitude===126.5000000;;;description===;;;dont_autoplay===false;;;fileTypeIcon===dted;;;iconpath===http://cometdev.domain.com/MetaCatalog/images/icons/dted.png;;;link_action===javascript:CometControlsPlayMPEGVideoByTag;;;link_label===Next;;;size===6803;;;startTime===;;;taglist===Title: :::Korea,,,Coordinates: :::38.5000000, 126.5000000,,,Size: :::0.000 TB,,,Filetype: :::Geo Spatial File;;;thumbnail==="+document.getElementById("tester").value+".thumb.png;;;path==="+document.getElementById("tester").value+";;;"

	o.metadataMap=new Array();
	o.metadataMap=CometCreateMetadataMap(rawlist);
	o.dont_autoplay=false;
//	o.url=o.metadataMap["url"];

	CometControlsLog("actionFunc: "+o.metadataMap["actionFunc"]);
	CometControlsLog("link: "+o.metadataMap["link"]);
	CometControlsLog("link_action: "+o.metadataMap["link_action"]);
	CometControlsLog("link_label: "+o.metadataMap["link_label"]);

	placemarkList[0]=o;
	var tag=0;
	var url=CometToolTipBuilderGetThumbnail(tag);
	document.getElementById('catalog').innerHTML='<div id="video_target" style="display:none;"></div><div id="thumbnail_target"><table width=790px height=670px><tbody id=object_info></tbody></table></div>';
	LoadFromDescription(tag);
	CometControlsLog("END LoadTest()");

}


/***************************************** UNDER CONSTRUCTION, HERE AFTER ************************************************/
var max_pos=99;
function tag_test() {
	PE_onClick(document.getElementById("tag_test").value);
}


var placemark_id;

//drag icon

function Dump() {

	//as xml
	console.dirxml(xml_content);

	//as string
//	CometControlsLog("begin---"+(new XMLSerializer().serializeToString(xml_content.documentElement))+"----end");
}


var longtext='[{"annotation_size":"4","caveat":"","desc":"","dont_autoplay":false,"fileType":"annotation","fileTypeIcon":"fits","link_action":"","link_label":"","path":"/NASA/1904-66_AIR.fits"},' +
'{"annotation_size":"5","caveat":"","desc":"","dont_autoplay":false,"fileType":"annotation","fileTypeIcon":"kml","latitude":"35.2016418","link_action":"","link_label":"","longitude":"-118.170194","path":"/NASA/Debrisinorbit.kmz"},' +
'{"annotation_size":"3","caveat":"","desc":"","dont_autoplay":false,"fileType":"annotation","fileTypeIcon":"video","link_action":"javascript:CometControlsPlayMPEGVideoByTag","link_label":"Play","path":"/NASA/HathawayMovie.m4v"},' +
'{"annotation_size":"5","caveat":"","desc":"","dont_autoplay":false,"fileType":"annotation","fileTypeIcon":"kml","latitude":"38.991613","link_action":"","link_label":"","longitude":"-76.853319","path":"/NASA/Satellitesinorbit.kmz"},' +
'{"annotation_size":"4","caveat":"","desc":"","dont_autoplay":false,"fileType":"annotation","fileTypeIcon":"fits","link_action":"","link_label":"","path":"/NASA/UITfuv.fits"},' +
'{"annotation_size":"5","caveat":"","desc":"","dont_autoplay":false,"fileType":"annotation","fileTypeIcon":"kml","latitude":"34.6466061","link_action":"","link_label":"","longitude":"-86.6745995","path":"/NASA/exoplanets.kmz"},' +
'{"annotation_size":"4","caveat":"","desc":"","dont_autoplay":false,"fileType":"annotation","fileTypeIcon":"fits","link_action":"","link_label":"","path":"/NASA/skv3181.fits"}' +
']';




/***************************** NEW FUNCTIONS FOR 1.20.3 ***************************************************/







// function progressHandlingFunction(e){
//     if(e.lengthComputable){
//         $('progress').attr({value:e.loaded,max:e.total});
//     }
// }






// function CometAdmin_JSONtotbody_singleRow(object) {
// 	return '<tr><td>'+object.utf8Name+'</td><td><button value="Delete" /></td></tr>';
// }

