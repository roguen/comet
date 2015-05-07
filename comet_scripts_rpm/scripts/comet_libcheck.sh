#!/bin/sh
# Copyright (c) 2015 Hitachi Data Systems, Inc.
# All Rights Reserved.
#
#    Licensed under the Apache License, Version 2.0 (the "License"); you may
#    not use this file except in compliance with the License. You may obtain
#    a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
#    Unless required by applicable law or agreed to in writing, software
#    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
#    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
#    License for the specific language governing permissions and limitations
#    under the License.
#
# Package: Custom Object Metadata Enhancement Toolkit run-time scripts
# Author: Chris Delezenski <chris.delezenski@hdsfed.com>
# Compilation Date: 2015-05-06
# License: Apache License, Version 2.0
# Version: 1.21.0
# (RPM) Release: 1
# SVN: r551+

export LIBS="commons-codec-1.6.jar commons-logging-1.1.1.jar HCPHelpersForJava.jar HDSCometHLApi.jar httpclient-4.2.5.jar httpcore-4.2.4.jar im4java-1.4.0.jar jackson-all-1.6.1.jar jedis-2.1.0.jar jid3lib-0.5.4.jar json-lib-2.3-jdk15.jar sqlite-jdbc-3.7.2.jar"


export SRC="/opt/COMETDist/libs"
export TGT="/opt/COMETDist/apache-tomcat/lib"
for f in $LIBS; do
	if ! [ -e ${TGT}/${f} ]; then
		(cd ${TGT} && sudo ln -s ${SRC}/${f})
	fi
done

