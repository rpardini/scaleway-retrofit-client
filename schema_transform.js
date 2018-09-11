const walker = require("@cloudflare/json-schema-walker");
const schemaFile = process.argv[2];
console.log("Transforming schema", schemaFile);
const fs = require('fs');
var json = JSON.parse(fs.readFileSync(schemaFile, 'utf8'));

function getPossibleCommercialTypes() {
    // @TODO: read directly from: https://cp-ams1.scaleway.com/products/servers/availability
    // also possibly from https://cp-par1.scaleway.com/products/servers/availability
    let json = JSON.parse(fs.readFileSync("../../../src/main/sample_json/commercial_types.json", 'utf8'));
    let validTypes = [];
    for (let oneType in json.servers) {
        validTypes.push(oneType);
    }
    return validTypes;
}


/*
The schema object (or boolean) being processed
The path to the schema from its parent
The parent schema object, if any
The path from the schema document root to the parent
 */
function raiosFunc (schemaObjOrBoolean, pathToSchemaFromParent, parentSchemaObjectIfAny, pathFromSchemaToParent) {

    // Hack all "simple" fields with name ending in '_date' to string with a date-time format.
    
    if ( (pathToSchemaFromParent[0] === "properties") && ( (pathToSchemaFromParent[1].endsWith("_date")) || (pathToSchemaFromParent[1].endsWith("_at"))) ) {
        console.log("Found a date, I think....", pathToSchemaFromParent);
        schemaObjOrBoolean["type"] = "string";
        schemaObjOrBoolean["format"] ="date-time";
    }
    
    if ( (pathToSchemaFromParent[0] === "properties") && (pathToSchemaFromParent[1] === "size") ) {
        console.log("Found a size, I think....", pathToSchemaFromParent);
        schemaObjOrBoolean["type"] = "number";
    }

    if ( (pathToSchemaFromParent[0] === "properties") && (pathToSchemaFromParent[1] === "commercial_type") ) {
        console.log("Found a commercial_type, I think....", pathToSchemaFromParent);
        schemaObjOrBoolean["enum"] =getPossibleCommercialTypes();
    }

    if ( (pathToSchemaFromParent[0] === "properties") && (pathToSchemaFromParent[1] === "action") ) {
        console.log("Found a action, I think....", pathToSchemaFromParent);
        schemaObjOrBoolean["enum"] = ["poweron", "poweroff", "reboot"];
    }

    if ( (pathToSchemaFromParent[0] === "properties") && (pathToSchemaFromParent[1] === "status") ) {
        console.log("Found a status, I think....", pathToSchemaFromParent);
        schemaObjOrBoolean["enum"] = ["pending", "success", "failed"]; // failed? theres no docs, SW!
    }

    if ( (pathToSchemaFromParent[0] === "properties") && ((pathToSchemaFromParent[1] === "location") || (pathToSchemaFromParent[1] === "default_bootscript"))  ) {
        console.log("Found a location/default_bootscript, I think....", pathToSchemaFromParent);
        const partWeWant = schemaObjOrBoolean.anyOf[1];
        delete schemaObjOrBoolean["anyOf"];
        schemaObjOrBoolean["required"] = partWeWant["required"];
        schemaObjOrBoolean["type"] = partWeWant["type"];
        schemaObjOrBoolean["properties"] = partWeWant["properties"];
    }

    if ( (pathToSchemaFromParent[0] === "properties") && (pathToSchemaFromParent[1] === "export_uri") ) {
        console.log("Found a export_uri, I think....", pathToSchemaFromParent);
        schemaObjOrBoolean["type"] = "string";
    }

    if ( (pathToSchemaFromParent[0] === "properties") && (pathToSchemaFromParent[1] === "volumes") ) {
        console.log("Found a volume, I think....", pathToSchemaFromParent);
        const propWeWant = schemaObjOrBoolean.properties[1];
        delete schemaObjOrBoolean["properties"];
        propWeWant["type"] = "object";
        propWeWant["additionalProperties"] = "false";
        schemaObjOrBoolean["additionalProperties"] = propWeWant; 
    }

    console.log("-----------------------------------------------------===========----------------------------------------");

}

walker.schemaWalk(json, null, raiosFunc, walker.getVocabulary(json));

console.log("Writing file...");
fs.writeFileSync(schemaFile, JSON.stringify(json, null, 2), 'utf8');