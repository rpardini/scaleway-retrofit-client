# Use a temporary state to build the python+node stuff first.
FROM node:10-alpine as pythonNodePhase
WORKDIR /src
# Add python pip
RUN apk add --update python py-pip bash
RUN pip install genson

# Add only the minimal to allow yarn install to run sucessfully
ADD package.json yarn.lock /src/
RUN yarn install

# Add the src dir and the generator, and run it.
ADD src /src/src
ADD schema_transform.js /src/
ADD generate.sh /src/
RUN chmod +x /src/generate.sh
RUN /src/generate.sh

# Now the java/maven build.
FROM maven:3-jdk-8
WORKDIR /src/
# Add only pom.xml; its unlikely to change so we maximize cache hits for the maven repo
ADD pom.xml /src/
RUN set -ex \
 && mvn --batch-mode --show-version --define 'maven.repo.local=/mvn/repo' --define 'altDeploymentRepository=local::default::file:///mvn/lib' dependency:list-repositories dependency:go-offline 

# Now add the processed/generated sources from the previous python+node stage...
COPY --from=pythonNodePhase /src/src /src/src

# And finally run maven, using the previously resolved dependencies; we compile and install locally first...
RUN set -ex && mvn --batch-mode --show-version --define 'maven.repo.local=/mvn/repo' --define 'altDeploymentRepository=local::default::file:///mvn/lib' install

# This is the command to deploy to s3, which will be run by "docker run" later.
VOLUME /root/.m2/settings.xml
CMD mvn --batch-mode --show-version --define 'maven.repo.local=/mvn/repo' --define 'altDeploymentRepository=s3.release::default::s3://ceapmaven-prod-repo/release' deploy