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
# Package: COMET binary helper programs
# Author: Chris Delezenski <chris.delezenski@hdsfed.com>
# Compilation Date: 2015-05-06
# License: Apache License, Version 2.0
# Version: 1.21.0
# (RPM) Release: 1
# SVN: r554


PWD=$(shell pwd)
VER=$(shell cometdev_getvar.sh version)
NAME=COMET-Tools
PNAME=$(NAME)-$(VER)
SED=$(shell cometdev_getvar.sh sed)
RPMTOP=$(shell cometdev_getvar.sh rpmtop)
PREFIX=$(shell cometdev_getvar.sh prefix)
GITDIR=$(shell cometdev_getvar.sh gitdir)

all: 
	make -C cometcfg
	make -C passcheck
	make -C prompter
	make -C classpath
clean: 
	rm -rf $(PNAME) $(NAME).spec $(PNAME)*rpm $(NAME)-* 
	make clean -C cometcfg
	make clean -C passcheck
	make clean -C prompter
	make clean -C classpath
	
diff: clean
	svn diff > diffs.txt
	nedit diffs.txt &


install: all
	mkdir -p $(DESTDIR)/usr/bin
	
	install -m 755 --strip cometcfg/cometcfg $(DESTDIR)/usr/bin
	install -m 755 --strip passcheck/passcheck $(DESTDIR)/usr/bin
	install -m 755 --strip prompter/prompter $(DESTDIR)/usr/bin
	install -m 755 --strip classpath/classpath $(DESTDIR)/usr/bin


rpms: clean
	mkdir -p $(PNAME)/cometcfg
	mkdir $(PNAME)/passcheck
	mkdir $(PNAME)/prompter
	mkdir $(PNAME)/classpath
	sed '$(SED)' < Makefile > $(PNAME)/Makefile
	sed '$(SED)' < $(NAME).spec.in > $(NAME).spec
	cometdev_install_common.sh $(PNAME)/
	touch $(PNAME)/configure
	chmod 755 $(PNAME)/configure

	cometdev_sed_java.sh cometcfg $(PNAME) cpp
	cometdev_sed_java.sh passcheck $(PNAME) cpp
	cometdev_sed_java.sh prompter $(PNAME) cpp
	cometdev_sed_java.sh classpath $(PNAME) cpp
	sed '$(SED)' < cometcfg/Makefile > $(PNAME)/cometcfg/Makefile
	sed '$(SED)' < passcheck/Makefile > $(PNAME)/passcheck/Makefile
	sed '$(SED)' < prompter/Makefile > $(PNAME)/prompter/Makefile
	sed '$(SED)' < classpath/Makefile > $(PNAME)/classpath/Makefile


	cp -avpf $(PREFIX)/devel/custom/LICENSE $(PNAME)/

	
	tar -cvp $(PNAME) -f - | gzip > $(RPMTOP)/SOURCES/$(PNAME).tar.gz
	rpmbuild -ba ./$(NAME).spec
	
	mv -vf $(RPMTOP)/RPMS/$(shell arch)/$(NAME)-* .
	mv -vf $(RPMTOP)/SRPMS/$(NAME)-* .
	
gitexport: clean
	#mkdir -p $(PNAME)/cometcfg
	#mkdir $(PNAME)/passcheck
	#mkdir $(PNAME)/prompter
	#mkdir $(PNAME)/classpath
	sed '$(SED)' < Makefile > $(GITDIR)/comet_tools_rpm/Makefile
	sed '$(SED)' < $(NAME).spec.in > $(GITDIR)/comet_tools_rpm/$(NAME).spec
	cometdev_install_common.sh $(GITDIR)/comet_tools_rpm/
	#touch $(PNAME)/configure
	#chmod 755 $(PNAME)/configure

	cometdev_sed_java.sh cometcfg $(GITDIR)/comet_tools_rpm/ cpp
	cometdev_sed_java.sh passcheck $(GITDIR)/comet_tools_rpm/ cpp
	cometdev_sed_java.sh prompter $(GITDIR)/comet_tools_rpm/ cpp
	cometdev_sed_java.sh classpath $(GITDIR)/comet_tools_rpm/ cpp
	sed '$(SED)' < cometcfg/Makefile > $(GITDIR)/comet_tools_rpm/cometcfg/Makefile
	sed '$(SED)' < passcheck/Makefile > $(GITDIR)/comet_tools_rpm/passcheck/Makefile
	sed '$(SED)' < prompter/Makefile > $(GITDIR)/comet_tools_rpm/prompter/Makefile
	sed '$(SED)' < classpath/Makefile > $(GITDIR)/comet_tools_rpm/classpath/Makefile


	cp -avpf $(PREFIX)/devel/custom/LICENSE $(GITDIR)/comet_tools_rpm/

	
