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
# Package: OpenSSL self-signed certification extension to COMET
# Author: Chris Delezenski <chris.delezenski@hdsfed.com>
# Compilation Date: 2015-05-06
# License: Apache License, Version 2.0
# Version: 1.21.0
# (RPM) Release: 1
# SVN: r554

PWD=$(shell pwd)
VER=$(shell cometdev_getvar.sh version)
NAME=COMET-SSL
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
	mkdir -p $(DESTDIR)$(PREFIX)/ssl
	cp -avpf server.xml $(DESTDIR)$(PREFIX)/ssl/
	cp -avpf comet_ssl_selfsigned.sh $(DESTDIR)/usr/bin/comet_ssl_selfsigned.sh
	

rpms: clean
	mkdir $(PNAME)
	sed '$(SED)' < Makefile > $(PNAME)/Makefile
	sed '$(SED)' < $(NAME).spec.in > $(NAME).spec
	cometdev_install_common.sh $(PNAME)/
	touch $(PNAME)/configure
	chmod 755 $(PNAME)/configure
	cometdev_sed.sh in $(PNAME)/
	cp -avpf server.xml $(PNAME)/

	cp -avpf $(PREFIX)/devel/custom/LICENSE $(PNAME)/

	
	tar -cvp $(PNAME) -f - | gzip > $(RPMTOP)/SOURCES/$(PNAME).tar.gz
	rpmbuild -ba ./$(NAME).spec --target noarch
	mv -vf $(RPMTOP)/RPMS/noarch/$(NAME)-* .
	mv -vf $(RPMTOP)/SRPMS/$(NAME)-* .

gitexport: clean
	mkdir $(PNAME)
	sed '$(SED)' < Makefile > $(GITDIR)/comet_ssl_rpm/Makefile
	sed '$(SED)' < $(NAME).spec.in > $(GITDIR)/comet_ssl_rpm/$(NAME).spec
	cometdev_install_common.sh $(GITDIR)/comet_ssl_rpm/
	#touch $(PNAME)/configure
	#chmod 755 $(PNAME)/configure
	cometdev_sed.sh in $(GITDIR)/comet_ssl_rpm/
	cp -avpf server.xml $(GITDIR)/comet_ssl_rpm/

	cp -avpf $(PREFIX)/devel/custom/LICENSE $(GITDIR)/comet_ssl_rpm/

