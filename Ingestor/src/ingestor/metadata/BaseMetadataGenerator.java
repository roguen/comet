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

import ingestor.SingleFileProcessor;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import com.hdsfed.cometapi.*;

//TODO: consider moving exceptions out
public abstract class BaseMetadataGenerator {
	private static ExtendedLogger ScreenLog = new ExtendedLogger(BaseMetadataGenerator.class.getPackage().getName());

	protected String currentAnnotation="";
	protected SingleFileProcessor mFileProc=null;
	protected String cmdline=null;
	protected File CurrentFile=null;
	// Might want to have these methods explicitly through Exception class to handle internal failures.
	
	public abstract void initialize(Object inInfo);
	
	public abstract String getMetadata(File inSourceFile);
	
	public void setAnnotation(String annotation) {
		// TODO Auto-generated method stub
		currentAnnotation=annotation;
	}

	public Boolean OverrideMetadataExists(File inSourceFile) throws IOException {
		return OverrideMetadataFileName(inSourceFile).exists();
	}
	
	public void OverrideMetadataDelete(File inSourceFile) throws IOException {
		OverrideMetadataFileName(inSourceFile).delete();
	}
		
	public File OverrideMetadataFileName(File inSourceFile) throws IOException {
		String extension=".xml";
		if(!currentAnnotation.equals(CometProperties.getDefaultAnnotation())) extension="."+currentAnnotation+".xml";
		return new File(inSourceFile.getCanonicalPath()+extension);
	}
	
	
	public String getOverrideMetadata(File inSourceFile) {
		//OutputStream outputStream = new ByteArrayOutputStream();
		try {
			File xmlfile=OverrideMetadataFileName(inSourceFile);
			if(!xmlfile.exists()) {
				xmlfile=new File(StringHelper.ChopRt(inSourceFile.getCanonicalPath(),"/")+"/"+currentAnnotation+".xml");
				if(!xmlfile.exists()) {
					return "";
				}
			}
			
			//step 1, read the file into memory
			//Create instance of DocumentBuilderFactory
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		 
			//Get the DocumentBuilder
			DocumentBuilder docBuilder = factory.newDocumentBuilder();
		 
			//Using existing XML Document
			Document doc = docBuilder.parse(xmlfile);
		 
			//set up a transformer
			TransformerFactory transfac = TransformerFactory.newInstance();
			Transformer trans = transfac.newTransformer();
		 
		    //create string from xml tree
		    StringWriter sw = new StringWriter();
		    StreamResult result = new StreamResult(sw);
		    DOMSource source = new DOMSource(doc);
		    trans.transform(source, result);
		    
		    if(CometProperties.getInstance().shouldDeleteSourceFiles()) xmlfile.delete();
		    
		    return sw.toString();
		} catch (Exception e) {
			ScreenLog.ExceptionOutputHandler(e);
		}
		return "";
	}
	
	public String getAnnotation() {
		return currentAnnotation;
	}
	
	public void setParent(SingleFileProcessor parent) {
		mFileProc=parent;
	}

	public void setCommandLine(String _cmdline) {
		// TODO Auto-generated method stub
		cmdline=_cmdline;
	}

	public File getCurrentFile() {
		return CurrentFile;
	}

	public void setCurrentFile(File currentFile) {
		CurrentFile = currentFile;
	}

	
	//TODO: combine with other functions that are similar
	protected String getFileTypeHR(String fileExt) {
		if(fileExt.equals("mp3")) return "MPEG Layer 3 Audio File";
		if(fileExt.equals("m4v")) return "MPEG v4";
		if(fileExt.equals("mkv")) return "Matroska Video File";
		if(fileExt.equals("sid")) return "Lizard Tech MrSID";
		if(fileExt.equals("pdf")) return "Portable Document Format";
		if(fileExt.equals("kml") ||fileExt.equals("kmz")) return "Keyhole Markup Language (Google Earth)";
		if(fileExt.equals("html")) return "Hyper-text Markup Language";
		if(fileExt.equals("txt")) return "Text Document";
		return "Unknown File Type";
	}

	//TODO: there is a better, more standard way, to do this
	protected String getFileExt(File inSourceFile) {
		String filename=inSourceFile.getName();
		if(null==filename || filename.length()==0 || !filename.contains(".")) return new String("");
		String ext=filename.substring(filename.length()-3, filename.length());
		return ext;
	}

	protected String getActionFunc(String substring) {
		if(substring.equals("mp3") || substring.equals("wav")) return "Audio";
		if(substring.equals("m4v") || substring.equals("mp4") || substring.equals("mkv")) return "Video";
		return null;
	}
	
	public static String removeExtension(String filename) {
		ScreenLog.out("removeExtension("+filename+")");
	//    String separator = System.getProperty("file.separator");
	 //   String filename=s;

/*	    // Remove the path upto the filename.
	    int lastSeparatorIndex = s.lastIndexOf(separator);
	    if (lastSeparatorIndex == -1) {
	        filename = s;
	    } else {
	        filename = s.substring(lastSeparatorIndex + 1);
	    }
*/
	    // Remove the extension.
	    int extensionIndex = filename.lastIndexOf(".");
	    if (extensionIndex == -1)
	        return filename;

	    return filename.substring(0, extensionIndex);
	}
	
}
