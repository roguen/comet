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

import com.hdsfed.cometapi.CometProperties;
import com.hdsfed.cometapi.ServletHelper;
import com.hdsfed.cometapi.StringHelper;

import main.ThreadTracker;

@WebServlet("/extensions.js")
public class Extensions extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public Extensions() { super();  }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter writer=response.getWriter();		
		
		writer.println("//Copyright (c) 2015 Hitachi Data Systems, Inc.");
		writer.println("//All Rights Reserved.");
		writer.println("//");
		writer.println("//   Licensed under the Apache License, Version 2.0 (the \"License\"); you may");
		writer.println("//   not use this file except in compliance with the License. You may obtain");
		writer.println("//   a copy of the License at");
		writer.println("//");
		writer.println("//		         http://www.apache.org/licenses/LICENSE-2.0");
		writer.println("//");
		writer.println("//   Unless required by applicable law or agreed to in writing, software");
		writer.println("//   distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT");
		writer.println("//   WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the");
		writer.println("//   License for the specific language governing permissions and limitations");
		writer.println("//   under the License.");
		writer.println("//");
		writer.println("//Package: COMET Web Application");
		writer.println("//Author: Chris Delezenski <chris.delezenski@hdsfed.com>");
		writer.println("//Compilation Date: 2015-05-06");
		writer.println("//License: Apache License, Version 2.0");
		writer.println("//Version: 1.21.0");
		writer.println("//(RPM) Release: 1");
		writer.println("//SVN: r554");

		writer.println("function CometExt_Init() {");
		
		if(CometProperties.getInstance().getExperimental()) {
			writer.println("\tCometLoading(\"Initializing UI Extensions...\");");
			//add scaffolding for each new tab
			writer.println("}");

			writer.println("function CometExt_LoadComplete() {");
			writer.println("\tCometLoading(\"UI Extensions Complete\");");
			writer.println("\tloadables.ext=true;");
			writer.println("}");
			
		} else {
			writer.println("loadables.ext=true;");
			writer.println("}");
			
		}
	
	}

}
