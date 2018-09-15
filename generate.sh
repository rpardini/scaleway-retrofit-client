#!/usr/bin/env bash

# This is called by Dockerfile. It depends on genson and a node transform program.

ORIG=$(pwd)
cd src/main/sample_json
for schemaType in *; do
  if [ -d ${schemaType} ]; then
    echo "Schema type $schemaType ..."
    echo "Generating consolidated schema from sample json..."
    genson -i 2 ${schemaType}/*.json >| ${ORIG}/src/main/schemas/${schemaType}.json
    echo "Running schema transformer for schema ${schemaType}..."
    node ${ORIG}/schema_transform.js ${ORIG}/src/main/schemas/${schemaType}.json 
  fi
done
cd "$ORIG" 
