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
//Package: Custom Object Metadata Enhancement Toolkit shared library
//Author: Chris Delezenski <chris.delezenski@hdsfed.com>
//Compilation Date: 2015-05-06
//License: Apache License, Version 2.0
//Version: 1.21.0
//(RPM) Release: 1
//SVN: r554
package com.hdsfed.cometapi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import com.hds.hcp.apihelpers.HCPUtils;

//TODO: functions need descriptions etc
//		some of these variables could probably be deprecated
//      Probably shouldn't ignore IOExceptions
//		Move exception handling to client programs
//      no need for both reload and refresh
//		need to be organize naming convention for variables
public class CometProperties {
	private static final String VERSION ="1.21.0";
	private static final String COMPILE_DATE ="2015-05-06";
	private static final String SVN ="554";

	private static final String defaultAnnotation="default";
	private static final String installDirectory="/opt/COMETDist/";
	private static final String DEFAULT_PROPERTIES_FILE = "/opt/COMETDist/comet.properties";
	private static final String DEFAULT_ROLES_FILE = "/opt/COMETDist/roles.json";
	private static String mPropertiesFilename = DEFAULT_PROPERTIES_FILE;
	private static Properties mProps;
	private String sEncodedUserName;
	private String sEncodedPassword;
//	private static LinkedList<CacheMap> downloadCache;
	private static LinkedList<String> downloadCache;
	private static String classPrefixMD = "ingestor.metadata";
	private static String classPrefixPreProc = "ingestor.preprocessing";
	private static String classPrefixPostProc = "ingestor.postprocessing";
	private static String classPrefixWalker = "ingestor.walkers";
	
	private static String currentUser;
	private static String currentRole;
	private static CometProperties instance;
	private static String internal_hdds_user_name;	
	private static String internal_hdds_password;
	private static String heartBeat="none";
	private static String lastDirectory="none";
	private static Boolean ingestionComplete=false;
	private static Boolean configMode=false;
	//change to enum in v1.21
	private static String ingestorRunningState="init";
	private static Boolean watcherMode;
	
	//probably should move these to another structure
	private static int percentComplete;
	private static int iteration;
	private static int uploadPoolSize;
	private static long timeInIteration;
	private static long timeInIterationTimeStamp;
	private static boolean terminated=false;
	private static String custom="";
	private static long lastHTTPUpdateTimeStamp=0;
	private static long timeBetweenBeats=-1;
	
	public void setInternalHDDSPassword(String password) {
		internal_hdds_password=password;
	}

	public void SetInternalHDDSUserName(String user_name) {
		internal_hdds_user_name=user_name;
	}

	public CometProperties() throws IOException {
		String propFile = System.getProperty("com.hds.asd.Comet.properties.file");
		
		// If we got something from the environment, use it.
		if (null != propFile && 0 < propFile.length()) {
			mPropertiesFilename = propFile;
		}
		refresh();
	}
	
	public CometProperties(String inPropertiesFile) throws IOException {
		mPropertiesFilename = inPropertiesFile;
		refresh();
	}

	public static Boolean isInstanceAvailable() {
		return (null!=instance);
	}
	
	public static void resetInstance()  {
		instance=null;
		instance = getInstance();
	}

	public static CometProperties getInstance() {
		if (instance == null)
			try {
				instance = new CometProperties(mPropertiesFilename);
//				CometProperties.ScreenLog.setDebug(instance.getDebug());
//				CometProperties.ScreenLog.setSilence(!instance.getDebug() && !instance.getVerbose());
				CometProperties.downloadCache=new LinkedList<String>(); 
			} catch (IOException ignored) { }
		return instance;
	}
	
	public String getPropFile() {
			return mPropertiesFilename;
	}
	
	public void reload() {
		try {
			refresh();
		} catch (IOException ignored) { }
	}
	private void refresh() throws IOException {
		mProps = new Properties();
		mProps.load(new FileInputStream(mPropertiesFilename));
	}

	public void save() throws FileNotFoundException, IOException {
		mProps.store(new FileOutputStream(mPropertiesFilename), null);
		
	}
	
	
	/***
	 * 
	 * SOURCE CONTENT PROPERTIES
	 * 
	 ***/
	public String getSourcePath() { //varlist: source.path = Default: /opt/COMETDist/InputDir.  Path to the data source or CSV of multiple data source paths. eg: /opt/COMETDist/InputDir
		return mProps.getProperty("source.path","/opt/COMETDist/InputDir");
	}

	public Boolean getSourceRO() { //varlist: source.readonly = Default: false.  When true, treat the data source as read-only and disable any parameters that imply r/w access.
		//skipping upload implies r/o
		if(getSkipUpload()) return true;
		
		return new Boolean(mProps.getProperty("source.readonly","false"));
	}
	
	/***
	 * 
	 * DESTINATION PROPERTIES
	 * 
	 ***/
	public String getDestinationUserName() { //varlist: destination.user = Default: (none).  HCP user to be used for all transactions.
		return mProps.getProperty("destination.user","");
	}

	public String getDestinationPassword() { //varlist: destination.password = Default: (none). HCP user's password, pre-encoded.
		return mProps.getProperty("destination.password","");
	}
	
	public Boolean isDestinationPasswordEncoded() { //varlist: destination.passwordEncoded = Not used
		return new Boolean(mProps.getProperty("destination.passwordEncoded", "true"));
	}
	public String getDestinationHCPNamespace() { //varlist: destination.hcp_config_namespace = Default: fall back to data namespace.  This is the configuration namespace on HCP for all COMET configuration data.
		//varlist: destination.hcp_namespace = Default: namespace.  This is the data namespace used by COMET on HCP.
		if(getConfigMode()) return mProps.getProperty("destination.hcp_config_namespace",mProps.getProperty("destination.hcp_namespace","namespace")); 
		return mProps.getProperty("destination.hcp_namespace","namespace");
	}
	public String getDestinationHCPTenant() { //varlist: destination.hcp_tenant = Default: tenant.  This is the tenant used by COMET on HCP.

		return mProps.getProperty("destination.hcp_tenant","tenant");
	}
	public String getDestinationHCPName() {  //varlist: destination.hcp_name = Default: hcp.  This is the name of the HCP cluster.
		return mProps.getProperty("destination.hcp_name","hcp");
	}

