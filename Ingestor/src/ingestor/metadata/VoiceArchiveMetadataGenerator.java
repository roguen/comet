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
//SVN: r551+

//NOTE: this code was originally developed by Cliff Grimm <clifford.grimm@hds.com>

package ingestor.metadata;

import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.Date;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import com.hdsfed.cometapi.ExtendedLogger;

//TODO: consider moving exceptions out
public class VoiceArchiveMetadataGenerator extends BaseMetadataGenerator {

	private static ExtendedLogger ScreenLog = new ExtendedLogger(VoiceArchiveMetadataGenerator.class.getPackage().getName());
	
	public void initialize(Object inParam) { return; };
	
	public String getMetadata(File inSourceFile) {
		String override=getOverrideMetadata(inSourceFile);
		if(override!="") return override;
		
		OutputStream outputStream = new ByteArrayOutputStream();

		try {
			XMLStreamWriter serializer;

			serializer = XMLOutputFactory.newInstance().createXMLStreamWriter(outputStream, "UTF-8");
			serializer.writeStartDocument("UTF-8", null);
			serializer.writeCharacters("\n");

			String fileName = inSourceFile.getName();
			String CallType = fileName.substring(0, fileName.indexOf('_'));
			int agentIdStartIdx = fileName.indexOf("_A") + 2;
			int agentIdEndIdx = fileName.indexOf('_', agentIdStartIdx);
			String AgentID = fileName.substring(agentIdStartIdx, agentIdEndIdx);
			String RecordingDate = fileName.substring(fileName.lastIndexOf('_') + 1, fileName.lastIndexOf('.'));
			
			// Recording Information
			serializer.writeStartElement("RecordingInfo");
			serializer.writeCharacters("\n    ");
			serializer.writeStartElement("CallType");
			serializer.writeCharacters(CallType);
			serializer.writeEndElement();
			serializer.writeCharacters("\n    ");
			serializer.writeStartElement("AgentId");
			serializer.writeCharacters(AgentID);
			serializer.writeEndElement();
			serializer.writeCharacters("\n    ");
			serializer.writeStartElement("RecordingDate");
			serializer.writeCharacters(RecordingDate);
			serializer.writeEndElement();
			serializer.writeCharacters("\n    ");

			// Source FIle Information
			serializer.writeStartElement("SourceFileInfo");
			serializer.writeCharacters("\n        ");
			serializer.writeStartElement("SourcePath");
			serializer.writeCharacters(inSourceFile.getCanonicalPath());
			serializer.writeEndElement();
			serializer.writeCharacters("\n        ");
			serializer.writeStartElement("Size");
			serializer.writeCharacters(String.valueOf(inSourceFile.length()));
			serializer.writeEndElement();
			serializer.writeCharacters("\n        ");
			serializer.writeEmptyElement("ModificationDate");
			serializer.writeAttribute("EpochTime", String.valueOf(inSourceFile.lastModified()));
			serializer.writeAttribute("LocalTime", 
					DateFormat.getDateTimeInstance().format(new Date(inSourceFile.lastModified())));
			serializer.writeCharacters("\n    ");
			serializer.writeEndElement(); // SourceFileInfo

			serializer.writeCharacters("\n");
			serializer.writeEndElement(); // Recording Info
			serializer.writeEndDocument();
		} catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }
		
		return outputStream.toString();
	}
}
