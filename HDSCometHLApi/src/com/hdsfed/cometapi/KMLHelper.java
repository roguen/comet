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
import java.net.MalformedURLException;
import java.net.URL;
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

//TODO: need function descriptions; maybe more bookends too
public class KMLHelper {
	private static ExtendedLogger ScreenLog = new ExtendedLogger(KMLHelper.class.getPackage().getName());
	public static final String MDM_TITLE = "Title";
	public static final String MDM_LONGITUDE = "longitude";
	public static final String MDM_LATITUDE = "latitude";
	public static XMLStreamWriter OpenNewKML(OutputStream outputStream) throws XMLStreamException {
		return OpenNewKML(outputStream,"Document");
	}	
	
	public static XMLStreamWriter OpenNewKML(OutputStream outputStream, String container_class) throws XMLStreamException {
		XMLStreamWriter serializer = XMLOutputFactory.newInstance().createXMLStreamWriter(outputStream, "UTF-8");
		serializer.writeStartDocument("UTF-8", null);
		serializer.writeCharacters("\n");
		serializer.writeStartElement("kml");
		serializer.writeAttribute("xmlns","http://www.opengis.net/kml/2.2");
		serializer.writeStartElement(container_class);
		return serializer;
	}
	
	public static XMLStreamWriter CloseNewKML(XMLStreamWriter serializer) throws XMLStreamException {
		serializer.writeEndElement(); // end Document
		serializer.writeEndElement(); // end KML
		serializer.writeEndDocument();
		return serializer;
	}
	
