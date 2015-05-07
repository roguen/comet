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

function CometAdmin_Terminal_Init() {
	CometControlsLog("initializing terminal....");	
	
    var id = 1;
    var filepath='';
    var greeting="Commands include help, edit, ls, and rm";
    $('#admin_terminal').terminal(function(command, term) {
        if (command == 'help') {
            term.echo(greeting);
	} else if (command=="edit") {
                term.echo('\tswitching to editor tab');
		cTabFliptoPage( "ADMIN",6);
        } else if (command.startsWith("edit ") ) {
		filepath=command.slice(5,command.length);
		term.echo('Loading file ' + filepath+' in editor tab');
                term.echo('\tswitching to editor tab');

		CometAdmin_LoadFile_inEditor(filepath);
		
		
		cTabFliptoPage( "ADMIN",6);

        }  else if (command.startsWith("ls") ) {
		if(command=="ls") {
			term.echo("ls requires an argument");
		} else {
			filepath=command.slice(3,command.length);
			term.echo('Listing contents of directory ' + filepath);
			term.pause();
			CometAdmin_Terminal_DirList(term,filepath);
		}
                
        }  else if (command.startsWith("rm ") ) {
		filepath=command.slice(3,command.length);
		
		term.push(function(command, term) {
              		if (command == 'yes' || command  == 'y' ) {
                	    term.echo('deleting file: '+filepath);
			    CometAdmin_Terminal_RemoveLocalFile(term, filepath);
                	} else {
			     term.echo('not deleting file: '+filepath);
                	}
			term.pop();
		}, {
			height: 500,
		        prompt: 'Are you Sure? [y|N] '
		});
 	} else if(command.length==0) { 
		CometControlsLog("enter pressed, no command");
		
	} else {
            term.echo("unknown command " + command);
        }
    }, {
        greetings: greeting,
	height: 500,
        prompt: 'comet> '
//        onBlur: function() {
 ///           // prevent loosing focus
  //          return false;
   //     }
    });
}

function CometAdmin_Terminal_Action(term, path, action) { 
	var request=CometControlsHttpGet("Admin?action="+action+"&path="+path);
	request.onreadystatechange = function() {
		if ((request.readyState == 4) && (request.status == 200)) {
			//grab table here
			term.pause();
			term.echo(request.responseText);
			term.resume();
			CometStopAJAXAnimation();
		}
	}
}


function CometAdmin_Terminal_DirList(term, path) {
	CometAdmin_Terminal_Action(term,path,"dirlist");
}
function CometAdmin_Terminal_RemoveLocalFile(term, path) {
	CometAdmin_Terminal_Action(term,path,"remove");
}

function CometAdmin_LoadFile_inEditor(in_path, tail) {
	if(in_path) { 
		document.getElementById("edit_file_path").value=in_path;
	}
	cFiler_LoadLocalTxtFile("editable_file", in_path);
	
}

function CometAdmin_SaveFile_inEditor() {
	cFiler_SaveLocalTxtFile(document.getElementById("editable_file").value, document.getElementById("edit_file_path").value);
}

function CometAdmin_UpgradeUploader_Init() {
	
	 
// 	document.getElementById("upgrade_via_file_upload_content").style.display="none";
// 	$(function() {
// 		$( "#upgrade_via_file_upload_drop" ).click(function() {
// 			$( "#upgrade_via_url_upload_content" ).toggle( "blind", {}, 200 );
// 			$( "#upgrade_via_file_upload_content" ).toggle( "blind", {}, 200 );
// 			return false;
// 		});
// 		$( "#upgrade_via_url_upload_drop" ).click(function() {
// 			$( "#upgrade_via_url_upload_content" ).toggle( "blind", {}, 200 );
// 			$( "#upgrade_via_file_upload_content" ).toggle( "blind", {}, 200 );
// 			return false;
// 		});
// 	});
	 	
	var options = { 
	beforeSend: function() 
	 {
        $("#upgradeProgress").show();
        //clear everything
        $("#upgradeBar").width('0%');
 //       $("#upgradeMessage").html("");
        $("#upgradePercent").html("0%");
    	//CometStartAJAXAnimation();
    },
    uploadProgress: function(event, position, total, percentComplete) 
    {
        $("#upgradeBar").width(percentComplete+'%');
        $("#upgradePercent").html(percentComplete+'%');
 
 
 
 
    },
    success: function() 
    {
        $("#upgradeBar").width('100%');
        $("#upgradePercent").html('100%');
 
    },
    complete: function(response) 
    {
    	
    	
    	
      //  $("#upgradeMessage").html("<font color='green'>"+response.responseText+"</font>");
       	CometAdmin_LoadJSONIntoUpgradeTable();

		UpdateStatusMessage(response.responseText);
		CometStopAJAXAnimation();
		setTimeout(function() {  $("#upgradeBar").width('0%');
        $("#upgradePercent").html('0%');
	
		document.getElementById("upgradeFile").value="";
	
	
	 }, 5000);
		
		
		
    },
    error: function()
    {
    	UpdateStatusMessage(response.responseText);
    	CometStopAJAXAnimation();
    }
 
}; 
     $("#upgradeForm").ajaxForm(options);
}


