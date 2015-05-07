/*
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
//Author: Cliff Grimm <cliff.grimm@hds.com>
//Compilation Date: 2015-05-06
//License: Apache License, Version 2.0
//Version: 1.21.0
//(RPM) Release: 1
//SVN: r551+
*/
#include <jni.h>
#include <sys/types.h>
#include <sys/ipc.h>
#include <sys/shm.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>

#include <gdal.h>
#include <cpl_conv.h>

#include <libxml2/libxml/xmlwriter.h>
#include <libxml2/libxml/xmlstring.h>
#include "processFile.h"

JNIEXPORT void JNICALL Java_ingestor_metadata_JNIWrapper_initialize
  (JNIEnv *env, jobject jobj, jobject inParam) {

    CPLSetConfigOption("NITF_OPEN_UNDERLYING_DS", "NO");

	GDALAllRegister();
	return;
}

//
// This function reads a file and returns the contents into a
//  byte array.  The byte array is returned back to Java caller.
//
JNIEXPORT jbyteArray JNICALL Java_ingestor_metadata_JNIWrapper_getCustomMetadata
  (JNIEnv *env, jobject jobj, jstring fileName) {
    jbyteArray jb = NULL;
    jboolean iscopy;
	xmlBufferPtr outputBufferPtr = NULL;

    // Point srcFileName to the input parameter string so it is
    //  usable by C language.
    const char *srcFileName = (*env)->GetStringUTFChars(
                env, fileName, &iscopy);

	/***
	 * Initialize the XML document writer.
	 */
    outputBufferPtr = xmlBufferCreate();
    if (NULL == outputBufferPtr) {
    	fprintf(stderr, "Failed to create xmlBuffer\n");
    	goto cleanup;
    }

    // Process a single files GDAL information.
    if (EXIT_FAILURE == processFile(srcFileName, outputBufferPtr)) {
    	fprintf(stderr, "processFile() returned a failure\n");
    	goto cleanup;
    }

    // Allocate a byte array of the needed size to pass back.
    jb=(*env)->NewByteArray(env, outputBufferPtr->use);

    // Set the byte array to be memory mapped file content.
    (*env)->SetByteArrayRegion(env, jb, 0, outputBufferPtr->use, (jbyte *)outputBufferPtr->content);

  cleanup:

    // Release any passed in parameters since Java is always by value.
    (*env)->ReleaseStringUTFChars(env, fileName, srcFileName);

    if (NULL != outputBufferPtr)
    	xmlBufferFree(outputBufferPtr);

    // Return the byte array.
    return (jb);
}
