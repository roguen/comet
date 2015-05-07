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
# SVN: r554

export THIS_SCRIPT_VERSION=1.21.0
export THIS_SCRIPT_SVN=554

echo "Version of this script's package: ${THIS_SCRIPT_VERSION} svn:r${THIS_SCRIPT_SVN}"
echo 
echo "UI and integrated library version:"
curl http://localhost/Version
echo
echo
echo "Ingestor version:"

export CLASSPATH=`/usr/bin/classpath`
export INSTALLDIR=/opt/COMETDist
export JAVA=/usr/bin/java
#echo "classpath=$CLASSPATH"
(cd ${INSTALLDIR}/libs && sudo ${JAVA} -cp $CLASSPATH -Djava.util.logging.config.file="/opt/COMETDist/comet.properties" ingestor.IngestorMain  -version)


