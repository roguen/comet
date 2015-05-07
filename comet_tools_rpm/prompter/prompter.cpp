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
 
using namespace std;

#include <string.h>
#include <ctype.h>
#include <stdlib.h>
#include <OpenStringLib.h>

int main(int argc, char* argv[]) {
	cArgs arguments(argc,argv,"--");
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
	cKeyboard kybd;
	
	cString password="", confirm="", label="", validchoices="", defaultval="", temp="", list="";
	cStringList csv;
	bool extraprompts=arguments.IsSet("extraprompt");
	if(arguments.IsSet("password")) {
		do {
			password=kybd.PasswordPrompt("Password:","",true);
			confirm=kybd.PasswordPrompt("Confirm:","",true);
		} while (password!=confirm && confirm!="");
		
		cout<<password<<flush;
		return 0;
	}
	
	if(arguments.IsSet("textprompt")) {
		cout<<kybd.TextPrompt(arguments.GetArg("--textprompt",2), arguments.GetArg("--textprompt",3), true)<<flush;
		return 0;
	}
	
	if(arguments.IsSet("listprompt")) {
		label=arguments.GetArg("--listprompt",2);
		validchoices=arguments.GetArg("--listprompt",3);
		defaultval=arguments.GetArg("--listprompt",4);
		if(defaultval.AtoSI()==cString(defaultval.AtoI())) cout<<kybd.ListPrompt(label, validchoices, defaultval.AtoI(), extraprompts)<<endl;
		else cout<<kybd.ListPrompt(label, validchoices, defaultval, extraprompts)<<endl;
	}
	if(arguments.IsSet("yesnoprompt")) {
		label=arguments.GetArg("--yesnoprompt",2);
		validchoices="yes,no";
		defaultval=arguments.GetArg("--yesnoprompt",3);
		cout<<kybd.ListPrompt(label, validchoices, defaultval, extraprompts)<<flush;
	}
	if(arguments.IsSet("enterkey") || arguments.IsSet("pressenter")) {
		cConsolePrompt::PressEnter(true);
	}
	if(arguments.IsSet("listedit")) {
		label=arguments.GetArg("--listedit",2);
		list=arguments.GetArg("--listedit",3);
		
		if(list=="") {
			//whoops, probably meant textprompt
			cout<<kybd.TextPrompt(label, list, true)<<flush;
		} else {		
			//correct use of listedit
			
			csv.FromString(list,",");
			csv+="append?";
			for(int i=0; i<csv.Length(); i++) {
				
				temp=kybd.TextPrompt(label+" (Leave blank to delete):", csv[i], true);
				if(temp=="append?") temp="";
				csv[i]=temp;
			}
			csv.Compact();
			cout<<csv.ToString(",")<<flush;
		}
			
	}
			
	return 0;
}

