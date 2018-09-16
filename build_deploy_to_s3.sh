#!/usr/bin/env bash

# build the image
docker build -t rpardini/scaleway-retrofit-client:latest .

# use the built image to deploy, using a volume to map local ~/.m2/settings.xml with s3 credentials
docker run --rm --name scaleway-retrofit-client-throw-away -it --volume ~/.m2/settings.xml:/root/.m2/settings.xml rpardini/scaleway-retrofit-client:latest

# throw away the image
docker image rm --force rpardini/scaleway-retrofit-client:latest

