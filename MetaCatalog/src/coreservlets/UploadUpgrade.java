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
package coreservlets;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.hdsfed.cometapi.AnnotationHelper;
import com.hdsfed.cometapi.CometProperties;
import com.hdsfed.cometapi.ExtendedLogger;
import com.hdsfed.cometapi.HCPClient;
//import java.util.Enumeration;

//TODO: if possible, make fileSizeThreshold configurable
//		this function will likely be rewritten for S3 support

//Servlet implementation class Download to wrap Download engine
@WebServlet("/UploadUpgrade")
@MultipartConfig(location="/tmp/", fileSizeThreshold=5*1024*1024*1024) 
public class UploadUpgrade extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	private static ExtendedLogger ScreenLog = new ExtendedLogger(UploadUpgrade.class.getPackage().getName());

    /**
     * @see HttpServlet#HttpServlet()
     */
    public UploadUpgrade() {
        super();
        // TODO Auto-generated constructor stub
    }
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)  {
		
		//doGet(request,response);
	
			try {
				processRequest(request,response);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	}
	
	@SuppressWarnings("resource")
	protected void processRequestORIG(HttpServletRequest request,
	        HttpServletResponse response)
	        throws ServletException, IOException {
	    response.setContentType("text/html;charset=UTF-8");

	    // Create path components to save the file
//	    final String path = request.getParameter("destination");
	    final String path = "/tmp/";
	    final Part filePart = request.getPart("upgradeFile");
	    final String fileName = getFileName(filePart);

	    OutputStream out = null;
	    InputStream filecontent = null;
	    PrintWriter writer = null;

	    try {
	        out = new FileOutputStream(new File(path + File.separator
	                + fileName));
	        filecontent = filePart.getInputStream();

	        int read = 0;
	        final byte[] bytes = new byte[1024];

	        while ((read = filecontent.read(bytes)) != -1) {
	            out.write(bytes, 0, read);
	        }
	  //      writer.println("New file " + fileName + " uploaded internally");
	        ScreenLog.out("File{0}being uploaded to {1}" + fileName+" "+path);
	    } catch (FileNotFoundException fne) {
	    	 writer=response.getWriter();
	    	 writer.println("You either did not specify a file to upload or are "
	                + "trying to upload a file to a protected or nonexistent "
	                + "location.");
	        writer.println("<br/> ERROR: " + fne.getMessage());

	        ScreenLog.out( "Problems during file upload. Error: {0}" +fne.getMessage());
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
	}

	protected void processRequest(HttpServletRequest request,
	        HttpServletResponse response)
	        throws Exception {
			
			
			File fileName=null;
			String path=null;
			if(request.getParameterMap().containsKey("url") && request.getParameterMap().containsKey("saveAs")) {
				
				ScreenLog.out("\n\nupgradeurl="+request.getParameter("url"));
				ScreenLog.out("\n\nsaveas="+request.getParameter("saveAs"));
				
				
				fileName = new File("/tmp/"+request.getParameter("saveAs"));
				path = CometProperties.getInstance().getUpgradeDir()+"/"+fileName.getName();

				//pull down URL to fileName
				HCPClient client=new HCPClient(CometProperties.getInstance());
				//save a local copy
								
				client.HttpGetHCPContent(new URL(request.getParameter("url")),fileName);
			
			} else {
				final Part filePart = request.getPart("upgradeFile");
				fileName = new File("/tmp/"+getFileName(filePart));
				path = CometProperties.getInstance().getUpgradeDir()+"/"+fileName.getName();
		
				processRequestORIG(request,response);
					
			}
			UploadFileToHCP(fileName, path, request,response);
	}
	
	
	
	@SuppressWarnings("static-access")
	protected void UploadFileToHCP(File localfile, String path, HttpServletRequest request,  HttpServletResponse response) {
    	ScreenLog.begin("upload to HCP");

		PrintWriter writer=null;
		try {
			writer = response.getWriter();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		ScreenLog.out("upload completed, now we want to write "+localfile+" to "+path+" on HCP, in the config namespace");
		
    	HCPClient client=null;
		try {
			client = new HCPClient(CometProperties.getInstance());
		} catch (URISyntaxException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		CometProperties.getInstance().setConfigMode(true);
		ScreenLog.out("want to use ns: "+CometProperties.getInstance().getDestinationHCPNamespace());
		
    	//client.setNamespace(CometProperties.getInstance().getDestinationHCPConfigNamespace());
    	try {

    		client.HttpPutHCPContent(localfile, AnnotationHelper.PathToURL(CometProperties.getInstance().getDestinationRootPath(), path));
    		
    		//client.HttpPutHCPContent(new InputStreamEntity( new FileInputStream(localfile), -1), AnnotationHelper.PathToURL(client.getRootpath(), path));
	        writer.println("File successfully uploaded to HCP as "+path);

	        
    	} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ScreenLog.out("err="+e.getMessage());
			writer.println("<br/> ERROR: " + e.getMessage());
		} finally {
			writer.close();
	        localfile.delete();
			
		}    	
		CometProperties.getInstance().setConfigMode(false);

    	ScreenLog.end("upload to HCP");
	}

	
	
	
	private String getFileName(final Part part) {
	    final String partHeader = part.getHeader("content-disposition");
	    
	    for (String content : partHeader.split(";")) {
	        if (content.trim().startsWith("filename")) {
	            return content.substring(
	                    content.indexOf('=') + 1).trim().replace("\"", "");
	        }
	    }
	    return null;
	}
	
	
}
