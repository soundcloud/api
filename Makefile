SBT := vendor/sbt/bin/sbt -Duser.home=$(shell echo "$$HOME") -Dsbt.boot.properties=project/sbt.boot.properties -J-Xmx3G -J-Xms512m
LOAD_ENV := $(shell echo "$$(./development.properties.sh default.properties) $$1" | tr '\n' ' ')
LOAD_INTEGRATION_TESTS_ENV := $(shell echo "$$(./integration.properties.sh) $$1" | tr '\n' ' ')

PIPELINE_NUMBER ?= stable

run: _dev_docker_compose
	$(LOAD_ENV) $(SBT) run

precheckin: test it-test

it-test: _dev_docker_compose
	$(LOAD_ENV) $(LOAD_INTEGRATION_TESTS_ENV) $(SBT) it:test

test:
	$(SBT) test

interactive: _dev_docker_compose
	$(LOAD_ENV) $(SBT)

dev: _dev_docker_compose

deploy-de:
	HEALTH_PATH=/-/health SCALE_STEP=1 BAZOOKA_APP=public-api-strangler BAZOOKA_ZONE=de INSTANCES=8 PROCESS_TYPE=api python bin/deploy.py

deploy-db:
	HEALTH_PATH=/-/health SCALE_STEP=5 BAZOOKA_APP=public-api-strangler BAZOOKA_ZONE=db INSTANCES=100 PROCESS_TYPE=api python bin/deploy.py

run: _dev_docker_compose
	$(LOAD_ENV) $(SBT) run

_dev_docker_compose:
	docker-compose up -d

