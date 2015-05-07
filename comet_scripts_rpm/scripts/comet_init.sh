#!/bin/bash
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
#
# comet        Startup script for the HDS COMET Server
#
# chkconfig: - 90 10
# description: COMET Server
# processname: comet
# config: /opt/COMETDist/comet.properties
# pidfile: /var/run/comet.pid
#

# Source function library.
. /etc/rc.d/init.d/functions

if [ -f /etc/sysconfig/comet ]; then
        . /etc/sysconfig/comet
fi

. /opt/COMETDist/include/hcp_shell_api.sh

ReloadVars

comet=${COMETD-/usr/bin/comet_service.sh}
prog="comet"
pidfile=${PIDFILE-/var/run/comet.pid}
lockfile=${LOCKFILE-/var/lock/subsys/comet}
RETVAL=0
STOP_TIMEOUT=${STOP_TIMEOUT-10}

start() {

        #wb=`cometcfg -e banner -isbool`
	
	#if [ "$wb" = "true" ]; then
	#	cp -f /opt/COMETDist/index_wb.jsp /opt/COMETDist/apache-tomcat/webapps/ROOT/index.jsp
	#	cp -f /opt/COMETDist/banner.html /opt/COMETDist/apache-tomcat/webapps/ROOT/
 	#else
	#	cp -f /opt/COMETDist/index_orig.jsp /opt/COMETDist/apache-tomcat/webapps/ROOT/index.jsp
	#fi	      
 
 
 	TMP_DIR=ingest-tempdir
 	rm -rf /tmp/$TMP_DIR && mkdir -p /tmp/$TMP_DIR && chown comet:comet /tmp/$TMP_DIR
	TMP_DIR=METACATALOG
 	rm -rf /tmp/$TMP_DIR && mkdir -p /tmp/$TMP_DIR && chown comet:comet /tmp/$TMP_DIR

	rm -rf /opt/COMETDist/ingestor.stop

	echo -n $"Starting $prog: "
	#$prog $OPTIONS --pid-file /var/run/comet.pid
	
	
	running=`service comet status`

	if [ "$running" = "comet is running" ]; then	
		failure "$prog startup"
		echo
		return 1
	fi
	
	${prog}_ingestor.sh >/dev/null 2>&1 &
	
	
	
	
	RETVAL=0
	if [ $RETVAL -eq 0 ] ; then
		success "$prog startup"
	else
		failure "$prog startup"
	fi
	if [ $RETVAL -eq 0 ]; then
		touch /var/lock/subsys/comet
	else
		RETVAL=1
	fi
	echo
	return $RETVAL
}

stop() {

	#/usr/bin/comet_rotating_backup.sh /opt/COMETDist/fileProc.cache >/dev/null 2>&1 


	echo -n $"Stopping $prog: "
	count=0
#	while [ -n "`pidof ${prog}_ingestor.sh`" -a $count -lt 15 ] ; do
#		killproc $prog -TERM >& /dev/null
#		RETVAL=$?
#		[ $RETVAL = 0 -a -z "`pidof ${prog}_ingestor.sh`" ] || sleep 3
#		count=`expr $count + 1`
#	done
	
	touch /opt/COMETDist/ingestor.stop
	
	if ! [ -e /var/lock/subsys/comet ]; then 
		failure "$prog shutdown"
		echo
		return 1
	fi
	
	killall ffmpeg
	
	#RETVAL=$?

#	if [ $RETVAL -eq 0 ]; then
		success "$prog shutdown"
#	else
#		RETVAL=1
#		failure "$prog shutdown"
#	fi
	echo
	
	
	
	
	return $RETVAL

}

forcestop() {
	echo "WARNING: this method of stopping COMET is not safe"
	echo "proceed? (type yes to proceed)"

	read yes
	if ! [ "$yes" = "yes" ]; then
		exit 1
	fi

	rm -f /var/lock/subsys/comet
	
	touch /opt/COMETDist/ingestor.stop
	killpid=`cat /var/run/comet.pid`
	count=0
	#sleep up to 5 minutes before we violently pull the plug on comet
	quit=0;
	while (( (quit == 0) && (count < 30) )); do
		kill -s 0 ${killpid} >/dev/null 2>&1
		quit=$?
		count=`expr $count + 1`
		
		sleep 1
	done
	
	kill -9 $killpid >/dev/null 2>&1
	rm -f /var/run/comet.pid	
	
	killpid="-1"
	while ! [ -z "$killpid" ]; do
		killpid=`ps aux  | grep Ingestor.jar | grep -v grep | cut -f 6 -d ' '`
		kill -9 $killpid >/dev/null 2>&1
		killpid=`ps aux  | grep Ingestor.jar | grep -v grep | cut -f 7 -d ' '`
		kill -9 $killpid >/dev/null 2>&1
	done
	
	rm -rf /opt/COMETDist/ingestor.stop
}

# See how we were called.
case "$1" in
  start)
	start
	;;
  stop)
	stop
	;;
  forcestop)
  	forcestop
	;;
  pause)
  	if [ -e /opt/COMETDist/ingestor.pause ]; then
		echo "comet is already paused"
	else
		echo "comet is paused"
		touch /opt/COMETDist/ingestor.pause
	fi
	;;
  resume)
  	if [ -e /opt/COMETDist/ingestor.pause ]; then
		echo "comet will resume"
		rm /opt/COMETDist/ingestor.pause
	else
		echo "comet is not paused"
	fi
	;;
  status)
  	if [ -e /var/run/comet.pid ]; then
		
		pid=`cat /var/run/comet.pid`
		
		kill -0 $pid >/dev/null 2>&1
		result=$?
		if [ "$result" = "0" ]; then
			if [ -e /opt/COMETDist/ingestor.stop ]; then
				echo "comet is shutting down"
			else 
				if [ -e /opt/COMETDist/ingestor.pause ]; then
					echo "comet is paused"
				else 
					echo "comet is running"
				fi
			fi
		else
			echo "comet is stopped"
			rm -rf /var/run/comet.pid
		fi
		
	else
		echo "comet is stopped"
	fi
	;;
  restart)
	stop
	start
	;;
  *)
	echo $"Usage: $prog {start|stop|restart|status|pause|resume}"
	RETVAL=2
esac

exit $RETVAL
