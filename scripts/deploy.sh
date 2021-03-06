#!/usr/bin/env bash
# based on: https://dracoblue.net/dev/uploading-snapshots-and-releases-to-maven-central-with-travis/

echo $GPG_SECRET_KEYS | base64 --decode | gpg --import
echo $GPG_OWNERTRUST | base64 --decode | gpg --import-ownertrust

if [ ! -z "$TRAVIS_TAG" ]
then
    echo "on a tag -> set pom.xml <version> to ${TRAVIS_TAG}"
    mvn -s scripts/settings.xml org.codehaus.mojo:versions-maven-plugin:2.1:set -DnewVersion=${TRAVIS_TAG}
    export SETTINGS=" javadoc:jar source:jar"
else
    echo "not on a tag -> keep snapshot version in pom.xml"
fi

mvn -s scripts/settings.xml -P release ${SETTINGS} deploy -DskipTests -B -U