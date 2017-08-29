#!/usr/bin/env bash

USER_NAME="$1 $2"
USER_USERNAME="$(echo "$1-$2" | tr '[:upper:]' '[:lower:]')"
USER_ID=$(uuidgen)
PROJECT_ID=$(uuidgen)
KEY_ID=$(uuidgen)
MESSAGE_ID=$(uuidgen)
GOOGLE_ID=$RANDOM$RANDOM$RANDOM$RANDOM
ACCESS_TOKEN=$(uuidgen | base64)
NOW="$(date '+%Y-%m-%d %H:%M:%S')"
LOCALE_NAMES="nl nl-NL en en-NZ en-GB en-US fr fr-FR de de-AT"
KEY_NAMES="a b c d e f g h i j"

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

echo "INSERT INTO locale (id,project_id,name,version,when_created,when_updated) VALUES"
LOCALE_IDS=""
for locale in $LOCALE_NAMES; do
  if [ "$LOCALE_IDS" != "" ]; then
    echo ","
  fi
  LOCALE_ID=$(uuidgen)
  LOCALE_IDS="$LOCALE_IDS $LOCALE_ID"
  echo -n "  ('$LOCALE_ID','$PROJECT_ID','$locale',1,'$NOW','$NOW')"
done
echo ";"

echo "INSERT INTO key (id,project_id,name,version,when_created,when_updated) VALUES"
KEY_IDS=""
for key in $KEY_NAMES; do
  if [ "$KEY_IDS" != "" ]; then
    echo ","
  fi
  KEY_ID=$(uuidgen)
  KEY_IDS="$KEY_IDS $KEY_ID"
  echo -n "  ('$KEY_ID','$PROJECT_ID','$key',1,'$NOW','$NOW')"
done
echo ";"

echo "INSERT INTO message (id,locale_id,key_id,value,version,when_created,when_updated) VALUES"
MESSAGE_IDS=""
for locale_id in $LOCALE_IDS; do
  for key_id in $KEY_IDS; do
    if [ "$MESSAGE_IDS" != "" ]; then
      echo ","
    fi
    MESSAGE_ID=$(uuidgen)
    MESSAGE_IDS="$MESSAGE_IDS $MESSAGE_ID"
    echo -n "  ('$MESSAGE_ID','$locale_id','$key_id','$locale_id - $key_id',1,'$NOW','$NOW')"
  done
done
echo ";"
echo
