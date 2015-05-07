#/************************************************************
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
# SVN: r551+
# ************************************************************/

Summary: Custom Object Metadata Enhancement Toolkit run-time scripts
Name: COMET-Scripts
Version: 1.21.0
Release: 1
License: Apache License, Version 2.0
Group: Development/Tools
Source0: %{name}-%{version}.tar.gz
BuildRoot: %{_tmppath}/%{name}-%{version}-%{release}-buildroot
Prefix:/usr
requires: COMET-Tools
requires: apache-tomcat
BuildRequires: COMET-devel
%description

%prep
%setup -q

%build
#we do not use autoconf
#%configure
make

%install
rm -rf $RPM_BUILD_ROOT
make DESTDIR="$RPM_BUILD_ROOT" install

%clean
rm -rf $RPM_BUILD_ROOT

%files
%defattr(755,root,root,-)
%{_bindir}/*
/opt/COMETDist
/etc/rc.d/init.d
/etc/logrotate.d
/var/log/comet

%post

(cd /opt/COMETDist/ && rm -f catalina.out)
(cd /opt/COMETDist/ && ln -s /var/log/comet/catalina.out)

if [ -e /opt/COMETDist/comet.properties ]; then
	cp /opt/COMETDist/comet.properties /opt/COMETDist/comet.properties.prev
else
	mv /opt/COMETDist/comet.properties.NEW /opt/COMETDist/comet.properties
fi


%changelog
* Mon Dec 15 2014 Chris Delezenski <chris.delezenski@hdsfed.com>
- fixed symlink conflict

* Sat May 3 2014 Chris Delezenski <chris.delezenski@hdsfed.com>
- First Release

