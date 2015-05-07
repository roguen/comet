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
public class ThumbnailMetadata extends BaseMetadataGenerator {
	private static ExtendedLogger ScreenLog = new ExtendedLogger(ThumbnailMetadata.class.getPackage().getName());

	public enum thumbnailType {
		TN_LOCAL_JPG,        // object.ext.thumb.jpg
		TN_LOCAL_PNG,        // object.ext.thumb.png
		TN_GENERATEDPNG,    // generated using gdal_translate or ImageMagik's convert utility
		TN_LOCAL_MASTER,  // local copy of thumb.png in the same directory as the object
		TN_URL_MASTER,   //url on HCP: /path/to/thumb.png  -- previously ingested and deleted locally
		TN_URL_PNG,   //url on HCP: /path/to/thumb.png  -- previously ingested and deleted locally
		TN_URL_JPG,   //url on HCP: /path/to/thumb.png  -- previously ingested and deleted locally
		TN_UNSUPPORTED
	}
	
	private File generatedThumbnail=null;
	public void initialize(Object inParam) { return; };

	//TODO: replace with library code
	private boolean SystemThree(String cmd) {
		ScreenLog.begin("SystemThree("+cmd+")");
		Runtime run = Runtime.getRuntime() ;
		Process pr = null;
		try {
			pr=run.exec(cmd) ;
			pr.waitFor() ;
			
		} catch (InterruptedException | IOException e) {
			// TODO Auto-generated catch block
			ScreenLog.ExceptionOutputHandler(e);
		}
		ScreenLog.end("SystemThree("+cmd+") = "+pr.exitValue());
		return pr.exitValue()==0;
	}
	//FIXME
	private boolean GdalTranslate(File inSourceFile, File tgtFile) {
		ScreenLog.out("gdal_translate("+inSourceFile+","+tgtFile);
		return SystemThree("/usr/bin/gdal_translate -of PNG "+inSourceFile.getAbsolutePath()+" "+tgtFile.getAbsolutePath());
	}
	//FIXME
	public boolean ImageMagickConvert(File srcFile, File tgtFile) {
		ScreenLog.out("about to execute:");
		String temp=srcFile.toString();
		if(temp.endsWith(".pdf")) temp+="[0]";
		ScreenLog.out("ImageMagick convert("+temp+","+tgtFile);
		return SystemThree("/usr/bin/convert -resize 400 "+temp+" "+tgtFile);
	}

	//TODO: change to make use of CSV from comet.properties
	public boolean isCompatibleGeoSpatialFile(File f) {
		return (f.toString().toLowerCase().endsWith(".dt1") ||  //DTED, compatible with gdal_translate
				f.toString().toLowerCase().endsWith(".i21") || //NTIFF, compatible with gdal_translate
				f.toString().toLowerCase().endsWith(".i41") || //NTIFF, compatible with gdal_translate
				f.toString().toLowerCase().endsWith(".i42")); //|| //NTIFF, compatible with gdal_translate
//				f.toString().toLowerCase().endsWith(".sid")); // LizardTech MrSID, compatible with gdal_translate	
	} //currently rejecting tiff and hgt
	
	//TODO: change to make use of CSV from comet.properties
	public boolean hasEmbeddedThumbnail(File f) {
		return (
				f.toString().toLowerCase().endsWith(".pdf") || //pdf
				f.toString().toLowerCase().endsWith(".jpg") || //image
				f.toString().toLowerCase().endsWith(".png") || //image
				f.toString().toLowerCase().endsWith(".tif") || //image
//				f.toString().toLowerCase().endsWith(".fits") || //image
				isCompatibleGeoSpatialFile(f));
	}

