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

//NOTE: this code was originally developed by Cliff Grimm <clifford.grimm@hds.com>

package ingestor;

import java.io.File;

import com.hdsfed.cometapi.*;

//TODO: integrate this into other functions and move check continue to be a static function of CometProperties
public class PauseStopProcessor {
	
	//TODO: merge together
//	private static Logger logger = Logger.getLogger(PauseStopProcessor.class.getPackage().getName());
	
	//temporarily change ScreenLog to logger
	private static ExtendedLogger logger = new ExtendedLogger(PauseStopProcessor.class.getPackage().getName());
	private CometProperties mProps;
	private File stopFile;
	private File pauseFile;
	private File reloadFile;
	private File flushFile;
	
	public PauseStopProcessor(CometProperties inProps) {
		mProps = inProps;
		stopFile = mProps.getStopFileName();
		pauseFile = mProps.getPauseFileName();
		reloadFile = mProps.getReloadFileName();
		flushFile = mProps.getFlushCacheFileName();
	}
	
	public boolean pauseCondition() throws Exception {
	
		if(pauseFile.exists()) return true;
		
		//primary doesn't pause unless pausefile exists
		//if(CometProperties.isPrimary()) return false;
		
		//secondary checks to see if he should become prime
		if(!CometProperties.isSolo()) CometAction.getPartnerConditionIsWorking(CometProperties.getPartner());
	
		return false;
	}
	
	
	public boolean checkContinue() throws Exception {
		//  See if we were asked to stop.
		if ( stopRequested() ) {
			logger.warning("User requested stop.");
			CometProperties.Terminate();
			
			return false; // Outta here.
		}
	
		// See if we should pause as requested.
		if (pauseCondition()) {
			logger.warning("Pause requested by User");
			ThreadTrackerDB.heartBeatOverHTTP("none","paused");

			do {
				int sleepInSeconds = mProps.getPauseSleepTime();
				
				logger.fine("Pause sleep for " + sleepInSeconds + " seconds");
				
				Thread.sleep(sleepInSeconds * 1000);

				// Have the stop file override the pause file.
				if ( stopRequested() ) {
					logger.warning("User requested stop. Terminating pause.");
					
					logger.warning("full stop requested....");

					CometProperties.Terminate();
					stopFile.delete();
					return false; // need to stop.  break out of this loop.
				}
				
				if ( reloadRequested() ) {
					logger.warning("User requested reload of comet.properties file");
					CometProperties.resetInstance();
					pauseFile.delete();
					reloadFile.delete();
				}
				
				if ( flushRequested() ) {
					logger.warning("User requested file cache flush");
					ThreadTrackerDB.heartBeatOverHTTP("none","flushing...");

					FileTracker.flush(CometProperties.getInstance().getFileProcessedCache());
					FileTracker.init(true,CometProperties.getInstance().getFileProcessedCache(), CometProperties.getInstance().getMaxFiles());
					ThreadTrackerDB.heartBeatOverHTTP("none","flush completed");

					pauseFile.delete();
					flushFile.delete();
				}
				
				
			} while (pauseFile.exists());
			
			logger.warning("Pause resumed by User");

			ThreadTrackerDB.heartBeatOverHTTP("none","resume");

		}
		
		logger.warning("about to return WasTerminated()="+CometProperties.WasTerminated());
		return !CometProperties.WasTerminated();
	}

	// Check whether a stop was requested by user.
	boolean stopRequested() {
		return stopFile.exists();
	}
	boolean reloadRequested() {
		return reloadFile.exists();
	}
	boolean flushRequested() {
		return flushFile.exists();
	}
}
