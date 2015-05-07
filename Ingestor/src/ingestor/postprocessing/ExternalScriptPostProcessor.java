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


import ingestor.PauseStopProcessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import com.hdsfed.cometapi.CometProperties;
import com.hdsfed.cometapi.ExtendedLogger;

//TODO: consider moving exceptions out
public class ExternalScriptPostProcessor extends BasePostProcessor {
	private static final String VERSION ="1.21.0";
	private static final String COMPILE_DATE ="2015-05-06";
	private static final String SVN ="551+";

	private static ExtendedLogger ScreenLog = new ExtendedLogger(ExternalScriptPostProcessor.class.getName());

	
	// Local member variables.
	private Boolean bIsInitialized = false;
	private CometProperties mProps;
	
	// Processor classes.
	PauseStopProcessor mPauseStopProcessor;
	private File externalScript=null;
	
	// file system walker

	
	@Override
	protected void initialize(String inExternalScript) {
		ScreenLog.setClass(ExternalScriptPostProcessor.class.getName());
		if (! bIsInitialized)  // Only initialize if we haven't already
		{
			// Setup properties member.
			mProps = CometProperties.getInstance();
			ScreenLog.begin("initialize post processor");
			mPauseStopProcessor = new PauseStopProcessor(mProps);
			externalScript=new File(inExternalScript);
			bIsInitialized = true;
			ScreenLog.end("initialize post processor");
		}
	}


	@Override
	protected void runPostProcess() throws InterruptedException, IOException {
		ScreenLog.setClass(ExternalScriptPostProcessor.class.getName());
		ScreenLog.begin("runPostProcess - "+ExternalScriptPostProcessor.class.getName());
		ScreenLog.info("\tVersion: "+VERSION+" r"+SVN);
		ScreenLog.info("\tCompiled on: "+COMPILE_DATE);
		ScreenLog.info("\tusing library version:"+CometProperties.getVersion()+" r"+CometProperties.getSvn()+" compiled on "+CometProperties.getCompileDate());

		Runtime run = null;
		Process pr = null;
		BufferedReader buf = null;
		String line=null;

		if(!externalScript.exists()) {
				ScreenLog.severe("External script does not exist: " + externalScript);
		} else {
			//TODO: wrap popen-like activity into another function
			ScreenLog.severe("Executing external script: " + externalScript);
			
			//execute the script
			run=Runtime.getRuntime() ;
			pr = run.exec(externalScript.toString());
			pr.waitFor() ;
			buf = new BufferedReader( new InputStreamReader( pr.getInputStream() ) ) ;
					
		    while ( (line = buf.readLine()) != null) ScreenLog.out(line);
		            
	           ScreenLog.out("script completed with exit code = "+pr.exitValue());
	           //free memory
			buf=null;
			line=null;
			pr=null;
			run=null;
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
