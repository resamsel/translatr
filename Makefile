LOG_FILE = /tmp/load-test.log
TARGET = target/load-test
CSV_HEADER = Id,Name,Username,ProjectId,AccessToken

PERSONAS = Margaret Armin
COUNT = 15
Armin_COUNT = 100

log = $(shell echo "$@" | tee -a "$(LOG_FILE)")
log_start = $(call log,$(1))

$(TARGET)/%.sql:
	mkdir -p $(TARGET)
	for i in `seq 1 $(or $($(@:$(TARGET)/%.sql=%_COUNT)),$($(@:$(TARGET)/%.sql=%_COUNT)),$(COUNT))`; do \
		measurements/user-template.sh $(@:$(TARGET)/%.sql=%) $$i ; \
	done >> $@

$(TARGET)/%.csv: $(TARGET)/%.sql
	mkdir -p $(TARGET)
	echo $(CSV_HEADER) > $@
	grep -E ^-- "$^" | sed 's/^-- //' | grep "$(@:$(TARGET)/%.csv=%)" >> $@

$(TARGET)/init.sql: $(PERSONAS:%=$(TARGET)/%.csv)
	mkdir -p $(TARGET)
	cat $(^:$(TARGET)/%.csv=$(TARGET)/%.sql) > $@

init.sql: $(TARGET)/init.sql

clean:
	rm -f $(PERSONAS:%=$(TARGET)/%.csv)
	rm -f $(PERSONAS:%=$(TARGET)/%.sql)
	rm -f $(TARGET)/init.sql
