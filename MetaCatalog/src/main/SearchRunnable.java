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

import com.hdsfed.cometapi.ExtendedLogger;

import main.SearchManager.SearchEngine;

//TODO: redesign such that run() should catch all exceptions
//		need to make sure that everything is thread safe
public class SearchRunnable implements Runnable {
	private static ExtendedLogger ScreenLog = new ExtendedLogger(SearchRunnable.class.getPackage().getName());

	private String query;
	private boolean releaseable=false;
	private String content;
	private SearchEngine mode;
	private boolean configMode=false;
	
	public SearchRunnable(String inQuery, SearchEngine inMode, Boolean inConfigMode) {
		// TODO Auto-generated constructor stub
		setQuery(inQuery);
		setMode(inMode);
		setConfigMode(inConfigMode);
	}

	@Override
	public void run() {
		ScreenLog.force("BEGIN:SearchRunnable::run(thread="+mode.getIndex()+")");
		//don't bother running an empty query, just die immediately
		if(getQuery().trim().equals("")) {
			setContent("");
			return;
		}
		this.content=SearchManager.ParseResults(SearchManager.searchFiles(getMode(), query, configMode),getMode());
		
		ScreenLog.force("\n\n\n"+this.content+"\n\n\n");
		
		
		ScreenLog.force("END:SearchRunnable::run(thread="+mode.getIndex()+") - should terminate now");
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public boolean isReleaseAble() {
		// TODO Auto-generated method stub
		return releaseable;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public SearchEngine getMode() {
		return mode;
	}

	public void setMode(SearchEngine mode) {
		this.mode = mode;
	}

	public boolean isConfigMode() {
		return configMode;
	}

	public void setConfigMode(boolean configMode) {
		this.configMode = configMode;
	}
}
