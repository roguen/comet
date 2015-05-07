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

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class ServletHelper {
	public static Map<String, String> GetParameterMap(HttpServletRequest request) throws IOException {
		Map<String, String> parameters=new HashMap<String,String>();
		for(String key: request.getParameterMap().keySet()) {
			parameters.put(key, request.getParameter(key));
		}		
		//only if there is content and the variable is not null
		if(request.getContentLength()>0 && (!parameters.containsKey("content") || (parameters.containsKey("content") && parameters.get("content").equals("")))) {
			//dump input stream to log
			ServletInputStream is=request.getInputStream();
			@SuppressWarnings("resource")
			java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
			String theString=s.hasNext() ? s.next() : "";
		    parameters.put("content", theString);
		    s.close();
		    is.close();
		}
		return parameters;
	}
	
	public static Map<String,String> GetHeaderMap(HttpServletRequest request) {
		Map<String, String> headers=new HashMap<String,String>();
		Enumeration<String> headerNames = request.getHeaderNames();
		while(headerNames.hasMoreElements()) {
			String headerName = (String)headerNames.nextElement();
			headers.put(headerName, request.getHeader(headerName));
		}
		return headers;
	}
	
	
	 // Helpers (can be refactored to public utility class) ----------------------------------------

    /**
     * Returns true if the given accept header accepts the given value.
     * @param acceptHeader The accept header.
     * @param toAccept The value to be accepted.
     * @return True if the given accept header accepts the given value.
     */
    public static boolean accepts(String acceptHeader, String toAccept) {
        String[] acceptValues = acceptHeader.split("\\s*(,|;)\\s*");
        Arrays.sort(acceptValues);
        return Arrays.binarySearch(acceptValues, toAccept) > -1
            || Arrays.binarySearch(acceptValues, toAccept.replaceAll("/.*$", "/*")) > -1
            || Arrays.binarySearch(acceptValues, "*/*") > -1;
    }

    /**
     * Returns true if the given match header matches the given value.
     * @param matchHeader The match header.
     * @param toMatch The value to be matched.
     * @return True if the given match header matches the given value.
     */
    public static boolean matches(String matchHeader, String toMatch) {
        String[] matchValues = matchHeader.split("\\s*,\\s*");
        Arrays.sort(matchValues);
        return Arrays.binarySearch(matchValues, toMatch) > -1
            || Arrays.binarySearch(matchValues, "*") > -1;
    }

    /**
     * Returns a substring of the given string value from the given begin index to the given end
     * index as a long. If the substring is empty, then -1 will be returned
     * @param value The string value to return a substring as long for.
     * @param beginIndex The begin index of the substring to be returned as long.
     * @param endIndex The end index of the substring to be returned as long.
     * @return A substring of the given string value as long or -1 if substring is empty.
     */
    public static long sublong(String value, int beginIndex, int endIndex) {
        String substring = value.substring(beginIndex, endIndex);
        return (substring.length() > 0) ? Long.parseLong(substring) : -1;
    }

    
    /**
     * Close the given resource.
     * @param resource The resource to be closed.
     */
    public static void close(Closeable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (IOException ignore) {
                // Ignore IOException. If you want to handle this anyway, it might be useful to know
                // that this will generally only be thrown when the client aborted the request.
            }
        }
    }
    
    public static Map<String,String> SessionManager(HttpServletRequest request, Map<String,String> parameters) {
        //could put into a session to parameter map function
		HttpSession session = request.getSession();
        parameters.put("session_id", session.getId());
        parameters.put("session_isnew", new Boolean(session.isNew()).toString());
        parameters.put("session_last_accessed", new Long(session.getLastAccessedTime()).toString());
        parameters.put("session_creation_time", new Long(session.getCreationTime()).toString());
        parameters.put("session_timeout_length", new Long(session.getMaxInactiveInterval()).toString());

        Integer counter = (Integer) session.getAttribute("session_counter");

        if (counter == null) {
            counter = new Integer(1);
        } else {
            counter = new Integer(counter.intValue() + 1);
        } 

        session.setAttribute("session_counter", counter);

        
        Enumeration<String> names = session.getAttributeNames();
        while (names.hasMoreElements()) {
          String name = (String) names.nextElement();
          String value = session.getAttribute(name).toString();
          parameters.put(name, value);
        }
    	
    	return parameters;
    }
    
    public static void setSessionAttribute(HttpServletRequest request, String name, String value) {
		HttpSession session = request.getSession();
		session.setAttribute(name, value);
    	
    	
    }
    public static void invalidateSession(HttpServletRequest request) {
		HttpSession session = request.getSession();
		session.invalidate();
		
    	
    }
    
    
}