	//TODO: delete functions do not respect readonly rule
	public boolean CreateThumbnail(File inSourceFile) throws IOException {
		ScreenLog.begin("CreateThumbnanil("+inSourceFile+")");
		//if it's not pdf, we can't handle it yet

		generatedThumbnail=GenerateThumbnailTarget(inSourceFile,"");
		File newthumbnail_containerdir=new File(generatedThumbnail.getParent());
		if(!newthumbnail_containerdir.exists()) {
			if(!newthumbnail_containerdir.mkdirs()) {
				ScreenLog.out("failed to make directory");
			}
		}
		
		//already exists? delete it
		//could be a problem if multiple files have the same name
		if(generatedThumbnail.exists()) {
			generatedThumbnail.delete();
		}
		
		//convert -resize 400 inSourceFile[0] newthumbnail
		File srcFile=inSourceFile;
		
		if(isCompatibleGeoSpatialFile(inSourceFile)) {
			//tricky, the srcFile here will be the output of gdal_translate
			int count=0;
			srcFile=GenerateThumbnailTarget(inSourceFile, ".thumb-input-"+count+".png");
			//do extra action to convert to png
			while (srcFile.exists()) {
				ScreenLog.out("srcFile="+srcFile+" already exists, trying a new one");
				srcFile=GenerateThumbnailTarget(inSourceFile, ".thumb-input-"+count+".png");
				count++;
			}
			
			if(!GdalTranslate(inSourceFile,srcFile)) return false;
			//srcFile should be the output of gdal_translate
		} else {
			//just push it up
			//setup command line to create thumbnail
			if(!hasEmbeddedThumbnail(inSourceFile)) {
				//copy the master thumbnail instead of running imagemagik
				srcFile=new File(inSourceFile.getParent()+"/thumb.png");
			}
		}
		ScreenLog.out("executing convert on "+srcFile+" to "+generatedThumbnail);
		Boolean ret=ImageMagickConvert(srcFile,generatedThumbnail);
		if(srcFile.toString().contains("ingest-tempdir")) {
			ScreenLog.out("would have deleted file: "+srcFile);
			srcFile.delete();
		}
		
		ScreenLog.end("CreateThumbnanil("+inSourceFile+")");
		return ret;
	}
	
	private File GenerateThumbnailTarget(File inSourceFile, String suffix) {
		//next, add the path to the file, minus the working path (proposed path on HCP)
		//to calculate this path, consider the substring from "length of working path" to end
		
		if(suffix.equals("")) suffix=".thumb.png";
		return AnnotationHelper.FilePathToHCPPath(inSourceFile,"/tmp/ingest-tempdir"+mFileProc.getPathPrefix(), mFileProc.getWorkingPath(), suffix);
	}

	File GetLocalPNG(File f) {
		return new File(f.getAbsolutePath()+".thumb.png");
	}

	URL GetURLPNG(File f) throws MalformedURLException, IOException {
		URL myurl=AnnotationHelper.PathToURL(CometProperties.getInstance().getDestinationRootPath(),
				AnnotationHelper.FilePathToHCPPath(GetLocalPNG(f),mFileProc.getPathPrefix(), mFileProc.getWorkingPath()).toString());
		return myurl;
	}
	URL GetURLJPG(File f) throws MalformedURLException, IOException {
		URL myurl=AnnotationHelper.PathToURL(CometProperties.getInstance().getDestinationRootPath(),
				AnnotationHelper.FilePathToHCPPath(GetLocalJPG(f),mFileProc.getPathPrefix(), mFileProc.getWorkingPath()).toString());
		return myurl;
	}

	
	File GetLocalJPG(File f) {
		return new File(f.getAbsolutePath()+".thumb.jpg");
	}
	
	File GetLocalMaster(File f) {
		return new File(f.getParentFile()+"/thumb.png");
	}

	URL GetURLMaster(File f) throws MalformedURLException, IOException {
		URL urlmaster=AnnotationHelper.PathToURL(CometProperties.getInstance().getDestinationRootPath(),
				AnnotationHelper.FilePathToHCPPath(
						GetLocalMaster(f),
						mFileProc.getPathPrefix(),
						mFileProc.getWorkingPath()).toString());
		return urlmaster;
	}

