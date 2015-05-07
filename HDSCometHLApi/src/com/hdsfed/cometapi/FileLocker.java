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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;


//purpose: provide a switchable locking/non-locking fileinputstream wrapper class
//NOTE: this class is a work-in-progress and not currently used
//TODO: make it do something useful ;^)

public class FileLocker {
	private static ExtendedLogger ScreenLog = new ExtendedLogger(FileLocker.class.getPackage().getName());

	private RandomAccessFile raf;
	private FileLock fileLock;
	private Boolean locked;
	private Boolean lockable;
	private boolean allowfallback;

	//default behavior is to lock
	// but setting lockable to false prior to opening can use a straight up fis
	public FileLocker() {
		raf=null;
		fileLock=null;
		locked=false;
		lockable=true;
		allowfallback=true;
	}
	
	public void open(File inFile) {
		try {
			raf=new RandomAccessFile(inFile, "rw");
			if(lockable) {
				fileLock=raf.getChannel().lock();
			} 
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			
			if(lockable && e.getMessage().toLowerCase().contains("permission denied") && allowfallback) { 
				ScreenLog.out("file not found error due to permissions, most likely due to file locking ("+inFile.getAbsolutePath()+")");
				ScreenLog.out("\tturning off file locking for this instance  ("+inFile.getAbsolutePath()+")");
				setLockable(false);
				//try again, without locking
				try {
					raf=new RandomAccessFile(inFile, "r");
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					ScreenLog.ExceptionOutputHandler(e1);
				}
			} else {
				ScreenLog.out("lockable="+lockable);
				ScreenLog.out("error msg="+e.getMessage());
				//if a file is inaccessible, it is essentially in the same state as being locked
				setLocked(true);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			ScreenLog.ExceptionOutputHandler(e);
		} catch (OverlappingFileLockException e) {	
			setLocked(true);
			ScreenLog.ExceptionOutputHandler(e);
		} finally {
		 	close();
		}
	}
	
	public void close() {
		try {
			if(lockable) {
				if(fileLock!=null) fileLock.release();
				if(raf!=null) raf.close();
			} 
        } catch (Exception ignore) {}
	}


	public Boolean getLocked() {
		return locked;
	}

	public void setLocked(Boolean locked) {
		this.locked = locked;
	}
	public FileInputStream getInputStream() throws IOException {
		return new FileInputStream(raf.getFD());
	}

	public Boolean getLockable() {
		return lockable;
	}

	public void setLockable(Boolean lockable) {
		this.lockable = lockable;
	}

	public boolean isAllowfallback() {
		return allowfallback;
	}

	public void setAllowFallBack(boolean allowfallback) {
		this.allowfallback = allowfallback;
	}
}


