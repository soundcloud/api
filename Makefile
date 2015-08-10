SBT := vendor/sbt/bin/sbt -Duser.home=$(shell echo "$$HOME") -Dsbt.boot.properties=project/sbt.boot.properties -J-Xmx3G -J-Xms512m

run: _dev_docker_compose
	./bin/source-config --config=config/default.sh $(SBT) run

precheckin: unit-test it-test

integration-test: _dev_docker_compose
	./bin/source-config --config=config/integration.sh $(SBT) it:test

unit-test:
	$(SBT) test

interactive: _dev_docker_compose
	./bin/source-config --config=config/default.sh $(SBT)

dev: _dev_docker_compose

_dev_docker_compose:
	docker-compose up -d

compile:
	$(SBT) compile

sc-debian-layout: clean
	$(SBT) scDebianLayout:packageBin

clean:
	rm -rf target
