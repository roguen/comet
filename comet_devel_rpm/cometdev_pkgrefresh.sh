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
# SVN: r554

if [ -z "${1}" ]; then
	echo "missing arugment"
	exit 1
fi

if [ -z "${2}" ]; then
	echo "missing arugment"
	exit 1
fi


TOPDIR=`cometdev_getvar.sh topdir`
REPO=`cometdev_getvar.sh repo`
BASE=${TOPDIR}/${REPO}


rm -rvf ${BASE}/packages/payload/${1}-*rpm
rm -rvf ${BASE}/packages/src/${1}-*rpm

(cd ${BASE}/${2} && make clean rpms && cp -vf *src.rpm ${BASE}/payload/src/ && cp -vf `ls $1-*.rpm | grep -v src` ${BASE}/payload/packages/)
