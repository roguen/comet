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
//SVN: r551+
package ingestor.postprocessing;


import ingestor.FileSystemWalker;
import ingestor.PauseStopProcessor;

import com.hdsfed.cometapi.CometProperties;
import com.hdsfed.cometapi.ExtendedLogger;
import com.hdsfed.cometapi.ThreadTrackerDB;

//TODO: consider moving exceptions out
public class TimePostProcessor extends BasePostProcessor {
	private static final String VERSION ="1.21.0";
	private static final String COMPILE_DATE ="2015-05-06";
	private static final String SVN ="551+";
	private static ExtendedLogger ScreenLog = new ExtendedLogger(TimePostProcessor.class.getName());

	// Local member variables.
	private Boolean bIsInitialized = false;
	private CometProperties mProps;
	private int sleepInSeconds=0;
	// Processor classes.
	PauseStopProcessor mPauseStopProcessor;
	
	// file system walker
	FileSystemWalker mFileSystemWalker;
	

	@Override
	protected void initialize(String inTime) {
		ScreenLog.setClass(TimePostProcessor.class.getName());
		if (! bIsInitialized)  // Only initialize if we haven't already
		{
			// Setup properties member.
			mProps = CometProperties.getInstance();
			ScreenLog.begin("TimePostProcessor::initialize()");
			mPauseStopProcessor = new PauseStopProcessor(mProps);
			
			if(inTime==null) sleepInSeconds=mProps.getPauseSleepTime();
			else sleepInSeconds=Integer.parseInt(inTime);
			
		}
	}

	@Override
	protected void runPostProcess() throws Exception {
		ScreenLog.setClass(TimePostProcessor.class.getName());
		ScreenLog.begin("runPostProcess::TimePostProcessor");
		ScreenLog.info("\tVersion: "+VERSION+" r"+SVN);
		ScreenLog.info("\tCompiled on: "+COMPILE_DATE);
		ScreenLog.info("\tusing library version:"+CometProperties.getVersion()+" r"+CometProperties.getSvn()+" compiled on "+CometProperties.getCompileDate());
		CometProperties.setPercentComplete(0);
		CometProperties.setTimeInIteration(0);
		CometProperties.setTimeInIterationTimeStamp(System.currentTimeMillis());
		ThreadTrackerDB.heartBeatOverHTTP("none","time-post-proc-init");

		int localSleep=sleepInSeconds;
		ScreenLog.fine("\tLoop sleep for " + localSleep + " seconds");
		// Only sleep in 5 seconds increments so we can check for stop/pause request.
		while (localSleep > 0 && mPauseStopProcessor.checkContinue()) {
			ScreenLog.out("sleepInSeconds="+sleepInSeconds);
			ScreenLog.out("localSleep="+localSleep);
			ScreenLog.out("percentage="+(((sleepInSeconds-localSleep)*100)/sleepInSeconds));
			CometProperties.setPercentComplete(((sleepInSeconds-localSleep)*100)/sleepInSeconds);
			CometProperties.setTimeInIteration(System.currentTimeMillis()-CometProperties.getTimeInIterationTimeStamp());
			ThreadTrackerDB.heartBeatOverHTTP("none","sleeping...");
			int thisSleep = (localSleep > 5 ? 5 : localSleep);
			Thread.sleep(thisSleep * 1000);
			localSleep -= thisSleep;
			// Have the stop file override the pause file.
			if (! mPauseStopProcessor.checkContinue() ) {
				//ingestor.stop will kill the ingestor
				ScreenLog.out("checkcontinue = true, break out");
				break; // need to stop.  break out of this loop.
			}
		}
		ScreenLog.fine("\tLoop slept for " + localSleep + " seconds");
		CometProperties.setPercentComplete(100);
		CometProperties.setTimeInIteration(System.currentTimeMillis()-CometProperties.getTimeInIterationTimeStamp());
		ThreadTrackerDB.heartBeatOverHTTP("none","time-post-proc-complete");
		ScreenLog.end("runPostProcess::TimePostProcessor");
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
}
