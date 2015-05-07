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
# Package: COMET::Data Ingestor Service
# Author: Chris Delezenski <chris.delezenski@hdsfed.com>
# Compilation Date: 2015-05-06
# License: Apache License, Version 2.0
# Version: 1.21.0
# (RPM) Release: 1
# SVN: r554
PROGRAM=Ingestor
JAR=$(PROGRAM).jar
PWD=$(shell pwd)
VER=$(shell cometdev_getvar.sh version)
NAME=COMET-$(PROGRAM)
PNAME=$(NAME)-$(VER)
SED=$(shell cometdev_getvar.sh sed)
RPMTOP=$(shell cometdev_getvar.sh rpmtop)
PREFIX=$(shell cometdev_getvar.sh prefix)
GITDIR=$(shell cometdev_getvar.sh gitdir)

INSTALLDIR=/opt/COMETDist/libs/


compile:
	cometdev_antbuild.sh compile
	make -C gdalnativelib -f Makefile
	cometdev_antbuild.sh create_run_jar
clean:
	cometdev_antbuild.sh clean
	rm -rf $(JAR) $(PNAME) $(NAME).spec $(PNAME)*rpm $(NAME)-*
	make -C gdalnativelib -f Makefile clean


deploy_inner: compile
	
	cometdev_sed.sh in .
	sudo cp -avpf comet_ingestor.sh /usr/bin/
	sudo cp -vf $(JAR) /opt/COMETDist/libs/
	sudo cp -avpf gdalnativelib/*so  /opt/COMETDist/libs/

install: compile

	mkdir -p $(DESTDIR)/usr/bin/
	cp -avpf comet_ingestor.sh $(DESTDIR)/usr/bin/
	mkdir -p $(DESTDIR)/opt/COMETDist/libs/

	mkdir -p $(DESTDIR)/opt/COMETDist/InputDir
	sudo cp -vf $(JAR) $(DESTDIR)/opt/COMETDist/libs/
	sudo cp -avpf gdalnativelib/*so  $(DESTDIR)/opt/COMETDist/libs/

deploy: clean 
	mkdir -p $(PNAME)
	sed '$(SED)' < Makefile > $(PNAME)/Makefile
	cometdev_install_common.sh $(PNAME)/

	cp -avpf build.xml $(PNAME)/
	cp -avpf jar-in-jar-loader.zip $(PNAME)/
	mkdir -p $(PNAME)/src/ingestor/metadata
	mkdir -p $(PNAME)/src/ingestor/postprocessing
	mkdir -p $(PNAME)/src/ingestor/walkers
	cometdev_sed_java.sh src $(PNAME)
	mkdir -p $(PNAME)/gdalnativelib
	cometdev_sed_java.sh gdalnativelib $(PNAME) c
	cometdev_sed_java.sh gdalnativelib $(PNAME) h
	sed '$(SED)' < gdalnativelib/Makefile > $(PNAME)/gdalnativelib/Makefile

	cometdev_sed.sh in $(PNAME)

	make -C $(PNAME) deploy_inner

rpms: clean
	mkdir -p $(PNAME)
	sed '$(SED)' < Makefile > $(PNAME)/Makefile
	sed '$(SED)' < $(NAME).spec.in > $(NAME).spec
	cometdev_install_common.sh $(PNAME)/
	touch $(PNAME)/configure
	chmod 755 $(PNAME)/configure

	cp -avpf build.xml $(PNAME)/
	cp -avpf jar-in-jar-loader.zip $(PNAME)/
	mkdir -p $(PNAME)/src/ingestor/metadata
	mkdir -p $(PNAME)/src/ingestor/postprocessing
	mkdir -p $(PNAME)/src/ingestor/walkers
	cometdev_sed_java.sh src $(PNAME)
	mkdir -p $(PNAME)/gdalnativelib
	cometdev_sed_java.sh gdalnativelib $(PNAME) c
	cometdev_sed_java.sh gdalnativelib $(PNAME) h
	sed '$(SED)' < gdalnativelib/Makefile > $(PNAME)/gdalnativelib/Makefile

	cometdev_sed.sh in $(PNAME)

	cp -avpf $(PREFIX)/devel/custom/LICENSE $(PNAME)/


	tar -cvp $(PNAME) -f - | gzip > $(RPMTOP)/SOURCES/$(PNAME).tar.gz
	rpmbuild -ba ./$(NAME).spec 
	mv -vf $(RPMTOP)/RPMS/$(shell arch)/$(NAME)-* .
	mv -vf $(RPMTOP)/SRPMS/$(NAME)-* .


buildlib: clean
	(cd ../HDSCometHLApi && sudo make clean install)
diff:
	svn diff > diffs.txt
	nedit diffs.txt &

gitexport: clean
	
	sed '$(SED)' < Makefile > $(GITDIR)/Ingestor/Makefile
	sed '$(SED)' < $(NAME).spec.in > $(GITDIR)/Ingestor/$(NAME).spec
	cometdev_install_common.sh $(GITDIR)/Ingestor/


	cp -avpf build.xml $(GITDIR)/Ingestor/
	cp -avpf jar-in-jar-loader.zip $(GITDIR)/Ingestor/
	mkdir -p $(GITDIR)/Ingestor/src/ingestor/metadata
	mkdir -p $(GITDIR)/Ingestor/src/ingestor/postprocessing
	mkdir -p $(GITDIR)/Ingestor/src/ingestor/walkers
	cometdev_sed_java.sh src $(GITDIR)/Ingestor
	mkdir -p $(GITDIR)/Ingestor/gdalnativelib
	cometdev_sed_java.sh gdalnativelib $(GITDIR)/Ingestor/ c
	cometdev_sed_java.sh gdalnativelib $(GITDIR)/Ingestor/ h
	sed '$(SED)' < gdalnativelib/Makefile > $(GITDIR)/Ingestor/gdalnativelib/Makefile

	cometdev_sed.sh in $(GITDIR)/Ingestor/

	cp -avpf $(PREFIX)/devel/custom/LICENSE $(GITDIR)/Ingestor/


