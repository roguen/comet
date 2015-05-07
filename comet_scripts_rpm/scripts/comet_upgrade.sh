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

. /opt/COMETDist/include/hcp_shell_api.sh

export CONFIG_MODE=true

ReloadVars


filelist=`ListDirectory ${UPGRADE_PKGS_ON_HCP_DIR} | grep -v rpm`

cmdline=""

echo -e "filelist=\"$filelist\""

for thisfile in $filelist; do
	#cmdline="$cmdline 'http://localhost/MetaCatalog/Relay?path=${UPGRADE_PKGS_ON_HCP_DIR}/${thisfile}&type=object&config'"

	echo "GetObject $thisfile `basename $thisfile`"

	GetObject /upgrade/$thisfile $thisfile
	sh ./${thisfile} web_upgrade

	
	#echo "return value was $RET"

	rpm -iUvh --force web_upgrade/packages/*.rpm
	RET=$?
	rm -rf web_upgrade
	rm $thisfile
	if [ "$RET" = "0" ]; then

		echo "upgrade successful"

	else
		
		echo "upgrade failed"
	fi
	break;
done
