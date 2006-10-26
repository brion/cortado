Name:           cortado
Version:        0.2.2.1
Release:        0.20060831.1
Summary:        Cortado - a Java media framework

Group:          Applications/Multimedia
License:	GPL/LGPL/BSD
URL:            http://www.flumotion.net/
Source:         http://www.flumotion.net/src/cortado/%{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root

BuildRequires:	ant

%description
Cortado is a Java media framework based on GStreamer's design.

%prep
%setup -q

%build
ant

%install
rm -rf $RPM_BUILD_ROOT

ant -Dprefix=$RPM_BUILD_ROOT%{_prefix} install_class
ant -Dprefix=$RPM_BUILD_ROOT%{_prefix} -Dtype=stripped install_applet
ant -Dprefix=$RPM_BUILD_ROOT%{_prefix} -Dtype=debug install_applet

# make some symlinks so that we can have a version-independent cortado
for type in ov ovt mmjs
do
  ln -sf %{_datadir}/cortado/cortado-$type-debug-%{version}.jar \
    $RPM_BUILD_ROOT%{_datadir}/cortado/cortado-$type.jar
done

%clean
rm -rf $RPM_BUILD_ROOT

%files
%defattr(-,root,root,-)
%doc LICENSE.* HACKING README NEWS RELEASE TODO cortado.doap
%{_libdir}/cortado
%{_datadir}/cortado

%changelog
* Thu Oct 26 2006 Thomas Vander Stichele <thomas at apestaart dot org>
- add doap file

