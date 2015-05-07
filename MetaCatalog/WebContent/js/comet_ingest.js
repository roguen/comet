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


function CometIngestor_Init() { 
	var request=cTabLoad("index_ingestor.html","ingestor_page");
	CometLoading("Initializing Ingestor UI...");

	request.onreadystatechange = function() {
		if ((request.readyState == 4) && (request.status == 200)) {
			CometControlsLog("loading "+request.html_page+" content to object "+request.tgt);
			document.getElementById(request.tgt).innerHTML=request.responseText;
			CometStopAJAXAnimation();
			InitTabs("INGEST");
			//upload not supported when using multiple sources
			if(global_properties.ingestor_multisource) {
				CometControlsLog("====>ingestor is multisource, hide upload tab");
				document.getElementById("INGEST_tabHeader_1").style.display="none";
				cTabFliptoPage( "INGEST", 2 );
			} else {
				CometControlsLog("====>ingestor is single source, allow upload tab");
				cTabFliptoPage( "INGEST", 1 );
				CometControlsInitializeUploader();
				CometControlsCHDir(root_ingest_dir_orig);
			}
			
			
			//for testing purposes, always automatically flip to page 2
			//cTabFliptoPage( "INGEST", 2 );
			
		
			//load ingestor
			if(!testing) {
				setTimeout(function() { CometIngestor_StartLoop(); CometIngestor_StopLoop();}, 5000);
//				setTimeout(function() { CometIngestor_StartLoop(); }, 5000);
			}
			//document.getElementById("ingest_button_span").innerHTML='<button id="ingest_button" onmousedown="CometControlsStartIngestorLoop()" value="startcountdown">Begin Ingest</button>'
			document.getElementById("migration_src").innerHTML=global_properties.migration_src;
			document.getElementById("migration_dest").innerHTML=global_properties.migration_dest;
			document.getElementById("RHS_tabHeader_"+page_number_ingest).style.display="none";
			//don't leave ingestor running.. it will muck up the console log
	//		CometIngestor_StopLoop();
			CometIngestor_LoadComplete();
		}
	}
}

function CometIngestor_StartLoop() {
	CometControlsHttpGet("Admin?action=continue");
	
	global_properties.ingest_running=true;
	shouldTrackIngest=true;
	CometIngestor_Loop();
}

function CometIngestor_UpperTable_ShowDataColumns(props, i) {
	var content;
	
//	if(props.threaddata[i].status=="idle") {
	if(props.threaddata[i].path=="idle" || props.threaddata[i].path=="none") {
		content="<td colspan=3><center>No Activity</center></td>";
	} else {
//		var d=new Date(props.threaddata[i].start_time * 1000);
		var d=new Date(props.threaddata[i].start_time);
	
	//<span onmouseover="CometControlsTip('The file object residing in HCP.')" onmouseout="CometControlsUnTip()">Object</span>
	
	
		//post 1.21
		//var tooltip='status='+props.threaddata[i].status+'<BR/>error='+props.threaddata[i].error+'<BR/>target system='+props.threaddata[i].tgt+'<BR/>custom='+props.threaddata[i].custom;
		
		
// 			id=i;
// 		path="none";
// 		size=0;
// 		start_time=0;
// 		bytes_written=0;
// 		status="idle";
// 		custom="";
// 		error="";
// 		tgt="";
		
		//save tooltips for post 1.21
		//content="<td><span onmouseover=\"CometControlsTip(\'"+tooltip+"\')\" onmouseout=\"CometControlsUnTip()\">"+CometControlsDisplayPath(props.threaddata[i].path,20)+"</span></td><td>"+props.threaddata[i].size+"</td><td>"+d.toString()+"</td>";
		content="<td>"+CometControlsDisplayPath(props.threaddata[i].path,30)+"</span></td><td>"+props.threaddata[i].size+"</td><td>"+d.toString().substring(0,24)+"</td>";
	}
	return content;
}

function CometIngestor_UpperTable(props) {
	var table="";

	for(var i=0; i<props.threaddata.length; i++) {
		table+="<tr><td>"+(props.threaddata[i].id+1)+"</td>"+CometIngestor_UpperTable_ShowDataColumns(props,i)+"</tr>";
	}
	return table;
}

