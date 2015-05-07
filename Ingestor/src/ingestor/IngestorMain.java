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

//NOTE: this code was originally developed by Cliff Grimm <clifford.grimm@hds.com>

package ingestor;

import ingestor.postprocessing.PostProcessor;
import ingestor.walkers.Walker;

import com.hdsfed.cometapi.CometProperties;
import com.hdsfed.cometapi.ExtendedLogger;
import com.hdsfed.cometapi.FileTracker;
import com.hdsfed.cometapi.StringHelper;
import com.hdsfed.cometapi.ThreadTrackerDB;

//TODO: make filesystemwatcher work correctly and integrate
//		document each function and variable to be more consistent with Cliff's work
//		fix statistics to work correctly with threading

public class IngestorMain {
	private static final String VERSION ="1.21.0";
	private static final String COMPILE_DATE ="2015-05-06";
	private static final String SVN ="554";

	//TODO: eventually, merge these two together.
	//private static Logger logger = Logger.getLogger(IngestorMain.class.getPackage().getName());
	private static ExtendedLogger ScreenLog = new ExtendedLogger(IngestorMain.class.getName());
	private static ExtendedLogger logger = new ExtendedLogger(IngestorMain.class.getName());


	// Local member variables.
	private Boolean bIsInitialized = false;
	private CometProperties mProps;
	
	// Processor classes.
	PauseStopProcessor mPauseStopProcessor;
	
	//Collection of iterators to ingest data
	Walker mWalkers;
	
	//post processing
	PostProcessor mPostProc;
	
	// Statistics Holder. -- does not work
	//private StatisticsCollector mTotalStatistics = new StatisticsCollector();

	/**
	 * Initialize the object by setting up internal data and establishing the HTTP client connection.
	 * 
	 * This routine is called by the ReadFromHCP and WriteToHCP routines, so calling it by the
	 * consumer of this class is unnecessary.
	 *
	 */
	private void initialize() throws Exception {

		if (! bIsInitialized)  // Only initialize if we haven't already
		{
			// Setup properties member.
			mProps = CometProperties.getInstance();
			ScreenLog.begin("initialize - main");
			
			StringHelper.DateStamp("/opt/COMETDist/ingest-started");

			mPauseStopProcessor = new PauseStopProcessor(mProps);
	
			mWalkers = new Walker();
			ScreenLog.begin("about to init walkers");
			mWalkers.initialize();
			ScreenLog.end("about to init walkers");

			FileTracker.init(CometProperties.getInstance().useFileProcessedCache(),CometProperties.getInstance().getFileProcessedCache(),CometProperties.getInstance().getMaxFiles());

			mPostProc = new PostProcessor();
			mPostProc.initialize();

			bIsInitialized = true;

			//TODO: make this configurable
			int count=300;
			while(!ThreadTrackerDB.isReadyHttp() && count!=0) {
				ScreenLog.out("apache is not ready, sleep for a bit (up to 5 minutes)");
				Thread.sleep(1000);
				count--;
			}
			
			//if tomcat isn't available, turn off heart beat functionality entirely
			if(!ThreadTrackerDB.isReadyHttp()) {
				//disable http tracking
				CometProperties.getInstance().setIngestorHeartbeatEnabled(false);
			}
			ScreenLog.end("initialize - main");
		}
	}

	private void runIngestor() throws Exception {
		ScreenLog.setClass(IngestorMain.class.getName());
		ScreenLog.begin("runIngestor()");
		// Setup object structures and information.
		initialize();

		int loopCount = mProps.getLoopCount();  // Note loop count will stay the same throughout execution
		int iterationCounter=0;

		logger.force("Beginning " + this.getClass().getName() + " execution");
		
		if (CometProperties.LOOPCOUNT_INFINITE == loopCount) {
			logger.info("Running in Infinite Loop mode");
		}
		
		/**
		 *  Loop for the number of iterations specified.
		 */
		while ((CometProperties.LOOPCOUNT_INFINITE == loopCount
				|| loopCount > 0) && !CometProperties.WasTerminated() ) {
			ScreenLog.setClass(IngestorMain.class.getName());

			iterationCounter++;
//			ThreadTrackerDB.newDBOverHTTP();
			CometProperties.setPercentComplete(0);
			CometProperties.setIteration(iterationCounter);
			CometProperties.setTimeInIteration(0);
			CometProperties.setTimeInIterationTimeStamp(System.currentTimeMillis());
			CometProperties.setUploadPoolSize(FileTracker.getSize());
			ThreadTrackerDB.heartBeatOverHTTP("none", "init");
			// Process pausing or stopping.
			if ( ! mPauseStopProcessor.checkContinue() ) {
				logger.warning("User requested stop. Terminating execution.");
				ScreenLog.out("check continue failed, bail out");
				break; // Outta here.
			}
			
			if(!CometProperties.getInstance().getBooleanProperty("debug.skipMain",false))  mWalkers.doWalkers();
			
			//mTotalStatistics.logStats("Mid-Run ("+ (mProps.isInfiniteLoopCount() ? "Infinite" : loopCount) + ")");
			
			FileTracker.flush(CometProperties.getInstance().getFileProcessedCache());

			// Decrement the loopCount if it is not infinite loop mode.
			if ( ! mProps.isInfiniteLoopCount() ) {
				ScreenLog.out("decrement loopCount was:"+loopCount);
				ScreenLog.out("isInfiniteLoopCount() returns "+mProps.isInfiniteLoopCount());
				loopCount--;
				ScreenLog.out("loopCount="+loopCount);
			}

			if(!CometProperties.WasTerminated()) mPostProc.runPostProcesses();
			ScreenLog.setClass(IngestorMain.class.getName());
			ScreenLog.out("======reload configuration for new loop=====");
			mProps.reload(); // Get a fresh copy of the settings in case they have changed.
			
			FileTracker.sort();
			ScreenLog.end("==== bottom of the loop ====");
		}
		//mTotalStatistics.logStats("Final Completion");
		ScreenLog.out("issue complete to REST");
		ThreadTrackerDB.heartBeatCompleteOverHTTP();

		logger.info("Exiting Program");
		ScreenLog.end("==== end of runtime ====");
	}
	
	//move to PauseStopProcessor
	public void Terminate() {
		CometProperties.setTimeInIteration(System.currentTimeMillis() - CometProperties.getTimeInIterationTimeStamp());
		ThreadTrackerDB.heartBeatOverHTTP("none", "terminating");
		CometProperties.Terminate();
		mWalkers.waitForThreads();
		CometProperties.setTimeInIteration(System.currentTimeMillis() - CometProperties.getTimeInIterationTimeStamp());
		ThreadTrackerDB.heartBeatOverHTTP("none", "terminated");
	}
	
	public static void main(String[] args) {
		try {
			logger.force("Ingestor Version: "+VERSION+" r"+SVN);
			logger.force("\tCompiled on: "+COMPILE_DATE);
			logger.force("\tusing library version:"+CometProperties.getVersion()+" r"+CometProperties.getSvn()+" compiled on "+CometProperties.getCompileDate());

			
		
			if(args.length>0 && args[0].toLowerCase().contains("version")) System.exit(0);
			
			IngestorMain myself = new IngestorMain();
			myself.runIngestor();
			
			
			
			
		} catch (Exception e) {
			ScreenLog.ExceptionOutputHandler(e);
			e.printStackTrace();
			System.exit(-1);
		}
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
