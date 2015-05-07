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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

//TODO: Should deprecate this class, and use standard java instead
public class StringHelper {
	private static ExtendedLogger ScreenLog = new ExtendedLogger(StringHelper.class.getName());
	final public static String SRC_FILENAME_CONST="__SRC_FILENAME__";
	final public static String TGT_FILENAME_CONST="__TGT_FILENAME__";
	
	public enum ConditionType {
		CT_STARTS_WITH, //starts with string
		CT_ENDS_WITH, //ends with string
		CT_CONTAINS, //contains string
		CT_IS, // direct match
		CT_ERR
	}
	
	
	static private int SeekLf(String this_string, char c) {
		for (int i=0; i<this_string.length(); i++) {
			if (this_string.charAt(i)==c) return i;	
		}
		return -1;
	}
	
	static public String ChopLf(String this_string, String chomp_off) {
		if(!this_string.contains(chomp_off)) return this_string;
		return this_string.substring(SeekLf(this_string,chomp_off.charAt(0))+1,this_string.length());
	}
	
	static private int SeekRt(String this_string, char c) {
		for (int i=-1; i+this_string.length()!=0; i--) {
			if (this_string.charAt(i)==c) return i;	
		}
		return -1;
	}
	
	static public String ChopRt(String this_string, String chomp_off) {
		if(!this_string.contains(chomp_off)) return this_string;
		return this_string.substring(0,this_string.lastIndexOf(chomp_off));
	}
	
	static public String ChopRt(String this_string, String chomp_off, Integer times) {
		if(times==1) return ChopRt(this_string, chomp_off);
		return ChopRt(this_string,chomp_off,times-1);
	}
	
	static public String ChopLf(String this_string, String chomp_off, Integer times) {
		if(times==1) return ChopLf(this_string,chomp_off);
		return ChopLf(this_string, chomp_off, times-1);
	}
	
	static public String ChopAllLf(String this_string, String chomp_off) { 
		if(!this_string.contains(chomp_off)) return this_string;
		return ChopLf(this_string, chomp_off, SeekRt(this_string,chomp_off.charAt(0))+this_string.length()+1); 
	}
	
	static public String ChopAllRt(String this_string, String chomp_off) {
		if(!this_string.contains(chomp_off)) return this_string;
		return this_string.substring(0,this_string.length()-SeekLf(this_string,chomp_off.charAt(0)));
	}
	
	static public String InputStreamToString(InputStream in) throws IOException {
		StringBuffer out = new StringBuffer();
	    byte[] b = new byte[4096];
	    for (int n; (n = in.read(b)) != -1;) {
	        out.append(new String(b, 0, n));
	    }
	    return out.toString();
	}
	
	static public String BooleanToString(Boolean b) {
			if(b) return "true";
			return "false";
	}

	static public String NoEscape(String s) {
		int length = s.length();
		char[] oldChars = new char[length+1];
		s.getChars(0, length, oldChars, 0);
		oldChars[length]='\0';//avoiding explicit bound check in while
		int newLen=-1;
		while(oldChars[++newLen]>=' ');//find first non-printable,
		                       // if there are none it ends on the null char I appended
		for (int  j = newLen; j < length; j++) {
		    char ch = oldChars[j];
		    if (ch >= ' ' || ch == '\n') {
		        oldChars[newLen] = ch;//the while avoids repeated overwriting here when newLen==j
		        newLen++;
		    }
		}
		s = new String(oldChars, 0, newLen);
		return s;
	}
	
	static public String ZeroPadded(Integer i) {
		String result=""+i;
		if(i<10) result="0"+result;
		return result;
	}
	
	public static String toTitleCase(String _input) {
		String input=_input.toLowerCase();
	    StringBuilder titleCase = new StringBuilder();
	    boolean nextTitleCase = true;
	    for (char c : input.toCharArray()) {
	        if (Character.isSpaceChar(c)) {
	            nextTitleCase = true;
	        } else if (nextTitleCase) {
	            c = Character.toTitleCase(c);
	            nextTitleCase = false;
	        }
	        titleCase.append(c);
	    }
	    return titleCase.toString();
	}

