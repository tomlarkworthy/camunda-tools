def variables = execution.getVariables();
println("variables " + variables);
def queryParams = variables.findAll { 
   it.key.startsWith("qp_")
}.collect { id, value ->
    id + "=" + value
}
def queryString = queryParams.size() == 0 ? ""
                                          : "?" + queryParams.join("&")
def url = 
   execution.getVariable("url") + 
   queryString
println(url)
url