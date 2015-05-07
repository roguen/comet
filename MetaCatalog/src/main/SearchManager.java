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
//Package: COMET Web Application
//Author: Chris Delezenski <chris.delezenski@hdsfed.com>
//Compilation Date: 2015-05-06
//License: Apache License, Version 2.0
//Version: 1.21.0
//(RPM) Release: 1
//SVN: r554

package main;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.hds.hcp.apihelpers.HCPUtils;
import com.hdsfed.cometapi.AnnotationHelper;
import com.hdsfed.cometapi.CometProperties;
import com.hdsfed.cometapi.ExtendedLogger;
import com.hdsfed.cometapi.HCPClient;
import com.hdsfed.cometapi.ServletHelper;
import com.hdsfed.cometapi.StringHelper;
import com.hdsfed.cometapi.XMLHelper;

//TODO: Eventually merge search and alternate_search together
//      Merge HCPInterface into this
//		FIXME: unified search should respect "musthave" "mayhave" "mustnothave" paradigm 
//		Make specialty queries (eg: date search, coordinate search etc) be more generic and move to separate
//		"query formulation" class
//		Need to make sure we are thread safe
//		Need to collect statistics
//		consider more query formations around retention classes and system metadata
public class SearchManager {
	private static ExtendedLogger ScreenLog = new ExtendedLogger(SearchManager.class.getPackage().getName());
	static String prefixDir="";
	
	//we allow MQE, HDDS, Direct or some combination
	// it is possible to skip MQE entirely and do just HDDS (content only) or Direct
	private Boolean MQESearch;
	
	static class Roles {
		
		
	    @JsonProperty("role")
		public String role;
	    
	    @JsonProperty("show_caveats")
	    public boolean show_caveats;
	    
	    @JsonProperty("classification")
		public String classification;
	    
	    @JsonProperty("admin")
	    public boolean admin;

	    @JsonProperty("search_criteria")
		public String search_criteria;

	    @JsonProperty("ingest")
	    public boolean ingest;

	    @JsonProperty("notes")
	    public boolean notes;

	    @JsonProperty("fallback")
		public String fallback;
	    
	    @JsonProperty("lhspf")
		public int lhspf;
	    
	    @JsonProperty("rhspf")
		public int rhspf;
	    
	    
	    @Override
	    public String toString() {
	        return "{ \"role\" : \""+role+"\"+" +
	        		"," +
	        		"\"show_caveats\" : "+new Boolean(show_caveats).toString() + 
	        		"," +
	        		"\"classification\" : \""+classification+"\""+
	        		","+
	        		"\"admin\" : "+new Boolean(admin).toString() + 
	        		"," +
	        		"\"search_criteria\" : \""+search_criteria+"\"" +
	        		"," +
	        		"\"ingest\" : "+new Boolean(ingest).toString() + 
	        		"," +
	        		"\"notes\" : "+new Boolean(notes).toString() + 
	        		"," +
	        		"\"fallback\" : \""+fallback+"\"" +
	        		"," +
	        		"\"lhspf\" : "+new Integer(lhspf).toString() + 
	        		"," +
	        		"\"rhspf\" : "+new Integer(rhspf).toString() + 
	        		  		
	        		
	        		" }";
	    }
	    
	};
	
	public enum SearchEngine {
		MQE(0,"mqe","HCP Metadata Query Engine",-1),
		HDDS(1,"hdds","Hitachi Data Discovery Suite",-1),
		DIRECT(2,"direct","HCP Directory Listing",-1);
		
		private final int index;
		private String label;
		private String desc;
		private int ql_index;
	    private SearchEngine(int index, String label, String desc, int ql_index) {
	        this.index = index;
	        this.label = label;
	        this.desc = desc;
	        this.ql_index=ql_index;
	    }

	    public int getIndex() {
	        return this.index;
	    }
	    public String getLabel() {
	        return this.label;
	    }
	    public String getDesc() {
	        return this.desc;
	    }
	    public int getQLIndex() {
	    	return this.ql_index;
	    }
	    public void setQLIndex(int ql_index) {
	    	this.ql_index=ql_index;
	    }
	};

	public enum Triggers {
			HDDS("content:","+(HDDSContent:","content"),
			DIRECT("/","+(absolutePath:","direct");
			