	public String getDestinationDomainName() { //varlist: destination.domain = Default: domain.com.  This is the domain of HCP.

		return mProps.getProperty("destination.domain","domain.com");
	}
	
	public URL getDestinationRootPath() throws IOException {
		return getDestinationRootPath("");
	}
	
	public URL getDestinationRootPath(String pathPrefix) throws IOException {
		if(neverUsePrefixes()) pathPrefix="";
		return new URL("https://"+getDestinationHCPNamespace()+"."+getDestinationHCPTenant()+"."+getDestinationHCPName()+"."+getDestinationDomainName()+"/rest"+pathPrefix);
	}

	public URL getMQESearchPath() throws MalformedURLException {
		return new URL("https://"+getDestinationHCPTenant()+"."+getDestinationHCPName()+"."+getDestinationDomainName()+"/query");
	}
	public URL getNEOSearchPath() throws MalformedURLException {
		return new URL("https://"+getHDDSName()+":"+getHDDSPort()+"/hdds/api");
	}
	
	public URL getNEOWebApiResponseDTD() throws MalformedURLException {
		return new URL("https://"+getHDDSName()+":"+getHDDSPort()+"/hdds/dtd/WebApiResponse.dtd");
	}
	
	/***
	 * 
	 * METADATA GENERATION MODULE PROPERTIES
	 * 
	 ***/
	public LinkedList<String> getMetadataClasses() { 		//varlist: metadata.classes = Default: (none).  This is a CSV list of metadata extraction modules.

		LinkedList<String> retVal = new LinkedList<String>();
		String[] list = mProps.getProperty("metadata.classes").split(",");
		for (int i = 0; i < list.length; i++) {
			if ( ! list[i].isEmpty())
				retVal.addLast(classPrefixMD+list[i]);
		}
		return retVal;
	}

	
	public LinkedList<File> getNativeLibModules() { //varlist: metadata.nativelibs = not used

		LinkedList<File> retVal = new LinkedList<File>();
		String[] list = mProps.getProperty("metadata.nativelibs").split(",");
		for (int i = 0; i < list.length; i++) {
			if ( ! list[i].isEmpty())
				retVal.addLast(new File(list[i]));
		}
		return retVal;
	}
	
	public String getGeoSpatialLibrary() { //varlist: metadata.gdalnative = not used
		return new String(mProps.getProperty("metadata.gdalnative","gdalnative"));
	}
	
	public LinkedList<String> getPreProcessorClasses() { //varlist: preprocess.classes = Default: (none).  This is a CSV of Java modules to be used during preprocessing, once per ingestion loop.

		if(!isPreProcessingEnabled() || mProps.getProperty("preprocess.classes")==null) return null;
		LinkedList<String> retVal = new LinkedList<String>();
		String[] list = mProps.getProperty("preprocess.classes").split(",");
		for (int i = 0; i < list.length; i++) {
			if ( ! list[i].isEmpty()) {
				if(list[i].startsWith(".")) retVal.addLast(classPrefixPreProc+list[i]);
				else retVal.addLast(list[i]);
			}
		}
		return retVal;
	}

	public LinkedList<String> getPostProcessorClasses() { //varlist: postprocess.classes = Default: .TimePostProcess(60).  This is a CSV of Java modules to be used during postprocessing, once per ingestion loop.

		if(!isPostProcessingEnabled() || mProps.getProperty("postprocess.classes")==null) return null;
		LinkedList<String> retVal = new LinkedList<String>();
		String[] list = mProps.getProperty("postprocess.classes").split(",");
		for (int i = 0; i < list.length; i++) {
			if ( ! list[i].isEmpty()) {
				if(list[i].startsWith(".")) retVal.addLast(classPrefixPostProc+list[i]);
				else retVal.addLast(list[i]);
			}
		}
		return retVal;
	}

	public LinkedList<String> getWalkerClasses() { //varlist: walker.classes = Default: .DefaultWalker.  This is a CSV of Java modules to be used during file walk, once per ingestion loop.

		if(mProps.getProperty("walker.classes",".DefaultWalker")==null) return null;
		LinkedList<String> retVal = new LinkedList<String>();
		String[] list = mProps.getProperty("walker.classes",".DefaultWalker").split(",");
		for (int i = 0; i < list.length; i++) {
			if ( ! list[i].isEmpty()) {
				if(list[i].startsWith(".")) retVal.addLast(classPrefixWalker+list[i]);
				else retVal.addLast(list[i]);
			}
		}
		return retVal;
	}

	public Boolean isPreProcessingEnabled() { //varlist: preprocess.enabled = Default: false.  Enable preprocessing classes.

		return new Boolean(mProps.getProperty("preprocess.enabled","false"));
	}

	public Boolean isPostProcessingEnabled() {//varlist: postprocess.enabled = Default: false.  Enable postprocessing classes.
		return new Boolean(mProps.getProperty("postprocess.enabled","false"));
	}
	
	public String getProperty(String key, String defaultval) {
		return mProps.getProperty(key, defaultval);
	}	
	
	
	/***
	 * 
	 * GENERAL EXECUTION BEHAVIOR PROPERTIES
	 * 
	 ***/
	public static final Integer LOOPCOUNT_INFINITE = -1;

	//BUG: at the moment, we only support infinite loop (continuous) mode
	public Integer getLoopCount() {
//		return new Integer(mProps.getProperty("execution.loopCount", "-1"));
		return -1;
	}
	
	public Boolean isInfiniteLoopCount() {
		return new Boolean(getLoopCount().equals(LOOPCOUNT_INFINITE));
	}
	
	public String getIngestionMode() {
		if(isInfiniteLoopCount()) return "continuous";
		return "run on demand";
	}
	
