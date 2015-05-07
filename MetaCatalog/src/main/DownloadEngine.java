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
//Package: COMET Web Application
//Author: Chris Delezenski <chris.delezenski@hdsfed.com>
//Compilation Date: 2015-05-06
//License: Apache License, Version 2.0
//Version: 1.21.0
//(RPM) Release: 1
//SVN: r554
package main;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.xml.stream.XMLStreamException;

import com.hdsfed.cometapi.AnnotationGenerator;
import com.hdsfed.cometapi.AnnotationHelper;
import com.hdsfed.cometapi.CometProperties;
import com.hdsfed.cometapi.ExtendedLogger;
import com.hdsfed.cometapi.HCPClient;
import com.hdsfed.cometapi.MetadataParser;
import com.hdsfed.cometapi.Range;
import com.hdsfed.cometapi.ServletHelper;
import com.hdsfed.cometapi.StringHelper;
import com.hdsfed.cometapi.TaglistMap;
import com.hdsfed.cometapi.XMLHelper;
//future
//import org.apache.commons.fileupload.FileItem;
//import org.apache.commons.fileupload.FileUploadException;
//import org.apache.commons.fileupload.disk.DiskFileItemFactory;
//import org.apache.commons.fileupload.servlet.ServletFileUpload;



//TODO: implement improved uploading functionality and pass-thru to HCP
//		implement output rendering to be plaintext, json or XML
public class DownloadEngine {
	private static ExtendedLogger ScreenLog = new ExtendedLogger(DownloadEngine.class.getPackage().getName());
	//future
	//    private ServletFileUpload uploader = null;

	private static final int DEFAULT_BUFFER_SIZE = 10240; // ..bytes = 10KB.
    private static final long DEFAULT_EXPIRE_TIME = 604800000L; // ..ms = 1 week.
    private static final String MULTIPART_BOUNDARY = "MULTIPART_BYTERANGES";

	
	public enum relayType {
		RT_CUSTOMMD,
		RT_CUSTOMMDINFO,
		RT_OBJECT,
		RT_GENERATE,
		RT_THUMBNAIL,
		RT_ICON,
		RT_CREATE, //create a new empty annotation
		RT_RENAME, //change the name of an annotation using copy and then delete
		RT_DELETE, //delete an existing annotation
		RT_TEST,
		RT_EXISTS,
		RT_LOGS,
		RT_EDIT_FILE,
		RT_JSON,
		RT_MISSING,
		RT_ARCHIVE,
		RT_UNSUPPORTED
	};

	private static relayType RelayType(String string) {
		if(string==null || string.equals("")) return relayType.RT_MISSING;
		if(string.toLowerCase().contains("custom-metadata-info")) return relayType.RT_CUSTOMMDINFO;
		if(string.toLowerCase().contains("custom-metadata")) return relayType.RT_CUSTOMMD;
		if(string.toLowerCase().contains("object")) return relayType.RT_OBJECT;
		if(string.toLowerCase().contains("genera")) return relayType.RT_GENERATE;
		if(string.toLowerCase().contains("thumbnail")) return relayType.RT_THUMBNAIL;
		if(string.toLowerCase().contains("icon")) return relayType.RT_ICON;
		if(string.toLowerCase().contains("create")) return relayType.RT_CREATE;
		if(string.toLowerCase().contains("rename")) return relayType.RT_RENAME;
		if(string.toLowerCase().contains("delete")) return relayType.RT_DELETE;
		if(string.toLowerCase().contains("test")) return relayType.RT_TEST;
		if(string.toLowerCase().contains("exists")) return relayType.RT_EXISTS;
		if(string.toLowerCase().contains("logs")) return relayType.RT_LOGS;
		if(string.toLowerCase().contains("editfile")) return relayType.RT_EDIT_FILE;
		if(string.toLowerCase().contains("json")) return relayType.RT_JSON;
		if(string.toLowerCase().contains("archive")) return relayType.RT_ARCHIVE;
		
		return relayType.RT_UNSUPPORTED;
	}

	public enum generatorType {
		GEN_DESCRIPTION,
		GEN_KML_PATH,
		GEN_COMBINED,
		GEN_TAGLIST,
		GEN_TOOLTIP,
		GEN_KML,
//		GEN_ICONPATH,
		GEN_TEST,
		GEN_UNSUPPORTED
	};

	private static generatorType GeneratorType(String string) {
		if(string.toLowerCase().contains("desc")) return generatorType.GEN_DESCRIPTION;
		if(string.toLowerCase().contains("kml_path")) return generatorType.GEN_KML_PATH;
		if(string.toLowerCase().contains("kml")) return generatorType.GEN_KML;
		if(string.toLowerCase().contains("taglist")) return generatorType.GEN_TAGLIST;
		if(string.toLowerCase().contains("tooltip")) return generatorType.GEN_TOOLTIP;
		if(string.toLowerCase().contains("combined")) return generatorType.GEN_COMBINED;
		if(string.toLowerCase().contains("test")) return generatorType.GEN_TEST;
		return generatorType.GEN_UNSUPPORTED;
	}
	
