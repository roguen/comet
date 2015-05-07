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

export INSTALLDIR="/opt/COMETDist"
export PROPFILE="/opt/COMETDist/comet.properties"
export STORE="cometcfg  $CONFIG_FILE -s "
export EXTRACT="cometcfg $CONFIG_FILE -e "

function ReloadVars() {
	if ! [ -z $1 ]; then
		export CONFIG_FILE="-uses $1"
	else
		export CONFIG_FILE=""
	fi
	export STORE="cometcfg  $CONFIG_FILE -s "
	export EXTRACT="cometcfg $CONFIG_FILE -e "
	
	export HCP_USER=`${EXTRACT} destination.user`
	export HDDS_USER=$HCP_USER
	export HCP_PASSWORD=`${EXTRACT} destination.password`
	
	if [ "${CONFIG_MODE}" = "true" ]; then
		export HCP_NAMESPACE=`${EXTRACT} destination.hcp_config_namespace`
	else
		export HCP_NAMESPACE=`${EXTRACT} destination.hcp_namespace`
	fi
	
	
	export HCP_TENANT=`${EXTRACT} destination.hcp_tenant`
	export HCP_NAME=`${EXTRACT} destination.hcp_name`
	export DOMAIN_NAME=`${EXTRACT} destination.domain`
	export HCP_IP=`${EXTRACT} destination.ip`
	export COMET_NAME=`${EXTRACT} comet.name`
	export COMET_FQDN=${COMET_NAME}
	
	export HCP_FQDN=${HCP_NAMESPACE}.${HCP_TENANT}.${HCP_NAME}.${DOMAIN_NAME}
	export URL_ROOTPATH="https://${HCP_FQDN}/rest"
	
	
	export UN_ENCODED=`echo -n $HCP_USER | /usr/bin/base64`
	export AUTHCOOKIE="Authorization: HCP ${UN_ENCODED}:${HCP_PASSWORD}"

	export HCP_ADMIN_URL="https://admin.${HCP_NAME}.${DOMAIN_NAME}:8000"
	export HCP_TENANT_URL="https://${HCP_TENANT}.${HCP_NAME}.${DOMAIN_NAME}:8000"
	export HCP_NAMESPACE_URL="https://${HCP_NAMESPACE}.${HCP_TENANT}.${HCP_NAME}.${DOMAIN_NAME}"
	export UPGRADE_PKGS_ON_HCP_DIR=`${EXTRACT} destination.upgradeDir`
	if [ -z $UPGRADE_PKGS_ON_HCP_DIR ]; then
		export UPGRADE_PKGS_ON_HCP_DIR="/upgrade"
	fi

	
	export HCP_SEARCH_URL="https://search.${HCP_NAME}.${DOMAIN_NAME}:8888"
	export INGEST_INPUTDIR_URL="file://${INSTALLDIR}/InputDir"
	export NFS_URL="file://${INSTALLDIR}/local"
	export HDDS_NAME=`${EXTRACT} hdds.name`
	export HDDS_PASSWORD=`${EXTRACT} hdds.password`
	export HDDS_IP=`${EXTRACT} hdds.ip`
	
	export HDDS_ADMIN_URL="https://${HDDS_NAME}:8443/hdds/admin"
	export HDDS_SEARCH_CONSOLE_URL="https://${HDDS_NAME}:8443/hdds/service/search"
	
	export HDDS_ENABLED=`${EXTRACT} hdds.enabled -isbool`
	export MULTIUSER=`${EXTRACT} multiuser.enabled`
	
	export COMET_VERSION=`${EXTRACT} comet.version`

	export INGESTOR_ENABLED=`${EXTRACT} ingestor.enabled -isbool`	
	
	export HCP_MAPI_URL="https://${HCP_TENANT}.${HCP_NAME}.${DOMAIN_NAME}:9090/mapi"
}


function hddsEnabled() {
	enabled=`${EXTRACT} hdds.enabled`
	if [ "$enabled" = "true" ]; then
		return 0
	fi
	return 1
}

function sedit() {
	sed $1 < $2 > temp.tmp
	mv -vf temp.tmp $2
}



function hasX11() {
	result=`set | grep XAUTH`
	if [ -z "$result" ]; then
		return 1
	fi
	return 0 
}

function isNFSMounted() {
	result=`mount | grep nfs | grep ${DOMAIN_NAME}`
	if [ -z "$result" ]; then
		return 1
	fi
	return 0
}

