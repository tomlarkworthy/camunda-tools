"use strict";
var __importStar = (this && this.__importStar) || function (mod) {
    if (mod && mod.__esModule) return mod;
    var result = {};
    if (mod != null) for (var k in mod) if (Object.hasOwnProperty.call(mod, k)) result[k] = mod[k];
    result["default"] = mod;
    return result;
};
Object.defineProperty(exports, "__esModule", { value: true });
const fs = require("fs");
const mustache = __importStar(require("mustache"));
const operationTemplate = fs.readFileSync('operation.mustache').toString();
function resolveRef(doc, ref) {
    if (ref == null)
        return null;
    if ("$ref" in ref) {
        if (ref["$ref"].startsWith("#/")) {
            // Root of local document
            const path = ref["$ref"].substring(2).split("/");
            // Walk curser through doc
            const subdoc = path.reduce((curser, segment) => curser[segment], doc);
            return subdoc;
        }
    }
    else {
        return ref;
    }
}
/**
 * Convert p1/{id}/p3/{id2} to p1/${pp_id}/p3/${pp_id2}
 * And p1/{+p2}/p3 to p1/${pp_p2}
 */
function pathTemplate2expression(template) {
    return template.replace(/(?<={)\s*[^{}]+\s*(?=})/g, match => 'pp_' + match.replace("+", ""))
        .replace(/{\s*[^{}]+\s*}/g, match => '$' + match);
}
function writeOperations(api, out) {
    const baseUrl = (api.host ? "https://" + api.host // Swagger 2.0 support
        : api.servers[0].url) // OpenAPI 3.0 support
        + (api.basePath ? api.basePath : ''); // Swagger 2.0 support
    console.log(baseUrl);
    var first = true;
    for (var path in api.paths) {
        var item = api.paths[path];
        const baseParams = (item.parameters || [])
            .map(p => resolveRef(api, p));
        for (var method in item) {
            if (method == "parameters")
                continue;
            const op = item[method];
            if (first) {
                out.write("[\n");
                first = false;
            }
            else
                out.write(",\n");
            const body = resolveRef(api, op.requestBody);
            const consumes = op.consumes;
            var contentType = null; // No body by default (e.g. GET, DELETE request)
            var contentSchema = {};
            if (body) {
                if (body.content['application/json']) {
                    contentType = 'application/json';
                    contentSchema = resolveRef(api, body.content['application/json'].schema);
                }
                else {
                    console.log("Unsupported body content type:", body.content, method, path);
                }
            }
            else if (consumes) {
                if (consumes.find(entry => entry == 'application/json')) {
                    contentType = 'application/json';
                } /*else if (consumes.find(entry => entry == 'application/x-www-form-urlencoded')) {
                    contentType = 'application/x-www-form-urlencoded'
                } */
                else {
                    console.log("Unsupported consumes content type:", consumes, method, path);
                }
            }
            const contentJson = contentType == "application/json" ? {
                value: "{}",
                description: '"TODO"',
                schema: contentSchema
            } : undefined;
            const params = (op.parameters || [])
                .map(p => resolveRef(api, p))
                .concat(baseParams)
                .map(p => ({ ...p, description: p.description ? JSON.stringify(p.description) : "null" }));
            const declaredFormDataParams = params.filter(p => p.in == 'formData')
                .map(p => ({ ...p, type: "String", path: `['${p.name}']` }))
                .map(p => ({ ...p, description: p.description ? JSON.stringify(p.description) : "null" }));
            const formDataParams = contentType == "application/json" ? [] // No Url encoded form params when using JSON (see Slack API)
                : declaredFormDataParams;
            const contentParams = contentJson ? extractContentFields(api, contentJson.schema).concat(declaredFormDataParams) : undefined;
            const response = resolveRef(api, (op.responses || {})["200"]);
            const responseMedia = (((response && response.content || {})['*/*']) || {});
            const responseParams = responseMedia ? extractContentFields(api, resolveRef(api, responseMedia.schema)) : undefined;
            out.write(mustache.render(operationTemplate, {
                calledElement: "ApiClient",
                id: `${method.toUpperCase()}-${path}`,
                path: path,
                urlExpression: baseUrl + pathTemplate2expression(path),
                method: method.toUpperCase(),
                queryParams: params.filter(p => p.in == 'query').map(p => ({ ...p, type: "String" })),
                pathParams: params.filter(p => p.in == 'path').map(p => ({ ...p, type: "String" })),
                headerParams: params.filter(p => p.in == 'header').map(p => ({ ...p, type: "String" })),
                formDataParams: formDataParams,
                contentParams: contentParams,
                contentType: contentType,
                contentJson: contentJson,
                responseParams: responseParams,
            }));
        }
    }
    out.write("]\n");
}
function extractContentFields(api, schema) {
    if (schema == null)
        return [];
    const results = [];
    for (var name in (schema.properties || [])) {
        const subschema = resolveRef(api, schema.properties[name]);
        switch (subschema.type) {
            case "string":
            case "boolean":
            case "integer":
                results.push({
                    name: name,
                    type: subschema.type,
                    path: `['${name}']`,
                    description: subschema.description ? JSON.stringify(subschema.description) : "null"
                });
                break;
            case "array":
                results.push({
                    name: name,
                    type: "array",
                    path: `['${name}']`,
                    description: subschema.description ? JSON.stringify(subschema.description) : "null"
                });
                break;
            case "object":
                results.push({
                    name: name,
                    type: "object",
                    path: `['${name}']`,
                    description: subschema.description ? JSON.stringify(subschema.description) : "null"
                });
                const children = extractContentFields(api, subschema);
                results.push(...children.map(child => ({
                    name: name + "_" + child.name,
                    type: child.type,
                    path: `['${name}']` + child.path,
                    description: child.description
                })));
                break;
            default:
                console.error("Unknown content_field type", subschema.type);
                break;
        }
    }
    return results;
}
{
    const api = require("./tasks_v1.json");
    const out = fs.createWriteStream("../.camunda/element-templates/tasks_v1.json");
    writeOperations(api, out);
    out.end();
}
{
    const api = require("./slack_web_openapi_v2.json");
    const out = fs.createWriteStream("../.camunda/element-templates/slack.json");
    writeOperations(api, out);
    out.end();
}
{
    const api = require("./secretsmanager_v1.json");
    const out = fs.createWriteStream("../.camunda/element-templates/secretsmanager.json");
    writeOperations(api, out);
    out.end();
}
{
    const api = require("./rebrandly.json");
    const out = fs.createWriteStream("../.camunda/element-templates/rebrandly.json");
    writeOperations(api, out);
    out.end();
}
/*
{
    const api: openapi3.OpenAPIObject = require("./docs_v1.json")
    const out = fs.createWriteStream("../.camunda/element-templates/docs_v1.json");
    writeOperations(api, out);
    out.end();

    
}
{
    const api: openapi3.OpenAPIObject = require("./youtube_data_v3.json")
    const out = fs.createWriteStream("../.camunda/element-templates/youtube_data_v3.json");
    writeOperations(api, out);
    out.end();
}*/
