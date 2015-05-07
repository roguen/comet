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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


//TODO: move all exceptions to client code
//		function descriptions needed
//		probably a better way for this class to exist
public class TaglistMap extends ResultMap {
	private static ExtendedLogger ScreenLog = new ExtendedLogger(TaglistMap.class.getPackage().getName());
	private String path="";
	public TaglistMap(String path) {
		super();
		setPath(path);
	}
	
	public TaglistMap(Document doc, String path) {
		super();
		if(doc!=null) FromDoc(doc);
		if(path!=null) setPath(path);
	}
	
	public static String SelfTest(Map<String,String> parameters)  {
		String content="BEGIN TaglistMap::SelfTest content - \"";
		ScreenLog.begin("TaglistMap::SelfTest");
		TaglistMap tm=new TaglistMap(parameters.get("path"));
		
		//test 1: go from CSV to document
		//String taglist_csv="COORDINATES: ::: 38.5000000, 126.5000000,,,FILETYPE: ::: Geo Spatial File,,,SIZE: ::: 0.000 TB,,,TITLE: ::: Korea,,,";
		//content+="my list looks like this: "+taglist_csv+"<br />";
		CometProperties mprop=CometProperties.getInstance();
		HCPClient client=null;
		
		try {
			client=new HCPClient(mprop);
			Document doc=XMLHelper.InputStreamToDoc(client.HttpGetHCPContentStream(AnnotationHelper.PathAndAnnotationToURL(mprop.getDestinationRootPath(), parameters.get("path"), "taglist")));
			content+="doc pulled from annotation looks like this: "+XMLHelper.DocToString(doc);
			tm.FromDoc(doc);

			content+="added content name=Title2, value=Dexter";
			tm.AppendtoEnd("Title2", "Dexter");

			//csv to v3 was successful, now try csv to v2
			content+="translated to v3, it looks like this: "+XMLHelper.DocToString(tm.ToDoc());
			content+="translated back to v2, it looks like this: "+XMLHelper.DocToString(tm.ToDocv2());
			content+="translated back to csv, it looks like this: "+tm.toString();
		} catch (Exception e) {
			ScreenLog.ExceptionOutputHandler(e);
		}
		
		content+="\" - END TaglistMap::SelfTest Content";
		ScreenLog.end("TaglistMap::SelfTest");
		return content;
	}
	
	
	public void FromString_v1(String taglistString) {
		String[] collection=taglistString.split(",,,");
		String name="";
		String value="";
		for (int i=0; i<collection.length; i++) {
			if(collection[i].contains(":::")) {
				name=collection[i].split(":::")[0].trim();
				value=collection[i].split(":::")[1].trim();
				if(name.contains(":")) name=name.substring(0,name.indexOf(":")).trim();
				AppendtoEnd(new String(name),new String(value));
			} else {
				ScreenLog.out("\tdid not detect ::: in "+collection[i]);
			}
		}
	}

	public void FromString(String taglistString) throws ParserConfigurationException, SAXException, IOException {
		if(taglistString.contains(":::")) {
			FromString_v1(taglistString);
		} else {
			//v2 or better
			FromDoc(XMLHelper.StringToDoc(taglistString));
		}
	}
	//outer would be ,,, and inner would be :::

	public String toString() {
		return toString(",,,",":::");
	}
	
	public String toString(String sep_outter, String sep_inner) {
		String result="";
		String temp_name="";
		String temp_value="";
		for(int i=0; i<getCount(); i++) {
			temp_name=get(i,"name");
			temp_value=get(i,"value");
			if(temp_name=="" || temp_value=="") continue;
			result+=temp_name+": "+sep_inner+temp_value;
			if(i+1<getCount()) result+=sep_outter;
		}
		return result;
	}

	public void FromDoc(Document doc) {
		//walk the dom and figure out what's what.
		//if there <taglist><tag> has an id attribute, we're version 3
		if(isDocv3(doc)) FromDocv3(doc);
		else {
			FromDocv2(doc);
		}
	}

