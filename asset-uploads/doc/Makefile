DOT ?= $(shell which dot)
GO  ?= $(shell which go)

FORMAT ?= svg

FSM_PARSER := fsm.go
FSM_SOURCE := $(PWD)/../request.go

default: clean fsm.$(FORMAT)

fsm.$(FORMAT): fsm.dot
	$(DOT) -T$(FORMAT) $? > $@

fsm.dot: $(FSM_PARSER) $(FSM_SOURCE)
	$(GO) run $(FSM_PARSER) -source $(FSM_SOURCE) > $@

.PHONY: clean
clean:
	rm -f fsm.{dot,$(FORMAT)}
