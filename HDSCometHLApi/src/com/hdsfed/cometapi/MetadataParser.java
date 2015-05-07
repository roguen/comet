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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

//TODO: consider merging with metadatamanager

public class MetadataParser {
	private static ExtendedLogger ScreenLog = new ExtendedLogger(MetadataParser.class.getPackage().getName());
	public enum actionType {
		AF_AUDIO,
		AF_VIDEO,
		AF_YOUTUBE,
		AF_KMLNETLINK,
		AF_UNSUPPORTED
	}

	private static actionType ActionFuncToEnum(String action) {
		if(action.equals("video")) return actionType.AF_VIDEO;
		if(action.equals("audio")) return actionType.AF_AUDIO;
		if(action.equals("youtubevideo")) return actionType.AF_YOUTUBE;
		if(action.equals("kmlnetworklink")) return actionType.AF_KMLNETLINK;
		return actionType.AF_UNSUPPORTED;
	}
	
	private static MetadataParser instance;
	private static SortedMap<String,String> MetadataMap;
	private static Set<String> CenterCoordinates;
	public static MetadataParser getInstance() {
		//delete this instance and make new for a new file path
		if (instance == null) {
			instance = new MetadataParser();
		}
		return instance;
	}

	public static void setInstance(MetadataParser instance) {
		MetadataParser.instance = instance;
	}

	static public Map<String,String> getCenterCoordinates(Document doc) {
		ScreenLog.begin("metadataParser:::getCentercoordinates");
		
		Map<String,String> result=new TreeMap<String,String>();
		List<Coordinates> coordinateList=new ArrayList<Coordinates>();
		
		coordinateList=XMLHelper.ExtractCoordinates(doc);
		
		
		int i=0;
		if(coordinateList.size()>3) {
			i=4;
		}
		if(coordinateList.size()>0) {
			result.put("Y", coordinateList.get(i).getY());
			result.put("X", coordinateList.get(i).getX());
		} else {
			result.put("Y", "0");
			result.put("X", "0");
			
		}
		ScreenLog.end("metadataParser:::getCentercoordinates");
		return result;
	}

	private static Boolean isDTDGeoFileType(String ending) {
		return (ending.endsWith("dtd") || ending.endsWith("dt1") || ending.endsWith("dt0") || ending.endsWith("min") || ending.endsWith("max") || ending.endsWith("avg"));
	}
	
	private static Boolean isNITFGeoFileType(String ending) {
		return (ending.endsWith("nitf") || ending.endsWith("ntf") || ending.endsWith("i21") || ending.endsWith("i42") || ending.endsWith("i41") || ending.endsWith("I41"));
	}
	
	private static Boolean isGeoTIFFGeoFileType(String ending) {
		return (ending.endsWith("tiff") || ending.endsWith("tif"));
	}
	
	private static Boolean isJPEGImageFileType(String ending) {
		return (ending.endsWith("jpeg") || ending.endsWith("jpg") || ending.endsWith("jp2")); 
	}
	
	private static Boolean isBiometricFileType(String ending) {
		return (ending.endsWith("efts") || ending.endsWith("bio") || ending.endsWith("eft") || ending.endsWith("fbi")); 
	}
	
	private static Boolean isZipFileType(String ending) {
		return (ending.endsWith("tgz") || ending.endsWith("gz") || ending.endsWith("bz2") || ending.endsWith("rar") || ending.endsWith("zip"));
	}
	
