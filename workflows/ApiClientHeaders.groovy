def headers = [:]
execution.getVariables().findAll { 
   it.key.startsWith("hp_")
}.each { key, value ->
    headers[key.substring(3)] = value;
}
headers