function isIngestorEnabled() {
	if [ "${INGESTOR_ENABLED}" = "true" ]; then
		return 0
	fi
	return 1
}


#arg1=HCP path and filename, eg /CustomerScenarios/MP3s/SomeFilename.mp3
#arg2=metadata, use - for stdin, eg /path/to/custommetadata.xml
#arg3=annotation (if blank, assume default)
#example PostAnnotation /CustomerScenarios/MP3s/SomeFilename.mp3 custommetadata.xml play
function PostAnnotation() {
	path=$1
	metadata=$2
	type="custom-metadata"
	annotation=$3
	if [ -z $annotation ]; then
		annotation="default"
	fi
	curl -k -iT $metadata -H  "${AUTHCOOKIE}" "${URL_ROOTPATH}${path}?type=${type}&annotation=${annotation}"
}

#arg1=HCP path and filename, eg /CustomerScenarios/MP3s/SomeFilename.mp3
#arg2=source annotation
#arg3=target annotation
function CopyAnnotation() {
	path=$1
	annotation_from=$2
	annotation_to=$3
	type="custom-metadata"
	curl -k -H  "${AUTHCOOKIE}" "${URL_ROOTPATH}${path}?type=${type}&annotation=${annotation_from}" >temp.xml
	cat temp.xml | PostAnnotation ${path} - ${annotation_to}
}

#arg1=HCP path and filename, eg /CustomerScenarios/MP3s/SomeFilename.mp3
#arg2=annotation
function DeleteAnnotation() {
	path=$1
	annotation=$2
	type="custom-metadata"
	curl -k -iX DELETE -H  "${AUTHCOOKIE}" "${URL_ROOTPATH}${path}?type=${type}&annotation=${annotation}"
}

#arg1=HCP path and filename, eg /CustomerScenarios/MP3s/SomeFilename.mp3
#arg2=src annotation
#arg3=target annotation
function RenameAnnotation() {
	path=$1
	annotation_from=$2
	annotation_to=$3
	CopyAnnotation $path $annotation_from $annotation_to
	DeleteAnnotation $path $annotation_from
}



#arg1=HCP path and filename
#arg2=annotation (if blank, assume default)
#example GetAnnotation /CustomerScenarios/MP3s/SomeFilename.mp3 play
function GetAnnotation() {
	path=$1
	type="custom-metadata"
	annotation=$2
	if [ -z $annotation ]; then
		annotation="default"
	fi
	curl -k -H  "${AUTHCOOKIE}" "${URL_ROOTPATH}${path}?type=${type}&annotation=${annotation}"
}

#arg1=HCP path and filename, eg /CustomerScenarios/MP3s/SomeFilename.mp3
# redirect to a file to catch output
function GetObject() {
	path=$1
	saveas=$2
	
#	echo "want to pull $path from HCP and save to local file: $saveas"
	curl -k -H  "${AUTHCOOKIE}" "${URL_ROOTPATH}${path}" > $saveas 2>/dev/null
}

#only upload the file content for now
#arg1=HCP path and filename, eg /CustomerScenarios/MP3s/SomeFilename.mp3
#arg2=local path to file for upload
function PutObject() {
	path=$1
	filename=$2
	curl -k -iT $filename -H  "${AUTHCOOKIE}" "${URL_ROOTPATH}${path}"
}

#arg1=HCP path and filename, eg /CustomerScenarios/MP3s/SomeFilename.mp3
function DeleteObject() {
	path=$1
	curl -k -iX DELETE -H  "${AUTHCOOKIE}" "${URL_ROOTPATH}${path}"
}

#arg1=HCP path and filename, eg /CustomerScenarios/MP3s/SomeFilename.mp3
function ListAnnotations() {
	path=$1
	type="custom-metadata-info"
	curl -k -H  "${AUTHCOOKIE}" "${URL_ROOTPATH}${path}?type=$type" | grep name | sed 's/</ /g;s/>/ /g;s/name/ /g;s@/@ @g'
}


function CopyAllAnnotations() {
	path=$1
	newpath=$2
	x=`ListAnnotations ${path}`
	
	for i in $x ; do
		echo "working on annotation i=$i"
		curl -k -H  "${AUTHCOOKIE}" "${URL_ROOTPATH}${path}?type=custom-metadata&annotation=$i" | curl -k -iT - -H  "${AUTHCOOKIE}" "${URL_ROOTPATH}${newpath}?type=custom-metadata&annotation=$i"
	done
}