	//TODO: track down other versions of this function in other classes and replace
	//		consider replacing with text file lookup
	public static String GetFileTypeIcon(String fp) {
		if(!fp.contains(".")) return "unknown";
		
		String ending=fp.substring(fp.lastIndexOf("."),fp.length()).toLowerCase();
		//geospatial
		if(isDTDGeoFileType(ending)) return "dted";
		if(isNITFGeoFileType(ending)) return "nitf";
		if(isGeoTIFFGeoFileType(ending)) return "geotiff";
		if(ending.endsWith("prm")) return "midas";
		if(ending.endsWith("hgt")) return "geohgt";
		if(ending.endsWith("shx") || ending.endsWith("shp") || ending.endsWith("dbf")) return "shape";
		if(ending.endsWith("sid")) return "mrsid";
		if(ending.endsWith("kml") || ending.endsWith("kmz")) return "kml";
		if(ending.endsWith("stream") || ending.endsWith("sql")) return "stream";
	
		//non-geospatial images
		if(isJPEGImageFileType(ending)) return "jpg";
		if(ending.endsWith("png")) return "png";
		if(ending.endsWith("gif")) return "gif";
		if(ending.endsWith("fits")) return "fits";
		//office
		if(ending.endsWith("pdf")) return "pdf";
		if(ending.endsWith("txt")) return "txt";
		if(ending.endsWith("doc") || ending.endsWith("docx")) return "word";
		if(ending.endsWith("ppt") || ending.endsWith("pptx")) return  "ppt";
		if(ending.endsWith("xls") || ending.endsWith("xlsx")) return  "xls";
		//everything else
		if(isBiometricFileType(ending)) return "biometric";
		if(ending.endsWith("m4v") || ending.endsWith("mpeg") || ending.endsWith("mp4") || ending.endsWith("mpg") || ending.endsWith("mkv")) return "video";
		if(isZipFileType(ending)) return "zip";
		if(ending.endsWith("enc") || ending.endsWith("crypt")) return "encrypt";
		if(ending.endsWith("key")) return "key";
		if(ending.endsWith("idx")) return "sw-idx";
		if(ending.endsWith("idf")) return "sw-idf";
		if(ending.endsWith("cwx")) return "sw-cwx";
		if(ending.endsWith("asm")) return "proe-asm";
		if(ending.endsWith("prt")) return "proe-prt";
		if(ending.endsWith("wav")) return "wav";
		if(ending.endsWith("mp3")) return "mp3";	
		return "unknown";
	}

	//TODO: track down other versions of the function in other classes and replace
	//		consider replacing with text file lookup
	public static String GetFileType(String fp) {
		if(!fp.contains(".")) return "Unknown file type";
		String ending=fp.substring(fp.lastIndexOf("."),fp.length()).toLowerCase();
		//geospatial
		if(isDTDGeoFileType(ending)) return "Geo Spatial File";
		if(isNITFGeoFileType(ending)) return "Geo Spatial File";
		if(isGeoTIFFGeoFileType(ending)) return "Geo Spatial File";
		if(ending.endsWith("prm")) return "Geo Spatial File";
		if(ending.endsWith("hgt")) return "Geo Spatial File";
		if(ending.endsWith("shx") || ending.endsWith("shp") || ending.endsWith("dbf")) return "Geo Spatial File";
	
		if(ending.endsWith("sid")) return "Geo Spatial File";
		if(ending.endsWith("kml") || ending.endsWith("kmz")) return "Keyhole Markup Language";

		if(ending.endsWith("stream")) return "Stream File";
		if(ending.endsWith("sql")) return "SQL Query File";

		//non-geospatial images
		if(isJPEGImageFileType(ending)) return "Image File";
		if(ending.endsWith("png")) return "Image File";
		if(ending.endsWith("gif")) return "Image File";
		//office
		if(ending.endsWith("pdf")) return "Document";
		if(ending.endsWith("txt")) return "Document";
		if(ending.endsWith("doc") || ending.endsWith("docx")) return "Document";
		if(ending.endsWith("ppt") || ending.endsWith("pptx")) return  "Document";
		if(ending.endsWith("xls") || ending.endsWith("xlsx")) return  "Document";
		//everything else
		if(isBiometricFileType(ending)) return "Biometric Data";
		if(ending.endsWith("m4v") || ending.endsWith("mpeg") || ending.endsWith("mp4") || ending.endsWith("mpg") || ending.endsWith("mkv")) return "Video";
		if(isZipFileType(ending)) return "Compressed";
		if(ending.endsWith("enc") || ending.endsWith("crypt")) return "Encrypted";
		if(ending.endsWith("key")) return "Key";
			
		if(ending.endsWith("fits")) return "Flexible Image";
		if(ending.endsWith("idx")) return "Engineering";
		if(ending.endsWith("idf")) return "Engineering";
		if(ending.endsWith("cwx")) return "Engineering";
		if(ending.endsWith("asm")) return "Engineering";
		if(ending.endsWith("prt")) return "Engineering";
			
		if(ending.endsWith("wav")) return "Audio";
		if(ending.endsWith("mp3")) return "Audio";	
		return "Unknown file type";
	}

