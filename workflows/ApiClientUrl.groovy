import static org.camunda.spin.Spin.*;
if (execution.hasVariable("query")) {
   query = JSON(execution.getVariable("query")).mapTo("java.util.Map<java.lang.String, java.lang.String>");
} else {
   query = [:]
}

def queryParams = query.findAll { 
   it.value != null 
}.collect {
   id, value -> id + "=" + value
}
def queryString = queryParams.size() == 0 ? ""
                                          : "?" + queryParams.join("&")
def url = execution.getVariable("url") + queryString
println(url)
url