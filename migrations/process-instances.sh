#!/bin/bash
set -euxo pipefail

curl -0 -X GET https://openresty-flxotk3pnq-ew.a.run.app/engine-rest/process-instance \
-H 'Content-Type: application/json; charset=utf-8' \
-H "Authorization: Bearer ${TOKEN}" | jq .