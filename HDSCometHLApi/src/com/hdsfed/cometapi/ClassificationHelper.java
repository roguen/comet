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
//Package: Custom Object Metadata Enhancement Toolkit shared library
//Author: Chris Delezenski <chris.delezenski@hdsfed.com>
//Compilation Date: 2015-05-06
//License: Apache License, Version 2.0
//Version: 1.21.0
//(RPM) Release: 1
//SVN: r554
package com.hdsfed.cometapi;

import java.util.HashMap;
import java.util.Map;

//TODO: classification levels should be read from a file
public class ClassificationHelper {
	private static ExtendedLogger ScreenLog = new ExtendedLogger(ClassificationHelper.class.getPackage().getName());
	static public String Validate(String c) {
		c=c.toUpperCase().trim();
		if(c.equals("TOP SECRET") || c.equals("SECRET") || c.equals("SECRET RELEASABLE") || c.equals("SECRET NO FORN") || c.equals("CONFIDENTIAL") || c.equals("TOP SECRET//SCI") || c.equals("FOUO") || c.equals("RESTRICTED") || c.equals("UNDER NDA") || c.equals("PROPRIETARY")) return c;
		return "UNCLASSIFIED";
	}
	
	private enum classificationType {
		CLASS_TS,
		CLASS_SECRET,
		CLASS_CONFIDENTIAL,
		CLASS_UNCLASSIFIED,
		CLASS_UNSUPPORTED
	};
	
	private static classificationType ClassToEnum(String c) {
		c=Validate(c);
		//TS and TS//SCI
		if(c.toLowerCase().contains("top secret")) return classificationType.CLASS_TS;
		
		//S, S R, and S NO F
		if(c.toLowerCase().contains("secret")) return classificationType.CLASS_SECRET;
		if(c.toLowerCase().contains("under nda")) return classificationType.CLASS_SECRET;
		if(c.toLowerCase().contains("restricted")) return classificationType.CLASS_SECRET;
		if(c.toLowerCase().contains("proprietary")) return classificationType.CLASS_SECRET;
		
		if(c.toLowerCase().contains("confidential")) return classificationType.CLASS_CONFIDENTIAL;
		if(c.toLowerCase().contains("unclassified")) return classificationType.CLASS_UNCLASSIFIED;
		if(c.toLowerCase().contains("fouo")) return classificationType.CLASS_UNCLASSIFIED;
		return classificationType.CLASS_UNSUPPORTED;
	}
	
	static public String GetBGColor(String c) {
		c=Validate(c);
		
		switch(ClassToEnum(c)) {
			case CLASS_TS:	
				return "background-color: rgb(170,170,0)";
			case CLASS_SECRET:
				return "background-color: rgb(255,0,0)";
			case CLASS_CONFIDENTIAL:
				return "background-color: rgb(0,0,170)";
			default: //unclass and fouo 
			break;
		}
		return "background-color: rgb(0,170,0)";
	}
	
	static public String GetFGColor(String c) {
		c=Validate(c);
		
		switch(ClassToEnum(c)) {
			case CLASS_TS:	
				ScreenLog.out("GetFGColor::: returning black, because we're TS!");
				return "color: rgb(0,0,0)";
			case CLASS_SECRET:
			case CLASS_CONFIDENTIAL:
			default: //unclass and fouo 
			break;
		}
		ScreenLog.out("GetFGColor::: returning white!");
		return "color: rgb(255,255,255)";
	}

	public static HTMLDoc ShowClassificationBanner(HTMLDoc htmldoc,String c) throws Exception {
		c=Validate(c);
		Map<String,String> row_class_attribs=new HashMap<String,String>();
		row_class_attribs.put("style", ClassificationHelper.GetBGColor(c));
		row_class_attribs.put("height","10");
		//"background-color: rgb(0, 170, 0)"
		htmldoc.CreateRow(row_class_attribs);

		Map<String,String> col_class_attribs=new HashMap<String,String>();
		col_class_attribs.put("colspan", "2");
		col_class_attribs.put("style", ClassificationHelper.GetFGColor(c));
		//"color: rgb(255,255,255)"
		htmldoc.CreateCol(col_class_attribs);
		htmldoc.writeContent("<center>"+c+" - (simulated)</center>");
		htmldoc.EndColumn();
		htmldoc.EndRow();
		return htmldoc;
	}
}
