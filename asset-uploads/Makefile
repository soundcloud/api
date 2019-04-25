APP_NAME ?= $(shell sc manifest name)
VERSION  ?= $(shell sc artifact-manager package-version)

GO ?= sc crun go-1:latest -- go

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
	sc add-config config/api-production
	sc add-config config/api-production-secrets.enc
	sc add-config config/api-test
	sc add-config config/api-test-secrets.enc
	sc gen-wrapper-script --target=api --wrapper=api
	sc artifact-manager package --runtime=base

.PHONY: publish
publish:
	sc artifact-manager publish

.PHONY: clean
clean:
	rm -rf target
	$(GO) clean

