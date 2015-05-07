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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.hds.hcp.apihelpers.HCPUtils;

//TODO: need function descriptions, parameters and "bookends"
//		any exceptions should be thrown, not caught

public class HCPClient {
	private static ExtendedLogger ScreenLog = new ExtendedLogger(HCPClient.class.getPackage().getName());
	public static final String HTTP_AUTH_HEADER = "Authorization";
	private String sEncodedUserName;
	private String sEncodedPassword;
	private String namespace;
	private String tenant;
	private String cluster;
	private String domain;
//	private URL rootpath;
	private HttpClient client;
	private int StatusCode;
	private int MaxSearchResults;
	private Boolean shouldDumpHTTPHeaders;
	private URL MQESearchURL;
	private Map<String,String> Headers;
	private String CombinedAnnotationName;
	private Boolean Execute;
	private static final int fake_sleep_time=100;
	
	private static final int DEFAULT_BUFFER_SIZE = 10240; // ..bytes = 10KB.

	
	private void FakeSleep() {
		try { Thread.sleep(fake_sleep_time); } catch(Exception ignored) {}
	}

	public HCPClient(CometProperties cometProps) throws URISyntaxException, IOException {
		setsEncodedUserName(HCPUtils.toBase64Encoding(cometProps.getDestinationUserName()));
		setsEncodedPassword(cometProps.getDestinationPassword());
		setNamespace(cometProps.getDestinationHCPNamespace());
		setTenant(cometProps.getDestinationHCPTenant());
		setCluster(cometProps.getDestinationHCPName());
		setDomain(cometProps.getDestinationDomainName());
		setShouldDumpHTTPHeaders(cometProps.shouldDumpHTTPHeaders());
		setMaxSearchResults(cometProps.getMaxSearchResults());
		setHeaders(new TreeMap<String,String>());
		setCombinedAnnotationName(cometProps.getCombinedAnnotation());
		setExecute(cometProps.shouldExecute());
		setMQESearchURL(new URL(cometProps.getMQESearchPath().toURI().toString()));
		setStatusCode(0);
		ScreenLog.setDebug(CometProperties.getInstance().getDebug());
		ScreenLog.setSilence(!CometProperties.getInstance().getDebug() && !CometProperties.getInstance().getVerbose());
	}
	
	private String getAuthToken() {
		return "HCP "+sEncodedUserName+":"+sEncodedPassword;
	}

	public String getsEncodedUserName() {
		return sEncodedUserName;
	}
	public void setsEncodedUserName(String sEncodedUserName) {
		this.sEncodedUserName = sEncodedUserName;
	}
	public String getsEncodedPassword() {
		return sEncodedPassword;
	}
	public void setsEncodedPassword(String sEncodedPassword) {
		this.sEncodedPassword = sEncodedPassword;
	}
	public HttpClient getClient() {
		return client;
	}
	public void setClient(HttpClient httpClient) {
		this.client = httpClient;
	}
	public String getNamespace() {
		return namespace;
	}
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	public String getTenant() {
		return tenant;
	}
	public void setTenant(String tenant) {
		this.tenant = tenant;
	}
	public String getCluster() {
		return cluster;
	}
	public void setCluster(String cluster) {
		this.cluster = cluster;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public int getStatusCode() {
		return StatusCode;
	}
	public void setStatusCode(int statusCode) {
		StatusCode = statusCode;
	}
	
	private int HttpHCPCatchError(HttpRequestBase httpRequest, HttpResponse httpResponse) throws IOException {
		return HttpHCPCatchError(httpRequest, httpResponse,false);
	}	
	
	private int HttpHCPCatchError(HttpRequestBase httpRequest, HttpResponse httpResponse, Boolean neverfail) throws IOException {
		Header[] capturedHeader=httpResponse.getAllHeaders();
		for(int i=0; i<capturedHeader.length; i++) {
				Headers.put(capturedHeader[i].getName(),capturedHeader[i].getValue());
		}
		setHeaders(Headers);

		if (2 != (int)(httpResponse.getStatusLine().getStatusCode() / 100)) {
			// Clean up after ourselves and release the HTTP connection to the connection manager.
			EntityUtils.consume(httpResponse.getEntity());
			if(Headers.containsKey("X-HCP-ErrorMessage") && Headers.get("X-HCP-ErrorMessage").contains("Custom Metadatasetting is not allowed on a directory") && httpResponse.getStatusLine().getStatusCode()==400) {
				ScreenLog.out("Caught directory, return 200");
				return 200;
			} else {  
				
				if(!neverfail) {
					throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(),
					"Unexpected status returned from " + httpRequest.getMethod() + " ("
					+ httpResponse.getStatusLine().getStatusCode() + ": " 
					+ httpResponse.getStatusLine().getReasonPhrase() + ")");
				}
			}
		}
		return (int)(httpResponse.getStatusLine().getStatusCode());
	}