	//TODO: deprecate "path" in favor of utf8name and urlName
	public void ParseAnnotations(Map<String,String> annotations, String path, Map<String,String> headers) throws Exception {
		ScreenLog.begin("ParseAnnotations("+path+")");
		SortedMap<String, String> cmmap=new TreeMap<String,String>();
		Map<String, String> coordinateMap=new TreeMap<String,String>();
		
		if(annotations.containsKey("geo")) {
			if(annotations.get("geo").equals("") || annotations.get("geo")=="" || annotations.get("geo").contains("ignore")) {
				annotations.put("geo", "");
				annotations.remove("geo");
			} else {
				coordinateMap=getCenterCoordinates(XMLHelper.StringToDoc(annotations.get("geo")));
				cmmap.put("latitude", coordinateMap.get("Y"));
				cmmap.put("longitude", coordinateMap.get("X"));
			}
		} 
		cmmap.put("caveat", getGenericTag("default","CAVEAT", annotations));

		//FUTURE: replace path with utf8name and urlname where appropriate 
		cmmap.put("path", path); //FUTURE: deprecate this one
		cmmap.put("urlName", path);
		cmmap.put("utf8name", AnnotationHelper.URIDecoder(path));
		cmmap.put("desc", getGenericTag("default","DESCRIPTION", annotations));
		cmmap.put("link_label", getGenericTag("default","LINK_LABEL", annotations));
		cmmap.put("link_action", getGenericTag("default","LINK", annotations));
		cmmap.put("content_urlName", getGenericTag("content","CONTENT_URL", annotations));
		cmmap.put("content_utf8Name", getGenericTag("content","CONTENT_UTF8NAME", annotations));
		
		
		if(cmmap.get("content_urlName").equals("")) cmmap.remove("content_urlName");
		if(cmmap.get("content_utf8Name").equals("")) cmmap.remove("content_utf8Name");
		
		
		cmmap.put("note_urlName", getGenericTag("note","NOTE_URL", annotations));
		cmmap.put("note_utf8Name", getGenericTag("note","NOTE_UTF8NAME", annotations));
		
		
		if(cmmap.get("note_urlName").equals("")) cmmap.remove("note_urlName");
		if(cmmap.get("note_utf8Name").equals("")) cmmap.remove("note_utf8Name");
		
		
		if(annotations.containsKey("path")) cmmap.put("has_kmlpath", "true");
		
		//not used .. yet
		//cmmap.put("startTime", getGenericTag("datetime","startTime", annotations));
		//should be a way of doing this without using "dont"
		cmmap.put("dont_autoplay",StringHelper.BooleanToString(getGenericTag("default","DONT_AUTOPLAY", annotations).toLowerCase().equals("true")));
		cmmap.put("annotation_size", ""+annotations.size());

		cmmap.put("actionFunc", getGenericTag("basic","ACTION_FUNC", annotations));

		if(headers.containsKey("X-HCP-Type")) cmmap.put("fileType", headers.get("X-HCP-Type"));
		TaglistMap tm=null;

		if(annotations.size()!=0) {
			tm=AnnotationGenerator.TaglistGenerator(annotations, path);
			for(int i=0; i<tm.getCount(); i++) {
				if(tm.getValue(i).contains("http") || tm.getValue(i).contains("javascript")) {
				cmmap.put("link_action", tm.getValue(i));
				cmmap.put("link_label", tm.getName(i));
				break;
				}
			}
		}
		
		if(cmmap.containsKey("actionFunc")) {
			cmmap.put("link_action", MetadataParser.ActionFuncToLinkAction(cmmap.get("actionFunc")));
			cmmap.put("link_label", MetadataParser.ActionFuncToLinkLabel(cmmap.get("actionFunc")));
			cmmap.remove("actionFunc");
		}
		
		if(cmmap.containsKey("fileType") && cmmap.get("fileType").contains("directory")) cmmap.put("fileTypeIcon", "folder");
		else cmmap.put("fileTypeIcon", MetadataParser.GetFileTypeIcon(path));
		
		
		if(cmmap.containsKey("longitude") && cmmap.containsKey("latitude") && !cmmap.get("longitude").isEmpty() && !cmmap.get("latitude").isEmpty()) cmmap.put("geotagged", "true");
		
		cmmap.put("mdm", "nill");
		cmmap.put("balloon", "nill");
		cmmap.put("flownin", "true");
		cmmap.put("playing", "false");
		cmmap.put("showBalloon", "false");
		
		if(annotations.size()==0) {
			cmmap.put("tooltip_contents", "Object has no metadata.");
		} else {
			cmmap.put("tooltip_contents", "Loading metadata.");
			//cmmap.put("tooltip_contents", "Relay?path="+cmmap.get("urlName")+"&type=generated&annotation=tooltips&stream&astext&size=300x400&skipaction" ></iframe>';
		}
		cmmap.put("active", "false"); //switch on after tooltips are loaded
		
		
		
		setMetadataMap(cmmap);
		ScreenLog.end("ParseAnnotations("+path+") successful");
	}

