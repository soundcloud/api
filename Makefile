BUILD_STACK := sbt

PUBLIC_API_STRANGLER_VERSION ?= $(shell artifact-manager package-version)

.PHONY: default
default: precheckin

run: _dev_docker_compose

precheckin:
	make unit-test
	mkdir -p ./target/bazooka/build
	make -f Makefile.pipeline package
	make end-to-end-test
	docker-compose stop

end-to-end-test:
	PUBLIC_API_STRANGLER_VERSION=$(PUBLIC_API_STRANGLER_VERSION) docker-compose up --force-recreate -d publicapistrangler
	crun $(BUILD_STACK) --docker-options="--add-host=docker:$(shell docker-ip)" -- sbt endToEnd/test
	docker-compose stop

unit-test:
	crun $(BUILD_STACK) -- sbt test

interactive:
	crun -i $(BUILD_STACK) -- sbt

compile:
	crun $(BUILD_STACK) -- sbt compile

sc-debian-layout: clean
	crun $(BUILD_STACK) -- sbt scDebianLayout:packageBin

clean:
	rm -rf target

.PHONY: _dev_docker_compose
