PUBLIC_API_STRANGLER_VERSION ?= $(shell artifact-manager package-version)

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
	crun sbt --docker-options="--add-host=docker:$(shell docker-ip)" -- $(SBT) endToEnd/test

test: unit-test

unit-test:
	crun sbt -- $(SBT) test

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
	javac -d jdk/target $<

.PHONY: _dev_docker_compose
_dev_docker_compose:
	PUBLIC_API_STRANGLER_VERSION=$(PUBLIC_API_STRANGLER_VERSION) docker-compose up --force-recreate -d publicapistrangler
