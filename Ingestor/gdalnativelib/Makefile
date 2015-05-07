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
PWD=$(shell pwd)
VER=$(shell cometdev_getvar.sh version)
#NAME=COMET-devel
#PNAME=$(NAME)-$(VER)
SED=$(shell cometdev_getvar.sh sed)
RPMTOP=$(shell cometdev_getvar.sh rpmtop)
PREFIX=$(shell cometdev_getvar.sh prefix)

NAME=libgdalnative
LIB=$(NAME).so
JDK=latest
#JDK=jdk1.6.0_37

#CFLAGS=-g -Werror -Wall -I/usr/java/$(JDK)/include -I/usr/java/$(JDK)/include/linux -I/opt/COMETDist/MrSIDGdal/include -I/usr/include/libxml2 -fPIC
CFLAGS=-g -Werror -Wall -I/usr/java/$(JDK)/include -I/usr/java/$(JDK)/include/linux  -I/usr/include/libxml2 -I/usr/include -fPIC
CC=gcc
SRCS=$(wildcard *.c)
OBJS=$(subst .c,.o,$(SRCS))
GDALDIR=$(shell pwd)/gdal


#LIBS := -lxml2 
LIBS := -lgdal -lxml2 

#/usr/lib64/libgdal.so
#/usr/lib64/libgdalconstjni.so
#/usr/lib64/libgdaljni.so
#/usr/lib64/libogrjni.so
#/usr/lib64/libosrjni.so


GDALVER=1.10.1
all: $(LIB)

#getgdal:
#	wget http://download.osgeo.org/gdal/$(GDALVER)/gdal-$(GDALVER).tar.gz

#buildgdal:
#	gunzip < gdal-$(GDALVER).tar.gz | tar -xvpf -
#	mkdir gdal
#	echo $(GDALDIR)
#	(cd gdal-$(GDALVER) && ./configure --prefix=$(GDALDIR) && make install)



# Tool invocations
libgdalnative.so: $(OBJS) 
	@echo 'Building target: $@'
	@echo 'Invoking: GCC C Linker'
#	gcc -L /opt/COMETDist/MrSIDGdal/lib -lgdalmrsid -L/usr/lib64 $(LIBS) -shared -o "libgdalnative-mrsid.so" $(OBJS) 
	gcc -L /usr/lib64 $(LIBS) -shared -o "libgdalnative.so" $(OBJS) 

# Other Targets
clean:
	rm -rf $(OBJS) $(LIB)

install: all
	mkdir -p $(DESTDIR)/usr/lib64/
	cp -avpf $(LIB) $(DESTDIR)/usr/lib64/
#	cp -avpf $(LIB) ../dist/
#	cp -avpf $(LIB) ../Ingestor/libs/
	
	
