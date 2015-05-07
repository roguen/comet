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

import java.io.OutputStream;
import java.net.URL;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

//TODO: need function descriptions... etc
public class HTMLHelper {
	private static ExtendedLogger ScreenLog = new ExtendedLogger(HTMLHelper.class.getPackage().getName());
	public static XMLStreamWriter OpenNewHTML(OutputStream outputStream) throws XMLStreamException {
		XMLStreamWriter serializer = XMLOutputFactory.newInstance().createXMLStreamWriter(outputStream, "UTF-8");
		serializer.writeStartDocument("UTF-8", null);
		serializer.writeCharacters("\n");
		serializer.writeStartElement("html");
		serializer.writeStartElement("body");
		return serializer;
	}
	
	public static XMLStreamWriter CloseNewHTML(XMLStreamWriter serializer) throws XMLStreamException {
		serializer.writeEndElement(); // end body
		serializer.writeEndElement(); // end html
		serializer.writeEndDocument();
		return serializer;
	}
	
	public static XMLStreamWriter CreateTable(XMLStreamWriter serializer) throws XMLStreamException {
		return CreateTable(serializer,"","");
	}
	
	public static XMLStreamWriter CreateTable(XMLStreamWriter serializer, String width, String height) throws XMLStreamException {
		serializer.writeStartElement("table");
		serializer.writeAttribute("cellpadding", "1");
		serializer.writeAttribute("cellspacing", "1");
		if(width!=null && width!="") serializer.writeAttribute("width", width);
		if(height!=null && height!="") serializer.writeAttribute("height", height);
		return serializer;
	}

	public static XMLStreamWriter CreateRow(XMLStreamWriter serializer) throws XMLStreamException {
		serializer.writeStartElement("tr");
		return serializer;
	}

	public static XMLStreamWriter CreateColumn(XMLStreamWriter serializer, String w, String h) throws XMLStreamException {
		serializer.writeStartElement("td");
		if(w!="") serializer.writeAttribute("width", w);
		if(h!="") serializer.writeAttribute("height", h);
		return serializer;
	}
	
	public static XMLStreamWriter CreateColumn(XMLStreamWriter serializer) throws XMLStreamException {
		return CreateColumn(serializer,"","");
	}

	public static XMLStreamWriter EndColumn(XMLStreamWriter serializer) throws XMLStreamException {
		serializer.writeEndElement();
		return serializer;
	}
	
	public static XMLStreamWriter EndRow(XMLStreamWriter serializer) throws XMLStreamException {
		serializer.writeEndElement();
		return serializer;
	}
	public static XMLStreamWriter EndTable(XMLStreamWriter serializer) throws XMLStreamException {
		serializer.writeEndElement();
		return serializer;
	}
	
	public static XMLStreamWriter CreateImage(XMLStreamWriter serializer, Map<String,String> attributes)  throws XMLStreamException  {
		ScreenLog.begin("CreateImage(..)");
		serializer.writeStartElement("img");
		for(String key: attributes.keySet()) {
			serializer.writeAttribute(key, attributes.get(key));
		}
		ScreenLog.end("CreateImage(..)");
		return serializer;
	}

	public static XMLStreamWriter CreateSimpleImage(XMLStreamWriter serializer, URL url,int w, int h)  throws XMLStreamException  {
		serializer.writeStartElement("img");
		serializer.writeAttribute("src", url.toString());
		if(w!=0) serializer.writeAttribute("width", String.valueOf(w));
		if(h!=0) serializer.writeAttribute("height", String.valueOf(h));
		//serializer.writeEndElement();
		return serializer;
	}

	public static XMLStreamWriter CreateEntireSimpleRow(XMLStreamWriter serializer, String name, String value) throws XMLStreamException {
		serializer=CreateRow(serializer);
		serializer=CreateColumn(serializer);
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "b", name);
		serializer=EndColumn(serializer);
		serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "td", value);
		serializer=EndRow(serializer);
		return serializer;
	}
}


