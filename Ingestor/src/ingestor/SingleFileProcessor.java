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

import ingestor.metadata.CustomMetadataExtractor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.entity.InputStreamEntity;

import com.hds.hcp.apihelpers.HCPUtils;
import com.hdsfed.cometapi.AnnotationHelper;
import com.hdsfed.cometapi.CometProperties;
import com.hdsfed.cometapi.ExtendedLogger;
import com.hdsfed.cometapi.HCPClient;
import com.hdsfed.cometapi.IngestorJSON;
import com.hdsfed.cometapi.StringHelper;
import com.hdsfed.cometapi.ThreadTrackerDB;

//TODO: need to reinforce thread safety in this class
//		since SFP is operating inside of a thread, we want the run() function to handle all exceptions
//		therefore SFP should throw but not catch exceptions
//		consider moving SFP into library
public class SingleFileProcessor {
	//TODO: merge loggers together
	private static ExtendedLogger logger = new ExtendedLogger(SingleFileProcessor.class.getName());
	private static ExtendedLogger ScreenLog = new ExtendedLogger(SingleFileProcessor.class.getName());
	//private static Logger logger = Logger.getLogger(SingleFileProcessor.class.getPackage().getName());
	private WriteStatus eObjectStatus;
	private WriteStatus eCustomMetadataStatus;
	private File mCurrentFile;
	
	// Local member variables.
	private Boolean bIsInitialized = false;
	//private Boolean bCanUseWholeIO = false;
	private HttpClient mHttpClient;
//	private String sHCPVersion;
	public String AuthToken;
	private CustomMetadataExtractor mCustomMetadataGenerator;
	
//	private static String UNKNOWN_HCP_VERSION = "<unknown>";
	//private CustomThumbnailGenerator mThumbnailGenerator;

	private Map<String,String> annotations=null;
	private String pathPrefix;
	private String workingPath;
	
	private int threadID=-1;
	
	/*
	 * Return values for CheckExistanceOnHCP method
	 */
	public enum ObjectState {
		OBJECT_DOES_NOT_EXIST,
		OBJECT_ONLY,
		OBJECT_AND_CUSTOM_METADATA
	};
	
	public enum WriteStatus {
		WRITE_NOT_ATTEMPTED,
		WRITE_SUCCESS,
		WRITE_FAILURE
	}
	
	SingleFileProcessor(String workingPath, String pathPrefix) {
		eObjectStatus = WriteStatus.WRITE_NOT_ATTEMPTED;
		eCustomMetadataStatus = WriteStatus.WRITE_NOT_ATTEMPTED;
		this.workingPath=workingPath;
		this.pathPrefix=pathPrefix;
		
		ScreenLog.setDebug(CometProperties.getInstance().getDebug());
		ScreenLog.setSilence(!CometProperties.getInstance().getDebug() && !CometProperties.getInstance().getVerbose());
	}

	WriteStatus getObjectStatus(){ return eObjectStatus; };
	WriteStatus getCustomMetadataStatus() { return eCustomMetadataStatus; };
	
	File getCurrentFile() { return mCurrentFile; };
	
	/**
	 * Initialize the object by setting up internal data and establishing the HTTP client connection.
	 * 
	 * This routine is called by the ReadFromHCP and WriteToHCP routines, so calling it by the
	 * consumer of this class is unnecessary.
	 *
	 */
	void initialize() throws Exception {

		if (! bIsInitialized)  // Only initialize if we haven't already
		{
			// Setup properties member.
			CometProperties mProps = CometProperties.getInstance();
			// Setup HTTP Client
			setmHttpClient(HCPUtils.initHttpClient());
			mCustomMetadataGenerator = new CustomMetadataExtractor(this);
			AuthToken=mProps.getAuthToken();
			
			//TODO: move this functionality into HCPClient
			// Determine if we can use Whole I/O by looking at the version number.
			//sHCPVersion = GetHCPVersion(mProps.getDestinationRootPath().toURI());
//			sHCPVersion = GetHCPVersion();

			//only support 6.0+
//			logger.info("Detected HCP Version: " + sHCPVersion);
//			try {
//				Float tmpValue = Float.valueOf(sHCPVersion.substring(0, 3));
//				if (6.0 > tmpValue) {
//					//bCanUseWholeIO = true;
//					
//					logger.info("This version of HCP is not supported");
//				}
//			} catch (NumberFormatException x) {
//				if (! sHCPVersion.equals(UNKNOWN_HCP_VERSION) ) {
//					throw x;
//				}
//			}
			
			bIsInitialized = true;
		}
	}

