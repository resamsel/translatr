#!/bin/bash

MAIN_DIR="$(cd $PWD/..; echo $PWD)"
DIST_DIR="$MAIN_DIR/target/universal"
LOG_FILE="/tmp/start.log"
RESULTS_DIR="/tmp/results-$$"

SECRET="794DC7FC-CB32-4D28-A173-AA388239C8BD"

set -e

log() {
  echo "$@" | tee -a "$LOG_FILE"
}

log_start() {
  export ts=$(date +%s)
  log -n "$@... "
}

log_end() {
  tt=$(($(date +%s) - $ts))
  log "OK ($tt s)"
}

echo -n > "$LOG_FILE"

log_start Stopping translatr
pkill -f translatr && log_end || log no running instances found

log_start Cleaning the database
cat "$MAIN_DIR/measurements/cleanup.sql" | docker exec -i translatr_db-translatr_1 psql -U postgres >> "$LOG_FILE"
log_end

if [ "$1" = "clean" ]; then
  cd "$MAIN_DIR"
  log_startstart Building the distribution
  bin/activator clean dist -J-Xmx1024m >> "$LOG_FILE"
  log_end
fi

cd "$DIST_DIR"
log_start Installing the distribution
rm -rf translatr-*/
unzip translatr-*.zip >> "$LOG_FILE"
log_end

cd translatr-*/
log_start Starting translatr
bin/translatr -Dconfig.file="$MAIN_DIR/measurements/load-test.conf" -Dplay.crypto.secret="$SECRET" >> "$DIST_DIR/load-test.log" 2>&1 &
while ! nc -z localhost 9000; do
  sleep 1 # wait for 1/10 of the second before check again
done >> "$LOG_FILE" 2>&1
log_end

log_start Initialising the database
cat "$MAIN_DIR/measurements/init.sql" | docker exec -i translatr_db-translatr_1 psql -U postgres translatr-load-test >> "$LOG_FILE"
log_end

cd "$MAIN_DIR/measurements"
log_start Run load test
jmeter -n -t "$MAIN_DIR/measurements/Translatr.jmx" -e -l /tmp/samples-$$.log -o "$RESULTS_DIR" >> "$LOG_FILE"
log_end

log Results of load test: "$RESULTS_DIR/index.html"
