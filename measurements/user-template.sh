#!/usr/bin/env bash

USER_NAME="$1 $2"
USER_USERNAME="$(echo "$1-$2" | tr '[:upper:]' '[:lower:]')"
USER_ID=$(uuidgen)
PROJECT_ID=$(uuidgen)
GOOGLE_ID=$RANDOM$RANDOM$RANDOM$RANDOM
ACCESS_TOKEN=$(uuidgen | base64)
NOW=$(date "+%Y-%m-%d %H:%M:%S")

cat <<EOF
-- $USER_ID,$USER_NAME,$USER_USERNAME,$PROJECT_ID,$ACCESS_TOKEN
INSERT INTO user_ (id,version,when_created,when_updated,username,name,email,email_validated,active)
  VALUES ('$USER_ID',1,'$NOW','$NOW','$USER_USERNAME','$USER_NAME','$USER_USERNAME@email.com',false,true);
INSERT INTO linked_account (version,when_created,when_updated,user_id,provider_user_id,provider_key)
  VALUES (1,'$NOW','$NOW','$USER_ID','$GOOGLE_ID','google');
INSERT INTO access_token (version,when_created,when_updated,user_id,name,key,scope)
  VALUES (1,'$NOW','$NOW','$USER_ID','JMeter','$ACCESS_TOKEN','read:notification,read:locale,write:locale,read:user,write:message,read:project,write:project,write:key,write:user,read:message,read:key,write:notification');
INSERT INTO project (id,deleted,name,version,when_created,when_updated,owner_id)
  VALUES ('$PROJECT_ID',false,'Test',1,'$NOW','$NOW','$USER_ID');
INSERT INTO project_user (version,when_created,when_updated,project_id,user_id,role)
  VALUES (1,'$NOW','$NOW','$PROJECT_ID','$USER_ID','Owner');

EOF