	 /**
	  * Internal function to look at the HCP machine and determine the passed object exists
	  *   on the system and whether it has custom-metadata or not.  It accomplishes this by
	  *   doing an HCP HTTP REST HEAD request to the object and looks at the metadata returned
	  *   about the object.
	  * 
	  * @param inDestinationPath URL to HCP file to retrieve state.
	  * @return ObjectState enumeration value as to the state on the HCP system.
	  * @throws ClientProtocolException
	  * @throws IOException
	  * @throws HttpResponseException
	  */
	//TODO: move this function into HCPClient
//	 private String GetHCPVersion_DONOTUSE() {
//		 String retVal = UNKNOWN_HCP_VERSION;
//		try {
//			logger.info("Getting HCP Version");
//			CometProperties mProps=CometProperties.getInstance();
//			HCPClient client=new HCPClient(mProps);
//			client.setRootpath(mProps.getDestinationRootPath(pathPrefix));
//			retVal=client.GetHCPVersion();
//		} catch (Exception x) {
//			logger.warning("Unable to determine HCP Version: " + x.getMessage());
//		}
//		if(retVal.length()<3) retVal=UNKNOWN_HCP_VERSION;
//		return retVal;
//	 }
	
	//TODO: need to replace this with CSV in comet.properties
	boolean blackListCheck(File inFile) {
		return (inFile.getName().equals(".DS_Store") ||
				inFile.getName().equals("thumbs.db") ||		
			   inFile.getName().endsWith(".default.xml") ||
			   inFile.getName().startsWith(".svn") ||
			   inFile.getName().startsWith(".") ||
			   inFile.getName().endsWith("tmp") ||
			   inFile.getName().startsWith("__MACOSX") ||
			   inFile.getName().endsWith(".inuse") ||
			   (inFile.getName().endsWith(".xml") && !CometProperties.getInstance().skipMetadata()));
		
	}		

	private boolean CombineMetadata(File inFile) {
		CometProperties mProps=CometProperties.getInstance();
		return (!mProps.shouldSkipMetadata(inFile) && mProps.shouldCombineAnnotations());
	}

