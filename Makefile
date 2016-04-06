SBT := vendor/sbt/bin/sbt -Duser.home=$(shell echo "$$HOME") -Dsbt.boot.properties=project/sbt.boot.properties -J-Xmx3G -J-Xms512m
DEVELOPMENT_CONFIG := bin/dev-wrap --config=config/development.sh

PUBLIC_API_STRANGLER_VERSION ?= $(shell artifact-manager package-version)

.PHONY: default
default: precheckin

_dev_docker_compose:
	env DOCKER_IP=$(shell docker-ip) PUBLIC_API_STRANGLER_VERSION=$(PUBLIC_API_STRANGLER_VERSION) docker-compose up -d publicapistrangler

_run-end-to-end-test:
	$(DEVELOPMENT_CONFIG) $(SBT) endToEnd/test

run: _dev_docker_compose

precheckin:
	make unit-test
	mkdir -p ./target/bazooka/build
	make -f Makefile.pipeline package publish
	remove-containers
	make end-to-end-test
	remove-containers

end-to-end-test:
	bin/end-to-end-test

unit-test:
	$(DEVELOPMENT_CONFIG) $(SBT) test

interactive:
	$(DEVELOPMENT_CONFIG) $(SBT)

compile:
	$(SBT) compile

sc-debian-layout: clean
	$(SBT) scDebianLayout:packageBin

clean:
	rm -rf target

.PHONY: _dev_docker_compose
