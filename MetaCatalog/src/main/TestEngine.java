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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hdsfed.cometapi.AnnotationHelper;
import com.hdsfed.cometapi.CometProperties;
import com.hdsfed.cometapi.ExtendedLogger;
import com.hdsfed.cometapi.HCPClient;
import com.hdsfed.cometapi.ServletHelper;
import com.hdsfed.cometapi.CometStorageURI;
import com.hdsfed.cometapi.StringHelper;

//TODO: need function descriptions
//NOTE: the purpose of this class is to provide a proving ground for new concepts
public class TestEngine {
	private static ExtendedLogger ScreenLog = new ExtendedLogger(TestEngine.class.getPackage().getName());
	public enum testActionType {
		TEST_ROLE,
		TEST_AUTH,
		TEST_RPM,
		TEST_PATH,
		TEST_REPEAT,
		TEST_SHELL,
		TEST_UNSUPPORTED
	};

	private static testActionType testActionType(String string) {
		//input: role, output: look up properties for this role in roles.json
		if(string.toLowerCase().contains("role")) return testActionType.TEST_ROLE;
		if(string.toLowerCase().contains("auth")) return testActionType.TEST_AUTH;
		if(string.toLowerCase().contains("rpm")) return testActionType.TEST_RPM;
		if(string.toLowerCase().contains("path")) return testActionType.TEST_PATH;
		if(string.toLowerCase().contains("repeat")) return testActionType.TEST_REPEAT;
		if(string.toLowerCase().contains("shell")) return testActionType.TEST_SHELL;
 
		return testActionType.TEST_UNSUPPORTED;
	}
	
	private static String Action(HCPClient client, CometProperties mProps, Map<String, String> parameters) {
		ScreenLog.begin("TestEngine::Action(client,mprops,parameters)");
		String content="";
		Runtime run = Runtime.getRuntime() ;
		Process pr = null;
		//URL encodedPathURL=null;
		BufferedReader buf = null;
		String line=null;
		try {
			switch(testActionType(parameters.get("action"))) {
				case TEST_ROLE:
					ScreenLog.out("test role");
					content="test role::: ";
					content+="action="+parameters.get("action")+" role="+parameters.get("role");
					
					
					SearchManager sm=new SearchManager();
					content+=" search_criteria="+sm.getRoleSearchConstraints(parameters.get("role"));
					
				break;
				case TEST_AUTH:
					
					pr = run.exec("/usr/bin/passcheck "+parameters.get("user")+" -verbose");
					OutputStream os=pr.getOutputStream();
					
					os.write(parameters.get("password").getBytes());
					os.close();
					pr.waitFor() ;
					buf = new BufferedReader( new InputStreamReader( pr.getInputStream() ) ) ;
				
					while ( (line = buf.readLine()) != null)
						content+=line;
					
					if(content.equals("success")) {
						content="password checks out";
					} else {
						content="password fails";
					}
				break;
				case TEST_RPM:
					pr = run.exec("/usr/bin/comet_packagecheck.sh");
					
					pr.waitFor() ;
					buf = new BufferedReader( new InputStreamReader( pr.getInputStream() ) ) ;
				
					while ( (line = buf.readLine()) != null)
						content+=line;
					
				break;	

				case TEST_PATH:
					ScreenLog.out(parameters);
								// /opt/COMETDist/InputDir/Media/Digital/Video/something.m4v
					content="file path, while on COMET: "+parameters.get("path")+"\n\n";
					content+="source directory: "+CometProperties.getInstance().getSourcePath()+"\n\n";
					content+="===========================\n";
					
					CometStorageURI uri=new CometStorageURI(new File(parameters.get("path")), null, CometProperties.getInstance().getDestinationRootPath());
					
					content+="original file on COMET: "+uri.getFile().getAbsolutePath()+"\n\n";
					content+="storage URI on HCP: "+uri.getUri()+"\n\n";
					content+="URL on HCP: "+uri.getHcpURL()+"\n\n";
					content+="temp file with changed extension to .webm (on COMET):\n";
					uri.ChangeExtension("webm");
					content+=uri.getTempFile().getAbsolutePath()+"\n\n";
					content+="temp file with changed extension on HCP: "+uri.getHcpURL()+"\n\n";
					
				break;	

				case TEST_REPEAT:
					content="content="+parameters.get("content");
		
				break;
				case TEST_SHELL:
					ScreenLog.begin("test_shell");
					File f=new File("/opt/COMETDist/InputDir/S3E3 - Hello Dancin' Homer World.m4v");
					content="hello world";
					
					String cmdline="/usr/bin/stuffit.sh -l __FILENAME__";
					
					ScreenLog.force(cmdline);
					
					content+=StringHelper.Popen3(cmdline,f.getAbsolutePath(), true, true);
					//content+=Popen3("/usr/bin/stuffit.sh -l "+f.getAbsolutePath(),true,true);
					ScreenLog.end("test_shell");
					break;
				case TEST_UNSUPPORTED:
				default:
					ScreenLog.out("operation not supported");
			}
		} catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }
		ScreenLog.end("TestEngine:Action(client,mprops,parameters)");
		return content;
	}
	
	static File GetLocalMaster(File f) {
		return new File(f.getParentFile().toString()+"/thumb.png");
	}

	public static void Get(HttpServletRequest request, HttpServletResponse response)  {
		try {
			response.setHeader("Cache-Control", "no-cache");
			response.setHeader("Pragma", "no-cache");
			Map<String, String> parameters=ServletHelper.GetParameterMap(request);
			parameters.put("hget", "true");
			Action(parameters, response,true);
		} catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }
	} //end Relay front end
	public static void Put(HttpServletRequest request, HttpServletResponse response)  {
		try {
			response.setHeader("Cache-Control", "no-cache");
			response.setHeader("Pragma", "no-cache");
			Map<String, String> parameters=ServletHelper.GetParameterMap(request);
			parameters.put("hput", "true");
			Action(parameters, response,false);
		} catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }
	} //end Relay front end

	public static void Action(Map<String,String> parameters, HttpServletResponse response, Boolean isGet) throws Exception {		
			ScreenLog.begin("AdminEngine::Action(parameters,response,isGet="+isGet+")");
			ScreenLog.out(parameters,"parameters");
			//users and roles are built in and stored statically
			CometProperties mProps=CometProperties.getInstance();
			HCPClient client=new HCPClient(mProps);
			PrintWriter writer =response.getWriter();
			writer.write(Action(client, mProps, parameters));
			ScreenLog.end("AdminEngine::Action(parameters,response,isGet="+isGet+")");
	}
}


 