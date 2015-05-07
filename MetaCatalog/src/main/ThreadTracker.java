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

package main;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hdsfed.cometapi.CometProperties;
import com.hdsfed.cometapi.ExtendedLogger;
import com.hdsfed.cometapi.HCPClient;
import com.hdsfed.cometapi.IngestorJSON;
import com.hdsfed.cometapi.ServletHelper;
import com.hdsfed.cometapi.StringHelper;
import com.hdsfed.cometapi.ThreadData;
import com.hdsfed.cometapi.ThreadTrackerDB;

//TODO: ensure that we are thread safe
//		need function descriptions etc

public class ThreadTracker {
	private static ExtendedLogger ScreenLog = new ExtendedLogger(ThreadTracker.class.getPackage().getName());
	public enum threadActionType {
		TACT_SHOW,
		TACT_UPDATE,
		TACT_NEW,
		TACT_TEST,
		TACT_HEARTBEAT,
		TACT_STOP,
		TACT_PAUSE,
		TACT_RESUME,
		TACT_STATUS,
		TACT_RELOAD,
		TACT_FLUSH,
		TACT_DEL_CACHE,
		TACT_UNSUPPORTED
	};

	private static threadActionType threadActionType(String string) {
		if(string.toLowerCase().contains("show")) return threadActionType.TACT_SHOW;
		if(string.toLowerCase().contains("update")) return threadActionType.TACT_UPDATE;
		if(string.toLowerCase().contains("test")) return threadActionType.TACT_TEST;
		if(string.toLowerCase().contains("new")) return threadActionType.TACT_NEW;
		if(string.toLowerCase().contains("heartbeat")) return threadActionType.TACT_HEARTBEAT;
		if(string.toLowerCase().contains("stop")) return threadActionType.TACT_STOP;
		if(string.toLowerCase().contains("pause")) return threadActionType.TACT_PAUSE;
		if(string.toLowerCase().contains("resume")) return threadActionType.TACT_RESUME;
		if(string.toLowerCase().contains("status")) return threadActionType.TACT_STATUS;
		if(string.toLowerCase().contains("reload")) return threadActionType.TACT_RELOAD;
		if(string.toLowerCase().contains("flush_cache")) return threadActionType.TACT_FLUSH;
		if(string.toLowerCase().contains("delete_cache")) return threadActionType.TACT_DEL_CACHE;
		
		return threadActionType.TACT_UNSUPPORTED;
	}

