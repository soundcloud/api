SBT := vendor/sbt/bin/sbt -Duser.home=$(shell echo "$$HOME") -Dsbt.boot.properties=project/sbt.boot.properties -J-Xmx3G -J-Xms512m
LOAD_ENV := $(shell echo "$$(./development.properties.sh default.properties) $$1" | tr '\n' ' ')
LOAD_INTEGRATION_TESTS_ENV := $(shell echo "$$(./integration.properties.sh) $$1" | tr '\n' ' ')

PIPELINE_NUMBER ?= stable

DEPLOY_SCRIPT ?= $(shell gen-deploy-script --arch=linux --name=public-api-strangler --revision=`git rev-parse --short HEAD`)

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
	crun bazooka-cli "./$(DEPLOY_SCRIPT) bazooka --boot-timeout=120s --health-port=app \
		--health-path=/-/health --scale-step=1 --bazooka-zone=de --instance-cnt=8 \
		--proc=api"

deploy-db:
	crun bazooka-cli "./$(DEPLOY_SCRIPT) bazooka --boot-timeout=120s --health-port=app \
		--health-path=/-/health --scale-step=3 --bazooka-zone=db --instance-cnt=150 \
		--proc=api"

_dev_docker_compose:
	docker-compose up -d

compile:
	$(SBT) compile
