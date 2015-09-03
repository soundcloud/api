SBT := vendor/sbt/bin/sbt -Duser.home=$(shell echo "$$HOME") -Dsbt.boot.properties=project/sbt.boot.properties -J-Xmx3G -J-Xms512m

run: _dev_docker_compose
	./bin/source-config --config=config/default.sh $(SBT) run

precheckin: unit-test integration-test

PUBLIC_API_STRANGLER_VERSION ?= $(shell PIPELINE_NUMBER=$(PIPELINE_NUMBER) artifact-manager package-version)

integration-test: spin-up-app run-integration-test

spin-up-app:
	bin/replace-var.sh docker-compose.yml /tmp/integrationcompose.yml PUBLIC_API_STRANGLER_VERSION=$(PUBLIC_API_STRANGLER_VERSION) PORT=5000 TELE_PORT=5001 DOCKER_IP=$(shell docker-ip)
	cat /tmp/integrationcompose.yml
	docker-compose -f /tmp/integrationcompose.yml -p publicapistrangler up -d publicapistrangler

run-integration-test:
	bin/wait-until-spun-up.sh
	./bin/source-configuration.sh --config=config/integration.sh $(SBT) 'it:test'

unit-test:
	$(SBT) test

interactive: _dev_docker_compose
	./bin/source-configuration.sh --config=config/default.sh $(SBT)

dev: _dev_docker_compose

_dev_docker_compose:
	docker-compose up -d

compile:
	$(SBT) compile

sc-debian-layout: clean
	$(SBT) scDebianLayout:packageBin

clean:
	rm -rf target
