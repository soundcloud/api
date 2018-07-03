GO ?= $(shell which go)

TEST_UPLOAD_SIZES  ?= 1 5 10 50 100 500 # mb
TEST_UPLOAD_ASSETS := $(TEST_UPLOAD_SIZES:%=test_upload_%.dat)

.PHONY: default
default: test

.PHONY: build
build:
	$(GO) build

.PHONY: test
test: $(TEST_UPLOAD_ASSETS) build start_echo_service
	./scripts/test.sh $(lastword $(TEST_UPLOAD_ASSETS))

test_upload_%.dat:
	dd if=/dev/urandom of=$@ bs=1MB count=$(@:test_upload_%.dat=%)

.PHONY: build_echo_service
build_echo_service:
	cd echo-service; $(GO) build

.PHONY: start_echo_service
start_echo_service: build_echo_service
	pkill echo-service || true
	cd echo-service; ./echo-service &

.PHONY: clean
clean:
	$(GO) clean
	cd echo-service; $(GO) clean
	rm -vf $(TEST_UPLOAD_ASSETS)
