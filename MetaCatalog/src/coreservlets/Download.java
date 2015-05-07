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
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import main.DownloadEngine;
//Servlet implementation class Download to wrap Download engine

//move code into main

@WebServlet(name = "download", urlPatterns= { "/Download" })
public class Download extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public Download() { super(); }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String download = request.getParameter("filename");
		
		String a=request.getParameter("askml");
		String b=request.getParameter("askml_path");
		
		boolean askml=(null!=a && a.equals("true"));
		boolean askmlpath=(null!=b && b.equals("true"));
			
		if (download == null || download == "") {
			response.setContentType("text/html; charset=Windows-31J");
	        PrintWriter out = response.getWriter();
	        out.println("<html>");
	        out.println("<body>");
	        out.println("<head>");
	        out.println("<title>Download Error</title>");
	        out.println("</head>");
	        out.println("<h3>Download Error:no filename to download</h3>");
	        out.println("</body>");
	        out.println("</html>");
	        out.close();
		} 
		else {
			DownloadEngine.beginDownload(request, response, download, askml, askmlpath); 
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response);
	}
}
