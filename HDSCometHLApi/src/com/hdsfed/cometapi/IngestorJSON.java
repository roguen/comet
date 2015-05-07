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
//SVN: r554


package com.hdsfed.cometapi;


import java.io.IOException;
import java.util.LinkedList;

import org.codehaus.jackson.annotate.JsonProperty;

public class IngestorJSON {
	private static ExtendedLogger ScreenLog = new ExtendedLogger(IngestorJSON.class.getPackage().getName());
	private static IngestorJSON instance;
    private ThreadJSON[] threadData;

	
	public void init() {
		
		threadData=new ThreadJSON[CometProperties.getInstance().getMaximumThreads()];
		for(int i=0; i<threadData.length; i++) {
			threadData[i]=new ThreadJSON();
			threadData[i].Init(i);
		}
		
	}
	
	public static Boolean isInstanceAvailable() {
		return (null!=instance);
	}
	
	public static void resetInstance()  {
		instance=null;
		instance = getInstance();
	}

	public static IngestorJSON getInstance() {
		if (instance == null) {
				instance = new IngestorJSON();
				instance.init();
		}
		return instance;
	}

	public void setData(int id, String path, long length) {
		threadData[id].id=id;
		threadData[id].path=path;
		threadData[id].size=length;
		threadData[id].start_time=System.currentTimeMillis();
	}
	
	public String getPath(int i) {
		return threadData[i].path;
	}
	public long getSize(int i) {
		return threadData[i].size;
	}
	
	
	//temporary function to load data from DB
	public void PopulateFromDB(ThreadTrackerDB db) {
		ScreenLog.begin("PopulateFromDB()");
		ThreadData [] td=db.getAll();
		
		
		ScreenLog.out("\n\nsize of threaddata array = "+threadData.length);
		ScreenLog.out("\n\nsize of db array = "+td.length);

		
		for(int i=0; i<threadData.length; i++) {
			threadData[i].size=td[i].getLength();
			threadData[i].id=td[i].getId();
			threadData[i].start_time=0; //Long.getLong(td[i].getDatestamp());
			threadData[i].path=td[i].getPath();
		}
		ScreenLog.end("PopulateFromDB()");

	}
	
	

	/*
	void Init(CometProperties props) {
		id=i;
		path="none";
		size=0;
		start_time=0;
		bytes_written=0;
		status="idle";
		custom="";
		error="";
		tgt="";
	}
	
	
    @JsonProperty("id")
	public int id;
	
    @JsonProperty("path")
	public String path;
    
    @JsonProperty("size")
    public long size;
    
    @JsonProperty("start_time")
	public long start_time;
    
    @JsonProperty("bytes_written")
    public long bytes_written;

    @JsonProperty("status")
	public String status;

    @JsonProperty("custom")
    public String custom;

    @JsonProperty("error")
    public String error;

    @JsonProperty("tgt")
	public String tgt;
    */
	
	
/*	content+="{\"heartbeat_datestamp\":\""+CometProperties.getHeartBeat()+"\",\"last_directory\":\""+CometProperties.getLastDirectory()
			+"\", \"complete\" : "+CometProperties.getIngestionComplete().toString()+", \"running\" : \""+CometProperties.getIngestorRunningState()+"\""
			+", \"mode\" : \""+CometProperties.getInstance().getIngestionMode()+"\""
			+", \"iteration\" : "+CometProperties.getIngestorIteration()
			+", \"percent_complete\" : "+CometProperties.getPercentComplete()
			+", \"upload_pool_size\" : \""+CometProperties.getUploadPoolSize()+"/"+CometProperties.getInstance().getMaxFiles()+"\""
			+", \"time_in_iteration\" : "+CometProperties.getTimeInIteration();*/

    
    
    public String ThreadDataToString() {
    	String tmp="";
    	for(int i=0; i<threadData.length; i++) {
    		tmp+=threadData[i].toString();
    		if(i+1!=threadData.length) tmp+=", ";
    	
    	}
    	return "[ "+tmp+" ]";
    }
    
    
    @Override
    public String toString() {
    	return " { " +
    			"\"hb_datestamp\": \""+CometProperties.getHeartBeat()+"\", "+
    			"\"last_dir\": \""+CometProperties.getLastDirectory()+"\", "+
    			//fixme
    			"\"state\": \""+CometProperties.getIngestorRunningState()+"\", "+
    			"\"iteration\" : "+CometProperties.getIngestorIteration()+", "+
    			"\"percent_complete\" : "+CometProperties.getPercentComplete()+", "+
    			"\"upload_pool_size\" : \""+CometProperties.getUploadPoolSize()+"/"+CometProperties.getInstance().getMaxFiles()+"\", "+
    			"\"time_in_iteration\" : "+CometProperties.getTimeInIteration()+", "+
    			"\"threaddata\": "+
					ThreadDataToString()+"}";
    			//"\"thread\": "+ThreadDataToString()+"}}}";

    	/*
    	{"menu": {
    		  "id": "file",
    		  "value": "File",
    		  "popup": {
    		    "menuitem": [
    		      {"value": "New", "onclick": "CreateNewDoc()"},
    		      {"value": "Open", "onclick": "OpenDoc()"},
    		      {"value": "Close", "onclick": "CloseDoc()"}
    		    ]
    		  }
    		}}
    	
    	
        return "{ \"id\" : "+id+
        		"," +
        		"\"path\" : "+path + 
        		"," +
        		"\"size\" : "+size+
        		","+
        		"\"start_time\" : "+start_time + 
        		"," +
        		"\"bytes_written\" : "+bytes_written + 
        		"," +
        		"\"status\" : \""+status+"\"" +
        		"," +
        		"\"custom\" : \""+custom+"\"" +
        		"," +
        		"\"error\" : \""+error+"\"" +
        		"," +
        		"\"tgt\" : \""+tgt+"\"" +
        		" }";
    }
    */
    }
}