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

package ingestor.metadata;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import ingestor.SingleFileProcessor;

import com.hdsfed.cometapi.*;

//TODO: merge loggers together
// consider moving exceptions out
public class CustomMetadataExtractor {

	private static Logger logger = Logger.getLogger(CustomMetadataExtractor.class.getPackage().getName());
//	private static ExtendedLogger ScreenLog = ExtendedLogger.getExtendedLogger(CustomMetadataExtractor.class.getPackage().getName());
	
	private boolean isInitialized = false;
	private LinkedList<String> mMetadataGeneratorClasses;
	private LinkedList<BaseMetadataGenerator> mMetadataGeneratorInstances;
	private boolean skipThisOne = false;

	private Map<String, String> annotations=null;
	private SingleFileProcessor mFileProc;
	
	public boolean ShouldSkipFile() {
		return skipThisOne;
	}
	public boolean ShouldIngestFile() {
		return !skipThisOne;
	}
	
	public CustomMetadataExtractor(SingleFileProcessor parent) throws IOException {
		mMetadataGeneratorClasses = CometProperties.getInstance().getMetadataClasses();
		mFileProc=parent;
	}

	private void initialize() {
		if (! isInitialized) {
			if (null == mMetadataGeneratorInstances) {
				mMetadataGeneratorInstances = new LinkedList<BaseMetadataGenerator>();
			} else {
				mMetadataGeneratorInstances.clear();  // Shouldn't be anything in it, but just in case.
			}
			//  Loop through all the class names, instantiate an instance, and call the initialization method on them.
			for (int i = 0; i < mMetadataGeneratorClasses.size(); i++) {
				String currentClass_and_annotations = mMetadataGeneratorClasses.get(i);
				String currentClass="";
				String annotation="";
				String everything_else="";
				String cmdline=null;
				
				if(!currentClass_and_annotations.contains("(")) {
					currentClass=currentClass_and_annotations;
					annotation=CometProperties.getDefaultAnnotation();
				} else {
					currentClass=currentClass_and_annotations.split("\\(")[0];
					everything_else=currentClass_and_annotations.split("\\(")[1];
					everything_else=everything_else.substring(0,everything_else.length()-1);
					
					if(!everything_else.contains(";")) annotation=everything_else;
					else {
							annotation=everything_else.split(";")[0];
							cmdline=everything_else.split(";")[1];
					}
				}
				
				try {
					logger.fine("Initializing metadata generator class: " + currentClass);
					logger.fine("-> with annotation "+ annotation);
					
					@SuppressWarnings("unchecked")
					Class<BaseMetadataGenerator> theClass = (Class<BaseMetadataGenerator>) Class.forName(currentClass);
					BaseMetadataGenerator thisInstance = (BaseMetadataGenerator)theClass.newInstance();
					
					thisInstance.initialize(null);
					thisInstance.setParent(mFileProc);
					
					thisInstance.setAnnotation(annotation);
					
					if(null!=cmdline) thisInstance.setCommandLine(cmdline);
					
					
					// Now that we successfully initialized the instance, add it to the list.
					mMetadataGeneratorInstances.add(thisInstance);
					
				} 
				catch (ClassNotFoundException x) {
					logger.warning("Class not found: \"" + currentClass + "\". Skipping...");
				} catch (IllegalAccessException x) {
					logger.warning("Illegal Access: \"" + currentClass + "\". No-arg constructor not found. Skipping...");
				} catch (InstantiationException x) {
					logger.warning("Instantiation Failure: \"" + currentClass + "\". Make sure it is a concrete class. Skipping...");
				} catch (ClassCastException x) {
					logger.warning("Class Cast Failure: \"" + currentClass + "\". Class does no inherit class \"" + BaseMetadataGenerator.class.getName() + "\". Skipping...");
				}
			}
			
			isInitialized = true;
		}
	}
	
	//TODO: move into AnnotationGenerator?
	public Map<String,String> generateAnnotations(File inFile) {
		String cm = "";
		String fullFilePath = "<unknown>";
		try {
			fullFilePath = inFile.getCanonicalPath();
		} catch (IOException e) {
			logger.warning("Failed to get Canonical Path of input file. Logging info will be limited");
		}
		
		logger.info("Requesting Metadata Generation for " + fullFilePath);
		
		// Call initialize just in case this is the first time being called.
		initialize();
		
		//init annotations
		setAnnotations(new TreeMap<String, String>());
		/**
		 * Now we are going to call each configured class in order until one of them 
		 *   returns back some metadata or the end of the list has been reached.
		 */
		for (int i = 0; i < mMetadataGeneratorInstances.size(); i++) {
			BaseMetadataGenerator currentInstance = mMetadataGeneratorInstances.get(i);
			
			String currentClass = currentInstance.getClass().getName();
			
			logger.fine("Calling class for metadata: " + currentClass);
			ThreadTrackerDB.updateDBOverHTTP(inFile.getAbsolutePath()+"-processing-metadata-"+currentClass,mFileProc.getThreadID(), inFile.length());

			cm=currentInstance.getMetadata(inFile);
			
			if(null==cm) cm="";
			
			if(!cm.equals("")) {
					annotations.put(currentInstance.getAnnotation(), cm);
					//stop with first good annotation, as-in classic Ingestor
					if(CometProperties.getInstance().getStopOnHit()) break;
			}
		}
		//return completed annotations
		return annotations;
	}
	public Map<String, String> getAnnotations() {
		return annotations;
	}
	public void setAnnotations(Map<String, String> annotations) {
		this.annotations = annotations;
	}
}



