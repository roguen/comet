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
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.hdsfed.cometapi.CometProperties;
import com.hdsfed.cometapi.ServletHelper;
import com.hdsfed.cometapi.StringHelper;

//TODO: consider renaming to just "properties"
//		move functionality into relay (?)

@WebServlet("/get-properties")
public class GetProperties extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public GetProperties() { super(); }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    response.setHeader("Cache-Control", "no-cache");
	    response.setHeader("Pragma", "no-cache");
	    Map<String, String> parameters=ServletHelper.GetParameterMap(request);
		PrintWriter writer=response.getWriter();		
		parameters.put("comet_version",CometVersionServlet.getVersion());


        //add the session to the parameters list
        parameters=ServletHelper.SessionManager(request,parameters);
        
        
		writer.write(StringHelper.MapToJSONString(CometProperties.getPropertiesMap(parameters)));
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
