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

#purpose: to automatically transcode a video to /tmp/ingest-tempdir/videoname.webm


FFMPEG="/opt/COMETDist/bin/ffmpeg"

INFILE=$1

OUTFILE=`basename $1 .m4v`


CMDLINE="${FFMPEG} -i ${INFILE} -c:v libvpx -b:v 1M -c:a libvorbis /tmp/ingest-tempdir/${OUTFILE}.webm"


if [ -e /tmp/ingest-tempdir/${OUTFILE}.webm ]; then
	echo "file already exists, deleting"
	rm -rf /tmp/ingest-tempdir/${OUTPUTFILE}.webm
fi


echo $CMDLINE

$CMDLINE

ret=$?

exit $ret

