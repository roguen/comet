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

. /opt/COMETDist/include/hcp_shell_api.sh

ReloadVars

NSLOOKUP=`nslookup ${HCP_FQDN} | grep NXDOMAIN`

if [ -z "$NSLOOKUP" ]; then
	echo "HCP is visible from this host, nothing to do"
	exit 0
fi


count=`cat /etc/hosts | grep ${HCP_FQDN} | wc -l`



export HCP_IP=`prompter --textprompt "HCP IP Address (node 0):" 192.168.9.101`


export ADMIN_URL="admin.${HCP_NAME}.${DOMAIN_NAME}"
export TENANT_URL=${HCP_TENANT}.${HCP_NAME}.${DOMAIN_NAME}
export NAMESPACE_URL=${HCP_NAMESPACE}.${HCP_TENANT}.${HCP_NAME}.${DOMAIN_NAME}
export SEARCH_URL="search.${HCP_NAME}.${DOMAIN_NAME}"
export NFS_URL="nfs.${HCP_NAME}.${DOMAIN_NAME}"
#export LINE="${HCP_IP} ${ADMIN_URL} ${TENANT_URL} ${NAMESPACE_URL} ${SEARCH_URL} ${NFS_URL}"

export HDDS_URL="${HDDS_NAME}.${DOMAIN_NAME}"
#export LINE2="${HDDS_IP} ${HDDS_URL}"

export LINE="${HCP_IP} ${ADMIN_URL} ${TENANT_URL} ${NAMESPACE_URL} ${SEARCH_URL} ${NFS_URL}"

export CONFIG_MODE=true

ReloadVars

export LINE3="${HCP_IP} ${NAMESPACE_URL} "

if [ "$count" -eq "0" ]; then
#	echo "append /etc/hosts with ${LINE}"
	echo "${LINE}" >>/etc/hosts
	
	if [ "$HDDS_ENABLED" == "true" -o "$HDDS_ENABLED" == "TRUE" ]; then
		HDDS_IP=`prompter --textprompt "HDDS IP Address:" "192.168.9.201"`
		export LINE2="${HDDS_IP} ${HDDS_FQDN}"
		echo "${LINE2}" >> /etc/hosts
	fi


	PARTNER_FQDN=`$extract comet.partnername`
	if ! [ -z "$PARTNER" ]; then
		PARTNER_IP=`prompter --textprompt "COMET Partner IP Address:" "192.168.9.221"`
		export LINE2="${PARTNER_IP} ${PARTNER_FQDN}"
		echo "${LINE2}" >> /etc/hosts
	fi


	echo "${LINE3}" >> /etc/hosts
	echo "127.0.0.1 ${COMET_FQDN}" >> /etc/hosts
	
else

	echo "no need to append /etc/hosts"	
#	echo "would have appended $LINE"
	if [ "$HDDS_ENABLED" == "true" -o "$HDDS_ENABLED" == "TRUE" ]; then
		echo "would have appended ${LINE2} as well"
	fi
fi
