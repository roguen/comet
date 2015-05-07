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

//TODO: need descriptions and parameters for new functions consistent with Cliff's work

package ingestor;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import com.hdsfed.cometapi.CometProperties;
import com.hdsfed.cometapi.ExtendedLogger;
import com.hdsfed.cometapi.FileTracker;
import com.hdsfed.cometapi.StringHelper;
import com.hdsfed.cometapi.ThreadTrackerDB;

//TODO: This class was written for Java v1.6; updated for 1.7; however, there is a file system walker built into 1.7 that should be 
//		more efficient than this one

public class FileSystemWalker {
	//TODO: merge the loggers together
	//      leaving them separate to minimize change; address it later
	//private static Logger logger = Logger.getLogger(FileSystemWalker.class.getPackage().getName());
	private static ExtendedLogger ScreenLog = new ExtendedLogger(FileSystemWalker.class.getName());

	//FIXME: statisticsCollector is broken
	//private StatisticsCollector mStatisticsCollector;
	private CometProperties mProps;
	private ThreadManager mThreadman;
	private PauseStopProcessor mPauseStopProcessor;
	private String mSourcePath="";
	//void reset() { mStatisticsCollector.reset(); };

	int counter=0;
	private File file_must_exist;
	private String directory_must_contain;
	private boolean useFile_must_exist;
	private boolean useDirectory_must_contain;
	private Boolean alwaysUsePrefix;
	private Boolean useSortPreferences=false;
	private LinkedList<String> sortPreferences=null;
//	private int percentComplete=0;

	//May not be necessary to copy CometProperties in here
	public FileSystemWalker(ThreadManager inThreadman, PauseStopProcessor inPauseStopProcessor, CometProperties inProps, String inSourcePath) {
		mThreadman = inThreadman;
		mPauseStopProcessor = inPauseStopProcessor;
		//mStatisticsCollector = inStatsCollector;
		mProps = inProps;
		mSourcePath = inSourcePath;
		
	}

	public String setPathPrefix(String pathPrefix) {
		if(!pathPrefix.startsWith("/") && !pathPrefix.equals("")) {
				return "/"+pathPrefix;
		}
		if(pathPrefix.equals("")) {
			return "";
		}
		int len=pathPrefix.length();
		return pathPrefix.substring(pathPrefix.lastIndexOf("/"),len);
	}
	
