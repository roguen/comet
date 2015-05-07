#/************************************************************
# * File Name:  COMET-MetaCatalog.spec
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
# Package: COMET Web Application
# Author: Chris Delezenski <chris.delezenski@hdsfed.com>
# Compilation Date: 2015-05-06
# License: Apache License, Version 2.0
# Version: 1.21.0
# (RPM) Release: 1
# SVN: r554
# ************************************************************/

Summary: Custom Object Metadata Enhancement Toolkit UI
Name: COMET-MetaCatalog
Version: 1.21.0
Release: 1
License: Apache License, Version 2.0
Group: Web Application
Source0: %{name}-%{version}.tar.gz
BuildRoot: %{_tmppath}/%{name}-%{version}-%{release}-buildroot
Prefix:/usr
BuildRequires: COMET-devel
BuildRequires: ant
BuildRequires:  COMET-HDSCometHLApi
requires: COMET-Tools
requires: apache-tomcat
requires:  COMET-HDSCometHLApi
requires: COMET-Scripts
requires: jre
requires: COMET-Ingestor
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
%defattr(-,root,root,-)
/opt/COMETDist/*


%post
if [ -e /opt/COMETDist/apache-tomcat/webapps/ROOT.war ]; then
	mkdir -p /opt/COMETDist/previous
	mv -vf /opt/COMETDist/apache-tomcat/webapps/ROOT.war `mktemp -u /opt/COMETDist/previous/ROOT-XXXXXX.war`
fi

comet_libcheck.sh


mv -vf /opt/COMETDist/ondeck/ROOT.war /opt/COMETDist/apache-tomcat/webapps/


%changelog
* Fri Jan 2 2015 Chris Delezenski <chris.delezenski@hdsfed.com>
- added lib check

* Sat May 3 2014 Chris Delezenski <chris.delezenski@hdsfed.com>
- First Release

