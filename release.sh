#!/usr/bin/env bash
env | grep MAVEN_GPG_PASSPHRASE > /dev/null
if [ $? -eq 1 ]; then
 echo "Environment variable 'MAVEN_GPG_PASSPHRASE' is missing. Set it to empty if there is no passphrase."
 exit 1
fi
./mvnw --batch-mode clean release:prepare release:perform && git push && git push --tags