	/**
	 * This routine will walk the directory structure specified (or defaulted) and
	 * call the file processor for which this object was initialized with.
	 * 
	 * NOTE: This routine is called recursively for sub-directories.
	 * 
	 * @param inDirectory - (Optional) File of a directory to process.
	 * @return true/false - Whether the file passed in was a directory 
	 *                      and was processed without asking to stop requested.
	 * @throws Exception 
	 */
	public boolean doWalk(File inDirectory, String workingPath, String pathPrefix, String exclude) throws Exception {
		LinkedList<File> regularFiles = new LinkedList<File>();
		LinkedList<File> directoryFiles = new LinkedList<File>();

		ScreenLog.begin("doWalk:::: alwaysUsePrefix state is "+alwaysUsePrefix.toString());
		if(CometProperties.WasTerminated()) {
			ScreenLog.end("doWalk::: prematurely exiting due to termination");
			return false;
		}
 		// Parameter existence check.
		if (null == inDirectory) {
			if(mSourcePath.contains(",")) {
				String[] parts = mSourcePath.split(",");
				for(int i=0; i<parts.length; i++) {
					if(alwaysUsePrefix) { //collision safe
						if(!doWalk(new File(parts[i]),parts[i],setPathPrefix(parts[i]),exclude)) return false;
					} else {
						if(!doWalk(new File(parts[i]),parts[i],setPathPrefix(""),exclude)) return false;
					} 
				}
				ScreenLog.end("doWalk:::: alwaysUsePrefix state is "+alwaysUsePrefix.toString());
				return true;
			
			} else {
				ScreenLog.end("doWalk:::: alwaysUsePrefix state is "+alwaysUsePrefix.toString());
				if(alwaysUsePrefix) { //collision safe
					return doWalk(new File(mSourcePath),mSourcePath,setPathPrefix(mSourcePath),exclude);
				} else {
					return doWalk(new File(mSourcePath),mSourcePath,setPathPrefix(""),exclude);
				} 
			}
		}
		
		ScreenLog.begin("doWalk("+inDirectory.toString()+","+workingPath+","+pathPrefix+")");
		
		// Make sure directory provided actually exists.
		if ( ! inDirectory.exists()) {
			ScreenLog.warning("Directory does not exist: \"" + inDirectory.getAbsolutePath() + "\"");
			return false;
		}
		
		if ( ! inDirectory.isDirectory() ) {
			ScreenLog.warning("File specified is not a directory: \"" + inDirectory.getAbsolutePath() + "\"");
			return false;
		}

		if( inDirectory.toString().contains("lost+found")) {
			ScreenLog.warning("Directory specified is blacklisted: \"" + inDirectory.toString() + "\"");
			return false;
		}
		
		if( !exclude.equals("") &&  inDirectory.toString().endsWith(exclude)) {
			ScreenLog.warning("Directory specified is marked for delay: \"" + inDirectory.toString() + "\" because it ends with \""+exclude+"\"");
			return false;
		}
		
		// Setup local dir statistics collector.
		// broken
		//	StatisticsCollector myStats = new StatisticsCollector();
		
		// Collect an array of children files of this directory.
		File[] children = inDirectory.listFiles();
		ScreenLog.out("Processing directory: " + inDirectory.getAbsolutePath());
		
		if(CometProperties.WasTerminated()) {
			ScreenLog.end("doWalk::: prematurely exiting due to termination");
			return false;
		}

		
		if(children!=null) {
			Arrays.sort(children);  // Sort just in case it helps process files in some kind of order
			if(useSortPreferences) {
				ScreenLog.out("about to crash!!!");
				LinkedList<File> temp=StringHelper.SortbyPreference(new LinkedList<File>(Arrays.asList(children)), sortPreferences);
				children=temp.toArray(new File[temp.size()]);
			}
		}
		                        // NOTE: Haven't proven this does anything useful.
		
		ScreenLog.fine("Processing directory: " + inDirectory.getAbsolutePath());
		ScreenLog.fine("Number of Children: " + (null == children ? 0 : children.length));

		// Only process the current directory if it returned a list of files contained in the
		//   directory.
		if (null != children) {
			// Loop through all the children and separate out into regular files and
			//    directories.  We will be processing them separately.
			for (int i=0 ; i < children.length ; i++) {
				// Store the file depending on if it is a regular file or a directory.
				if (children[i].isFile()) {
					if(!FileTracker.contains(children[i].toString())) regularFiles.add(children[i]);
					//Threadman replaces the direct call to SingleFileProcessor
					//TODO: investigate if we still need this
					//SingleFileProcessor.writeStringToFileList(children[i].getAbsolutePath());
				} else if (children[i].isDirectory() && OkToIngest(children[i])) {
					directoryFiles.add(children[i]);
				} 
				// Remove reference to help with memory management
				children[i] = null;
			}
			
			children = null;
		}
		
		// Report on what we found.
		ScreenLog.fine("Number of (new) regular files found:  " + regularFiles.size());
		ScreenLog.fine("Number of files in upload pool:  " + FileTracker.getSize() +" of "+CometProperties.getInstance().getMaxFiles());
		ScreenLog.fine("Number of subdirectories found: " + directoryFiles.size());

		/*
		 * Now starting processing of the files.  Will first process all the regular
		 *   file in the current directory, and later will start digging down into
		 *   each sub-directory.
		 */
		
		// First process the regular files in current directory.
		int num=0;
		int orig_size=regularFiles.size();
		while ( ! regularFiles.isEmpty() ) {
			File oneFile = regularFiles.removeFirst();
			num++;
			// If we setup a file processor, then call it.
			if (null != mThreadman) {
				if(!FileTracker.contains(oneFile.toString())) {
					FileTracker.add(oneFile.toString());
					if(counter<CometProperties.getInstance().getMaxFiles()) {
						counter++;
					} else {
						FileTracker.removeFirst();
					}
					if(!CometProperties.WasTerminated()) {
						ScreenLog.out("====> setting percentage to  "+num+" / " + orig_size +" = " + (num/orig_size));
						CometProperties.setPercentComplete((num*100)/orig_size);
						CometProperties.setTimeInIteration(System.currentTimeMillis() - CometProperties.getTimeInIterationTimeStamp());
						CometProperties.setUploadPoolSize(FileTracker.getSize());

						ThreadTrackerDB.heartBeatOverHTTP(inDirectory.getAbsolutePath(),"scanning");
						mThreadman.processFile(oneFile,workingPath,pathPrefix);
					}
				} else {
					ScreenLog.fine("File: "+oneFile.toString()+" already processed, skipping");
				}
				
				//TODO: reinstate after we fix statistics
				//myStats.update(mFileProcessor.getObjectStatus(), mFileProcessor.getCustomMetadataStatus());
			}
			
			// Handle Pauses or make sure we should continue processing.
			if ( ! mPauseStopProcessor.checkContinue() ) {
				// Should not continue;
				return false;
			}
		}
		FileTracker.flush(CometProperties.getInstance().getFileProcessedCache());
		// Now time to process the sub-directories
		while ( ! directoryFiles.isEmpty() ) {
			File oneDirectory = directoryFiles.removeFirst();
			
			//special case, ignore svn directory
			if(oneDirectory.toString().contains(".svn") || OnSkipList(oneDirectory)) {
				ScreenLog.out("caught and skipping directory :::"+oneDirectory.toString());
				continue;
			} 
			if(!doWalk(oneDirectory,workingPath,pathPrefix, exclude)) {
				ScreenLog.severe("doWalk("+oneDirectory+") returned false, halting ingestion process");
				
				return false;
			}
			
			// Handle Pauses or make sure we should continue processing.
			if ( ! mPauseStopProcessor.checkContinue() ) {
				// Should not continue;
				return false;
			}
			
			// Delete this directory if configured to do so.
			if (mProps.shouldDeleteSourceEmptyDirs()) {
				if ( ! oneDirectory.delete() ) {
					ScreenLog.warning("Failed to delete source directory with current permissions: "+ oneDirectory.getAbsolutePath());
					if (mProps.shouldForceDeleteSourceFiles()) {
						if ( oneDirectory.setWritable(true, true) && ! oneDirectory.delete() ) {
							ScreenLog.warning("Failed to delete source directory after attempt to make writable");
						}
					}
				}
			}
		}
		// Log the statistics regular files for this directory.  Does not include sub-directory information.
		//TODO: reinstate after we fix statistics
		//myStats.logStats("Directory Stats (" + inDirectory.getAbsolutePath() + ")");
		ScreenLog.end("doWalk("+inDirectory.toString()+","+workingPath+","+pathPrefix+","+exclude+") :::: alwaysUsePrefix state is "+alwaysUsePrefix.toString());
		CometProperties.setPercentComplete(100);
		CometProperties.setTimeInIteration(System.currentTimeMillis() - CometProperties.getTimeInIterationTimeStamp());
		CometProperties.setUploadPoolSize(FileTracker.getSize());

		ThreadTrackerDB.heartBeatOverHTTP(inDirectory.getAbsolutePath(),"scanning");

		return !CometProperties.WasTerminated();
	}

	
	private boolean OnSkipList(File oneDirectory) {
		ArrayList<String> skipdirs=mProps.getSkipAllButDirList();
		//if the list is empty, just return false
		if(skipdirs.size()==0) return false;
		for(int i=0; i<skipdirs.size(); i++) {
			if(oneDirectory.toString().contains(skipdirs.get(i))) return false;
		}
		//if not in list, return true
		return true;
	}