#arg1=HCP path and filename, eg /CustomerScenarios/MP3s/SomeFilename.mp3
#arg2=NEW HCP path and filename, eg /CustomerScenarios/MP3s/SomeFilename.mp3
function CopyObject() {
	path=$1
	newpath=$2
	
	curl -k -H  "${AUTHCOOKIE}" "${URL_ROOTPATH}${path}" | curl -k -iT - -H  "${AUTHCOOKIE}" "${URL_ROOTPATH}${newpath}"
	
	
	CopyAllAnnotations $path $newpath
}

function CopyBetweenNamespaces() {
	#path on HCP
	path=$1
	#source namespace
	src=$2
	
	#target namespace is currently selected via comet.properties (URL_ROOTPATH)
	
#	cmd="curl -k -H  \"${AUTHCOOKIE}\" -H \"X-HCP-CopySource: ${2}.${HCP_TENANT}${path}\"  \"${temp_url}${path}\""

#	echo $cmd
	
	curl -k -i -X PUT -H  "${AUTHCOOKIE}" -H "X-HCP-MetadataDirective: ALL" -H "X-HCP-CopySource: ${2}.${HCP_TENANT}${path}"  "${URL_ROOTPATH}${path}"
	
	
	echo "Authcookie=${AUTHCOOKIE}"
	
	echo "return code=$?"
	
	# now delete the src object
	
#	curl -k -iX DELETE -H  "${AUTHCOOKIE}" "https://${src}.${HCP_TENANT}.${HCP_NAME}.${DOMAIN_NAME}/rest${path}"
}

#arg1 path on HCP
#arg2 src namespace (tenant and target namespace is provided by comet.properties)
function MoveBetweenNamespaces() {
	CopyBetweenNamespaces $1 $2
	# now delete the src object
	
	curl -k -iX DELETE -H  "${AUTHCOOKIE}" "https://${src}.${HCP_TENANT}.${HCP_NAME}.${DOMAIN_NAME}/rest${path}"
}

#arg1=HCP path and filename, eg /CustomerScenarios/MP3s/SomeFilename.mp3
#arg2=NEW HCP path and filename, eg /CustomerScenarios/MP3s/SomeFilename.mp3
function MoveObject() {
	path=$1
	newpath=$2
	CopyObject $path $newpath
	DeleteObject $path

}


#TODO: directory manipulation functions
function CreateDirectory() {
	:
}

function RemoveDirectory() {
	:
}

function ListDirectory() {
	
	path=$1
	curl -k -H  "${AUTHCOOKIE}" "${URL_ROOTPATH}${path}" 2> /dev/null | grep urlName | cut -f 2 -d \"

}

function ShowVars() {

	echo HCP User=$HCP_USER
	echo HCP Password hash=$HCP_PASSWORD
	echo HCP Namespace=$HCP_NAMESPACE
	echo HCP Tenant Name=$HCP_TENANT
	echo HCP Cluster Name=$HCP_NAME
	echo Domain Name=$DOMAIN_NAME
	
	echo HCP Fully Qualified Domain Name FQDN=$HCP_FQDN
	echo HCP URL Root Path=$URL_ROOTPATH
	echo HCP Username Encoded=$UN_ENCODED
	echo HCP Auth Cookie=$AUTHCOOKIE

	echo HCP Admin URL=$HCP_ADMIN_URL
	echo HCP Tenant URL=$HCP_TENANT_URL
	echo HCP Namespace URL=$HCP_NAMESPACE_URL
	echo HCP Search URL=$HCP_SEARCH_URL
	echo HCP Ingest URL=$INGEST_INPUTDIR_URL
	echo HCP NFS URL=$NFS_URL
	echo HCP IP=${HCP_IP}
	echo COMET FQDN=${COMET_NAME}
	
	echo 
	echo COMET Version=${COMET_VERSION}
	echo "Ingestor Enabled=${INGESTOR_ENABLED}"


	echo HCP MAPI URL=${HCP_MAPI_URL}

	if hddsEnabled; then
		echo "HDDS is ENABLED"
		echo HDDS Admin URL=$HDDS_ADMIN_URL
		echo HDDS Search Console URL=$HDDS_SEARCH_CONSOLE_URL
		echo "HDDS Name (FQDN)=$HDDS_NAME"
		echo HDDS User=$HDDS_USER
		echo HDDS IP=$HDDS_IP
	else
		echo "HDDS is disabled"
	fi

	echo "config_mode=${CONFIG_MODE}"

}