	public static XMLStreamWriter CreateLookAt(XMLStreamWriter serializer, String longitude, String latitude) throws XMLStreamException {
		serializer.writeStartElement("LookAt");
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, MDM_LONGITUDE,longitude);
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, MDM_LATITUDE,latitude);
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "altitude","0");
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "heading","0");
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "tilt","0");
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "range","4000000");
		serializer.writeEndElement(); // LookAt
		return serializer;
	}
	
	public static XMLStreamWriter CreateStartTimeStamp(XMLStreamWriter serializer, Map<String, String> metadataMap) throws XMLStreamException {
		Boolean mode=Boolean.FALSE;
		if(metadataMap.containsKey("startTime")) {
			mode=Boolean.TRUE;
		} else if(!(metadataMap.containsKey("startDate") && metadataMap.containsKey("startHour") && metadataMap.containsKey("startMin"))) {
			return serializer;
		}
		serializer.writeStartElement("TimeStamp");
		if(mode) {
			serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "when",metadataMap.get("startTime"));
		} else {
			serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "when",MetadataParser.MetadataMapToTimeStamp(metadataMap, "startDate", "startHour", "startMinute"));
			
		}
		serializer.writeEndElement(); // TimeStamp
		return serializer;
	}

	public static XMLStreamWriter CreateStyle(XMLStreamWriter serializer, String id, URL iconFullPath) throws XMLStreamException {
		serializer.writeStartElement("Style");
		serializer.writeAttribute("id",id);
		serializer.writeStartElement("IconStyle");
		serializer.writeStartElement("Icon");
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "href",iconFullPath.toString());
		serializer.writeEndElement(); // Icon
		serializer.writeEndElement(); // IconStyle
		serializer.writeStartElement("BalloonStyle");
		//ok to hard code this because $[description] is actually a variable within the KML structure
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "text","$[description]");
		serializer.writeEndElement(); // BalloonStyle
		serializer.writeStartElement("ListStyle");
		serializer.writeStartElement("ItemIcon");
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "href",iconFullPath.toString());
		serializer.writeEndElement(); // ItemIcon
		serializer.writeEndElement(); // ListStyle
		serializer.writeEndElement(); // Style
		return serializer;
	}

	public static XMLStreamWriter CreateGenericPlaceMark(XMLStreamWriter serializer, Map<String, String> placemarkMap) throws XMLStreamException {
		serializer.writeStartElement("Placemark");
		serializer.writeAttribute("id",placemarkMap.get("id"));
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "name",placemarkMap.get("name"));
		serializer.writeStartElement("Snippet");
		serializer.writeAttribute("maxLines","0");
		serializer.writeEndElement(); // empty other than attribute
		serializer.writeStartElement("description");
		serializer.writeCData(placemarkMap.get("balloon_content"));
		serializer.writeEndElement(); // description (contains CDATA)
		if(placemarkMap.containsKey("startDateTime")) {
			serializer.writeStartElement("TimeSpan");
			serializer=XMLHelper.CreateSimpleTagAndContent(serializer,"begin",placemarkMap.get("startDateTime"));
			if(placemarkMap.containsKey("endDateTime")) {
				serializer=XMLHelper.CreateSimpleTagAndContent(serializer,"end",placemarkMap.get("endDateTime"));
			}
			serializer.writeEndElement(); // Timespan
		}
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "styleUrl",placemarkMap.get("style"));
		serializer.writeStartElement("Point");
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "coordinates",placemarkMap.get(MDM_LONGITUDE)+","+placemarkMap.get(MDM_LATITUDE));
		serializer.writeEndElement(); // Point
		serializer.writeEndElement(); // Placemark
		return serializer;
	}

	public static XMLStreamWriter CreateGenericPlaceMarkPair(XMLStreamWriter serializer, Map<String, String> placemarkMap) throws XMLStreamException {
		String temp_id=placemarkMap.get("id");
		//first one expires
		placemarkMap.put("style","#hereNow");
		placemarkMap.put("id", temp_id+"_now");
		serializer=CreateGenericPlaceMark(serializer,placemarkMap);
		//second one starts where the first one leaves off and does not expire (shadow pm)
		placemarkMap.put("startDateTime", placemarkMap.get("endDateTime"));
		placemarkMap.remove("endDateTime");
		placemarkMap.put("style","#hereThen");
		placemarkMap.put("id", temp_id+"_then");
		serializer=CreateGenericPlaceMark(serializer,placemarkMap);
		return serializer;
	}

	private static XMLStreamWriter CreateStyle(XMLStreamWriter serializer, String id, String scale, URL imgsrc) throws XMLStreamException {
		serializer.writeStartElement("Style");
		serializer.writeAttribute("id", id);
		serializer.writeStartElement("BalloonStyle");
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "text", "$[description]");
		serializer.writeEndElement(); //end BalloonStyle
		serializer.writeStartElement("IconStyle");
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "scale", scale);
		serializer.writeStartElement("Icon");
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "href", imgsrc.toString());
		serializer.writeEndElement(); //end Icon
		serializer.writeEndElement(); //end IconStyle
		serializer.writeEndElement(); //end Style
		return serializer;
	}
	
	private static XMLStreamWriter CreateStyleMap(XMLStreamWriter serializer, String styleIDs, String stylemapID) throws XMLStreamException {
		serializer.writeStartElement("StyleMap");
		serializer.writeAttribute("id", stylemapID);
		serializer.writeStartElement("Pair");
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "key", "normal");
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "styleUrl", styleIDs.split(",")[0]);
		serializer.writeEndElement(); //end Pair
		serializer.writeStartElement("Pair");
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "key", "highlight");
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "styleUrl", styleIDs.split(",")[1]);
		serializer.writeEndElement(); //end Pair
		serializer.writeEndElement(); //end StyleMap
		return serializer;
	}

	private static XMLStreamWriter CreateIndividualPlacemark(XMLStreamWriter serializer, Map<String, String> metadataMap) throws XMLStreamException {
		ScreenLog.begin("CreateIndividualPlacemark");
		serializer.writeStartElement("Placemark");
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "name", metadataMap.get(MDM_TITLE));
		serializer.writeStartElement("description");
		serializer.writeCData(metadataMap.get("description"));
		serializer.writeEndElement(); //end description
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "styleUrl", "#styleMap");
		serializer.writeStartElement("Point");
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "coordinates", metadataMap.get(MDM_LONGITUDE)+","+metadataMap.get(MDM_LATITUDE));
		serializer.writeEndElement(); //end Point
		serializer=KMLHelper.CreateLookAt(serializer, metadataMap.get(MDM_LONGITUDE), metadataMap.get(MDM_LATITUDE));
		serializer.writeEndElement(); //end Placemark
		ScreenLog.end("CreateIndividualPlacemark");
		return serializer;
	}

	public static Document GenerateIndividualKML(Map<String, String> metadataMap) throws XMLStreamException, ParserConfigurationException, SAXException, IOException {
		ScreenLog.begin("GenerateIndividualKML");
		//ok,we have coordinates, now create a KML document.
		OutputStream outputStream = new ByteArrayOutputStream();
		XMLStreamWriter serializer=KMLHelper.OpenNewKML(outputStream);
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "name",metadataMap.get(MDM_TITLE));
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "open","1");
		serializer=KMLHelper.CreateStyle(serializer,"style1","1.0",new URL(metadataMap.get("icon_url")));
		serializer=KMLHelper.CreateStyle(serializer,"style2","2.0",new URL(metadataMap.get("icon_url")));
		serializer=KMLHelper.CreateStyleMap(serializer, "style1,style2","styleMap");
		serializer=KMLHelper.CreateIndividualPlacemark(serializer,metadataMap);
		serializer=KMLHelper.CloseNewKML(serializer);
		ScreenLog.end("GenerateIndividualKML");
		return XMLHelper.StringToDoc(outputStream.toString());
	}

	public static ResultMap DocumentToResultMap(Document doc) {
		ScreenLog.begin("KMLHelper::DocumentToResultMap(..)");
		ResultMap rm=new ResultMap();
		NodeList nList = doc.getElementsByTagName("position");
		if(nList.getLength()==0) ScreenLog.out("document empty?");
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				if(eElement!=null) {
					//bad bad bad
					//fix me.. should ingest into RM whatever happens to be there
					rm.put(
							Integer.parseInt(eElement.getAttribute("id")),
							MDM_LONGITUDE,
							XMLHelper.getTagValue(MDM_LONGITUDE,eElement));
					rm.put(
							Integer.parseInt(eElement.getAttribute("id")),
							MDM_LATITUDE,
							XMLHelper.getTagValue(MDM_LATITUDE,eElement));
					rm.put(
							Integer.parseInt(eElement.getAttribute("id")),
							"starttime",
							XMLHelper.getTagValue("starttime",eElement));
					rm.put(
							Integer.parseInt(eElement.getAttribute("id")),
							"endtime",
							XMLHelper.getTagValue("endtime",eElement));
				} // end if eelement
		   } // end if nnode.getnodetype()
		} //end for int temp
		ScreenLog.end("DocumentToResultMap(..)");
		return rm;
	}
	
	public static TaglistMap KMLReducto(String path, Document kml_orig) {
		ScreenLog.begin("KMLReducto");
		TaglistMap tm=new TaglistMap(path);
		//first the easy stuff, grab the name portion at the top

		tm.AppendtoEnd("name", XMLHelper.GetFirstInstance(kml_orig.getElementsByTagName("name")));
		
		//grab the lookAt stuff
		NodeList nl=kml_orig.getElementsByTagName("LookAt");
		NodeList nl2=nl.item(0).getChildNodes();
		Map<String,String> simpleMap=XMLHelper.NodeListToMap(nl2);
		for(String key: simpleMap.keySet()) {
			tm.AppendtoEnd(key, simpleMap.get(key));
		}
		NodeList nl3=kml_orig.getElementsByTagName("Placemark");
		for(int i=0; i<nl3.getLength(); i++)
		tm.AppendtoEnd("placemark"+i, PlaceMarkCSV(nl3.item(i)));
		ScreenLog.end("KMLReducto");
		return tm;
	}
	
	private static String PlaceMarkCSV(Node item) {
		NodeList nl=item.getChildNodes();
		Map<String,String> simpleMap=XMLHelper.NodeListToMap(nl);
		ScreenLog.out(simpleMap);
		return simpleMap.get("name")+", "+simpleMap.get("Point").trim();
	}

	public static String TaglistMaptoMultiKML(TaglistMap tm) throws XMLStreamException, MalformedURLException {
		OutputStream os = new ByteArrayOutputStream();
		XMLStreamWriter serializer=KMLHelper.OpenNewKML(os);
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "name",tm.get("name"));
		//look at for overall
		serializer.writeStartElement("LookAt");
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "longitude",tm.get("longitude"));
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "latitude",tm.get("latitude"));
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "altitude",tm.get("altitude"));
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "heading",tm.get("heading"));
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "tilt",tm.get("tilt"));
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "range",tm.get("range"));
		serializer.writeEndElement(); // LookAt

		//stylemap
		serializer.writeStartElement("Style");
		serializer.writeAttribute("id", "styleMap");
		serializer.writeStartElement("IconStyle");
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "scale","2");
		serializer.writeStartElement("Icon");
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "href",new URL(tm.get("icon_url")).toString());
		serializer.writeEndElement(); // Icon
		serializer.writeEndElement(); // IconStyle
		
		serializer.writeStartElement("BalloonStyle");
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "text","$[description]");
		serializer.writeEndElement(); // BalloonStyle
		serializer.writeEndElement(); // Style
		
		//Document newkml=;
		String[] list=null;
		String longitude="";
		for(int i=7; i<tm.getCount(); i++) {
			list=tm.getValue(i).split(",");
			//longitude
			if(list[1].contains("absolute")) {
				longitude=list[1].split("e")[1].trim();
			} else {
				longitude=list[1].trim();
			}
			
			serializer.writeStartElement("Placemark");
			serializer.writeAttribute("id",String.valueOf(i-7));
			serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "name",list[0]);

			serializer.writeStartElement("Snippet");
			serializer.writeAttribute("maxLines","2");
			serializer.writeCharacters("Current Position");
			serializer.writeEndElement(); //snippet
			serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "styleUrl","#styleMap");
			serializer.writeStartElement("Point");
			serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "altitudeMode","absolute");
			serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "coordinates",longitude+","+list[2].trim()+","+list[3].trim());
			serializer.writeEndElement(); // Point
			serializer.writeEndElement(); // Placemark
		}
		serializer=KMLHelper.CloseNewKML(serializer);
		return os.toString();
	}
}
