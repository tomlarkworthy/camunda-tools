// No request parameter, no body
if (!execution.hasVariable("request")) return null;
def request = execution.getVariable("request").toString();
println "request: " + request
request