	public static String BytesToHRSize(String len) {
		if(len.length()<5) {
			if(Integer.parseInt(len)<=1024) {
				return len+" bytes";
			}
		}
		//if bigger than 1k, divide by 1024 and slap a type on the end
		DecimalFormat format = new DecimalFormat("0.000");
		double newlen=Double.parseDouble(len);
		newlen=newlen/1024.0;
		if(newlen<=1024.0) {
			return format.format(newlen)+" K";
		}
		
		newlen=newlen/1024.0;
		if(newlen<=1024.0) {
			return format.format(newlen)+" M";
		}

		newlen=newlen/1024.0;
		if(newlen<=1024.0) {
			return format.format(newlen)+" G";
		}
		
		newlen=newlen/1024.0;
		if(newlen<=1024.0) {
			return format.format(newlen)+" T";
		}
		
		newlen=newlen/1024.0;
		if(newlen<=1024.0) {
			return format.format(newlen)+" P";
		}
		return format.format(newlen)+" E";
	}
	
	//move to filehelper class eventually
	//correct way of copying a file in java??
	public static Boolean copyFile(File source, File dest) throws IOException {
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        Boolean retval=Boolean.TRUE;
		try {
            inputChannel = new FileInputStream(source).getChannel();
            outputChannel = new FileOutputStream(dest).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        } finally {
            inputChannel.close();
            outputChannel.close();
            retval=Boolean.FALSE;
        }
        return retval;
    }

	//probably should move these to a "date helpers" class
	//input: yyyy-MM-dd HH:mm
	//output: yyyy-MM-ddTHH:mmZ
	public static String formatDateTime(String s) {
		//if string still contains a space after trim, assume it is the wrong format
		//if it doesn't contain a space, it's probably good to go
		if(!s.contains(" ")) return s;
		
		return s.trim().replace(" ", "T")+"Z";
	}
	
	//s format must be yyyy-MM-dd HH:mm
	// if it's already converted, SimpleDateFormat will likely fail
	static public String minusMinutes(String s, long minutes) throws Exception {
		if(!s.contains(" ")) throw new Exception();
		
		Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(s);
		
		//minus x minutes
		long time=date.getTime()-(minutes*10000L);
		//reset time to 1 minute ago
		date.setTime(time);
		
		//create formatter
		SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd HH:mm");
		
		//return date in original format
        return sf.format(date);
	}
	
	//convert a CSV with separator sep into a sorted array with unique elements
	//Do a deep copy because the original result list is fixed sized list
	static public List<String> CSVtoSortedUniqueList(String csv, String sep, String putback) {
		List<String> tempList = new ArrayList<String>(Arrays.asList(csv.split(sep)));
		Set<String> tempHashSet = new HashSet<String>();
		String temp="";
		for(int i=0; i<tempList.size(); i++) {
			temp=tempList.get(i)+putback;
			if(!tempList.get(i).trim().equals("") && !tempHashSet.contains(tempList.get(i).trim())) tempHashSet.add(temp.trim());
		}
		return new ArrayList<String>(tempHashSet);
	}
	
	//convert a list back into a CSV with separator sep
	static public String ListtoCSV(List<String> thelist, String sep) {
		if(thelist.isEmpty()) return "";
		StringBuilder sb = new StringBuilder();
		for (Object obj : thelist) {
		  sb.append(obj.toString());
		  sb.append(sep);
		}
		return sb.substring(0,sb.toString().length()-sep.length());
	}

	static public String MapToJSONString(Map<String,String> map) throws JsonGenerationException, JsonMappingException, IOException {
		ScreenLog.begin("MapToJSONString(...)");
		String result="";
		ObjectMapper om=new ObjectMapper();
		result=om.writeValueAsString(map);
		result=result.replace("\"true\"","true").replace("\"false\"","false");
		ScreenLog.end("MapToJSONString("+result+")");
		return result;
	}
	
