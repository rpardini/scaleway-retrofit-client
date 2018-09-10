#!/usr/bin/env bash

echo "Generating consolidated schema from sample json..."
genson -i 2 src/main/resources/sample_json/server/*.json >| src/main/resources/schema/server.json

echo "Generate schema for Organization..."
genson -i 2 src/main/resources/sample_json/org/*.json >| src/main/resources/schema/organization.json

echo "Maven will generate pojo from schema..."
mvn clean compile