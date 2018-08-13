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
test-integration: build config/test test_upload_1.dat
	docker-compose up -d
	./scripts/test.sh \
		--config=config/test \
		--asset_data=test_upload_1.dat

test_upload_%.dat:
	dd if=/dev/urandom of=$@ bs=1MB count=$(@:test_upload_%.dat=%)

.PHONY: clean
clean:
	$(GO) clean
	rm -vf config/test test_upload_*.dat

# Generic target for decrypting files with shibboleth
%: %.enc
	curl --silent --fail --output $@ --upload-file $< https://shibboleth.int.s-cloud.net/decrypt