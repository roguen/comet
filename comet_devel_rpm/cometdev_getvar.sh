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

if [ -z "$1" ]; then
	echo "missing argument"
	exit 1
fi

export TOPDIR=/workspace
export REPO=`cat /opt/COMETDist/devel/custom/TRUNK`
export CUSTOM=${TOPDIR}/${REPO}/custom
export UU="__"

case $1 in
	timestamp)
		date +%m-%d-%Y-%H%M%S
	;;
	rpmtop)
		echo ${TOPDIR}/RPM
	;;
	topdir)
		echo ${TOPDIR}
	;;
	prefix)
		echo "/opt/COMETDist"
	;;
	repo)
		echo ${REPO}
	;;
	workingcopy)
		echo ${TOPDIR}/${REPO}
	;;
	cdwc)
		cd ${TOPDIR}/${REPO}
	;;
	targetdir)
		cat ${CUSTOM}/SED_TARGETDIR
	;;
	author)
		if [ -e ./SED_AUTHOR ]; then
			AUTHOR=`cat ./SED_AUTHOR`
		else
			AUTHOR=`cat ${CUSTOM}/SED_AUTHOR`
		fi
		echo $AUTHOR
	;;
	copyright)
		if [ -e ./SED_COPYRIGHT ]; then
			COPYRIGHT=`cat ./SED_COPYRIGHT`
		else
			COPYRIGHT=`cat ${CUSTOM}/SED_COPYRIGHT`
		fi
		echo $COPYRIGHT

	;;
	license)
		if [ -e ./SED_LICENSE ]; then
			LICENSE=`cat ./SED_LICENSE`
		else
			LICENSE=`cat ${CUSTOM}/SED_LICENSE`
		fi
		echo $LICENSE
	;;
	release)
		if [ -e ./SED_RELEASE ]; then
			RELEASE=`cat ./SED_RELEASE`
		else
			RELEASE=1
		fi
		echo $RELEASE
	;;
	description)
		if [ -e ./SED_DESCRIPTION ]; then
			DESCRIPTION=`cat ./SED_DESCRIPTION`
		else
			DESCRIPTION="missing description file"
		fi
		echo $DESCRIPTION
	;;
	version)
		if [ -e ./SED_VERSION ]; then
			VERSION=`cat ./SED_VERSION`
		else
			MAJOR=`cat ${TOPDIR}/${REPO}/ver/MAJOR_VER`
			MINOR=`cat ${TOPDIR}/${REPO}/ver/MINOR_VER`
			SUB=`cat ${TOPDIR}/${REPO}/ver/SUB_MINOR_VER`
			BUILD_PRE=`cat ${TOPDIR}/${REPO}/ver/BUILD_VER_PRE`
			BUILD_RC=`cat ${TOPDIR}/${REPO}/ver/BUILD_VER_RC`
			PRE=`cat ${TOPDIR}/${REPO}/ver/PRE`
			RC=`cat ${TOPDIR}/${REPO}/ver/RC`
			
			if [ "$PRE" == "1" ]; then
				VERSION="${MAJOR}.${MINOR}.${SUB}pre${BUILD_PRE}"
			elif ! [ "$RC" == "0" ]; then
				VERSION="${MAJOR}.${MINOR}.${SUB}RC${BUILD_RC}"
			else
				VERSION="${MAJOR}.${MINOR}.${SUB}"
			fi	
		fi
		echo $VERSION
	;;
	svn)
		if [ -e ./SED_SVN ]; then
			cat ./SED_SVN
		else
			svn update >/dev/null 2>&1
			rev=`svn info | grep Revision | cut -f 2 -d ' '`
			ch=`svn st | grep -v ? | wc -l`
			
			if [ "$ch" = "0" ]; then
				echo $rev
			else
				echo "${rev}+"
			fi
		fi
	;;
	sed)
	 echo 's/'${UU}'RELEASE__/'`cometdev_getvar.sh release`'/g;s/'${UU}'AUTHOR__/'`cometdev_getvar.sh author`'/g;s/'${UU}'COPYRIGHT__/'`cometdev_getvar.sh copyright`'/g;s/'${UU}'DATE__/'`date -I`'/g;s/'${UU}'LICENSE__/'`cometdev_getvar.sh license`'/g;s/'${UU}'VERSION__/'`cometdev_getvar.sh version`'/g;s/'${UU}'SVN__/'`cometdev_getvar.sh svn`'/g;s/'${UU}'DESCRIPTION__/'`cometdev_getvar.sh description`'/g;'
	;;
esac
