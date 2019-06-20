VERSION ?= $(shell sc artifact-manager package-version)

GO ?= sc crun go-1:latest -- go

.PHONY: test
test: test-unit test-integration

.PHONY: test-unit
test-unit:
	$(GO) test

.PHONY: test-integration
test-integration: package
	cd testdata && ./test_integration.sh --tag=$(VERSION)

target/api: $(wildcard *.go)
	$(GO) build -o $@

.PHONY: assemble
assemble: config/api-production
assemble: config/api-production-secrets.enc
assemble: config/api-test
assemble: config/api-test-secrets.enc
assemble: target/api
	sc add-config config/api-production
	sc add-config config/api-production-secrets.enc
	sc add-config config/api-test
	sc add-config config/api-test-secrets.enc
	sc add-files --strip-components=1 target/api
	sc gen-wrapper-script \
		--target='api -addr=$$HOSTNAME:$$PORT -admin-addr=$$HOSTNAME:$$TELE_PORT -max-request-bytes=$$MAX_REQUEST_BYTES' \
		--wrapper=api

.PHONY: package
package: assemble
	sc artifact-manager package --runtime=base

.PHONY: clean
clean:
	rm -rf target
	$(GO) clean

.PHONY: publish
publish:
	sc artifact-manager publish

# We're deploying into the "public-api-strangler" system/namespace.
# The "api" component will be called "asset-uploads-api" in that namespace.
COMPONENT ?= asset-uploads-api
ENV       ?= $(error need to set ENV)
SYSTEM    ?= public-api-strangler
ZONE      ?= $(error need to set ZONE)

define DEPLOY_FLAGS
	--component=$(COMPONENT) \
	--env=$(ENV) \
	--system=$(SYSTEM) \
	--zone=$(ZONE)
endef

# Glimpse naming conventions require mapping "production" to "prod".
# The public-api-strangler deployment is the "public-api" product in glimpse.
.PHONY: publish-deploy
publish-deploy: GLIMPSE_ENV=$(ENV:production=prod)
publish-deploy: GLIMPSE_PRODUCT=public-api
publish-deploy:
	sc artifact-manager deploy $(DEPLOY_FLAGS) publish \
		--command="./api --config=api-$(ENV)" \
		--glimpse=http.$(COMPONENT).$(GLIMPSE_ENV).$(GLIMPSE_PRODUCT) \
		--pod=config/pod.yaml \
		--prometheus.port=telemetry

.PHONY: promote-to-stable
promote-to-stable:
	sc artifact-manager promote stable

.PHONY: promote-to-release
promote-to-release:
	sc artifact-manager promote release

.PHONY: deploy
deploy:
	sc artifact-manager deploy $(DEPLOY_FLAGS) run
