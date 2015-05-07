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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.hdsfed.cometapi.ExtendedLogger;

//TODO: replace usage of runtime and run.exec with common pipe function
public class PipedInGenerator extends BaseMetadataGenerator {
	private static ExtendedLogger ScreenLog = new ExtendedLogger(PipedInGenerator.class.getPackage().getName());
	
	public void initialize(Object inParam) { return; };
	
	public String getMetadata(File inSourceFile) {
		String override=getOverrideMetadata(inSourceFile);
		if(override!="") return override;
		
		OutputStream outputStream = new ByteArrayOutputStream();

		try {
			cmdline+=" "+inSourceFile.getAbsolutePath();
			ScreenLog.out("run command: " + cmdline);
			
			Runtime run = Runtime.getRuntime() ;
			Process pr = run.exec(cmdline) ;
			BufferedReader buf = new BufferedReader( new InputStreamReader( pr.getInputStream() ) ) ;
			// read everything and output to outputStream as you go
			String s=null;
			while((s = buf.readLine()) != null) { 
					outputStream.write(s.getBytes());
	    	} 
			pr.waitFor() ;
			
		} catch (Exception e) {
			ScreenLog.ExceptionOutputHandler(e);
		}

		return outputStream.toString();
	}

}
