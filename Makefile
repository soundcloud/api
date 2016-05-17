BUILD_STACK := sbt

PWD?=$(HOME)
define SBT
	sbt -Duser.home=$(PWD)
endef

PUBLIC_API_STRANGLER_VERSION ?= $(shell artifact-manager package-version)

.PHONY: default
default: precheckin

_dev_docker_compose:
	PUBLIC_API_STRANGLER_VERSION=$(PUBLIC_API_STRANGLER_VERSION) docker-compose up --force-recreate -d publicapistrangler

_run-end-to-end-test:
	crun $(BUILD_STACK) --docker-options="--add-host=docker:$(shell docker-ip)" -- $(SBT) endToEnd/test

run: _dev_docker_compose

precheckin:
	make unit-test
	mkdir -p ./target/bazooka/build
	make -f Makefile.pipeline package
	make end-to-end-test
	docker-compose stop

end-to-end-test:
	bin/end-to-end-test

unit-test:
	crun $(BUILD_STACK) -- $(SBT) test

interactive:
	crun $(BUILD_STACK) -- $(SBT)

compile:
	crun $(BUILD_STACK) -- $(SBT) compile

sc-debian-layout: clean
	crun $(BUILD_STACK) -- $(SBT) scDebianLayout:packageBin

clean:
	rm -rf target

.PHONY: _dev_docker_compose
