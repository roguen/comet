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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.hdsfed.cometapi.AnnotationHelper;
import com.hdsfed.cometapi.CometProperties;
import com.hdsfed.cometapi.CometStorageURI;
import com.hdsfed.cometapi.ExtendedLogger;
import com.hdsfed.cometapi.HCPClient;
import com.hdsfed.cometapi.StringHelper;
import com.hdsfed.cometapi.ThreadTrackerDB;
import com.hdsfed.cometapi.XMLHelper;

//TODO: Plenty of room for improvement.
//Executing this class should be optional
// need standardization of communication with HCP
public class VideoTranscoder extends BaseMetadataGenerator {
	private static ExtendedLogger ScreenLog = new ExtendedLogger(VideoTranscoder.class.getPackage().getName());
	
	public enum videoType {
		VIDEO_LOCAL_MKV,        // object.mkv
		VIDEO_LOCAL_M4V,        // object.m4v
		VIDEO_LOCAL_MP4,    // object.mp4
		VIDEO_UNSUPPORTED
	}
	
//	private File generatedVideo=null;
	public void initialize(Object inParam) { return; };

	//TODO: replace with library code
	

//	private boolean SystemThree(String cmd) {
//		ScreenLog.begin("SystemThree("+cmd+")");
//		Runtime run = Runtime.getRuntime() ;
//		Process pr = null;
//
//		try {
//			pr=run.exec(cmd) ;
//			BufferedReader buf = new BufferedReader( new InputStreamReader( pr.getInputStream() ) ) ;
//			BufferedReader buf_stderr=new BufferedReader( new InputStreamReader(pr.getErrorStream() ));
//			
//			// read everything and output to outputStream as you go
//			String stdout=null;
//			String stderr=null;
//			
//			Boolean quit=false;
//			while(!quit) {
//				stdout = buf.readLine();
//				stderr = buf_stderr.readLine();
//				if(stdout!=null) ScreenLog.out(stdout);
//				if(stderr!=null) ScreenLog.out(stderr);
//				
//				if(stdout==null && stderr==null) quit=true;
//			}
//
//			
//			
//			pr.waitFor() ;
//			
//		} catch (InterruptedException | IOException e) {
//			// TODO Auto-generated catch block
//			ScreenLog.ExceptionOutputHandler(e);
//		}
//		ScreenLog.end("SystemThree("+cmd+") = "+pr.exitValue());
//		return pr.exitValue()==0;
//	}
//	
//	@SuppressWarnings("unused")
//	private String PopenThree(String cmdline, File f) {
//		return StringHelper.Popen3(cmdline,f.getAbsolutePath(),true,true);
//	}	
//	private String PopenThree(String cmdline, Boolean useStdout, Boolean useStderr) {
//		ScreenLog.begin("PopenThree("+cmdline+")");
//		OutputStream outputStream = new ByteArrayOutputStream();
//		try {
//				ScreenLog.out("run command: " + cmdline);
//				Runtime run = Runtime.getRuntime() ;
//				Process pr = run.exec(cmdline) ;
//				BufferedReader buf = new BufferedReader( new InputStreamReader( pr.getInputStream() ) ) ;
//				BufferedReader buf_stderr=new BufferedReader( new InputStreamReader(pr.getErrorStream() ));
//				
//				// read everything and output to outputStream as you go
//				String stdout=null;
//				String stderr=null;
//				
//				Boolean quit=false;
//				while(!quit) {
//					stdout = buf.readLine();
//					
//					stderr = buf_stderr.readLine();
//					if(stdout!=null) {
//						stdout+="\n";
//						if(useStdout) outputStream.write(stdout.getBytes());
//							ScreenLog.out("\n\tstdout:::"+stdout);
//					}
//					if(stderr!=null) {
//							if(useStderr) outputStream.write(stderr.getBytes());
//							ScreenLog.out("\n\tstderr:::"+stderr);
//					}
//					
//					if(stdout==null && stderr==null) quit=true;
//				}
//				
//				
//				pr.waitFor() ;
//				d
//				String errcode="Exit value: "+pr.exitValue();
//				ScreenLog.out(errcode);
//		} catch (Exception e) {
//			ScreenLog.ExceptionOutputHandler(e);
//		}
//		return outputStream.toString();
//	}
	