	private static String ActionFuncToLinkAction(String actionFunc) {
		switch(ActionFuncToEnum(actionFunc.toLowerCase())) {
		case AF_AUDIO:
			return "javascript:CometControlsPlayAudioByTag";
		case AF_VIDEO:
			return "javascript:CometControlsPlayMPEGVideoByTag";
		case AF_YOUTUBE:
			return "javascript:CometControlsPlayYouTubeVideoByTag";
		case AF_KMLNETLINK:
			return "javascript:GoogleControlsLoadKMLviaNetworkLinkByTag";
		default:
		}
		ScreenLog.out("Action Function: "+actionFunc+" is unsupported at this time.");
		return "";
	}

	private static String ActionFuncToLinkLabel(String actionFunc) {
		switch(ActionFuncToEnum(actionFunc.toLowerCase())) {
		case AF_AUDIO:
		case AF_VIDEO:
		case AF_YOUTUBE:
			return "Play";
		case AF_KMLNETLINK:
			return "Load";
		default:
		}
		return "";
	}

	private String getGenericTag(String look_in_annotation, String xml_tag, Map<String, String> annotations) throws ParserConfigurationException, SAXException, IOException {
		String result="";
		if(!annotations.containsKey(look_in_annotation)) return result;
		result=XMLHelper.GetSimpleTagContent(XMLHelper.StringToDoc(annotations.get(look_in_annotation)), xml_tag);
		return result;
	}

	public static Map<String,String> getMetadataMap() {
		return MetadataMap;
	}

	public static void setMetadataMap(SortedMap<String,String> metadataMap) {
		MetadataParser.MetadataMap = metadataMap;
	}

	public static Set<String> getCenterCoordinates() {
		return CenterCoordinates;
	}

	public static void setCenterCoordinates(Set<String> centerCoordinates) {
		CenterCoordinates = centerCoordinates;
	}
	
	public static String MetadataMapToTimeStamp(Map<String,String> MetadataMap, String dateTag, String hourTag, String minTag) {
		return CreateTimeStamp(MetadataMap.get(dateTag),
				Integer.parseInt(MetadataMap.get(hourTag)),
				Integer.parseInt(MetadataMap.get(minTag)));
	}

	public static String CreateTimeStamp(String dateString, int hourTag, int minTag) {
		return dateString+"T"+StringHelper.ZeroPadded(hourTag)+":"+StringHelper.ZeroPadded(minTag);
	}
}
