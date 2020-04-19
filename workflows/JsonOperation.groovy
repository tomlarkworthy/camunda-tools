import java.util.regex.Pattern
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Configuration;
import static org.camunda.spin.Spin.*;

/**
* Looks for process variables of the form
* <PREFIX>_<COMMAND>_<TARGET>_<DATATYPE>_<JSONPATH>
* where prefix is supplied in a local variable
*
* E.g.
* pre_SET_request_String_$['token']   : 'ksadklasdkhjasdkljashda'
* post_EXTRACT_response_List_$.items  : 'taskList'
*/
// <COMMAND>_<TARGET>_<DATATYPE>_<PATH>
def command_re = ~/^(\S+)_(\S+)_(\S+)_(.*)$/

def prefix = execution.getVariableLocal("prefix") ? execution.getVariableLocal("prefix") + "_" : "_";

def popPath(path) { // This is bullshit that we need
    def parentkey_re = ~/^(?<parent>.*)\['(?<key>.*)'\]$/
    fullPath = JsonPath.compile(path).getPath(); // Canonicalize the path so simplify the regex
    def matcher = fullPath =~ parentkey_re
    matcher.matches()
    return [matcher.group('parent'), matcher.group('key')]
}

def exec(command, target, datatype, path, value) {
    println command + " " + target + " " + datatype + " " + path + " " + value
    
    def configuration = Configuration.defaultConfiguration().jsonProvider();

    if (command == "SET") {
        // read current value for the output
        def output = execution.getVariable(target);
        if (output == null) output = "{}";
        // println ("Setting " + output.toString() + " path " + path + " value " + value);

        // JsonPath.set will not create a key, so we have to go one level up and put 
        // the key. Which is an annoyance
        (parentPath, key) = popPath(path)
        
        Object result = JsonPath
                .using(configuration)
                .parse(output.toString())
                .put(parentPath, key, value) // see https://github.com/json-path/JsonPath/issues/570
                .json();

        execution.setVariable(target, result);
    } else if (command == "EXTRACT") {
        // read current value for the target
        def input = execution.getVariable(target)
        if (input == null) input = "{}";
        println("input " + input.toString())
        Object data = JsonPath
                .using(configuration)
                .parse(input.toString())
                .read(path);
        // Set value
        execution.setVariable(value, data);
    } else {
        System.err.println "Unrecognised command: " + command; 
    }
}

execution.getVariables().findAll { 
    it.key.startsWith(prefix)
}.each { key, value -> 
    def cmd = key.substring(prefix.length());
    def matcher = command_re.matcher(cmd)
    if (!matcher.matches()) {
        System.err.println "Cannot parse command line: " + cmd; 
    } else {
        def command = matcher.group(1);
        def target = matcher.group(2);
        def datatype = matcher.group(3);
        def path = matcher.group(4);
        exec(command, target, datatype, path, value)
    }
}