		    private String trigger;
			private String prefix;
			private String parameter;

			private Triggers(String trigger, String prefix, String p) {
		        this.setTrigger(trigger);
		        this.setPrefix(prefix);
		        this.setParameter(parameter);
		    }

			public String getTrigger() {
				return trigger;
			}

			public void setTrigger(String trigger) {
				this.trigger = trigger;
			}
			
			public String getPrefix() {
				return prefix;
			}

			public void setPrefix(String prefix) {
				this.prefix = prefix;
			}
			
			public String getParameter() {
				return parameter;
			}

			public void setParameter(String parameter) {
				this.parameter = parameter;
			}
	}
	
	public static SearchEngine GetSearchEngine(String request) {
		if(request.toLowerCase().contains("neo") || request.toLowerCase().contains("hdds")) {
			return SearchEngine.HDDS;
		} else if(request.toLowerCase().contains("direct")) {
			return SearchEngine.DIRECT;
		} 
		return SearchEngine.MQE;
	}

	public static SearchEngine GetSearchEngine(HttpServletRequest request) {
		String s="";
		if(request.getParameter("searchEngine")==null) {
			s="-1";
		} else {
			s=request.getParameter("searchEngine").toString();
		}
		return GetSearchEngine(s);
	}

	private String FormDateQueryFromParameters(Map<String,String> parameters) {
		ScreenLog.begin("FormDateQueryFromParameters()");	
		String query="";

		//if no date parameters exist, quit early
		if(!parameters.containsKey("start_date") && !parameters.containsKey("end_date")) {
			return query;
		}
		setMQESearch(true);
		CometProperties mprop=CometProperties.getInstance();
		
		String[] datetagGroupFacets=mprop.getDateList().split(";");
		
		ScreenLog.out("\tfacets detected: "+datetagGroupFacets.length);	
		
		//ScreenLog.out("starting date is "+start_date);
		//ScreenLog.out("ending date is "+end_date);
		
		//assume that start_date is valid and give me everything from start date onwards
		//finally, replace datetag with CometProperties revealing split CSV of dates to search
	
		//bonus: integrate time search as well?
		boolean isepoch = false;
		for(int j=0; j<datetagGroupFacets.length; j++) {
			String[] datetagGroup;
			if(datetagGroupFacets[j].length()>1) {
				datetagGroup=datetagGroupFacets[j].split(",");
				for(int i=0; i<datetagGroup.length; i++) {
					isepoch=GetEpoch(datetagGroup[i]);
					if(parameters.containsKey("start_date")) {
						if(!query.trim().equals("")) query+=" ";
						query+=QueryMode(j)+FormDateQuery(SearchEngine.MQE, datetagGroup[i], parameters.get("start_date"), true, isepoch);
					}
					if(parameters.containsKey("end_date")) {
						if(!query.trim().equals("")) query+=" ";
						query+=QueryMode(j)+FormDateQuery(SearchEngine.MQE, datetagGroup[i], parameters.get("end_date"), false, isepoch);
					} //end if end_date
				} //end for i
			}
		} //end for j
//		ScreenLog.end("FormDateQueryFromParameters()");	
	
		return query;
	}
	
	// Search engine type (NEO/MQE), datetag (MQE content property or NEO xml path), date param, bool fromto (true if date is starting point), bool isepoch (convert search term to epoch first) 
	public static String FormDateQuery(SearchEngine se, String datetag, String param, boolean fromto, boolean isepoch) {
		ScreenLog.begin("FormDateQuery...");
		String query="";
		if(!param.contains("T"))  {
			if(isepoch) {
				try {
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
					Date date = df.parse(param);
				 	long epoch = date.getTime();
		  			param=Long.toString(epoch/1000);
				} catch (ParseException e) { ScreenLog.ExceptionOutputHandler(e); }
			} else {
				param+="T00:00:00Z";
			}
		}
	
		switch(se) {
			case MQE:
				if(fromto) {
					query="("+datetag+":{"+param+" TO *])";
				} else {
					query="("+datetag+":[* TO "+param+"})";
				}
			break;
			case HDDS:
				query="CustomMetadata:XML("+datetag+"=RANGE("+param+" TO *)) "; 
				ScreenLog.out("Search engine not yet supported for date searches in metadata");
			break;
			default:
				return "";
		}
		ScreenLog.end("FormDateQuery...");
		return query;
	}