	private static String AnnotationGeneratorEngine(HCPClient client, CometProperties mProps, Map<String, String> parameters) {
		ScreenLog.begin("AnnotationGeneratorEngine(client,mprops,parameters)");
		String content="";
		if(mProps.getShowCaveats()) parameters.put("showclass", "");
		parameters.put("imageprefix",mProps.getImagePrefix());
		switch(GeneratorType(parameters.get("annotation"))) {
			case GEN_DESCRIPTION:
			try {	
				content=AnnotationGenerator.DescGenerator(client, parameters);
				if(!parameters.containsKey("astext")) {
					content="<html><head><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"css/styles.css\"/></head><body>"+content+"</body></html>";
					parameters.put("astext","1");
				}
			} catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }
		
			break;
			case GEN_KML_PATH:
			try {
				ScreenLog.out("BEGIN GEN_KML_PATH, path="+parameters.get("path"));
				content=XMLHelper.DocToString(AnnotationGenerator.PathKMLGenerator(client, parameters)).trim();
				if(content.isEmpty()) {
					ScreenLog.out("Unable to get KML_PATH, try just plain KML, path="+parameters.get("path"));
					if(parameters.get("path").endsWith(".kml") || parameters.get("path").endsWith(".kmz")) {
						//ScreenLog.out("KML Wrapper generator");
						//content=XMLHelper.DocToString(AnnotationGenerator.KMLWrapperGenerator(client,parameters));
						content="object";
					} else {
						content=AnnotationGenerator.KMLGenerator(client,parameters);
					}
				}
				ScreenLog.out("END GEN_KML_PATH, path="+parameters.get("path"));
			} catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }
			break;
			case GEN_COMBINED:
				try {
					return AnnotationGenerator.CombineAnnotations(client,parameters);
				} catch (XMLStreamException e) {
					ScreenLog.ExceptionOutputHandler(e);
					content="<error>Error!</error>";
				}
			break;
			case GEN_TAGLIST:
				try {
					return XMLHelper.DocToString(AnnotationGenerator.TaglistGenerator(client, parameters));
				} catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }
			break;
			case GEN_TOOLTIP:
				try {
					if(parameters.containsKey("size")) parameters.put("taglistsize", parameters.get("size"));
					content=AnnotationGenerator.ToolTipGenerator(client,parameters);
				} catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }
			break;
			case GEN_KML:
				try {
					content=AnnotationGenerator.KMLGenerator(client,parameters);
				} catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }
			break;
			case GEN_TEST:
				try {
					content=AnnotationGenerator.TestGenerator(client,parameters);
				} catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }
			break;	
			default:
				content="<unsupported>Unsupported generator</unsupported>";
			break;
		}
		
		ScreenLog.out("content="+content);
		
		ScreenLog.end("AnnotationGeneratorEngine(client,mprops,parameters)");
		return content;
	}

	public static BufferedImage scaleImage(BufferedImage img, int width, int height) {
	    int imgWidth = img.getWidth();
	    int imgHeight = img.getHeight();
	    if (imgWidth*height < imgHeight*width) {
	        width = imgWidth*height/imgHeight;
	    } else {
	        height = imgHeight*width/imgWidth;
	    }
	    BufferedImage newImage = new BufferedImage(width, height,
	            BufferedImage.TYPE_INT_ARGB);
	    Graphics2D g = newImage.createGraphics();
	    try {
	        //g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
	        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	        g.setComposite(AlphaComposite.Clear);
	        g.fillRect(0, 0, width, height);
	        g.setComposite(AlphaComposite.Src);
	        //drawPoints(Tablet.getPenPoints(), g, Color.BLACK);
	        //g.clearRect(0, 0, width, height);
	        g.drawImage(img, 0, 0, width, height, null);
	    } finally {
	        g.dispose();
	    }
	    return newImage;
	}
	
	public static void RelayGet(HttpServletRequest request, HttpServletResponse response)  {
		try {
			//necessary?
			//response.setHeader("Cache-Control", "no-cache");
			//response.setHeader("Pragma", "no-cache");
//			Map<String, String> parameters=ServletHelper.GetParameterMap(request);
//			Map<String, String> headers=ServletHelper.GetHeaderMap(request);
			DownloadEngine de=new DownloadEngine();
			
			
//			ScreenLog.out("\n\nnumber of parts: "+request.getParts().size()+"\n\n");
			
//			ScreenLog.out("\n\ncontent length="+request.getContentLength()+"\n\n");
			
//			ScreenLog.out("\n\ncontent type="+request.getContentType()+"\n\n");
					
//			ScreenLog.out("\n\nheaders="+headers+"\n\n");
			
//			if(headers.containsKey("range")) {
//				ScreenLog.out("\n\nrange="+headers.get("range")+"\n\n");
//				parameters.put("start_byte", headers.get("range").split(" - ")[0]);
				
//				if(headers.get("range").split(" - ").length>1) 
//				parameters.put("end_byte", headers.get("range").split(" - ")[1]);
//			}
			
			de.Relay(request, response,true);
		} catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }
	} //end Relay front end
	public static void RelayPut(HttpServletRequest request, HttpServletResponse response)  {
		try {
//			response.setHeader("Cache-Control", "no-cache");
//			response.setHeader("Pragma", "no-cache");
//			Map<String, String> parameters=ServletHelper.GetParameterMap(request);
			DownloadEngine de=new DownloadEngine();
			de.Relay(request, response, false);
		} catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }
	} //end Relay front end

