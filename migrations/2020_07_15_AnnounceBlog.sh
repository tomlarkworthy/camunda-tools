#!/bin/bash
set -euxo pipefail


#PLAN=$(curl -0 -v -X POST https://openresty-flxotk3pnq-ew.a.run.app/engine-rest/migration/generate \
#-H 'Content-Type: application/json; charset=utf-8' \
#-H "Authorization: Bearer ${TOKEN}" \
#--data-binary @- << EOF
#{
#  "sourceProcessDefinitionId": "AnnounceBlog:27:a55af726-a6a8-11ea-9dbb-53e5a934995a",
#  "targetProcessDefinitionId": "AnnounceBlog:28:a046f6be-ae85-11ea-ad69-1b59ea565fcd",
#  "instructions": []
#}
#EOF
#)

curl -0 -v -X POST https://openresty-flxotk3pnq-ew.a.run.app/engine-rest/migration/execute \
-H 'Content-Type: application/json; charset=utf-8' \
-H "Authorization: Bearer ${TOKEN}" \
--data-binary @- << EOF
{
  "migrationPlan": {
    "sourceProcessDefinitionId": "AnnounceBlog:27:a55af726-a6a8-11ea-9dbb-53e5a934995a",
    "targetProcessDefinitionId": "AnnounceBlog:28:a046f6be-ae85-11ea-ad69-1b59ea565fcd",
    "instructions": [{
      "sourceActivityIds":["Gateway_1kvfyci"],
      "targetActivityIds":["Gateway_0kubl9d"]
    }]
  },
  "processInstanceQuery": {
    "processDefinitionId": "AnnounceBlog:27:a55af726-a6a8-11ea-9dbb-53e5a934995a"
  }
}