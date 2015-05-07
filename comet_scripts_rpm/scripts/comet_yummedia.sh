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

NETWORKFALLBACK=1

DEVICE=/dev/cdrom

OPTS=-r

if ! [ -e /media/CentOS ]; then
	mkdir /media/CentOS

	if ! mount $OPTS $DEVICE /media/CentOS; then
		echo "unable to mount $DEVICE"
		if [ "$NETWORKFALLBACK" = "1" ]; then
			echo "trying network fall back"
		
			yum -y $@
			exit 0	
		fi
		exit 1	
	fi
fi

yum --disablerepo=\* --enablerepo=c6-media -y $@


umount /media/CentOS

