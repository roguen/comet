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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.http.entity.InputStreamEntity;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

//TODO: move exceptions out
public class XMLHelper {
	private static ExtendedLogger ScreenLog = new ExtendedLogger(XMLHelper.class.getPackage().getName());
	public static String StringToString(String string) throws TransformerFactoryConfigurationError, TransformerException, ParserConfigurationException, SAXException, IOException {
		return XMLHelper.DocToString(XMLHelper.StringToDoc(string));
	}
	public static String DocToString(Document doc) throws TransformerFactoryConfigurationError, TransformerException {
		if(doc==null) return new String("");
		String xmlString="";
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");

		//initialize StreamResult with File object to save to file	
		StreamResult result = new StreamResult(new StringWriter());
		DOMSource source = new DOMSource(doc);
		transformer.transform(source, result);
		xmlString = result.getWriter().toString();
			
		return xmlString;
	}
	
	public static Document StringToDoc(String xml_content) throws ParserConfigurationException, SAXException, IOException {
		if(xml_content==null || xml_content=="") {
			return null;
		}
		if(xml_content.contains("& ")) {
			ScreenLog.out("WARNING: xml contains ampersand!");
			xml_content=xml_content.replaceAll("&", "&amp;");
		}
		Document doc=null;
		byte[] xml_byte_array;
		xml_byte_array = xml_content.getBytes("UTF-8");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		DocumentBuilder builder = factory.newDocumentBuilder();
		ByteArrayInputStream bastream = new ByteArrayInputStream(xml_byte_array);
		doc = builder.parse(bastream);
		return doc;
	}

