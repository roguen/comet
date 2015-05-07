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

# usage:
# makecookie.sh [-i]
# options:
#     -i  -  Run in interactive mode, where all pieces are collected on each run
# 	    Default: run in non-interactive mode, where variables are used instead.

if ! [ -z $1 ]; then
	
	echo "Namespace? "
	read namespace
	
	echo "Tenant? "
	read tenant
	
	echo "HCP Cluster? "
	read hcpname
	
	echo "Domain? "
	read domain

	echo "Path to object?"
	read path

	echo "User?"
	read user
	echo "Password? (terminal characters will not appear as you type)"
	# turn off echo
	stty -echo
	read password
	#turn echo back on
	stty echo

else
	# fill these in with your own values
	namespace=namespace
	tenant=tenant119
	hcpname=hcp
	domain=domain.com
	path=/StreamTest/florida.stream
	user=comet
	password="password1!"
fi




echo "You have selected the following fully qualified domain name:"

FQDN="https://${namespace}.${tenant}.${hcpname}.${domain}"
echo
echo "On HCP, your object is here: ${FQDN}/rest${path}"
echo 
ENCODED_UN=`echo -en ${user} | base64`

ENCODED_PW=`echo -en ${password} | /usr/bin/md5sum |cut -f 1 -d ' '`


AUTHCOOKIE="Authorization: HCP ${ENCODED_UN}:${ENCODED_PW}"
echo
echo "your authentication token is: ${AUTHCOOKIE}"


URL_ROOTPATH=${FQDN}/rest${path}


echo -e "curl test would look like this: curl -I -k -H \"${AUTHCOOKIE}\" ${URL_ROOTPATH}"
echo
echo -e "Your admin interface should be located here: https://admin.${hcpname}.${domain}:8000/"
echo -e "Your tenant admin interface should be located here: https://${tenant}.${hcpname}.${domain}:8000/"
echo -e "Your namespace browser interface should be located here: ${FQDN}"
echo

echo "executing curl test:"
if curl -I -k -H "${AUTHCOOKIE}" ${URL_ROOTPATH}; then
	echo "curl test succeeded"
else
	echo "curl test failed somehow"
fi	
