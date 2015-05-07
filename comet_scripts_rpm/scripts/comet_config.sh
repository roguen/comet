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

### Main.....

# Load existing vars into memory from configuration file
ReloadVars

## Configure System (Name and access credentials)

echo "================== COMET Configuration =================="
echo "== This tool may be run at any time to change settings =="	
echo "== Or simply edit /opt/COMETDist/comet.properties ======="
echo

echo "===== COMET System details ====="
echo

${STORE} comet.name=`prompter --textprompt "Name of COMET system:" ${COMET_NAME}`
	
echo "===== User credentials ======"	
echo
${STORE} destination.user=`prompter --textprompt "User name (for HCP and HDDS):" ${HCP_USER}`



changepassword="yes"

if ! [ -z ${HCP_PASSWORD} ]; then
	changepassword=`prompter --yesnoprompt "Change Password? :" "yes" --extraprompt`
fi	

if [ "$changepassword" = "yes" ]; then
	#export PW_BASE64=`prompter --password "Password:" | base64`
	export PW_BASE64=`prompter --password "Password:" | base64`
	${STORE} destination.password=`echo -n ${PW_BASE64} | base64 -d | /usr/bin/md5sum |cut -f 1 -d ' '`
	echo ${PW_BASE64} | base64 -d | sudo passwd --stdin comet
fi


HCP_USER=`${EXTRACT} destination.user`

ADMIN_RIGHTS=`prompter --yesnoprompt "Enable admin rights for ${HCP_USER}?"`

#echo "result was ${ADMIN_RIGHTS}"

if [ "${ADMIN_RIGHTS}" = "yes" ]; then
	${STORE} multiuser.admins="${HCP_USER},root"
else
	${STORE} multiuser.admins="root"
fi



## Configure HCP access

echo "===== HCP Setup ======"	
echo

${STORE} destination.hcp_namespace=`prompter --textprompt "HCP Namespace:" ${HCP_NAMESPACE}`

export CONFIG_MODE=true
ReloadVars
${STORE} destination.hcp_config_namespace=`prompter --textprompt "HCP Config Namespace:" ${HCP_NAMESPACE}`
export CONFIG_MODE=
ReloadVars


${STORE} destination.hcp_tenant=`prompter --textprompt "HCP Tenant:" ${HCP_TENANT}`
${STORE} destination.hcp_name=`prompter --textprompt "HCP Name:" ${HCP_NAME}`
${STORE} destination.domain=`prompter --textprompt "Domain:" ${DOMAIN_NAME}`

ReloadVars

if [ -e /usr/bin/comet_ssl_selfsigned.sh ]; then
	createit=`prompter --yesnoprompt "Create Self-signed SSL certificate? :" "yes" --extraprompt`

	if [ "${createit}" = "yes" ]; then
		echo "Creating self-signed cert"
		comet_ssl_selfsigned.sh
	fi
fi

RESTORE="no"

COMET_NAME=`${EXTRACT} comet.name`

if ObjectExists /${COMET_NAME}.properties; then
	echo "There is a backup of the COMET configuration file on HCP"
	RESTORE=`prompter --yesnoprompt "Restore from backup and skip rest of configuration?"`
fi	

if [ "${RESTORE}" = "yes" ]; then
	comet_restore.sh
else

	echo "===== COMET Login Options ======="
	echo
	BoolToYesNoPrompt destination.autoLoginEnabled "Enable automatic login?"

	export AUTO=`${EXTRACT} destination.autoLoginEnabled`
	
	if [ "${AUTO}" = "true" ]; then
		${STORE} destination.autoLoginUser=`prompter --textprompt "Auto Login username? " "${HCP_USER}"`
		${STORE} destination.autoLoginRole=`prompter --textprompt "Auto Login role? " "general"`
	fi



	echo "===== COMET-Ingestor Setup ======"	
	echo
	BoolToYesNoPrompt ingestor.enabled "Enable Ingestor service?"

	export INGEST_ENABLED=`${EXTRACT} ingestor.enabled -isbool`

	### if disabling ingestor, skip all ingestor-related settings
	if [ "${INGEST_ENABLED}" = "true" ]; then
	
	#	BoolToYesNoPrompt ingestor.gui.enabled "Enable Ingestor GUI?"


		echo "Should archived files (eg zip, tgz, etc)"
		BoolToYesNoPrompt execution.unzip "be unzipped prior to ingestion?"
	
		echo
		BoolToYesNoPrompt source.readonly "Are the data sources read only?"
	
	
		BoolToYesNoPrompt ingestor.useFileProcessCache "Use file processor cache?"
		
	
		BoolToYesNoPrompt execution.useFileLocking "Use file locking?"
		
		
		
		echo "Metadata Settings::::::"
		echo 
	
	
		BoolToYesNoPrompt metadata.skip "Only ingest the file? (skip all metadata extraction)"
	
		#choice=`${EXTRACT} metadata.skip`
	
#		if [ "${choice}" = "true" ]; then
#			echo "COMET-Ingestor will not extract metadata"
#		else 
#			BoolToYesNoPrompt metadata.shouldCombineAnnotations "After Ingest, combine all annotations?"
#		fi	
		
#		HAHA=`prompter --yesnoprompt "Enable high availability (requires partner comet system)?"`
#		
#		if [ "$HAHA" = "yes" ]; then 
#			echo "Enabling High Availability"
#			
#			${STORE} comet.partnername=`prompter --textprompt "Partner COMET Name: (FQDN)" "comettoo.domain.com"`
#	
#			HAHA=`prompter --yesnoprompt "Is this system the primary?"`
#			
#			
#			if [ "$HAHA" = "yes" ]; then 
#				touch /opt/COMETDist/ingestor.primary
#			else
#				rm /opt/COMETDist/ingestor.primary
#			fi # end make prime		
#
#		fi # end HAHA	
	fi # end ingestor configuration

	echo "===== COMET-Search and HDDS support Setup ======"	
	echo

	# write this for 1.21
	#search.constraints
	#search.dateList.mustHave
	#search.dateList.mayHave
	#search.dateList.mustNotHave
	BoolToYesNoPrompt search.reverseLookup "Enable Reverse Lookup searches?"
	BoolToYesNoPrompt hdds.enabled "Enable Hitachi Data Discovery Suite (HDDS) integration?"

	export HDDS_ENABLED=`${EXTRACT} hdds.enabled -isbool`
	if [ "${HDDS_ENABLED}" = "true" ]; then 
		${STORE} hdds.name=`prompter --textprompt "HDDS Name: " "hdds.domain.com"`
		${STORE} hdds.password=${PW_BASE64}
		
		${STORE} hdds.user=`${EXTRACT} destination.user`
	fi

	echo "===== COMET-UI Setup ======"	
	echo

	BoolToYesNoPrompt showCaveats "Enable Classification?"

	BoolToYesNoPrompt enableTranscoding "Enable Automatic Video Transcoding?"

	${STORE} comet.geoDriver=`prompter --listprompt "Geo Visual Driver: " "disable,google_earth" 1`

fi

echo "running hostbuilder.. to ensure connectivity to HCP"
/usr/bin/comet_hostbuilder.sh

echo "Be sure to restart comet and tomcat services"
echo "service comet restart"
echo "service tomcat restart"

echo "Configuration complete"
