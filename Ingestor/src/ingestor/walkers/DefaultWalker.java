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
//Package: COMET::Data Ingestor Service
//Author: Chris Delezenski <chris.delezenski@hdsfed.com>
//Compilation Date: 2015-05-06
//License: Apache License, Version 2.0
//Version: 1.21.0
//(RPM) Release: 1
//SVN: r554
package ingestor.walkers;


import ingestor.FileSystemWalker;
import ingestor.PauseStopProcessor;
import ingestor.ThreadManager;

import com.hdsfed.cometapi.CometProperties;
import com.hdsfed.cometapi.ExtendedLogger;
import com.hdsfed.cometapi.ThreadTrackerDB;

//TODO: consider moving exceptions out
public class DefaultWalker extends BaseWalker {
	private static final String VERSION ="1.21.0";
	private static final String COMPILE_DATE ="2015-05-06";
	private static final String SVN ="554";
	private static ExtendedLogger ScreenLog = new ExtendedLogger(DefaultWalker.class.getName());

	// Local member variables.
	private Boolean bIsInitialized = false;
	private CometProperties mProps;
	// Processor classes.
	PauseStopProcessor mPauseStopProcessor;
	
	// file system walker
	FileSystemWalker mFileSystemWalker;
	ThreadManager mThreadman;

	@Override
	protected void initialize(String inSourcePath) {
		ScreenLog.setClass(DefaultWalker.class.getName());
		if (! bIsInitialized)  // Only initialize if we haven't already
		{
			// Setup properties member.
			mProps = CometProperties.getInstance();
			ScreenLog.begin("DefaultWalker::initialize()");
			mPauseStopProcessor = new PauseStopProcessor(mProps);
			
			mThreadman = new ThreadManager();
			mThreadman.initialize(mProps.getMaximumThreads());
			
			mFileSystemWalker = new FileSystemWalker(mThreadman, mPauseStopProcessor, mProps, mProps.getSourcePath());
			mFileSystemWalker.setAlwaysUsePrefix(mProps.alwaysUsePrefix());
			
			//default walker should behave exactly like it did before
			if(mProps.useSortPreferences()) {
				mFileSystemWalker.setUseSortPreferences(mProps.useSortPreferences());
				mFileSystemWalker.setSortPreferences(mProps.getSortPreferences());
			}
			ScreenLog.force("Class: "+this.getClass().getName());
			ScreenLog.force("\tVersion: "+VERSION+" r"+SVN);
			ScreenLog.force("\tCompiled on: "+COMPILE_DATE);
			ScreenLog.force("\tusing library version:"+CometProperties.getVersion()+" r"+CometProperties.getSvn()+" compiled on "+CometProperties.getCompileDate());
			
		}
	}

	@Override
	protected void run() throws Exception {
		ScreenLog.setClass(DefaultWalker.class.getName());
		ScreenLog.begin("DefaultWalker::run");
		CometProperties.setPercentComplete(0);
		CometProperties.setTimeInIteration(0);
		CometProperties.setTimeInIterationTimeStamp(System.currentTimeMillis());
		ThreadTrackerDB.heartBeatOverHTTP("none","default-walk");

		if(!CometProperties.getSkipWalk()) {
			ScreenLog.force("begin default walk");
			mFileSystemWalker.doWalk(null,"","","");
		} else {
			ScreenLog.force("skipping default walk");
		}

		CometProperties.setPercentComplete(99);
		CometProperties.setTimeInIteration(System.currentTimeMillis()-CometProperties.getTimeInIterationTimeStamp());
		ThreadTrackerDB.heartBeatOverHTTP("none","waiting-for-threads");
		mThreadman.waitForThreads();
		
		CometProperties.setPercentComplete(100);
		CometProperties.setTimeInIteration(System.currentTimeMillis()-CometProperties.getTimeInIterationTimeStamp());
		ThreadTrackerDB.heartBeatOverHTTP("none","default-walk-complete");

		
		ScreenLog.end("DefaultWalker::run");
	}

	public static String getVersion() {
		return VERSION;
	}

	public static String getCompileDate() {
		return COMPILE_DATE;
	}

	public static String getSvn() {
		return SVN;
	}

	@Override
	protected void waitForThreads() {
		// TODO Auto-generated method stub
		mThreadman.waitForThreads();
	}
}
