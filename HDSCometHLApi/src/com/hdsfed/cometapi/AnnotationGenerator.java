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
//SVN: r551+

package com.hdsfed.cometapi;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;


//Overall TODO: each function should have prereqs and a description
//      each function larger than 5 lines should have "book ends" for logging / tracing purposes



public class AnnotationGenerator {
	public static final String MDM_TITLE = "Title";
	private static ExtendedLogger ScreenLog = new ExtendedLogger(AnnotationGenerator.class.getPackage().getName());

	public static Document PathKMLGenerator(HCPClient client, Map<String,String> parameters) throws Exception {
		if(!parameters.containsKey("path")) return null;
		return PathKMLGenerator(AnnotationHelper.HttpGetAnnotationsMap(client, parameters.get("path"), true),parameters,client.getHeaders());
	}
	
	//prereqs: data, all necessary data should already be obtained in metadataMap, but not resultMap
	// resultmap will be local for now
	
	//return value is a valid (and complete) xml document, which can be changed into a string using XMLHelper
	
	public static Document PathKMLGenerator(Map<String,String> annotations, Map<String,String> parameters, Map<String,String> headers) throws Exception {
		ScreenLog.begin("PathKMLGenerator");
		
		if(!annotations.containsKey("path")) return null;

		MetadataParser mp=MetadataParser.getInstance();
		mp.ParseAnnotations(annotations,parameters.get("path"),headers);
		Map<String, String> metadataMap=MetadataParser.getMetadataMap();
		ScreenLog.out("retrieved metadataMap, with size of "+metadataMap.size());
		//create taglist map

		TaglistMap tm=new TaglistMap(TaglistGenerator(new HCPClient(CometProperties.getInstance()), parameters.get("path")),parameters.get("path"));

		String title=tm.getTitle();
		ResultMap pathMap=null;
		pathMap=KMLHelper.DocumentToResultMap(XMLHelper.StringToDoc(annotations.get("path")));
		ScreenLog.out("finished with DocumentToResultMap(...)");
		if(pathMap.size()==0) {
			if(annotations.containsKey("kml")) {
				return XMLHelper.StringToDoc(annotations.get("kml"));
			}
			return null;
		} 
		
		OutputStream os = new ByteArrayOutputStream();
		XMLStreamWriter serializer=KMLHelper.OpenNewKML(os);

		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "name",title);

		serializer=KMLHelper.CreateLookAt(serializer, metadataMap.get("longitude"),metadataMap.get("latitude"));

		//date stamp from datetime annotation?

		serializer=KMLHelper.CreateStartTimeStamp(serializer,metadataMap);
		serializer=KMLHelper.CreateStyle(serializer,"hereNow",new URL(parameters.get("imageprefix")+"images/blue-triangle.png"));
		serializer=KMLHelper.CreateStyle(serializer,"hereThen",new URL(parameters.get("imageprefix")+"images/gray-triangle.png"));
		
		metadataMap.put("table_width", "1000px");
		metadataMap.put("table_height", "600px");
		parameters.put("Coordinates", tm.get("coordinates"));
		metadataMap.put("description", DescGenerator(tm,parameters));

		ScreenLog.out("description generation complete");
		
