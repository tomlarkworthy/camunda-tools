import java.util.regex.Pattern
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
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
def command_re = ~/^(\S+) (\S+) (\S+) (.*)$/

def prefix = execution.getVariableLocal("prefix") ? execution.getVariableLocal("prefix") + " ": " ";

def popPath(path) { // This is bullshit that we need
    def parentkey_re = ~/^(?<parent>.*)\['(?<key>.*)'\]$/
    fullPath = JsonPath.compile(path).getPath(); // Canonicalize the path so simplify the regex
    def matcher = fullPath =~ parentkey_re
    matcher.matches()
    return [matcher.group('parent'), matcher.group('key')]
}

def exec(command, target, datatype, path, value) {
    println command + " " + target + " " + datatype + " " + path + " " + value
    
    def configuration = Configuration
        .defaultConfiguration()
        .addOptions(Option.SUPPRESS_EXCEPTIONS)
        .jsonProvider();

    if (command == "SET") {
        if (value == null) return;
        // read current value for the output
        def output = execution.getVariable(target);
        if (output == null) output = "{}";
        println ("Set " + output.toString() + " path " + path + " value " + value);
        (parentPath, key) = popPath(path)

        Object document = JsonPath
            .using(configuration)
            .parse(output.toString())

        def (ancestor, akey) = popPath(path)
        def emptyNodes = []

        while (true) {
            try {
                // No idea why Option.SUPPRESS_EXCEPTIONS is not fireing
                document.read(ancestor) // PathNotFoundException
                // OK its not null so we founda full one can can break while
                break;
            } catch (e) {
                emptyNodes.push(ancestor)
                (ancestor, akey) = popPath(ancestor)
            }
        }
        while (emptyNodes.size() > 0) {
            (holder, akey) = popPath(emptyNodes.pop())
            document.put(holder, akey, [:]);
        }

        result = document
            .put(parentPath, key, value) // see https://github.com/json-path/JsonPath/issues/570
            .jsonString(); // TODO we should probably stay in Spin
        println("Result " + result.toString());
        execution.setVariable(target, result);
    } else if (command == "EXTRACT") {
        // read current value for the target
        def input = execution.getVariable(target)
        if (input == null) input = "{}";
        println("input " + input.toString());
        try {
            Object data = JsonPath
                    .using(configuration)
                    .parse(input.toString())
                    .read(path);
            println "Extracted: " + data.toString();
            execution.setVariable(value, data);
        } catch (PathNotFoundException e) {
            System.err.println(e.getMessage()); 
            execution.setVariable(value, null);
        } catch (NullPointerException e) {
            System.err.println(e.getMessage()); 
            execution.setVariable(value, null);
        }
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