	public static void touch(final String f) throws IOException {
		touch(new File(f));
	}
	
	//TODO: move to filehelper class or replace with standard java code
	public static void touch(final File file) throws IOException {
	    if (file.exists()) {
	        if (file.isDirectory()) {
	            throw new IOException("File '" + file + "' exists but is a directory");
	        }
	        if (file.canWrite() == false) {
	            throw new IOException("File '" + file + "' cannot be written to");
	        }
	    } else {
	        final File parent = file.getParentFile();
	        if (parent != null) {
	            if (!parent.mkdirs() && !parent.isDirectory()) {
	                throw new IOException("Directory '" + parent + "' could not be created");
	            }
	        }
	        final OutputStream out = new FileOutputStream(file);
	        out.close();
	    }
	    final boolean success = file.setLastModified(System.currentTimeMillis());
	    if (!success) {
	        throw new IOException("Unable to set the last modification time for " + file);
	    }
	}

	public static String ReducedLengthString(String s, int len) {
		if(s.length()<=len) return s;
		return s.substring(0,len/2)+"..."+s.substring(s.length()-len/2, s.length());
	}
	
	//TODO: should deprecate this function
	public static Boolean FileExists(final String s) {
		File f=new File(s);
	  return f.exists();	
	}
	
	//TODO: necessary?
	public static StringBuffer ReplaceAll(StringBuffer buf, String lookfor, String replacewith) {
		int charIndex = buf.indexOf(lookfor);
		while (-1 != charIndex) {
			buf.replace(charIndex, charIndex+1, replacewith);
			charIndex = buf.indexOf(lookfor);
		}
		return buf;
	}
	
	public static void DateStamp(String filename) throws IOException {
		// TODO Auto-generated method stub
		Date date = new Date();
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String datestamp=sdf.format(date);
		
		//write to file
		FileOutputStream fos=new FileOutputStream(new File(filename));
		fos.write(datestamp.getBytes(), 0, datestamp.length());
		fos.close();
	}
	