	boolean processFile(File inFile) throws Exception {
		
		if(CometProperties.WasTerminated()) {
			logger.severe("COMET was externally terminated prematurely");
			return false;
		}
		
		
		if ( null == inFile) {
			logger.severe("Invalid input parameter.  inFile is null");
			return false;
		}
		
		if ( ! bIsInitialized ) {
			logger.severe("Programming Error. Object Not Initialized");
			return false;
		}
		
		mCurrentFile = inFile;
		Boolean skipmd=CometProperties.getInstance().shouldSkipMetadata(mCurrentFile);
		logger.fine("Processing File:" + mCurrentFile.getAbsolutePath());
		
		//TODO: still useful to track this?
		eObjectStatus = WriteStatus.WRITE_NOT_ATTEMPTED;
		eCustomMetadataStatus = WriteStatus.WRITE_NOT_ATTEMPTED;
		
		if ( ! mCurrentFile.exists()) {
			logger.warning("File does not exist: " + mCurrentFile.getAbsolutePath() + " (Skipping)");
			return false;
		}

		if(CometProperties.WasTerminated()) {
			logger.severe("COMET was externally terminated prematurely");
			return false;
		}

		
		//TODO: verify that shouldUnzip is not active when the source file system is r/o
		if(isArchive(mCurrentFile) && CometProperties.getInstance().shouldUnzip()) {
			ThreadTrackerDB.updateDBOverHTTP(mCurrentFile.getAbsolutePath()+"-unzipping",threadID, mCurrentFile.length());

		//need to improve this black list
			if(UnzipArchive(mCurrentFile)) {
				if(!mCurrentFile.delete()) {
					logger.warning("Unable to delete archive file: " + mCurrentFile.getAbsolutePath());
				}
			}
			logger.warning("Archive files are not to be ingested: " + mCurrentFile.getAbsolutePath() + " (Skipping)");
			return false;
		}
		
		if(CometProperties.WasTerminated()) {
			logger.severe("COMET was externally terminated prematurely");
			return false;
		}

		
		if(blackListCheck(mCurrentFile)) {
			logger.warning("File is on black list: " + mCurrentFile.getAbsolutePath() + " (Skipping)");
			return false;
		} else if(StringHelper.FileExists(mCurrentFile.getAbsolutePath()+".inuse")) {
			logger.warning("File is not black list, but is currently locked: "+mCurrentFile.getAbsolutePath()+" (skipping)");
			return false;
		} //else file is not on blacklist and free to move on

		ThreadTrackerDB.updateDBOverHTTP(mCurrentFile.getAbsolutePath(),threadID, mCurrentFile.length());
		boolean success=CometProperties.getSkipUpload() || WriteToHCP((new File(workingPath)).getAbsolutePath(), mCurrentFile, skipmd);
		
		
				
		//if writing to HCP fails (eg inappropriate for filetype to write to HCP), just quit
		if(!success) return false;
		
		// If the write to HCP succeeded, 
		//     remove the file we just processed off the local file system..
		//we're not quite done yet, we need to combine annotations into the new default annotation

		
		if(CometProperties.WasTerminated()) {
			logger.severe("COMET was externally terminated prematurely");
			return false;
		}

		if(CometProperties.getSkipUpload()) {
				skipmd=true;
				//delay execution for a number of seconds to ensure we see the file being fake-processed
				Delay();
				
				
		}
		
		
		
		
		//attempt to write metadata, if allowed to
		if(!skipmd && CombineMetadata(mCurrentFile)) {

			HCPClient client=new HCPClient(CometProperties.getInstance());
			URL encodedURL=AnnotationHelper.FSToURLPath(CometProperties.getInstance().getDestinationRootPath(pathPrefix), workingPath,new File(mCurrentFile.toString()));
			client.setRootpath(CometProperties.getInstance().getDestinationRootPath(pathPrefix));
			String captured=client.HttpPutHCPContent(AnnotationHelper.AnnotationMapToCombinedAnnotation(annotations), AnnotationHelper.PathAndAnnotationToURL(client.getRootpath(), encodedURL.toString(),CometProperties.getInstance().getCombinedAnnotation()));
			//neccesary to capture and show output here?
			ScreenLog.out("output from PUT operation: "+captured);
			success=true;
		} else {
			ScreenLog.out("\tCombineMetadata() returned false, not writing combined annotation for file "+mCurrentFile.toString());
		}
			
		//TODO: should verify that shouldDeleteSourceFiles() is inactive when read/only
		//TODO: wrap this delete process into another function
		if (success && CometProperties.getInstance().shouldDeleteSourceFiles()) {
			ThreadTrackerDB.updateDBOverHTTP(mCurrentFile.getAbsolutePath()+"-deleting",threadID, mCurrentFile.length());

			ScreenLog.out("\tattempting delete... of  "+mCurrentFile.getPath());
			if ( mCurrentFile.delete() ) {
				ScreenLog.out("\t\tdelete was successful!");
			} else {	
				ScreenLog.out("\t\tdelete was unsuccessful, try force delete");
				logger.warning("Failed to delete source file with current permissions: " + mCurrentFile.getAbsolutePath());
				
				if(CometProperties.getInstance().shouldForceDeleteSourceFiles()) {
					if (mCurrentFile.setWritable(true, true) && ! mCurrentFile.delete() ) {
						ScreenLog.out("\t\tforce delete was unsuccessful");
						logger.warning("Failed to delete source file after attempt to make writable");
					} else {
						ScreenLog.out("\t\tforce delete was successful");
					}
				} else {
					ScreenLog.out("\t\tforce delete not allowed");
				}
			}
			ScreenLog.out("\tafter delete attempt of "+mCurrentFile.getPath());
		}
		return success;
	}

