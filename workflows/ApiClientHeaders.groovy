import static org.camunda.spin.Spin.*;
if (!execution.hasVariable("headers")) return [:];
// execution.getVariable("headers").mapTo("java.util.Map<java.lang.String, java.lang.String>");
// TODO we should have headers as native types probably
JSON(execution.getVariable("headers")).mapTo("java.util.Map<java.lang.String, java.lang.String>");
