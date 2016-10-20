PUBLIC_API_STRANGLER_VERSION ?= $(shell artifact-manager package-version)

STRANGLER_CONTAINER ?= $(shell docker ps --filter "ancestor=docker.dev.s-cloud.net/public-api-strangler:$(PUBLIC_API_STRANGLER_VERSION)" --format "{{.Names}}")
ZOOKEEPER_CONTAINER ?= $(shell docker ps --filter "ancestor=docker.dev.s-cloud.net/sc-zookeeper" --format "{{.Names}}")
DOCKER_IP ?= $(shell docker-ip)

PWD?=$(HOME)

ifeq ($(USE_CRUN),false)
	SBT = vendor/sbt/bin/sbt
	SBT_INTERACTIVE = $(SBT)
	JAVAC = javac
else
	SBT = crun sbt -- sbt
	SBT_INTERACTIVE = crun -i sbt -- sbt
	JAVAC = crun jdk-8 -- javac
endif

.PHONY: default
default: precheckin

run: _dev_docker_compose

precheckin:
	make unit-test
	mkdir -p ./target/bazooka/build
	make -f Makefile.pipeline package
	make end-to-end-test
	docker-compose stop

end-to-end-test: _dev_docker_compose
	bin/wait-for-http $(DOCKER_IP):4567/-/health # wait for publicapistub
	bin/wait-for-http $(DOCKER_IP):5000/-/health # wait for publicapistrangler
	crun sbt --docker-options="--link=$(STRANGLER_CONTAINER):strangler --link=$(ZOOKEEPER_CONTAINER):zookeeper" -- vendor/sbt/bin/sbt endToEnd/test

test: unit-test

unit-test:
	$(SBT) test

interactive-lite:
	source config/baremetal.sh && $(SBT)

interactive:
	$(SBT_INTERACTIVE)

compile:
	$(SBT) compile

sc-debian-layout: clean patched-jdk
	$(SBT) scDebianLayout:packageBin

clean:
	rm -rf target
	rm -rf project/project
	rm -rf project/target
	rm -rf jdk/target

.PHONY: patched-jdk
patched-jdk: jdk/target/sun/nio/ch/Util.class

jdk/target/sun/nio/ch/Util.class: jdk/src/share/classes/sun/nio/ch/Util.java
	mkdir -p jdk/target
	$(JAVAC) -d jdk/target $<

.PHONY: _dev_docker_compose
_dev_docker_compose:
	PUBLIC_API_STRANGLER_VERSION=$(PUBLIC_API_STRANGLER_VERSION) docker-compose up --force-recreate -d publicapistrangler
