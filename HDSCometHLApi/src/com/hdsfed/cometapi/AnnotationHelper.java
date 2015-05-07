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
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//TODO: functions need descriptions and prereqs
public class AnnotationHelper {
	private static String HTTP_SEPARATOR = "/";
	private static ExtendedLogger ScreenLog = new ExtendedLogger(AnnotationHelper.class.getPackage().getName());

	public static String AnnotationMapToCombinedAnnotation(Map<String,String> annotations) {
		int count=0;
		//loop through twice to make sure we don't include blank annotations in the count

		//nasty kludge.. should be doing this in a single loop
		// At worst, this is only iterating over 10 annotations, so let it go for now
		for (String key : annotations.keySet()) {
			if(!annotations.get(key).equals("")) count++;
		}
		String all_metadata="<all_annotations><number_of_annotations>"+count+"</number_of_annotations>";
		String this_annotation=null;
	
		for (String key : annotations.keySet()) {
			if(annotations.get(key).equals("")) {
				ScreenLog.out("AnnoMapToCombined: skipping annotation "+key);	
				continue;
			}
			this_annotation=annotations.get(key);
			if(this_annotation.substring(0, 10).contains("<?xml")) this_annotation=StringHelper.ChopLf(this_annotation,">");
			all_metadata+="<annotation name=\""+key+"\">"+this_annotation+"</annotation>";
		}	
		all_metadata+="</all_annotations>";
		return all_metadata;
	}

	public static URL PathAndAnnotationToURL(URL rootPath, String fp, String annotation) throws MalformedURLException {
		if(annotation==null || annotation=="null") annotation="default";
		return new URL(PathToURL(rootPath,fp).toString()+"?type=custom-metadata&annotation="+annotation);
	}

	//FUTURE: Want to use this everywhere.  For now, use only in the ingestor
	//TODO: Need to investigate if the standard URIEncoder can be used instead
	public static String URIEncoder(String fp) {
		StringBuffer buf=new StringBuffer(fp);
		
		//prime the pump
		//might cause problems for something already encoded
		//buf=StringHelper.ReplaceAll(buf,"%","%25");
		
		
//		buf=StringHelper.ReplaceAll(buf," ","%20");
		buf=StringHelper.ReplaceAll(buf," ","%20");
		buf=StringHelper.ReplaceAll(buf,"!","%21");
//		buf=StringHelper.ReplaceAll(buf,"\"","%22");
//		buf=StringHelper.ReplaceAll(buf,"#","%23");
//		buf=StringHelper.ReplaceAll(buf,"$","%24");
//		buf=StringHelper.ReplaceAll(buf,"%","%25");

		buf=StringHelper.ReplaceAll(buf,"&","%26");

//		buf=StringHelper.ReplaceAll(buf,"'","%27");
//		buf=StringHelper.ReplaceAll(buf,"(","%28");
//		buf=StringHelper.ReplaceAll(buf,")","%29");
//		buf=StringHelper.ReplaceAll(buf,"*","%2A");
		buf=StringHelper.ReplaceAll(buf,"+","%2B");
//		buf=StringHelper.ReplaceAll(buf,",","%2C");
//		buf=StringHelper.ReplaceAll(buf,"-","%2D");
//		buf=StringHelper.ReplaceAll(buf,".","%2E");
		buf=StringHelper.ReplaceAll(buf,"?","%3F");

		
		fp=buf.toString();
		
		fp=fp.replaceAll("\\[", "%5B");
		fp=fp.replaceAll("\\]", "%5D");
//		buf=StringHelper.ReplaceAll(buf,"\\[","%5B");
//		buf=StringHelper.ReplaceAll(buf,"\\]","%5D");
//		buf=StringHelper.ReplaceAll(buf,"<","%3C");
//		buf=StringHelper.ReplaceAll(buf,">","%3E");
		return fp;
	}

	//TODO: push exception handling out to client program
	//		decoding is far safer, so just use the built-in
	public static String URIDecoder(String fp) {
		ScreenLog.begin("Decoder for ("+fp+")");
		try {
			String s=URLDecoder.decode(fp,"UTF-8");
			ScreenLog.end("Decoder successful for ("+fp+")");

			return s;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			ScreenLog.ExceptionOutputHandler(e);
		}
		ScreenLog.end("unsuccessful for ("+fp+")");
		return fp;
	}

	
	public static URL PathToURL(URL rootPath, String fp) throws MalformedURLException {
		//fp could have been https://ns.tn.hcp.domain.com/rest/somepath/file.ext
		//now fp is just /somepath/file.ext
		//make sure that fp doesn't contain spaces, ampersands, commas, or brackets [ ]

		//FUTURE: Should make sure our URLs are properly encoded
		int len=fp.length();
		if(fp.contains(rootPath.toString())) fp=fp.substring(rootPath.toString().length(),len);
		fp=URIEncoder(fp);

		if(!fp.contains("http")) fp=rootPath.toString()+fp;
		return new URL(fp);
	}

