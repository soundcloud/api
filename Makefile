SBT := vendor/sbt/bin/sbt -Duser.home=$(shell echo "$$HOME") -Dsbt.boot.properties=project/sbt.boot.properties -J-Xmx3G -J-Xms512m
DEFAULT_CONFIG := bin/dev-wrap --config=config/default.sh

PUBLIC_API_STRANGLER_VERSION ?= $(shell PIPELINE_NUMBER=$(PIPELINE_NUMBER) artifact-manager package-version)

_dev_docker_compose:
	docker-compose up -d dev

_run-end-to-end-test:
	$(DEFAULT_CONFIG) $(SBT) endToEnd/test

run: _dev_docker_compose
	$(DEFAULT_CONFIG) $(SBT) run

precheckin: unit-test end-to-end-test

ci: unit-test ci-end-to-end-test

ci-end-to-end-test:
	bin/replace-var docker-compose.yml /tmp/integrationcompose.yml PUBLIC_API_STRANGLER_VERSION=$(PUBLIC_API_STRANGLER_VERSION) PORT=5000 TELE_PORT=5001 DOCKER_IP=$(shell docker-ip)
	docker-compose -f /tmp/integrationcompose.yml -p publicapistrangler up -d publicapistrangler
	bin/dev-wrap --config=config/integration.sh $(SBT) endToEnd/test

end-to-end-test:
	bin/end-to-end-test

unit-test:
	$(DEFAULT_CONFIG) $(SBT) test

interactive: _dev_docker_compose
	$(DEFAULT_CONFIG) $(SBT)

compile:
	$(SBT) compile

sc-debian-layout: clean
	$(SBT) scDebianLayout:packageBin

clean:
	rm -rf target

.PHONY: _dev_docker_compose
