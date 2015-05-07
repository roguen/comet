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
package ingestor.metadata;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.hdsfed.cometapi.AnnotationHelper;
import com.hdsfed.cometapi.CometProperties;
import com.hdsfed.cometapi.ExtendedLogger;
import com.hdsfed.cometapi.HCPClient;

//TODO: Plenty of room for improvement.
//Executing this class should be optional
// need standardization of communication with HCP
public class NoteGenerator extends BaseMetadataGenerator {
	private static ExtendedLogger ScreenLog = new ExtendedLogger(NoteGenerator.class.getName());

	public enum noteType {
		TN_LOCAL_NOTE,        // object.txt
		TN_URL_NOTE,   //url on HCP: /path/to/object.txt  -- previously ingested and deleted locally
		TN_UNSUPPORTED
	}
	
	public void initialize(Object inParam) { return; };


	
//	private File GenerateThumbnailTarget(File inSourceFile, String suffix) {
//		//next, add the path to the file, minus the working path (proposed path on HCP)
//		//to calculate this path, consider the substring from "length of working path" to end
//		
//		if(suffix.equals("")) suffix=".thumb.png";
//		return AnnotationHelper.FilePathToHCPPath(inSourceFile,"/tmp/ingest-tempdir"+mFileProc.getPathPrefix(), mFileProc.getWorkingPath(), suffix);
//	}

	URL GetURLNote(File f) throws MalformedURLException, IOException {
		URL myurl=AnnotationHelper.PathToURL(CometProperties.getInstance().getDestinationRootPath(),
				AnnotationHelper.FilePathToHCPPath(GetLocalNote(f),mFileProc.getPathPrefix(), mFileProc.getWorkingPath()).toString());
		return myurl;
	}
	
	File GetLocalNote(File f) {
		
		ScreenLog.out("filepath: "+f.getAbsolutePath());
		
		ScreenLog.out("returning: "+removeExtension(f.getAbsolutePath())+".txt");
		return new File(removeExtension(f.getAbsolutePath())+".txt");
	}
	

	//TODO: replace error condition with Exception
	private noteType GetNoteType(File f) throws MalformedURLException, IOException, URISyntaxException {
		ScreenLog.begin("GetNoteType("+f+")");
		
		if(f.toString().endsWith(".txt")) return noteType.TN_UNSUPPORTED;

		
		HCPClient client=new HCPClient(CometProperties.getInstance());
		if(GetLocalNote(f).exists()) return noteType.TN_LOCAL_NOTE;
		if(client.HCPObjectExists(GetURLNote(f))) return noteType.TN_URL_NOTE; 

		ScreenLog.out("\t(NOTE) file: "+f+" is not supported.. apparently");
		ScreenLog.end("GetNoteType("+f+")");
		return noteType.TN_UNSUPPORTED;
	}
	
	//path in this sense is either a url or a path on HCP
	public String formMetadata(File file ) throws ParserConfigurationException, TransformerException {
		ScreenLog.begin("note generator - formMetadata("+file+")");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		//Get the DocumentBuilder
		DocumentBuilder docBuilder = factory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("NOTE");
		Element subElement=doc.createElement("NOTE_URL");
	 	subElement.setTextContent(AnnotationHelper.URIEncoder(file.toString()));
		rootElement.appendChild(subElement);
		
		Element subElement2=doc.createElement("NOTE_UTF8NAME");
	 	subElement2.setTextContent(file.toString());
		rootElement.appendChild(subElement2);
	
		doc.appendChild(rootElement);
		//set up a transformer
		TransformerFactory transfac = TransformerFactory.newInstance();
		Transformer trans = transfac.newTransformer();
	    //create string from xml tree
	    StringWriter sw = new StringWriter();
	    StreamResult result = new StreamResult(sw);
	    DOMSource source = new DOMSource(doc);
	    trans.transform(source, result);
		ScreenLog.end("formMetadata("+file+") == \"" + sw.toString()+"\"");
	    return sw.toString();
	}
	
	//TODO: move exceptions out
	public String getMetadata(File inSourceFile) {
		ScreenLog.setClass(NoteGenerator.class.getName());
		String override=getOverrideMetadata(inSourceFile);
		if(override!="") return override;
		
		try {
			switch(GetNoteType(inSourceFile)) {
			case TN_URL_NOTE:
			case TN_LOCAL_NOTE:
				return formMetadata(AnnotationHelper.FilePathToHCPPath(GetLocalNote(inSourceFile),mFileProc.getPathPrefix(), mFileProc.getWorkingPath()));
			case TN_UNSUPPORTED:
			default:
				break;
			}
		} catch (Exception e) {
			ScreenLog.ExceptionOutputHandler(e);
		}
		return "";
	}
}