	private String FormQuadrantQueryFromParameters(Map<String,String> parameters) {
		String query="";
		if(!parameters.containsKey("south") || !parameters.containsKey("west") || 
		   !parameters.containsKey("north") || !parameters.containsKey("east")) {
			return query;
		}
		setMQESearch(true);
		return FormQuadrantQuery(SearchEngine.MQE,parameters.get("south"),
			parameters.get("north"),
			parameters.get("west"),
			parameters.get("east"));
	}
	
	
	public static String FormQuadrantQuery(SearchEngine se, String s, String n, String w, String e) {
		ScreenLog.begin("FormQuandrantQuery");
		
		String query="";
		switch(se) {
		case MQE:
			query = "+(cornerY:{"+s+" TO *]) +(cornerY:[* TO "+n+"}) +(cornerX:{"+w+" TO *]) +(cornerX:[* TO "+e+"})";
		break;
		case HDDS:
			query="CustomMetadata:XML(/GDALInfo/NEOCornerCoordinates/Corner/Y=RANGE("+s+" TO "+n+")) " +
					"AND CustomMetadata:XML(/GDALInfo/NEOCornerCoordinates/Corner/X=RANGE("+w+" TO "+e+"))";

		break;	
		default:
			query="invalid";
		break;
		}
		
		ScreenLog.end("FormQuandrantQuery = returning query = "+query);
		return query;
	}
	
	private static String GetURLs(Node node, boolean comma_on_next) throws Exception {
		ScreenLog.begin("GETUrls");
		if(null==node) {
			return "";
		}
		URL inner_url=null;
		
		String results="", retval="", temp="";
		if(node.getNodeType()==Node.CDATA_SECTION_NODE && !node.getNodeValue().trim().equals("") && node.getNodeValue().contains("https")) {
			if(null==node.getNodeValue()) {
				return "";
			}
			inner_url=new URL(AnnotationHelper.URIEncoder(node.getNodeValue()));
		 	ScreenLog.out("(GetURLS) inner_url to send to filepath2output: "+inner_url);
			temp=HCPInterface.FilePath2Output(inner_url);
			
			if(!temp.trim().equals("")) {
		 	//	if(comma_on_next) retval+=",\n"+temp;
		 	//	else {
		 				//first entry!
		 	//			comma_on_next=true;
		 				retval=temp;
		 	//	}
		 		return retval;
		 	}
		//	if(!temp.trim().equals("")) return temp+",\n";
		}
		
		if(node.getChildNodes().getLength()>0) {
			NodeList nl=node.getChildNodes();
			for(int i=0; i<nl.getLength(); i++) {
				retval=GetURLs(nl.item(i),comma_on_next);
				if(!retval.trim().equals("") && !results.contains(retval)) results+=retval;
				
			}
		}
		if(!results.isEmpty()) ScreenLog.out("returning results:::"+results);
		
		//if(comma_on_next && !results.trim().equals("")) return ",\n"+results;
		ScreenLog.end("GETUrls");

		return results;
	}
	
	//TODO: consider moving result parsers out	
	public static String ParseResults(String search_result, SearchEngine se) {
		String returnthis="";
		switch(se) {
		case DIRECT:
			returnthis=ParseResults_MQE(search_result,true);
		break;
		case MQE:
			returnthis=ParseResults_MQE(search_result,false);
		break;
		case HDDS:
			returnthis=ParseResults_NEO(search_result);
		break;
		default:
		}

		
		ScreenLog.out("\n\n\nreturnthis(before)=\""+returnthis+"\"\n\n\n");
		
		String array[]=returnthis.split("\\n");
	

//		List<String> uniqueList = new ArrayList<String>();
		
		StringBuilder sb = new StringBuilder();
		
		for(int i=0; i<array.length; i++) {
			//if(!uniqueList.contains(array[i]))
			//uniqueList.add(array[i]);
			if(!sb.toString().contains(array[i])) {
				sb.append(array[i]);
				sb.append("\n");
			}
		}
		
		returnthis=sb.toString();
		ScreenLog.out("\n\n\nreturnthis(after)=\""+returnthis+"\"\n\n\n");
		
	    return returnthis;
	}
	