	private void Delay() throws InterruptedException {
		// TODO Auto-generated method stub
		if(!CometProperties.getIngestDelay()) return;
		
		IngestorJSON ij=IngestorJSON.getInstance();
		
		Thread.sleep(ij.getSize(getThreadID()));

	}

	//verify that a file has been migrated to HCP
	boolean verifyFile(File inFile) throws Exception {
		
		if(CometProperties.WasTerminated()) {
			logger.severe("COMET was externally terminated prematurely");
			return false;
		}
		
		
		if ( null == inFile) {
			logger.severe("Invalid input parameter.  inFile is null");
			return false;
		}
		
		if ( ! bIsInitialized ) {
			logger.severe("Programming Error. Object Not Initialized");
			return false;
		}
		
		
		mCurrentFile = inFile;
		
		//need to build the encodedPathURL
		URL encodedPathURL=AnnotationHelper.FSToURLPath(CometProperties.getInstance().getDestinationRootPath(pathPrefix),(new File(workingPath)).getAbsolutePath(), mCurrentFile);

		boolean success=CheckExistanceOnHCP(encodedPathURL);
		
		//if writing to HCP fails (eg inappropriate for filetype to write to HCP), just quit
		if(!success) return false;
		
		return success;
	}

	//TODO: move this function into the library
	private String GetFileHeader(File thisFile) throws InterruptedException, IOException {
		String cmd = "/usr/bin/file "+thisFile.toString() ;
		logger.info("run command: " + cmd);
		Runtime run = Runtime.getRuntime() ;
		Process pr = null;
		pr=run.exec(cmd) ;
		pr.waitFor() ;
		BufferedReader buf = new BufferedReader( new InputStreamReader( pr.getInputStream() ) ) ;
		return buf.readLine();
	}
	
	//TODO: move this function into the library
	private boolean UnzipArchive(File inSourceFile) throws IOException, InterruptedException {
		logger.begin("UnzipArchive("+inSourceFile+")");
		String line="";
		String cmd = "" ;
		Runtime run = Runtime.getRuntime() ;
		Process pr = null;
		String srcPath="", tgtPath="";
		line=GetFileHeader(inSourceFile);
		srcPath=inSourceFile.getAbsolutePath();
		tgtPath=inSourceFile.getParentFile().getAbsolutePath();
		
		logger.force("\tsrcPath="+srcPath);
		logger.force("\ttgtPath="+tgtPath);
		
		logger.force("\tline="+line);
		if(line.contains("gzip compressed data")) {
			cmd="/bin/tar -C "+tgtPath+" -xvzf "+srcPath;
		} else if(line.contains("xz compressed data")) {
			cmd="/bin/tar  -C "+tgtPath+" -xvJf "+srcPath;
		} else if (line.contains("Zip archive data")) {
			cmd="/usr/bin/unzip "+srcPath+" -d "+tgtPath;
		} else {
			logger.force("\t\tUnable to determine compression type, exiting");
			return false;
		}
		logger.force("\t\tcmd="+cmd);
		
		//logger.fine("\nexecuting: "+cmd+"\n");
		pr = run.exec(cmd) ;
		
		BufferedReader buf = new BufferedReader( new InputStreamReader( pr.getInputStream() ) ) ;
		BufferedReader buferr = new BufferedReader( new InputStreamReader( pr.getErrorStream() ) ) ;
		
		// read everything and output to outputStream as you go
		String s=null;
		ScreenLog.out("===== stdout =====");
		while((s = buf.readLine()) != null) { 
			ScreenLog.out("line="+s);
		}
		
		ScreenLog.out("===== stderr =====");
		while((s = buferr.readLine()) != null) { 
			ScreenLog.out("line="+s);
		} 
		pr.waitFor() ;

		logger.end("UnzipArchive("+inSourceFile+")==exit("+pr.exitValue()+")");
		return pr.exitValue()==0;
	}

