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

package ingestor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;

import com.hdsfed.cometapi.AnnotationHelper;
import com.hdsfed.cometapi.ExtendedLogger;

//TODO: make this class do something useful 
//NOTE: this class is currently a work-in-progress and is not currently used		

public class FileSystemWatcher {
	private static ExtendedLogger ScreenLog = new ExtendedLogger(AnnotationHelper.class.getPackage().getName());

	private boolean idle=true;
	Boolean getIdle() {
		return idle;
	}
	
	void setIdle(Boolean _newidle) {
		idle=_newidle;
	}
	
	void InitWatcher(String p) {
		Path myDir=Paths.get(p);
		File f=null;

		WatchService watcher;
		try {
			watcher = myDir.getFileSystem().newWatchService();
			//only care about newly created files
			myDir.register(watcher,StandardWatchEventKinds.ENTRY_CREATE);
			
			WatchKey watckKey=watcher.take();
			
			List<WatchEvent<?>>events=watckKey.pollEvents();
			for(@SuppressWarnings("rawtypes") WatchEvent event:events){
				if(event.kind()==StandardWatchEventKinds.ENTRY_CREATE){
					ScreenLog.out("externally, the file:"+event.context().toString()+" was created");
					//ScreenLog.out("\tneed to reset watcher");
					//myDir.register(watcher,StandardWatchEventKinds.ENTRY_CREATE);
					//watckKey=watcher.take();
					idle=false;
					InitWatcher(p);
					f=new File(p+"/"+event.context());
					ScreenLog.out("\tchecking to see if new inode is a directory: "+p+event.context());
					if(f.isDirectory()) InitWatcher(f.getPath());
				} // end if event.kind...
			} //end for watchevent
		} catch (IOException | InterruptedException e) { ScreenLog.ExceptionOutputHandler(e); }
	} //end InitWatcher
};