	private void FromDocv2(Document doc) {
		clear();
		Node node = doc.getFirstChild();
		NodeList nList=node.getChildNodes();
		for (int i = 0; i < nList.getLength(); i++) {
			Node nNode = nList.item(i);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				if(eElement!=null) {
					AppendtoEnd(new String(eElement.getTagName().trim()),new String(eElement.getTextContent().trim()));
				} // end if eelement
			} // end if nnode.getnodetype()
		} //end for int temp
	}

	public Document ToDocv2() throws XMLStreamException, ParserConfigurationException, SAXException, IOException {
		OutputStream os=new ByteArrayOutputStream();
		XMLStreamWriter serializer = XMLOutputFactory.newInstance().createXMLStreamWriter(os, "UTF-8");
		serializer.writeStartDocument("UTF-8", null);
		serializer.writeCharacters("\n\t");
		serializer.writeStartElement("taglist");
		String temp_name="", temp_value="";
		for(int i=0; i<getCount(); i++) {
			serializer.writeCharacters("\n\t");
			if(temp_name=="" || temp_value=="") continue;
			serializer=XMLHelper.CreateSimpleTagAndContent(serializer,temp_name,temp_value);
		}
		serializer.writeCharacters("\n");
		serializer.writeEndElement(); //"taglist");
		// TODO Auto-generated method stub
		return XMLHelper.StringToDoc(os.toString());
	}
	public Document ToDocv3() throws XMLStreamException, ParserConfigurationException, SAXException, IOException {
		OutputStream os=new ByteArrayOutputStream();
		XMLStreamWriter serializer = XMLOutputFactory.newInstance().createXMLStreamWriter(os, "UTF-8");
		serializer.writeStartDocument("UTF-8", null);
		serializer.writeCharacters("\n");
		serializer.writeStartElement("taglist");
		serializer.writeCharacters("\n");
		String temp_name="", temp_value="";
		for(int i=0; i<getCount(); i++) {
			temp_name=get(i,"name");
			temp_value=get(i,"value");
			if(temp_name=="" || temp_value=="") continue;

			serializer=XMLHelper.CreateTagListTuplet(serializer,i,temp_name,temp_value);
		}
		serializer.writeEndElement(); //"taglist");
		return XMLHelper.StringToDoc(os.toString());
	}
	
	public Document ToDoc() throws XMLStreamException, ParserConfigurationException, SAXException, IOException {
		return ToDocv3();
	}
	public static Boolean isDocv3(Document doc) {
		NodeList nList = doc.getElementsByTagName("tag");
		return (nList.getLength()!=0);
	} 
	public static Boolean isDocv2(Document doc) {
		return (!isDocv3(doc));
	} 
	public void FromDocv3(Document doc) {
		NodeList nList = doc.getElementsByTagName("tag");
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				if(eElement!=null) {
					putCouple(Integer.parseInt(eElement.getAttribute("id")),XMLHelper.getTagValue("name",eElement),XMLHelper.getTagValue("value",eElement));
		      } // end if eelement
		   } // end if nnode.getnodetype()
		} //end for int temp
	}
	
	public static TaglistMap DocToTaglistMap(Document doc, String path) {
		TaglistMap tm=new TaglistMap(path);
		tm.FromDocv3(doc);
		return tm;
	}

	public void putCouple(int i, String name, String value) {
		if(name=="" || value=="") return;
		put(i, "name", StringHelper.toTitleCase(name));
		put(i, "value", value);
	}
	
	public void AppendtoEnd(String name, String value) {
		if(name=="" || value=="") return;
		int n=getCount();
		putCouple(n,StringHelper.toTitleCase(name),value);
	}
	public XMLStreamWriter toRows(XMLStreamWriter serializer) throws XMLStreamException {
		String temp_name="", temp_value="";
		for(int i=0; i<getCount(); i++) {
			temp_name=get(i,"name");
			temp_value=get(i,"value");
			if(temp_name=="" || temp_value=="") continue;
			serializer=HTMLHelper.CreateEntireSimpleRow(serializer,temp_name,temp_value);
		}
		return serializer;
	}
	public String get(String name) {
		for(int i=0; i<getCount(); i++) {
			if(getName(i).toLowerCase().equals(name.toLowerCase())) return getValue(i);
		}
		return "";
	}
	
	public String getName(int i) {
		return get(i,"name");
	}
	public String getValue(int i) {
		return get(i,"value");
	}
	
	public int getIndexWithName(String name) {
		for(int i=0; i<getCount(); i++) {
			if(getName(i).toLowerCase().equals(name.toLowerCase())) return i;
		}
		return -1;
	}
	
	public void remove(String key) {
		super.remove(getIndexWithName(key));
	}

	public void remove(int i) {
		super.remove(i);
	}
	public void AppendtoEnd(TaglistMap tm_tomerge_map, String skiplist) {
		for(int i=0; i<tm_tomerge_map.getCount(); i++) {
			//add it, but only if not in the skip list
			if(!skiplist.toLowerCase().contains(tm_tomerge_map.getName(i).toLowerCase()+",")) AppendtoEnd(tm_tomerge_map.getName(i),tm_tomerge_map.getValue(i));
		}
	}
	public String getTitle() {
		for(int i=0; i<getCount(); i++) {
			if(getName(i)!=null && getName(i).toLowerCase().equals("title") && !getValue(i).equals("")) {
				return getValue(i);
			}
		}
		return getPath();
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
}
