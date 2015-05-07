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
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;

import com.hds.hcp.apihelpers.HCPUtils;

//TODO: move exceptions out to client code
public class ThreadTrackerDB {
	private static ExtendedLogger ScreenLog = new ExtendedLogger(ThreadTrackerDB.class.getName());
	private static String prevState="";
	
	public void closeConnection(Connection conn, Statement stmt) {
		if(stmt!=null) try { stmt.close(); } catch (Exception ignore) {}
		if(conn!=null) try { conn.close(); } catch (Exception ignore) {}
	}
	 
	public Connection openConnection() throws Exception {
		//if(CometProperties.getInstance().getExperimental()) 
		return null;
		//Class.forName("org.sqlite.JDBC");
		//replace with string from comet.properties
		//conn = DriverManager.getConnection("jdbc:sqlite:/"+CometProperties.getInstance().getDatabasePath());
		//return DriverManager.getConnection("jdbc:sqlite:/"+CometProperties.getInstance().getDatabasePath());
	}
	
	public synchronized Boolean updateData(int id, String path, long length) {
		ScreenLog.begin("ThreadTrackerDB:updateData("+id+")");
		Date date = new Date();
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String datestamp=sdf.format(date);
		
		IngestorJSON ij=null;
		
		//new way
//		if(CometProperties.getInstance().getExperimental()) {
			ij=IngestorJSON.getInstance();
			
			ij.setData(id,path,length);
			
			
//		} else {
		
		//old way
//		try {
//			Connection conn=openConnection();
//			Statement stmt=conn.createStatement();
//			stmt.executeUpdate("UPDATE threads set path = '"+path+"', len = '"+length+"', datestamp= '"+datestamp+"' where ID="+id+";");
//			closeConnection(conn,stmt);
//		} catch ( Exception e ) {
//			if(e.getMessage().contains("database is locked")) {
//				ScreenLog.out("database is locked... try again");
//				return false;
//			} else {
//				ScreenLog.out("set("+id+","+path+","+length+") = "+ e.getClass().getName() + ": " + e.getMessage() );
//				ScreenLog.ExceptionOutputHandler(e);
//			}
//		}
//		
//		}
		ScreenLog.end("ThreadTrackerDB::updateData("+id+")");
		return true;
	}

	public synchronized ThreadData get(int id) {
		ScreenLog.begin("ThreadTrackerDB::get("+id+")");
		ThreadData td=new ThreadData();
		td.setPath("");
		td.setLength(0);
		IngestorJSON ij=null;
//		if(CometProperties.getInstance().getExperimental()) {
			ij=IngestorJSON.getInstance();
			td.setPath(ij.getPath(id));
			td.setLength(ij.getSize(id));
			td.setId(id);
			ScreenLog.end("ThreadData::get("+id+") = returning td");

			return td;
//		} else {
//		
//		try {
//			Connection conn=openConnection();
//			Statement stmt=conn.createStatement();
//			ResultSet rs = stmt.executeQuery( "SELECT path,len,datestamp FROM threads WHERE id = "+id+";" );
//			td.setPath("");
//			while ( rs.next() ) {
//				td.setPath(rs.getString("path"));
//				td.setLength(rs.getInt("length"));
//				td.setId(id);
//				td.setDatestamp(rs.getString("datestamp"));
//			}
//			rs.close();
//			closeConnection(conn,stmt);
//			ScreenLog.end("ThreadData::get("+id+") = returning td");
//			return td;
//		} catch ( Exception e ) {
//			ScreenLog.out( "get("+id+") = " +e.getClass().getName() + ": " + e.getMessage() );
//			ScreenLog.ExceptionOutputHandler(e);
//		}
//		}
		
//		ScreenLog.end("ThreadTrackerDB::get("+id+") = returning null");
//		return null;
	}

	public synchronized ThreadData[] getAll() {
//		ScreenLog.begin("ThreadTrackerDB::getAll()");
		//purposely cause an exception if called when experimental
		//if(CometProperties.getInstance().getExperimental())
		return null;
		
		
//		ThreadData [] tdList=new ThreadData[CometProperties.getInstance().getMaximumThreads()];
//		try {
//			Connection conn=openConnection();
//			Statement stmt=conn.createStatement();
//			ResultSet rs = stmt.executeQuery( "SELECT id,path,len,datestamp FROM threads ORDER BY id;");
//			for(int i=0; i<CometProperties.getInstance().getMaximumThreads() && rs.next(); i++ ) {
//				tdList[i]=new ThreadData();
//				tdList[i].setPath(rs.getString("path"));
//				tdList[i].setId(rs.getInt("id"));
//				tdList[i].setLength(Long.parseLong(rs.getString("len")));
//				tdList[i].setDatestamp(rs.getString("datestamp"));
//			}
//			rs.close();
//			closeConnection(conn,stmt);
//			ScreenLog.end("ThreadTrackerDB::getAll() = returning tdlist");
//			return tdList;
//		} catch ( Exception e ) {
//			ScreenLog.ExceptionOutputHandler(e);
//		}
//		ScreenLog.end("ThreadTrackerDB::getAll() = returning null");
//		return null;
	}
		