	public static String ParseResults_NEO(String search_result) {
		ScreenLog.begin("ParseResults_NEO("+search_result+")");
	
		String retval="";
		String temp="";
		temp=search_result.replaceAll("http:","https:");
		search_result=temp;
		CometProperties mprop;
		mprop=CometProperties.getInstance();
		boolean comma_on_next=false;
		try {
			temp=search_result.replaceAll(mprop.getNEOWebApiResponseDTD().toString(),"http://localhost"+CometProperties.getInstance().getWebAppPath()+"WebApiResponse.dtd");
			search_result=temp;
			temp="";
		    NodeList responseNodes = XMLHelper.StringToDoc(search_result).getElementsByTagName("response");
		    Element responseElement = (Element) responseNodes.item(0);

		    NodeList responseNodes2 = responseElement.getChildNodes();
		    Element tempe=(Element)responseNodes2.item(responseNodes2.getLength()-1);
		    NodeList dataNodes=tempe.getChildNodes();
		    String new_search_results="";
		    for (int i=0; i<dataNodes.getLength(); i++) {
			      CharacterData child = (CharacterData)dataNodes.item(i);
			      new_search_results+=child.getData();
		    }
		    Node node = XMLHelper.StringToDoc(new_search_results).getFirstChild().getFirstChild();
			NodeList nodeList = node.getChildNodes();
			for (int i=0; i<nodeList.getLength(); i++) {
				temp=GetURLs(nodeList.item(i),comma_on_next);
				if(!temp.trim().equals("")) {
					ScreenLog.out("adding to retval = \""+temp+"\"");
					if(comma_on_next) {
						ScreenLog.out("adding comma before payload: "+temp);
						retval+=",";
					}
					else comma_on_next=true;
					retval+=temp;
				}
			}
		} catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }

