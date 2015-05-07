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
# Package: Development toolchain for HDSFED COMET
# Author: Chris Delezenski <chris.delezenski@hdsfed.com>
# Compilation Date: 2015-05-06
# License: Apache License, Version 2.0
# Version: 1.21.0
# (RPM) Release: 1
# SVN: r551+

TOPDIR=`cometdev_getvar.sh topdir`
REPO=`cometdev_getvar.sh repo`
BASE=${TOPDIR}/${REPO}/ver

BUILD_PRE=`cat ${BASE}/BUILD_VER_PRE`
BUILD_RC=`cat ${BASE}/BUILD_VER_RC`
BUILD=`cat ${BASE}/BUILD_VER`
PRE=`cat ${BASE}/PRE`
RC=`cat ${BASE}/RC`
if [ "$PRE" == "1" ]; then
	BUILD_PRE=`expr $BUILD_PRE + 1`
	echo $BUILD_PRE > ${BASE}/BUILD_VER_PRE
elif [ "$RC" == "1" ]; then
	BUILD_RC=`expr $BUILD_RC + 1`
	echo $BUILD_RC > ${BASE}/BUILD_VER_RC
else	
	BUILD=`expr $BUILD + 1`
	echo $BUILD > ${BASE}/BUILD_VER
fi	