	private boolean isArchive(File inSourceFile) {
		return (inSourceFile.toString().contains(".mdo") ||
				inSourceFile.toString().contains(".tgz") ||
				inSourceFile.toString().contains(".xz") ||
				inSourceFile.toString().contains(".tar.gz") ||
				inSourceFile.toString().contains(".zip"));
	}

//	
//	  * Internal function to look at the HCP machine and determine the passed object exists
//	  *   on the system and whether it has custom-metadata or not.  It accomplishes this by
//	  *   doing an HCP HTTP REST HEAD request to the object and looks at the metadata returned
//	  *   about the object.
//	  * 
//	  * @param inDestinationPath URL to HCP file to retrieve state.
//	  * @return ObjectState enumeration value as to the state on the HCP system.
//	  * @throws ClientProtocolException
//	  * @throws IOException
//	  * @throws HttpResponseException
//	  
	//TODO: deprecate this function and instead directly setup and call client.HCPObjectExists
	//		OR: create static helper functions in HCPClient
	 private Boolean CheckExistanceOnHCP(URL encodedPathURL) throws URISyntaxException, IOException {
		 HCPClient client=new HCPClient(CometProperties.getInstance());
		 client.setRootpath(CometProperties.getInstance().getDestinationRootPath(pathPrefix));
		 return client.HCPObjectExists(encodedPathURL);
	 }	 
	 
	/**
	 * This method performs a PUT of an object data file and/or custom metadata depending
	 * on the state of the object on the HCP system and the configuration of the execution
	 * based on the properties file and the HCP system version.
	 * @param objectOnly 
	 * @throws Exception 
	 */
	 //TODO: Create our own Exceptions and throw them
	 private Boolean WriteToHCP(String inInitialPath, File inSourceFile, boolean objectOnly) throws Exception {
		 Boolean retVal = Boolean.TRUE; // Let's be optimistic.
		 logger.info("Processing File: " + inSourceFile.getCanonicalPath());
			
		 //Build the destination path based on the source path.
		 //TODO: newer functions are not portable and assume Linux file system
		 URL encodedPathURL=AnnotationHelper.FSToURLPath(CometProperties.getInstance().getDestinationRootPath(pathPrefix),inInitialPath,inSourceFile);
		 if(!CheckExistanceOnHCP(encodedPathURL)) {
			 retVal=retVal && WriteObjectToHCP(encodedPathURL, inSourceFile);
		 } 
		 if(!objectOnly) {
			 retVal=retVal && WriteAnnotationsToHCP(encodedPathURL, inSourceFile);
		 }
		 return retVal;
	 }

	 //TODO: consider combining this with WriteToHCP; or moving functionality into HCPClient
	 public Boolean WriteObjectToHCP(URL encodedPathURL, File inSourceFile) throws Exception {
		 if(inSourceFile.toString().endsWith(".inuse")) throw new IOException();
		 ScreenLog.begin("Write Object to HCP");
		 HCPClient client=new HCPClient(CometProperties.getInstance());
		 client.setRootpath(CometProperties.getInstance().getDestinationRootPath(pathPrefix));
		 FileInputStream fis=null;
		 Boolean fileLock=false;
		 //FileLocker fis=null;
		 File fileLockFile=new File(inSourceFile.getAbsolutePath()+".inuse");
		 if(CometProperties.getInstance().getUseFileLocking()) {
			 if(fileLockFile.exists()) {
				 ScreenLog.warning("\tLock file already exists, bail:"+inSourceFile.getAbsolutePath()+".inuse");
				 return true; //acceptable outcome
			 } else {
				 ScreenLog.out("\tLock file does not exist, create it:"+inSourceFile.getAbsolutePath()+".inuse");
				 StringHelper.touch(fileLockFile);
				 fileLock=true;
			 }
		 } else {
			 ScreenLog.fine("\tNot using file locking");
		 }
		
		 fis=new FileInputStream(inSourceFile);
		 String captured=client.HttpPutHCPContent(new InputStreamEntity( fis, -1), encodedPathURL);
		
		 fis.close();
		 if(fileLock) {
			 ScreenLog.fine("deleting file lock");
			 fileLockFile.delete();
		 }
		 ScreenLog.out("output from PUT operation: "+captured);
		 ScreenLog.out("filename("+inSourceFile.toString()+") status code = "+client.getStatusCode()+"\n");
		 
		 if(409==client.getStatusCode()) {
			 logger.fine(" Object already exists on HCP, ignore the error for transaction \"" + inSourceFile.getAbsolutePath() + "\" to \"" + encodedPathURL);
		 }
		 // If the return code is anything BUT 200 range indicating success, we have to throw an exception.
		 else {
		 	if (2 != client.getStatusCode() / 100) eObjectStatus = WriteStatus.WRITE_FAILURE;
		 	else {
		 		eObjectStatus = WriteStatus.WRITE_SUCCESS;
		 	}
		 	Date d=new Date();
			logger.force("["+d.toString()+"] PUT \"" + inSourceFile.getAbsolutePath() + "\" to \"" + encodedPathURL+"\" "+client.getStatusCode());
		 }
		 ScreenLog.end("Write Object to HCP");
		 return eObjectStatus==WriteStatus.WRITE_SUCCESS;
	 }

