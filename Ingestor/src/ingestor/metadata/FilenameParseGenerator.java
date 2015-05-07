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
package ingestor.metadata;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import com.hdsfed.cometapi.CometProperties;
import com.hdsfed.cometapi.ExtendedLogger;
import com.hdsfed.cometapi.TaglistMap;
import com.hdsfed.cometapi.XMLHelper;

//TODO: consider moving exceptions out
public class FilenameParseGenerator extends BaseMetadataGenerator {
	private static ExtendedLogger ScreenLog = new ExtendedLogger(FilenameParseGenerator.class.getName());
	public void initialize(Object inParam) { return; };
	
	public String getMetadata(File inSourceFile) {
		ScreenLog.setClass(FilenameParseGenerator.class.getName());
		setCurrentFile(inSourceFile);
		//TaglistGenerator is an implementation of override, so no need to return.
		String override=getOverrideMetadata(inSourceFile);
		
		if(!inSourceFile.toString().endsWith(".txt") && !inSourceFile.toString().endsWith(CometProperties.getInstance().getMetadataContentExtension())) return override;
		if(!inSourceFile.toString().contains(" - ")) return override;
		
		
		if(override.equals("")) {
			OutputStream outputStream = new ByteArrayOutputStream();

			try {
				XMLStreamWriter serializer;

				serializer = XMLOutputFactory.newInstance().createXMLStreamWriter(outputStream, "UTF-8");
				serializer.writeStartDocument("UTF-8", null);
				serializer.writeCharacters("\n");
				serializer.writeStartElement("DETAILS");
				serializer.writeCharacters("\n    ");
				serializer.writeStartElement("Description");
				serializer.writeCharacters(removeExtension(inSourceFile.getName()).split(" - ")[1]);
				serializer.writeEndElement();
				serializer.writeCharacters("\n    ");
				serializer.writeStartElement("Season");
				serializer.writeCharacters(inSourceFile.getName().split(" - ")[0].split("E")[0].split("S")[1]);
				serializer.writeEndElement();
				serializer.writeCharacters("\n    ");
				serializer.writeStartElement("Episode");
				serializer.writeCharacters(inSourceFile.getName().split(" - ")[0].split("E")[1]);
				serializer.writeEndElement();
				serializer.writeCharacters("\n    ");
				serializer.writeEndElement();
				serializer.writeCharacters("\n");
				serializer.writeEndDocument();
			}
			catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }
			override=outputStream.toString();
		}
		
		return override;
/*		try {
			TaglistMap tm=new TaglistMap(XMLHelper.StringToDoc(override),inSourceFile.getAbsolutePath());
			return XMLHelper.DocToString(tm.ToDocv3());
		} catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }
		return "";*/
	}
}
