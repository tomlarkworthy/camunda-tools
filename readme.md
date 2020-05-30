
# Open API notes



# At least once notes

REST API access

    curl https://camunda-flxotk3pnq-ew.a.run.app/engine-rest/job/

Modler supports bearer token deployment

# Delete all process instances

curl https://openresty-flxotk3pnq-ew.a.run.app/engine-rest/process-definition -H "Authorization: Bearer ${TOKEN}" | jq . > process.json

cat process.json | jq '.[].id' | grep Process_14vrtt5 - > todelete.txt

cat todelete.txt | xargs -L1 -I {} curl -X DELETE https://openresty-flxotk3pnq-ew.a.run.app/engine-rest/process-definition/{} -H "Authorization: Bearer ${TOKEN}"

