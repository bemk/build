#!/bin/bash

if [ "$1" == "clean" ]; then
	rm -rf ./deb ./*.deb ./build
	exit
fi

echo "Creating the binary..."

cat <(echo -e "#!/bin/sh\nMYSELF=\`which \"\$0\" 2>/dev/null\`\n[ \$? -gt 0 -a -f \"\$0\" ] && MYSELF=\"./\$0\"\njava=java\nif test -n \"\$JAVA_HOME\"; then\n    java=\"\$JAVA_HOME/bin/java\"\nfi\nexec \"\$java\" \$java_args -jar \$MYSELF \"\$@\"\nexit 1\n") ./target/build.jar > build
chmod +x ./build

#awk '{ if($1=="version.build") print $1 " " $2 " " ($3+1); else print $0; }' ./src/main/resources/version.properties > ./src/main/resources/version.properties.tmp && mv ./src/main/resources/version.properties.tmp ./src/main/resources/version.properties

echo "Creating debian package..."

mkdir -p ./deb
cd ./deb

# ------------------------------------------------
# --- Move the files to their install location ---
# ------------------------------------------------
mkdir -p ./usr/bin
cp ../build ./usr/bin


# Further install steps

mkdir -p ./DEBIAN
find . -type d | xargs chmod 755 # this is necessary on Debian Woody, don't ask me why

PACKAGENAME="Build"
VERSION="$1.$2-$3"
HOMEPAGE="http://github.com/bemk/build/"
SECTION="admin"
PRIORITY="optional"
ARCH="all" # May want to change this...
DEPENDS="dash (>= 0.5.7-3~)" # Add java dependicy
SUGGESTS="gcc (>= 4.7.3-1~), clang-3.2 (>= 3.2-1~)"
CONFLICTS=""
REPLACES=""
SIZE=$(( (`ls -nl ../build | awk '{print $5}'` + 1023) / 1024))
MAINTAINER="Bart Kuivenhoven <bemkuivenhoven@gmail.com>"
DESCRIPTION="A new bulid system..."
LICENSE=$(awk  '{ print " " $0 }' ../gpl-2.0.txt | awk '{ if (NF < 1) print " ."; else print $0; }')

echo -e "Package: $PACKAGENAME\nVersion: $VERSION\nHomepage: $HOMEPAGE\nSection: $SECTION\nPriority: $PRIORITY\nArchitecture: $ARCH\nDepends: $DEPENDS\nSuggests: $SUGGESTS\nConflicts: $CONFLICS\nReplaces: $REPLACES\nInstalled-Size: $SIZE\nMaintainer: $MAINTAINER\nDescription: $DESCRIPTION\nLicense: GPL-2+\n$LICENSE" > ./DEBIAN/control

find . -type f ! -regex '.*.hg.*' ! -regex '.*?debian-binary.*' ! -regex '.*?DEBIAN.*' -printf '%P ' | xargs md5sum > ./DEBIAN/md5sums

cd ..

fakeroot dpkg-deb --build deb
mv deb.deb build_$VERSION.deb

rm -rf ./deb
