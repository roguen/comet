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
# Package: COMET Web Application
# Author: Chris Delezenski <chris.delezenski@hdsfed.com>
# Compilation Date: 2015-05-06
# License: Apache License, Version 2.0
# Version: 1.21.0
# (RPM) Release: 1
# SVN: r551+
PROGRAM=MetaCatalog
WAR=ROOT.war
PWD=$(shell pwd)
VER=$(shell cometdev_getvar.sh version)
NAME=COMET-$(PROGRAM)
PNAME=$(NAME)-$(VER)
SED=$(shell cometdev_getvar.sh sed)
RPMTOP=$(shell cometdev_getvar.sh rpmtop)
PREFIX=$(shell cometdev_getvar.sh prefix)
GITDIR=$(shell cometdev_getvar.sh gitdir)

INSTALLDIR=/opt/COMETDist/apache-tomcat/webapps/

war:
	cometdev_antbuild.sh create_war

all_local:
	mkdir -p ./WebContent/WEB-INF/lib/
	
	# FIXME:
	# shouldn't have to copy these all into the war
	# in fact it is not recommended by people that seem to know what they're doing
	# however, I don't know any better.. and things break horribly if we don't
	# I don't have time to learn how to do this the right way
	cp -avpf /usr/share/java-alternate/lib/commons-codec-1.6.jar ./WebContent/WEB-INF/lib/
	cp -avpf /usr/share/java-alternate/lib/commons-logging-1.1.1.jar ./WebContent/WEB-INF/lib/
#	cp -vf /usr/share/java-alternate/lib/HCPHelpersForJava.jar  ./WebContent/WEB-INF/lib/
#	cp -vf /usr/share/java-alternate/lib/HDSCometHLApi.jar  ./WebContent/WEB-INF/lib/
	cp -avpf /usr/share/java-alternate/lib/httpclient-4.2.5.jar  ./WebContent/WEB-INF/lib/
	cp -avpf /usr/share/java-alternate/lib/httpcore-4.2.4.jar  ./WebContent/WEB-INF/lib/
	cp -avpf /usr/share/java-alternate/lib/jackson-all-1.6.1.jar ./WebContent/WEB-INF/lib/
	cp -avpf /usr/share/java-alternate/lib/json-lib-2.3-jdk15.jar ./WebContent/WEB-INF/lib/
#	cp -avpf /usr/share/java-alternate/lib/servlet-api.jar ./WebContent/WEB-INF/lib/
#	cp -avpf /usr/share/java-alternate/lib/sqlite-jdbc-3.7.2.jar ./WebContent/WEB-INF/lib/
#	cp -avpf /usr/share/java-alternate/lib/jedis-2.1.0.jar ./WebContent/WEB-INF/lib/

clean:
	rm -rf temp_unrpm
	rm -rf $(PROGRAM).war $(PNAME) $(NAME).spec $(PNAME)*.rpm $(NAME)-*
	rm -rf ./WebContent/WEB-INF/lib/
	cometdev_antbuild.sh clean
	

clean_local: clean
	sudo rm -rvf /opt/COMETDist/apache-tomcat/webapps/MetaCatalog*
	sudo truncate -s0 /opt/COMETDist/apache-tomcat/logs/catalina.out
	
restart: 
	> /opt/COMETDist/apache-tomcat/logs/catalina.out
	$(SHUTDOWNTC)
	$(STARTTC)