function CometIngestor_Loop() {
	if(!global_properties.ingest_running) {
		document.getElementById("ingest_button_span").innerHTML='<button id="ingest_button" onmousedown="CometIngestor_StartLoop()" >Begin Ingest Tracking</button>'
		return;
	}
	
	document.getElementById("ingest_button_span").innerHTML='<button id="ingest_button" onmousedown="CometIngestor_StopLoopBTN()" >Stop Ingest Tracking</button>'


	CometControlsLog("BEGIN CometIngestor_Loop() --- infinite loop--- NEW!!!!");
	
	var request=CometControlsHttpGet("IngestorThreads?action=heartbeat");
	
	request.onreadystatechange = function() {
		if ((request.readyState == 4) && (request.status == 200)) {
			
			//grab JSON here
			var ingestor_props=JSON.parse(request.responseText);
			
			CometControlsLog("response output: \n"+request.responseText+"\n\n");
			document.getElementById("ingestor_table").innerHTML=CometIngestor_UpperTable(ingestor_props);



				
			var table="<tr><td width=50%>";
			table+="Ingestor state is "+ingestor_props.state;
			

			table+="</td><td>";
			table+="last check in "+ingestor_props.hb_datestamp;
			table+="</td></tr><tr><td>";
			table+="last directory scanned: "+ingestor_props.last_dir;
			table+="</td><td>";
			
			if(ingestor_props.custom) {
				table+=ingestor_props.custom;
			} else {
				table+="&nbsp;"
//				if(ingestor_props.complete) table+="Ingestion is complete";
//				else if(ingestor_props.mode=="continuous") table+="Ingestion is continuous";
//				else table+="Ingestion is incomplete";
			}	
			
			table+="</td></tr><tr><td>";
			table+="Iteration #: "+ingestor_props.iteration+"</td><td>Time in this iteration: "+Number(ingestor_props.time_in_iteration)/1000.0+" seconds";
			table+="</td></tr><tr><td>";
			table+="Upload Pool usage:  "+ingestor_props.upload_pool_size+"</td><td>Percent Complete: "+ingestor_props.percent_complete+"%";
			table+="</td></tr>";
			document.getElementById("ingestor_status").innerHTML=table;
			CometControlsLog("ingestorthreads?heartbeat is complete, stop animating if possible");
			CometStopAJAXAnimation();
		}
	}

	if(global_properties.ingest_running) {
		//recursive call to keep the loop going every ingestor_poll_frequency milliseconds
		setTimeout(function() { CometIngestor_Loop(); },global_properties.ingest_poll_frequency);
	}	
	CometControlsLog("END CometIngestor_Loop() --- infinite loop");
}

function CometIngestor_StopLoopBTN() {
	shouldTrackIngest=false;
	CometIngestor_StopLoop();
	
}

function CometIngestor_StopLoop() {
	global_properties.ingest_running=false;
}

function CometIngestor_LoadComplete() {
	loadables.ingest=true;
	CometLoading("Ingestor UI Complete");
}

function CometControlsPauseIngestor() {
	var url="IngestorThreads?action=pause";
	CometStartAJAXAnimation();
	$.ajax({
		url: url,
		type: 'GET',
		success: function(result) {
			CometStopAJAXAnimation();
			document.getElementById("ingest_pause_button_span").innerHTML='<button id="pause_resume_button" onmousedown="CometControlsResumeIngestor()" >Resume Ingestor</button>';
			UpdateStatusMessage("COMET::Ingestor Paused");
		}
	});
}

function CometControlsResumeIngestor() {
	var url="IngestorThreads?action=resume";
	CometStartAJAXAnimation();
	$.ajax({
		url: url,
		type: 'GET',
		success: function(result) {
			CometStopAJAXAnimation();
			document.getElementById("ingest_pause_button_span").innerHTML='<button id="pause_resume_button" onmousedown="CometControlsPauseIngestor()" >Pause Ingestor</button>';
			UpdateStatusMessage("COMET::Ingestor Resumed");
		}
	});
}

function CometControlsFlushIngestorCache() {
	var url="IngestorThreads?action=flush_cache";
	CometStartAJAXAnimation();
	$.ajax({
		url: url,
		type: 'GET',
		success: function(result) {
			CometStopAJAXAnimation();
			
			UpdateStatusMessage("COMET::Ingestor Cache Flushed");
		}
	});
}

function CometControlsDeleteIngestorCache() {
	var url="IngestorThreads?action=delete_cache";
	CometStartAJAXAnimation();
	$.ajax({
		url: url,
		type: 'GET',
		success: function(result) {
			CometStopAJAXAnimation();
			
			UpdateStatusMessage("COMET::Ingestor Cache Deleted");
		}
	});
}

function CometControlsUpoadFileViaURL_btn() {
	CometControlsUploadFileViaURL(document.getElementById("upgradeUrl").value, document.getElementById("upgrade_via_url_saveas"), "UploadUpgrade");
}

function CometControlsUploadFileViaURL(url, saveas, webapp) { 
	CometStartAJAXAnimation();
	
	var request = getRequestObject();
	var params = "url="+encodeURIComponent(url)+"&saveAs="+saveas;

	request.onreadystatechange = function() {
		if ((request.readyState == 4) && (request.status == 200) && (request.responseText!="")) {
			CometControlsLog("==========results received============");	
			CometStopAJAXAnimation();
		}
	};
	//if(params!="") params="?"+params
//	request.open("GET", "do-Query" + params, true);
	request.open("POST", webapp , true);
	request.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
//	request.send(null);
	request.send(params);
}



