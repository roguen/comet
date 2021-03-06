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
# Package: OpenSSL self-signed certification extension to COMET
# Author: Chris Delezenski <chris.delezenski@hdsfed.com>
# Compilation Date: 2015-05-06
# License: Apache License, Version 2.0
# Version: 1.21.0
# (RPM) Release: 1
# SVN: r554
# ************************************************************/

Summary: OpenSSL self-signed certification extension to COMET
Name: COMET-SSL
Version: 1.21.0
Release: 1
License: Apache License, Version 2.0
Group: Server
Source0: %{name}-%{version}.tar.gz
BuildRoot: %{_tmppath}/%{name}-%{version}-%{release}-buildroot
Prefix:/usr
requires: COMET-Tools
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
/opt/COMETDist/ssl
/usr/bin


%post

mv /opt/COMETDist/apache-tomcat/conf/server.xml /opt/COMETDist/apache-tomcat/conf/server.xml.PRIOR
cp /opt/COMETDist/ssl/server.xml /opt/COMETDist/apache-tomcat/conf/server.xml


%changelog
* Thu May 22 2014 Chris Delezenski <chris.delezenski@hdsfed.com>
- First Release