/*
 *  Future implementation
 * 	public static void RelayPut(HttpServletRequest request, HttpServletResponse response)  {
 
		try {
			response.setHeader("Cache-Control", "no-cache");
			response.setHeader("Pragma", "no-cache");

		//	ServletOutputStream out = response.getOutputStream();
			ScreenLog.out("BEGIN === DownloadEngine::RelayPut()");
		
			try {
				List<FileItem> fileItemsList = uploader.parseRequest(request);
				Iterator<FileItem> fileItemsIterator = fileItemsList.iterator();
				while(fileItemsIterator.hasNext()){
					FileItem fileItem = fileItemsIterator.next();
					
					File file = new File(request.getServletContext().getAttribute("FILES_DIR")+File.separator+fileItem.getName());
					fileItem.write(file);
					out.write("File "+fileItem.getName()+ " uploaded successfully.");
					out.write("<br>");
					out.write("<a href=\"UploadDownloadFileServlet?fileName="+fileItem.getName()+"\">Download "+fileItem.getName()+"</a>");
				}
			} catch (FileUploadException e) {
				out.write("Exception in uploading file.");
			} catch (Exception e) {
				out.write("Exception in uploading file.");
			}
			
			
			Map<String, String> parameters=ServletHelper.GetParameterMap(request);
			RelayPut(parameters, content, response,false);
		} catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }
	} //end Relay front end

	*/
	
	
	//TODO: this function is entirely too long... should be chopped into manageable pieces
	
	@SuppressWarnings("incomplete-switch")
	public void Relay(HttpServletRequest request, HttpServletResponse response, Boolean isGet ) throws Exception {		
			ScreenLog.begin("Relay(parameters,response,isGet="+isGet);
			
			
			Map<String, String> parameters=ServletHelper.GetParameterMap(request);
			Map<String, String> headers=ServletHelper.GetHeaderMap(request);

			
			//ScreenLog.out(parameters,"parameters");
			
			//relay has modes of operation:
			//   stream     (cat to window as stdout)
			//   not stream (download as file)
			
			switch(RelayType(parameters.get("type"))) {
				case RT_TEST:
				case RT_EXISTS:
				case RT_JSON:
					parameters.put("stream","1");
				break;
				case RT_OBJECT:
					//REFACTOR!  We need a quick fix to implement archiving; this is a hack and should be refactored in the next version
					//from the FE's perspective, the URI will look like Relay?path=/path/to/object&type=object&action=archive
					//from this function's perspective, we're changing the parameters to register as:
					// Relay?path=/path/to/object&type=archive; meaning move this object from it's path to /archive/ in the same ns.
					if(parameters.containsKey("action") && parameters.get("action").equals("archive")) parameters.put("type","archive");
					
					if(parameters.containsKey("path") && parameters.get("path").endsWith(".m4v") && parameters.containsKey("webm")) {
						
						
						String temp_path=parameters.get("path");
						temp_path=temp_path.substring(0,temp_path.length()-4)+".webm";
						parameters.put("path", temp_path);
						ScreenLog.out("rewriting path with webm"+temp_path);
					}
					
				break;

			}
			
			if(RelayType(parameters.get("type"))==relayType.RT_JSON) parameters.put("astext","1");
			
			if(!parameters.containsKey("stream") && isGet) {
				//prep for file download
				ScreenLog.out("prep for file transfer");
				
				if(parameters.containsKey("iskml")) response.setContentType("application/vnd.google-earth.kml+xml");
				else response.setContentType("application/octet-stream");
			
			} 
			
			//relay has types of operations as well
			//	 custom-metadata
			//	custom-metadata-info
			//  object
			//  generate
			//  thumbnail

			//users and roles are built in and stored statically
			CometProperties mProps=CometProperties.getInstance();
			HCPClient client=new HCPClient(mProps);

			PrintWriter writer =null;
			OutputStream result_out =null;
			ByteArrayOutputStream os=null;
			BufferedImage img=null;
			String content="";
			int newwidth=0;
			int newheight=0;
			Boolean resize=Boolean.FALSE;
			
			if(parameters.containsKey("size") && parameters.get("size").contains("x")) {
				newwidth=Integer.parseInt(parameters.get("size").split("x")[0]);
				newheight=Integer.parseInt(parameters.get("size").split("x")[1]);
				resize=Boolean.TRUE;
				
				ScreenLog.out("new size = "+newwidth+","+newheight);
			} 
			CometProperties.setConfigMode(parameters.containsKey("config"));
			parameters.put("rootpath",mProps.getDestinationRootPath().toString());
			parameters.put("imageprefix", mProps.getImagePrefix());
			CometProperties.setConfigMode(false);
			File infile=null;
			
			ScreenLog.out("DownloadEngine:: rootpath(based on config state)=\""+parameters.get("rootpath")+"\"");
			switch(RelayType(parameters.get("type"))) {
			case RT_MISSING:
					ScreenLog.out("Relay:::type=(missing)");
					
				break;
				case RT_CUSTOMMD:
					ScreenLog.out("Relay:::type=custom-metadata");
					writer=response.getWriter();
					ScreenLog.out("Custom metadata");
					if(!parameters.containsKey("annotation")) parameters.put("annotation", "default");
					if(isGet) {
						if(!parameters.containsKey("stream")) {
							if(!parameters.containsKey("saveas")) parameters.put("saveas", URLtoJustFileName(parameters.get("path")) +"."+ parameters.get("annotation")+".xml");
							response.setHeader("Content-Disposition","attachment; filename=\"" +parameters.get("saveas")+"\"");
						}
						content=XMLHelper.StringToString(client.HttpGetHCPContent(AnnotationHelper.PathAndAnnotationToURL(new URL(parameters.get("rootpath")), parameters.get("path"), parameters.get("annotation"))));
						writer.write(content);
						if(parameters.containsKey("saveas")) {
							writer.write(client.HttpPutHCPContent(content,AnnotationHelper.PathAndAnnotationToURL(new URL(parameters.get("rootpath")), parameters.get("path"),parameters.get("saveas"))));
						}	
					} else {
						if(parameters.containsKey("content")) {
							client.HttpPutHCPContent(parameters.get("content"),AnnotationHelper.PathAndAnnotationToURL(new URL(parameters.get("rootpath")), parameters.get("path"),parameters.get("annotation")));
							writer.write("save succeeded");
						} else {
								ScreenLog.out("\t attempted put without content.. not ready for that yet");
								throw new Exception();
						}
						
						if(parameters.containsKey("recombine")) {
							ScreenLog.out("should recombine all annotations");
							writer.write(client.HttpPutHCPContent(AnnotationGenerator.CombineAnnotations(client,parameters),
									AnnotationHelper.PathAndAnnotationToURL(client.getRootpath(), parameters.get("path"), client.getCombinedAnnotationName())));
						}
						
						
					}
				break;
				case RT_CUSTOMMDINFO:
					writer=response.getWriter();
					if(!parameters.containsKey("stream")) {
						if(!parameters.containsKey("saveas")) parameters.put("saveas", URLtoJustFileName(parameters.get("path")) +"."+ parameters.get("annotation")+".xml");
						response.setHeader("Content-Disposition","attachment; filename=\"" +parameters.get("saveas")+"\"");
					}
					content=client.HttpGetHCPContent(AnnotationHelper.AnnotationListToURL(new URL(parameters.get("rootpath")), parameters.get("path")));
					writer.write(content);
				break;
				case RT_OBJECT:
					ScreenLog.begin("RT_OBJECT");
					
					Boolean alreadyExists=false;
					
					CometProperties.setConfigMode(parameters.containsKey("config"));

					
					if(isGet) {
						if(!parameters.containsKey("stream")) {
							if(!parameters.containsKey("saveas")) parameters.put("saveas", URLtoJustFileName(parameters.get("path")));
							response.setHeader("Content-Disposition","inline; filename=\"" +parameters.get("saveas")+"\";");
						}
						result_out = response.getOutputStream();

						InputStream is=null;
						
						
						
						if(parameters.containsKey("cached")) {
							
							ScreenLog.out("BEGIN cached object");
							
							//create file location to use as cache, add it to the cache register
							File inputFile=CometProperties.DownloadToCache(URLDecoder.decode(parameters.get("path"), "UTF-8"));

							
							
							//pull down the file from HCP to the cached location
							
							if(!inputFile.getParentFile().exists()) {
								inputFile.getParentFile().mkdirs();
							}
							
							if(!inputFile.exists()) {
								is=client.HttpGetHCPContentStream(
										AnnotationHelper.PathToURL(CometProperties.getInstance().getDestinationRootPath(),parameters.get("path")));
								
								HCPClient.InputStreamToFile( is, inputFile);

							}

//							Transfer-Encoding: chunked
							response.setHeader("Transfer-Encoding","chunked");
							parameters.put("chunked", "");
							//switch the input stream to open the newly created cache file instead of HCP
							
							if(parameters.containsKey("start_byte")) ScreenLog.out("\nstart byte="+parameters.get("start_byte"));
							if(parameters.containsKey("end_byte")) ScreenLog.out("\nend byte="+parameters.get("end_byte"));
							
							
							//is=new FileInputStream(inputFile);
						
						
							//serve file ranged
							
							
							ScreenLog.out("\tserving local file: "+inputFile+" within range "+headers.get("range"));
							ServeLocalFileRanged(request,response, inputFile);
							
						
						} else {
							
							is=client.HttpGetHCPContentStream(
									AnnotationHelper.PathToURL(CometProperties.getInstance().getDestinationRootPath(),parameters.get("path")));

							//serve the input stream as normal
							int num;
							byte buf[] = new byte[4096];
							DataInputStream dis = new DataInputStream(is);
							
							while((num = dis.read(buf)) != -1){ 
								result_out.write(buf, 0, num);
							}	
							dis.close();
						}


					} else {
						//isPut
						//first delete the object
						
						//then write it from stream
						
						if(parameters.containsKey("content")) {
							URL hcp_url=AnnotationHelper.PathToURL(CometProperties.getInstance().getDestinationRootPath(),parameters.get("path"));
							
							URL hcp_url_moved=AnnotationHelper.PathToURL(CometProperties.getInstance().getDestinationRootPath(),parameters.get("path")+"_moved");
							
							
							
							
							//1) check to see if an object exists at this location
							alreadyExists=client.HCPObjectExists(hcp_url);
							
							if(alreadyExists) {
								//if exists
								//1b) copy existing object to _moved (with all metadata)
								client.HttpDeepCopyHCPContent(hcp_url, hcp_url_moved);
								AnnotationHelper.CopyAllAnnotations(client,CometProperties.getInstance().getDestinationRootPath(),parameters.get("path"),parameters.get("path")+"_moved");

								//1c) delete existing
								client.HttpDeleteHCPContent(hcp_url);
							
							} 
							//2) put the new object to old location
							client.HttpPutHCPContent(parameters.get("content"),hcp_url);
							
							//if exists
							if(alreadyExists) {
								//3) copy annotations from _moved object to this one
								AnnotationHelper.CopyAllAnnotations(client,CometProperties.getInstance().getDestinationRootPath(),parameters.get("path")+"_moved",parameters.get("path"));
							
								//4) delete _moved object
								client.HttpDeleteHCPContent(hcp_url_moved);
							}		
							
							
							
							writer.write("save skipped");
						} else {
								ScreenLog.out("\t attempted put without content");
								throw new Exception();
						}

						
					}
					
					
					
					CometProperties.setConfigMode(false);
				break;
				case RT_GENERATE: //intended for text
					if(!parameters.containsKey("annotation")) parameters.put("annotation", "test");
					if(!parameters.containsKey("stream")) {
						if(!parameters.containsKey("saveas")) parameters.put("saveas", URLtoJustFileName(parameters.get("path")) +"."+ parameters.get("annotation")+".xml");
						response.setHeader("Content-Disposition","attachment; filename=\"" +parameters.get("saveas")+"\"");
					}
					content=AnnotationGeneratorEngine(client,mProps,parameters);
					if(content=="object") {
						//ScreenLog.out("was going to generate a kml_path, but returning object instead");
						if(!parameters.containsKey("stream")) {
							if(!parameters.containsKey("saveas")) parameters.put("saveas", URLtoJustFileName(parameters.get("path")));
								response.setHeader("Content-Disposition","attachment; filename=\"" +parameters.get("saveas")+"\"");
								
						}
						result_out = response.getOutputStream();
						int num2;
						byte buf2[] = new byte[4096];
						DataInputStream dis2 = new DataInputStream(client.HttpGetHCPContentStream(
								AnnotationHelper.PathToURL(new URL(parameters.get("rootpath")),parameters.get("path"))));
						while((num2 = dis2.read(buf2)) != -1){ 
							result_out.write(buf2, 0, num2);
						}
						
						dis2.close();
						return;
					
					}
					writer=response.getWriter();

					if(!parameters.containsKey("astext")) content=XMLHelper.StringToString(content);
					if(!parameters.containsKey("saveas_annotation")) {
						//ScreenLog.out("saveas is missing");
						writer.write(content);
					} else {
						//ScreenLog.out("saveas is "+parameters.get("saveas_annotation"));
						writer.write(client.HttpPutHCPContent(content,AnnotationHelper.PathAndAnnotationToURL(new URL(parameters.get("rootpath")), parameters.get("path"),parameters.get("saveas_annotation"))));
					}
				break;
				case RT_THUMBNAIL:
					ScreenLog.begin("Determine URL for thumbnail of object and push that for path="+parameters.get("path"));
					Set<String> annotationSet=AnnotationHelper.HttpGetAnnotationsSet(client, parameters.get("path"));	
					result_out = response.getOutputStream();
					if(!parameters.containsKey("stream")) {
						if(!parameters.containsKey("saveas")) parameters.put("saveas", URLtoJustFileName(parameters.get("path")) +".thumb.png");
						response.setHeader("Content-Disposition","attachment; filename=\"" +parameters.get("saveas")+"\"");
					}
					int num2;
					byte buf2[] = new byte[4096];
					if(annotationSet.contains("thumbnail")) {
						img=ImageIO.read(client.HttpGetHCPContentStream(
								AnnotationHelper.PathToURL(new URL(parameters.get("rootpath")),
							XMLHelper.GetSimpleTagContent(
										XMLHelper.StringToDoc(
						client.HttpGetHCPContent(
								AnnotationHelper.PathAndAnnotationToURL(new URL(parameters.get("rootpath")),parameters.get("path"),"thumbnail")
								) //end client.HttpGetHCPContent
							) //end Stringtodoc
							,
							"THUMBNAIL_URL") //end get simple tag content
							
										)));	
						
					} else if(annotationSet.contains("default")) {
						String newpath=XMLHelper.GetSimpleTagContent(
								XMLHelper.StringToDoc(
										client.HttpGetHCPContent(
												AnnotationHelper.PathAndAnnotationToURL(new URL(parameters.get("rootpath")),parameters.get("path"),"default")
												) //end client.HttpGetHCPContent
												) //end Stringtodoc
												,"THUMBNAIL").trim(); //end get simple tag content
						ScreenLog.out("size of extension is "+newpath.length());
						ScreenLog.out("newpath=\""+newpath+"\"");
						if(newpath.equals("") || newpath.length()==0) {
							ScreenLog.out("trying "+parameters.get("imageprefix")+"images/noimage.png");
							img=ImageIO.read(client.HttpGetContentStream(new URL(parameters.get("imageprefix")+"images/noimage.png"))); 
						}
						else {
							ScreenLog.out("path to thumbnail is: "+parameters.get("rootpath")+newpath);
							img=ImageIO.read(client.HttpGetHCPContentStream(
								new URL(parameters.get("rootpath")+newpath)));
							//,
								//		parameters.get("path")+"."+XMLHelper.GetSimpleTagContent(
								//		XMLHelper.StringToDoc(client.HttpGetHCPContent(
								//				AnnotationHelper.PathAndAnnotationToURL(new URL(parameters.get("rootpath")),parameters.get("path"),"default")
								//				) //end client.HttpGetHCPContent
								//				) //end Stringtodoc
								//				,"THUMBNAIL") //end get simple tag content
								//				)));	
										//newpath)));
						}
					} else { //end if thumbnail exists
						img=ImageIO.read(client.HttpGetContentStream(new URL(parameters.get("imageprefix")+"images/noimage.png")));
					}	
					//common code
					if(newwidth==0) newwidth=img.getWidth();
					if(newheight==0) newheight=img.getHeight();
						
					if(resize) img=scaleImage(img, newwidth,newheight);

						//BufferedImage image = ImageIO.read(url);
					os = new ByteArrayOutputStream();
					ImageIO.write(img, "png", os);
						
					DataInputStream dis2 = new DataInputStream(new ByteArrayInputStream(os.toByteArray()));
					while((num2 = dis2.read(buf2)) != -1){ 
						result_out.write(buf2, 0, num2);
					}
					dis2.close();
					
					ScreenLog.end("Determine URL for thumbnail of object and push that for path="+parameters.get("path"));

				break;	
				case RT_ICON:
					result_out = response.getOutputStream();
					if(!parameters.containsKey("stream")) {
						if(!parameters.containsKey("saveas")) 
							parameters.put("saveas", URLtoJustFileName(parameters.get("path")) +
									                 "."+
									                 MetadataParser.GetFileTypeIcon(parameters.get("path"))+
									                ".png");
						response.setHeader("Content-Disposition","attachment; filename=\"" +parameters.get("saveas")+"\"");
					}
					int num4;
					byte buf4[] = new byte[4096];

					img=ImageIO.read(client.HttpGetContentStream(AnnotationHelper.PathToIconURL(parameters.get("imageprefix"),parameters.get("path"))));
					if(newwidth==0) newwidth=img.getWidth();
					if(newheight==0) newheight=img.getHeight();
				
					//you got your image.. now resize that thing
					if(resize) img=scaleImage(img, newwidth,newheight);
					os = new ByteArrayOutputStream();
					ImageIO.write(img, "png", os);
					DataInputStream dis4 = new DataInputStream(new ByteArrayInputStream(os.toByteArray()));
					while((num4 = dis4.read(buf4)) != -1){ 
						result_out.write(buf4, 0, num4);
					}
					dis4.close();
			
				case RT_CREATE:
					writer=response.getWriter();
					//check to see that we have an annotation
					if(!parameters.containsKey("annotation") || !parameters.containsKey("path")) {
						ScreenLog.out("insufficient parameters, bail out");
						return;
					}
					writer.write(client.HttpPutHCPContent("<xml>new annotation</xml>", AnnotationHelper.PathAndAnnotationToURL(client.getRootpath(), parameters.get("path"), parameters.get("annotation"))));
					
					if(parameters.containsKey("recombine")) {
						ScreenLog.out("should recombine all annotations");
						writer.write(client.HttpPutHCPContent(AnnotationGenerator.CombineAnnotations(client,parameters),
								AnnotationHelper.PathAndAnnotationToURL(client.getRootpath(), parameters.get("path"), client.getCombinedAnnotationName())));
					}
				break;
				case RT_RENAME:
					writer=response.getWriter();
					if(!parameters.containsKey("annotation") || !parameters.containsKey("path") || !parameters.containsKey("saveas_annotation")) {
						ScreenLog.out("insufficient parameters, bail out");
						return;
					}

					if(parameters.get("annotation").equals(parameters.get("saveas_annotation"))) {
						ScreenLog.out("err: annotation source and target are the same");
						return;
					}

					writer.write(AnnotationHelper.AnnotationRename(client, parameters.get("path"), parameters.get("annotation"), parameters.get("saveas_annotation")));
					if(parameters.containsKey("recombine")) {
						ScreenLog.out("should recombine all annotations");
						writer.write(client.HttpPutHCPContent(AnnotationGenerator.CombineAnnotations(client,parameters),
								AnnotationHelper.PathAndAnnotationToURL(client.getRootpath(), parameters.get("path"), client.getCombinedAnnotationName())));
					}
				//	ScreenLog.end("DownloadEngine:::RENAME Annotation");
					
				break;
				case RT_DELETE:
				//	ScreenLog.begin("DownloadEngine:::DELETE Annotation");
					
					
					CometProperties.setConfigMode(parameters.containsKey("config"));
					
					writer=response.getWriter();

					
					writer.write(client.HttpDeleteHCPContent(AnnotationHelper.PathAndAnnotationToURL(client.getRootpath(),parameters.get("path"),parameters.get("annotation"))));
					if(parameters.containsKey("recombine")) {
						ScreenLog.out("should recombine all annotations");
						writer.write(client.HttpPutHCPContent(AnnotationGenerator.CombineAnnotations(client,parameters),
								AnnotationHelper.PathAndAnnotationToURL(client.getRootpath(), parameters.get("path"), client.getCombinedAnnotationName())));
					}
				//	ScreenLog.end("DownloadEngine:::Delete Annotation");
					CometProperties.setConfigMode(false);

				break;
				case RT_EXISTS:
					if(!parameters.containsKey("path")) break;
					writer=response.getWriter();
					writer.write("{ \"path\" : \""+parameters.get("path")+"\", \"exists\" :"+client.HCPObjectExists(parameters.get("path")).toString()+" }");
				break;

				case RT_TEST:
				break;
				case RT_LOGS:
					if(!parameters.containsKey("annotation")) break;
					
					if(isGet && parameters.get("annotation").toLowerCase().equals("gui") || parameters.get("annotation").toLowerCase().equals("metacatalog") || parameters.get("annotation").toLowerCase().equals("ui")) {
						ScreenLog.out("get UI log file");
						parameters.put("saveas", "gui-log.txt");
						infile=new File("/var/log/comet/catalina.out");
					} else if(isGet && parameters.get("annotation").toLowerCase().equals("hosts")) {
						ScreenLog.out("/etc/hosts file");
						parameters.put("saveas", "etc-hosts.txt");
						infile=new File("/etc/hosts");
					} else if(isGet && parameters.get("annotation").toLowerCase().equals("ingest") || parameters.get("annotation").toLowerCase().equals("ingestor")) {
						ScreenLog.out("Ingestor output file");
						parameters.put("saveas", "ingestor-log.txt");
						infile=new File("/var/log/comet/Ingestor0.log.0");
					} else if(isGet && parameters.get("annotation").toLowerCase().equals("syslog") || parameters.get("annotation").toLowerCase().equals("systemlog")) {
						ScreenLog.out("/var/log/messages file");
						parameters.put("saveas", "syslog.txt");
						infile=new File("/var/log/messages");
					} else if(isGet && parameters.get("annotation").toLowerCase().equals("fstab") || parameters.get("annotation").toLowerCase().equals("filesystems")) {
						ScreenLog.out("/etc/fstab file");
						parameters.put("saveas", "filesystems.txt");
						infile=new File("/etc/fstab");
				//	} else if(isGet && parameters.get("annotation").toLowerCase().equals("uploadlog")) {	
				//		ScreenLog.out("upload log");
				//		parameters.put("saveas", "uploadlog.txt");
				//		infile=new File(CometProperties.getInstance().getFileProcessedCache()+".txt");
					} else if(parameters.get("annotation").toLowerCase().equals("properties") || parameters.get("annotation").toLowerCase().equals("props")) {
						ScreenLog.out("comet.properties");
						if(isGet) {
							parameters.put("saveas", "comet_properties.txt");
							infile=new File(CometProperties.getPropertiesFile());
						} else {
							File f=new File(CometProperties.getPropertiesFile());
							String temp_content=parameters.get("content").replaceAll("\r\n", "\n");
							Files.write(f.toPath(),temp_content.getBytes());
							CometProperties.getInstance().reload();
						}
					} else if(parameters.get("annotation").toLowerCase().equals("roles")) {
						if(isGet) {
							parameters.put("saveas", "roles_json.txt");
							infile=new File(CometProperties.getDefaultRolesFile());
						} else {
							File f=new File(CometProperties.getDefaultRolesFile());
							String temp_content=parameters.get("content").replaceAll("\r\n", "\n");
							Files.write(f.toPath(),temp_content.getBytes());
						}
					}

					if(isGet) {
						if(!parameters.containsKey("stream")) {
							response.setHeader("Content-Disposition","attachment; filename=\"" +parameters.get("saveas")+"\"");
						}
						if(!parameters.containsKey("tail")) parameters.put("tail", "0");
						//TODO:move this function into library
						DumpFileToOutputStream(infile,response.getOutputStream(),Integer.parseInt(parameters.get("tail")));
					}
				break;	

				//will eventually replace all local file i/o activity
				case RT_EDIT_FILE:
					ScreenLog.begin("edit a file (get is "+new Boolean(isGet).toString()+")");
					
					ScreenLog.out(parameters);
					
					
					if(!parameters.containsKey("path")) throw new Exception("Missing path");
						
						
					if(isGet) {
						//load
						infile=new File(parameters.get("path"));
						
						if(!parameters.containsKey("saveas")) parameters.put("saveas", parameters.get("path"));
					
						if(!parameters.containsKey("stream")) {
							response.setHeader("Content-Disposition","attachment; filename=\"" +new File(parameters.get("saveas")).getName()+"\"");
						}
						if(!parameters.containsKey("tail")) parameters.put("tail", "0");
						//TODO:move this function into library
						
						
						try {
							DumpFileToOutputStream(infile,response.getOutputStream(),Integer.parseInt(parameters.get("tail")));
						} catch (FileNotFoundException e) {
							ScreenLog.severe("exception thrown: file not found");
							response.setStatus(HttpServletResponse.SC_NOT_FOUND);							
						} finally {
						}
					} else {
						//save
						if(!parameters.containsKey("content")) {
							
							response.setStatus(HttpServletResponse.SC_NO_CONTENT);
							throw new Exception("missing content");
						}
						File f=new File(parameters.get("path"));
						String temp_content=parameters.get("content").replaceAll("\r\n", "\n");
						Files.write(f.toPath(),temp_content.getBytes());
					
						if(parameters.containsKey("reloadproperties")) CometProperties.getInstance().reload();	
					}
					ScreenLog.end("edit a file (get is "+new Boolean(isGet).toString()+")");

				break;
				
				case RT_JSON:
					if(!parameters.containsKey("path")) {
							content="{}";
					} else {
						try {
							URL objectURL=AnnotationHelper.PathToURL(mProps.getDestinationRootPath(), parameters.get("path"));
							if(!parameters.containsKey("tag")) parameters.put("tag", "");
							content=StringHelper.MapToJSONString(AnnotationGenerator.COMETMetadataMapFromURL(objectURL, parameters.get("tag")));
							
						//	ScreenLog.out("returning search results:::"+content);
							
						} catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }
					}
					writer=response.getWriter();
					writer.write(content);
					//content=FilePath2Output(AnnotationHelper.PathToURL(mProps.getDestinationRootPath(), parameters.get("path")),parameters.get("tag")));
				break;
				case RT_ARCHIVE:
					ScreenLog.begin("DownloadEngine:::Archive Object");
					
					
					CometProperties.setConfigMode(parameters.containsKey("config"));
					
					writer=response.getWriter();
					
					URL from_url=AnnotationHelper.PathToURL(CometProperties.getInstance().getDestinationRootPath(), parameters.get("path"));
					URL to_url=AnnotationHelper.PathToURL(CometProperties.getInstance().getDestinationRootPath(), "/archive/"+new File(parameters.get("path")).getName());
					
//					ScreenLog.out("\n\n\nfrom_url====="+from_url+"\n\n\n");
//					ScreenLog.out("\n\n\nto_url====="+to_url+"\n\n\n");
					
					writer.write(client.HttpDeepCopyHCPContent(from_url, to_url));
					writer.write(client.HttpDeleteHCPContent(from_url));
					
					ScreenLog.end("DownloadEngine:::Archive Object");
					CometProperties.setConfigMode(false);

				break;
				default:
					//TODO: throw exception here?
					ScreenLog.out("illegal type");
				break;
			} 
			ScreenLog.out("END === DownloadEngine::Relay()");
	}

	
	   private void ServeLocalFileRanged
       (HttpServletRequest request, HttpServletResponse response, File file)
           throws IOException
   {
       // Validate the requested file ------------------------------------------------------------

       // Get requested file by path info.
//       String requestedFile = request.getPathInfo();
//       String requestedFile = request.getParameter("path");

       // Check if file is actually supplied to the request URL.
 /*      if (requestedFile == null) {
           // Do your thing if the file is not supplied to the request URL.
           // Throw an exception, or send 404, or show default/warning page, or just ignore it.
           response.sendError(HttpServletResponse.SC_NOT_FOUND);
           return;
       }

       // URL-decode the file name (might contain spaces and on) and prepare file object.
       File file = new File(basePath, URLDecoder.decode(requestedFile, "UTF-8"));
*/
       // Check if file actually exists in filesystem.
       if (!file.exists()) {
           // Do your thing if the file appears to be non-existing.
           // Throw an exception, or send 404, or show default/warning page, or just ignore it.
           response.sendError(HttpServletResponse.SC_NOT_FOUND);
           return;
       }

       // Prepare some variables. The ETag is an unique identifier of the file.
       String fileName = file.getName();
       long length = file.length();
       long lastModified = file.lastModified();
       String eTag = fileName + "_" + length + "_" + lastModified;
       long expires = System.currentTimeMillis() + DEFAULT_EXPIRE_TIME;


       // Validate request headers for caching ---------------------------------------------------

       // If-None-Match header should contain "*" or ETag. If so, then return 304.
       String ifNoneMatch = request.getHeader("If-None-Match");
       if (ifNoneMatch != null && ServletHelper.matches(ifNoneMatch, eTag)) {
           response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
           response.setHeader("ETag", eTag); // Required in 304.
           response.setDateHeader("Expires", expires); // Postpone cache with 1 week.
           return;
       }

       // If-Modified-Since header should be greater than LastModified. If so, then return 304.
       // This header is ignored if any If-None-Match header is specified.
       long ifModifiedSince = request.getDateHeader("If-Modified-Since");
       if (ifNoneMatch == null && ifModifiedSince != -1 && ifModifiedSince + 1000 > lastModified) {
           response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
           response.setHeader("ETag", eTag); // Required in 304.
           response.setDateHeader("Expires", expires); // Postpone cache with 1 week.
           return;
       }


       // Validate request headers for resume ----------------------------------------------------

       // If-Match header should contain "*" or ETag. If not, then return 412.
       String ifMatch = request.getHeader("If-Match");
       if (ifMatch != null && !ServletHelper.matches(ifMatch, eTag)) {
           response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
           return;
       }

       // If-Unmodified-Since header should be greater than LastModified. If not, then return 412.
       long ifUnmodifiedSince = request.getDateHeader("If-Unmodified-Since");
       if (ifUnmodifiedSince != -1 && ifUnmodifiedSince + 1000 <= lastModified) {
           response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
           return;
       }


       // Validate and process range -------------------------------------------------------------

       // Prepare some variables. The full Range represents the complete file.
       Range full = new Range(0, length - 1, length);
       List<Range> ranges = new ArrayList<Range>();

       // Validate and process Range and If-Range headers.
       String range = request.getHeader("Range");
       if (range != null) {

           // Range header should match format "bytes=n-n,n-n,n-n...". If not, then return 416.
           if (!range.matches("^bytes=\\d*-\\d*(,\\d*-\\d*)*$")) {
               response.setHeader("Content-Range", "bytes */" + length); // Required in 416.
               response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
               return;
           }

           // If-Range header should either match ETag or be greater then LastModified. If not,
           // then return full file.
           String ifRange = request.getHeader("If-Range");
           if (ifRange != null && !ifRange.equals(eTag)) {
               try {
                   long ifRangeTime = request.getDateHeader("If-Range"); // Throws IAE if invalid.
                   if (ifRangeTime != -1 && ifRangeTime + 1000 < lastModified) {
                       ranges.add(full);
                   }
               } catch (IllegalArgumentException ignore) {
                   ranges.add(full);
               }
           }

           // If any valid If-Range header, then process each part of byte range.
           if (ranges.isEmpty()) {
               for (String part : range.substring(6).split(",")) {
                   // Assuming a file with length of 100, the following examples returns bytes at:
                   // 50-80 (50 to 80), 40- (40 to length=100), -20 (length-20=80 to length=100).
                   long start = ServletHelper.sublong(part, 0, part.indexOf("-"));
                   long end = ServletHelper.sublong(part, part.indexOf("-") + 1, part.length());

                   if (start == -1) {
                       start = length - end;
                       end = length - 1;
                   } else if (end == -1 || end > length - 1) {
                       end = length - 1;
                   }

                   // Check if Range is syntactically valid. If not, then return 416.
                   if (start > end) {
                       response.setHeader("Content-Range", "bytes */" + length); // Required in 416.
                       response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                       return;
                   }

                   // Add range.
                   ranges.add(new Range(start, end, length));
               }
           }
       }


       // Prepare and initialize response --------------------------------------------------------

       // Get content type by file name and set default GZIP support and content disposition.
       String contentType = null; //getServletContext().getMimeType(fileName);
       boolean acceptsGzip = false;
       String disposition = "inline";

       // If content type is unknown, then set the default value.
       // For all content types, see: http://www.w3schools.com/media/media_mimeref.asp
       // To add new content types, add new mime-mapping entry in web.xml.
       if (contentType == null) {
           contentType = "application/octet-stream";
       }

       // If content type is text, then determine whether GZIP content encoding is supported by
       // the browser and expand content type with the one and right character encoding.
       if (contentType.startsWith("text")) {
           String acceptEncoding = request.getHeader("Accept-Encoding");
           acceptsGzip = acceptEncoding != null && ServletHelper.accepts(acceptEncoding, "gzip");
           contentType += ";charset=UTF-8";
       } 

       // Else, expect for images, determine content disposition. If content type is supported by
       // the browser, then set to inline, else attachment which will pop a 'save as' dialogue.
       else if (!contentType.startsWith("image")) {
           String accept = request.getHeader("Accept");
           disposition = accept != null && ServletHelper.accepts(accept, contentType) ? "inline" : "attachment";
       }

       // Initialize response.
       response.reset();
       response.setBufferSize(DEFAULT_BUFFER_SIZE);
       response.setHeader("Content-Disposition", disposition + ";filename=\"" + fileName + "\"");
       response.setHeader("Accept-Ranges", "bytes");
       response.setHeader("ETag", eTag);
       response.setDateHeader("Last-Modified", lastModified);
       response.setDateHeader("Expires", expires);


       // Send requested file (part(s)) to client ------------------------------------------------

       // Prepare streams.
       RandomAccessFile input = null;
       OutputStream output = null;

       try {
           // Open streams.
           input = new RandomAccessFile(file, "r");
           output = response.getOutputStream();

           if (ranges.isEmpty() || ranges.get(0) == full) {

               // Return full file.
        	   Range r = full;
               response.setContentType(contentType);
               response.setHeader("Content-Range", "bytes " + r.start + "-" + r.end + "/" + r.total);

               //if (content) {
                   if (acceptsGzip) {
                       // The browser accepts GZIP, so GZIP the content.
                       response.setHeader("Content-Encoding", "gzip");
                       output = new GZIPOutputStream(output, DEFAULT_BUFFER_SIZE);
                   } else {
                       // Content length is not directly predictable in case of GZIP.
                       // So only add it if there is no means of GZIP, else browser will hang.
                       response.setHeader("Content-Length", String.valueOf(r.length));
                   }

                   // Copy full range.
                   HCPClient.FileToOutputStream(input, output, r.start, r.length);
               //}

           } else if (ranges.size() == 1) {

               // Return single part of file.
               Range r = ranges.get(0);
               response.setContentType(contentType);
               response.setHeader("Content-Range", "bytes " + r.start + "-" + r.end + "/" + r.total);
               response.setHeader("Content-Length", String.valueOf(r.length));
               response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.

//               if (content) {
                   // Copy single part range.
            	   HCPClient.FileToOutputStream(input, output, r.start, r.length);
  //             }

           } else {

               // Return multiple parts of file.
               response.setContentType("multipart/byteranges; boundary=" + MULTIPART_BOUNDARY);
               response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.

//               if (content) {
                   // Cast back to ServletOutputStream to get the easy println methods.
                   ServletOutputStream sos = (ServletOutputStream) output;

                   // Copy multi part range.
                   for (Range r : ranges) {
                       // Add multipart boundary and header fields for every range.
                       sos.println();
                       sos.println("--" + MULTIPART_BOUNDARY);
                       sos.println("Content-Type: " + contentType);
                       sos.println("Content-Range: bytes " + r.start + "-" + r.end + "/" + r.total);

                       // Copy single part range of multi part range.
                       HCPClient.FileToOutputStream(input, output, r.start, r.length);
                   }

                   // End with multipart boundary.
                   sos.println();
                   sos.println("--" + MULTIPART_BOUNDARY + "--");
          //     }
           }
       } finally {
           // Gently close streams.
           ServletHelper.close(output);
           ServletHelper.close(input);
       }
   }

	
	
	//TODO: consider moving to library
	public static void DumpFileToOutputStream(File infile, OutputStream os, int tail) throws IOException {
		if(!infile.exists()) return;
		
		//read from file src
		FileInputStream fis=new FileInputStream(infile);
		//write to response.writer
		//(new InputStreamEntity( fis.getInputStream(),
		int num;
		byte buf[] = new byte[4096];
		DataInputStream dis = new DataInputStream(fis);
		int accumulation=0;
		while((num = dis.read(buf)) != -1){ 
			accumulation+=num;
			if(tail==0 || accumulation>infile.length()-tail) os.write(buf, 0, num);
		}
		dis.close();
	}

	public static void beginDownload(HttpServletRequest request, HttpServletResponse response, String path, boolean askml, boolean askmlpath) {
		if(path.contains(",")) {
			if(askml) {
				DownloadEngine.downloadMultiKMLInstance(path,response);
			} else {
				DownloadEngine.bulkDownload(path, response);
			}
		} else {
			Map<String,String> parameters=new HashMap<String,String>();
			parameters.put("path",path);
			if(askml) {
				parameters.put("saveas",URLtoJustFileName(path) + ".path.kml");
				parameters.put("annotation","kml_path");
				parameters.put("type","generate");
			} else {
				parameters.put("type","object");
			}	
			try {
				DownloadEngine de=new DownloadEngine();
				de.Relay(request,response,true);
			} catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }
		}
	}
	
	public static void downloadMultiKMLInstance(String multi_path, HttpServletResponse response) {
		String [] paths_to_include=multi_path.split(",");	
		
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Pragma", "no-cache");
		CometProperties mProps=CometProperties.getInstance();
		PrintWriter writer =null;

		//TODO: should be configurable
		response.setContentType("application/vnd.google-earth.kml+xml");
		response.setHeader("Content-Disposition","filename=\"results.kml\"");
		try {
			writer=response.getWriter();
			writer.write(XMLHelper.DocToString(AnnotationGenerator.KMLTour(new URL(mProps.getImagePrefix()),paths_to_include)));
		} catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }
	}

	public static String URLtoJustFileName(String url) {
		String []parts = url.split("/");
		return parts[parts.length-1];
	}
	

	//TODO: zip file should include path structure as it exists on HCP
	// consider moving parts of this into a standard function
	// consider creating the zip on disk so we don't run out of memory
	public static void bulkDownload (String file_path, HttpServletResponse response){
		ScreenLog.begin("bulkDownload(file_path="+file_path+")");
		try {
			//got our url file list
			String []fileList = file_path.split(",");
			byte[] buf = new byte[2048];
			//create zip output stream to reponse
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ZipOutputStream out = new ZipOutputStream(baos);
	
			//Create list for file URLs - these are files from all different locations
			//..code to add URLs to the list
			// Create the ZIP file
			// Compress the files
	   
			String fp="";
			CometProperties mprop=CometProperties.getInstance();
			HCPClient client=new HCPClient(mprop);

			for (int i=0; i<fileList.length; i++) {
				//	FileInputStream fis;
				//	fis = new FileInputStream();
				
				fp=fileList[i].toString();
				if(!fp.contains("http")) fp=mprop.getDestinationRootPath().toString()+fp;
				ScreenLog.out("adding filename to zip: "+fp);
				
				BufferedInputStream bis = new BufferedInputStream(client.HttpGetHCPContentStream(new URL(fp)));
				// Add ZIP entry to output stream.
				//File file = new File(fileList[i].toString());
				//  		String entryname = file.getName();
				out.putNextEntry(new ZipEntry(URLtoJustFileName(fileList[i].toString())));
				int bytesRead;
				while ((bytesRead = bis.read(buf)) != -1) {
					out.write(buf, 0, bytesRead);
				}
				out.closeEntry();
				bis.close();
			}
			out.flush();
			baos.flush();
			out.close();
			baos.close();
			ServletOutputStream sos = response.getOutputStream();
			response.setContentType("application/zip");
			response.setHeader("Content-Disposition", "attachment; filename=\"download.zip\"");
			sos.write(baos.toByteArray());
			out.flush();
			out.close();
			sos.flush();
		} catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }
		return;
	}
	
    public static String getFileName(final Part part) {
    	for (String content : part.getHeader("content-disposition").split(";")) {
    		if (content.trim().startsWith("filename")) { 
    			return content.substring(content.indexOf('=') + 1).trim().replace("\"", "");
    		}
    	}
    	return null;
    }

	public static void beginUpload(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    response.setContentType("text/html;charset=UTF-8");

	    // Create path components to save the file
	    final String path = CometProperties.getInstance().getSourcePath()+"/"+request.getParameter("uploaddir");
	    final Part filePart = request.getPart("file");
	    final String fileName = getFileName(filePart);

	    OutputStream out = null;
	    InputStream filecontent = null;
	    final PrintWriter writer = response.getWriter();
	    try {
	        out = new FileOutputStream(new File(path + File.separator + fileName));
	        filecontent = filePart.getInputStream();
	        int read = 0;
	        final byte[] bytes = new byte[1024];

	        while ((read = filecontent.read(bytes)) != -1) {
	            out.write(bytes, 0, read);
	        }
	        writer.println("New file " + fileName + " created at " + path);
	        ScreenLog.out("INFO: file "+fileName+" uploading to path "+path);
	    } catch (FileNotFoundException fne) {
	        writer.println("You either did not specify a file to upload or are "
	                + "trying to upload a file to a protected or nonexistent "
	                + "location.");
	        writer.println("<br/> ERROR: " + fne.getMessage());
	        ScreenLog.out("SEVERE: "+fne.getMessage());
	    } finally {
	        if (out != null) {
	            out.close();
	        }
	        if (filecontent != null) {
	            filecontent.close();
	        }
	        if (writer != null) {
	            writer.close();
	        }
	    }
	    
	    ScreenLog.end("Upload");
	}

	public static void RMdir(HttpServletRequest request, HttpServletResponse response) {
	    // Create path components to save the file
	    final String path = CometProperties.getInstance().getSourcePath()+request.getParameter("dir");
	    //ScreenLog.out("subdir to remove = "+path);
	    try {
	    	File tempFile = new File(path);
	    	if(!tempFile.delete()) {
	    			ScreenLog.out("Unable to delete path: "+path);
	    	}
		} catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }
		
	}

	public static void Mkdir(HttpServletRequest request,
			HttpServletResponse response) {
		// TODO Auto-generated method stub
	    ScreenLog.out("subdir to create = "+request.getParameter("dir"));

	    // Create path components to save the file
	    final String path = CometProperties.getInstance().getSourcePath()+request.getParameter("dir");

	    File theDir = new File(path);

	    // if the directory does not exist, create it
	    if (!theDir.exists()) {
	    	//ScreenLog.out("creating directory: " + path);
	    	boolean result = theDir.mkdir();  
	    	if(result){    
	    		ScreenLog.out("DIR created");  
	    	}
	    }
	}
	
	public static void TaglistTest(HttpServletRequest request, HttpServletResponse response) {
		try {
			response.setHeader("Cache-Control", "no-cache");
			response.setHeader("Pragma", "no-cache");
			PrintWriter writer=response.getWriter();		
			writer.write(TaglistMap.SelfTest(ServletHelper.GetParameterMap(request)));
		} catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }
	}

	public static void RelayDelete(HttpServletRequest request, HttpServletResponse response)  {
		try {
			response.setHeader("Cache-Control", "no-cache");
			response.setHeader("Pragma", "no-cache");
			Map<String, String> parameters=ServletHelper.GetParameterMap(request);
			DownloadEngine de=new DownloadEngine();
			de.RelayDelete(parameters, response);
		} catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }
	} //end Relay front end
	
		
	public void RelayDelete(Map<String,String> parameters, HttpServletResponse response) {
		ScreenLog.begin("RelayDelete");
		PrintWriter writer;
		try {
			HCPClient client=new HCPClient(CometProperties.getInstance());
			CometProperties.setConfigMode(parameters.containsKey("config"));
			writer = response.getWriter();
			ScreenLog.out("want to delete:",parameters.get("path"));
			writer.write("delete: "+parameters.get("path"));
			
			URL url=AnnotationHelper.PathToURL(CometProperties.getInstance().getDestinationRootPath(),parameters.get("path"));
			
			ScreenLog.out("url="+url);
			
			client.HttpDeleteHCPContent(url);
			
			
		} catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }

		CometProperties.setConfigMode(false);

		ScreenLog.end("RelayDelete");
	}
}
 