	//FIXME
	private boolean VideoTranscode(File inSourceFile, File tgtFile) throws IOException, InterruptedException {
		ScreenLog.begin("VideoTranscode("+inSourceFile+","+tgtFile+")");
		ThreadTrackerDB.updateDBOverHTTP(inSourceFile.getAbsolutePath()+"-transcoding-video",mFileProc.getThreadID(), inSourceFile.length());

		
		//if(!tgtFile.getParentFile().mkdirs()) return false;
		
		
		String cmdline="/opt/COMETDist/bin/ffmpeg -i "+StringHelper.SRC_FILENAME_CONST+
				" -c:v libvpx"+
				" -b:v 1M"+
				" -c:a libvorbis "+StringHelper.TGT_FILENAME_CONST;
		
		//ScreenLog.out("\n\nexists: "+inSourceFile.getAbsolutePath()+" ::: "+inSourceFile.exists());
		
		//String cmdline="/usr/bin/comet_vidtrans.sh "+inSourceFile.getAbsolutePath();
		ScreenLog.end("VideoTranscode("+inSourceFile+","+tgtFile+")");
		return StringHelper.System3(cmdline,inSourceFile.getAbsolutePath(),tgtFile.getAbsolutePath());
	}
	
//	./ffmpeg -i "$FILE" -c:v libvpx -b:v 1M -c:a libvorbis "${FILE%.m4v}.webm"

	private Map<String,String> FFMPEGMetdata(File inSourceFile) {
		//implied \n outter, = inner and ; comments stripped out
		
		
		
		return XMLHelper.StringToMap(StringHelper.Popen3("/opt/COMETDist/bin/ffmpeg -i "+StringHelper.SRC_FILENAME_CONST+" -f ffmetadata -",inSourceFile.getAbsolutePath(),true,false));
//		return XMLHelper.StringToMap(PopenThree("/usr/bin/comet_videomd.sh "+inSourceFile.getAbsolutePath(),true,false));
//		String returnval=PopenThree("/usr/bin/comet_videomd.sh "+inSourceFile.getAbsolutePath(),true,false);
//		
//		ScreenLog.out("\nreturn value as string is "+returnval);
//		
//		Map<String,String> mymap=new HashMap<String,String>();
//		
//		mymap=XMLHelper.StringToMap(returnval);
//		
//		ScreenLog.out("\nbefore map\n");
//		ScreenLog.out(mymap);
//		ScreenLog.out("\naftermap\n");
//		
//		return mymap;
	}
	
	
	//TODO: change to make use of CSV from comet.properties
	public boolean isCompatibleVideoFile(File f) {
		return (f.toString().toLowerCase().endsWith(".mkv") ||  //MKV, matroska format, typically ripped from DVD in this format
				f.toString().toLowerCase().endsWith(".m4v") || //MPEG4 encoded, typically H.264 for iOS devices
				f.toString().toLowerCase().endsWith(".mp4")); //MPEG4 encoded, typically H.264, for generic devices
	} 
	
	//TODO: delete functions do not respect readonly rule
//	public boolean CreateVideo(File inSourceFile) throws IOException {
//		ScreenLog.begin("CreateVideo("+inSourceFile+")");
//		//if it's not pdf, we can't handle it yet
//
//		File tempfile=null;
//		
//		if(isCompatibleVideoFile(inSourceFile)) {
//
//			generatedVideo=GenerateVideoTarget(inSourceFile);
//			
//			//already exists? delete it
//			//could be a problem if multiple files have the same name
//			
//			tempfile=new File(ingest_tmp+GetWebmFile(inSourceFile.getName()));
//			
//			if(tempfile.exists()) {
//				tempfile.delete();
//			}
//
//			if(!VideoTranscode(inSourceFile,tempfile)) {
//				ScreenLog.end("CreateVideo("+inSourceFile+") - transcode failed somehow, returning false");
//
//				return false;
//			}
//			ThreadTrackerDB.updateDBOverHTTP(inSourceFile.getAbsolutePath()+"-transcode-succeeded",mFileProc.getThreadID(), inSourceFile.length());
//			//srcFile should be the output of gdal_translate
//		} 
//		ScreenLog.end("CreateVideo("+inSourceFile+") - transcode succeeded");
//		return true;
//	}
	
//	private String GetWebmFile(String filename) {
//		int index = filename.lastIndexOf(".");
//		return filename.substring(0,index)+".webm";
//	}
//	
//	private File GenerateVideoTarget(File inSourceFile) {
//		return AnnotationHelper.FilePathToHCPPath(new File(inSourceFile.getParent()+GetWebmFile(inSourceFile.getAbsolutePath())),ingest_tmp+mFileProc.getPathPrefix(), mFileProc.getWorkingPath(), "");
//	}


	//TODO: replace error condition with Exception
	private videoType GetVideoType(File f) throws MalformedURLException, IOException, URISyntaxException {
		ScreenLog.begin("GetVideoType("+f+")");
		if(f.getName().endsWith(".mkv")) return videoType.VIDEO_LOCAL_MKV;
		if(f.getName().endsWith(".mp4")) return videoType.VIDEO_LOCAL_MP4;
		if(f.getName().endsWith(".m4v")) return videoType.VIDEO_LOCAL_M4V;
		ScreenLog.out("\tfile: "+f+" is not supported.. apparently");
		ScreenLog.end("GetVideoType("+f+")");
		return videoType.VIDEO_UNSUPPORTED;
	}
	
