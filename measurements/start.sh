#!/bin/bash

MAIN_DIR="$(cd $PWD/..; echo $PWD)"
DIST_DIR="$MAIN_DIR/target/universal"
LOG_FILE="/tmp/start.log"
SECRET="794DC7FC-CB32-4D28-A173-AA388239C8BD"

set -e

log() {
  echo "$@ " | tee -a "$LOG_FILE"
}

echo -n > "$LOG_FILE"

log -n Stopping translatr...
pkill -f translatr && log OK || log No running instances found

log -n Cleaning the database...
cat "$MAIN_DIR/measurements/cleanup.sql" | docker exec -i translatr_db-translatr_1 psql -U postgres >> "$LOG_FILE"
log OK

if [ "$1" = "clean" ]; then
  cd "$MAIN_DIR"
  log -n Building the distribution...
  bin/activator clean dist -J-Xmx1024m >> "$LOG_FILE"
  log OK
fi

cd "$DIST_DIR"
log -n Installing the distribution...
rm -rf translatr-*/
unzip translatr-*.zip >> "$LOG_FILE"
log OK

cd translatr-*/
log -n Starting translatr...
bin/translatr -Dconfig.file="$MAIN_DIR/measurements/load-test.conf" -Dplay.crypto.secret="$SECRET" >> "$DIST_DIR/load-test.log" 2>&1 &
log OK

log -n Waiting for startup...
while ! nc -z localhost 9000; do
  sleep 1 # wait for 1/10 of the second before check again
done >> "$LOG_FILE" 2>&1
log OK

log -n Initialising the database...
cat "$MAIN_DIR/measurements/init.sql" | docker exec -i translatr_db-translatr_1 psql -U postgres translatr-load-test >> "$LOG_FILE"
log OK

log
log Ready for load testing.
log