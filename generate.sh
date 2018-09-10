#!/usr/bin/env bash

echo "Generating consolidated schema from sample json..."
genson -i 2 src/main/sample_json/server/*.json >| src/main/schemas/server.json

echo "Generate schema for Organization..."
genson -i 2 src/main/sample_json/org/*.json >| src/main/schemas/organization.json

echo "Generate schema for Images..."
genson -i 2 src/main/sample_json/images/*.json >| src/main/schemas/images.json

echo "Maven will generate pojo from schema..."
mvn clean compile