	public static Document InputStreamToDoc(InputStream is) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		return dBuilder.parse(is);
	}
	
	public static InputStreamEntity DocToInputStreamEntity(Document doc) throws TransformerFactoryConfigurationError, TransformerException {
		return new InputStreamEntity( new ByteArrayInputStream(DocToString(doc).getBytes()), -1);
	}
	
	//look for a tag sTag in eElement and return the value
	public static String getTagValue(String sTag, Element eElement) {
		if(eElement.getElementsByTagName(sTag)==null) return "";
		if(eElement.getElementsByTagName(sTag).getLength()==0) return "";
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
		if(nlList==null || nlList.getLength()==0) return "";
		Node nValue = (Node) nlList.item(0);
		return nValue.getNodeValue();
	}

	//Get the content of a simple tag
	public static String GetSimpleTagContent(Document doc, String tag) {
		tag=StringHelper.toTitleCase(tag);
		String content="";
		NodeList nList2 = doc.getElementsByTagName(tag);
		if(nList2.getLength()==0) {
			nList2 = doc.getElementsByTagName(tag.toUpperCase());
		}
		if(nList2.getLength()==0) {
			nList2 = doc.getElementsByTagName(tag.toLowerCase());
		}
		if(nList2.getLength()==0) return "";
		for (int temp = 0; temp < nList2.getLength(); temp++) {
			Node nNode2 = nList2.item(temp);
			if (nNode2.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode2;
				if(eElement!=null) {
					  content=eElement.getTextContent();
				} // end if eelement
			} // end if nnode.getnodetype()
		} //end for int temp
		return content;
	}	
	
	public static Node GetSimpleTagNode(Document doc, String tag) {
		NodeList nList2 = doc.getElementsByTagName(tag);
		for (int temp = 0; temp < nList2.getLength(); temp++) {
			Node nNode2 = nList2.item(temp);
			if (nNode2.getNodeType() == Node.ELEMENT_NODE) {
				return nNode2;
			} // end if nnode.getnodetype()
		} //end for int temp
		return null;
	}	

	public static List<Coordinates> ExtractCoordinates(Document doc) {
		List<Coordinates> coordinateList=new ArrayList<Coordinates>();
		NodeList nList = doc.getElementsByTagName("Corner");
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				if(eElement!=null) {
					coordinateList.add(
							new Coordinates(
									eElement.getAttribute("name"),
									getTagValue("X",eElement),
		    			  			getTagValue("Y",eElement)));
		      } // end if eelement
		   } // end if nnode.getnodetype()
		} //end for int temp
		return coordinateList;
	}
	
	public static XMLStreamWriter CreateSimpleTagAndContent(XMLStreamWriter serializer, String tag, String content) throws XMLStreamException {
		return CreateSimpleTagAndContent(serializer,tag,content,true);
	}
	public static XMLStreamWriter CreateSimpleTagAndContent(XMLStreamWriter serializer, String tag, String content, boolean allow_empty) throws XMLStreamException {
		if(!allow_empty && content.equals("")) return serializer;
		serializer.writeStartElement(tag);
		serializer.writeCharacters(content);
		serializer.writeEndElement(); // end simple tag
		return serializer;
	}

	//temporarily brought back in
	public static Map<String, String> DocumentToMap(Document doc) {
		Map<String,String> taglistMap=new HashMap<String,String>();
		NodeList nList = doc.getElementsByTagName("tag");
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				if(eElement!=null) {
					taglistMap.put(eElement.getAttribute("id"),XMLHelper.getTagValue("name",eElement)+"="+XMLHelper.getTagValue("value",eElement));
		      } // end if eelement
		   } // end if nnode.getnodetype()
		} //end for int temp
		return taglistMap;
	}

	public static Map<String, String> SimpleDocumentToMap(Document doc) {
		Map<String,String> simpleMap=new HashMap<String,String>();
		//trim off outter layer
		Node node = doc.getFirstChild();
		NodeList nList=node.getChildNodes();
		for (int i = 0; i < nList.getLength(); i++) {
			Node nNode = nList.item(i);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				if(eElement!=null) {
					simpleMap.put(eElement.getTagName(),eElement.getTextContent());
		      } // end if eelement
		   } // end if nnode.getnodetype()
		} //end for int temp
		return simpleMap;
	}

	public static Map<String, String> NodeListToMap(NodeList nList) {
		Map<String,String> simpleMap=new HashMap<String,String>();
		for (int i = 0; i < nList.getLength(); i++) {
			Node nNode = nList.item(i);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				if(eElement!=null) {
					simpleMap.put(eElement.getTagName(),eElement.getTextContent());
		      } // end if eelement
		   } // end if nnode.getnodetype()
		} //end for int temp
		return simpleMap;
	}

	public static Document AppendContentToEnd(Document doc, String tag, String content) {
		if(tag==null || tag=="" || content==null || content=="") return doc;
		Element e=doc.createElement(tag);
		e.setTextContent(content);
		Node lastone=doc.getLastChild();
		lastone.appendChild(e);
		return doc;
	}
	
	public static XMLStreamWriter CreateTagListTuplet(XMLStreamWriter serializer, int i, String key, String value) throws XMLStreamException {
		if(value==null || key==null) return serializer;
		serializer.writeCharacters("\t");
		serializer.writeStartElement("tag");
		serializer.writeAttribute("id",String.valueOf(i));
		serializer.writeCharacters("\n\t\t");
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "name",key);
		serializer.writeCharacters("\n\t\t");
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "value",value);
		serializer.writeCharacters("\n\t");
		serializer.writeEndElement(); //end tag
		serializer.writeCharacters("\n");
		return serializer;
	}
	
	public static String GetFirstInstance(NodeList nList) {
		for (int i = 0; i < nList.getLength(); i++) {
			Node nNode = nList.item(i);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				if(eElement!=null) return eElement.getTextContent();
		   } // end if nnode.getnodetype()
		} //end for int temp
		return new String("");
	}
	
	public static HashMap<String, String> StringToMap(String str) {
         return StringToMap(str,"\\n","=");
    }

	public static HashMap<String, String> StringToMap(String str, String outter, String inner) {
		//chop the string into tokens using the outter delimiter first, typically \n
        String[] tokens = str.split(outter);
        HashMap<String, String> map = new HashMap<String, String>();
        for(int i=0;i<tokens.length;i++) {
        	//skip comments
        	if(tokens[i].startsWith(";")) continue;
        	
        	//chop the string further by inner delimiter, typically =
            String[] strings = tokens[i].split(inner);
            if(strings.length==2) map.put(strings[0], strings[1]);
        }

        return map;
    }
	
	
}