function CometAdmin_JSONtotbody(objectArray) {
	var content="";
	var tempcontent="";
	if(objectArray) {
		for(var i = 0; i<objectArray.length; i++) {
	//		tempcontent='<tr><td style="width:500px">'+objectArray[i].utf8name+'</td><td><img onmouseover="CometControlsTip(\'Click to Delete Object\')" onmouseout="CometControlsUnTip()" onmousedown="Delete(\''+objectArray[i].urlName+'\',true)" src="images/icons/trash-icon.png" width="32" height="32"></td></tr>';
			tempcontent='<tr><td style="width:500px">'+objectArray[i].utf8name+'</td><td><img onmouseover="CometControlsTip(\'Click to Delete Object\')" onmouseout="CometControlsUnTip()" onmousedown="CometAdmin_UpgradeTable_DeleteBTN(\''+objectArray[i].urlName+'\',true)" src="images/icons/trash-icon.png" width="32" height="32"></td>';
			tempcontent+='<td><img onmouseover="CometControlsTip(\'Click to Archive Object\')" onmouseout="CometControlsUnTip()" onmousedown="CometAdmin_UpgradeTable_ArchiveBTN(\''+objectArray[i].urlName+'\',true)" src="images/icons/archive-icon.png" width="32" height="32"></td></tr>';
			CometControlsLog("row = "+tempcontent);
			content+=tempcontent;
		}
	} else {
		CometControlsLog("whoops, array is not populated and is in fact null");
	}
	return content;
}

function CometAdmin_LoadJSONIntoUpgradeTable() {
	CometStartAJAXAnimation();
	$.get( "query?query="+global_properties.upgradeDir+"&config", function( data ) {
		document.getElementById("upgrade_content_tbody").innerHTML=CometAdmin_JSONtotbody(data);
		CometStopAJAXAnimation();
	}, "json" );
}

function CometAdmin_UpgradeTable_DeleteBTN(path, isConfig) {
	var url="Relay?path="+path;
	if(isConfig) url+="&config";
	
	CometStartAJAXAnimation();
	$.ajax({
	    url: url,
	    type: 'DELETE',
	    success: function(result) {
			CometStopAJAXAnimation();
			//refresh table
			CometAdmin_LoadJSONIntoUpgradeTable();
			UpdateStatusMessage("File "+path+" successfully deleted from HCP.");
	    }
	});
}
function CometAdmin_UpgradeTable_ArchiveBTN(path, isConfig) {
	var url="Relay?type=object&action=archive&path="+path;
	if(isConfig) url+="&config";
	
	CometStartAJAXAnimation();
	$.ajax({
	    url: url,
	    type: 'GET',
	    success: function(result) {
			CometStopAJAXAnimation();
			//refresh table
			CometAdmin_LoadJSONIntoUpgradeTable();
			UpdateStatusMessage("File "+path+" successfully moved to archive.");
	    }
	});
}

function CometAdmin_LoadComplete() {
 	CometAdmin_LoadJSONIntoUpgradeTable();

	CometLoading("Admin UI Complete");
	loadables.admin=true;
}

function CometAdmin_Init() {			
	var request=cTabLoad("index_admin.html","admin_page");
	CometLoading("Initializing Admin UI...");
	
	request.onreadystatechange = function() {
		if ((request.readyState == 4) && (request.status == 200)) {
			CometControlsLog("loading "+request.html_page+" content to object "+request.tgt);
			document.getElementById(request.tgt).innerHTML=request.responseText;
			CometStopAJAXAnimation();
				
			InitTabs("ADMIN");
			InitTabs("LOG");
	
			document.getElementById("migration_src_admin").innerHTML=global_properties.migration_src;
	
			cTabFliptoPage( "ADMIN", 1 );
			cTabFliptoPage( "LOG", 1 );
			CometRefreshLogs();
			CometAdmin_Terminal_Init();
	
			CometControlsLog("\t\t========= about to initialize uploader =============");
	
			CometAdmin_UpgradeUploader_Init();
			CometControlsLog("\t\t========= initialize uploader complete =============");
		
			document.getElementById("RHS_tabpage_"+page_number_admin).style.display="none";
			CometAdmin_LoadComplete();
		}
	};
}



