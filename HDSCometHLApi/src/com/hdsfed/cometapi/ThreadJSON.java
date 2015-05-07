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

import org.codehaus.jackson.annotate.JsonProperty;

public class ThreadJSON {
	private static ExtendedLogger ScreenLog = new ExtendedLogger(ThreadJSON.class.getPackage().getName());

	public void Init(int i) {
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
	public int id=-1;
	
    @JsonProperty("path")
	public String path="";
    
    @JsonProperty("size")
    public long size=0;
    
    @JsonProperty("start_time")
	public long start_time=0;
    
    @JsonProperty("bytes_written")
    public long bytes_written=0;

    @JsonProperty("status")
	public String status="";

    @JsonProperty("custom")
    public String custom="";

    @JsonProperty("error")
    public String error="";

    @JsonProperty("tgt")
	public String tgt="";
    
    
    
    @Override
    public String toString() {
        return "{ \"id\" : "+id+
        		"," +
        		"\"path\" : \""+path + "\"" + 
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
    
}
