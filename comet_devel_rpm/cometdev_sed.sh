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

SED=`cometdev_getvar.sh sed`

if [ -z ${1} ]; then
	echo "missing arguments"
	exit 1
fi

if [ -z ${2} ]; then
	echo "missing arguments"
	exit 1
fi

for i in ./in/*.in; do
	j=`basename $i | cut -f 1 -d .`
	i=$j
	
	#echo $SED ${1}/${i}.sh.in ${2}/${i}.sh
	/bin/sed "${SED}" < ${1}/${i}.sh.in > ${2}/${i}.sh
	chmod 755 ${2}/${i}.sh
done