	public static Boolean isInfiniteLoopCount(int inLoopCount) {
		return true; //new Boolean(inLoopCount == LOOPCOUNT_INFINITE);
	}
	
	//Need to replace /opt/COMETDist/ with a sed variable
	public File getStopFileName() { //varlist: execution.stopRequestFile = not used
		return new File(mProps.getProperty("execution.stopRequestFile", "/opt/COMETDist/ingestor.stop"));
	}
	
	public File getPauseFileName() { //varlist: execution.pauseRequestFile = not used
		return new File(mProps.getProperty("execution.pauseRequestFile", "/opt/COMETDist/ingestor.pause"));
	}

	//request reload of comet.properties
	public File getReloadFileName() { //varlist: execution.reloadRequestFile = not used
		return new File(mProps.getProperty("execution.reloadRequestFile","/opt/COMETDist/ingestor.resume"));
	}

	//tell ingestor to save the cache to disk
	public File getFlushCacheFileName() { //varlist: execution.flushCacheRequestFile = not used
		return new File(mProps.getProperty("execution.flushCacheRequestFile","/opt/COMETDist/ingestor.flushcache"));
	}

		
	public Boolean shouldDeleteSourceFiles() { //varlist: execution.deleteSourceFiles = Default: true.  Delete source files after ingestion, provided the source path is read write.
		return new Boolean(mProps.getProperty("execution.deleteSourceFiles", "true")) && !getSourceRO();
	}
	
	public Boolean shouldForceDeleteSourceFiles() {  //varlist: execution.forceDeleteSourceFiles = Default: true.  Delete source files after ingestion, provided the source path is read write, overriding DAC permissions if necessary.
		return new Boolean(mProps.getProperty("execution.forceDeleteSourceFiles", "true")) && !getSourceRO();
	}
	
	public Boolean shouldDeleteSourceEmptyDirs() {  //varlist: execution.deleteSourceEmptyDirs = Default: true.  Delete source directories after ingestion, provided the source path is read write.
		return new Boolean(mProps.getProperty("execution.deleteSourceEmptyDirs", "true")) && !getSourceRO();
	}

	public Boolean shouldDeleteExistingMetadataOnEmpty() {  //varlist: execution.deleteExistingMetadataOnEmpty = not used
		return new Boolean(mProps.getProperty("execution.deleteExistingMetadataOnEmpty", "false")) && !getSourceRO();
	}
	public Boolean shouldExecute() {  //varlist: execution.execute = not used
		return new Boolean(mProps.getProperty("execution.execute", "true"));
	}
	public Boolean shouldUnzip() {  //varlist: execution.unzip = Default: true.  Unzip archive files (.zip, .tgz etc) prior to ingest.  Source path must be writable.
		return new Boolean(mProps.getProperty("execution.unzip", "true")) && !getSourceRO();
	}
	
	public Integer getMaximumThreads(String key, int defaultNumThreads) {
		return new Integer(mProps.getProperty(key, Integer.toString(defaultNumThreads)));
	}
	public Integer getMaximumThreads() {  //varlist: execution.maxThreads = Default: 10.  This is the number of threads used while migrating data to HCP.
		return getMaximumThreads("execution.maxThreads", 10);
	}

	
	public Boolean shouldUpdateMetadata() { //varlist: execution.updateMetadata = Default: true.  Update metadata of existing objects.
		return new Boolean(mProps.getProperty("execution.updateMetadata", "true"));
	}
	
	public Integer getPauseSleepTime() { //varlist: execution.pauseSleepInSeconds = Default: 10. Time between checks for ingestor.pause.
		return new Integer(mProps.getProperty("execution.pauseSleepInSeconds", "10"));
	}
	
	public Integer getLoopSleepTime() { //varlist: execution.loopSleepInSeconds = not used
		return new Integer(mProps.getProperty("execution.loopSleepInSeconds", "60"));
	}
	
	public Boolean shouldDumpHTTPHeaders() { //varlist: execution.debugging.httpheaders = not used
		return new Boolean(mProps.getProperty("execution.debugging.httpheaders", "false"));
	}

	public Boolean getUseFileLocking() { //varlist: execution.useFileLocking = Default: true.  When set true on a read/write file system, the ingest process will create .inuse files to lock files currently under migration.  These lock files allow the same file system to be serviced by multiple COMETs.
		
		return new Boolean(mProps.getProperty("execution.useFileLocking", "true")) && !getSourceRO();
	}
	public Boolean getUseFileLockingFallBack() {//varlist: execution.useFileLocking.allowFallBack = not used
		return new Boolean(mProps.getProperty("execution.useFileLocking.allowFallBack", "true")) && !getSourceRO();
	}
	
	public String getHDDSUserName() {//varlist: hdds.user = Default: comet.  User on HDDS for content search. 
		String temp=""; 
		String cached_user=mProps.getProperty("hdds.user", "comet");
		if(cached_user!=null && cached_user!="") temp=cached_user;
		if(internal_hdds_user_name=="" || internal_hdds_user_name==null) {
			internal_hdds_user_name="";
			return temp;
		}
		return internal_hdds_user_name;
		
	}
	public String getHDDSPassword() { //varlist: hdds.password = Default: none.  Base64 encoded password for HDDS user. 
		String temp=""; 
		String cached_password=mProps.getProperty("hdds.password", "cGFzc3dvcmQxIQ==");
		if(cached_password!=null && cached_password!="") temp=cached_password;
		if(internal_hdds_password=="" || internal_hdds_password==null) {
			internal_hdds_password="";	
			return temp;
		}
		return internal_hdds_password;
	}
	
	public String getCometVersion() { //varlist: comet.version = Default: set at compile time.  Override the compiled-in version of COMET. 
		return new String(mProps.getProperty("comet.version", VERSION+" r"+SVN));
	}
	
	public String getLibraryVersion() {
		return new String("HDSCometHLAPI version "+VERSION+" svn r"+SVN+" Compiled on "+COMPILE_DATE);
	}
	