	/*
	 * 	ise = content in an InputStreamEntity
	 *  url = full url (fqdn + rest path to object + put modifiers)
	 */
	public String HttpPutHCPContent(InputStreamEntity ise, URL url) throws Exception {
		if(!getExecute()) {
			FakeSleep();
			return "";
		}
		String result="";
		HttpResponse httpResponse;
		HttpClient mHttpClient = HCPUtils.initHttpClient();
		
		ScreenLog.out("(HCPClient) PUT request to HCP is: "+url.toString());
		
		HttpPut httpRequest = new HttpPut(url.toString());
		httpRequest.setEntity(ise);
		httpRequest.setHeader(HTTP_AUTH_HEADER, getAuthToken());
		httpResponse = mHttpClient.execute(httpRequest);

		setStatusCode(HttpHCPCatchError(httpRequest, httpResponse));
		InputStream is=httpResponse.getEntity().getContent();
		if(httpResponse.getEntity().getContentLength()>0) {
			//capture results, if they exist
			result=StringHelper.InputStreamToString(is);
			EntityUtils.consume(httpResponse.getEntity());
		}
		return result;
	}
	
	//url is already the target
	//warning: more than likely, the URL could be formed while making use of the source
	//      url has to be reconstructed by the calling funciton using URLBuilder in CometProperties
	// srcns_and_tenant should look like namespace.tenant
	public void HttpCopyWholeObject(URL url, String srcns_and_tenant_and_path) throws Exception {
		if(!getExecute()) return;
		ScreenLog.begin("HttpCopyWholeObject "+url.toString());
		
		//String result="";
		HttpResponse httpResponse;
		HttpClient mHttpClient = HCPUtils.initHttpClient();
		HttpPut httpRequest = new HttpPut(url.toString());
		httpRequest.setHeader("X-HCP-MetadataDirective","ALL");
		httpRequest.setHeader("X-HCP-CopySource",srcns_and_tenant_and_path);
		httpRequest.setHeader(HTTP_AUTH_HEADER, getAuthToken());
		httpResponse = mHttpClient.execute(httpRequest);
		setStatusCode(HttpHCPCatchError(httpRequest, httpResponse));
		ScreenLog.end("HttpCopyWholeObject "+url.toString());
	}

	private String QueryHCP(InputStreamEntity ise, URL url) throws Exception {
		if(!getExecute()) return "";
		ScreenLog.begin("QueryHCP(ise,"+url.toString()+")");
		String result="";
		HttpResponse httpResponse;
		HttpClient mHttpClient = HCPUtils.initHttpClient();
		HttpPost httpRequest = new HttpPost(url.toString());
		httpRequest.setHeader(HTTP_AUTH_HEADER, getAuthToken());
		httpRequest.setHeader("Content-Type", "application/xml");
		httpRequest.setHeader("Accept", "application/xml");
		httpRequest.setEntity(ise);
		httpResponse = mHttpClient.execute(httpRequest);
		setStatusCode(HttpHCPCatchError(httpRequest, httpResponse));
		result=StringHelper.InputStreamToString(httpResponse.getEntity().getContent());
		EntityUtils.consume(httpResponse.getEntity());
		ScreenLog.end("QueryHCP(ise,"+url.toString()+")");
		return result;
	}
	