	private static String ThreadWeaver(HCPClient client, CometProperties mProps, Map<String, String> parameters) throws IOException {
		ScreenLog.begin("ThreadWeaver(client,mprops,parameters)");
		ScreenLog.out(parameters);
		String content="";
		
		ThreadTrackerDB db=new ThreadTrackerDB();

		
	//	Boolean exp=CometProperties.getInstance().getExperimental();
		
		ThreadData [] td=null; //new ThreadData[CometProperties.getInstance().getMaximumThreads()];
		
		IngestorJSON ij=null;

//		if(exp) {
				//need to populate the json with content from the DB (temporary)
			ij=IngestorJSON.getInstance();
			//ij.PopulateFromDB(db);
			
			
//		}
		
		
		int tryit=0;
		switch(threadActionType(parameters.get("action"))) {
			case TACT_SHOW:
				
				if(CometProperties.getIngestorRunningState().contains("running")) {
					td=db.getAll();
					for(int i=0; i<td.length; i++) {
						content+="<tr>";
						content+="<td>"+(td[i].getId()+1)+"</td>";
						if(td[i].getPath().equals("idle"))
							content+="<td colspan=3><center>No activity</center></td>";
						else if(td[i].getPath().contains("STALLED"))
							content+="<td colspan=3><center>STALLED due to connectivity issues or namespace is full</center></td>";
						else if(td[i].getPath().contains("not-written"))
							content+="<td colspan=3><center>File not written</center></td>";
						else	
							content+="<td>"+StringHelper.ReducedLengthString(td[i].getPath(),50)+"</td>" +
									"<td>"+td[i].getLength()+"</td>" +
					         "<td>"+td[i].getDatestamp()+"</td>";
					}	
				} else {
					for(int i=0; i<CometProperties.getInstance().getMaximumThreads(); i++) {
						content+="<tr>";
						content+="<td>"+(i+1)+"</td>";
						content+="<td colspan=3><center>No activity</center></td>";
					}
				}
				content+="</tr>";
				
				if(parameters.containsKey("complete"))
				content="<html><head><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"css/styles.css\"/></head><body>"+
						"<table><thead><th>ID</th><th>File Path</th><th>File Length</th><th>Ingest Start</th></thead>" +
						content+"</table></body></html>";
			break;
			case TACT_UPDATE:
			try {
				CometProperties.setIngestorRunningState("running");

				if(!parameters.get("id").equals("") && !parameters.get("path").equals("") && !parameters.get("length").equals("")) {
				//	if(exp) { //new way
				//		ScreenLog.out("doing things the new way");
					
					
					
				//	} else { // old way
					while(!db.updateData(Integer.parseInt(parameters.get("id")), parameters.get("path"), Integer.parseInt(parameters.get("length")))) {
						tryit++;
						CometProperties.setIngestorRunningState("err: DB locked");
						ScreenLog.out("error: db was locked, waiting 1 second.. and trying again (tryit="+tryit+")");
						Thread.sleep(1000);
					}
				//	}
				}
				content="<html><head><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"css/styles.css\"/></head><body>update</body></html>";
				

			} catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }
			break;
			
			
			case TACT_NEW:
				ScreenLog.begin("thread Action new");
				db.newDB();
				content="successfully created new db<br>";
				content+="<ul>";
				content+="</ul>";
				content="<html><head><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"css/styles.css\"/></head><body>"+content+"</body></html>";

				//TODO: make watcher do something useful
				//CometProperties.setIngestorWatcher(parameters.containsKey("watcher"));
				CometProperties.setIngestionComplete(parameters.containsKey("ingest_complete"));
				CometProperties.setIngestorRunningState("init");
				CometProperties.setIngestorPercentComplete(0);
				CometProperties.setIngestorIteration(0);
				CometProperties.setUploadPoolSize(0);
				
			break;
			case TACT_HEARTBEAT:
				if(!parameters.containsKey("hput")) {
					content=ij.toString();
					break;
				}
				
				//put
				if(!parameters.containsKey("state")) parameters.put("state","runnning");
				
				if(parameters.containsKey("hput")) {
					ScreenLog.begin("\n\t\t\theartbeat put");
					CometProperties.setIngestionComplete(parameters.containsKey("ingest_complete"));
					if(parameters.containsKey("iteration")) {
						ScreenLog.out("setting iteration to "+parameters.get("iteration"));
						CometProperties.setIngestorIteration(parameters.get("iteration"));
					}
					if(parameters.containsKey("lastdir")) {
						CometProperties.setLastDirectory(parameters.get("lastdir"));
					}
					
					CometProperties.setIngestorRunningState(parameters.get("state"));
					
					if(parameters.containsKey("ingest_complete")) {
						CometProperties.setIngestorRunningState("complete");
						CometProperties.setIngestorPercentComplete(100);
					}
					if(parameters.containsKey("percent")) {
						CometProperties.setIngestorPercentComplete(parameters.get("percent"));
					}
					if(parameters.containsKey("upload_pool_size")) {
						CometProperties.setUploadPoolSize(parameters.get("upload_pool_size"));
					}
					
					if(parameters.containsKey("custom")) {
						ScreenLog.out("contains custom: "+parameters.get("custom"));
						CometProperties.setCustom(parameters.get("custom"));
					} else {
						ScreenLog.out("missing custom");
					}
					
					if(parameters.containsKey("time_in_iteration")) {
						CometProperties.setTimeInIteration(parameters.get("time_in_iteration"));
						ScreenLog.out("time_in_iteration should be set to "+ parameters.get("time_in_iteration"));
						ScreenLog.out("time_in_iteration was set to "+ CometProperties.getTimeInIteration());
					} else {
						ScreenLog.out("time_in_iteration was not set");
					}
	
					
					ScreenLog.end("\n\t\t\theartbeat put");
					content=ij.toString();
//					content+="{}";
				} 
//				else {
//					content+="{\"heartbeat_datestamp\":\""+CometProperties.getHeartBeat()+"\",\"last_directory\":\""+CometProperties.getLastDirectory()
//							+"\", \"complete\" : "+CometProperties.getIngestionComplete().toString()+", \"running\" : \""+CometProperties.getIngestorRunningState()+"\""
//							+", \"mode\" : \""+CometProperties.getInstance().getIngestionMode()+"\""
//							+", \"iteration\" : "+CometProperties.getIngestorIteration()
//							+", \"percent_complete\" : "+CometProperties.getPercentComplete()
//							+", \"upload_pool_size\" : \""+CometProperties.getUploadPoolSize()+"\""
//							+", \"time_in_iteration\" : "+CometProperties.getTimeInIteration();
//					
//					if(CometProperties.customHasContent()) {
//						content+=", \"custom\" : \""+CometProperties.getCustom()+"\"";
//					}
//					content+="}";
//				}
			break;	
			case TACT_STOP:
				//touch ingestor.stop
				File stopfile=CometProperties.getInstance().getStopFileName();
				stopfile.createNewFile();
				content="{}";
			break;
			case TACT_PAUSE:
				File pausefile=CometProperties.getInstance().getPauseFileName();
				if(!pausefile.exists()) pausefile.createNewFile();
				content="{}";

			break;
			case TACT_RESUME:
				File resumefile=CometProperties.getInstance().getPauseFileName();
				if(resumefile.exists()) resumefile.delete();
				content="{}";
			break;
			case TACT_STATUS:
				File statusfile=CometProperties.getInstance().getPauseFileName();
				if(statusfile.exists()) content="{ \"ingestor_paused\" : true }";
				else content="{ \"ingestor_paused\" : false }";
				
			break;
			case TACT_RELOAD:
				File pausefile2=CometProperties.getInstance().getPauseFileName();
				if(!pausefile2.exists()) pausefile2.createNewFile();
				File reloadfile=CometProperties.getInstance().getReloadFileName();
				if(!reloadfile.exists()) reloadfile.createNewFile();
			break;
			case TACT_FLUSH:
				File pausefile3=CometProperties.getInstance().getPauseFileName();
				if(!pausefile3.exists()) pausefile3.createNewFile();
				File flushfile=CometProperties.getInstance().getFlushCacheFileName();
				if(!flushfile.exists()) flushfile.createNewFile();
			break;
			case TACT_DEL_CACHE:
				File flushcache=new File(CometProperties.getInstance().getFileProcessedCache());
				if(flushcache.exists()) flushcache.delete();
				
			break;
			case TACT_TEST:
			break;	
			case TACT_UNSUPPORTED:
			default:
				try {
					ScreenLog.out("error: action="+parameters.get("action")+" is not supported at this time.");
					content="<html><head><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"css/styles.css\"/></head><body>test or unsupported</body></html>";
				} catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }
			break;	
		}
		ScreenLog.end("ThreadWeaver(client,mprops,parameters)");
		return content;
	}

	public static void getThreads(HttpServletRequest request, HttpServletResponse response)  {
		try {
			response.setHeader("Cache-Control", "no-cache");
			response.setHeader("Pragma", "no-cache");
			Map<String, String> parameters=ServletHelper.GetParameterMap(request);
			parameters.put("hget", "true");
			ThreadWeaver(parameters, response,true);
		} catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }
	} //end Relay front end
	public static void putThreads(HttpServletRequest request, HttpServletResponse response)  {
		try {
			response.setHeader("Cache-Control", "no-cache");
			response.setHeader("Pragma", "no-cache");
			Map<String, String> parameters=ServletHelper.GetParameterMap(request);
			parameters.put("hput", "true");
			ThreadWeaver(parameters, response,false);
		} catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }
	} //end Relay front end

	public static void ThreadWeaver(Map<String,String> parameters, HttpServletResponse response, Boolean isGet) throws Exception {		
			ScreenLog.begin("ThreadWeaver(parameters,response,isGet="+isGet+")");
			//users and roles are built in and stored statically
			CometProperties mProps=CometProperties.getInstance();
			HCPClient client=new HCPClient(mProps);
			PrintWriter writer =response.getWriter();
			writer.write(ThreadWeaver(client, mProps, parameters));
			ScreenLog.end("=== ThreadTracker::ThreadWeaver(parameters,response,isGet="+isGet+")");
	}
}


 
