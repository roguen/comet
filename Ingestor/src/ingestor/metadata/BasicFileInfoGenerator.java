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
//Package: COMET::Data Ingestor Service
//Author: Chris Delezenski <chris.delezenski@hdsfed.com>
//Compilation Date: 2015-05-06
//License: Apache License, Version 2.0
//Version: 1.21.0
//(RPM) Release: 1
//SVN: r554

//NOTE: this code was originally developed by Cliff Grimm <clifford.grimm@hds.com>

package ingestor.metadata;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import com.hdsfed.cometapi.ExtendedLogger;

//TODO: move exceptions out
public class BasicFileInfoGenerator extends BaseMetadataGenerator {
	private static ExtendedLogger ScreenLog = new ExtendedLogger(BasicFileInfoGenerator.class.getName());
	public void initialize(Object inParam) { return; };
	
	public String getMetadata(File inSourceFile) {
		ScreenLog.setClass(BasicFileInfoGenerator.class.getName());
		String override=getOverrideMetadata(inSourceFile);
		if(override!="") return override;
		
		OutputStream outputStream = new ByteArrayOutputStream();

		try {
			XMLStreamWriter serializer;

			serializer = XMLOutputFactory.newInstance().createXMLStreamWriter(outputStream, "UTF-8");
			serializer.writeStartDocument("UTF-8", null);
			serializer.writeCharacters("\n");
			serializer.writeStartElement("SourceFileInfo");
			serializer.writeCharacters("\n    ");
			serializer.writeStartElement("Path");
			serializer.writeCharacters(inSourceFile.getCanonicalPath());
			serializer.writeEndElement();
			serializer.writeCharacters("\n    ");
			serializer.writeStartElement("Size");
			serializer.writeCharacters(String.valueOf(inSourceFile.length()));
			serializer.writeEndElement();
			serializer.writeCharacters("\n    ");
			serializer.writeStartElement("FileType");
			serializer.writeCharacters(getFileTypeHR(getFileExt(inSourceFile)));
			serializer.writeEndElement();
			serializer.writeCharacters("\n    ");
			String action_func=getActionFunc(getFileExt(inSourceFile));
			
			if(null!=action_func) {
				serializer.writeStartElement("ACTION_FUNC");
				serializer.writeCharacters(action_func);
				serializer.writeEndElement();
				serializer.writeCharacters("\n    ");
			}
			
			
			String link=getActionLink(inSourceFile.getName(), getFileExt(inSourceFile));
			
			if(null!=link) {
				serializer.writeStartElement("LINK");
				serializer.writeCharacters(link);
				serializer.writeEndElement();
				serializer.writeCharacters("\n    ");
				serializer.writeStartElement("LINK_LABEL");
				serializer.writeCharacters("load");
				serializer.writeEndElement();
				serializer.writeCharacters("\n    ");
				
			}
			
		
			serializer.writeEmptyElement("ModificationDate");
			serializer.writeAttribute("EpochTime", String.valueOf(inSourceFile.lastModified()));
			serializer.writeAttribute("ISO8601Time", "Put-ISO-Time-Here");
			serializer.writeCharacters("\n");
			serializer.writeEndElement(); // SourceFileInfo
			serializer.writeCharacters("\n");
			serializer.writeEndDocument();
		}
		catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }
		return outputStream.toString();
	}
	private String getActionLink(String name, String fileExt) {
		if(fileExt.equals("kml") || fileExt.equals("kmz")) {
				return "javascript:GoogleControlsLoadKMLviaNetworkLinkByRLU(\'"+name+"\')";
		}
		return null;
	}
	
}
