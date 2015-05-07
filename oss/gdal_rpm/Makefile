
INSTALLDIR=/opt/COMETDist/libs/


NAME=gdal
PNAME=$(NAME)-$(VER)
VER=$(shell cat SED_VERSION)
RELEASE=$(shell cat SED_RELEASE)

all:
	(cd gdalnativelib && make getgdal)
	
	make -C gdalnativelib -f Makefile
clean:

	rm -rf  $(PNAME) $(NAME).spec $(PNAME)*rpm $(NAME)-*
	
stamp:
	date +%F--%H%M%S > STAMP

deploy: compile
	mkdir -p $(DESTDIR)$(INSTALLDIR)
	mkdir -p $(DESTDIR)/usr/bin/
	
	cp -vf $(JAR) $(DESTDIR)$(INSTALLDIR)
	
	cp -avpf gdalnativelib/*so  $(DESTDIR)$(INSTALLDIR)

install: deploy
#	cp -avpf gdalnativelib/gdal $(DESTDIR)/opt/COMETDist/gdal




	mkdir -p $(DESTDIR)/usr/lib64/
	(cd $(DESTDIR)/usr/lib64/ && ln -s /opt/COMETDist/gdal/lib/libgdal.so.1) 
	
rpms_OLD: 
	mkdir -p $(PNAME)

	cp -avpf Makefile $(PNAME)/
	cp -avpf SED_RELEASE $(PNAME)/
	cp -avpf SED_VERSION $(PNAME)/

	svn export gdalnativelib $(PNAME)/gdalnativelib
	
	(cd $(PNAME)/gdalnativelib && make getgdal)
	(cd $(PNAME)/gdalnativelib && make buildgdal)
#	cp -avpf gdalnativelib/gdal $(PNAME)/gdalnativelib/
	
	#sed 's/__VERSION__/$(VER)/g;s/__RELEASE__/$(RELEASE)/g' < $(NAME).spec.in > $(NAME).spec
	cp -avpf $(NAME).spec $(PNAME)/
	touch $(PNAME)/configure
	chmod 755 $(PNAME)/configure
	tar -cvp $(PNAME) -f - | gzip > /workspace/RPM/SOURCES/$(PNAME).tar.gz
	rpmbuild -ba ./$(NAME).spec 
	mv -vf /workspace/RPM/RPMS/$(shell arch)/$(PNAME)-* .
	mv -vf /workspace/RPM/SRPMS/$(PNAME)-* .

rpmprep:
	(cd /workspace/RPM/SOURCES && wget http://download.osgeo.org/gdal/$(VER)/gdal-$(VER).tar.gz )

rpms:



#	(cd gdal-$(GDALVER) && ./configure --prefix=$(GDALDIR) && make install)


#	cp -avpf Makefile $(PNAME)/
#	cp -avpf SED_RELEASE $(PNAME)/
#	cp -avpf SED_VERSION $(PNAME)/
	
	sed 's/__VERSION__/$(VER)/g;s/__RELEASE__/$(RELEASE)/g' < $(NAME).spec.in > $(NAME).spec
	

	rpmbuild -ba ./$(NAME).spec 
	mv -vf /workspace/RPM/RPMS/$(shell arch)/$(PNAME)-* .
	mv -vf /workspace/RPM/SRPMS/$(PNAME)-* .
	
	




diff:
	svn diff > diffs.txt
	nedit diffs.txt &

