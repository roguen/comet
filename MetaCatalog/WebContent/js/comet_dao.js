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

//Purpose of this file:
// 	define low level data access functions for
//	COMET's rest interface


// function cDAO_SaveFile(content, url, ct) {
// 	CometStartAJAXAnimation();
// 	$.ajax({
//    		url: url,
// 		type: 'PUT',
// 		contentType : ct,
// 		data: content,
// 		contentLength: content.length,
// 		dataType: ct,
// 		success: function(result) {
// 			CometStopAJAXAnimation();
// 			UpdateStatusMessage("File "+saveas+" saved.");
// 		},
// 		error: function(result) {
// 			CometStopAJAXAnimation();
// 			UpdateStatusMessage("Error: unable to save file: "+saveas);
// 		}
// 	});
// }
// 
// function cDAO_Load(url, tgt) {
// 	var request = getRequestObject();
// 	request.open("GET", url, true);
// 	request.send(null);
// 	return request;
// }


function cDao_SaveLocalTxtFile(content, saveas) {
	CometStartAJAXAnimation();
	var ct="text";
	var url="Relay?path="+saveas+"&type=editfile" 
	$.ajax({
   		url: url,
		type: 'PUT',
		contentType : ct,
		data: content,
		contentLength: content.length,
		dataType: ct,
		success: function(result) {
			CometStopAJAXAnimation();
			UpdateStatusMessage("File "+saveas+" saved.");
		},
		error: function(result) {
			CometStopAJAXAnimation();
			UpdateStatusMessage("Error: unable to save file: "+saveas);
		}
	});
}

function cDao_SaveAnnotation(content, path, annotation) {
	CometStartAJAXAnimation();
//	var ct="text/xml"; - causes an error ?!
	var ct="text";
	var url="Relay?path="+path+"&type=custom-metadata&annotation="+annotation+"&recombine"; 
	$.ajax({
   		url: url,
		type: 'PUT',
		contentType : ct,
		data: content,
		contentLength: content.length,
		dataType: ct,
		success: function(result) {
			CometStopAJAXAnimation();
			UpdateStatusMessage("Annotation "+annotation+" saved.");
		},
		error: function(result, tstatus,ethrown  ) {
			CometStopAJAXAnimation();
			CometControlsLog("status="+tstatus+" error thrown: "+ethrown);
			UpdateStatusMessage("Error: unable to save annotation: "+annotation);
		}
	});
}



// function cFiler_LoadLocalTxtFile(textareaID, path, tail) {
// 	
// 	var url="Relay?path="+path+"&type=editfile";
// 	if(tail) url+="&tail="+tail;
// 	url+="&stream";
// 	var request=CometControlsHttpGet(url);
// 	request.tgt=textareaID;
// 	request.onreadystatechange = function() {
// 		if ((request.readyState == 4) && (request.status == 200)) {
// 			CometStopAJAXAnimation();
// 			document.getElementById(request.tgt).value=request.responseText;
// 		}
// 	}
// }


function cFiler_SaveLocalTxtFile(content, saveas) {
	CometStartAJAXAnimation();
	$.ajax({
   		url: 'Relay?path='+saveas+'&type=editfile',
		type: 'PUT',
		contentType : "text",
		data: content,
		contentLength: content.length,
		dataType: "text",
		success: function(result) {
			CometStopAJAXAnimation();
			UpdateStatusMessage("File "+saveas+" saved.");
		},
		error: function(result) {
			CometStopAJAXAnimation();
			UpdateStatusMessage("Error: unable to save file: "+saveas);
		}
	});
}

function cFiler_LoadLocalTxtFile(textareaID, path, tail) {
	
	var url="Relay?path="+path+"&type=editfile";
	if(tail) url+="&tail="+tail;
	url+="&stream";
	var request=CometControlsHttpGet(url);
	request.tgt=textareaID;
	request.onreadystatechange = function() {
		if ((request.readyState == 4) && (request.status == 200)) {
			CometStopAJAXAnimation();
			document.getElementById(request.tgt).value=request.responseText;
		}
	}
}



