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
package ingestor;

import java.io.File;
import java.io.IOException;

import com.hdsfed.cometapi.CometProperties;
import com.hdsfed.cometapi.ExtendedLogger;
import com.hdsfed.cometapi.ThreadJSON;

//TODO: need function descriptions and parameters
public class ThreadManager {
	private static ExtendedLogger ScreenLog = new ExtendedLogger(ThreadManager.class.getName());
	private int threadMax;
	private int nat=-1; //next available thread
	private Thread[] threadArray;
	private static ThreadJSON[] threadData;
	private SingleFileProcessorRunnable[] fprocArray;
		
	public ThreadManager() {
	}

	public void sleep(int millisec) {
		try { Thread.sleep(millisec); } catch (Exception ignored) { }
	}
	
	public void initialize(int threadMax) {
		ScreenLog.begin("initalizing thread manager with "+threadMax+" threads available");
		this.threadMax=threadMax;
		fprocArray=new SingleFileProcessorRunnable[this.threadMax];
		threadArray=new Thread[this.threadMax];
		threadData=new ThreadJSON[this.threadMax];
		for(int i=0; i<this.threadMax; i++) {
			threadData[i]=new ThreadJSON();
			threadData[i].Init(i);
		}
		ScreenLog.setDebug(CometProperties.getInstance().getDebug());
		ScreenLog.setSilence(!CometProperties.getInstance().getDebug() && !CometProperties.getInstance().getVerbose());
		ScreenLog.end("initalizing thread manager with "+threadMax+" threads available");
	}
	
	public void processFile(File inFile, String workingPath, String pathPrefix) throws IOException {
		ScreenLog.begin("processFile("+inFile+","+workingPath+","+pathPrefix+")");
		while(WaitingForNextAvailableThread()) {
			sleep(100);
		}

		ScreenLog.out("\tfound an available thread, ready to process the file "+inFile.toString());
		ScreenLog.out("\tthread # "+nat+" appears to be open");
		
		if(fprocArray[nat]!=null) fprocArray[nat]=null;
		fprocArray[nat]=new SingleFileProcessorRunnable();
		fprocArray[nat].initialize(inFile, nat,  workingPath,  pathPrefix);
		
		threadData[nat].path=inFile.getAbsolutePath();
		threadData[nat].size=inFile.length();
		threadData[nat].start_time=System.currentTimeMillis();
		threadData[nat].tgt=CometProperties.getInstance().getDestinationRootPath().getPath();
		
		threadArray[nat]=new Thread(fprocArray[nat]);
		if(!CometProperties.WasTerminated()) threadArray[nat].start();
//		threadDB.set(nat, inFile.getAbsolutePath());
		ScreenLog.end("processFile("+inFile+","+workingPath+","+pathPrefix+")");
	}

	private boolean WaitingForNextAvailableThread() {
		for(int i=0; i<threadMax; i++) {
			if(threadArray[i]==null) {
				nat=i;
				return false;
			}
			if(!threadArray[i].isAlive()) {
				nat=i; // next available thread
				return false;
			}
		}
		return true;
	}

	public void waitForThreads() {
		ScreenLog.begin("waitForThreads()");
		boolean cond_not_met=true;
		while(cond_not_met) {
			cond_not_met=false;
			for(int i=0; i<threadArray.length; i++) {
				if(threadArray[i]!=null && threadArray[i].isAlive()) cond_not_met=true;
			}
			sleep(100);
		}
		ScreenLog.end("waitForThreads()");
	}

	public int getThreadMax() {
		return threadMax;
	}

	public void setThreadMax(int threadMax) {
		this.threadMax = threadMax;
	}
}
