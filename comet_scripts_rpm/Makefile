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

PWD=$(shell pwd)
VER=$(shell cometdev_getvar.sh version)
NAME=COMET-Scripts
PNAME=$(NAME)-$(VER)
SED=$(shell cometdev_getvar.sh sed)
RPMTOP=$(shell cometdev_getvar.sh rpmtop)
PREFIX=$(shell cometdev_getvar.sh prefix)
TOPDIR=$(shell cometdev_getvar.sh topdir)
GITDIR=$(shell cometdev_getvar.sh gitdir)

all:

diff:
	svn diff > diffs.txt
	nedit diffs.txt &

clean: 
	rm -rf $(OBJS) core *.*~ *.bak $(PNAME) hcp_shell_api.sh $(NAME).spec $(PNAME)*rpm $(NAME)-*
	

install: 
	mkdir -p $(DESTDIR)/usr/bin
	mkdir -p $(DESTDIR)$(PREFIX)/include
	mkdir -p $(DESTDIR)/etc/rc.d/init.d
	
	install -m 755 scripts/* $(DESTDIR)/usr/bin/
	mv -vf $(DESTDIR)/usr/bin/hcp_shell_api.sh $(DESTDIR)$(PREFIX)/include/
	mv -vf $(DESTDIR)/usr/bin/comet_init.sh $(DESTDIR)/etc/rc.d/init.d/comet
	mv -vf $(DESTDIR)/usr/bin/tomcat_init.sh $(DESTDIR)/etc/rc.d/init.d/tomcat
	
	cp -avpf index*jsp $(DESTDIR)$(PREFIX)
	cp -avpf banner.html $(DESTDIR)$(PREFIX)
	cp -avpf roles.json $(DESTDIR)$(PREFIX)
	
	cp comet.properties.NEW $(DESTDIR)$(PREFIX)
	mkdir -p $(DESTDIR)$(PREFIX)/patches/
	mkdir -p $(DESTDIR)/var/log/comet/
	cp -avpf logging.properties.patch.gz $(DESTDIR)$(PREFIX)/patches/	
	cp -avpf catalina.policy.patch.gz $(DESTDIR)$(PREFIX)/patches/	
	cp -avpf apache-tomcat-bin.patch.gz $(DESTDIR)$(PREFIX)/patches/	

	mkdir -p $(DESTDIR)/etc/logrotate.d/
	mv tomcat.logrotate $(DESTDIR)/etc/logrotate.d/tomcat
	

rpms: clean
	mkdir $(PNAME)
	sed '$(SED)' < Makefile > $(PNAME)/Makefile
	sed '$(SED)' < $(NAME).spec.in > $(NAME).spec
	cometdev_install_common.sh $(PNAME)/
	touch $(PNAME)/configure
	chmod 755 $(PNAME)/configure
	


	sed '$(SED)' < comet.properties.in >$(PNAME)/comet.properties.NEW
	sed '$(SED)' < index_orig.jsp > $(PNAME)/index_orig.jsp
	sed '$(SED)' < index_wb.jsp > $(PNAME)/index_wb.jsp
	sed '$(SED)' < banner.html > $(PNAME)/banner.html

	cp -avpf tomcat.logrotate $(PNAME)/
	cp -avpf roles.json $(PNAME)/
	cp -avpf logging.properties.patch.gz $(PNAME)/
	cp -avpf catalina.policy.patch.gz $(PNAME)/
	cp -avpf apache-tomcat-bin.patch.gz $(PNAME)/	
	
	cp -avpf $(PREFIX)/devel/custom/LICENSE $(PNAME)/
	
	
	mkdir -p $(PNAME)/scripts
	cometdev_sed.sh in $(PNAME)/scripts
	
	tar -cvp $(PNAME) -f - | gzip > $(RPMTOP)/SOURCES/$(PNAME).tar.gz
	rpmbuild -ba ./$(NAME).spec --target noarch
	mv -vf $(RPMTOP)/RPMS/noarch/$(NAME)-* .
	mv -vf $(RPMTOP)/SRPMS/$(NAME)-* .
gitexport: clean
	
	sed '$(SED)' < Makefile > $(GITDIR)/comet_scripts_rpm/Makefile
	sed '$(SED)' < $(NAME).spec.in > $(GITDIR)/comet_scripts_rpm/$(NAME).spec
	cometdev_install_common.sh $(GITDIR)/comet_scripts_rpm/
#	touch $(PNAME)/configure
#	chmod 755 $(PNAME)/configure
	


	sed '$(SED)' < comet.properties.in >$(GITDIR)/comet_scripts_rpm/comet.properties.NEW
	sed '$(SED)' < index_orig.jsp > $(GITDIR)/comet_scripts_rpm/index_orig.jsp
	sed '$(SED)' < index_wb.jsp > $(GITDIR)/comet_scripts_rpm/index_wb.jsp
	sed '$(SED)' < banner.html > $(GITDIR)/comet_scripts_rpm/banner.html

	cp -avpf tomcat.logrotate $(GITDIR)/comet_scripts_rpm/
	cp -avpf roles.json $(GITDIR)/comet_scripts_rpm/
	cp -avpf logging.properties.patch.gz $(GITDIR)/comet_scripts_rpm/
	cp -avpf catalina.policy.patch.gz $(GITDIR)/comet_scripts_rpm/
	cp -avpf apache-tomcat-bin.patch.gz $(GITDIR)/comet_scripts_rpm/	
	
	cp -avpf $(PREFIX)/devel/custom/LICENSE $(GITDIR)/comet_scripts_rpm/
	
	
	mkdir -p $(GITDIR)/comet_scripts_rpm/scripts
	cometdev_sed.sh in $(GITDIR)/comet_scripts_rpm/scripts
	