	 /**
	 * Writes custom-metadata ONLY to an already existing object in HCP.
	 * 
	 * @param encodedPathURL
	 * @param inSourceFile
	 * @throws Exception 
	 */
	 //TODO: consider moving this function into HCPClient
	 private Boolean WriteAnnotationsToHCP(URL encodedPathURL, File inSourceFile) throws Exception {
		 annotations= mCustomMetadataGenerator.generateAnnotations(inSourceFile);
		 HCPClient client=new HCPClient(CometProperties.getInstance());
		 client.setRootpath(CometProperties.getInstance().getDestinationRootPath(pathPrefix));
			ThreadTrackerDB.updateDBOverHTTP(mCurrentFile.getAbsolutePath()+"-writing-metadata",threadID, mCurrentFile.length());

		for (String key : annotations.keySet()) {
			if(key.equals("") || annotations.get(key).equals("") || annotations.get(key).contains("ignore")) {
				//annotations.remove(key);
				logger.fine("annotation "+key+" was blank or marked to be ignored, skipping");
				ScreenLog.out("\tannotation "+key+" was either blank or marked to be ignored, don't ingest");
				annotations.put(key, "");
				continue;
			} else {
				ScreenLog.out("\tannotation "+key+" was neither blank nor marked to be ignored, ingesting...");
			}
			String currentAnnotation=key;
			logger.info("Sending PUT Custom Metadata to URL:  " + encodedPathURL+" for annotation "+currentAnnotation);
			if(annotations.containsKey(key) && !annotations.get(key).equals("")) 
				client.HttpPutHCPContent(new InputStreamEntity( new ByteArrayInputStream(annotations.get(key).getBytes()), -1),new URL(encodedPathURL + "?type=custom-metadata&annotation="+currentAnnotation));
			if(client.getStatusCode()/100==2) eCustomMetadataStatus = WriteStatus.WRITE_SUCCESS;
		} //end for loop
		
		return eCustomMetadataStatus==WriteStatus.WRITE_SUCCESS;
	}

	public Map<String, String> getAnnotations() {
		return mCustomMetadataGenerator.getAnnotations();
	}

	public HttpClient getmHttpClient() {
		return mHttpClient;
	}

	public void setmHttpClient(HttpClient mHttpClient) {
		this.mHttpClient = mHttpClient;
	}

	public String getHCPName() {
		return CometProperties.getInstance().getDestinationHCPName();
	}

	public String getWorkingPath() {
		return workingPath;
	}

	public void setWorkingPath(String workingPath) {
		this.workingPath = workingPath;
	}

	public String getPathPrefix() {
		// TODO Auto-generated method stub
		return pathPrefix;
	}
	public void setPathPrefix(String pathPrefix) {
		// TODO Auto-generated method stub
		this.pathPrefix=pathPrefix;
	}

	public int getThreadID() {
		return threadID;
	}

	public void setThreadID(int threadID) {
		this.threadID = threadID;
	}

}
