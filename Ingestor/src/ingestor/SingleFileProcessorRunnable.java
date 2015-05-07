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
package ingestor;

import java.io.File;

import com.hdsfed.cometapi.CometProperties;
import com.hdsfed.cometapi.ExtendedLogger;
import com.hdsfed.cometapi.FileTracker;
import com.hdsfed.cometapi.ThreadTrackerDB;

//TODO: catch more exceptions individually
public class SingleFileProcessorRunnable implements Runnable {
	private static ExtendedLogger ScreenLog = new ExtendedLogger(SingleFileProcessorRunnable.class.getName());
	private SingleFileProcessor mFileProcessor;
	private File oneFile;
	private int id;
//	private int hcpFailDelay=0;
	private boolean success;
	
	public void initialize(File oneFile, int identifier, String workingPath, String pathPrefix) {
		this.oneFile=oneFile;
		this.id=identifier;
		try {
			this.mFileProcessor=new SingleFileProcessor(workingPath,pathPrefix);
			this.mFileProcessor.initialize();
			this.mFileProcessor.setThreadID(identifier);
//			this.hcpFailDelay=CometProperties.getHCPFailDelay();
			ScreenLog.setDebug(CometProperties.getInstance().getDebug());
			ScreenLog.setSilence(!CometProperties.getInstance().getDebug() && !CometProperties.getInstance().getVerbose());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			ScreenLog.ExceptionOutputHandler(e);
		}
	}

	public void run() {
		run(0);
	}
	
	
	
	public void run(int count) {
		
		ScreenLog.begin("SingleFileProcessorRunnable::Run() (thread id="+id+"); filename="+oneFile);
		ThreadTrackerDB.updateDBOverHTTP(oneFile.getAbsolutePath(),id, oneFile.length());
		setSuccess(false);
		if(CometProperties.WasTerminated()) return;
		try {
			if(CometProperties.getInstance().getSkipProcessing() || mFileProcessor.processFile(oneFile)) {
				ScreenLog.out("\t(t="+id+"):"+oneFile+": file successfully uploaded");
				
				ThreadTrackerDB.updateDBOverHTTP("verifying",id,0);
				
				if(CometProperties.getInstance().getSkipVerify() || mFileProcessor.verifyFile(oneFile)) {
					ScreenLog.out("\t(t="+id+"):"+oneFile+": file verified");
				} else {
					ScreenLog.out("\t(t="+id+"):"+oneFile+": file not verified, throwing exception");
					throw new Exception();
				}
			} else {
				ScreenLog.warning("\t(t="+id+"):"+oneFile+": was not written to HCP, but this is probably intentional");
				ThreadTrackerDB.updateDBOverHTTP(oneFile.getAbsolutePath()+" notwritten",id,0);
				ScreenLog.severe("\t(t="+id+") halting thread for "+CometProperties.getHCPFailDelay()+" minutes.");
				try { Thread.sleep(CometProperties.getHCPFailDelay()*300); } catch (Exception ignore) { } 
			}
			setSuccess(true);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			ScreenLog.ExceptionOutputHandler(e);
		} finally {	

			if(!success) {
				ThreadTrackerDB.updateDBOverHTTP("STALLED",id,0);
				ScreenLog.severe("\t(t="+id+") halting thread for "+CometProperties.getHCPFailDelay()+" minutes.");
			
				//lock object
				FileTracker.remove(oneFile.toString());
				//unlock object
				try { Thread.sleep(CometProperties.getHCPFailDelay()*60000); } catch (Exception ignore) { }
				if(count==CometProperties.MigrationRetryLimit()) {
					ScreenLog.severe("\t(t="+id+") unable to transfer file: "+oneFile.toString()+", will try again later");
				} else {
					ScreenLog.severe("\t(t="+id+") halted thread recovered, trying again now.");
					ScreenLog.severe("\t(t="+id+") for file: "+oneFile.toString()+"; retry count is "+count);
					
					run(count+1);
				}
			} else {	
				ScreenLog.out("\t(t="+id+") file ("+oneFile+") successfully transfered.");
			}
		} 
		ThreadTrackerDB.updateDBOverHTTP("idle",id,0);
		ScreenLog.end("SingleFileProcessorRunnable::Run() (thread id="+id+"); filename="+oneFile);
	}
	
	public String getHCPName() {
		return this.mFileProcessor.getHCPName();
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}
}
