#***************************************************************
# * (C) Copyright 1998-2014 Chris Delezenski <cmd184psu@gmail.com>
# * This software is released under the LGPL v2
# ***************************************************************/

NAME=OpenString
CXXFLAGS=-g -Werror -Wall 
CC=g++
LIBRARY=lib$(NAME).a
SRCS=$(wildcard *.cpp)
OBJS=$(subst .cpp,.o,$(SRCS))
HDRS=master.h dynstring.h stringlist.h vectorlist.h  duallist.h   fileunit.h filelist.h timeanddate.h executive.h keyboard.h phptranslation.h
SPEC=$(NAME).spec
ARCH=$(shell ./myarch.sh)
RPMTOP=$(shell cat RPMTOP)
VERSION=$(shell cat SED_VERSION)
PNAME=$(NAME)Library-$(VERSION)
RELEASE=$(shell cat SED_RELEASE)
LICENSE=$(shell cat SED_LICENSE)

all: makehead $(LIBRARY) 


$(LIBRARY): $(OBJS) 
	ar rcs $(LIBRARY) $(OBJS)

makehead:
	cat $(HDRS) > $(NAME)Lib.h
	echo "#endif" >>$(NAME)Lib.h

clean: 
	rm -rf $(LIBRARY) *.o core  $(PNAME)-* $(NAME)*rpm $(NAME)*spec  $(NAME)Library-*

deploy: all
	
	cp $(LIBRARY) /usr/$(shell ./libarch.sh)/
	cp $(NAME)Lib.h /usr/include/
	cp mysqlpp.h /usr/include/
	cp curlpp.h /usr/include/
	/sbin/ldconfig

install:
	mkdir -p $(DESTDIR)/usr/$(shell ./libarch.sh)/
	mkdir -p $(DESTDIR)/usr/include/
	
	cp $(LIBRARY) $(DESTDIR)/usr/$(shell ./libarch.sh)/
	cp $(NAME)Lib.h $(DESTDIR)/usr/include/
	cp mysqlpp.h $(DESTDIR)/usr/include/
	cp curlpp.h $(DESTDIR)/usr/include/


$(OBJS): $(HDRS)

diffs: 
	svn diff >diffs.txt
	nedit diffs.txt&
rpm:
	mkdir -p $(PNAME)/
	cp -avpf *.cpp $(PNAME)/
	cp -avpf *.h $(PNAME)/
	cp -avpf *.sh $(PNAME)/
	cp Makefile $(PNAME)/
	
	cp -avpf SED_AUTHOR $(PNAME)/
	cp -avpf SED_LICENSE $(PNAME)/
	cp -avpf SED_VERSION $(PNAME)/
	
	touch $(PNAME)/configure
	chmod 755 $(PNAME)/configure
	
	sed 's/__VERSION__/$(VERSION)/g;s/__RELEASE__/$(RELEASE)/g;s/__LICENSE__/$(LICENSE)/g' < $(NAME)Library.spec.in > $(NAME)Library.spec

	
#		sed 's/__VERSION__/$(VER)/g;s/__RELEASE__/$(RELEASE)/g' < $(NAME).spec.in > $(NAME).spec

	
	
	tar -cvp $(PNAME) -f - | gzip > $(RPMTOP)/SOURCES/$(PNAME).tar.gz
	rpmbuild -ba $(NAME)Library.spec 
	mv -vf $(RPMTOP)/RPMS/$(shell arch)/$(NAME)Library-* .
	mv -vf $(RPMTOP)/SRPMS/$(NAME)Library-* .
	
	