	public static String URLToPath(URL url) throws MalformedURLException {
		return url.toString().substring(url.toString().indexOf("/rest")+5, url.toString().length());
	}
	
	//custom-metadata-info on HCP 6.1 is broken
	public static URL AnnotationListToURL(URL rootPath, String fp) throws MalformedURLException {
		if(!fp.contains("http")) fp=rootPath.toString()+fp;
		return new URL(PathToURL(rootPath,fp).toString()+"?type=custom-metadata-info");
	}

	public static Map<String,String> AnnotationMapParseRequest(HttpServletRequest request) {
		Map<String, String> annotationMap=new TreeMap<String,String>();
		String annotation_hint=request.getParameter("annotation_hint");
		String[] annotationList=annotation_hint.split(",");
		for(int i=0; i<annotationList.length;i++ ) {
			annotationMap.put(annotationList[i], request.getParameter("xmlcontent_"+annotationList[i]));
		}
		return annotationMap;
	}
	
	public static Map<String,String> AddCombinedAnnotationToMap(Map<String,String> srcMap, String combinedAnnotationName) {
		Map<String, String> tgtMap=srcMap;
		tgtMap.put(combinedAnnotationName, AnnotationMapToCombinedAnnotation(srcMap));
		return tgtMap;
	}

	public static void HttpPutAnnotationstoHCP(Map<String,String> annotations, HCPClient client, String fp) {
		//assume client was initialized already
		//assume that combined annotations were embedded in map, if so desired
		for (String key : annotations.keySet()) {
			if(key.equals("") || annotations.get(key).equals("")) {
				ScreenLog.out("annotation ("+key+") was blank, skipping");
				continue;
			}
			try {
				client.HttpPutHCPContent(annotations.get(key),AnnotationHelper.PathAndAnnotationToURL(client.getRootpath(),fp,key));
			} catch (Exception e) {
				ScreenLog.ExceptionOutputHandler(e);
			}
		} //end for loop
	}
	
	public static Map<String,String> HttpGetAnnotationsMap(HCPClient client, String fp, Boolean skipcombined) {
		return HttpGetAnnotationsFromHCP(client,fp,skipcombined, true);
	}

	public static Set<String> HttpGetAnnotationsSet(HCPClient client, String fp) {
		return HttpGetAnnotationsFromHCP(client,fp,false, false).keySet();
	}
	