	//path in this sense is either a url or a path on HCP
	public String formMetadata(File inSourceFile ) throws ParserConfigurationException, TransformerException, IOException {
		ScreenLog.begin("formMetadata("+inSourceFile+")");
		
		CometStorageURI uri=new CometStorageURI(inSourceFile);
		
		ThreadTrackerDB.updateDBOverHTTP(uri.getUri()+"-metadataextraction(video)",mFileProc.getThreadID(), inSourceFile.length());

		Map<String, String> ffmpeg_map=FFMPEGMetdata(inSourceFile);
		String override="";
//		ffmpeg_map.put("WEBM_URL", GetWebmFile(inSourceFile).getAbsoluteFile().toString());
		
		uri.ChangeExtension("webm");
		
		ffmpeg_map.put("WEBM_URL", uri.getUri());
		OutputStream outputStream = new ByteArrayOutputStream();
		try {
			XMLStreamWriter serializer;

			serializer = XMLOutputFactory.newInstance().createXMLStreamWriter(outputStream, "UTF-8");
			serializer.writeStartDocument("UTF-8", null);
			serializer.writeCharacters("\n");
			serializer.writeStartElement("VIDEO");
			serializer.writeCharacters("\n    ");

			
			for(String key : ffmpeg_map.keySet()) {
				serializer.writeStartElement(StringHelper.toTitleCase(key));
				serializer.writeCharacters("\n    ");
				serializer.writeCharacters(ffmpeg_map.get(key));
				serializer.writeEndElement();
				serializer.writeCharacters("\n");
			}
			serializer.writeEndElement();
			serializer.writeEndDocument();
		}
		catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }
		override=outputStream.toString();
		ScreenLog.end("formMetadata("+inSourceFile+")");
		return override;
	}
	
	//TODO: move exceptions out
	public String getMetadata(File inSourceFile) {
		ScreenLog.setClass(VideoTranscoder.class.getName());
		ScreenLog.begin("VideoTranscoder::getMetadata("+inSourceFile+")");
		
		if(!inSourceFile.exists()) {
			ScreenLog.severe("\n\n"+inSourceFile.getAbsolutePath()+" does not exist");
			return "";
		}

		String override=getOverrideMetadata(inSourceFile);
		if(override!="") {
			ScreenLog.end("VideoTranscoder::getMetadata("+inSourceFile+") - returning override metadata file content");
			return override;
		}
		
		try {
			CometStorageURI uri=new CometStorageURI(inSourceFile);
			CometStorageURI uri_webm=new CometStorageURI(inSourceFile);
			uri_webm.ChangeExtension("webm");
			
			switch(GetVideoType(uri.getFile())) {
				case VIDEO_LOCAL_MKV:
				case VIDEO_LOCAL_M4V:
				case VIDEO_LOCAL_MP4:
					if(CometProperties.getInstance().getEnableTranscoding() && VideoTranscode(uri.getFile(),uri_webm.getTempFile())) {
					//URL encodedPathURL=AnnotationHelper.FSToURLPath(CometProperties.getInstance().getDestinationRootPath(mFileProc.getPathPrefix()), mFileProc.getWorkingPath(),GetWebmFile(inSourceFile));
					//replace with library call (eventually)
						ScreenLog.out("\n\n--->writing transcoded video: "+uri_webm.getTempFile());
						ScreenLog.out("\n\n--->to HCP as URL:"+uri_webm.getHcpURL());
					
						//propopsed URL on HCP for webm, local copy of newly generated webm
						//File temp_file=new File(ingest_tmp+generatedVideo.getAbsolutePath());
						ThreadTrackerDB.updateDBOverHTTP(uri_webm.getUri(),mFileProc.getThreadID(), inSourceFile.length());
						mFileProc.WriteObjectToHCP(uri_webm.getHcpURL(),uri_webm.getTempFile());
						uri_webm.getTempFile().delete();

					} 
					//generate annotation for original file including metadata via ffmpeg
					ScreenLog.end("VideoTranscoder::getMetadata("+inSourceFile+") - returning result of formMetadata()");
					return formMetadata(inSourceFile);
			case VIDEO_UNSUPPORTED:
			default:
				break;
			}
		} catch (Exception e) {
			ScreenLog.ExceptionOutputHandler(e);
		}
		ScreenLog.end("VideoTranscoder::getMetadata("+inSourceFile+") - returning zip");

		return "";
	}

//	public File getGeneratedVideo() {
//		return generatedVideo;
//	}
//
//	public void setGeneratedVideo(File generatedVideo) {
//		this.generatedVideo = generatedVideo;
//	}
}