	public static <T> LinkedList<T> SortbyPreference(LinkedList<T> oldchildren, LinkedList<String> sortPreferences) throws Exception {
        ScreenLog.begin("SortByPreference");

        LinkedList<T> newchildren=new LinkedList<T>();
		
		int star=-1;
		for(int j=0; j<sortPreferences.size(); j++) 
			 if(sortPreferences.get(j).equals("*")) 
				 star=j;
			
		
		if(star==-1) star=sortPreferences.size();
		
		
		//step one, iterate over the presort preferences
//		ScreenLog.out("\t(step 1) looking at oldchildren (size="+oldchildren.size()+") from pref 0 to "+star);
        for(int j=0; j<star; j++) {
            for(int i=0; i<oldchildren.size(); i++) {
            	if(ChildMeetsCondition(oldchildren.get(i),sortPreferences.get(j))) {
  //          		ScreenLog.out("\t\tChild "+oldchildren.get(i)+" meets criteria");
            		if(!newchildren.contains(oldchildren.get(i))) {
            			
  //          			ScreenLog.out("\t\t\tadding child:: "+oldchildren.get(i));
            			newchildren.add(oldchildren.get(i));
            		} else {
  //          			ScreenLog.out("\t\t\tskipping child "+oldchildren.get(i)+", because it was previously already added");
            		}
            	} else {
   //         		ScreenLog.out("\t\tChild "+oldchildren.get(i)+" does not meet criteria");
            	}
            }
        }	
 
        //step two, iterate over post sort preferences, and move over those not meeting the post sort preference criteria
//		ScreenLog.out("\t(step 2) looking at oldchildren (size="+oldchildren.size()+") from "+(star+1)+" to "+sortPreferences.size());
        for(int j=star+1; j<sortPreferences.size(); j++) {
            for(int i=0; i<oldchildren.size(); i++) {
            	if(!ChildMeetsCondition(oldchildren.get(i),sortPreferences.get(j))) {
 //             		ScreenLog.out("\t\tChild "+oldchildren.get(i)+" meets criteria");
            		if(!newchildren.contains(oldchildren.get(i))) {
            			
 //           			ScreenLog.out("\t\t\tadding child:: "+oldchildren.get(i));
            			newchildren.add(oldchildren.get(i));
            		} //else {
 //           			ScreenLog.out("\t\t\tskipping child "+oldchildren.get(i)+", because it was previously already added");
 //           		}
            	} //else {
//            		ScreenLog.out("\t\tChild "+oldchildren.get(i)+" does not meet criteria");
 //           	}
            }
        }	
        
        //step three, sort the rest in the correct order
//		ScreenLog.out("\t(step 3) looking at oldchildren  (size="+oldchildren.size()+") from pref# "+(star+1)+" to "+sortPreferences.size());
        for(int j=star+1; j<sortPreferences.size(); j++) {
            for(int i=0; i<oldchildren.size(); i++) {
            	if(ChildMeetsCondition(oldchildren.get(i),sortPreferences.get(j))) {
//              		ScreenLog.out("\t\tChild "+oldchildren.get(i)+" meets criteria");
            		if(!newchildren.contains(oldchildren.get(i))) {
            			
//            			ScreenLog.out("\t\t\tadding child:: "+oldchildren.get(i));
            			newchildren.add(oldchildren.get(i));
//            		} else {
 //           			ScreenLog.out("\t\t\tskipping child "+oldchildren.get(i)+", because it was previously already added");
            		}
//            	} else {
//            		ScreenLog.out("\t\tChild "+oldchildren.get(i)+" does not meet criteria");
            	}
             }
        }	
        ScreenLog.end("SortByPreference");
        return newchildren;
	}

	

	private static boolean ChildMeetsCondition(Object object, String condition) throws Exception {
//		ScreenLog.begin("ChildMeetsCondition "+object+" cond: "+condition);
		switch(WhichCondition(condition.split(":"))) {
		case CT_STARTS_WITH:
//			ScreenLog.end("ChildMeetsCondition "+object+" cond: "+condition);
			return object.toString().startsWith(condition.split(":")[1]);
		case CT_ENDS_WITH:
//			ScreenLog.end("ChildMeetsCondition "+object+" cond: "+condition);
			return object.toString().endsWith(condition.split(":")[1]);
		case CT_CONTAINS:
//			ScreenLog.end("ChildMeetsCondition "+object+" cond: "+condition);
			return object.toString().contains(condition.split(":")[1]);
		case CT_ERR:
			throw new Exception();
		default:
		}
//		ScreenLog.end("ChildMeetsCondition "+object+" cond: "+condition);
		return object.toString().equals(condition);
	}

	public static ConditionType WhichCondition(String[] split) {
		if(split!=null && split.length>1) {
			if(split[0].toLowerCase().equals("startswith")) return ConditionType.CT_STARTS_WITH;
			if(split[0].toLowerCase().equals("endswith")) return ConditionType.CT_ENDS_WITH;
			if(split[0].toLowerCase().equals("contains")) return ConditionType.CT_CONTAINS;
			return ConditionType.CT_ERR;
		}
		return ConditionType.CT_IS;
	}
	
