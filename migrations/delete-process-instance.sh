#!/bin/bash
set -euxo pipefail

curl -0 -X DELETE https://openresty-flxotk3pnq-ew.a.run.app/engine-rest/process-instance/$1 \
-H 'Content-Type: application/json; charset=utf-8' \
-H "Authorization: Bearer ${TOKEN}" | jq .