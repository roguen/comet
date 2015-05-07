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

import java.io.PrintWriter;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hdsfed.cometapi.AnnotationGenerator;
import com.hdsfed.cometapi.AnnotationHelper;
import com.hdsfed.cometapi.CometProperties;
import com.hdsfed.cometapi.ExtendedLogger;
import com.hdsfed.cometapi.HCPClient;
import com.hdsfed.cometapi.StringHelper;


//TODO: consider deprecating this class
public class HCPInterface {
	private static ExtendedLogger ScreenLog = new ExtendedLogger(HCPInterface.class.getPackage().getName());

	private static Map<String,String> annotations;
	public HCPInterface() {
		setAnnotations(new TreeMap<String,String>());
	}
	
	public static String FilePath2Output(URL fp) throws Exception {
		return StringHelper.MapToJSONString(AnnotationGenerator.COMETMetadataMapFromURL(fp, null));
	}
	public static String FilePath2Output(URL fp, String tag) throws Exception {
		return StringHelper.MapToJSONString(AnnotationGenerator.COMETMetadataMapFromURL(fp, tag));
	}

	//TODO: rewrite this function using Relay
	public void ReloadMetadata(HttpServletRequest request, HttpServletResponse response) {
		ScreenLog.out("HCPInterface::ReloadMetadata");
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Pragma", "no-cache");
		CometProperties mprops=CometProperties.getInstance();
		try {
			// Write the content to disk.
			//InputStream responseStream = 
			PrintWriter out = response.getWriter();
			
			String tag,path; //,annotation;
			tag=request.getParameter("tag").split(",")[0];
			path=request.getParameter("tag").split(",")[1];
			out.println(FilePath2Output(AnnotationHelper.PathToURL(mprops.getDestinationRootPath(), path),tag));
		} catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }
	}
	
	/**
	 * @param request
	 * @param response
	 */
	//TODO: deprecated by Download and Relay
	public void PullMetadata(HttpServletRequest request, HttpServletResponse response) {
		ScreenLog.begin("HCPInterface::PullMetadata()");
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Pragma", "no-cache");
		CometProperties mprop=CometProperties.getInstance();
		try {
			HCPClient client=new HCPClient(mprop);
			PrintWriter out = response.getWriter();
			ScreenLog.out("sending to HttpGetHCPContent "+
					AnnotationHelper.PathAndAnnotationToURL(client.getRootpath(), request.getParameter("filename"),request.getParameter("annotation")).toString());
			out.println(client.HttpGetHCPContent(AnnotationHelper.PathAndAnnotationToURL(client.getRootpath(), request.getParameter("filename"),request.getParameter("annotation"))));
		} catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }
		ScreenLog.end("HCPInterface::PullMetadata()");
	}
	
	//TODO: deprecated by Download and Relay
	public static void PushCustomMetadata(String xml_content, String fp, String annotation) {
		try {
			CometProperties mprop=CometProperties.getInstance();
			HCPClient client=new HCPClient(mprop);
			client.HttpPutHCPContent(xml_content, AnnotationHelper.PathAndAnnotationToURL(client.getRootpath(), fp,annotation));
			ScreenLog.out("completion!");
		} catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }
	}
	
	//TODO: deprecated by Download and Relay
	public void PushMetadata(HttpServletRequest request, HttpServletResponse response) {
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Pragma", "no-cache");
		String fp=request.getParameter("filename");
		setAnnotations(AnnotationHelper.AnnotationMapParseRequest(request));
		CometProperties mprop=CometProperties.getInstance();
		try {
			HCPClient client=new HCPClient(mprop);
			AnnotationHelper.HttpPutAnnotationstoHCP(AnnotationHelper.AddCombinedAnnotationToMap(annotations, mprop.getCombinedAnnotation()), client, fp);
		} catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }
	}
	static public Map<String,String> getAnnotations() {
		return annotations;
	}
	static public void setAnnotations(Map<String,String> inAnnotations) {
		annotations = inAnnotations;
	}
}