	public Boolean getShowCaveats() { //varlist: showCaveats = Default: true.  Show classification banners in the UI. 
		return new Boolean(mProps.getProperty("showCaveats","true"));
	}
	
	public Boolean getEnableTranscoding() { //varlist: enableTransocde = Default: false.  Enables automatic transcoding of uploaded video to webm via ffmpeg
		return new Boolean(mProps.getProperty("enableTranscoding", "false"));
	}

	public String getWordCloudContent() { //varlist: wordCloud = Default: /WordClouds/scenarios.html.  Starting point for word cloud tab. 
		return mProps.getProperty("wordCloud","/WordClouds/scenarios.html");
	}
	
	public Boolean getDebug() { //varlist: debug = Default: false.  Universal debug mode.  Greatly increases log traffic. 
		return new Boolean(mProps.getProperty("debug","false"));
	}
	
	public Boolean getVerbose() { //varlist: verbose = Default: false.  Slightly increase verbosity, in particular in the javascript client code. 
		return new Boolean(mProps.getProperty("verbose","false")) || getDebug();
	}
	
//	public Boolean getGE() { //varlist: google_earth = Default: false.  Enable use of the Google Earth browser plugin for geo spatial visualization. 
//		return new Boolean(mProps.getProperty("google_earth","false"));
//	}

	public Boolean getIngestorEnabled() { //varlist: ingestor.enabled = Default: false.  Enable the ingestion service. 
		return new Boolean(mProps.getProperty("ingestor.enabled","false"));
	}
	
	public Boolean getIngestorHeartbeatEnabled() { //varlist: ingestor.heartbeat.enabled = Default: true.  Allow the ingestion service to update its status in the COMET UI via REST. 
		return new Boolean(mProps.getProperty("ingestor.heartbeat.enabled","true"));
	}

	public void setIngestorHeartbeatEnabled(Boolean b) {
		mProps.setProperty("ingestor.heartbeat.enabled",b.toString());
	}
	
	public String getImagePrefix() {
		return httpProtocol()+"://"+getCometName()+getWebAppPath();
	}
	
	public String getWebAppPath() { //varlist: ui.webapp.path = not used 
		return mProps.getProperty("ui.webapp.path","/");
	}
		
	public Integer getMaxSearchResults() { //varlist: maxsearchresults = Default: 100.  Maximum search results to request from HCP via MQE. 
		return Integer.parseInt(mProps.getProperty("maxsearchresults","100").toString());
	}
	
	public String getClassification() { //varlist: classification = Default: UNCLASSIFIED.  System-level classification for demo purposes.
		return mProps.getProperty("classification","UNCLASSIFIED");
	}
	
	public void reload(String newfilename){
		mPropertiesFilename=newfilename;
		try { refresh(); } catch (IOException ignored) { }
	}
	
	public static Map<String,String> getPropertiesMap(Map<String,String> parameters) throws IOException {
		return getPropertiesMap(CometProperties.DEFAULT_PROPERTIES_FILE, parameters);
	}	

	public static Map<String,String> getPropertiesMap(String prop_file_name, Map<String,String> parameters) throws IOException {
		Map<String, String> props=new HashMap<String,String>();
		
		CometProperties mprop=CometProperties.getInstance();
		
		//shouldn't need this line
		mprop.reload(prop_file_name);

		props.put("hdds_name", mprop.getHDDSName());
		props.put("hdds_enabled", mprop.getHDDSEnabled().toString());
		props.put("showCaveats", mprop.getShowCaveats().toString());
		
		if(parameters.containsKey("comet_version"))
		props.put("comet_version", parameters.get("comet_version"));
		else
		props.put("comet_version", mprop.getCometVersion());
			
		props.put("hcp_user", mprop.getDestinationUserName());
		props.put("hdds_user", mprop.getDestinationUserName());
		props.put("verbose",mprop.getVerbose().toString());
		props.put("ingestor_enabled",mprop.getIngestorEnabled().toString());
		props.put("extensions_enabled",mprop.getExtensionsEnabled().toString());
		//necessary?
		//props.put("imageprefix",mprop.getImagePrefix());
		props.put("maxsearchresults",String.valueOf(mprop.getMaxSearchResults()));
		props.put("classification", mprop.getClassification());
		props.put("geo_enabled",mprop.isGeoEnabled().toString());
		props.put("ingestor_multisource", new Boolean(mprop.getSourcePath().contains(",")).toString());
		props.put("ingestor_source", mprop.getSourcePath());
		props.put("destination_rootPath", mprop.getDestinationRootPath().toString());
		props.put("wordCloud", mprop.getWordCloudContent());
		props.put("ingestor_continuous", new Boolean(mprop.getLoopCount().equals(CometProperties.LOOPCOUNT_INFINITE)).toString());
		props.put("reverse_lookup", mprop.getReverseLookup().toString());
		
		props.put("ballon_click","true");
		props.put("allow_geo_edit","true");
		props.put("allow_path_edit","true");
		props.put("allow_kml_load","true");
		props.put("allow_delete","true");
		props.put("allow_notes","true");
		props.put("allow_terminal", "true");
		props.put("allow_editor", "true");
		props.put("allow_upgrade","true");
		props.put("allow_failover",  "true");
		props.put("allow_sysmon", "true");
		
		
		props.put("geodriver", getGeoDriver());
		
		props.put("allow_exp", mprop.getExperimental().toString());
		
		props.put("ingest_poll_frequency", mprop.getIngestorPollFrequency().toString());
		props.put("upgradeDir", mprop.getUpgradeDir());
		
		
		props.put("timing_current" , Long.toString(System.currentTimeMillis()));
		props.put("timing_betweenupdates" , Long.toString(CometProperties.getTimeBetweenBeats()));			

		
		
		if(mprop.getDestinationAutoLoginEnabled()) {
			props.put("autologin_enabled",mprop.getDestinationAutoLoginEnabled().toString());
			props.put("autologin_user", mprop.getDestinationAutoLoginUser());
			props.put("autologin_role", mprop.getDestinationAutoLoginRole());
		}
		
		if(parameters.containsKey("session_id")) {
			for(String s:parameters.keySet()) {
				if(s.startsWith("session_")) {
					props.put(s,parameters.get(s));
				} //end if
			} //end for
		} // end if
		
		
		props.put("migration_src", mprop.getCometName());
		props.put("migration_dest",  mprop.getDestinationHCPNamespace());
		props.put("config_ns", mprop.getDestinationConfigHCPNamespace());
		
		
		props.put("webapp", mprop.getWebAppPath());
		
		return props;
	}
	
