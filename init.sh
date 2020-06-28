#!/bin/bash

SELF=$0

PSQL="docker exec -i translatr_db-translatr_1 psql -U postgres"
VERSION="3.0.1"
MAIN_DIR="$(cd $PWD; echo $PWD)"
TARGET_DIR="$MAIN_DIR/target"
DIST_DIR="$TARGET_DIR/universal"
LOAD_TEST_DIR="$TARGET_DIR/load-test"
LOG_FILE="/tmp/load-test.log"
JMETER="$(which jmeter)"
JMETER_VERSION="apache-jmeter-5.1.1"
JMETER_URL="http://www.apache.org/dist/jmeter/binaries/$JMETER_VERSION.zip"
export JMETER_OPTS="-Xmx1024m"

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

#
# Main functions
#

help() {
	echo usage: $SELF [clean] command
	echo
	echo Tools for Translatr development and testing
	echo
	echo clean$'\t\t'cleans the build directories
	echo
	echo Commands:
	echo
	echo load-test$'\t'start a load test
	echo start$'\t\t'build and start the server
	echo stop$'\t\t'stop the server
}

clean_database() {
	log_start Resetting database
	cat "$MAIN_DIR/load-test/cleanup.sql" | $PSQL >> "$LOG_FILE"
	echo 'create extension if not exists "uuid-ossp";' \
		| $PSQL -d translatr-load-test >> "$LOG_FILE"
	log_end
}

clean_dist() {
    cd "$MAIN_DIR"
    log_start Cleaning translatr build
    bin/activator clean >> "$LOG_FILE"
    log_end
}

build_dist() {
	if ! ls $DIST_DIR/translatr-$VERSION.zip > /dev/null 2>&1; then
		cd "$MAIN_DIR"
		log_start Building translatr
		bin/activator dist -J-Xmx1024m >> "$LOG_FILE"
		log_end
	fi
}

unzip_dist() {
	if ! ls $DIST_DIR/translatr-$VERSION/ > /dev/null 2>&1; then
		cd "$DIST_DIR"
		log_start Installing translatr
		unzip translatr-$VERSION.zip >> "$LOG_FILE"
		log_end
	fi
}

start_translatr() {
	cd "$DIST_DIR/translatr-$VERSION/"
	log_start Starting translatr
	bin/translatr \
		-Dconfig.file="$MAIN_DIR/load-test/load-test.conf" \
		-Dlogger.file="$MAIN_DIR/load-test/logback.xml" \
		-Dplay.crypto.secret="$SECRET" >> /dev/null &
	while ! nc -z localhost 9000; do
		sleep 1
	done >> "$LOG_FILE" 2>&1
	log_end
}

init_database() {
	cd "$MAIN_DIR"
	log_start Initialising database
  make clean target/load-test/init.sql >> "$LOG_FILE"
  cat "$LOAD_TEST_DIR/init.sql" | $PSQL translatr-load-test >> "$LOG_FILE"
	log_end
}

stop_translatr() {
	log_start Stopping translatr
	pkill -f translatr-$VERSION && log_end || log not running
	while pgrep -f translatr-$VERSION; do
		sleep 1
	done >> "$LOG_FILE" 2>&1
	rm -f "$DIST_DIR/translatr-$VERSION/RUNNING_PID"
}

prepare_jmeter() {
	if [ "$JMETER" = "" ]; then
		if [ ! -f "$TARGET_DIR/$JMETER_VERSION/bin/jmeter" ]; then
			cd "$TARGET_DIR"
			log_start Downloading JMeter
			wget -q "$JMETER_URL"
			log_end
			log_start Installing JMeter
			unzip "$JMETER_VERSION.zip" >> "$LOG_FILE"
			log_end
		fi
		export JMETER="$TARGET_DIR/$JMETER_VERSION/bin/jmeter"
	fi
}

run_translatr() {
	stop_translatr
	clean_database
	build_dist
	unzip_dist
	start_translatr
	init_database
}

run_load_test() {
	TEST_FILE="$1"
	SAMPLES_FILE="$LOAD_TEST_DIR/samples-$TEST_FILE.log"
	RESULTS_DIR="$LOAD_TEST_DIR/results-$TEST_FILE"

	log
	log Load testing: $TEST_FILE
	log

	run_translatr
	prepare_jmeter
	rm -rf "$RESULTS_DIR" "$SAMPLES_FILE"
	log_start Running load test
	$JMETER -n -t "$MAIN_DIR/load-test/$TEST_FILE.jmx" \
		-e -l "$SAMPLES_FILE" \
		-q "$LOAD_TEST_DIR/load-test.properties" \
		-JBaseDir="$LOAD_TEST_DIR" \
		-o "$RESULTS_DIR" >> "$LOG_FILE"
	log_end
	stop_translatr

	log Results: "$RESULTS_DIR/index.html"
}

load_test() {
	log Prepare and run load tests

	[ "$CLEAN" = "clean" ] && clean_dist

#	run_load_test load-test
	run_load_test load-test-ro
}

CMD=help
CLEAN=

for cmd in $@; do
	case $cmd in
		clean) CLEAN=clean;;
		load-test) CMD=load_test;;
		start) CMD=run_translatr;;
		stop) CMD=stop_translatr;;
		*) log Unknown command: $cmd; help; exit -1;;
	esac
done

$CMD
