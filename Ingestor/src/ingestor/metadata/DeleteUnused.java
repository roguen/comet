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
//SVN: r551+
package ingestor.metadata;

import java.io.File;
import com.hdsfed.cometapi.ExtendedLogger;

public class DeleteUnused extends BaseMetadataGenerator {
	private static ExtendedLogger ScreenLog = new ExtendedLogger(PipedInGenerator.class.getPackage().getName());
	public void initialize(Object inParam) { return; };
	public String getMetadata(File inSourceFile) {
		try {
			if(OverrideMetadataExists(inSourceFile))
				OverrideMetadataDelete(inSourceFile);
		} catch (Exception e) { ScreenLog.ExceptionOutputHandler(e); }
		return new String("");
	}
}
