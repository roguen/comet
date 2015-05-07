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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.entity.InputStreamEntity;

import com.hds.hcp.apihelpers.HCPUtils;
import com.hdsfed.cometapi.CometAction;
import com.hdsfed.cometapi.CometProperties;
import com.hdsfed.cometapi.ExtendedLogger;
import com.hdsfed.cometapi.HCPClient;
import com.hdsfed.cometapi.ServletHelper;


//TODO: need function descriptions and parameters
// new functionality: diagnostics and healthreport

public class AdminEngine {
	private static ExtendedLogger ScreenLog = new ExtendedLogger(AdminEngine.class.getPackage().getName());
	public enum adminActionType {
		ADMIN_REBOOT,
		ADMIN_SHUTDOWN,
		ADMIN_UPGRADE_BIN,
		ADMIN_UPGRADE_RPM,
		ADMIN_BACKUP,
		ADMIN_RESTORE,
		ADMIN_COMET_STATUS,
		ADMIN_COMET_START,
		ADMIN_COMET_STOP,
		ADMIN_INGEST_CONTINUE,
		ADMIN_AUTHORIZE,
		ADMIN_HCPCHECK,
		ADMIN_MOUNTCHECK,
		ADMIN_RPM,
		ADMIN_UPTIME,
		ADMIN_MEMORY,
		ADMIN_INGEST_START_TIME,
		ADMIN_CONFIGCHECK,
		ADMIN_DIRLIST,
		ADMIN_FILEREMOVE,
		ADMIN_UNSUPPORTED,
		ADMIN_HAHA
	};

	private static adminActionType adminActionType(String string) {
		if(string.toLowerCase().contains("reboot")) return adminActionType.ADMIN_REBOOT;
		if(string.toLowerCase().contains("shutdown")) return adminActionType.ADMIN_SHUTDOWN;
		if(string.toLowerCase().contains("upgrade_bin")) return adminActionType.ADMIN_UPGRADE_BIN;
		if(string.toLowerCase().contains("upgrade_rpm")) return adminActionType.ADMIN_UPGRADE_RPM;
		if(string.toLowerCase().contains("backup")) return adminActionType.ADMIN_BACKUP;
		if(string.toLowerCase().contains("restore")) return adminActionType.ADMIN_RESTORE;
		if(string.toLowerCase().contains("status")) return adminActionType.ADMIN_COMET_STATUS;
		if(string.toLowerCase().contains("ingeststarttime")) return adminActionType.ADMIN_INGEST_START_TIME;
		if(string.toLowerCase().contains("start")) return adminActionType.ADMIN_COMET_START;
		if(string.toLowerCase().contains("stop")) return adminActionType.ADMIN_COMET_STOP;
		if(string.toLowerCase().contains("continue")) return adminActionType.ADMIN_INGEST_CONTINUE;
		if(string.toLowerCase().contains("authorize")) return adminActionType.ADMIN_AUTHORIZE;
		if(string.toLowerCase().contains("hcpcheck")) return adminActionType.ADMIN_HCPCHECK;
		if(string.toLowerCase().contains("mount")) return adminActionType.ADMIN_MOUNTCHECK;
		if(string.toLowerCase().contains("memory")) return adminActionType.ADMIN_MEMORY;
		if(string.toLowerCase().contains("uptime")) return adminActionType.ADMIN_UPTIME;
		if(string.toLowerCase().contains("configcheck")) return adminActionType.ADMIN_CONFIGCHECK;
		if(string.toLowerCase().contains("dirlist")) return adminActionType.ADMIN_DIRLIST;
		if(string.toLowerCase().contains("remove")) return adminActionType.ADMIN_FILEREMOVE;
		if(string.toLowerCase().contains("haha")) return adminActionType.ADMIN_HAHA;
		
		
		
		//not enough time to implement these, but basically diagnostic would be a json dump of key values that can be
		//interpretted by javascript outside the system to determine if the system is "healthy"
		
//		if(string.toLowerCase().contains("diagnostic")) return adminActionType.ADMIN_DIAGNOSTIC;
		
		//health report would be similar to diagnostic, but possibly in a more verbose and human-readable form, intended to be dumped to a text file
//		if(string.toLowerCase().contains("healthreport")) return adminActionType.ADMIN_HEALTHREPORT;
		if(string.toLowerCase().contains("rpm")) return adminActionType.ADMIN_RPM;
 		
		return adminActionType.ADMIN_UNSUPPORTED;
	}

