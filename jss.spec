Name:           jss
Version:        4.2.4
Release:        alt0
Summary:        Java Security Services (JSS)

Group:          Development/Java
License:        MPL/GPL/LGPL
URL:            http://www.mozilla.org/projects/security/pki/jss/
Source0:        %{name}-%{version}.tar.gz
Source1:        MPL-1.1.txt
Source2:        gpl.txt
Source3:        lgpl.txt

BuildRequires:  libnss-devel >= 3.11.4
BuildRequires:  libnspr-devel >= 4.6.4
BuildRequires:  java-devel < 0:1.5.0
BuildRequires:  perl
Requires:       java

# don't use sun.net.www.protocol.http.HttpURLConnection.userAgent
Patch1:         jss-useragent.patch

%description
Java Security Services (JSS) is a java native interface which provides a bridge
for java-based applications to use native Network Security Services (NSS).
This only works with gcj. Other JREs require that JCE providers be signed.


%prep
%setup -q
%patch1 -p1

%build
export JAVA_HOME=/usr/lib/jvm/java-1.4.2

# Generate symbolic info for debuggers
XCFLAGS="-g $RPM_OPT_FLAGS"
export XCFLAGS

PKG_CONFIG_ALLOW_SYSTEM_LIBS=1
PKG_CONFIG_ALLOW_SYSTEM_CFLAGS=1

export PKG_CONFIG_ALLOW_SYSTEM_LIBS
export PKG_CONFIG_ALLOW_SYSTEM_CFLAGS

NSPR_INCLUDE_DIR=`/usr/bin/pkg-config --cflags-only-I nspr | sed 's/-I//'`
NSPR_LIB_DIR=`/usr/bin/pkg-config --libs-only-L nspr | sed 's/-L//'`

NSS_INCLUDE_DIR=`/usr/bin/pkg-config --cflags-only-I nss | sed 's/-I//'`
NSS_LIB_DIR=`/usr/bin/pkg-config --libs-only-L nss | sed 's/-L//'`

export NSPR_INCLUDE_DIR
export NSPR_LIB_DIR
export NSS_INCLUDE_DIR
export NSS_LIB_DIR

%ifarch x86_64 ppc64 ia64 s390x sparc64
USE_64=1
export USE_64
%endif

# The Makefile is not thread-safe
make -C mozilla/security/coreconf
make -C mozilla/security/jss

%install
rm -rf $RPM_BUILD_ROOT docdir

# Copy the license files here so we can include them in %doc
cp -p %{SOURCE1} .
cp -p %{SOURCE2} .
cp -p %{SOURCE3} .

# There is no install target so we'll do it by hand

# jars
install -d -m 0755 $RPM_BUILD_ROOT/usr/share/java/
install -m 644 mozilla/dist/xpclass_dbg.jar ${RPM_BUILD_ROOT}/usr/share/java/jss4-%{version}.jar
pushd  $RPM_BUILD_ROOT/usr/share/java
    ln -fs jss4-%{version}.jar jss4.jar
popd

# We have to use the name libjss4.so because this is dynamically
# loaded by the jar file.
install -d -m 0755 $RPM_BUILD_ROOT%{_libdir}
install -m 0755 mozilla/dist/Linux*.OBJ/lib/libjss4.so ${RPM_BUILD_ROOT}%{_libdir}/

# FIXME - sign jss4.jar. In order to use JSS as a JCE provider it needs to be
# signed with a Sun-issued certificate. Since we would need to make this
# certificate and private key public to provide reproducability in the rpm
# building we have to ship an unsigned jar.
#
# Instructions for getting a signing cert can be found here:
# http://java.sun.com/javase/6/docs/technotes/guides/security/crypto/HowToImplAProvider.html#Step61
#
# This signing is not required by every JVM. gcj ignores the signature and does
# not require one. The Sun and IBM JVMs both check and enforce the signature.
# Behavior of other JVMs is not known but they probably enforce the signature
# as well.

%clean
rm -rf $RPM_BUILD_ROOT

# No ldconfig is required since this library is loaded by Java itself.
%files
%defattr(-,root,root,-)
%doc mozilla/security/jss/jss.html MPL-1.1.txt gpl.txt lgpl.txt
/usr/share/java/*
%{_libdir}/lib*.so


%changelog
* Thu May 31 2007 Vitaly Kuznetsov <vitty@altlinux.ru> 4.2.4-alt0
- Initial for Sisyphus

* Wed May 16 2007 Rob Crittenden <rcritten@redhat.com> 4.2.4-5
- Include the 3 license files
- Remove Requires for nss and nspr. These libraries have versioned symbols
  so BuildRequires is enough to set the minimum.
- Add sparc64 for the 64-bit list

* Mon May 14 2007 Rob Crittenden <rcritten@redhat.com> 4.2.4-4
- Included additional comments on jar signing and why ldconfig is not
  required.

* Thu May 10 2007 Rob Crittenden <rcritten@redhat.com> 4.2.4-3
- Added information on how to pull the source into a tar.gz

* Thu Mar 15 2007  Rob Crittenden <rcritten@redhat.com> 4.2.4-2
- Added RPM_OPT_FLAGS to XCFLAGS

- Added link to Sun JCE information
* Mon Feb 27 2007 Rob Crittenden <rcritten@redhat.com> 4.2.4-1
- Initial build
