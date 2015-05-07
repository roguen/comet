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

//NOTE: this code was originally developed by Cliff Grimm <clifford.grimm@hds.com>

package ingestor.metadata;

import java.io.File;

import com.hdsfed.cometapi.AnnotationHelper;
import com.hdsfed.cometapi.ExtendedLogger;
import com.hdsfed.cometapi.StringHelper;
import com.hdsfed.cometapi.XMLHelper;

//TODO: Need to figure out how to create multiple JNI generators
public class JNIFileInfoGenerator extends BaseMetadataGenerator {

	private static ExtendedLogger ScreenLog = new ExtendedLogger(JNIFileInfoGenerator.class.getPackage().getName());
	private JNIWrapper caller;
	
	public void initialize(Object inParam) {
		caller = new JNIWrapper();
		//don't initialize until we need it
		caller.initialize(null);
		return;
	}
	
	public String getMetadata(File inSourceFile) {
		ScreenLog.begin("JNI:::getMetadata("+inSourceFile+")");
		String override=getOverrideMetadata(inSourceFile);
		if(override!="") {
			ScreenLog.end("JNI:::getMetadata("+inSourceFile+") = returning override of size  "+override.length());
			return override;
		}
		
		String retval="";
		byte buf[] = null;
		try {
			//short circuit extraction for dedicated thumb nail images
			
			//TODO: file name exceptions should be configurable
			if(inSourceFile.getCanonicalPath().contains(".thumb.") ||
			   inSourceFile.getCanonicalPath().contains("datasource_")) {
				return new String("");
			}
			buf = caller.getCustomMetadata(inSourceFile.getCanonicalPath());
			if(buf==null) return "";
			//check for odd escape characters
			String s=new String(buf);
			retval=StringHelper.NoEscape(s);
		
			if(!AnnotationHelper.ValidateGeo(XMLHelper.StringToDoc(retval),getAnnotation())) retval="";
		} catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }
		ScreenLog.end("JNI:::getMetadata("+inSourceFile+") = "+retval.length());
		return retval;
	}
}