	public Boolean isGeoEnabled() {
		return !getGeoDriver().equals("disabled");
	}

	private Boolean getExtensionsEnabled() { //varlist: comet.uiExtensions = Default: false. Enable comet UI extensions (not implemented)
		return new Boolean(mProps.getProperty("comet.uiExtensions","false"));
	}

	private String getDestinationConfigHCPNamespace() {
		Boolean state=configMode;
		configMode=true;
		String result=getDestinationHCPNamespace();
		configMode=state;
		return result;
	}

	private Boolean getHDDSEnabled() { //varlist: hdds.enabled = Default: true.  Enable HDDS integration with COMET
		return new Boolean(mProps.getProperty("hdds.enabled","true"));// TODO Auto-generated method stub
	}

	public String getHDDSName() { //varlist: hdds.name = Default: hdds.domain.com.  Fully qualified name of the HDDS server. 
		return mProps.getProperty("hdds.name","hdds.domain.com");
	}

	public String getHDDSPort() { //varlist: hdds.port = not used 
		return mProps.getProperty("hdds.port","8443");
	}

	public String getCometName() { //varlist: comet.name = Default: comet.domain.com.  The fully qualified domain name of COMET. 
		return mProps.getProperty("comet.name","comet.domain.com");
	}

	public String getSearchConstraints() { //varlist: search.constraints = Default: none.  Add additional MQE style search constraints implicitly to every search.
		return mProps.getProperty("search.constraints","");
	}
	
	public String getDateListMustHave() { //varlist: search.dateList.mustHave = Default: none. All date searches must happen within selected range using this MQE content class element.
		return mProps.getProperty("search.dateList.mustHave","");
	}
	public String getDateListMayHave() { //varlist: search.dateList.mayHave = not used
		return mProps.getProperty("search.dateList.mayHave","");
	}
	public String getDateListMustNotHave() { //varlist: search.dateList.mustNotHave = Default: none. Exclude all date search results that happen within selected range using this MQE content class element.
		return mProps.getProperty("search.dateList.mustNotHave","");
	}
	public Boolean getReverseLookup() { //varlist: search.reverseLookup = Default: true. When enabled, return the URL embedded in the object's content annotation rather than the search result itself.
		return new Boolean(mProps.getProperty("search.reverseLookup","true"));
	}

	public String getDateList() {
		return getDateListMustHave()+";"+getDateListMayHave()+";"+getDateListMustNotHave();
	}

	public static String getClassPrefixMD() {
		return classPrefixMD;
	}

	public static void setClassPrefixMD(String classPrefixMD) {
		CometProperties.classPrefixMD = classPrefixMD;
	}

	public boolean shouldCombineAnnotations() { //varlist: metadata.shouldCombineAnnotations = Default: true. When enabled, all annotations will automatically be combined into a single, HDDS-friendly annotation
		return new Boolean(mProps.getProperty("metadata.shouldCombineAnnotations","true"));
	}

	public String getCombinedAnnotation() { //varlist: metadata.combinedAnnotaiton = Default: default.  This is the name of the annotation to combine all others into.
		return new String(mProps.getProperty("metadata.combinedAnnotation","default"));
	}

	public Boolean getStopOnHit() { //varlist: metadata.stopOnHit = Default: false. When false, use all metadata classes.  When true, stop after first class that yields metadata.
		return new Boolean(mProps.getProperty("metadata.stopOnHit","false"));
	}

	public boolean skipMetadata() { //varlist: metadata.skip = Default: false. When true, no metadata extraction or processing will take place.
		return new Boolean(mProps.getProperty("metadata.skip","false"));
	}
	
	public String getAuthToken() {
		return HCPUtils.HTTP_AUTH_HEADER+": HCP "+sEncodedUserName+":"+sEncodedPassword;
	}

	public static String getCurrentUser() {
		return currentUser;
	}

	public static void setCurrentUser(String currentUser) {
		CometProperties.currentUser = currentUser;
	}

	public static String getCurrentRole() {
		return currentRole;
	}

	public static void setCurrentRole(String currentRole) {
		CometProperties.currentRole = currentRole;
	}

	public URL URLBuilderAltNS(String alternate_ns, String objectPath) throws MalformedURLException {
		return new URL("https://"+alternate_ns+"."+getDestinationHCPTenant()+"."+getDestinationHCPName()+"."+getDestinationDomainName()+"/rest"+objectPath);
	}

	public ArrayList<String> getSkipAllButDirList() { //varlist: metadata.skipAllButDirList = not used
		String temp=mProps.getProperty("metadata.skipAllButDirList","");
		ArrayList<String> results;
		results=new ArrayList<String>();
		if(!temp.contains(",")) {
			results.add(temp);
		} else if (temp!="") {
			results=new ArrayList<String>(Arrays.asList(temp.split(",")));
		}
		return results;
	}

	private ArrayList<String> getSkipMetadataDirList() { //varlist: metadata.skipMetadataDirList = Default: none.  A CSV of directory name fragments to skip for metadata processing.
		String temp=mProps.getProperty("metadata.skipMetadataDirList","");
		ArrayList<String> results;
		results=new ArrayList<String>();
		if(!temp.contains(",")) {
			results.add(temp);
		} else if (temp!="") {
			results=new ArrayList<String>(Arrays.asList(temp.split(",")));
		}
		return results;
	}
	
