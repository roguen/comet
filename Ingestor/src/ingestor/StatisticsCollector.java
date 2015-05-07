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

//NOTE: this code was originally developed by Cliff Grimm <clifford.grimm@hds.com>

package ingestor;

import java.util.logging.Level;

import com.hdsfed.cometapi.ExtendedLogger;

//TODO: Fix this class so that it works correctly with annotations and threads
public class StatisticsCollector {

	//private static Logger logger = Logger.getLogger(IngestorMain.class.getPackage().getName());
	private static ExtendedLogger logger = new ExtendedLogger(StatisticsCollector.class.getName());

	private int iSuccessObject;
	private int iFailureObject;
	private int iNotProcessedObject;
	private int iSuccessCM;
	private int iFailureCM;
	private int iNotProcessedCM;
	
	public int getObjectSuccesses() { return iSuccessObject; };
	public int getObjectFailures() { return iFailureObject; };
	public int getObjectNotProcessed() { return iNotProcessedObject; };
	
	public int getCustomMetadataSuccesses() { return iSuccessCM; };
	public int getCustomMetadataFailures() { return iFailureCM; };
	public int getCustomMetadataNotProcessed() { return iNotProcessedCM; };

	StatisticsCollector() {
		reset();
	}
	
	void reset() {
		iSuccessObject = iFailureObject = 0;
		iSuccessCM = iFailureCM = 0;
	}
	
	void update(SingleFileProcessor.WriteStatus inObject, SingleFileProcessor.WriteStatus inCM) {

		// Save the object statistics
		switch(inObject) {
		case WRITE_SUCCESS:
			iSuccessObject++;
			break;
		case WRITE_FAILURE:
			iFailureObject++;
			break;
		case WRITE_NOT_ATTEMPTED:
			iNotProcessedObject++;
			break;
		}
		
		// Save the custom metadata statistics
		switch(inCM) {
		case WRITE_SUCCESS:
			iSuccessCM++;
			break;
		case WRITE_FAILURE:
			iFailureCM++;
			break;
		case WRITE_NOT_ATTEMPTED:
			iNotProcessedCM++;
			break;
		}
	}

	//FIXME: statistics are broken
	// Log statistics at the INFO level.
	void logStats(String inLabel) {
		logStats(Level.INFO, inLabel);
	}

	// General routine to log the statistics at the specified level.
	void logStats(Level inLevel, String inLabel) {
		
		logger.log(inLevel, inLabel + ":"
				+ "\n  Object:          (" + getObjectSuccesses() 
				+ ", " + getObjectFailures() + ", " 
				+ getObjectNotProcessed() + ")"
				+ "\n  Custom Metadata: (" + getCustomMetadataSuccesses() 
				+ ", " + getCustomMetadataFailures() + ", " 
				+ getCustomMetadataNotProcessed() + ")");
	}
	
}
