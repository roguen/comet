# __COPYRIGHT__
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
# Package: __DESCRIPTION__
# Author: __AUTHOR__
# Compilation Date: __DATE__
# License: __LICENSE__
# Version: __VERSION__
# (RPM) Release: __RELEASE__
# SVN: r__SVN__


PWD=$(shell pwd)
VER=$(shell cometdev_getvar.sh version)
NAME=tomcat-native
PNAME=$(NAME)-$(VER)
SED=$(shell cometdev_getvar.sh sed)
RPMTOP=$(shell cometdev_getvar.sh rpmtop)
PREFIX=$(shell cometdev_getvar.sh prefix)

all:
	echo "nothing to do"
wget:
	wget http://mirrors.sonic.net/apache/tomcat/tomcat-connectors/native/$(VER)/source/tomcat-native-$(VER)-src.tar.gz
clean: 
	rm -rf $(NAME) $(PNAME) *rpm $(NAME).spec
diffs: clean
	svn diff >diffs.txt
	nedit diffs.txt&

install: 
	mkdir -p $(DESTDIR)$(PREFIX)/$(PNAME)
#	(cd $(DESTDIR)$(PREFIX) && gunzip < $(PWD)/$(PNAME).tar.gz | tar -xvpf -)
	(cd $(DESTDIR)$(PREFIX) && ln -s $(PNAME) $(NAME))
	cp -avpf $(PWD)/* $(DESTDIR)$(PREFIX)/$(PNAME)/
	#(cd $(DESTDIR)$(PREFIX) && rm -rvf $(NAME)/webapps/*)

rpmprep: clean
	#mkdir -p $(PNAME)
	#cp $(PNAME).tar.gz $(PNAME)/
	gunzip < $(PNAME)-src.tar.gz | tar -xvpf -
	
	mv -vf $(PNAME)-src $(PNAME)
	
	sed '$(SED)' < Makefile > $(PNAME)/Makefile
	sed '$(SED)' < $(NAME).spec.in > $(NAME).spec
	cometdev_install_common.sh $(PNAME)/
	touch $(PNAME)/configure
	chmod 755 $(PNAME)/configure

rpms:  rpmprep
	tar -cvp $(PNAME) -f - | gzip > $(RPMTOP)/SOURCES/$(PNAME).tar.gz
	rpmbuild -ba ./$(NAME).spec --target noarch
	mv -vf $(RPMTOP)/RPMS/noarch/$(NAME)-* .
	mv -vf $(RPMTOP)/SRPMS/$(NAME)-* .