	public String getMetadataContentExtension() { //varlist: metadata.contentExtension = Default: .m4v.  The file extension of objects considered to be content.
		return new String(mProps.getProperty("metadata.contentExtension",".m4v"));
	}
	
	
	public Boolean shouldSkipMetadata(File inFile) {
		//true no matter what
		if(skipMetadata()) return true;
		
		//special cases
		if(inFile.getName().endsWith("thumb.jpg") || inFile.getName().endsWith("thumb.png") ||
		inFile.getName().endsWith(".html") || 	inFile.getName().endsWith(".notes.txt")) return true;
		ArrayList<String> al=getSkipMetadataDirList();
		for(int i=0; i<al.size(); i++) {
			if(al.get(i).trim().equals("")) continue;
			//found case insensitive filename fragment in list, return true
			if(inFile.toString().toLowerCase().contains(al.get(i).toLowerCase())) return true;
		}
		//not in list, don't skip metadata
		return false;
	}

	public static String getHeartBeat() {
		return heartBeat;
	}

	public static void setHeartBeat() {
		Date date = new Date();
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String heartBeat=sdf.format(date);
		CometProperties.heartBeat = heartBeat;
	}

	public static String getLastDirectory() {
		return lastDirectory;
	}

	public static void setLastDirectory(String lastDirectory) {
		CometProperties.lastDirectory = lastDirectory;
	}

	public static Boolean getIngestionComplete() {
		return ingestionComplete;
	}

	public static void setIngestionComplete(Boolean ingestionComplete) {
		CometProperties.ingestionComplete = ingestionComplete;
		if(ingestionComplete) {
			CometProperties.ingestorRunningState = "complete";
			CometProperties.lastDirectory="(none)";
			CometProperties.heartBeat="(none)";
		} else {
			CometProperties.setHeartBeat();
			//CometProperties.ingestorRunning=true;
		}
	}

	public static void setIngestorWatcher(Boolean watcher) {
		if(!getInstance().getUseWatcher()) {
			return;
		}
		CometProperties.watcherMode = watcher;
		if(watcherMode) {
			//CometProperties.ingestorRunning = false;
			CometProperties.lastDirectory="<-- watching -->";
			CometProperties.setHeartBeat();
		}
	}
	
	public static String getIngestorRunningState() {
		return ingestorRunningState;
	}