	public synchronized void newDB() {
		//purposely skip, if called when experimental
//		if(CometProperties.getInstance().getExperimental()) return;
//
//		try {
//			File dbfile=new File(CometProperties.getInstance().getDatabasePath());
//			if(dbfile.exists()) dbfile.delete();
//			Connection conn=openConnection();
//			Statement stmt=conn.createStatement();
//			stmt.executeUpdate("CREATE TABLE threads " +
//					"(ID INT PRIMARY KEY     NOT NULL," +
//					" PATH           TEXT    NOT NULL, " +
//					" LEN         LONG    NOT NULL," +
//					" DATESTAMP    TEXT);");
//			for(int i=0; i<CometProperties.getInstance().getMaximumThreads(); i++) {
//				stmt.executeUpdate("INSERT INTO threads (ID,PATH,LEN,DATESTAMP) VALUES ("+i+",'idle',0,NULL);");
//			}	
//			closeConnection(conn,stmt);
//			
//			CometProperties.setLastDirectory("(none)");
//		} catch ( Exception e ) {
//			ScreenLog.ExceptionOutputHandler(e);
//		}
	}
	
	static private synchronized int HttpCatchError(HttpRequestBase httpRequest, HttpResponse httpResponse) throws IOException {
		if (2 != (int)(httpResponse.getStatusLine().getStatusCode() / 100)) {
			// Clean up after ourselves and release the HTTP connection to the connection manager.
			EntityUtils.consume(httpResponse.getEntity());
			//ScreenLog.out(Headers,"BEGIN header dump");
			throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(),
					"Unexpected status returned from " + httpRequest.getMethod() + " ("
					+ httpResponse.getStatusLine().getStatusCode() + ": " 
					+ httpResponse.getStatusLine().getReasonPhrase() + ")");
		}
		return (int)(httpResponse.getStatusLine().getStatusCode());
	}

	static private synchronized int genericDBActionOverHttpPUT(String args, Boolean force) {
		
		if(!CometProperties.getInstance().getIngestorHeartbeatEnabled()) {
			ScreenLog.out("....requested heartbeat action, but local heartbeat for ingest is disabled, return 200");
			return 200;
		}
		
		if(CometProperties.isHeartBeatTooSoon() && !force) {
			ScreenLog.out("....requested heartbeat action, but heartbeat too rapid; skipping args=="+args+" and return 200");
			ScreenLog.out("\theartbeat was too soon because cp.isHeartBeatTooSoon returned true");
			ScreenLog.out("\tcaused by: (cur time)"+System.currentTimeMillis()+" - (last update)"+ CometProperties.getLastHTTPUpdateTimeStamp()+"  < (time btwn beats) "+ CometProperties.getTimeBetweenBeats());
			return 200;
		}
		
		ScreenLog.begin("ThreadTrackerDB::genericDBActionOverHttpPUT("+args+")");
		HttpResponse httpResponse;
		URL url;
		int status=-1;
		try {
			HttpClient mHttpClient = HCPUtils.initHttpClient();

			url = new URL("http://localhost"+CometProperties.getInstance().getWebAppPath()+"IngestorThreads"+args);

			ScreenLog.out("\n\texec put to "+url);

			
			HttpPut httpRequest = new HttpPut(url.toString());

			httpResponse = mHttpClient.execute(httpRequest);
			status=HttpCatchError(httpRequest, httpResponse);
			
			ScreenLog.out("\n\tresponse from webapp: "+httpResponse.getAllHeaders());

			
			if(httpResponse.getEntity().getContentLength()>0) {
				//capture results, if they exist
				//result=StringHelper.InputStreamToString(is);
				EntityUtils.consume(httpResponse.getEntity());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			ScreenLog.ExceptionOutputHandler(e);
		}
		ScreenLog.end("ThreadTrackerDB::genericDBActionOverHttpPUT("+args+") returning status="+status);
		return status;
	}
	
	static public Boolean isReadyHttp() {
		try { return genericDBActionOverHttpGET("?action=heartbeat",false)==200; } catch (Exception ignore) { }
		return false;
	}

	static private synchronized int genericDBActionOverHttpGET(String args, Boolean force) throws Exception {
		if(!CometProperties.getInstance().getIngestorHeartbeatEnabled()) {
			ScreenLog.fine("requested heartbeat action, but local heartbeat for ingest is disabled, return 200");
			return 200;
		}
		
		if(CometProperties.isHeartBeatTooSoon() && !force) {
			ScreenLog.fine("requested heartbeat action, but heartbeat too rapid; skipping args=="+args+" and return 200");
			return 200;
		}
		ScreenLog.begin("ThreadTrackerDB::genericDBActionOverHttpGET("+args+")");
		
		HttpResponse httpResponse;
		
		URL url;
		int status=-1;
		HttpClient mHttpClient = HCPUtils.initHttpClient();
		url = new URL("http://localhost"+CometProperties.getInstance().getWebAppPath()+"IngestorThreads"+args);
		HttpGet httpRequest = new HttpGet(url.toString());

		httpResponse = mHttpClient.execute(httpRequest);
		status=HttpCatchError(httpRequest, httpResponse);
		if(httpResponse.getEntity().getContentLength()>0) {
			EntityUtils.consume(httpResponse.getEntity());
		}
		//forcefully clear it out
		mHttpClient=null;
		httpResponse=null;
		ScreenLog.end("ThreadTrackerDB::genericDBActionOverHttpGET("+args+") status="+status);
		return status;
	}
	
	static public void heartBeatWatcherModeOverHTTP() {
		genericDBActionOverHttpPUT("?action=heartbeat&watcher",false);
	}
	static public void heartBeatCompleteOverHTTP() {
		
		CometProperties.setUploadPoolSize(0);
		CometProperties.setIngestorIteration(0);
		CometProperties.setTimeInIteration(0);
		ThreadTrackerDB.heartBeatOverHTTP("none","terminated");
		genericDBActionOverHttpPUT("?action=heartbeat&ingest_complete",true);
	}
	
	static public void heartBeatOverHTTP(String directory, String state) {
		heartBeatOverHTTP(directory, state, false);
		
	}	
	static public void heartBeatOverHTTP(String directory, String state, Boolean force) {
		ScreenLog.begin("heartbeatOverHttp("+directory+","+state+")");
		String content="?action=heartbeat&lastdir="+directory;
		
		if(state.equals(prevState)) force=true;
		
		
		if(state!=null && !state.equals("")) content+="&state="+state;
		
		if(CometProperties.getIteration()>=0) content+="&iteration="+CometProperties.getIteration();
		
		if(CometProperties.getPercentComplete()>=0) content+="&percent="+CometProperties.getPercentComplete();

		if(CometProperties.getUploadPoolSize()>=0) content+="&upload_pool_size="+CometProperties.getUploadPoolSize();

		if(CometProperties.getTimeInIteration()>=0) content+="&time_in_iteration="+CometProperties.getTimeInIteration();

		if(CometProperties.customHasContent()) content+="&custom="+AnnotationHelper.URIEncoder(CometProperties.getCustom());
		else {
			ScreenLog.out("custom has no content, so don't post it");
		}
		ScreenLog.out("output from ingestor::: want to put content="+content);
		
		genericDBActionOverHttpPUT(content,force);
		ScreenLog.end("heartbeatOverHttp("+directory+","+state+")");
	}
	
	static public synchronized void updateDBOverHTTP(String path, int id, long len) {
		ScreenLog.begin("updateDBOverHTTP:outter("+path+","+id+",...)");
		IngestorJSON ij=null;
		ij=IngestorJSON.getInstance();
		Boolean force=false;
		if(!ij.getPath(id).equals(path)) {
				ScreenLog.out("\tforcing change because path is different("+path+","+id+",...)");
			
				force=true;
				ij.setData(id, path, len);
		}
		updateDBOverHTTP( path, id, len, force);
		ScreenLog.end("updateDBOverHTTP:outter("+path+","+id+",...)");
		
	}	
	static public synchronized void updateDBOverHTTP(String path, int id, long len, Boolean force) {
		ScreenLog.begin("updateDBOverHTTP("+path+","+id+",...) - returned because heartbeat is disabled");
		if(!CometProperties.getInstance().getIngestorHeartbeatEnabled()) {
			ScreenLog.end("updateDBOverHTTP("+path+","+id+",...) - returned because heartbeat is disabled");
			return;
		}
		
		Boolean cond_met=false;
		int status=-1;
		int tryIt=0;
		while(!cond_met) {
			//use encoder here to allow spaces with ingestor
			status=genericDBActionOverHttpPUT("?action=update&id="+id+"&path="+AnnotationHelper.URIEncoder(path)+"&length="+len,force);
			cond_met=(status!=500);
			if(!cond_met) {
				try { Thread.sleep(1000); } catch (Exception ignored) {}
			}
			tryIt++;
			
			if(tryIt>10) {
				cond_met=true;
				ScreenLog.out("\tafter 10 attempts.. giving up");
			}
		}
	}
	
//	static public void newDBOverHTTP() {
//		if(!CometProperties.getInstance().getExperimental())
//		genericDBActionOverHttpPUT("?action=new",true);
//	}
}


