PUBLIC_API_STRANGLER_VERSION ?= $(shell artifact-manager package-version)

STRANGLER_CONTAINER ?= $(shell docker ps --filter "ancestor=docker.dev.s-cloud.net/public-api-strangler:$(PUBLIC_API_STRANGLER_VERSION)" --format "{{.Names}}")
ZOOKEEPER_CONTAINER ?= $(shell docker ps --filter "ancestor=docker.dev.s-cloud.net/sc-zookeeper" --format "{{.Names}}")

PWD?=$(HOME)
define SBT
	sbt
endef

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
	docker ps -a
	crun sbt --docker-options="--link=$(STRANGLER_CONTAINER):strangler --link=$(ZOOKEEPER_CONTAINER):zookeeper" -- $(SBT) endToEnd/test

test: unit-test

unit-test:
	crun sbt -- $(SBT) test

interactive-lite:
	source config/baremetal.sh && $(SBT)

interactive:
	crun -i sbt -- $(SBT)

compile:
	crun sbt -- $(SBT) compile

sc-debian-layout: clean patched-jdk
	crun sbt -- $(SBT) scDebianLayout:packageBin

clean:
	rm -rf target
	rm -rf project/project
	rm -rf project/target
	rm -rf jdk/target

.PHONY: patched-jdk
patched-jdk: jdk/target/sun/nio/ch/Util.class

jdk/target/sun/nio/ch/Util.class: jdk/src/share/classes/sun/nio/ch/Util.java
	mkdir -p jdk/target
	crun jdk-8 -- javac -d jdk/target $<

.PHONY: _dev_docker_compose
_dev_docker_compose:
	PUBLIC_API_STRANGLER_VERSION=$(PUBLIC_API_STRANGLER_VERSION) docker-compose up --force-recreate -d publicapistrangler
