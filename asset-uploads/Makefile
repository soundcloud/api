GO ?= $(shell which go)

.PHONY: default
default: test

.PHONY: build
build:
	$(GO) build

.PHONY: test
test: test-unit test-integration

.PHONY: test-unit
test-unit:
	$(GO) test

.PHONY: test-integration
test-integration: build config/test test_upload_1.dat dependencies
	./scripts/test.sh \
		--config=config/test \
		--asset_data=test_upload_1.dat

test_upload_%.dat:
	dd if=/dev/urandom of=$@ bs=1MB count=$(@:test_upload_%.dat=%)

.PHONY: clean
clean:
	$(GO) clean
	rm -vf test_upload_*.dat

.PHONY: dependencies
dependencies:
	docker-compose -f test/docker-compose.yml down
	docker-compose -f test/docker-compose.yml up -d
