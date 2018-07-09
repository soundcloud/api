GO ?= $(shell which go)

.PHONY: default
default: test

.PHONY: build
build:
	$(GO) build

.PHONY: test
test: build config/test test_upload_1.dat
	docker-compose up -d
	./scripts/test.sh \
		--config=config/test \
		--asset_data=test_upload_1.dat

config/test: config/test.enc
	curl -sf -o $@ -T $< http://shibboleth.int.s-cloud.net/decrypt

test_upload_%.dat:
	dd if=/dev/urandom of=$@ bs=1MB count=$(@:test_upload_%.dat=%)

.PHONY: clean
clean:
	$(GO) clean
	rm -vf test_upload_*.dat