		Map<String,String> placemarkMap=new HashMap<String,String>();
		placemarkMap.put("style","#hereNow");
		for(int i=0; i<pathMap.size(); i++) {
			//change placemark parameters for each run
			ScreenLog.out("data from list: lat,long:"+pathMap.get(i,"latitude")+","+pathMap.get(i,"longitude")+" st:"+pathMap.get(i,"starttime"));
			if(pathMap.get(i,"latitude").equals("") && pathMap.get(i,"longitude").equals("") && pathMap.get(i,"starttime").equals("")) {
				ScreenLog.out("\t---->detected dead position, skip it");
				continue;
			}

			pathMap.put(i,"starttime",StringHelper.formatDateTime(pathMap.get(i, "starttime")));

			if(i<pathMap.size()-1) {
					pathMap.put(i, "endtime", StringHelper.formatDateTime(StringHelper.minusMinutes(pathMap.get(i+1,"starttime"),1L)));
			}
			
			metadataMap.put("override_latitude", pathMap.get(i,"latitude"));
			metadataMap.put("override_longitude", pathMap.get(i,"longitude"));
			parameters.put("Title",title+" at position #"+i);
			parameters.put("Coordinates", metadataMap.get("override_latitude")+", "+metadataMap.get("override_longitude"));
			placemarkMap.put("balloon_content", DescGenerator(tm, parameters));
			placemarkMap.put("id","pm"+String.valueOf(i));
			placemarkMap.put("name",title+" at position #"+i);
			placemarkMap.put("latitude",pathMap.get(i,"latitude"));
			placemarkMap.put("longitude",pathMap.get(i,"longitude"));
			placemarkMap.put("startDateTime", pathMap.get(i,"starttime"));
			
			if(pathMap.containsKeyPair(i,"endtime") && pathMap.get(i,"endtime").length()>0) {
				placemarkMap.put("endDateTime", pathMap.get(i,"endtime"));
				ScreenLog.out("\tCreateGenericPlaceMarkPair = endtime=\""+pathMap.get(i,"endtime")+"\" size of endtime="+pathMap.get(i,"endtime").length());
				serializer=KMLHelper.CreateGenericPlaceMarkPair(serializer, placemarkMap);
			} else {
				placemarkMap.remove("endDateTime");
				placemarkMap.put("style","#hereNow");
				ScreenLog.out("\tCreateGenericPlaceMark instead of pair");
				serializer=KMLHelper.CreateGenericPlaceMark(serializer, placemarkMap);
			}
		}
		
