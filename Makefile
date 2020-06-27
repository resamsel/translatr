LOG_FILE = /tmp/load-test.log
TARGET = target/load-test
CSV_HEADER = Id,Name,Username,ProjectId,ProjectName,AccessToken

PERSONAS = Margaret Armin Anne Martin Marie Roberto Peter Sophia
THREADS = 20
LOOPS = 25
KEY_FACTOR = 1

Armin_THREADS = 50
Armin_LOOPS = 30
Roberto_THREADS = 10
Roberto_LOOPS = 15
Peter_THREADS = 10
Peter_LOOPS = 100
Sophia_THREADS = 10
Sophia_LOOPS = 10
Sophia_KEY_FACTOR = 10000

log = $(shell echo "$@" | tee -a "$(LOG_FILE)")
log_start = $(call log,$(1))

init:
	mkdir -p $(TARGET)

$(TARGET)/load-test.properties:
	echo "[AppSpecific]" > $@

$(TARGET)/%.sql: $(TARGET)/load-test.properties
	$(eval PERSONA := $(@:$(TARGET)/%.sql=%))
	$(eval T := $(or $($(PERSONA)_THREADS),$($(PERSONA)_THREADS),$(THREADS)))
	$(eval L := $(or $($(PERSONA)_LOOPS),$($(PERSONA)_LOOPS),$(LOOPS)))
	$(eval K := $(or $($(PERSONA)_KEY_FACTOR),$($(PERSONA)_KEY_FACTOR),$(KEY_FACTOR)))
	for i in `seq 1 $(T)`; do \
		KEY_FACTOR=$(K) load-test/user-template.sh $(@:$(TARGET)/%.sql=%) $$i ; \
	done >> $@

	echo >> $<
	echo "; Persona $(PERSONA)" >> $<
	echo "$(@:$(TARGET)/%.sql=%.threads) = $(T)" >> $<
	echo "$(@:$(TARGET)/%.sql=%.loops) = $(L)" >> $<
	echo "$(@:$(TARGET)/%.sql=%.csv) = $(PWD)/$(@:$(TARGET)/%.sql=$(TARGET)/%.csv)" >> $<

$(TARGET)/%.csv: $(TARGET)/%.sql
	echo $(CSV_HEADER) > $@
	grep -E ^-- "$^" | sed 's/^-- //' | grep "$(@:$(TARGET)/%.csv=%)" >> $@

$(TARGET)/init.sql: $(PERSONAS:%=$(TARGET)/%.csv)
	cat $(^:$(TARGET)/%.csv=$(TARGET)/%.sql) > $@

clean:
	rm -f $(PERSONAS:%=$(TARGET)/%.csv)
	rm -f $(PERSONAS:%=$(TARGET)/%.sql)
	rm -f $(TARGET)/init.sql
	rm -f $(TARGET)/load-test.properties

-include init
