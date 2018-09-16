# Scaleway API client using Retrofit and JSON schemas

This is a very simple Scaleway API implementation using

- JSON Schema generation (via `genson` and a custom transformer using `@cloudflare/json-schema-walker`)
- Retrofit2 and OkHttp
- Streams, Optional<T> and Java 8 niceties

It does not aim to be complete.

## Quirks

It does aim to remove the idiosyncrasies from Scaleway API implementation:

- commercialTypes change all the time
- commercialTypes have associated architecture, so directly related to OS images
- commercialTypes have exact disk/volume requirements (eg START1-L needs an extra 150gb volume to start)
- there's a shitload of undocumented behaviour, fields, and even whole operations (...tasks...) on Scaleway API
- date formats and field nomenclature are not consistent
- SW does not offer a OpenAPI, Swagger, or JSON schema anywhere, so specially enums have to handcrafted

## It assumes a lot:

- Even though SW api allows it, we try to use the server name as key (in a given region)
- There's a single organization associated to the Scaleway token
- Mostly focused on `START1` commercialTypes, instead of baremetal, etc. These can work, but have not been tested.
- bootType = LOCAL (instead of bootscript)
- cloud-init will be used to provision the servers


## TODO:

- support for deleting/powering off servers
- support for ipv6-enabled servers
- Dockerfile that allows isolated building, with `genson` and `node/yarn` and `maven`.


## Building

Check out `build_deploy_to_s3.sh`: it builds a throw-away Docker image to make the Python/Node/Maven building easier.
The docker image is then used to run `mvn deploy` with the mapped maven settings file, so it has the credentials to S3.

