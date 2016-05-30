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

end-to-end-test: _dev_docker_compose
	crun sbt --docker-options="--add-host=docker:$(shell docker-ip)" -- sbt endToEnd/test

unit-test:
	crun sbt -- sbt test

interactive:
	crun -i sbt -- sbt

compile:
	crun sbt -- sbt compile

sc-debian-layout: clean
	crun sbt -- sbt scDebianLayout:packageBin

clean:
	rm -rf target
	rm -rf project/project
	rm -rf project/target

.PHONY: _dev_docker_compose
_dev_docker_compose:
	PUBLIC_API_STRANGLER_VERSION=$(PUBLIC_API_STRANGLER_VERSION) docker-compose up --force-recreate -d publicapistrangler
