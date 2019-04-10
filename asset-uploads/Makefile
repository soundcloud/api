APP_NAME ?= $(shell sc manifest name)
VERSION  ?= $(shell sc artifact-manager package-version)

GO ?= $(shell which go)

.PHONY: test
test: test-unit test-integration

.PHONY: test-unit
test-unit:
	$(GO) test

.PHONY: test-integration
test-integration: package
	cd testdata && ./test_integration.sh --tag=$(VERSION)

.PHONY: package
package:
	mkdir -p target/deb/srv/$(APP_NAME)/app
	$(GO) build -o target/deb/srv/$(APP_NAME)/app/api
	sc add-config config/test
	sc gen-wrapper-script --target=api --wrapper=api
	sc artifact-manager package --runtime=base

.PHONY: clean
clean:
	rm -rf target
	$(GO) clean