function TestComet() {
	echo -e "Authcookie=\"${AUTHCOOKIE}\""
	curl -I -k -H "${AUTHCOOKIE}" ${URL_ROOTPATH}
}

function ObjectExists() {
	
	path=$1
	
	#echo -e "executing: curl -k -I -H \"${AUTHCOOKIE}\" \"${URL_ROOTPATH}${path}\" "
	retcode_curl=`curl -k -I -H "${AUTHCOOKIE}" "${URL_ROOTPATH}${path}" 2>/dev/null | grep -v X-HCP | grep HTTP |cut -f 2 -d ' '`
	
	if [ -z "$retcode_curl" ]; then
		return 1
	fi
	
	retcode=`expr $retcode_curl / 100`
	
	if  [ "$retcode" = "2" ]; then
		return 0
	fi
	return 1
}

function RestoreConfigurationFile() {
	saveas=$1
	if [ -z $saveas ]; then
		saveas="/opt/COMETDist/comet.properties"
	fi
	path=/${COMET_NAME}.properties
	
#	echo path=$path
#	echo saveas=$saveas

	if ObjectExists ${COMET_NAME}.properties; then
	
	#	echo "GetObject $path ${saveas}"
	
		GetObject "$path" "${saveas}"
		if ! [ "$?" = "0" ]; then
			echo "file not restored"
			return 1
		fi
		echo "file restored"
		return 0
	fi
	return 1
}

function BackupConfigurationFile() {
	path=/${COMET_NAME}.properties
	filename=$1
	if [ -z $filename ]; then
		filename="/opt/COMETDist/comet.properties"
	fi

	if ObjectExists ${path}; then
		echo "need to delete it first"
		DeleteObject $path
	fi
	
	PutObject $path $filename
	if ! [ "$?" = "0" ]; then
		echo "file not backed up"
		return 1
	fi
	echo "file backed up"
	return 0
}


function BoolToYesNoPrompt() {
	# $1 is variable name, such as ingestor.enabled
	# $2 is prompt
	
	var_name=${1}
	prompt=${2}
	
	temp=`${EXTRACT} ${var_name} -isbool`
	
	if [ "${temp}" = "true" ]; then
		temp="yes"
	else
		temp="no"
	fi
	
	temp=`prompter --yesnoprompt "${prompt}" "${temp}" --extraprompt`
	if [ "${temp}" = "yes" ]; then
		${STORE} ${var_name}=true
	else
		${STORE} ${var_name}=false
	fi		
}

#TextPrompt "Minutes after failure:" ingestor.hcpFailDelay 2
function TextPrompt() {
	VAR=`${EXTRACT} $2`
	if [ "${VAR}" = "" ]; then
		VAR=$3
	fi
	${STORE} ${2}=`prompter --textprompt ${1} ${VAR}`
	if [ "${VAR}" = "" ]; then
		VAR=$3
		${STORE} ${2}=${VAR}
	fi
}


# recursively delete all the objects
function RecDelete() {
	# step one, list all contents in this directory
	listing=`ListDirectory $1`

	for var in $listing; do
		if [ "$1" = "/" ]; then
			RecDelete /$var
		else
			RecDelete $1/$var
		fi
	done	 
	DeleteObject $1
}

function RotatingBackup() {
	local_path=/backups/`basename $1`
	local_filename=$1
	n=$2

	if [ -z $n ]; then
		n=10
	fi

	export CONFIG_MODE=true
	ReloadVars
#	curl -k -iT $filename -H  "${AUTHCOOKIE}" "${URL_ROOTPATH}${path}"
	#first, delete $n object
	
	DeleteObject ${local_path}-${n}
	echo "DeleteObject ${local_path}-${n}"
	for ((i = $n ; i >1 ; i--)); do
		# working on ith iteration
		
		ObjectExists ${local_path}-`expr $i - 1`
		if [ "$?" = "0" ]; then
			MoveObject ${local_path}-`expr $i - 1` ${local_path}-$i
			echo "MoveObject ${local_path}-`expr $i - 1` ${local_path}-$i"
		fi
	done
	
	ObjectExists ${local_path}
	if [ "$?" = "0" ]; then
		MoveObject ${local_path} ${local_path}-1
		echo "MoveObject ${local_path} ${local_path}-1"
	fi
	
	
	echo "PutObject ${local_path} ${local_filename}"
	PutObject ${local_path} ${local_filename}
	export CONFIG_MODE=false
	ReloadVars

}

