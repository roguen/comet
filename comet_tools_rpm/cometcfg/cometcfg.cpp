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
//Package: COMET binary helper programs
//Author: Chris Delezenski <chris.delezenski@hdsfed.com>
//Compilation Date: 2015-05-06
//License: Apache License, Version 2.0
//Version: 1.21.0
//(RPM) Release: 1
//SVN: r554

//#define __TESTING__ 1

using namespace std;

#include <string.h>
#include <ctype.h>
#include <stdlib.h>
#include <OpenStringLib.h>

//USAGE:
//   -extract name
//      return value for name
//
//   -extract name -isbool
//	forces returned value to be interpretted as boolean, "true" is returned for TRUE, True, TrUE or 1, all others "false" is returned
//
//   -extract name -isnum
//      forces returned value to be interpretted as a number, all non-number values return as 0
//
//  -store name=value
//	store in the config file name=value
//  -uses filename
//	alternatively, use a different configuration file
//  -selftest
//	return # of lines containing name=value pairs, # of lines total, # of lines of comments and blank lines
//  -removecomments
//	load entire file into memory, strip out all lines containing nothing but space or start with a hash (#)
//  -saveas [filename]
//      when saving the file due to removecomments or store, save to an alternate filename

bool FileExists(const char *fname) {
   ifstream infile(fname);
	if(infile) { infile.close(); return true; }
   infile.close();
   return false;
}



int ExtractValue_Index(const cStringList &thelist, const cString &target) {
	for(int i=0; i<thelist.Length(); i++) {
		if(thelist[i].StartsWith(target+"=") || thelist[i].StartsWith(target+" =")) {
			return i;
		}
	}	
	return -1;	
}
cString ExtractValue(const cStringList &thelist, const cString &target) {
	int index=ExtractValue_Index(thelist,target);
	
	if(index==-1) return cString("");
	return thelist[index].ChopLf('=');
}

void StorePair(cStringList &thelist, const cString &target, const cString &newvalue) {
	int index=ExtractValue_Index(thelist,target);
	
	if(index==-1) {
		thelist+=target+"="+newvalue;
	} else {
		thelist[index]=target+"="+newvalue;
	}
}

bool IsBool(cString s) {
	if(s.ToLower()=="true") return true;
	return false;
}


int main(int argc, char* argv[]) {
	cArgs arguments(argc,argv,"-");


	if(arguments.IsSet("version")) {
		cout<<"Program: "<<argv[0]<<endl;
		cout<<"Description: COMET binary helper programs"<<endl;
		cout<<"Author: Chris Delezenski <chris.delezenski@hdsfed.com>"<<endl;
		cout<<"Copyright: Copyright (c) 2015 Hitachi Data Systems, Inc."<<endl;
		cout<<"Compilation Date: 2015-05-06"<<endl;
		cout<<"License: Apache License, Version 2.0"<<endl;
		cout<<"Version: 1.21.0"<<endl;
		cout<<"(RPM) Release: 1"<<endl;
		cout<<"SVN: 554r"<<endl;
		exit(0);
	}


#ifdef __TESTING__	
	cString filename="./local_test.properties";
#else
	cString filename="/opt/COMETDist/comet.properties";
#endif
	cString extractme="", storeme="", temp="";
	bool extractmode=false, storemode=false, verbose=false;
	
	
	
#ifdef __TESTING__	
	verbose=true;
#else
	verbose=false;
#endif
	
	if(arguments.IsSet("verbose")) verbose=true;
	
	if(!verbose) close(2);
	
	
	cStringList file_contents;
	
	if(arguments.IsSet("uses")) {
		filename=arguments.GetArg("uses",1);
	
		cerr<<"want to load from file: "<<filename<<endl;
	}


	cerr<<"loading from file: "<<filename<<endl;
	
	if(!FileExists(filename)) {
		cerr<<"file "<<filename<<" does not exist"<<endl;
		exit(1);
	}

	file_contents.FromFile(filename);

	if(arguments.IsSet("extract")) {
		extractme=arguments.GetArg("-extract",1);
		extractmode=true;
	}else {
		cerr<<"not hit 1"<<endl;
	}	
	
	if(arguments.IsSet("e")) {
		extractme=arguments.GetArg("e",1);
		extractmode=true;
	}else {
		cerr<<"not hit 2"<<endl;
	}	
	
		
	if(extractmode) {
		cerr<<"want to extract "<<extractme<<endl;
		temp=ExtractValue(file_contents,extractme);
		
		
		cerr<<"extracted value="<<temp<<endl;
		if(IsBool(temp)) {
			cerr<<"cString::IsBool() returns true"<<endl;
		} else {
			cerr<<"cString::IsBool() return false"<<endl;
		}
		
//		cerr<<"bool converted back to string
		
		
		
		if(arguments.IsSet("isbool")) cout<<cString::BoolToString(IsBool(temp)).ToLower()<<flush;
		else if(arguments.IsSet("isnum")) cout<<temp.AtoSI()<<flush;
		else cout<<temp<<flush;
		exit(0);
	} else {
		cerr<<"not in extract mode"<<endl;
	}	
	
	if(arguments.IsSet("s")) {
		storeme=arguments.GetArg("s",1);
		storemode=true;
	}
	if(arguments.IsSet("store")) {
		storeme=arguments.GetArg("store",1);
		storemode=true;
	}
	
	if(storemode) {
		if(arguments.IsSet("saveas")) {
			filename=arguments.GetArg("saveas",1);
		}
		extractme=storeme.ChopAllRt('=').Trim();
		storeme=storeme.ChopLf('=').Trim();
		
		cerr<<"extractme=\""<<extractme<<"\""<<endl;
		cerr<<"want to replace: "<<ExtractValue(file_contents,extractme)<<" with "<<storeme<<endl;
		
		
		StorePair(file_contents,extractme,storeme);
		
		cerr<<"then save to: "<<filename<<endl;
		file_contents.ToFile(filename);
		return 0;
	}
	if(arguments.IsSet("removecomments")) {
		if(arguments.IsSet("saveas")) {
			filename=arguments.GetArg("saveas",1);
		}
		for(int i=0; i<file_contents.Length(); i++) {
			if(file_contents[i].Length()>0 && file_contents[i][0]=='#') file_contents[i]="";
		}
		file_contents.Sort();
		file_contents.Compact();
		file_contents.ToFile(filename);
		return 0;
	}
	return 0;
}
