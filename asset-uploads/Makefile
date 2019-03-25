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
test-integration: build config/test testdata/test_upload_1.dat dependencies
	env NO_AUTO_UPDATE=1 ./testdata/test.sh \
		--config=config/test \
		--asset_data=testdata/test_upload_1.dat

testdata/test_upload_%.dat:
	dd if=/dev/urandom of=$@ bs=1MB count=$(@:testdata/test_upload_%.dat=%)

.PHONY: clean
clean:
	$(GO) clean
	rm -vf testdata/test_upload_*.dat

.PHONY: dependencies
dependencies:
	docker-compose -f testdata/docker-compose.yml down
	docker-compose -f testdata/docker-compose.yml up -d
