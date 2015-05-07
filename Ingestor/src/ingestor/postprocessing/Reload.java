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
package ingestor.postprocessing;


import ingestor.FileSystemWalker;
import ingestor.PauseStopProcessor;

import com.hdsfed.cometapi.CometProperties;
import com.hdsfed.cometapi.ExtendedLogger;

//TODO: consider moving exceptions out
public class Reload extends BasePostProcessor {
	private static final String VERSION ="1.21.0";
	private static final String COMPILE_DATE ="2015-05-06";
	private static final String SVN ="554";
	private static ExtendedLogger ScreenLog = new ExtendedLogger(Reload.class.getName());

	// Local member variables.
	private Boolean bIsInitialized = false;
	private CometProperties mProps;
	// Processor classes.
	PauseStopProcessor mPauseStopProcessor;
	
	// file system walker
	FileSystemWalker mFileSystemWalker;
	

	@Override
	protected void initialize(String inTime) {
		ScreenLog.setClass(Reload.class.getName());
		if (! bIsInitialized)  // Only initialize if we haven't already
		{
			// Setup properties member.
			mProps = CometProperties.getInstance();
			ScreenLog.begin("initialize post processor");
			mPauseStopProcessor = new PauseStopProcessor(mProps);
		}
	}

	@Override
	protected void runPostProcess() throws InterruptedException {
		ScreenLog.begin("runPostProcess::reload");
		ScreenLog.info("\tVersion: "+VERSION+" r"+SVN);
		ScreenLog.info("\tCompiled on: "+COMPILE_DATE);
		ScreenLog.info("\tusing library version:"+CometProperties.getVersion()+" r"+CometProperties.getSvn()+" compiled on "+CometProperties.getCompileDate());
		
		ScreenLog.fine("reload comet.properties");
		// Only sleep in 5 seconds increments so we can check for stop/pause request.
		CometProperties.resetInstance();
		ScreenLog.end("runPostProcess::reload");
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
