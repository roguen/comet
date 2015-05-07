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
# Package: COMET::Data Ingestor Service
# Author: Chris Delezenski <chris.delezenski@hdsfed.com>
# Compilation Date: 2015-05-06
# License: Apache License, Version 2.0
# Version: 1.21.0
# (RPM) Release: 1
# SVN: r551+

INSTALLDIR=/opt/COMETDist
if [ "$#" == "0" ]; then
	CONFIG=${INSTALLDIR}/comet.properties
else
	CONFIG=$1
fi

. ${INSTALLDIR}/include/hcp_shell_api.sh

ReloadVars

if ! isIngestorEnabled; then
	exit 0
fi

#export CLASSPATH=${INSTALLDIR}/libs/
export JAVAPATH=/usr/java/latest/
export JAVABINDIR=${JAVAPATH}/bin
export JAVA=${JAVABINDIR}/java

export LD_LIBRARY_PATH=${INSTALLDIR}/libs/

echo $$ > /var/run/comet.pid

DEBUG=`cometcfg -e debug -isbool`

if [ "$DEBUG" = "true" ]; then
	OUTPUT="/opt/COMETDist/ingestor-output.txt"
else
	OUTPUT="/dev/null"
fi

FIND=find
#FIND=gnu_find
#export CLASSPATH=`(cd ${INSTALLDIR}/libs &&  $FIND ./ -iname "*.jar" | sed ':a;N;$!ba;s/\n/:/g' )`
export CLASSPATH=`/usr/bin/classpath`

echo "classpath=$CLASSPATH"
#(cd ${INSTALLDIR} && ${JAVA} -Djava.util.logging.config.file="${CONFIG}" -jar libs/Ingestor.jar >$OUTPUT 2>&1)
(cd ${INSTALLDIR}/libs && ${JAVA} -cp $CLASSPATH -Djava.util.logging.config.file="/opt/COMETDist/comet.properties" ingestor.IngestorMain  >$OUTPUT 2>&1)
	
