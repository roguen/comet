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
# Package: Custom Object Metadata Enhancement Toolkit shared library
# Author: Chris Delezenski <chris.delezenski@hdsfed.com>
# Compilation Date: 2015-05-06
# License: Apache License, Version 2.0
# Version: 1.21.0
# (RPM) Release: 1
# SVN: r551+

PWD=$(shell pwd)
VER=$(shell cometdev_getvar.sh version)
LIBRARY=HDSCometHLApi
JAR=$(LIBRARY).jar
NAME=COMET-$(LIBRARY)
PNAME=$(NAME)-$(VER)
SED=$(shell cometdev_getvar.sh sed)
RPMTOP=$(shell cometdev_getvar.sh rpmtop)
PREFIX=$(shell cometdev_getvar.sh prefix)
GITDIR=$(shell cometdev_getvar.sh gitdir)

all:
	cometdev_antbuild.sh compile	
	cometdev_antbuild.sh create_jar

clean:
	cometdev_antbuild.sh clean
	rm -rf $(LIBRARY).jar $(PNAME) $(NAME).spec $(NAME)-*
diff:
	svn diff > diffs.txt
	nedit diffs.txt &

install: all
	mkdir -p $(DESTDIR)$(PREFIX)/libs/
	cp -vf $(JAR) $(DESTDIR)$(PREFIX)/libs/
	cp -avpf /usr/share/java-alternate/lib/commons-codec-1.6.jar $(DESTDIR)$(PREFIX)/libs/
	cp -avpf /usr/share/java-alternate/lib/commons-logging-1.1.1.jar $(DESTDIR)$(PREFIX)/libs/
	cp -vf /usr/share/java-alternate/lib/HCPHelpersForJava.jar $(DESTDIR)$(PREFIX)/libs/
	cp -avpf /usr/share/java-alternate/lib/httpclient-4.2.5.jar $(DESTDIR)$(PREFIX)/libs/
	cp -avpf /usr/share/java-alternate/lib/httpcore-4.2.4.jar $(DESTDIR)$(PREFIX)/libs/
	cp -avpf /usr/share/java-alternate/lib/im4java-1.4.0.jar $(DESTDIR)$(PREFIX)/libs/
	cp -avpf /usr/share/java-alternate/lib/jackson-all-1.6.1.jar $(DESTDIR)$(PREFIX)/libs/
	cp -avpf /usr/share/java-alternate/lib/jar-in-jar-loader.zip $(DESTDIR)$(PREFIX)/libs/
	cp -avpf /usr/share/java-alternate/lib/jid3lib-0.5.4.jar $(DESTDIR)$(PREFIX)/libs/
	cp -avpf /usr/share/java-alternate/lib/json-lib-2.3-jdk15.jar $(DESTDIR)$(PREFIX)/libs/
	cp -avpf /usr/share/java-alternate/lib/servlet-api.jar $(DESTDIR)$(PREFIX)/libs/
	cp -avpf /usr/share/java-alternate/lib/sqlite-jdbc-3.7.2.jar $(DESTDIR)$(PREFIX)/libs/
	cp -avpf /usr/share/java-alternate/lib/jedis-2.1.0.jar $(DESTDIR)$(PREFIX)/libs/
	
rpms: 
	mkdir -p $(PNAME)
	cp -avpf build.xml $(PNAME)/
	sed '$(SED)' < Makefile > $(PNAME)/Makefile
	sed '$(SED)' < $(NAME).spec.in > $(NAME).spec
	cometdev_install_common.sh $(PNAME)/
	touch $(PNAME)/configure
	chmod 755 $(PNAME)/configure

	cometdev_antbuild.sh clean
	mkdir -p $(PNAME)/src/com/hdsfed/cometapi/
	cometdev_sed_java.sh src $(PNAME)

	cp -avpf $(PREFIX)/devel/custom/LICENSE $(PNAME)/


	tar -cvp $(PNAME) -f - | gzip > $(RPMTOP)/SOURCES/$(PNAME).tar.gz
	rpmbuild -ba ./$(NAME).spec --target noarch
	mv -vf $(RPMTOP)/RPMS/noarch/$(PNAME)-* .
	mv -vf $(RPMTOP)/SRPMS/$(PNAME)-* .

deploy:
	
	mkdir -p $(PNAME)
	cp -avpf build.xml $(PNAME)/
	sed '$(SED)' < Makefile > $(PNAME)/Makefile
	cometdev_install_common.sh $(PNAME)/

	cometdev_antbuild.sh clean
	mkdir -p $(PNAME)/src/com/hdsfed/cometapi/
	cometdev_sed_java.sh src $(PNAME)


	make -C $(PNAME) deploy_inner

deploy_inner:
	cometdev_antbuild.sh compile
	cometdev_antbuild.sh create_jar
	sudo cp -vf $(JAR) ../libs
	sudo cp -vf $(JAR) $(PREFIX)/libs/

	
gitexport: clean 
	cp -avpf build.xml $(GITDIR)/HDSCometHLApi/
	sed '$(SED)' < Makefile > $(GITDIR)/HDSCometHLApi/Makefile
	sed '$(SED)' < $(NAME).spec.in > $(GITDIR)/HDSCometHLApi/$(NAME).spec
	cometdev_install_common.sh $(GITDIR)/HDSCometHLApi/
	#touch $(PNAME)/configure
	#chmod 755 $(PNAME)/configure

	cometdev_antbuild.sh clean
	mkdir -p $(GITDIR)/HDSCometHLApi/src/com/hdsfed/cometapi/
	cometdev_sed_java.sh src $(GITDIR)/HDSCometHLApi/

	cp -avpf $(PREFIX)/devel/custom/LICENSE $(GITDIR)/HDSCometHLApi/


