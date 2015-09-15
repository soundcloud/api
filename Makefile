SBT := vendor/sbt/bin/sbt -Duser.home=$(shell echo "$$HOME") -Dsbt.boot.properties=project/sbt.boot.properties -J-Xmx3G -J-Xms512m

PUBLIC_API_STRANGLER_VERSION ?= $(shell PIPELINE_NUMBER=$(PIPELINE_NUMBER) artifact-manager package-version)

_local_container:
	bin/replace-var docker-compose.yml /tmp/integrationcompose.yml PUBLIC_API_STRANGLER_VERSION=$(PUBLIC_API_STRANGLER_VERSION) PORT=5000 TELE_PORT=5001 DOCKER_IP=$(shell docker-ip)
	docker-compose -f /tmp/integrationcompose.yml -p publicapistrangler up -d publicapistrangler

_dev_docker_compose:
	docker-compose up -d dev

_run-local-integration-test:
	bin/dev-wrap --config=config/default.sh $(SBT) it:test

run: _dev_docker_compose
	bin/dev-wrap --config=config/default.sh $(SBT) run

precheckin: unit-test local-integration-test

integration-test: _local_container
	bin/dev-wrap --config=config/integration.sh $(SBT) it:test

local-integration-test:
	bin/local-integration-test

unit-test:
	bin/dev-wrap --config=config/default.sh $(SBT) test

interactive: _dev_docker_compose
	bin/dev-wrap --config=config/default.sh $(SBT)

compile:
	$(SBT) compile

sc-debian-layout: clean
	$(SBT) scDebianLayout:packageBin

clean:
	rm -rf target
