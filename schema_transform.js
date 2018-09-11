const walker = require("@cloudflare/json-schema-walker");
const schemaFile = process.argv[2];
console.log("Transforming schema", schemaFile);
const fs = require('fs');
var json = JSON.parse(fs.readFileSync(schemaFile, 'utf8'));


// Warn if overriding existing method
if (Array.prototype.equals)
    console.warn("Overriding existing Array.prototype.equals. Possible causes: New API defines the method, there's a framework conflict or you've got double inclusions in your code.");
// attach the .equals method to Array's prototype to call it on any array
Array.prototype.equals = function (array) {
    // if the other array is a falsy value, return
    if (!array)
        return false;

    // compare lengths - can save a lot of time 
    if (this.length != array.length)
        return false;

    for (var i = 0, l = this.length; i < l; i++) {
        // Check if we have nested arrays
        if (this[i] instanceof Array && array[i] instanceof Array) {
            // recurse into the nested arrays
            if (!this[i].equals(array[i]))
                return false;
        }
        else if (this[i] != array[i]) {
            // Warning - two different object instances will never be equal: {x:20} != {x:20}
            return false;
        }
    }
    return true;
}
// Hide method from for-in loops
Object.defineProperty(Array.prototype, "equals", {enumerable: false});


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
    
    if ( (pathToSchemaFromParent[0] === "properties") && (pathToSchemaFromParent[1].endsWith("_date")) ) {
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