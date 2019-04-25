DOT ?= $(shell which dot)

FORMAT ?= svg

default: clean overview.$(FORMAT)

%.$(FORMAT): %.dot
	$(DOT) -T $(FORMAT) $< > $@

.PHONY: clean
clean:
	rm -f *.$(FORMAT)