	private static String ExtensionToXPath(String ext) {
		return " +(objectPath:/*."+ext+")";
	}
	public static String QueryBuilderFileList(String q) {
		ScreenLog.begin("QueryBuilderFileList("+q+")");
		
		if(q.contains("(")) {
			ScreenLog.out("\tq contains a parenthetical, just return");
			ScreenLog.end("QueryBuilderFileList("+q+")");
			return q;
		}	
	
		String newquery="";
		if(q.contains(",")) {
			String [] extensionlist=q.split(",");
			for(int i=0; i<extensionlist.length; i++) {
				newquery+=ExtensionToXPath(extensionlist[i]);
			}
		} else {
			newquery=ExtensionToXPath(q);
		}
		return newquery;
	}
	
	public static String QueryBuilder(String q, int offset, int max) {
		ScreenLog.begin("QueryBuilder("+q+","+offset+","+max+")");
		if(q.contains(",") && !q.contains("(")) {
			ScreenLog.out("\tq contains a comma and a parenthetical!");
			String [] extensionlist=q.split(",");
			String newquery="";
			for(int i=0; i<extensionlist.length; i++) {
				newquery+=" +(objectPath:/*."+extensionlist[i]+")";
			}
			q=newquery;
		} else {
			ScreenLog.out("\tq DOES NOT contain a comma and a parenthetical!");
		}
		ScreenLog.out("about to build final query and execute query");
		ScreenLog.out("\tquery going in: \""+q+"\"");
		
		//should use serializer here...
		String query="<queryRequest>\n"+
				"	<object>\n<query>" +
				//"		<query>+(cornerY:{35 TO *]) +(cornerY:[* TO 39}) +(cornerX:{-75 TO *]) +(cornerX:[* TO -65})</query>\n" +
				//"       <query>+metadataCaveat:UNCLASSIFIED</query>\n" +
				q +
				"</query>\n" +
				"		<contentProperties>true</contentProperties>\n" +
				"		<count>"+String.valueOf(max)+"</count>\n" +
				"		<facets></facets>\n" +
				"		<objectProperties></objectProperties>\n" +
				"		<offset>"+String.valueOf(offset)+"</offset>\n" +
				"		<sort></sort>\n" +
				"		<verbose>false</verbose>\n" +
				"	</object>\n "+
				"</queryRequest>\n";
		ScreenLog.end("QueryBuilder("+q+","+offset+","+max+")");
		return query;
	}

	public String QueryHCP(Document query) throws Exception {
		return QueryHCP(XMLHelper.DocToInputStreamEntity(query), getMQESearchURL());
	}
	public String QueryHCP(String query) throws Exception {
		return QueryHCP(new InputStreamEntity( new ByteArrayInputStream(query.getBytes()), -1), getMQESearchURL());
	}
	public String HttpPutHCPContent(String xml_content, URL url) throws Exception {
		return HttpPutHCPContent(new InputStreamEntity( new ByteArrayInputStream(xml_content.getBytes()), -1), url);
	}
	public String HttpPutHCPContent(File inFile, URL url) throws FileNotFoundException, Exception {
		return HttpPutHCPContent(new InputStreamEntity( new FileInputStream(inFile), -1), url);
	}
	public String HttpPutHCPContent(Document doc, URL url) throws Exception {
		return HttpPutHCPContent(XMLHelper.DocToString(doc), url);
	}
	
	/*
	 * 	ise = content in an InputStreamEntity
	 *  url = full url (fqdn + rest path to object + put modifiers)
	 */
	public String HttpGetHCPHeader(URL url, String header) throws Exception {
		return HttpGetHCPHeader(url, header,true);
	}
	
