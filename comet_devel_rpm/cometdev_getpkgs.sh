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

# this script will use yumdownloader to pull the listed RPMs from
# the internet


# put code here to freshen the packages in payload/packages/ from custom

# populate custom/extra_packages.txt with RPMs to pull from non-standard repositories (eg. epel, rpmforge etc)

TOPDIR=`cometdev_getvar.sh topdir`
REPO=`cometdev_getvar.sh repo`
BASE=${TOPDIR}/${REPO}/custom

YUM_PACKAGES_FILE=${BASE}/yum_packages.txt


if [ -e "${YUM_PACKAGES_FILE}" ]; then
	echo "Extra packages list found"
else
	echo "No additional packages required"
	exit 0
fi

#LOCAL_YUM='--disablerepo=\* --enablerepo=c6-media'
#if ! [ -e /media/CentOS ]; then
#	if ! ln -s /media/CentOS_* /media/CentOS; then
#		echo "Unable to create symlink for rpm install; using networking for yum instead"
#		echo "WARNING: yum via network may fail if a proxy is required!"
#		echo "Press Enter to proceed"
#		read enter
#		LOCAL_YUM=""
#	fi
#fi


echo "Downloading RPMs..."

while read line ;do
	sudo yumdownloader $line
done < ${YUM_PACKAGES_FILE}


#if [ -e ../../custom/skip_custom_packages ]; then
#	echo "Skipping custom packages"
#	rm -rf *.1
#else
#
#if [ -e ../../custom/get_custom_packages.sh ]; then
#	echo "Found custom package script"
#	if ! ../../custom/get_custom_packages.sh; then
#		echo "Unable to complete custom package script"
#	fi
#fi
#
#fi

