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
//SVN: r551+
package com.hdsfed.cometapi;

//This package will eventually be replaced with logger... maybe

import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

//this class was built originally because logger didn't seem flexible enough
//instead of replacing all the calls to ScreenLog with logger, let's merge
//the two because we're still adding some value here (selective output of maps, parameters and tie-in with debug and verbose)
//TODO: the logger variable should either be local to the client and passed in or generated here, not both
//		some legacy code still has its own logger in addition to ScreenLog
public class ExtendedLogger  {
	private static Logger logger = null; //ExtendedLogger(AnnotationGenerator.class.getPackage().getName());
	private static String this_class=null;
	public ExtendedLogger(String name) {
		logger=Logger.getLogger("");
		this_class=name;
		debug=CometProperties.getInstance().getDebug();
		Boolean v=CometProperties.getInstance().getVerbose();
		silencer=(!v && !debug);
	}
	
	public void setClass(String newclass) {
		this_class=newclass;
	}
	
	public ExtendedLogger(String name, Boolean d, Boolean v) {
		logger=Logger.getLogger(name);
		debug=d;
		silencer=(!v && !d);
	}

	private Boolean debug=true;

	private boolean silencer=false;

	public void begin(String s) {
		if(debug) out("===BEGIN===",s,"===BEGIN===");
	}
	public void end(String s) {
		if(debug) out("=== END ===",s, "=== END ===");
	}
	
	public void out(String s) {
		out("",s, "");
	}
	public void out(String label, String s) {
		out(label,s, label);
	}
	
	public void out(String label, String s, String suffix) {
		if(silencer && !debug) return;
		if(debug) {
			logger.finest("");
			logger.finest("=========================================================");
		}
		if(label.equals("")) logger.warning(this_class+" "+s);
		else logger.info( label+":"+this_class+": \""+s+"\" :"+suffix);
		if(debug) {
			logger.finest("=========================================================");
			logger.finest("");
		}
	}
	
	public void info(String s) {
		if(silencer && !debug) return;
		logger.info(this_class+":: "+s);
	}
	
	public void finer(String s) {
		if(silencer && !debug) return;
		logger.finer(this_class+":: "+s);
	}
	
	public void fine(String s) {
		if(silencer && !debug) return;
		logger.fine(this_class+":: "+s);
	}
	
	public void warning(String s) {
		if(silencer && !debug) return;
		logger.warning(this_class+":: "+s);
	}
	
	//severe errors are never silenced
	public void severe(String s) {
		logger.severe(this_class+":: "+s);
	}
	
	public <T> void out(String label, LinkedList<T> ll) {
		for(int i=0; i<ll.size(); i++) {
			out(label+" "+i+": ",ll.get(i).toString());
		}
	}

	public void out(Map<String,String> keys) {
		//only output full parameter maps with debugging turned on
		if(silencer && !debug) return;
		out(keys,null);
	}

	public void out(Map<String,String> keys, String title) {
		if(!debug && silencer) return;
		out("");
		if(title!=null) out(title);
		if(keys==null) out("\tempty set");
		else {
			for(String key: keys.keySet()) {
				//parameters may contain a password; protect ourselves from accidents
				if(key.toLowerCase().contains("password")) out("\tkey pair="+key+", ********");
				else out("\tkey pair="+key+", "+keys.get(key));
			}	
		}
		out("");
	}

	//very verbose
	//silencer is rendered false when debug are enabled
	public void setDebug(Boolean s) {
		debug=s;
	}
	
	//no output at all, when true
	//minimal output when false
	public void setSilence(Boolean s) {
		silencer=s;
	}
	
	public void ExceptionOutputHandler(Exception e) {
		//if debug, give me the stacktrace, otherwise, just the error message is sufficient
		if(debug) e.printStackTrace();
		logger.severe("Exception Thrown from "+this_class+" error was "+e.getMessage());
	}
		public void log(Level inLevel, String s) {
		if(silencer && !debug) return;
		logger.log(inLevel,this_class+":: "+s);
	}

	public void force(String string) {
		logger.info(string);
		System.out.println(string);
	}

}