	public String HttpGetHCPHeader(URL url, String header, Boolean neverfail) throws Exception {
		if(!getExecute()) return "";
		HttpResponse httpResponse;
		HttpClient mHttpClient = HCPUtils.initHttpClient();
		HttpHead httpRequest = new HttpHead(url.toString());
		httpRequest.setHeader(HTTP_AUTH_HEADER, getAuthToken());
		httpResponse = mHttpClient.execute(httpRequest);
		setStatusCode(HttpHCPCatchError(httpRequest, httpResponse, neverfail));
		if(header=="") return "";
		if(!Headers.containsKey(header)) return "";
		return Headers.get(header);
	}

	public void HttpGetHCPHeader(URL url) throws Exception {
		HttpGetHCPHeader(url,"",true);
	}
	
	public Boolean HCPObjectExists(String path)  {
		Boolean r=false;
		try { r=HCPObjectExists(AnnotationHelper.PathToURL(getRootpath(), path)); } catch (MalformedURLException ignore) {}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return r;
	}
	
	public Boolean HCPObjectExists(URL url) {
		try { HttpGetHCPHeader(url); } catch (Exception ignore) { setStatusCode(-1); }
		return Headers.containsKey("X-HCP-Type");
	}
	
	public String HttpGetHCPContent(URL url) throws Exception {
		ScreenLog.begin("HttpGetHCPContent("+url.toString()+")");
		if(!getExecute()) {
			FakeSleep();
			return "";
		}
		String result="";
		HttpResponse httpResponse;
		HttpClient mHttpClient = HCPUtils.initHttpClient();
		HttpGet httpRequest = new HttpGet(url.toString());
		httpRequest.setHeader(HTTP_AUTH_HEADER, getAuthToken());
		httpResponse = mHttpClient.execute(httpRequest);
		setStatusCode(HttpHCPCatchError(httpRequest, httpResponse));
		InputStream is=httpResponse.getEntity().getContent();
		if(httpResponse.getEntity().getContentLength()>0) {
			//capture results, if they exist
			result=StringHelper.InputStreamToString(is);
			EntityUtils.consume(httpResponse.getEntity());
		}
		ScreenLog.end("HttpGetHCPContent("+url.toString()+")");
		return result;
	}
	
	public String HttpDeepCopyHCPContent(URL from_url, URL to_url) throws Exception {
		return HttpPutHCPContent(HttpGetHCPContent(from_url), to_url);
	}
	
	public String HttpDeleteHCPContent(URL url) throws Exception {
		if(!getExecute()) return "";
		String result="";
		HttpResponse httpResponse;
		HttpClient mHttpClient = HCPUtils.initHttpClient();
		HttpDelete httpRequest = new HttpDelete(url.toString());
		httpRequest.setHeader(HTTP_AUTH_HEADER, getAuthToken());
		httpResponse = mHttpClient.execute(httpRequest);
		setStatusCode(HttpHCPCatchError(httpRequest, httpResponse));
		InputStream is=httpResponse.getEntity().getContent();
		if(httpResponse.getEntity().getContentLength()>0) {
			//capture results, if they exist
			result=StringHelper.InputStreamToString(is);
			EntityUtils.consume(httpResponse.getEntity());
		}
		return result;
	}
	
	public InputStream HttpGetHCPContentStream(URL url) throws Exception {
		if(!getExecute()) {
			FakeSleep();
			return null;
		}
		ScreenLog.begin("HttpGetHCPContentStream("+url.toString()+")");
		HttpResponse httpResponse;
		HttpClient mHttpClient = HCPUtils.initHttpClient();
		HttpGet httpRequest = new HttpGet(url.toString());
		
		httpRequest.setHeader(HTTP_AUTH_HEADER, getAuthToken());
		httpResponse = mHttpClient.execute(httpRequest);
		setStatusCode(HttpHCPCatchError(httpRequest, httpResponse));
		ScreenLog.end("HttpGetHCPContentStream("+url.toString()+")");
		return httpResponse.getEntity().getContent();
	}

