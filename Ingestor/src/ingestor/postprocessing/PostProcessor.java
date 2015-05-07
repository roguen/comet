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

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

import com.hdsfed.cometapi.CometProperties;
import com.hdsfed.cometapi.ExtendedLogger;
import com.hdsfed.cometapi.ThreadTrackerDB;

//TODO: merge loggers together
// consider moving exceptions out
//PostProcessor handles all the post processing needs by calling each class in turn
public class PostProcessor {
	private static ExtendedLogger ScreenLog = new ExtendedLogger(PostProcessor.class.getName());
	private LinkedList<String> mPostProcessorClasses=null;
	private LinkedList<BasePostProcessor> mPostProcessorInstances=null;
	private LinkedList<String> arguments=null;
	
	
	public PostProcessor() throws IOException {
		mPostProcessorClasses=CometProperties.getInstance().getPostProcessorClasses();
		mPostProcessorInstances=new LinkedList<BasePostProcessor>();
	}
	
	public void runPostProcesses() throws InterruptedException, IOException, Exception {
		CometProperties.setPercentComplete(100);
		CometProperties.setTimeInIteration(System.currentTimeMillis() - CometProperties.getTimeInIterationTimeStamp());
		ThreadTrackerDB.heartBeatOverHTTP("none", "post");

		for (int i = 0; i < mPostProcessorInstances.size(); i++) {
			ScreenLog.begin("===>about to run post process "+(i+1)+" of "+ mPostProcessorInstances.size());
			mPostProcessorInstances.get(i).runPostProcess();
			ScreenLog.end("===>about to run post process "+(i+1)+" of "+ mPostProcessorInstances.size());
		}
		CometProperties.setPercentComplete(100);
		CometProperties.setTimeInIteration(System.currentTimeMillis() - CometProperties.getTimeInIterationTimeStamp());
		ThreadTrackerDB.heartBeatOverHTTP("none", "post-complete");
	}

	//TODO: move into AnnotationGenerator?
	public void initialize() {
		ScreenLog.begin("PostProcess::Initialize()");
		
		if(mPostProcessorClasses==null) {
			ScreenLog.end("PostProcess::Initialize()");
			return;
		}
		
		arguments=null;
		String currentClass="";
		String everything_else="";
		try {
			ScreenLog.out("\tstart of loop for size="+mPostProcessorClasses.size());
			for (int i = 0; i < mPostProcessorClasses.size(); i++) {
				ScreenLog.begin("\t\tloop for "+(i+1)+"/"+mPostProcessorClasses.size());
				currentClass="";
				everything_else="";
				arguments=new LinkedList<String>();
				
				String currentClass_and_args = mPostProcessorClasses.get(i);
				ScreenLog.out("\tabout to init "+mPostProcessorClasses.get(i));
				
				if(!currentClass_and_args.contains("(")) {
					currentClass=currentClass_and_args;
					
				} else {
					currentClass=currentClass_and_args.split("\\(")[0];
					everything_else=currentClass_and_args.split("\\(")[1];
					everything_else=everything_else.substring(0,everything_else.length()-1);
					ScreenLog.out("everything_else="+everything_else);
					
					if(!everything_else.contains(";")) arguments.add(everything_else);
					else {
						arguments=new LinkedList<String>(Arrays.asList(everything_else.split(";")));
						
					}
				}
				
				ScreenLog.out("Initializing Post Processing class: " + currentClass);
				ScreenLog.out("arg:",arguments);
					
				@SuppressWarnings("unchecked")
				Class<BasePostProcessor> theClass = (Class<BasePostProcessor>) Class.forName(currentClass);
				BasePostProcessor thisInstance = (BasePostProcessor)theClass.newInstance();
					
				ScreenLog.begin("\tabout to init class:::"+currentClass);
				
				if(arguments.size()>0) thisInstance.initialize(arguments.get(0));
				else thisInstance.initialize(null);
					
				//thisInstance.runPostProcess();

				// Now that we successfully initialized the instance, add it to the list.
				mPostProcessorInstances.add(thisInstance);
		
				ScreenLog.out("\t\tpostproc instances size = "+mPostProcessorInstances.size());
				ScreenLog.end("\t\tpostproc loop for "+(i+1)+"/"+mPostProcessorClasses.size());
			} 
		}
		catch (ClassNotFoundException x) {
			ScreenLog.warning("====Class not found: \"" + currentClass + "\". Skipping...");
		} catch (IllegalAccessException x) {
			ScreenLog.warning("====Illegal Access: \"" + currentClass + "\". No-arg constructor not found. Skipping...");
		} catch (InstantiationException x) {
			ScreenLog.warning("====Instantiation Failure: \"" + currentClass + "\". Make sure it is a concrete class. Skipping...");
		} catch (ClassCastException x) {
			ScreenLog.warning("====Class Cast Failure: \"" + currentClass + "\". Class does no inherit class \"" + BasePostProcessor.class.getName() + "\". Skipping...");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ScreenLog.end("PostProcess::Initialize()");
	}	
}			
