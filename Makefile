SBT := vendor/sbt/bin/sbt -Duser.home=$(shell echo "$$HOME") -Dsbt.boot.properties=project/sbt.boot.properties -J-Xmx3G -J-Xms512m
CONFIG := bin/dev-wrap --config=config/default.sh

_dev_docker_compose:
	docker-compose up -d dev

_run-local-integration-test:
	$(CONFIG) $(SBT) it:test

run: _dev_docker_compose
	$(CONFIG) $(SBT) run

precheckin: unit-test local-integration-test

integration-test:
	bin/local-integration-test

unit-test:
	$(CONFIG) $(SBT) test

interactive: _dev_docker_compose
	$(CONFIG) $(SBT)

compile:
	$(SBT) compile

sc-debian-layout: clean
	$(SBT) scDebianLayout:packageBin

clean:
	rm -rf target
