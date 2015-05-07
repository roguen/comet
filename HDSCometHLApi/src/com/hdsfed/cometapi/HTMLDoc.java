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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

//TODO: functions need descriptions etc

public class HTMLDoc {
	private static ExtendedLogger ScreenLog = new ExtendedLogger(HTMLDoc.class.getPackage().getName());
	private Stack<String> st=null;
	private OutputStream os=null;
	private Writer w=null;
	private int indent=0;
	private Boolean formatting=false;
	
	//initialize
	HTMLDoc(OutputStream outputStream) {
		st=new Stack<String>();
		setOutputStream(outputStream);
		setWriter(new OutputStreamWriter(outputStream));
	}
	
	//open a normal html document
	public void OpenNewHTML(OutputStream outputStream) throws IOException {
		writeStartElement("html",true);
		w.write("\n");

		writeStartElement("body",true);
		w.write("\n");
	}
	public void writeStartElement(String s, Boolean hascontent) throws IOException {
		writeStartElementwithAttributes(s, null, hascontent);
	}
	
	private void writeIndention() throws IOException {
		for(int i=0; i<indent; i++) {
			w.write("\t");
		}
	}
	
	public void writeStartElementwithAttributes(String s, Map<String,String> attrib, Boolean hascontent) throws IOException {
		if(formatting) {
			writeIndention();
			indent++;
		}
		
		if(hascontent) st.push(s);
		w.write("<"+s);
		
		if(attrib!=null) {
			for(String key: attrib.keySet()) {
				w.write(" "+key+"=\""+attrib.get(key)+"\"");
			}
		}
		w.write(">");
		if(formatting) {
			w.write("\n");
		}
	}
	public void writeContent(String content) throws IOException {
		if(formatting) {
			writeIndention();
		}
		w.write(content);
		if(formatting) {
			w.write("\n");
			indent--;
		}
	}
	
	public void writeClosedElement(String s, String content, Map<String,String> attrib) throws Exception {
		if(formatting) {
			writeIndention();
		}
		Boolean format_state=formatting;
		formatting=false;
		writeStartElementwithAttributes(s,attrib,true);
		w.write(content);
		writeEndElement();
		formatting=format_state;
		if(formatting) {
			w.write("\n");
		}
	}
		
	private void write(String s) throws Exception {
		if(s==null) {
			ScreenLog.out("attempt to write null value");
			throw new Exception();
		}
		w.write(s);
	}
		
	public void writeEndElement() throws Exception {
		if(st.empty()) throw new Exception();
		String s=st.pop();
		if(formatting) {
			indent--;
			writeIndention();
		}
		w.write("</"+s+">");
		if(formatting) {
			w.write("\n");
		}
	}	

	public void flush() throws IOException {
		w.flush();
	}
	
	public void close() throws Exception {
		w.flush();
		while(!st.empty()) writeEndElement();
		w.close();
	}
	
	public void CreateTable() throws IOException {
		CreateTable(null);
	}
	
	public void CreateTable(int w, int h) throws IOException {
		Map<String,String> attribs=new HashMap<String,String>();
		if(w!=0) attribs.put("width", String.valueOf(w));
		if(h!=0) attribs.put("height", String.valueOf(h));
		CreateTable(attribs);
	}
	
	public void CreateTable(Map<String,String> attribs) throws IOException {
		writeStartElementwithAttributes("table", attribs, true);
	}
	
	public void CreateRow(Map<String,String> attribs) throws IOException {
		writeStartElementwithAttributes("tr", attribs, true);
	}
	
	public void CreateRow() throws IOException {
		CreateRow(null);
	}
	
	public void CreateCol(int w, int h) throws IOException {
		Map<String,String> attribs=new HashMap<String,String>();
		if(w!=0) attribs.put("width", String.valueOf(w));
		if(h!=0) attribs.put("height", String.valueOf(h));
		CreateCol(attribs);
	}
	
	public void CreateCol(Map<String,String> attribs) throws IOException {
		writeStartElementwithAttributes("td", attribs, true);
	}
	public void CreateCol() throws IOException {
		CreateCol(null);
	}
	
	public void EndColumn() throws Exception {
		writeEndElement();
	}

	public void EndRow() throws Exception {
		writeEndElement();
	}
	
	public void EndTable() throws Exception {
		writeEndElement();
	}
	
	public void CreateImage(Map<String,String> attribs)  throws Exception  {
		writeStartElementwithAttributes("img", attribs, false);
	}

	public void CreateImage(URL url,int w, int h) throws Exception {
		Map<String,String> attribs=new HashMap<String,String>();
		if(w!=0) attribs.put("width", String.valueOf(w));
		if(h!=0) attribs.put("height", String.valueOf(h));
		attribs.put("src", url.toString());
		CreateImage(attribs);
	}
	
	public static String CreateImageString(URL url,int w, int h) {
		Map<String,String> attribs=new HashMap<String,String>();
		if(w!=0) attribs.put("width", String.valueOf(w));
		if(h!=0) attribs.put("height", String.valueOf(h));
		attribs.put("src", url.toString());
		return CreateImageString(attribs);
	}
	
	public static String CreateImageString(Map<String,String> attribs) {
		String result="<img";
		for(String key: attribs.keySet()) {
			result+=" "+key+"=\""+attribs.get(key)+"\"";
		}
		result+=" />";
		
		return result;
	}
	
	public void CreateRowwithColumnPair(Map<String,String> row_attribs, Map<String,String> col_attribs, String label, String value, Boolean label_bolded) throws Exception {
		CreateRow(row_attribs);
		CreateCol(col_attribs);
		if(label_bolded) {
			writeClosedElement("b", label, null);
		} else {
			write(label);
		}
		EndColumn();
		CreateCol(col_attribs);
		if(label==null) {
				ScreenLog.out("attempted to label with null");
				throw new Exception();
		}
		if(value==null) {
			ScreenLog.out("attempted to value with null");
			throw new Exception();
		}
		write(value);
		EndColumn();
		EndRow();
	}

	public void CreateRowwithColumnPair(String label, String value, Boolean label_bolded) throws Exception {
		CreateRowwithColumnPair(null,null,label,value,label_bolded);
	}
	
	public OutputStream getOutputStream() {
		return os;
	}

	public void setOutputStream(OutputStream os) {
		this.os = os;
	}

	public Writer getWriter() {
		return w;
	}

	public void setWriter(Writer w) {
		this.w = w;
	}

	public int getIndent() {
		return indent;
	}

	public void setIndent(int indent) {
		this.indent = indent;
	}

	public Boolean getFormatting() {
		return formatting;
	}

	public void setFormatting(Boolean formatting) {
		this.formatting = formatting;
	}

	//TODO: move out exception
	public String toString() {
		try {
			close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			ScreenLog.ExceptionOutputHandler(e);
		}
		return this.os.toString();
	}
}


