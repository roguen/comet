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

PWD=$(shell pwd)
VER=$(shell cometdev_getvar.sh version)
NAME=COMET-devel
PNAME=$(NAME)-$(VER)
SED=$(shell cometdev_getvar.sh sed)
RPMTOP=$(shell cometdev_getvar.sh rpmtop)
PREFIX=$(shell cometdev_getvar.sh prefix)
GITDIR=$(shell cometdev_getvar.sh gitdir)

clean: 
	rm -rf $(PNAME) $(NAME).spec $(PNAME)*rpm $(NAME)-* scripts
diff: clean
	svn diff > diffs.txt
	nedit diffs.txt &


install: 
	mkdir -p $(DESTDIR)/usr/bin
	mkdir -p $(DESTDIR)$(PREFIX)/devel/templates
	install -m 755 *.sh $(DESTDIR)/usr/bin/
	
	cp -avpf *template* $(DESTDIR)$(PREFIX)/devel/templates/
	cp -avpf custom $(DESTDIR)$(PREFIX)/devel/
	cp -avpf ver $(DESTDIR)$(PREFIX)/devel/
	cp -avpf doc $(DESTDIR)$(PREFIX)/devel/
	cp -avpf Makefile.master $(DESTDIR)$(PREFIX)/devel/Makefile

prime:
	# need to prime the pump since this package relies on its own scripts
	sudo cp -avpf in/cometdev_sed.sh.in /usr/bin/cometdev_sed.sh
	sudo chmod 755 /usr/bin/cometdev_sed.sh
	sudo cp -avpf in/cometdev_getvar.sh.in /usr/bin/cometdev_getvar.sh
	sudo chmod 755 /usr/bin/cometdev_getvar.sh


deploy: prime
	mkdir scripts
	cometdev_sed.sh in scripts
	sudo install -m 755 scripts/* /usr/bin/


rpms: clean
	#begin standard prep
	mkdir -p $(PNAME)
	sed '$(SED)' < Makefile > $(PNAME)/Makefile
	sed '$(SED)' < Makefile.master > $(PNAME)/Makefile.master
	sed '$(SED)' < $(NAME).spec.in > $(NAME).spec
	cometdev_install_common.sh $(PNAME)/
	touch $(PNAME)/configure
	chmod 755 $(PNAME)/configure
	#end standard prep
	
	#specific to this RPM
	cometdev_sed.sh in $(PNAME)

	cp -avpf custom/LICENSE $(PNAME)/


	cp -avpf *template* $(PNAME)/
	svn export custom $(PNAME)/custom
	svn export ver $(PNAME)/ver
	svn export doc $(PNAME)/doc


	tar -cvp $(PNAME) -f - | gzip > $(RPMTOP)/SOURCES/$(PNAME).tar.gz
	rpmbuild -ba ./$(NAME).spec --target noarch
	mv -vf $(RPMTOP)/RPMS/noarch/$(NAME)-* .
	mv -vf $(RPMTOP)/SRPMS/$(NAME)-* .

gitexport:
	sed '$(SED)' < Makefile > $(GITDIR)/comet_devel_rpm/Makefile
	sed '$(SED)' < $(NAME).spec.in > $(GITDIR)/comet_devel_rpm/$(NAME).spec
	cometdev_install_common.sh $(GITDIR)/comet_devel_rpm/
	cometdev_sed.sh in $(GITDIR)/comet_devel_rpm/
	cp -avpf custom/LICENSE $(GITDIR)/comet_devel_rpm/
	cp -avpf *template* $(GITDIR)/comet_devel_rpm/
	#svn export custom $(GITDIR)/comet_devel_rpm/custom
	#svn export ver $(GITDIR)/comet_devel_rpm/ver
	#svn export doc $(GITDIR)/comet_devel_rpm/doc

unlink:
	sudo rm -rf /usr/bin/cometdev_antbuild.sh
	sudo rm -rf /usr/bin/cometdev_sed.sh
	sudo rm -rf /usr/bin/cometdev_sed_java.sh
	sudo rm -rf /usr/bin/cometdev_unrpm.sh
	sudo rm -rf /usr/bin/cometdev_getvar.sh
	sudo rm -rf /usr/bin/cometdev_install_common.sh

link:
	(cd /usr/bin && sudo ln -s $(PWD)/in/cometdev_antbuild.sh.in cometdev_antbuild.sh)
	(cd /usr/bin && sudo ln -s $(PWD)/in/cometdev_sed.sh.in cometdev_sed.sh)
	(cd /usr/bin && sudo ln -s $(PWD)/in/cometdev_sed_java.sh.in cometdev_sed_java.sh)
	(cd /usr/bin && sudo ln -s $(PWD)/in/cometdev_unrpm.sh.in cometdev_unrpm.sh)
	(cd /usr/bin && sudo ln -s $(PWD)/in/cometdev_getvar.sh.in cometdev_getvar.sh)
	(cd /usr/bin && sudo ln -s $(PWD)/in/cometdev_install_common.sh.in cometdev_install_common.sh)
	

