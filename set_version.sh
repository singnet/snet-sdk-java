#!/bin/sh

if [ "$#" -ne 1 ]; then
    echo "usage: ./set_version.sh <version>"
    exit 1
fi

VERSION=$1
JAVADOC_VERSION=`echo ${VERSION} | sed 's/-/--/g'`

mvn versions:set -DnewVersion=${VERSION} -DgenerateBackupPoms=false

find * -type f -exec sed -i -E 's~(<snet.sdk.java.version>)[^<]+(</snet.sdk.java.version>)~\1'${VERSION}'\2~' {} \;
find * -type f -exec sed -i -E 's/(snetSdkJavaVersion\=).+/\1'${VERSION}'/' {} \;
sed -i -E 's/(javadoc-).+(-brightgreen)/\1'${JAVADOC_VERSION}'\2/' ./README.md
sed -i -E 's~(snet-sdk-java/snet-sdk-java/).+(/javadoc)~\1'${VERSION}'\2~' ./README.md


