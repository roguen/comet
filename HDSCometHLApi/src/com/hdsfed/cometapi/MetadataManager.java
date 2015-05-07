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
//Package: Custom Object Metadata Enhancement Toolkit shared library
//Author: Chris Delezenski <chris.delezenski@hdsfed.com>
//Compilation Date: 2015-05-06
//License: Apache License, Version 2.0
//Version: 1.21.0
//(RPM) Release: 1
//SVN: r551+
package com.hdsfed.cometapi;

import java.util.Map;

//TODO: move exception handling out to clients
//		may be able to combine this class into others
public class MetadataManager {
	private static ExtendedLogger ScreenLog = new ExtendedLogger(MetadataManager.class.getPackage().getName());

	private static MetadataManager instance;
	private static String filePath;
	private static Map<String,String> SystemMetadataMap;
	
	public MetadataManager(String inFp, HCPClient client) {
		setFilePath(inFp);
		//system metadata is nearly equivalent to the headers of the object
		try {
			client.HttpGetHCPHeader(AnnotationHelper.PathToURL(client.getRootpath(), getFilePath()));
			setSystemMetadataMap(client.getHeaders());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			ScreenLog.ExceptionOutputHandler(e);
		}
	}

	public static String GetObjectSize() throws Exception  {
		if(SystemMetadataMap==null) throw new Exception();
		return SystemMetadataMap.get("X-HCP-Size");
	}
	
	public static String GetObjectHash() throws Exception  {
		if(SystemMetadataMap==null) throw new Exception();
		return SystemMetadataMap.get("X-HCP-Hash");
	}
	
	public static String GetObjectIngestTimeh() throws Exception  {
		if(SystemMetadataMap==null) throw new Exception();
		return SystemMetadataMap.get("X-HCP-IngestTime");
	}
	
	public static MetadataManager getInstance(String infp, HCPClient client) {
		//delete this instance and make new for a new file path
		if(getFilePath()!=infp) {
				instance=null;
		}
		
		if (instance == null) {
			instance = new MetadataManager(infp,client);
		}
		return instance;
	}
	
	public static void setInstance(MetadataManager instance) {
		MetadataManager.instance = instance;
	}
	
	public static String getFilePath() {
		return filePath;
	}
	
	public static void setFilePath(String filePath) {
		MetadataManager.filePath = filePath;
	}
	
	public static Map<String,String> getSystemMetadataMap() {
		return SystemMetadataMap;
	}
	
	public static void setSystemMetadataMap(Map<String,String> systemMetadataMap) {
		SystemMetadataMap = systemMetadataMap;
	}
}