	public static <T> void WriteListToFile(File f, LinkedList<T> ll) throws IOException {
			if(!f.exists()) {
				f.createNewFile();
			}
			
			FileWriter fw = new FileWriter(f.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			for(int i=0; i<ll.size(); i++) {
				bw.write(ll.get(i).toString());
			}
			bw.close();
	}
	public static String Popen3(String cmdline, String src_filepath, Boolean useStdout, Boolean useStderr) {
		return Popen3(cmdline,src_filepath, null, useStdout, useStderr);
	}

	public static String Popen3(String cmdline, String src_filepath, String tgt_filepath, Boolean useStdout, Boolean useStderr) {
		ScreenLog.begin("PopenThree("+cmdline+")");
	
		String [] cmdarray=cmdline.split(" ");
		for(int i=0; i<cmdarray.length; i++ ) {
			if(cmdarray[i].equals(SRC_FILENAME_CONST)) cmdarray[i]=src_filepath;
			if(tgt_filepath!=null && cmdarray[i].equals(TGT_FILENAME_CONST)) cmdarray[i]=tgt_filepath;
		}
		return Popen3(cmdarray, useStdout, useStderr);
	}
	
	public static String Popen3(String []cmdarray, Boolean useStdout, Boolean useStderr) {
		OutputStream outputStream = new ByteArrayOutputStream();
		try {
			//logger.out("run command: " + cmdline);
			Runtime run = Runtime.getRuntime() ;
			Process pr = run.exec(cmdarray) ;
			BufferedReader buf = new BufferedReader( new InputStreamReader( pr.getInputStream() ) ) ;
			BufferedReader buf_stderr=new BufferedReader( new InputStreamReader(pr.getErrorStream() ));
			
			// read everything and output to outputStream as you go
			String stdout=null;
			String stderr=null;
			
			Boolean quit=false;
			while(!quit) {
				stdout = buf.readLine();
				stderr = buf_stderr.readLine();
				if(stdout!=null) {
					stdout+="\n";
					if(useStdout) outputStream.write(stdout.getBytes());
						ScreenLog.out("\n\tstdout:::"+stdout);
				}
				if(stderr!=null) {
						if(useStderr) outputStream.write(stderr.getBytes());
						ScreenLog.out("\n\tstderr:::"+stderr);
				}
				
				if(stdout==null && stderr==null) quit=true;
			}
			pr.waitFor() ;
			String errcode="Exit value: "+pr.exitValue();
			ScreenLog.out(errcode);
		} catch (Exception e) {
			ScreenLog.ExceptionOutputHandler(e);
		}
		return outputStream.toString();
	}

	public static Boolean System3(String cmdline, String src_filepath, String tgt_filepath) throws IOException, InterruptedException {
		ScreenLog.begin("SystemThree("+cmdline+")");
		String [] cmdarray=cmdline.split(" ");
		for(int i=0; i<cmdarray.length; i++ ) {
			if(cmdarray[i].equals(SRC_FILENAME_CONST)) cmdarray[i]=src_filepath;
			if(tgt_filepath!=null && cmdarray[i].equals(TGT_FILENAME_CONST)) cmdarray[i]=tgt_filepath;
		}
		return System3(cmdarray);
	}

	public static Boolean System3(String cmdline, String filepath) throws IOException, InterruptedException {
		return System3(cmdline,filepath,null);
	}
	
	public static Boolean System3(String []cmdarray) throws IOException, InterruptedException {
		Runtime run = Runtime.getRuntime() ;
		Process pr = run.exec(cmdarray) ;
		
		//logger.out("run command: " + cmdline);
		BufferedReader buf = new BufferedReader( new InputStreamReader( pr.getInputStream() ) ) ;
		BufferedReader buf_stderr=new BufferedReader( new InputStreamReader(pr.getErrorStream() ));
			
			// read everything and output to outputStream as you go
		String stdout=null;
		String stderr=null;
			
		Boolean quit=false;
		while(!quit) {
			stdout = buf.readLine();
			stderr = buf_stderr.readLine();
			if(stdout!=null) {
				stdout+="\n";
				ScreenLog.out("\n\tstdout:::"+stdout);
			}
			if(stderr!=null) {
				ScreenLog.out("\n\tstderr:::"+stderr);
			}
			
			if(stdout==null && stderr==null) quit=true;
		}
		pr.waitFor() ;
		String errcode="Exit value: "+pr.exitValue();
		ScreenLog.out(errcode);
		return new Boolean(pr.exitValue()==0);
	}

}


