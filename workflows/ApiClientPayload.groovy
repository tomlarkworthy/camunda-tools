import groovy.json.jsonSlurper;

// No content parameter, no body
if (!execution.hasVariable("content")) return null;

// Otherwise start with the base content
def json = jsonSlurper.parseText(
    execution.getVariable("content");
)

// Override with the additional fields
execution.getVariables().findAll { 
   it.key.startsWith("cp_")
}.each { key, value ->
    json[key.substring(3)] = value;
}

// Serialize to a string
JsonOutput.toJson(json)