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

import org.farng.mp3.MP3File;
import org.farng.mp3.id3.ID3v1;

import com.hdsfed.cometapi.ExtendedLogger;
import com.hdsfed.cometapi.XMLHelper;

//TODO: need to support more ID3 tags
public class MP3InfoGenerator extends BaseMetadataGenerator {
	private static ExtendedLogger ScreenLog = new ExtendedLogger(MP3InfoGenerator.class.getName());
	public void initialize(Object inParam) { return; };
	public String getMetadata(File inSourceFile) {
		ScreenLog.begin("MP3INFOGen");
		String override=getOverrideMetadata(inSourceFile);
		if(override!="") return override;
		if(!getFileExt(inSourceFile).equals("mp3")) return "";
		OutputStream outputStream = new ByteArrayOutputStream();
		try {
			XMLStreamWriter serializer;
			serializer = XMLOutputFactory.newInstance().createXMLStreamWriter(outputStream, "UTF-8");
			serializer.writeStartDocument("UTF-8", null);
			serializer.writeCharacters("\n");
			serializer.writeStartElement("MP3Info");
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
			serializer=XMLHelper.CreateSimpleTagAndContent(serializer,"Title",getMp3Title(inSourceFile).trim(),false);
			serializer=XMLHelper.CreateSimpleTagAndContent(serializer,"Album",getMp3Album(inSourceFile).trim(),false);
			serializer=XMLHelper.CreateSimpleTagAndContent(serializer,"Artist",getLeadArtist(inSourceFile).trim(),false);
			serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "Year", getYear(inSourceFile).trim(),false);
			serializer=XMLHelper.CreateSimpleTagAndContent(serializer, "Comment", getComment(inSourceFile).trim(),false);
			serializer.writeEndElement(); // Mp3Info
			serializer.writeCharacters("\n");
			serializer.writeEndDocument();
		}
		catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }
		ScreenLog.fine(outputStream.toString());
		ScreenLog.end("MP3INFOGen");
		return outputStream.toString();
	}

	private String getActionLink(String name, String fileExt) {
		if(fileExt.equals("kml") || fileExt.equals("kmz")) {
				return "javascript:GoogleControlsLoadKMLviaNetworkLinkByRLU(\'"+name+"\')";
		}
		return null;
	}

	private String getComment(File inSourceFile) {
		MP3File mp3file;
		String title=new String("");
		try {
			mp3file = new MP3File(inSourceFile);
			ID3v1 tag=new ID3v1();
			tag=(ID3v1) mp3file.getID3v1Tag();
			title=tag.getComment();
		} catch (Exception ignored) { } 
		return title;
	}

	private String getYear(File inSourceFile) {
		MP3File mp3file;
		String title=new String("");
		try {
			mp3file = new MP3File(inSourceFile);
			ID3v1 tag=new ID3v1();
			tag=(ID3v1) mp3file.getID3v1Tag();
			title=tag.getYear();
		} catch (Exception ignored) { }
		return title;
	}

	private String getMp3Title(File inSourceFile) {
		MP3File mp3file;
		String title=new String("");
		try {
			mp3file = new MP3File(inSourceFile);
			ID3v1 tag=new ID3v1();
			tag=(ID3v1) mp3file.getID3v1Tag();
			title=tag.getSongTitle();
		} catch (Exception ignored) {} 
		return title;
	}

	private String getMp3Album(File inSourceFile) {
		MP3File mp3file;
		String title=new String("");
		try {
			mp3file = new MP3File(inSourceFile);
			ID3v1 tag=new ID3v1();
			tag=(ID3v1) mp3file.getID3v1Tag();
			title=tag.getAlbumTitle();
		} catch (Exception ignored) {}
		return title;
	}
	
	private String getLeadArtist(File inSourceFile) {
		MP3File mp3file;
		String title=new String("");
		try {
			mp3file = new MP3File(inSourceFile);
			ID3v1 tag=new ID3v1();
			tag=(ID3v1) mp3file.getID3v1Tag();
			title=tag.getLeadArtist();
		} catch (Exception ignored) {} 
		return title;
	}

}