		serializer=KMLHelper.CloseNewKML(serializer);
		ScreenLog.end("PathKMLGenerator");
		return XMLHelper.StringToDoc(os.toString());
	}

	private static String getGenericTag(String look_in_annotation, String xml_tag, Map<String, String> annotations) throws ParserConfigurationException, SAXException, IOException {
		String result="";
		if(!annotations.containsKey(look_in_annotation)) return result;
		result=XMLHelper.GetSimpleTagContent(XMLHelper.StringToDoc(annotations.get(look_in_annotation)), xml_tag);
		return result;
	}

	
	private static TaglistMap AppendMp3Content(TaglistMap tm, String mp3annotation) throws ParserConfigurationException, SAXException, IOException {
		ScreenLog.begin("TaglistMap::AppendMp3Content");
		Map<String, String> mp3data=XMLHelper.SimpleDocumentToMap(XMLHelper.StringToDoc(mp3annotation));
		if(mp3data.containsKey("Title")) tm.AppendtoEnd("Title", mp3data.get("Title"));
		if(mp3data.containsKey("Album")) tm.AppendtoEnd("Album", mp3data.get("Album"));
		if(mp3data.containsKey("Artist")) tm.AppendtoEnd("Artist", mp3data.get("Artist"));
		if(mp3data.containsKey("Year")) tm.AppendtoEnd("Year", mp3data.get("Year"));
		if(mp3data.containsKey("Comment")) tm.AppendtoEnd("Comment", mp3data.get("Comment"));
		ScreenLog.end("TaglistMap::AppendMp3Content");
		return tm;
	}

	private static TaglistMap InsertFileType(TaglistMap tm, String fp) {
		//FIXME: need to pull this from url and create filetype function
		String content="";
		content=MetadataParser.GetFileType(fp);
		if(content=="") return tm;
		tm.AppendtoEnd("FILETYPE", content);
		return tm;
	}
	
	private static TaglistMap InsertSize(Map<String,String> annotations,TaglistMap tm) throws ParserConfigurationException, SAXException, IOException {
		if(!annotations.containsKey("basic")) return tm;
		tm.AppendtoEnd("SIZE", StringHelper.BytesToHRSize(String.valueOf(getGenericTag("basic", "Size", annotations))));
		return tm;
	}

	static private TaglistMap InsertCoordinates(Map<String,String> annotations, TaglistMap tm, String fp) throws ParserConfigurationException, SAXException, IOException {
		if(annotations.containsKey("geo") && !fp.endsWith("thumb.png") && !fp.endsWith("thumb.jpg")) {
			if(annotations.get("geo").equals("") || annotations.get("geo")=="" || annotations.get("geo").contains("ignore")) {
				annotations.put("geo", "");
				annotations.remove("geo");
			} else {
				
				
				Map<String,String> coordinateMap=MetadataParser.getCenterCoordinates(XMLHelper.StringToDoc(annotations.get("geo")));
				
				
//				List<Coordinates> coordinateList=new ArrayList<Coordinates>();
//				coordinateList=XMLHelper.ExtractCoordinates(XMLHelper.StringToDoc(annotations.get("geo")));
//				int i=0;
				
	//			if(coordinateList.size()>3) {
		///			i=4;
		//		}
				tm.AppendtoEnd("Coordinates",  coordinateMap.get("Y")+", "+coordinateMap.get("X"));
			}
		} 
		return tm;
	}	
	
	private static TaglistMap InsertTitle(Map<String,String> annotations, TaglistMap tm) throws ParserConfigurationException, SAXException, IOException {
		//FIXME: need to pull this from details
		String content="";
		if(!annotations.containsKey("details")) return tm;
		content=XMLHelper.GetSimpleTagContent(XMLHelper.StringToDoc(annotations.get("details")), "TITLE");

		//try again, in case we're dealing with an older format for details
		if(content=="") {
			content=XMLHelper.GetSimpleTagContent(XMLHelper.StringToDoc(annotations.get("details")), "DESCRIPTION");
		}

		if(content=="") return tm;

		tm.AppendtoEnd("TITLE", content);
		// TODO Auto-generated method stub
		return tm;
	}

	private static TaglistMap InsertAction(Map<String,String> annotations, TaglistMap tm) throws ParserConfigurationException, SAXException, IOException {
		//FIXME: need to pull this from details
		String link="",link_label="";
		if(!annotations.containsKey("details")) return tm;
		link=XMLHelper.GetSimpleTagContent(XMLHelper.StringToDoc(annotations.get("details")), "LINK");
		link_label=XMLHelper.GetSimpleTagContent(XMLHelper.StringToDoc(annotations.get("details")), "LINK_LABEL");

		//try again, in case we're dealing with an older format for details
		if(link_label=="") link_label="Link";
		if(link=="") return tm;
		tm.AppendtoEnd(link_label, link);
		return tm;
	}
	
	static private TaglistMap InsertClassification(Map<String,String> annotations,TaglistMap tm) throws ParserConfigurationException, SAXException, IOException {
		String content="";
		if(!annotations.containsKey("details")) return tm;
		content=XMLHelper.GetSimpleTagContent(XMLHelper.StringToDoc(annotations.get("details")), "CAVEAT");

		//try again, in case we're dealing with an older format for details
		if(content=="") {
			content=XMLHelper.GetSimpleTagContent(XMLHelper.StringToDoc(annotations.get("details")), "Classification");
		}
		
		if(content=="") content="UNCLASSIFIED";

		tm.AppendtoEnd("classification", content);
		// TODO Auto-generated method stub
		return tm;
	}

	public static TaglistMap TaglistGenerator(Map<String,String> annotations, String path) throws Exception {
		ScreenLog.begin("====TaglistGenerator====");
		TaglistMap tm=new TaglistMap(path);

		if(annotations.containsKey("mp3")) {
			tm=AppendMp3Content(tm, annotations.get("mp3"));
		} else {
			tm=InsertTitle(annotations, tm);
		}	
		tm=InsertClassification(annotations, tm);
		tm=InsertFileType(tm, path);
		//append to the end, mp3 details, if they exist
			
		String skiplist="title,description,size,filetype,coordinates,link,link_label,caveat,classification,url,";
		
		//append to the end, a tlv2 formatted annotation
		
		if(annotations.containsKey("details")) {
			tm.AppendtoEnd(new TaglistMap(XMLHelper.StringToDoc(annotations.get("details")),path),skiplist);
		}			
		//append to the end, a tlv3 formatted annotation
		
		if(annotations.containsKey("taglist")) {
			tm.AppendtoEnd(new TaglistMap(XMLHelper.StringToDoc(annotations.get("taglist")),path),skiplist);
		}	
	
		//parse and insert coordinates
		//first check if the geo annotation is good, otherwise, kill it
		tm=InsertCoordinates(annotations, tm, path);
		tm=InsertSize(annotations, tm);
		tm=InsertAction(annotations,tm);
			
		ScreenLog.end("===TaglistGenerator===");
		return tm;
	}
	
	public static Document TaglistGenerator(HCPClient client, Map<String,String> parameters) throws Exception {
		if(!parameters.containsKey("path")) return null;
		return TaglistGenerator(client, parameters.get("path"));
	}

	public static Document TaglistGenerator(HCPClient client, String path) throws Exception {
		return TaglistGenerator(AnnotationHelper.HttpGetAnnotationsMap(client, path, true),path).ToDocv3();
	}

	public static String CombineAnnotations(HCPClient client, Map<String,String> parameters) throws XMLStreamException {
		return AnnotationHelper.AnnotationMapToCombinedAnnotation(AnnotationHelper.HttpGetAnnotationsMap(client, parameters.get("path"), true));
	}
		
	private static XMLStreamWriter KMLTour_NetworkLink(XMLStreamWriter serializer, URL imageprefix_url, String path, String title) throws XMLStreamException, MalformedURLException {
		//outer container is a network Link
		serializer.writeStartElement("NetworkLink");
		//for now, just use "url" + #
		//<name>url 0</name>
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "name", title);

		//<visibility>0</visibility>
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "visibility", "0");
		
		//<open>1</open>
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "open", "1");
		
	    //<description>desc</description> -- replace this with CDATA and use Description generator
		//serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "description", description);

	    //  <refreshVisibility>0</refreshVisibility>
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "refreshVisibility", "0");
		
		//  <flyToView>0</flyToView>
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "flyToView", "0");
		//now create the link and href
		serializer.writeStartElement("Link");
		ScreenLog.out("\tpath="+path);
		
		//URL imageprefix_url=new URL("http://cometdev.hcpdomain.com/MetaCatalog/");
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "href", AnnotationHelper.PathToKMLPathURL(imageprefix_url, path).toString());
		serializer.writeEndElement(); //end Link
		serializer.writeEndElement(); //end NetworkLink
		return serializer;
	}
	
	public static Document KMLTour(URL imageprefix_url, String [] paths_to_include) throws Exception {
		ScreenLog.begin("KMLTour()");
		OutputStream os = new ByteArrayOutputStream();
		XMLStreamWriter serializer=KMLHelper.OpenNewKML(os,"Folder");
		for(int i=0; i<paths_to_include.length; i++) {
			serializer=AnnotationGenerator.KMLTour_NetworkLink(serializer, imageprefix_url, paths_to_include[i], getTitle(paths_to_include[i]));
		}
		serializer=KMLHelper.CloseNewKML(serializer);
		ScreenLog.end("KMLTour()");
		return XMLHelper.StringToDoc(os.toString());
	}
	
	private static String getTitle(String path) throws Exception {
		TaglistMap tm=new TaglistMap(TaglistGenerator(new HCPClient(CometProperties.getInstance()), path),path);
		return tm.getTitle();
	}

	public static Map<String,String> DescGenerator_primer(Map<String,String> parameters) throws MalformedURLException {
		if(parameters.containsKey("size")) {
			if(!parameters.get("size").contains("x")) {
				parameters.put("thumbnailsize_w", parameters.get("size"));
				parameters.put("size", parameters.get("thumbnailsize_w")+"x0");
			} else {
				parameters.put("thumbnailsize_w", parameters.get("size").split("x")[0]);
				parameters.put("thumbnailsize_h", parameters.get("size").split("x")[1]);
			}
		} else {
			parameters.put("thumbnailsize_w", String.valueOf(200));
			parameters.put("size", "200x0");
		}
		parameters.put("thumbnail_url", AnnotationHelper.PathToThumbnailURL(parameters.get("imageprefix"),AnnotationHelper.URIEncoder(parameters.get("path")),parameters.get("size")).toString());
		parameters.put("icon_url", AnnotationHelper.PathToIconURL(parameters.get("imageprefix"), AnnotationHelper.URIEncoder(parameters.get("path"))).toString());
		parameters.put("iconsize_w", String.valueOf(45));
		return parameters;
	}

	public static String DescGenerator(HCPClient client, Map<String,String> parameters) throws Exception {
		return AnnotationGenerator.DescGenerator(AnnotationGenerator.TaglistGenerator(AnnotationHelper.HttpGetAnnotationsMap(client, parameters.get("path"), true), parameters.get("path")), parameters);
	}
	
	public static String DescGenerator(TaglistMap tm, Map<String,String> parameters) throws Exception {
		ScreenLog.begin("DescGenerator");

		if(!parameters.containsKey("primed")) {
			parameters=DescGenerator_primer(parameters);
			parameters.put("primed", "1");
		}
		
		Map<String,String> attribs=null;
		OutputStream outputStream = new ByteArrayOutputStream();
		HTMLDoc htmldoc=new HTMLDoc(outputStream);
		
		//htmldoc.setFormatting(true);
		
		if(!parameters.containsKey("thumbnailsize_w")) parameters.put("thumbnailsize_w","200");
		
		//outer table
		htmldoc.CreateTable(Integer.parseInt(parameters.get("thumbnailsize_w"))+310,310);
		
		Map<String,String> col_class_attribs=null; //, row_class_attribs=null;
		//outer row with classification bar
		//int index=0;

		if(parameters.containsKey("showclass")) {
			htmldoc=ClassificationHelper.ShowClassificationBanner(htmldoc,tm.get("classification"));
		}
		htmldoc.CreateRow();
		//	htmldoc.CreateRowwithColumnPair("Path: ", tm.get("path"), true);

		col_class_attribs=new HashMap<String,String>();
		col_class_attribs.put("colspan", "2");

		htmldoc.CreateCol(col_class_attribs);
		
		htmldoc.writeContent("<b>Path:</b> "+parameters.get("path"));
		
		htmldoc.EndColumn();

		htmldoc.EndRow();

		
		htmldoc.CreateRow();
		//LHS column for thumbnail reflected image
		htmldoc.CreateCol(Integer.parseInt(parameters.get("thumbnailsize_w")),0);
		

		
		String id="";
		if(parameters.containsKey("tag")) id="thumbnail_id_"+parameters.get("tag");
		attribs=new HashMap<String, String>();
	
		if(id!="") attribs.put("id", id);
		attribs.put("src", new URL(parameters.get("thumbnail_url")).toString());
		attribs.put("width", parameters.get("thumbnailsize_w"));
		attribs.put("class","reflect");

		//and so on...
		if(parameters.containsKey("onthumbnailclick")) {
			attribs.put("onclick",parameters.get("onthumbnailclick"));
		}

		htmldoc.CreateImage(attribs);
		attribs=null;
		htmldoc.EndColumn();
		
		//RHS column for taglist
		htmldoc.CreateCol();
		//within this cell, create an inner table for RHS
		parameters.put("skip_class", "true");
		htmldoc=TaglistTable(htmldoc,tm,parameters);
		htmldoc.EndColumn();
		htmldoc.EndRow();

		if(parameters.containsKey("showclass")) {
			htmldoc=ClassificationHelper.ShowClassificationBanner(htmldoc,tm.get("classification"));
		}	
		
		htmldoc.close();
		ScreenLog.end("DescGenerator");
		return outputStream.toString();
	}

	private static HTMLDoc TaglistTable(HTMLDoc htmldoc, TaglistMap tm, Map<String, String> parameters) throws Exception {
		ScreenLog.begin("TaglistTable");
		Map<String,String> attribs=null;
		
		//generated from taglist somehow
		int w=0,h=0;
		if(parameters.containsKey("taglistsize")) {
			if(parameters.get("taglistsize").contains("x")) {
				w=Integer.parseInt(parameters.get("taglistsize").split("x")[0]);
				h=Integer.parseInt(parameters.get("taglistsize").split("x")[1]);
			}
		}
		
		if(w==0) w=300;
		if(h==0) h=300;
		
		htmldoc.CreateTable(w,h);
		w=0;
		h=0;
		Boolean showClass=(parameters.containsKey("showclass") && !parameters.containsKey("skip_class"));
		
		if(showClass) {
			htmldoc=ClassificationHelper.ShowClassificationBanner(htmldoc,tm.get("classification"));
		}
		//top row
		//very first row is icon and file type
		if(parameters.containsKey("iconsize_w")) w=Integer.parseInt(parameters.get("iconsize_w"));
		if(parameters.containsKey("iconsize_h")) w=Integer.parseInt(parameters.get("iconsize_h"));
		
		attribs=new HashMap<String,String>();
		attribs.put("class", "mid");
		
		Map<String,String> img_attribs=new HashMap<String,String>();
		img_attribs.put("class", "mid");
		
		if(w!=0) img_attribs.put("width", String.valueOf(w));
		if(h!=0) img_attribs.put("height", String.valueOf(h));
		img_attribs.put("src", new URL(parameters.get("icon_url")).toString());
		
		String filetype="";
		if(parameters.containsKey("tag")) {
			img_attribs.remove("class");
			filetype="<table><tr><td><span dn=\""+parameters.get("tag")+"\">" +
					HTMLDoc.CreateImageString(img_attribs) +"</span></td><td>" +
					tm.get("filetype").trim() + "</td></tr></table>";
		} else {
			filetype=HTMLDoc.CreateImageString(img_attribs)+" "+tm.get("filetype");
		}
		
		
		htmldoc.CreateRowwithColumnPair(null,attribs,"File Type: ", filetype, true);

		if(tm!=null) {
			int j=tm.getIndexWithName("Coordinates");
			int k=tm.getIndexWithName("title");
			if(j!=-1 && parameters.containsKey("Coordinates")) {
				tm.putCouple(j, "Coordinates", parameters.get("Coordinates"));
			} 
		
			if(k!=-1 && parameters.containsKey("Title")) {
				ScreenLog.out("want to overwrite "+tm.getName(k)+"="+tm.getValue(k));
				ScreenLog.out("\t--->with "+parameters.get("Title"));
				tm.put(k, "name", "Title");
				tm.put(k, "value", parameters.get("Title"));
				ScreenLog.out("and now... "+tm.getName(k)+"="+tm.getValue(k));
			}
			
			for(int i=0; i<tm.getCount(); i++) {
				if(tm.getName(i).toLowerCase().contains("filetype") || tm.getName(i).trim()=="" || tm.getName(i).toLowerCase().contains("classification")) continue;
				
				//special javascript actionFunc
				if(tm.getValue(i).contains("javascript:") && tm.getValue(i).contains("tag") && !tm.getValue(i).contains("(") && parameters.containsKey("tag")) {
					if(!parameters.containsKey("skipaction")) {
						htmldoc.CreateRowwithColumnPair("Action:", "<a href=\""+tm.getValue(i)+"('"+parameters.get("tag")+"')\">"+tm.getName(i)+"</a>" , true);
					}
				//regular javascript or html link
				} else if((tm.getValue(i).contains("javascript:") || tm.getValue(i).contains("http"))) {
					if(!parameters.containsKey("skipaction")) htmldoc.CreateRowwithColumnPair("Action:", "<a href=\""+tm.getValue(i)+"\">"+tm.getName(i)+"</a>" , true);
				} else {
				//everything else	
					htmldoc.CreateRowwithColumnPair(tm.getName(i)+": ", tm.getValue(i), true);
				}
			}
		//	serializer=tm.toRows(serializer);
		}

		if(showClass) {
				htmldoc=ClassificationHelper.ShowClassificationBanner(htmldoc,tm.get("classification"));
		}
		htmldoc.EndTable();
		ScreenLog.end("TaglistTable");
		return htmldoc;
	}

	public static String ToolTipGenerator(HCPClient client, Map<String,String> parameters) throws Exception {
		return AnnotationGenerator.ToolTipGenerator(AnnotationGenerator.TaglistGenerator(AnnotationHelper.HttpGetAnnotationsMap(client, parameters.get("path"), true), parameters.get("path")), parameters);
	}
	
	public static String ToolTipGenerator(TaglistMap tm, Map<String,String> parameters) throws Exception {
		if(!parameters.containsKey("primed")) {
			parameters=DescGenerator_primer(parameters);
			parameters.put("primed", "1");
		}
		return TaglistTable(new HTMLDoc(new ByteArrayOutputStream()), tm, parameters).toString(); 
	}

	public static String KMLGenerator(HCPClient client, Map<String,String> parameters) throws Exception {
		return AnnotationGenerator.KMLGenerator(AnnotationGenerator.TaglistGenerator(AnnotationHelper.HttpGetAnnotationsMap(client, parameters.get("path"), true), parameters.get("path")), parameters);
	}
	
	public static String KMLGenerator(TaglistMap tm, Map<String,String> parameters) throws Exception {
		if(!parameters.containsKey("primed")) {
			parameters=DescGenerator_primer(parameters);
			parameters.put("primed", "1");
		}
		parameters.put("description", AnnotationGenerator.DescGenerator(tm, parameters));
		ScreenLog.out("coordinates in tm are : "+tm.get("Coordinates"));
		if(tm.get("Coordinates")=="") return "";
		parameters.put("latitude",tm.get("Coordinates").split(",")[0].trim());
		parameters.put("longitude",tm.get("Coordinates").split(",")[1].trim());
		ScreenLog.out("coordinates are : "+parameters.get("latitude")+","+parameters.get("longitude"));
		parameters.put("Title", tm.getTitle());
		return XMLHelper.DocToString(KMLHelper.GenerateIndividualKML(parameters));
	}

	public static Document KMLWrapperGenerator(HCPClient client, Map<String,String> parameters) throws Exception {
		if(!parameters.containsKey("path")) return null;
		return KMLWrapperGenerator(AnnotationHelper.HttpGetAnnotationsMap(client, parameters.get("path"), true),parameters);
	}

	private static Document KMLWrapperGenerator(Map<String, String> annotations, Map<String, String> parameters) throws Exception {
		if(!parameters.containsKey("path")) return null;
		if(!parameters.get("path").endsWith(".kmz") && !parameters.get("path").endsWith(".kml")) return null;
		ScreenLog.begin("====KMLWrapperGenerator====");

		OutputStream os = new ByteArrayOutputStream();
		XMLStreamWriter serializer=KMLHelper.OpenNewKML(os,"Folder");
		String title=getTitle(parameters.get("path"));

		//outer container is a network Link
		serializer.writeStartElement("NetworkLink");
		//for now, just use "url" + #
		//<name>url 0</name>
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "name", title);

		//<visibility>0</visibility>
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "visibility", "0");
		
		//<open>1</open>
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "open", "1");
		
	    //<description>desc</description> -- replace this with CDATA and use Description generator
		//serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "description", description);

	    //  <refreshVisibility>0</refreshVisibility>
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "refreshVisibility", "0");
		
		//  <flyToView>0</flyToView>
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "flyToView", "0");

		//now create the link and href
		serializer.writeStartElement("Link");
		
		//URL imageprefix_url=new URL("http://cometdev.hcpdomain.com/MetaCatalog/");
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "href", AnnotationHelper.PathToObjectURL(parameters.get("imageprefix"), parameters.get("path")).toString());
	
		serializer.writeEndElement(); //end Link
		serializer.writeEndElement(); //end NetworkLink
		serializer=KMLHelper.CloseNewKML(serializer);
		ScreenLog.end("====KMLWrapperGenerator====");
		return XMLHelper.StringToDoc(os.toString());
	}
	
	//move to metadataparser? or metadatamanager?
	//function takes a URL and optional "tag" and generates a metadata map, which can be easily converted to XML or json
	//this function replaces FilePathToOutput()
	public static Map<String,String> COMETMetadataMapFromURL(URL objectURL, String tag) throws Exception  {
		ScreenLog.begin("COMETMetadataMapFromURL("+objectURL.toString()+")");
		
		if(objectURL.toString().contains("thumb")) {
			ScreenLog.warning(" ===> thumbnail should not have a map! ("+objectURL.toString()+") - unsuccessful");
		}
		
		CometProperties mProps=CometProperties.getInstance();
		//create a client object
		HCPClient client=new HCPClient(mProps);
	
		//get all annotations except default; retrieving annotations automatically populates the map and headers
		//	annotations=AnnotationHelper.HttpGetAnnotationsMap(client,fp,true);
		//temporarily, allow default to be populated
	
		String path="";
		Map<String,String> annotations=null;
		try {
			client.HttpGetHCPHeader(objectURL);
			//headers=client.getHeaders();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			ScreenLog.ExceptionOutputHandler(e1);
		}
	
		//grab our SystemMetadata, set it statically
		MetadataManager.setSystemMetadataMap(client.getHeaders());
	
		annotations=AnnotationHelper.HttpGetAnnotationsMap(client,objectURL.toString(),false);
	
		//using the above order, establish a map
		Map<String, String> metadataMap=null;
		
		MetadataParser mp=MetadataParser.getInstance();
	
		path=AnnotationHelper.URLToPath(objectURL);
		ScreenLog.out("\tpath="+path);
		mp.ParseAnnotations(annotations,path,client.getHeaders());
		metadataMap=MetadataParser.getMetadataMap();

		if(tag!=null && !tag.equals("")) metadataMap.put("tag", tag);
		ScreenLog.end("COMETMetadataMapFromURL("+path+","+tag+") successful");
		
		
		
		if(metadataMap.containsKey("content_urlName") && CometProperties.getInstance().getReverseLookup()) {
			
			
			ScreenLog.out("=========");
			ScreenLog.out("content_urlName=\""+metadataMap.get("content_urlName")+"\"");
			ScreenLog.out("=========");
			
			//if(tag==null || !tag.equals("-2"))
			return COMETMetadataMapFromURL(AnnotationHelper.PathToURL(CometProperties.getInstance().getDestinationRootPath(), metadataMap.get("content_urlName")), tag);
			
			
		}
		
		return metadataMap;
	}

	public static String TestGenerator(HCPClient client, Map<String,String>parameters) throws Exception {
		Document kml_orig=XMLHelper.InputStreamToDoc(new DataInputStream(client.HttpGetHCPContentStream(
				AnnotationHelper.PathToURL(new URL(parameters.get("rootpath")),parameters.get("path")))));
		return KMLHelper.TaglistMaptoMultiKML(KMLHelper.KMLReducto(parameters.get("path"), kml_orig));
	}
}
