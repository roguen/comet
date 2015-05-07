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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

//TODO: determine if this class may be combined with safemap
//		Originally, resultmap was a "catcher" for MySQL results
public class ResultMap {
//	private static ExtendedLogger ScreenLog = new ExtendedLogger(ResultMap.class.getPackage().getName());
	private List<Map<String,String>> mapofMaps;
	private int count=0;
	public ResultMap() {
		mapofMaps=new ArrayList<Map<String,String>>();
	}
	public void clear() {
		mapofMaps=new ArrayList<Map<String,String>>();
		count=0;
	}

	public Map<String,String> getInnerMap(int i) {
		return mapofMaps.get(i);
	}
	
	public String get(int i, String key) {
		if(i>getCount() || i<0) return "";
		if(!mapofMaps.get(i).containsKey(key)) return "";
		return mapofMaps.get(i).get(key);
	}

	public Set<String> keySet(int i) {
		//index out of range
		if(i>getCount() || i<0) return null;
		return mapofMaps.get(i).keySet();
	}

	public int getCount() {
		return count;
	}
	
	public void put(int i, String inner_key, String value) {
		if(i>=0 && i<getCount() && mapofMaps.get(i)!=null) {
			mapofMaps.get(i).put(inner_key, value); //replace
		}
		else { //append
			mapofMaps.add(new HashMap<String,String>());
			count++;
			put(i,inner_key,value);
		}
	}

	public Boolean containsIndex(int i) {
		return (mapofMaps.get(i)!=null);
	}

	public int size() {
		return mapofMaps.size();
	}

	public Boolean containsKeyPair(int i, String key) {
		if(i<0 && i>mapofMaps.size()) return Boolean.FALSE;
		if(!mapofMaps.get(i).containsKey(key)) return Boolean.FALSE;
		return Boolean.TRUE;
	}
	
	public void remove(int i) {
		if(i!=-1 && count!=0) {
			mapofMaps.remove(i);
			count--;
		}
	}
}