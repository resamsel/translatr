#!/usr/bin/env bash

USER_NAME="$1 $2"
USER_USERNAME="$(echo "$1-$2" | tr '[:upper:]' '[:lower:]')"
USER_ID=$(uuidgen)
PROJECT_ID=$(uuidgen)
PROJECT_NAME=Test
KEY_ID=$(uuidgen)
MESSAGE_ID=$(uuidgen)
GOOGLE_ID=$RANDOM$RANDOM$RANDOM$RANDOM
ACCESS_TOKEN=$(uuidgen | base64)
NOW="$(date '+%Y-%m-%d %H:%M:%S')"
LOCALE_NAMES="nl nl-NL en en-NZ en-GB en-US fr fr-FR de de-AT"
KEY_NAMES="a b c d e f g h i j"
KEY_INDICES="$(seq -s' ' 1 ${KEY_FACTOR:-1})"

cat <<EOF
-- $USER_ID,$USER_NAME,$USER_USERNAME,$PROJECT_ID,$PROJECT_NAME,$ACCESS_TOKEN
INSERT INTO user_ (id,when_created,when_updated,username,name,email,email_validated,active)
  VALUES ('$USER_ID','$NOW','$NOW','$USER_USERNAME','$USER_NAME','$USER_USERNAME@email.com',false,true);
INSERT INTO linked_account (when_created,when_updated,user_id,provider_user_id,provider_key)
  VALUES ('$NOW','$NOW','$USER_ID','$GOOGLE_ID','google');
INSERT INTO access_token (when_created,when_updated,user_id,name,key,scope)
  VALUES ('$NOW','$NOW','$USER_ID','JMeter','$ACCESS_TOKEN','read:notification,read:locale,write:locale,read:user,write:message,read:project,write:project,write:key,write:user,read:message,read:key,write:notification');
INSERT INTO project (id,deleted,name,when_created,when_updated,owner_id)
  VALUES ('$PROJECT_ID',false,'$PROJECT_NAME','$NOW','$NOW','$USER_ID');
INSERT INTO project_user (when_created,when_updated,project_id,user_id,role)
  VALUES ('$NOW','$NOW','$PROJECT_ID','$USER_ID','Owner');
EOF

cat <<-EOF
  INSERT INTO locale (id,project_id,name,when_created,when_updated)
    select uuid_generate_v1(), '$PROJECT_ID', l, current_timestamp, current_timestamp
      from regexp_split_to_table('$LOCALE_NAMES', ' ') l;
EOF
cat <<-EOF
  INSERT INTO key (id,project_id,name,when_created,when_updated)
    select uuid_generate_v1(), '$PROJECT_ID', k || ki, current_timestamp, current_timestamp
      from regexp_split_to_table('$KEY_NAMES', ' ') k, regexp_split_to_table('$KEY_INDICES', ' ') ki;
EOF

cat <<-EOF
  INSERT INTO message (id,locale_id,key_id,value,when_created,when_updated)
    select uuid_generate_v1(), l.id, k.id, k.name || ' - ' || l.name, current_timestamp, current_timestamp
      from locale l, key k
        where l.project_id = '$PROJECT_ID' and k.project_id = '$PROJECT_ID';
EOF

echo