	private static String Action(HttpServletRequest request, HCPClient client, CometProperties mProps, Map<String, String> parameters) {
		ScreenLog.begin("AdminEngine::Action(client,mprops,parameters)");
		String content="";
		Runtime run = Runtime.getRuntime() ;
		Process pr = null;
		URL encodedPathURL=null;
		BufferedReader buf = null;
		String line=null;

		try {
			switch(adminActionType(parameters.get("action"))) {
				case ADMIN_REBOOT:
					ScreenLog.out("about to reboot");
					pr = run.exec("/sbin/reboot");
					pr.waitFor() ;
					content="Server preparing for reboot";
					break;
				case ADMIN_SHUTDOWN:
					ScreenLog.out("about to shutdown");
					pr = run.exec("/sbin/poweroff");
					pr.waitFor() ;
					content="Server powering down";
					break;
				case ADMIN_UPGRADE_RPM:
					ScreenLog.out("about to upgrade (install rpms)");
					pr = run.exec("/usr/bin/comet_install_rpms.sh");
					pr.waitFor() ;
					buf = new BufferedReader( new InputStreamReader( pr.getInputStream() ) ) ;
		            while ( (line = buf.readLine()) != null)
		                content+=line+"\n";
				break;
				case ADMIN_UPGRADE_BIN:
					ScreenLog.out("about to upgrade (apply binary)");
					pr = run.exec("/usr/bin/comet_upgrade.sh");
					pr.waitFor() ;
					buf = new BufferedReader( new InputStreamReader( pr.getInputStream() ) ) ;
		            while ( (line = buf.readLine()) != null)
		                content+=line+"\n";
				break;
				
				case ADMIN_BACKUP:
					ScreenLog.out("backup comet.properties to HCP");
//					client.setRootpath(CometProperties.getInstance().getDestinationRootPath());
					FileInputStream fis=null;
					fis=new FileInputStream(CometProperties.getPropertiesFile());
					//check for existance first
					CometProperties.setConfigMode(true);
					encodedPathURL=new URL(CometProperties.getInstance().getDestinationRootPath()+"/"+CometProperties.getInstance().getCometName()+".properties");
					ScreenLog.out("writing to URL: "+encodedPathURL);
					if(client.HCPObjectExists(encodedPathURL)) {
						client.HttpDeleteHCPContent(encodedPathURL);
					}
					
					client.HttpPutHCPContent(new InputStreamEntity( fis, -1), encodedPathURL);
					fis.close();					
					CometProperties.setConfigMode(false);

					content="backup complete";
				break;
				case ADMIN_RESTORE:
					ScreenLog.out("restore comet.properties from HCP");
					
					CometProperties.setConfigMode(true);

					encodedPathURL=new URL(CometProperties.getInstance().getDestinationRootPath()+"/"+CometProperties.getInstance().getCometName()+".properties");
					if(client.HCPObjectExists(encodedPathURL)) {
						HCPClient.InputStreamToFile(client.HttpGetHCPContentStream(encodedPathURL), new File(CometProperties.getPropertiesFile()));	
						content="restore complete";
					} else {
						content="unable to restore comet.properties, file not found";
					} 
					CometProperties.setConfigMode(false);

				break;
				
				case ADMIN_COMET_STATUS:
					pr=run.exec("service comet status");
					pr.waitFor() ;
					buf = new BufferedReader( new InputStreamReader( pr.getInputStream() ) ) ;
					content=buf.readLine();
				break;	
				case ADMIN_COMET_START:
					pr=run.exec("service comet start");
					pr.waitFor() ;
					buf=new BufferedReader( new InputStreamReader( pr.getInputStream() ) ) ;
					content=buf.readLine();
				break;	
				case ADMIN_COMET_STOP:
					pr=run.exec("service comet stop");
					pr.waitFor() ;
					buf = new BufferedReader( new InputStreamReader( pr.getInputStream() ) ) ;
					content=buf.readLine();
				break;	
				case ADMIN_INGEST_CONTINUE:
					if(CometProperties.getInstance().getStopFileName().delete()) content="{ \"ingest_continues\" : true }";
					else content="{ \"ingest_continues\" : false }";
				break;
				
				case ADMIN_HCPCHECK:	
					//client.HttpGetHCPHeader(client.getRootpath());
					content="{ \"hcp_status\" : \""+client.GetHCPStatus()+"\" }";
					//new Boolean(client.getHeaders().containsKey("X-HCP-Type")).toString()+"}";
					
				break;
				case ADMIN_AUTHORIZE:
					Boolean authorized=CometProperties.getInstance().getDestinationAutoLoginEnabled();
					if(parameters.containsKey("logout")) {
						
						ServletHelper.invalidateSession(request);
						
						content="{ \"authorized\" : false, "+
								 "\"admin_user\" : false, "+
								 "\"username\" : \"\", "+
								 "\"password\" : \"\", "+
								 "\"role\" : \"\" }";
						return content;
					}
					
					
					
					String temp=client.GetHCPStatus();
					String result="";
					
					parameters=ServletHelper.SessionManager(request,parameters);
					//if authorized, skip ahead because we're doing autologin
					
					//if session is in play, let's verify the incoming username and role against it
					if(!authorized && parameters.containsKey("session_user") && parameters.containsKey("session_role") && parameters.containsKey("session_auth")) {
						authorized=parameters.get("session_user").equals(parameters.get("username")) && parameters.get("session_role").equals(parameters.get("role"));
					}		
					//if still not authorized, then we must authenticate
					if(!authorized) {
						//entire if block was indented, no changes
						if(!temp.contains("down")) { 
							ScreenLog.out("HCP is up... attempting authentication");
							client.setsEncodedUserName(HCPUtils.toBase64Encoding(parameters.get("username")));
							client.setsEncodedPassword(HCPUtils.toMD5Digest(parameters.get("password")));
							content="";
							client.HttpGetHCPHeader(client.getRootpath());
							authorized=new Boolean(client.getStatusCode()/100==2);
						} else {
							ScreenLog.out("HCP is down... fall back on host OS");
							
							pr = run.exec("/usr/bin/passcheck "+parameters.get("username")+" -verbose");
							OutputStream os=pr.getOutputStream();
							
							os.write(parameters.get("password").getBytes());
							os.close();
							pr.waitFor() ;
							buf = new BufferedReader( new InputStreamReader( pr.getInputStream() ) ) ;
							//while(buf.)
							while ( (temp = buf.readLine()) != null) result+=temp;
							if(result.equals("success")) {
								authorized=true;
							} else {
								authorized=false;
							}
						}
						
						//if we authenticated correctly, plant the seed for repeatable access without login
						if(authorized) {
							ServletHelper.setSessionAttribute(request,"session_user",parameters.get("username"));
							ServletHelper.setSessionAttribute(request,"session_role",parameters.get("role"));
							ServletHelper.setSessionAttribute(request,"session_auth","true");
							
						}	
						
					}
					
					content="{ \"authorized\" : "+authorized.toString()+", "+
								 "\"admin_user\" : "+CometProperties.getInstance().isAdminUser(parameters.get("username")).toString()+", "+
								 "\"username\" : \""+parameters.get("username")+"\", "+
								 "\"password\" : \""+parameters.get("password")+"\", "+
								 "\"role\" : \""+parameters.get("role")+"\" }";
				break;
				case ADMIN_MOUNTCHECK:
			
					pr = run.exec("/bin/mount");
					pr.waitFor() ;
					buf = new BufferedReader( new InputStreamReader( pr.getInputStream() ) ) ;
					
		            while ( (line = buf.readLine()) != null)
		            	content+=line+"\n";
		            content+="\n";
		            
		          	pr = run.exec("/bin/df -h");
					pr.waitFor() ;
					buf = new BufferedReader( new InputStreamReader( pr.getInputStream() ) ) ;
					
		            while ( (line = buf.readLine()) != null)
		            	content+=line+"\n";

				break;
				case ADMIN_RPM:
					pr = run.exec("/usr/bin/comet_packagecheck.sh");
					pr.waitFor() ;
					buf = new BufferedReader( new InputStreamReader( pr.getInputStream() ) ) ;
					while ( (line = buf.readLine()) != null)
						content+=line+"\n";
					
				break;	
				case ADMIN_UPTIME:
					pr = run.exec("/usr/bin/uptime");
					pr.waitFor() ;
					buf = new BufferedReader( new InputStreamReader( pr.getInputStream() ) ) ;
					while ( (line = buf.readLine()) != null)
						content+=line+"\n";
				break;
				case ADMIN_MEMORY:
					pr = run.exec("/usr/bin/free");
					pr.waitFor() ;
					buf = new BufferedReader( new InputStreamReader( pr.getInputStream() ) ) ;
					while ( (line = buf.readLine()) != null)
						content+=line+"\n";
				break;
				case ADMIN_INGEST_START_TIME:
					fis=new FileInputStream("/opt/COMETDist/ingest-started");
					buf = new BufferedReader( new InputStreamReader( fis ) ) ;
					while ( (line = buf.readLine()) != null)
						content+=line+"\n";
				break;
				
				case ADMIN_CONFIGCHECK:
					pr = run.exec("/usr/bin/comet_showvars.sh");
					pr.waitFor() ;
					buf = new BufferedReader( new InputStreamReader( pr.getInputStream() ) ) ;
					while ( (line = buf.readLine()) != null)
						content+=line+"\n";
				break;
				
				case ADMIN_DIRLIST:
					if(!parameters.containsKey("path")) parameters.put("path", "/opt/COMETDist");
					

					if(parameters.get("path").contains("*")) {
						ScreenLog.warning("globbing is not yet supported");
						content="err: globbing is not supported\n";
					} else {	
						pr = run.exec("/bin/ls -lah "+parameters.get("path"));
						pr.waitFor() ;
						buf = new BufferedReader( new InputStreamReader( pr.getInputStream() ) ) ;
					
		            	while ( (line = buf.readLine()) != null)
		            		content+=line+"\n";
					}
				break;
				case ADMIN_FILEREMOVE:
					if(!parameters.containsKey("path")) {
							ScreenLog.out("nothing to do!");
					} else if(parameters.get("path").equals("/")) {
						ScreenLog.severe("attempted to delete /");
					} else if(parameters.get("path").contains("*")) {
						ScreenLog.warning("globbing is not yet supported");
						content="globbing is not yet supported\n";

					}	else {
						
						try {
							pr = run.exec("/bin/rm -rvf "+parameters.get("path"));
							pr.waitFor() ;
							buf = new BufferedReader( new InputStreamReader( pr.getInputStream() ) ) ;
						
							while ( (line = buf.readLine()) != null)
								content+=line+"\n";
						} catch (Exception e) {
							ScreenLog.severe("exception: unable to delete path: "+parameters.get("path"));
						} finally {	
							if(content.equals("")) content="unable to delete path: "+parameters.get("path")+"\n";
						}
					}	
				break;
				case ADMIN_HAHA:	
					//client.HttpGetHCPHeader(client.getRootpath());
					
					//want to know:
					//solo, primary, secondary
					//partner name
					//condition of partner
					
					String currentState="solo";
					
					if(!CometProperties.isSolo()) {
						if(CometProperties.isPrimary()) currentState="primary";
						else currentState="secondary";
					}
					
					content="{ \"comet_haha_state\" : \""+currentState+"\"," +
							"\"comet_partner_name\" : \""+CometProperties.getPartner()+"\"," + 
							"\"comet_partner_condition\" : \""+CometAction.getPartnerCondition(CometProperties.getPartner())+"\"" +
							" }";
					
					
					
					//new Boolean(client.getHeaders().containsKey("X-HCP-Type")).toString()+"}";
					
				break;
				default:
					ScreenLog.out("operation not supported");
			}
		} catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }
		ScreenLog.end("AdminEngine:Action(client,mprops,parameters)");
		return content;
	}

	
	public static void Get(HttpServletRequest request, HttpServletResponse response)  {
		try {
			response.setHeader("Cache-Control", "no-cache");
			response.setHeader("Pragma", "no-cache");
			Map<String, String> parameters=ServletHelper.GetParameterMap(request);
			parameters.put("hget", "true");
			Action(request,parameters, response,true);
		} catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }
	} //end Relay front end
	public static void Put(HttpServletRequest request, HttpServletResponse response)  {
		try {
			response.setHeader("Cache-Control", "no-cache");
			response.setHeader("Pragma", "no-cache");
			Map<String, String> parameters=ServletHelper.GetParameterMap(request);
			parameters.put("hput", "true");
			Action(request, parameters, response,false);
		} catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }
	} //end Relay front end

	public static void Action(HttpServletRequest request, Map<String,String> parameters, HttpServletResponse response, Boolean isGet) throws Exception {		
			ScreenLog.begin("AdminEngine::Action(parameters,response,isGet="+isGet+")");
			ScreenLog.out(parameters,"parameters");
			//users and roles are built in and stored statically
			CometProperties mProps=CometProperties.getInstance();
			HCPClient client=new HCPClient(mProps);
			PrintWriter writer =response.getWriter();
			writer.write(Action(request,client, mProps, parameters));
			ScreenLog.end("AdminEngine::Action(parameters,response,isGet="+isGet+")");
	}

	public static void AuthorizeGet(HttpServletRequest request, HttpServletResponse response)  {
			try {
				response.setHeader("Cache-Control", "no-cache");
				response.setHeader("Pragma", "no-cache");
				Map<String, String> parameters=ServletHelper.GetParameterMap(request);
				parameters.put("hget", "true");
				parameters.put("action", "authorize");
				Action(request, parameters, response,true);
			} catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }
	}
}


 