	//TODO: replace error condition with Exception
	private thumbnailType GetThumbnailType(File f) throws MalformedURLException, IOException, URISyntaxException {
		ScreenLog.begin("GetThumbnailType("+f+")");
		HCPClient client=new HCPClient(CometProperties.getInstance());
		if(GetLocalJPG(f).exists()) return thumbnailType.TN_LOCAL_JPG;
		if(GetLocalPNG(f).exists()) return thumbnailType.TN_LOCAL_PNG;
		if(client.HCPObjectExists(GetURLPNG(f))) return thumbnailType.TN_URL_PNG; 
		if(client.HCPObjectExists(GetURLJPG(f))) return thumbnailType.TN_URL_JPG; 
		if(hasEmbeddedThumbnail(f)) return thumbnailType.TN_GENERATEDPNG;
		if(GetLocalMaster(f).exists()) return thumbnailType.TN_LOCAL_MASTER;
		if(client.HCPObjectExists(GetURLMaster(f))) return thumbnailType.TN_URL_MASTER; 

		ScreenLog.out("\tfile: "+f+" is not supported.. apparently");
		ScreenLog.end("GetThumbnailType("+f+")");
		return thumbnailType.TN_UNSUPPORTED;
	}
	
	//path in this sense is either a url or a path on HCP
	public String formMetadata(File file ) throws ParserConfigurationException, TransformerException {
		ScreenLog.begin("formMetadata("+file+")");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		//Get the DocumentBuilder
		DocumentBuilder docBuilder = factory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("THUMBNAIL");
		Element subElement=doc.createElement("THUMBNAIL_URL");
	 	subElement.setTextContent(file.toString());
		rootElement.appendChild(subElement);
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
		ScreenLog.setClass(ThumbnailMetadata.class.getName());
		String override=getOverrideMetadata(inSourceFile);
		if(override!="") return override;
		
		try {
			switch(GetThumbnailType(inSourceFile)) {
			case TN_URL_JPG:
			case TN_LOCAL_JPG:
				return formMetadata(AnnotationHelper.FilePathToHCPPath(GetLocalJPG(inSourceFile),mFileProc.getPathPrefix(), mFileProc.getWorkingPath()));
			case TN_GENERATEDPNG:
				if(CreateThumbnail(inSourceFile)) {
					String ending=".thumb.png";
					URL encodedPathURL=AnnotationHelper.FSToURLPath(CometProperties.getInstance().getDestinationRootPath(mFileProc.getPathPrefix()), mFileProc.getWorkingPath(),new File(inSourceFile.toString()+ending));
					//replace with library call (eventually)
					ScreenLog.out("--->writing generated thumbnail: "+generatedThumbnail);
					ScreenLog.out("--->to HCP as URL:"+encodedPathURL.toString());
					mFileProc.WriteObjectToHCP(encodedPathURL, generatedThumbnail);
				} else {
					return "";
				}
			case TN_URL_PNG:
			case TN_LOCAL_PNG:
				return formMetadata(AnnotationHelper.FilePathToHCPPath(GetLocalPNG(inSourceFile),mFileProc.getPathPrefix(), mFileProc.getWorkingPath()));
			case TN_LOCAL_MASTER:
			case TN_URL_MASTER:
				return formMetadata(AnnotationHelper.FilePathToHCPPath(GetLocalMaster(inSourceFile),mFileProc.getPathPrefix(), mFileProc.getWorkingPath()));
			case TN_UNSUPPORTED:
			default:
				break;
			}
		} catch (Exception e) {
			ScreenLog.ExceptionOutputHandler(e);
		}
		return "";
	}

	public File getGeneratedThumbnail() {
		return generatedThumbnail;
	}

	public void setGeneratedThumbnail(File generatedThumbnail) {
		this.generatedThumbnail = generatedThumbnail;
	}
}