		ScreenLog.end("ParseResults_NEO("+search_result+")");
		return retval;
	}
	
	
	public static String ParseResults_MQE(String search_result, Boolean fake) {
		ScreenLog.begin("ParseResults_MQE("+search_result.length()+","+fake.toString()+")");
		String retval="", temp="";
		
		Boolean comma_on_next=false;
		try {
			String parsertag="object";
			URL inner_url=null;
			if(fake) {
					parsertag="entry";
			}
	
			NodeList url_list = null;
			int n=0;
			
			if(!search_result.isEmpty()) {
					url_list=XMLHelper.StringToDoc(search_result).getElementsByTagName(parsertag);
					n=url_list.getLength();
			}
			
			
			for (int i = 0; i <n ; i++) {
				ScreenLog.out("looping  through "+(i+1)+" of "+url_list.getLength()+" urls.");
				Node urlNode = url_list.item(i);
				if (urlNode.getNodeType() == Node.ELEMENT_NODE) {
				 	Element eElement = (Element) urlNode;
				 	
				 	if(fake) {
				 		//do something
				 		inner_url=new URL(prefixDir+"/"+eElement.getAttribute("urlName").replaceAll("\\[", "%5B").replaceAll("\\]", "%5D"));
				 	} else {
				 		inner_url=new URL(eElement.getAttribute("urlName").replaceFirst("http:", "https:").replaceAll("\\[", "%5B").replaceAll("\\]", "%5D"));
				 	}
				 	ScreenLog.out("\thanding off inner_url=\""+inner_url+"\" to FilePath2Output() function");
			 		temp=HCPInterface.FilePath2Output(inner_url);
				 	if(!temp.trim().equals("")) {
				 		if(comma_on_next) retval+=",\n"+temp;
				 		else {
				 				//first entry!
				 				comma_on_next=true;
				 				retval=temp;
				 		}
				 	}
	
				}
			}
		} catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }
	
		ScreenLog.end("ParseResults_MQE("+search_result.length()+")");
		return retval+"\n";
	}

	public List<String> ParseResultsToList_MQE(String search_result) throws ParserConfigurationException, SAXException, IOException {
		ScreenLog.begin("ParseResultsToList()");
		List<String> urllist=new ArrayList<String>();
		
		if(search_result.isEmpty()) return urllist;
		
		
		
		NodeList url_list = XMLHelper.StringToDoc(search_result).getElementsByTagName("object");
		String temp="";
		for (int i = 0; i < url_list.getLength(); i++) {
			Node urlNode = url_list.item(i);
			if (urlNode.getNodeType() == Node.ELEMENT_NODE) {
			 	Element eElement = (Element) urlNode;
			 	temp=eElement.getAttribute("urlName").replaceFirst("http:", "https:");
			 	if(!urllist.contains(temp)) urllist.add(temp);
			}
		}
		ScreenLog.end("ParseResultsToList()");
		return urllist;
	}

	public static void DoSearchInterface(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Pragma", "no-cache");
		SearchManager sm=new SearchManager();
		PrintWriter out;
		Map<String, String> parameters=ServletHelper.GetParameterMap(request);
		ScreenLog.out(parameters);
		String content="";;
		try {
			out = response.getWriter();
			content=sm.DoSearch(parameters);
			out.println(content);
		} catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }
	}

	//TODO: remove legacy references to pre-Unified search
	// merge with alternative_search
	private String DoSearch(Map<String, String> parameters)	{
		ScreenLog.begin("DoSearch::: BEGIN");
		String content="";
		String sep="";
		try {
			String query="";
			setMQESearch(false);
			if(!parameters.containsKey("query")) ScreenLog.out("missing query parameter");
			else ScreenLog.out("initial query is "+parameters.get("query"));
			ScreenLog.out(parameters,"parameter list");
			ScreenLog.out("MQESearch="+getMQESearch().toString());
			if(parameters.containsKey("query")) 
				setMQESearch(!parameters.get("query").startsWith(Triggers.DIRECT.getTrigger()) &&
							 !parameters.get("query").toLowerCase().startsWith(Triggers.HDDS.getTrigger()) && parameters.get("query").contains("(") && parameters.get("query").contains(")"));
			
			if(parameters.containsKey("query") && !parameters.get("query").startsWith(Triggers.DIRECT.getTrigger()) &&
							 !parameters.get("query").toLowerCase().startsWith(Triggers.HDDS.getTrigger()) && !getMQESearch()) {
				query=parameters.get("query");
				parameters.put("query", "content:\""+query+"\"");
				
				
				ScreenLog.out("\n\nlooking for query="+parameters.get("query")+"\n\n\n");
				ScreenLog.out(parameters);
				
				
				query=FormHDDSQueryFromParameters(parameters);
			}
			else {
			query+=FormDirectQueryFromParameters(parameters);
			if(!query.trim().equals("") && !query.endsWith(" ")) sep=" ";
			else sep="";
			query+=sep+FormHDDSQueryFromParameters(parameters);
			if(!query.trim().equals("") && !query.endsWith(" ")) sep=" ";
			else sep="";
			query+=sep+FormDateQueryFromParameters(parameters);
			if(!query.trim().equals("") && !query.endsWith(" ")) sep=" ";
			else sep="";
			
			ScreenLog.out("\n\nbefore form quandrant, query="+query);
			
			query+=sep+FormQuadrantQueryFromParameters(parameters);

			ScreenLog.out("\n\nafter form quandrant, query="+query);

			if(!query.trim().equals("")) sep=" ";
			if(parameters.containsKey("query") && !parameters.get("query").equals("") && getMQESearch()) {
				if(!query.trim().equals("") && !query.endsWith(" ")) sep=" ";
				else sep="";
				query+=sep+parameters.get("query");
				if(!query.trim().equals("") && !query.endsWith(" ")) sep=" ";
				else sep="";
			}
			}
			if(getMQESearch()) {
				query+=sep+FormConstraintsFromParameters(parameters);
			} else {
				ScreenLog.out("\tno constraints added; either query was null or mqe is turned off: "+getMQESearch().toString());
			}
			//update query parameter
			parameters.put("query", query);
			
			content=DoAlternateSearch(parameters);
			
			ScreenLog.end("DoSearch(query="+query+",content length="+content.length()+")");
//			ScreenLog.out("=========================================");
//			ScreenLog.out(content);
//			ScreenLog.out("=========================================");
			
		} catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }
		
		//reset config state
		CometProperties.setConfigMode(false);
		
		return content;
	}

	private String FormHDDSQueryFromParameters(Map<String, String> parameters) {
		//missing query parameter, or query parameter not valid return nothing
		if(parameters.containsKey(Triggers.HDDS.getParameter()))
			return Triggers.HDDS.getPrefix()+parameters.containsKey(Triggers.HDDS.getParameter())+")";
		if(!parameters.containsKey("query") || !parameters.get("query").toLowerCase().startsWith(Triggers.HDDS.getTrigger())) return "";
		String temp=parameters.get("query");
		return Triggers.HDDS.getPrefix()+temp.substring(Triggers.HDDS.getTrigger().length(), temp.length())+")";
	}

	private String FormDirectQueryFromParameters(Map<String, String> parameters) {
		if(parameters.containsKey(Triggers.DIRECT.getParameter()))
			return Triggers.DIRECT.getPrefix()+parameters.get(Triggers.DIRECT.getParameter())+")";
		if(!parameters.containsKey("query") && !parameters.containsKey(Triggers.DIRECT.getParameter())) return "";
		String temp=parameters.get("query");
		if(temp.length()>0 && temp.charAt(0)=='/') {
			return Triggers.DIRECT.getPrefix()+temp+")";
		}
		return "";
	}
	
	private String FormConstraintsFromParameters(Map<String, String> parameters) throws IOException {
		ScreenLog.begin("FormConstraintsFromParameters(...parameters...)");
		CometProperties mprops=CometProperties.getInstance();
		//ignore thumbnails as they tend to not have metadata
		//metadata pull code should be more resistant to crashes
		String query = "-(objectPath:thumb)";
		String r=parameters.get("only_geo");
		if(null!=r && r.equals("true")) query+=" +(customMetadataAnnotation:geo)";
				
		if(!mprops.getSearchConstraints().trim().equals("")) query+=" "+mprops.getSearchConstraints();
		
		String role_search_constraints;
		if(!parameters.containsKey("role")) { 
			ScreenLog.out("\tmissing parameter: role");
		}
	
		role_search_constraints = getRoleSearchConstraints(parameters.get("role"));
		if(!role_search_constraints.equals("")) query+=" "+role_search_constraints;
		ScreenLog.end("FormConstraintsFromParameters(...query="+query+")");
		return query;
	}

	public String getRoleSearchConstraints(String role) throws IOException {
		ScreenLog.begin("getRoleSearchConstraints("+role+")");
		ObjectMapper mapper = new ObjectMapper();
		String role_json_content=new String(Files.readAllBytes(Paths.get("/opt/COMETDist/roles.json")));
		List<Roles> roleList =null;
		roleList = mapper.readValue(role_json_content, new TypeReference<List<Roles>>() { });
		int id=getRoleId(roleList, role);
		if(id==-1) return "";
		ScreenLog.end("getRoleSearchConstraints("+role+")=\""+roleList.get(id).search_criteria+"\"");
		return roleList.get(id).search_criteria;
	}
	
	private int getRoleId(List<Roles> roleList, String tgt_role) {
		for(int i=0; i<roleList.size(); i++) {
			if(roleList.get(i).role.equals(tgt_role)) return i;
		}
		return -1;
	}

	Boolean ThreadsAlive(Thread []t) {
		for(int i=0; i<t.length; i++) if(t[i].isAlive()) return true;
		return false;
	}
	
	private String DoAlternateSearch(Map<String, String> parameters) throws IOException {
		ScreenLog.begin("DoAltSearch(...)");
		String putback=")";
		String sep="\\"+putback+"\\ ";
		CometProperties mprops=CometProperties.getInstance();
	
		ScreenLog.out("want to search for \""+parameters.get("query")+" \"");
		
		//parse query parameter into list
		List<String> queryList=StringHelper.CSVtoSortedUniqueList(parameters.get("query")+" ",sep, putback);
		
		ScreenLog.out("size = "+queryList.size());
		
		SearchEngine.HDDS.setQLIndex(-1);
		SearchEngine.DIRECT.setQLIndex(-1);
		
		ScreenLog.out("qlindex (HDDS) = "+SearchEngine.HDDS.getQLIndex());
		ScreenLog.out("qlindex (DIRECT) = "+SearchEngine.DIRECT.getQLIndex());
		
		for(int i=0; i<queryList.size(); i++) {
			ScreenLog.out("i="+i+" qa["+i+"]=\""+queryList.get(i)+"\"");
			if(queryList.get(i).toLowerCase().contains("hddscontent:")) {
				ScreenLog.out("found hdds query at index="+i);
				SearchEngine.HDDS.setQLIndex(i);
			}
			else if(queryList.get(i).toLowerCase().contains("absolutepath:")) {
				ScreenLog.out("found direct query at index="+i);
				SearchEngine.DIRECT.setQLIndex(i);
			}
		}
		
		String temp="";
		SearchRunnable[] sr=new SearchRunnable[SearchEngine.values().length];
	
		for(SearchEngine ses: SearchEngine.values()) {
			temp="";
			if(ses!=SearchEngine.MQE) {
				if(ses.getQLIndex()==-1) {
					parameters.remove(ses.getLabel()+"_query");
				} else {	
					//acquire special query
					temp=queryList.get(ses.getQLIndex());
					//remove from list
					queryList.remove(ses.getQLIndex());
					//show it
					ScreenLog.out(ses.getLabel()+"_query="+temp);
					//chop it, so that "+(special:content)" becomes "content"
					temp=temp.substring(temp.indexOf(":")+1, temp.length()-1);
					ScreenLog.out(ses.getLabel()+"_query(chopped)="+temp);
					//replace tenant120 with actual tenant from props file
					
					if(ses==SearchEngine.HDDS) temp="Tenant:"+mprops.getDestinationHCPTenant()+" AND Content:"+temp;
					parameters.put(ses.getLabel()+"_query", temp);
					temp="";
				} //end else indices
			} // end if ses!=SearchEngine.MQE
		} //end for ses loop
	
		String content="";
		parameters.put("mqe_query", StringHelper.ListtoCSV(queryList, " ").trim());
	
		CometProperties.setConfigMode(parameters.containsKey("config"));
		
		ScreenLog.out("config mode = "+CometProperties.getConfigMode());
		ScreenLog.out("\ttherefore, destination root = "+mprops.getDestinationRootPath());
		
		Thread[] t=new Thread[3];
	
		//TODO: need to get the intersection
		for(SearchEngine ses: SearchEngine.values()) {
			if(parameters.containsKey(ses.getLabel()+"_query")) {
				content=parameters.get(ses.getLabel()+"_query");
			} else content="";
	
			sr[ses.getIndex()]=new SearchRunnable(content,ses,parameters.containsKey("config"));
			t[ses.getIndex()]=new Thread(sr[ses.getIndex()]);
			t[ses.getIndex()].start();
		}	
		ScreenLog.out("waiting for threads...");
		while(ThreadsAlive(t)) {
			try { Thread.sleep(1000); } catch (Exception ignored) { }
		}
		
		ScreenLog.end("DoAltSearch(...)");
		return "["+GetRunnableContent(sr)+"]";
	}

	private String GetRunnableContent(SearchRunnable[] sr) {
		String content="";
		boolean comma_comes_next=false;
		String temp_content="";
		
		for(int i=0; i<sr.length; i++) {
			if(!sr[i].getContent().equals("")) {
				if(!comma_comes_next) comma_comes_next=true;
				else content+=","; 
				
				temp_content=sr[i].getContent();
				if(!temp_content.trim().equals("")) {
					ScreenLog.out("adding content from thread "+i+" = "+temp_content);	
					content+=temp_content;
				}
				
				//short-circuit our comma because there is no preceding content
				if(comma_comes_next && content.trim().equals("")) comma_comes_next=false;
			}
		}
		
		ScreenLog.out("about to return content, with length of: "+content.length());
		
		String temp=content.trim();
		content=temp;
		ScreenLog.out("about to return content, with length of: "+content.length());
		
		if(content.endsWith(",")) {
			ScreenLog.out("\tcomma detected, return content minus 1");
			ScreenLog.out("\tabout to return content, with length of: "+content.substring(0,content.length()-1).length());
			
			return content.substring(0,content.length()-1);
		}
		
		return content;
	}
	
	private String QueryMode(int j) {
		switch(j) {
		case 0:
				return "+";
		case 1: 
				return "";
		}
		return "-";
	}
	
	//TODO: move to other class
	private boolean GetEpoch(String s) {
		return (s.toLowerCase().contains("ingesttime") || s.toLowerCase().contains("accesstime") || s.toLowerCase().contains("changetime") || s.toLowerCase().contains("epoch"));
	}

