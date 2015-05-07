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

import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hdsfed.cometapi.CometProperties;
//TODO: move code into main; move exceptions out
@WebServlet("/Version")
public class CometVersionServlet extends HttpServlet {
	private static final String VERSION ="1.21.0";
	private static final String COMPILE_DATE ="2015-05-06";
	private static final String SVN ="554";
	private static final long serialVersionUID = 1L;
	 public CometVersionServlet() {
	        super();
	        // TODO Auto-generated constructor stub
	    }
	 
	 	protected String run() {
	 		CometProperties.resetInstance();
	 		return "{ \"metacatalog_ver\" : \""+VERSION+" r"+SVN+"\"," +
	 						"\"library_ver\" : \""+CometProperties.getVersion()+" r"+CometProperties.getSvn()+"\"," +
	 						"\"metacatalog_compiled_date\" : \""+COMPILE_DATE+"\"," +
	 						"\"library_compile_date\" : \""+CometProperties.getCompileDate()+"\" }";
	 	}

	 	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
			try {
				PrintWriter writer=response.getWriter();
				writer.write(run());
			} catch (Exception ignore) {}
		}

	 	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
			try { doGet(request,response); } catch (Exception ignore) {} 
		}

	 	protected void doPut(HttpServletRequest request, HttpServletResponse response) {
			try { doGet(request,response); } catch (Exception ignore) {} 
		}

	 	public static String getVersion() {
			return VERSION;
		}

		public static String getCompileDate() {
			return COMPILE_DATE;
		}

		public static String getSvn() {
			return SVN;
		}
}
