SBT := vendor/sbt/bin/sbt -Duser.home=$(shell echo "$$HOME") -Dsbt.boot.properties=project/sbt.boot.properties -J-Xmx3G -J-Xms512m
DEFAULT_CONFIG := bin/dev-wrap --config=config/default.sh

PUBLIC_API_STRANGLER_VERSION ?= $(shell artifact-manager package-version)

_dev_docker_compose:
	env DOCKER_IP=$(shell docker-ip) PUBLIC_API_STRANGLER_VERSION=$(PUBLIC_API_STRANGLER_VERSION) docker-compose up -d publicapistrangler

_run-end-to-end-test:
	$(DEFAULT_CONFIG) $(SBT) endToEnd/test

run: _dev_docker_compose

precheckin:
	make unit-test
	mkdir -p ./target/bazooka/build
	make -f Makefile.pipeline package publish
	make end-to-end-test

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