	//TODO: move exception handling to client
	private static Map<String,String> HttpGetAnnotationsFromHCP(HCPClient client, String fp,Boolean skipcombined, Boolean populate) {
		ScreenLog.begin("HttpGetAnnotationsFromHCP(...,"+fp+",skipcominbed="+skipcombined+",populate="+populate);
		Map<String,String> annotations=null;
		Document doc=null;
		String annotation="";
		String content="";
		try {
			//should catch return code if there is one
			client.HttpGetHCPHeader(AnnotationHelper.PathToURL(client.getRootpath(), fp));
			
			if(client.getHeaders().containsKey("X-HCP-Type") && client.getHeaders().get("X-HCP-Type").contains("directory")) return new TreeMap<String,String>();
			if(client.getHeaders().containsKey("X-HCP-Type") && client.getHeaders().get("X-HCP-Type").contains("object") && client.getHeaders().get("X-HCP-Custom-Metadata").contains("false")) return new TreeMap<String,String>();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			ScreenLog.ExceptionOutputHandler(e);
		}
		
		try {
			annotations=new TreeMap<String,String>();
			doc=XMLHelper.InputStreamToDoc(client.HttpGetHCPContentStream(AnnotationHelper.AnnotationListToURL(client.getRootpath(),fp)));
			if(doc==null) {
				ScreenLog.out("document is null.. bail out");
				return null;
			}

			//parse the doc and generate a list of annotations
			NodeList nodeList = doc.getElementsByTagName("annotation");
			if(nodeList==null) {
				return null;
			}
			
			for (int i = 0; i < nodeList.getLength(); i++) {
				ScreenLog.out("inner loop");
				Node node = nodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					annotation=XMLHelper.getTagValue("name",(Element) node);
					
					if(annotation==null) {
						ScreenLog.out("this node is missing a name element, bail out");
						continue;
					}

					if(!(annotation.equals(client.getCombinedAnnotationName()) && skipcombined) && !annotation.equals("")) {
						if(populate) {
							String s=client.HttpGetHCPContent(PathAndAnnotationToURL(client.getRootpath(),fp,annotation));
							s=s.replaceAll("&", "&amp;");
							content=XMLHelper.DocToString(XMLHelper.StringToDoc(s));
						} else {
							content="";
						} //end if populate
						annotations.put(annotation, content);
					} //end if skipcombined
				} // end if node is element
			} //end for
		} catch (Exception e) {
			ScreenLog.ExceptionOutputHandler(e);
		}
		ScreenLog.end("HttpGetAnnotationsFromHCP(...,"+fp+",skipcominbed="+skipcombined+",populate="+populate);
		return annotations;
	}
	
	public static Boolean ValidateGeo(Document doc, String annotation) {
		//if not geo, we're always valid
		if(!annotation.equals("geo")) return Boolean.TRUE;
		Map<String,String> coordinateMap=MetadataParser.getCenterCoordinates(doc);
		if(Float.parseFloat(coordinateMap.get("X"))>180.0 || Float.parseFloat(coordinateMap.get("Y"))>180.0) return Boolean.FALSE;
		
		//for now, we're always valid, but we want to check to see that
		//X and Y are within bounds for this type
		return Boolean.TRUE;
	}

	public static URL PathToObjectURL(String servletPrefix, String path) throws MalformedURLException {
		return new URL(servletPrefix+"Relay?path="+path+"&type=object&stream");
	}
	private static URL PathToImageURL(String servletPrefix, String path, String size, String imagetype) throws MalformedURLException {
		return new URL(servletPrefix+"Relay?path="+path+"&type="+imagetype+"&stream&size="+size);
	}
	public static URL PathToIconURL(String servletPrefix, String path) throws MalformedURLException {
		return new URL(servletPrefix+"images/icons/"+MetadataParser.GetFileTypeIcon(path)+".png");
		//return PathToImageURL(prefix, path, size, "icon");
	}
	public static URL PathToThumbnailURL(String servletPrefix, String path, String size) throws MalformedURLException {
		return PathToImageURL(servletPrefix, path, size, "thumbnail");
	}
	
	public static URL FSToURLPath(URL rootpath, String s_inInitialPath, File inSourceFile) throws MalformedURLException {
		String encodedPath="";
		File inInitialPath=new File(s_inInitialPath);
		StringBuffer destFilePath;
		destFilePath = new StringBuffer(inSourceFile.toString());

		// Remove the initial path from the full path.
		if (0 == destFilePath.indexOf(inInitialPath.getAbsolutePath())) {
			destFilePath.delete(0, inInitialPath.getAbsolutePath().length() + File.separator.length());
		}
		
		// Need to be OS agnostic and replace any FS separators with HTTP separator, if it isn't the same.
		if (! File.separator.equals(HTTP_SEPARATOR)) { 
			int charIndex = destFilePath.indexOf(File.separator);
			while (-1 != charIndex) {
				destFilePath.replace(charIndex, charIndex + File.separator.length(), HTTP_SEPARATOR);
				charIndex = destFilePath.indexOf(File.separator);
			}
		}
		
		// Now provide a URL with the file part encoded just in case it has special characters.
		encodedPath = rootpath + HTTP_SEPARATOR + URIEncoder(destFilePath.toString());
		
		ScreenLog.out("finally, returning encoded Path: "+encodedPath);
		return new URL(encodedPath);
	}
	public static URL URLToKMLPathURL(URL imageprefix_url, URL url) throws MalformedURLException {
		return PathToKMLPathURL(imageprefix_url,AnnotationHelper.URLToPath(url));
	}

	public static URL PathToKMLPathURL(URL imageprefix_url, String path) throws MalformedURLException {
		return new URL(imageprefix_url+"Relay?path="+path+"&type=generate&annotation=kml_path&stream");
	}
	
	public static String AnnotationCopy(HCPClient client, String path, String old_annotation, String new_annotation) throws MalformedURLException, Exception {
		return client.HttpDeepCopyHCPContent(AnnotationHelper.PathAndAnnotationToURL(client.getRootpath(), path, old_annotation),
				AnnotationHelper.PathAndAnnotationToURL(client.getRootpath(), path, new_annotation));
	}
	public static String AnnotationRename(HCPClient client, String path, String old_annotation, String new_annotation) throws MalformedURLException, Exception {
		return AnnotationCopy(client,path,old_annotation,new_annotation)+" "+client.HttpDeleteHCPContent(AnnotationHelper.PathAndAnnotationToURL(client.getRootpath(),path,old_annotation));
	}
	
	//current file, wrapper prefix, and the current source path to subtract from inFile
	public static File FilePathToHCPPath(File inFile, String prefix, String workingPath) {
		if(CometProperties.getInstance().neverUsePrefixes()) { prefix=""; }
		return FilePathToHCPPath(inFile,prefix,workingPath,"");
	}

	public static File FilePathToHCPPath(File inFile, String prefix, String workingPath, String suffix) {
		if(CometProperties.getInstance().neverUsePrefixes()) { prefix=""; }
		if(prefix.endsWith("/")) prefix=prefix.substring(0, prefix.length()-1);
		return new File(prefix+inFile.toString().substring(workingPath.length(), inFile.toString().length())+suffix);
	}
	
//	(client,CometProperties.getInstance().getDestinationRootPath(),parameters.get("path")+"_moved",parameters.get("path"));
	public static void CopyAllAnnotations(HCPClient client, URL rootpath, String from_path, String to_path)  {
		//acquire list of all annotations
		//Map<String,String> annotations=
		AnnotationHelper.HttpPutAnnotationstoHCP(AnnotationHelper.HttpGetAnnotationsFromHCP(client, from_path, false, true), client,to_path);
		//return "";
	}
	
	
	
	
	
	
	
}