deploy: clean_local
	mkdir -p $(PNAME)

	cp -avpf build.xml $(PNAME)/
	sed '$(SED)' < Makefile > $(PNAME)/Makefile

	svn export WebContent $(PNAME)/WebContent
	rm -rf $(PNAME)/WebContent/*.html
	rm -rf $(PNAME)/WebContent/js/comet.js
	rm -rf $(PNAME)/WebContent/js/comet*.js
	rm -rf $(PNAME)/WebContent/js/googleearth.js
	rm -rf $(PNAME)/WebContent/css/*.css
		
	cometdev_sed_java.sh WebContent/js $(PNAME) js
	cometdev_sed_java.sh WebContent $(PNAME) html
	cometdev_sed_java.sh WebContent/css $(PNAME) css
	
	#cp -avpf WebContent/WEB-INF/lib  $(PNAME)/WebContent/WEB-INF/	

	mkdir -p $(PNAME)/src/main
	mkdir -p $(PNAME)/src/coreservlets

	cometdev_sed_java.sh src $(PNAME)


	make -C $(PNAME) deploy_inner
deployjs:
	sudo cp -avpf WebContent/js/comet*.js /opt/COMETDist/apache-tomcat/webapps/ROOT/js/
	sudo cp -avpf WebContent/css/*.css /opt/COMETDist/apache-tomcat/webapps/ROOT/css/
	sudo cp -avpf WebContent/*.html /opt/COMETDist/apache-tomcat/webapps/ROOT/


deploy_inner: all_local war 
	sudo cp -avf $(WAR) $(INSTALLDIR)

install:
	mkdir -p $(DESTDIR)/opt/COMETDist/ondeck/
	mkdir -p $(DESTDIR)/opt/COMETDist/previous/
	# move from ondeck to apache-tomcat/Webapps/ during post
	# move old webapp to /opt/COMETDist/prior
	cp -vf $(WAR) $(DESTDIR)/opt/COMETDist/ondeck/
	

rpms: clean
	mkdir -p $(PNAME)

	cp -avpf build.xml $(PNAME)/
	sed '$(SED)' < Makefile > $(PNAME)/Makefile
	sed '$(SED)' < $(NAME).spec.in > $(NAME).spec
	cometdev_install_common.sh $(PNAME)/
	touch $(PNAME)/configure
	chmod 755 $(PNAME)/configure

	cp -avpf $(PREFIX)/devel/custom/LICENSE $(PNAME)/

	svn export WebContent $(PNAME)/WebContent
	rm -rf $(PNAME)/WebContent/*.html
	rm -rf $(PNAME)/WebContent/js/comet.js
	rm -rf $(PNAME)/WebContent/js/comet*.js
	rm -rf $(PNAME)/WebContent/js/googleearth.js
	rm -rf $(PNAME)/WebContent/css/*.css
		
	cometdev_sed_java.sh WebContent/js $(PNAME) js
	cometdev_sed_java.sh WebContent $(PNAME) html
	cometdev_sed_java.sh WebContent/css $(PNAME) css
	
#	mkdir temp_unrpm
#	(cd temp_unrpm && cometdev_unrpm.sh ../../HDSCometHLApi/COMET-HDSCometHLApi*.noarch.rpm)
#	(cd temp_unrpm && mv ./opt/COMETDist/libs ../$(PNAME)/WebContent/WEB-INF/lib)

	mkdir -p $(PNAME)/src/main
	mkdir -p $(PNAME)/src/coreservlets

	cometdev_sed_java.sh src $(PNAME)



	tar -cvp $(PNAME) -f - | gzip > $(RPMTOP)/SOURCES/$(PNAME).tar.gz
	rpmbuild -ba ./$(NAME).spec --target noarch
	mv -vf $(RPMTOP)/RPMS/noarch/$(PNAME)-* .
	mv -vf $(RPMTOP)/SRPMS/$(PNAME)-* .

buildlib: clean
	(cd ../HDSCometHLApi && make clean deploy)

diff:
	svn diff > diffs.txt
	nedit diffs.txt &
	
gitexport: clean
	cp -avpf build.xml $(GITDIR)/MetaCatalog/
	sed '$(SED)' < Makefile > $(GITDIR)/MetaCatalog/Makefile
	sed '$(SED)' < $(NAME).spec.in > $(GITDIR)/MetaCatalog/$(NAME).spec
	cometdev_install_common.sh $(GITDIR)/MetaCatalog/

	cp -avpf $(PREFIX)/devel/custom/LICENSE $(GITDIR)/MetaCatalog/

	svn export WebContent $(GITDIR)/MetaCatalog/WebContent
	rm -rf $(GITDIR)/MetaCatalog/WebContent/*.html
	rm -rf $(GITDIR)/MetaCatalog/WebContent/js/comet.js
	rm -rf $(GITDIR)/MetaCatalog/WebContent/js/comet*.js
	rm -rf $(GITDIR)/MetaCatalog/WebContent/js/googleearth.js
	rm -rf $(GITDIR)/MetaCatalog/WebContent/css/*.css
		
	cometdev_sed_java.sh WebContent/js $(GITDIR)/MetaCatalog/ js
	cometdev_sed_java.sh WebContent $(GITDIR)/MetaCatalog/ html
	cometdev_sed_java.sh WebContent/css $(GITDIR)/MetaCatalog/ css
	
#	mkdir temp_unrpm
#	(cd temp_unrpm && cometdev_unrpm.sh ../../HDSCometHLApi/COMET-HDSCometHLApi*.noarch.rpm)
#	(cd temp_unrpm && mv ./opt/COMETDist/libs ../$(PNAME)/WebContent/WEB-INF/lib)

	mkdir -p $(GITDIR)/MetaCatalog/src/main
	mkdir -p $(GITDIR)/MetaCatalog/src/coreservlets

	cometdev_sed_java.sh src $(GITDIR)/MetaCatalog/