	public static void setIngestorRunningState(String ingestorRunningState) {
		CometProperties.ingestorRunningState = ingestorRunningState;
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

	public String getDatabasePath() {  //varlist: comet.dbPath = not used
		return mProps.getProperty("comet.dbPath","/opt/COMETDist/comet.db");
	}

	public Boolean isAdminUser(String username) {  //varlist: multiuser.admins = Default: comet,root. A CSV of users that can become administrators.
		//always return false if no admins are defined
		String admins_csv=mProps.getProperty("multiuser.admins","");
		if(admins_csv.equals("")) return false;
		String [] adminlist=admins_csv.split(",");
		for(int i=0; i<adminlist.length; i++) {
				if(adminlist[i].toLowerCase().equals(username.toLowerCase())) return true;
		}
		//only return true if the admin is confirmed to be in list
		return false;
	}

	public static String getDefaultAnnotation() {
		return defaultAnnotation;
	}

	public static String getInstallDirectory() {
		return installDirectory;
	}

	public static String getDefaultRolesFile() {
		return DEFAULT_ROLES_FILE;
	}
	public static String getDefaultPropertiesFile() {
		return DEFAULT_PROPERTIES_FILE;
	}
	public static String getPropertiesFile() {
		return mPropertiesFilename;
	}
	
	//file system watcher is experimental in this version
	private Boolean getUseWatcher() {  //varlist: ingestor.useWatcher = not used
		return new Boolean(mProps.getProperty("ingestor.useWatcher","false")) && getExperimental();
	}
	
	public Boolean getExperimental() { //varlist: experimental = Default: false.  When true, enable features considered to be experimental.
		return new Boolean(mProps.getProperty("experimental","false"));
	}

	public Integer getMaxFiles() {  //varlist: ingestor.maxFiles = Default: 10,000,000.  Maximum size of the upload file cache.  When the file cache reaches this limit, efficiency will drop and the ingestor will need to check existence on HCP for files already uploaded.
		return Integer.parseInt(mProps.getProperty("ingestor.maxFiles","10000000"));
	}
	
	public static Integer getHCPFailDelay() {  //varlist: ingestor.hcpFailDelay = Default: 2. After an upload failure, sleep for X minutes before trying again.
		return CometProperties.getInstance().getIntProperty("ingestor.hcpFailDelay",2);
 	}
		
	public Integer getIngestorPollFrequency() { //varlist: ingestor.pollFrequency = Default: 3000 (milliseconds).  Allow the UI to poll the REST API once every 3 seconds.  A lower number means higher accuracy, but could lead to "too many open files" error.
		return Integer.parseInt(mProps.getProperty("ingestor.pollFrequency","3000"));
	}

	@SuppressWarnings("unused")
	private String getExternalScript() {  //varlist: ingestor.externalScript = not used
		return mProps.getProperty("ingestor.externalScript","/usr/bin/external.sh");
	}
	@SuppressWarnings("unused")
	private Boolean useExternalScript() {  //varlist: ingestor.useExternalScript = not used
		return new Boolean(mProps.getProperty("ingestor.useExternalScript","false"));
	}

	public Boolean alwaysUsePrefix() {  //varlist: ingestor.alwaysUsePrefix = Default: false.  When true, ingested files will retain their parent directory structure.
		return new Boolean(mProps.getProperty("ingestor.alwaysUsePrefix","false"));
	}
	
	public Boolean neverUsePrefix() {
		return !alwaysUsePrefix();
	}

	public Boolean neverUsePrefixes() {
		return !alwaysUsePrefix();
	}

	public String getLastDir() {  //varlist: ingestor.doLastDir = not used
		return mProps.getProperty("ingestor.doLastDir","");
	}
	public Boolean isXferSecure() {  //varlist: gui.secureXfer = Default: true.  Force internal RESTful calls over HTTPS.
		return new Boolean(mProps.getProperty("gui.secureXfer","true"));
	}
	public String httpProtocol() {
		if(isXferSecure()) return new String("https");
		return new String("http");
	}

	public Boolean hasLastDir() {
		return new Boolean(getLastDir().equals(""));
	}

	public static Boolean getConfigMode() {
		return configMode;
	}

	public static void setConfigMode(Boolean configMode) {
		CometProperties.configMode = configMode;
	}
	
	public String getUpgradeDir() {  //varlist: destination.upgradeDir = Default: /upgrade.  Target directory for RPMs on HCP in the configuration namespace.
		return mProps.getProperty("destination.upgradeDir","/upgrade");
	}

	public Boolean useSortPreferences(String key, Boolean defaultValue) {
		return new Boolean(mProps.getProperty(key,defaultValue.toString()));
	}
	
	public boolean useSortPreferences() {  //varlist: ingestor.useSortPreference = Default: false.  When true, sort the directories to be ingested in a particular way, allowing a specific directory to come first or last.
		return useSortPreferences("ingestor.useSortPreference",false);
	}

	public LinkedList<String> getSortPreferences() {//varlist: ingestor.sortPreference = Default: none.  When ingestor.useSortPreference is true, this variable becomes active.  Sort the directories to be ingested based on a pattern. Eg: *,lastdir means sort everything alphabetically, but lastdir should be pushed to the end.
		return getSortPreferences("ingestor.sortPreferences");
	}

	public LinkedList<String> getSortPreferences(String key) {
		String pref_csv=mProps.getProperty(key,"");
		if(pref_csv.equals("") || pref_csv.equals("*")) return null;
		return new LinkedList<String>(Arrays.asList(pref_csv.split(",")));
	}

	public static int getPercentComplete() {
		return percentComplete;
	}

	public static void setPercentComplete(int percentComplete) {
		CometProperties.percentComplete = percentComplete;
	}

	public static int getIteration() {
		return iteration;
	}

	public static int getIngestorIteration() {
		return getIteration();
	}
	
	public static void setIteration(int iteration) {
		CometProperties.iteration = iteration;
	}
	
	public static void setIngestorPercentComplete(int percent) {
		if(percent<0) percent=0;
		else if(percent>100) percent=100;
		
		setPercentComplete(percent);
	}
	public static void setIngestorPercentComplete(String percent) {
		if(percent==null || percent.equals("")) percent="0"; 
		setPercentComplete(Integer.parseInt(percent));
	}

	public static void setIngestorIteration(int iteration) {
		setIteration(iteration);
	}
	public static void setIngestorIteration(String iteration) {
		if(iteration==null || iteration.equals("")) iteration="0"; 
		setIteration(Integer.parseInt(iteration));
	}

	public static int getUploadPoolSize() {
		return uploadPoolSize;
	}

	public static void setUploadPoolSize(String uploadPoolSize) {
		if(uploadPoolSize==null || uploadPoolSize.equals("")) uploadPoolSize="0"; 
		setUploadPoolSize(Integer.parseInt(uploadPoolSize));
	}
	public static void setUploadPoolSize(int uploadPoolSize) {
		CometProperties.uploadPoolSize = uploadPoolSize;
	}

	public static void Terminate() {
		// TODO Auto-generated method stub
		terminated=true;
	}
	public static Boolean WasTerminated() {
		return terminated;
	}

	public String getFileProcessedCache() { //varlist: ingestor.fileProcessCache = Default: /opt/COMETDist/fileProc.cache.  Path to the file processing cache file.
		// TODO Auto-generated method stub
		return mProps.getProperty("ingestor.fileProcessCache","/opt/COMETDist/fileProc.cache");
	}

	public Boolean useFileProcessedCache() { //varlist: ingestor.useFileProcessCache = Default: true.  When true, uploaded files will be recorded in memory and flushed to a file cache.  The file cache prevents unnecessary extra HTTP transactions and improves performance.
		// TODO Auto-generated method stub
		return new Boolean(mProps.getProperty("ingestor.useFileProcessCache","true"));
	}

	public Boolean getBooleanProperty(String string, Boolean defaultval) {
		// TODO Auto-generated method stub
		return new Boolean(getProperty(string,defaultval.toString()));
	}
	public Integer getIntProperty(String string, Integer defaultval) {
		// TODO Auto-generated method stub
		return new Integer(getProperty(string,defaultval.toString()));
	}

	public Long getLongProperty(String string, Long defaultval) {
		// TODO Auto-generated method stub
		return new Long(getProperty(string,defaultval.toString()));
	}
	
	public TimeUnit getTimeUnitProperty(String string, TimeUnit defaultval) {
		// TODO Auto-generated method stub
		return TimeUnit.valueOf(getProperty(string,defaultval.toString()));
	}

	public static long getTimeInIteration() {
		return timeInIteration;
	}

	public static void setTimeInIteration(String timeInIteration) {
		long t=0;
		if(timeInIteration!=null && !timeInIteration.equals("")) {
			t=Long.parseLong(timeInIteration);
		}
		setTimeInIteration(t);
	}

	public static void setTimeInIteration(long timeInIteration) {
		CometProperties.timeInIteration = timeInIteration;
	}

	public static long getTimeInIterationTimeStamp() {
		return timeInIterationTimeStamp;
	}

	public static void setTimeInIterationTimeStamp(long l) {
		CometProperties.timeInIterationTimeStamp = l;
	}



	public static String getCustom() {
		return custom;
	}

	public static void setCustom(String custom) {
		CometProperties.custom = custom;
	}
	public static Boolean customHasContent() {
		return !CometProperties.custom.equals("");
	}

	public static int MigrationRetryLimit() { //varlist: ingestor.migrationRetryLimit = Default: 10. On upload failure, retry up to X times.
		// TODO Auto-generated method stub
		return CometProperties.getInstance().getIntProperty("ingestor.migrationRetryLimit", 10);
	}

	public static Boolean isHeartBeatTooSoon() {
		Boolean retval=new Boolean(System.currentTimeMillis()-CometProperties.getLastHTTPUpdateTimeStamp() < CometProperties.getTimeBetweenBeats());
		//if difference is greater than 500 milliseconds, then update lastHTTPUpdateTimeStamp
		if(!retval) {
			CometProperties.setLastHTTPUpdateTimeStamp(System.currentTimeMillis());
		} else {
		//	ScreenLog.out("too soon? retval="+retval+" time="+System.currentTimeMillis()+" last update="+System.currentTimeMillis()+" time between="+CometProperties.getTimeBetweenBeats());			
		}
		return retval;
	}
	
	public static void setTimeBetweenBeats(long t) {
		CometProperties.timeBetweenBeats=t;
	}
	public static long getTimeBetweenBeats() {
		if(CometProperties.timeBetweenBeats==-1) CometProperties.timeBetweenBeats=CometProperties.getInstance().getHeartbeatTiming();
		
		
		return CometProperties.timeBetweenBeats;
	}
	public static Boolean getSkipWalk(String s, Boolean b) {
		return CometProperties.getInstance().getBooleanProperty(s,b);
	}
	public static Boolean getSkipWalk() { //varlist: ingestor.skipWalk = Default: false.  When true, skip the file walking process.  Mainly used for debugging or artificially populating the file cache.
		return CometProperties.getSkipWalk("ingestor.skipWalk",false);
	}

	public static Boolean getSkipVerify(String s, Boolean b) {
		return CometProperties.getInstance().getBooleanProperty(s,b);
	}
	public static Boolean getSkipVerify() { //varlist: debug.skipVerify = Default: false.  When true, skip the file verification process.  Mainly used for debugging or artificially populating the file cache.
		return CometProperties.getSkipVerify("debug.skipVerify",false);
	}

	
	public static Boolean getSkipProcessing(String s, Boolean b) {
		return CometProperties.getInstance().getBooleanProperty(s,b);
	}
	public static Boolean getSkipProcessing() { //varlist: debug.skipVerify = Default: false.  When true, skip the file verification process.  Mainly used for debugging or artificially populating the file cache.
		return CometProperties.getSkipProcessing("debug.skipProcessing",false);
	}

	
	public static Boolean getSkipUpload(String s, Boolean b) {
		return CometProperties.getInstance().getBooleanProperty(s,b);
	}
	public static Boolean getSkipUpload() { //varlist: debug.skipVerify = Default: false.  When true, skip the file verification process.  Mainly used for debugging or artificially populating the file cache.
		return CometProperties.getSkipUpload("debug.skipUpload",false);
	}

	public static Boolean getIngestDelay(String s, Boolean b) {
		return CometProperties.getInstance().getBooleanProperty(s,b);
	}
	public static Boolean getIngestDelay() { //varlist: debug.ingestDelay = Default: 5000.  Sleep for 5 seconds during fake-ingest, but only if debug.skipUpload is true
		return CometProperties.getIngestDelay("debug.ingestDelay",false);
	}

	public Boolean getDestinationAutoLoginEnabled() { //varlist: destination.autoLoginEnabled = not used
		return new Boolean(mProps.getProperty("destination.autoLoginEnabled","false"));
	}
	public String getDestinationAutoLoginUser() {//varlist: destination.autoLoginUser = not used
		return new String(mProps.getProperty("destination.autoLoginUser",getDestinationUserName()));
	}
	public String getDestinationAutoLoginRole() {//varlist: destination.autoLoginRole = not used
		return new String(mProps.getProperty("destination.autoLoginRole","general"));
	}

	
	public Integer getHeartbeatTiming() { //varlist: ingestor.heartbeat.timing = Default: 500.  Every X milliseconds, update the UI via RESTful interface.  Setting too small will cause "too many open files".  Setting too large will greatly reduce accuracy of thread tracker.
		return getIntProperty("ingestor.heartbeat.timing", 500);
	}
	
	//solo comet does not have a partner
	public static Boolean isSolo() {
		return getPartner().isEmpty();
	}
	
	public static Boolean isPrimary() { 
		//a solo COMET is always primary
		if(isSolo()) return true;
		
		File isPrime=new File("/opt/COMETDist/ingestor.primary");
		return isPrime.exists();
	}
	
	public static Boolean isSecondary() { 
		return !isPrimary();
	}
	
	public static String getPartner() { //varlist: comet.partnername = Default: not set.  When using high availability, comet.partnername should be set to the fully qualified domain of the matching COMET system.
		return new String(mProps.getProperty("comet.partnername",""));
	}
	
	public static String getDownloadCacheLocation() { //varlist: comet.downloadCacheLocation = Default: /tmp/METACATALOG. temporary directory to download files upon request
		return new String(mProps.getProperty("comet.downloadCacheLocation","/tmp/METACATALOG"));
	}

	public static File DownloadToCache(String path) {
		//make sure downloadCache is solid and not null
		getInstance();
		
		if(!downloadCache.contains(getDownloadCacheLocation()+path)) {
			downloadCache.addFirst(getDownloadCacheLocation()+path);		
		
			//keep the cache relatively small.. no bigger than 10 videos (5 if you consider webm copies)
			if(downloadCache.size()>10) {
				File removethis=new File(downloadCache.getLast());
				downloadCache.removeLast();
				if(removethis.exists()) removethis.delete();
			}
		}
		return new File(getDownloadCacheLocation()+path);
	}
	
	public static String getGeoDriver() {
		return new String(mProps.getProperty("comet.geoDriver","disabled"));
	}
	
	public void disableGeoDriver() {
		mProps.setProperty("comet.geoDriver", "disabled");
	}

	public static long getLastHTTPUpdateTimeStamp() {
		return lastHTTPUpdateTimeStamp;
	}

	public static void setLastHTTPUpdateTimeStamp(long lastHTTPUpdateTimeStamp) {
		CometProperties.lastHTTPUpdateTimeStamp = lastHTTPUpdateTimeStamp;
	}
	
}
