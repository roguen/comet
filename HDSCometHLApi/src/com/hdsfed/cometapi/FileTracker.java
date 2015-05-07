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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.LinkedList;


public class FileTracker {
	private static ExtendedLogger ScreenLog = new ExtendedLogger(FileTracker.class.getName());

	
	private static LinkedList<String> mUploaded=null;


	private static FileTracker instance;


	private static Boolean mUseFileCache=false;
	private static String mFileCache="";
	private static int maximumFiles=1000000;
	//private static String prefix="";
	private enum FTActionType {
		FT_INIT,
		//FT_LOAD,
		FT_REMOVE_FIRST, //remove first element because we're beyond capacity
		FT_ADD, //add to end
		FT_REMOVE, // remove specific object from list
		FT_CONTAINS,
		FT_SORT,
		FT_SAVE,
		FT_UNSUPPORTED;
	}
	
	public static FileTracker getInstance() {
		if (instance == null) {
			instance = new FileTracker();
			instance.UploadedLL(FileTracker.FTActionType.FT_INIT, null);
		}
		return instance;
	}
		
	public synchronized int getUploadedSize() {
		return mUploaded.size();
	}	
	
	public static int getSize() {
		return getInstance().getUploadedSize();
	}
	
	public static void init(Boolean inUseFileCache, String inFileCache, int maximumFiles) {
		FileTracker ft=getInstance();
		ft.setmUseFileCache(inUseFileCache);
		//ft.setPrefix(inPrefix);
		if(maximumFiles!=0) ft.setMaximumFiles(maximumFiles);
		ft.UploadedLL(FileTracker.FTActionType.FT_INIT, inFileCache);
		
	}

//	private void setPrefix(String inPrefix) {
//		prefix=inPrefix;
//	}

	public static void removeFirst() {
		getInstance().UploadedLL(FileTracker.FTActionType.FT_REMOVE_FIRST, null);
	}

	public static void remove(String s) {
		getInstance().UploadedLL(FileTracker.FTActionType.FT_REMOVE, s);
	}
	
	public static void add(String s) {
		getInstance().UploadedLL(FileTracker.FTActionType.FT_ADD, s);
	}
	public static void flush(String s) {
		FileTracker ull=getInstance();
		ull.UploadedLL(FileTracker.FTActionType.FT_SAVE, s);
	}

	public static Boolean contains(String s) {
		return getInstance().UploadedLL(FileTracker.FTActionType.FT_CONTAINS, s);
	}
	
	public static void sort() {
		getInstance().UploadedLL(FileTracker.FTActionType.FT_SORT, null);
	}

	public static Boolean getmUseFileCache() {
		return FileTracker.mUseFileCache;
	}

	public void setmUseFileCache(Boolean mUseFileCache) {
		FileTracker.mUseFileCache = mUseFileCache;
	}
	
	//purpose of this function is to make sure that only 1 thread
	//can read/write/add/remove mUploaded at a time.
//	@SuppressWarnings("unchecked")
//	public synchronized Boolean UploadedLL(FTActionType action, String s) {
//		return getInstance().UploadedLL(action,s,null);
//	}	

	@SuppressWarnings("unchecked")
	public synchronized Boolean UploadedLL(FTActionType action, String s) {
		Boolean retval=false;
		switch(action) {
			case FT_INIT:
				mUploaded=null;
				mUploaded=new LinkedList<String>();
				
				ScreenLog.out("size of newly formed list is "+mUploaded.size());
				retval=true;
//			break;	
//			case FT_LOAD:
				if(mUseFileCache) {
					if(s!=null && !s.equals("")) mFileCache=s;
					
					if(mFileCache.equals("")) mFileCache=CometProperties.getInstance().getFileProcessedCache();
					
					File loaderFile = new File(mFileCache);
					try {
						
						
						if(!loaderFile.exists()) {
							ScreenLog.fine("file :"+loaderFile+" does not exist, so skip load.");
							
							return true;
						}
						FileInputStream fis=new FileInputStream(loaderFile);
						ObjectInputStream ois=new ObjectInputStream(fis);
						
						mUploaded=(LinkedList<String>) ois.readObject();
						fis.close();
						ois.close();
					} catch ( FileNotFoundException e){
						ScreenLog.ExceptionOutputHandler(e);
					} catch (Exception e) {
						ScreenLog.ExceptionOutputHandler(e);
					}
				}
				mFileCache=s;	
				retval=true;

				
				
			break;
			case FT_SAVE:
				
				if(!mUseFileCache) {
					retval=true;
				} else {
					retval=false;
				
					if((mFileCache==null || mFileCache.equals("")) && s!=null) {
						mFileCache=s;
					} else if(mFileCache==null || mFileCache.equals("")) {
						mFileCache=CometProperties.getInstance().getFileProcessedCache();
					}
				
					File saveFile = new File(mFileCache);
					try {
						FileOutputStream fos = new FileOutputStream(saveFile);
						ObjectOutputStream oos = new ObjectOutputStream(fos);
						oos.writeObject(mUploaded);
						fos.close();
						oos.close();
						retval=true;
					} catch (Exception e) {
						ScreenLog.ExceptionOutputHandler(e);
					}
				}
			break;
			
			case FT_REMOVE_FIRST:
				mUploaded.removeFirst();
				retval=true;
			break;
			case FT_REMOVE:
				if(null==s || s.equals("")) return false;
				mUploaded.remove(s);
				retval=true;
			break;
			case FT_ADD:
				if(null==s || s.equals("")) return false;
				
				if(mUploaded.size()<maximumFiles) mUploaded.addLast(s);
				retval=true;
			break;
			//Probably should update this to use Collections.binarysearch()
			case FT_CONTAINS:
				if(null==s || s.equals("")) return false;
				retval=mUploaded.contains(s);
			break;
			case FT_SORT:
				Collections.sort(mUploaded);
				retval=true;
			break;
			default:
			case FT_UNSUPPORTED:
				ScreenLog.severe("unsupported LL action");
				retval=false;
			break;
		}
		return retval;
	}

	@SuppressWarnings("unchecked")
	public static LinkedList<String> GetList() {
		LinkedList<String> local_list=new LinkedList<String>();
		String mFileCache=CometProperties.getInstance().getFileProcessedCache();
		File loaderFile = new File(mFileCache);
		try {
			if(!loaderFile.exists()) {
					ScreenLog.fine("file :"+loaderFile+" does not exist, so skip load.");
					return null;
			}	
			FileInputStream fis=new FileInputStream(loaderFile);
			ObjectInputStream ois=new ObjectInputStream(fis);
			local_list=(LinkedList<String>) ois.readObject();
			fis.close();
			ois.close();
		} catch ( FileNotFoundException e){
				ScreenLog.ExceptionOutputHandler(e);
		} catch (Exception e) {
			ScreenLog.ExceptionOutputHandler(e);
		}
		return local_list;
	}
	
	public static int getMaximumFiles() {
		return maximumFiles;
	}

	public void setMaximumFiles(int maximumFiles) {
		FileTracker.maximumFiles = maximumFiles;
	}

	
}	
