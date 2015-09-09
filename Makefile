SBT := vendor/sbt/bin/sbt -Duser.home=$(shell echo "$$HOME") -Dsbt.boot.properties=project/sbt.boot.properties -J-Xmx3G -J-Xms512m

run: dev
	bin/dev-wrap --config=config/default.sh $(SBT) run

precheckin: unit-test local-integration-test

PUBLIC_API_STRANGLER_VERSION ?= $(shell PIPELINE_NUMBER=$(PIPELINE_NUMBER) artifact-manager package-version)

_testing_publicapistrangler:
	bin/replace-var.sh docker-compose.yml /tmp/integrationcompose.yml PUBLIC_API_STRANGLER_VERSION=latest PORT=5000 TELE_PORT=5001 DOCKER_IP=$(shell docker-ip)
	docker-compose -f /tmp/integrationcompose.yml -p publicapistrangler up -d publicapistrangler

integration-test: _testing_publicapistrangler
	bin/dev-wrap --config=config/integration.sh $(SBT) it:test

run-local-integration-test:
	bin/dev-wrap --config=config/default.sh $(SBT) it:test

local-integration-test:
	bin/local-integration-test

unit-test:
	bin/dev-wrap --config=config/default.sh $(SBT) test

interactive: _dev_docker_compose
	bin/dev-wrap --config=config/default.sh $(SBT)

dev: _dev_docker_compose

_dev_docker_compose:
	docker-compose up -d dev

compile:
	$(SBT) compile

sc-debian-layout: clean
	$(SBT) scDebianLayout:packageBin

clean:
	rm -rf target
