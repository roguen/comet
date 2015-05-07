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
//SVN: r551+
 
using namespace std;

#include <string.h>
#include <ctype.h>
#include <stdlib.h>
#include <iostream>
#include <syslog.h>
#include <shadow.h>
#include <OpenStringLib.h>

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
		cout<<"SVN: 551+r"<<endl;
		exit(0);
	}
	if(argc<1) {
		cout<<"usage: echo -en \"password\" | passcheck username"<<endl;
		cout<<"usage: echo -en \"password\" | passcheck username -verbose"<<endl;
	}

	cString user=argv[1];
	
	cString password="";
	std::string line="";
	
	
	if(std::getline(std::cin, line)) {
		//only grab very first line
		
        	password=line.c_str();
	}
	bool verbose=argc>1 && (cString(argv[2])=="-verbose");
	
	
	struct spwd *shadow = getspnam(user);
	if(shadow == NULL) {
		if(verbose) cout<<"fail"<<endl;
		return 1;
	}
	cString shdw=shadow->sp_pwdp;
//	cout<<"shdw=\""<<shdw<<"\""<<endl;
	cString crypted=crypt(password,shdw);
//	cout<<"crypted=\""<<crypted<<"\""<<endl;

	if(crypted == shdw) {
		if(verbose) cout<<"success"<<endl;
		return 0;
	}	
	if(verbose) cout<<"fail"<<endl;

	return 1;
}
