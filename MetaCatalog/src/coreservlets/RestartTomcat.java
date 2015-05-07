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
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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

@WebServlet("/RestartTomcat")
public class RestartTomcat extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public RestartTomcat() { super();  }

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

		
		if(request.getParameterMap().containsKey("apikey") && request.getParameter("apikey").equals("c025350c53e41d249378f3fe2a9a0017")) {
			writer.println("restarting tomcat");
			
				String cmd = "/etc/rc.d/init.d/tomcat restart" ;
				//logger.info("run command: " + cmd);
				Runtime run = Runtime.getRuntime() ;
				Process pr = null;
				pr=run.exec(cmd) ;
				try {
					pr.waitFor() ;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				BufferedReader buf = new BufferedReader( new InputStreamReader( pr.getInputStream() ) ) ;
				writer.println(buf.readLine());
			
			
			
			
		} else {
			writer.println("invalid!");
		}
		
	}

}
