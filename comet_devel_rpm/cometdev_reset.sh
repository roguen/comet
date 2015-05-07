#!/bin/sh

sudo service comet stop
sudo service tomcat stop

quit=0

while ! [ $quit -eq 1 ]; do
	echo "while not quit"
	
	tc=`sudo service tomcat status`
	c=`sudo service comet status`
	
	echo "tc=$tc"
	echo "c=$c"
	
	if [ "$c" == "comet was stopped" ]; then
		quit=1
	fi
	
	sleep 1
done
sudo rm -rf /var/log/comet/*

ps aux | grep java | grep -v eclipse | grep -v grep