	public void setSourcePath(String sourcePath) {
		this.mSourcePath = sourcePath;
	}

	public String getSourcePath() {
		return mSourcePath;
	}

	public void ingestIfFileExists(File inIngestIfFileExists) {
		// TODO Auto-generated method stub
		file_must_exist=inIngestIfFileExists;
		useFile_must_exist=true;
	}

	public void ingestIfDirContains(String inIngestIfDirContains) {
		// TODO Auto-generated method stub
		directory_must_contain = inIngestIfDirContains;
		useDirectory_must_contain=true;
	}
	
	public Boolean OkToIngest(File thisDirectory) throws Exception {
		//use cases::
		File checkfor=null;
		
		//if not must contain and not file_must exist, do it
		// eg Archiver1...n
		//unrestricted case
		if(!useDirectory_must_contain && !useFile_must_exist) return true;

		//if must contain and directory doesn't contain, do it
		// eg ActivityControlMaestro (top)
		if(useDirectory_must_contain && !thisDirectory.toString().endsWith(directory_must_contain)) return true;
		
		
		//if(useDirectory_must_contain && thisDirectory.endsWith(directory_must_contain)) {
		
		if(!useFile_must_exist) throw new Exception();
			
		checkfor=new File(thisDirectory+"/"+file_must_exist);
			
			
		//if must_contain and directory contains and file_must_exist and useFile_must_exist, then do it
		// eg restricted .act directory and ActivityDone present

		//if must contain and directory contains and file_must exist, but doesn't, then don't do it
		// eg restricted .act directory and ActivityDone not present yet
		return checkfor.exists();
	}

	public boolean isAlwaysUsePrefix() {
		return alwaysUsePrefix;
	}

	public void setAlwaysUsePrefix(boolean alwaysUsePrefix) {
		this.alwaysUsePrefix = alwaysUsePrefix;
	}

	public Boolean getUseSortPreferences() {
		return useSortPreferences;
	}

	public void setUseSortPreferences(Boolean useSortPreferences) {
		this.useSortPreferences = useSortPreferences;
	}

	public LinkedList<String> getSortPreferences() {
		return sortPreferences;
	}

	public void setSortPreferences(LinkedList<String> sortPreferences) {
		this.sortPreferences = sortPreferences;
	}
}
