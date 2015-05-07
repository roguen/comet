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

if [ -z "$1" ]; then
	echo "missing argument"
	echo  "usage: $0 package_directory"
	exit 1
fi

cometdev_getvar.sh author > $1/SED_AUTHOR
cometdev_getvar.sh copyright > $1/SED_COPYRIGHT
cometdev_getvar.sh license > $1/SED_LICENSE
cometdev_getvar.sh release > $1/SED_RELEASE
cometdev_getvar.sh description > $1/SED_DESCRIPTION
cometdev_getvar.sh version > $1/SED_VERSION
cometdev_getvar.sh svn > $1/SED_SVN
date -I > $1/SED_COMPILED


