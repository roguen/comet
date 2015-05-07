package com.hdsfed.cometapi;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;

import com.hds.hcp.apihelpers.HCPUtils;

public class CometAction {
	private static ExtendedLogger ScreenLog = new ExtendedLogger(CometAction.class.getName());

	private static CometAction instance;
	private HttpClient client;
	public static CometAction getInstance() {
		if (instance == null) instance = new CometAction();
		return instance;
	}
	/*static private synchronized int genericDBActionOverHttpGET(String args) throws Exception {
		if(!CometProperties.getInstance().getIngestorHeartbeatEnabled() || CometProperties.isHeartBeatTooSoon()) return 200;
		ScreenLog.begin("ThreadTrackerDB::genericDBActionOverHttpGET("+args+")");
		
		HttpResponse httpResponse;
		
		URL url;
		int status=-1;
		HttpClient mHttpClient = HCPUtils.initHttpClient();
		url = new URL("http://localhost/IngestorThreads"+args);
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
	}*/
	
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
	public static String getPartnerCondition(String fqdn_ofpartner) throws Exception {
		ScreenLog.begin("CometAction::getPartnerCondition("+fqdn_ofpartner+")");
		//CometAction instance=getInstance();
		
		if(fqdn_ofpartner.isEmpty()) throw new Exception("partner COMET not specified");
		
		String condition="connection broken";
		HttpResponse httpResponse;
		
		URL url;
		int status=-1;
		HttpClient mHttpClient = HCPUtils.initHttpClient();
		url = new URL(CometProperties.getInstance().httpProtocol()+"://"+fqdn_ofpartner+CometProperties.getInstance().getWebAppPath()+"Admin?action=hcpcheck");
		HttpGet httpRequest = new HttpGet(url.toString());
		
		
		httpResponse = mHttpClient.execute(httpRequest);
		status=HttpCatchError(httpRequest, httpResponse);
		if(httpResponse.getEntity().getContentLength()>0) {
			EntityUtils.consume(httpResponse.getEntity());
		}
		
		//should parse output, but for now, we only care that we got a 200-level code back
		if(status/100==2) condition="partner system up";
		
		//forcefully clear it out
		mHttpClient=null;
		httpResponse=null;
		ScreenLog.end("CometAction::getPartnerCondition("+fqdn_ofpartner+") status="+status);
		//return status;
		return condition;
	}

	public static Boolean getPartnerConditionIsWorking(String fqdn_ofpartner) throws Exception {
		return new Boolean(getPartnerCondition(fqdn_ofpartner).equals("partner system up"));
	}
	

}
