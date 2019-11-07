#####################
#!/bin/sh
#####################

version=$1

if [ ! -n "$version" ]; then
    echo -e "Please input the version to update: \c"
	read version
fi

echo "Update version is $version-SNAPSHOT"

mvn versions:set -DnewVersion=${version}-SNAPSHOT -DgenerateBackupPoms=false -pl ./

#mvn -f "pom.xml" versions:set -DoldVersion=* -DnewVersion=1.2.0-SNAPSHOT -DprocessAllModules=true -DallowSnapshots=true -DgenerateBackupPoms=true

#mvn versions:commit