//TODO: break apart and consider moving to HCPClient
	public static String searchFiles(SearchEngine se, String query, Boolean config_mode) {
		ScreenLog.begin("SearchManager:::searchFiles");
	
		try {
			String search_result = "";
	//		String command_name = "SearchFiles";
			
			CometProperties mprop=CometProperties.getInstance();
			switch(se) {
				case DIRECT:
				case MQE:
					HCPClient client=new HCPClient(mprop);
				
					ScreenLog.out("query="+query);
					
					if(se==SearchEngine.DIRECT) {
						
						//first, check to see if the object is a directory
						client.HttpGetHCPHeader(AnnotationHelper.PathToURL(mprop.getDestinationRootPath(), query));
											
						if(!client.getHeaders().isEmpty() && client.getHeaders().containsKey("X-HCP-Type") && client.getHeaders().get("X-HCP-Type").equals("directory")) {
							//query is actually a path here
							search_result=client.HttpGetHCPContent(AnnotationHelper.PathToURL(mprop.getDestinationRootPath(), query));
						} else {
//							search_result="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<directory><entry urlName=\""+query.substring(query.lastIndexOf("/")+1,query.length())+"\" /></directory>";
							search_result="";
							//correct query for prefixDir below
							query=query.substring(0,query.lastIndexOf("/"));
						}
						
						if(query.equals("/"))  prefixDir=mprop.getDestinationRootPath().toString();
						else {
							
							CometProperties.setConfigMode(config_mode);
							
							prefixDir=mprop.getDestinationRootPath()+query;
						}
					} else {
						search_result=client.QueryHCP(HCPClient.QueryBuilder(query, 0, mprop.getMaxSearchResults()));
					}
					//HCPMQESearchEngine hcpmqe_webapi = new HCPMQESearchEngine();
					//search_result = hcpmqe_webapi.executeCommand(query);
				break;
				case HDDS:
					HttpClient mHttpClient;
					
					// Get an HTTP Client setup for sample usage.
					// ugly hack: wrap the client to ignore bad cert coming out of HDDS
					mHttpClient = HCPUtils.initHttpClient();	
					HttpPost httpRequest = new HttpPost(mprop.getNEOSearchPath().toURI());
					query=query.replaceAll("\\\\","");
						
					List<NameValuePair> params = new ArrayList<NameValuePair>(5);
					params.add(new BasicNameValuePair("#c", "Search"));
					params.add(new BasicNameValuePair("-u", mprop.getDestinationUserName()));
					params.add(new BasicNameValuePair("-p", mprop.getHDDSPassword()));
					params.add(new BasicNameValuePair("-en",""));
					params.add(new BasicNameValuePair("MaxHits", String.valueOf(mprop.getMaxSearchResults())));
					params.add(new BasicNameValuePair("QueryType", "structured"));
					params.add(new BasicNameValuePair("QueryString", query));
			
					httpRequest.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
						
					//httpRequest.setEntity((HttpEntity) new StringEntity(mQuery));
					HttpResponse httpResponse = mHttpClient.execute(httpRequest);
					
					ScreenLog.out("status code is "+httpResponse.getStatusLine().getStatusCode());
					
					if (2 != (int)(httpResponse.getStatusLine().getStatusCode() / 100)) {
						// Clean up after ourselves and release the HTTP connection to the
						// connection manager.
						EntityUtils.consume(httpResponse.getEntity());
						throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(),"Unexpected status returned from " + httpRequest.getMethod() + " ("	+ httpResponse.getStatusLine().getStatusCode() + ": "+ httpResponse.getStatusLine().getReasonPhrase() + ")");
					}
					search_result=StringHelper.InputStreamToString(httpResponse.getEntity().getContent()).replace("\n", "");
				break;
				default:
				
				break;
			}
			ScreenLog.end("SearchManager:::searchFiles (successful)");
			return search_result;                                                                         
		} catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }
		ScreenLog.end("SearchManager:::searchFiles (unsuccessful)");
		return "-1";
	} //end of function

	public Boolean getMQESearch() {
		return this.MQESearch;
	}
	
	public void setMQESearch(Boolean MQESearch) {
		this.MQESearch = MQESearch;
	}
		

};	// end of class	

