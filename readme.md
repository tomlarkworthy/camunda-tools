
# Open API notes



# At least once notes

REST API access

    curl https://camunda-flxotk3pnq-ew.a.run.app/engine-rest/job/

Modler supports bearer token deployment

# Delete all process instances

curl https://openresty-flxotk3pnq-ew.a.run.app/engine-rest/process-definition -H "Authorization: Bearer ${TOKEN}" | jq . > process.json


cat process.json | jq '.[].id' | xargs -L1 -I {} curl -X DELETE https://openresty-flxotk3pnq-ew.a.run.app/engine-rest/process-definition/{} -H "Authorization: Bearer ${TOKEN}"

cat process.json | jq '.[].id' | grep Process_14vrtt5 - > todelete.txt

cat todelete.txt | xargs -L1 -I {} curl -X DELETE https://openresty-flxotk3pnq-ew.a.run.app/engine-rest/process-definition/{} -H "Authorization: Bearer ${TOKEN}"

# GA 

Search for dimentions in GA

https://ga-dev-tools.appspot.com/dimensions-metrics-explorer/

Link to Datastudio Filters

https://datastudio.google.com/reporting/1XAGzyAcXG2wLSaKqe1tBsmmzUVoqvktl/page/Kq7UB?params=%7B%22df4%22:%22include%25EE%2580%25800%25EE%2580%2580IN%25EE%2580%2580%252Fblog%252Fopenresty-a-swiss-army-proxy-for-serverless%22%7D


https://datastudio.google.com/reporting/1XAGzyAcXG2wLSaKqe1tBsmmzUVoqvktl/page/Kq7UB?params=%7B%22df5%22:%22include%25EE%2580%25800%25EE%2580%2580IN%25EE%2580%258020200524%22,%22df4%22:%22include%25EE%2580%25800%25EE%2580%2580IN%25EE%2580%2580%252Fwork%252Fwartsila-weleap%22%7D