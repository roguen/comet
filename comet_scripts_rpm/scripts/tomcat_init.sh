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
# SVN: r554
#
# tomcat        Startup script for the apache tomcat Server
#
# chkconfig: - 85 15
# description: Tomcat Server
# processname: tomcat
# config: /usr/share/apache-tomcat/
# pidfile: /var/run/tomcat.pid
#

# Source function library.
. /etc/rc.d/init.d/functions

if [ -f /etc/sysconfig/tomcat ]; then
        . /etc/sysconfig/tomcat
fi

tomcat=/opt/COMETDist/apache-tomcat/bin/startup.sh
prog="tomcat"
pidfile=${PIDFILE-/var/run/tomcat.pid}
lockfile=${LOCKFILE-/var/lock/subsys/tomcat}
RETVAL=0
STOP_TIMEOUT=${STOP_TIMEOUT-10}

# The semantics of these two functions differ from the way apachectl does
# things -- attempting to start while running is a failure, and shutdown
# when not running is also a failure.  So we just do it the way init scripts
# are expected to behave here.
start() {
	touch /TOMCAT_WAS_STARTED
        echo -n $"Starting $prog: "
        $tomcat && success || failure
        RETVAL=$?
        [ $RETVAL = 0 ] && touch ${lockfile}
        echo
        return $RETVAL
}

# When stopping tomcat, a delay (of default 10 second) is required
# before SIGKILLing the tomcat parent; this gives enough time for the
# tomcat parent to SIGKILL any errant children.
stop() {
	echo -n $"Stopping $prog: "
#	killproc -p ${pidfile} -d ${STOP_TIMEOUT} $tomcat
	/opt/COMETDist/apache-tomcat/bin/shutdown.sh
	RETVAL=$?
	echo
	[ $RETVAL = 0 ] && rm -f ${lockfile} ${pidfile}
}


# See how we were called.
case "$1" in
  start)
	start
	;;
  stop)
	stop
	;;
  status)
 #        status -p ${pidfile} $tomcat
	result=`ps aux| grep java | grep tomcat | wc -l`
	
	if [ "$result" = "0" ]; then
		RETVAL=1
		echo "tomcat is stopped"
	else
		RETVAL=0
		echo "tomcat is running"
	fi
	
	#RETVAL=$?
	;;
  restart)
	stop
	sleep 1
	start
	;;
  *)
	echo $"Usage: $prog {start|stop|restart|status}"
	RETVAL=2
esac

exit $RETVAL