	public InputStream HttpGetContentStream(URL url) throws Exception {
		if(!getExecute()) {
			FakeSleep();
			return null;
		}

		HttpResponse httpResponse;
		HttpClient mHttpClient = HCPUtils.initHttpClient();
		HttpGet httpRequest = new HttpGet(url.toString());
		
		//httpRequest.setHeader(HTTP_AUTH_HEADER, getAuthToken());
		httpResponse = mHttpClient.execute(httpRequest);
		setStatusCode(HttpHCPCatchError(httpRequest, httpResponse));
		return httpResponse.getEntity().getContent();
	}
	
	public static void InputStreamToFile(InputStream is, File outFile) throws IOException {
		//read from file src
		
		if(outFile.exists()) return;
		
		FileOutputStream fos=new FileOutputStream(outFile);
		
		//write to response.writer
		//(new InputStreamEntity( fis.getInputStream(),
		
		int num;
		byte buf[] = new byte[4096];
		while((num = is.read(buf)) != -1){ 
			fos.write(buf, 0, num);
		}
		is.close();
		fos.close();
	}

    /**
     * Copy the given byte range of the given input to the given output.
     * @param input The input to copy the given range to the given output for.
     * @param output The output to copy the given range from the given input for.
     * @param start Start of the byte range.
     * @param length Length of the byte range.
     * @throws IOException If something fails at I/O level.
     */
	public static void FileToOutputStream(RandomAccessFile input, OutputStream output, long start, long length)
	        throws IOException
	    {
	        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
	        int read;

	        if (input.length() == length) {
	            // Write full range.
	            while ((read = input.read(buffer)) > 0) {
	                output.write(buffer, 0, read);
	            }
	        } else {
	            // Write partial range.
	            input.seek(start);
	            long toRead = length;

	            while ((read = input.read(buffer)) > 0) {
	                if ((toRead -= read) > 0) {
	                    output.write(buffer, 0, read);
	                } else {
	                    output.write(buffer, 0, (int) toRead + read);
	                    break;
	                }
	            }
	        }
	}
	
	public void HttpGetHCPContent(URL url,File outFile) throws FileNotFoundException, Exception {
		HCPClient.InputStreamToFile(HttpGetContentStream(url),outFile);
	}

	//move exception out
	public String GetHCPStatus()  {
		try {
			HttpGetHCPHeader(getRootpath());
			if(getHeaders().containsKey("X-HCP-Type")) return "working";
			return "misconfigured";
		} catch (Exception e) {
			ScreenLog.out("HCP is down!!");
		}
		return "HCP is down or unreachable";
	}
	
	public String GetHCPVersion() throws Exception {
		if(!getExecute()) return "6";
		return HttpGetHCPHeader(getRootpath(),"X-HCP-SoftwareVersion");
	}
	
	public Boolean getShouldDumpHTTPHeaders() {
		return shouldDumpHTTPHeaders;
	}

	public void setShouldDumpHTTPHeaders(Boolean shouldDumpHTTPHeaders) {
		this.shouldDumpHTTPHeaders = shouldDumpHTTPHeaders;
	}

	public URL getRootpath() throws IOException {
		return CometProperties.getInstance().getDestinationRootPath();
	}
	
	public void setRootpath(URL rootpath) {
		ScreenLog.out("WARNING: HCPClient::setRootpath("+rootpath+") was called; this function is deprecated");
		
	}

	public int getMaxSearchResults() {
		return MaxSearchResults;
	}

	public void setMaxSearchResults(int maxSearchResults) {
		MaxSearchResults = maxSearchResults;
	}

	public URL getMQESearchURL() {
		return MQESearchURL;
	}

	public void setMQESearchURL(URL mQESearchURL) {
		MQESearchURL = mQESearchURL;
	}

	public Map<String,String> getHeaders() {
		return Headers;
	}

	public void setHeaders(Map<String,String> headers) {
		Headers = headers;
	}

	public String getCombinedAnnotationName() {
		return CombinedAnnotationName;
	}

	public void setCombinedAnnotationName(String combinedAnnotationName) {
		CombinedAnnotationName = combinedAnnotationName;
	}

	public Boolean getExecute() {
		return Execute;
	}

	public void setExecute(Boolean execute) {
		Execute = execute;
	}
}