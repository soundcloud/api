APP_NAME := $(shell manifest name)

API_ENTRYPOINT     := api
API_INSTANCE_COUNT := 100
APITRACKUPLOAD_ENTRYPOINT     := apitrackupload
APITRACKUPLOAD_INSTANCE_COUNT := 10

API_CONFIG := production_api.sh
APITRACKUPLOAD_CONFIG := production_apitrackupload.sh
BAZOOKA_ZONES := db
RUNTIME_STACK := jdk-8

DEPLOY_SCRIPT ?= $(shell gen-deploy-script --arch=linux --name=public-api-strangler --revision=`artifact-manager package-version`)

DOCKER_IP ?= $(shell docker-ip)

CLUSTER ?= k2

ifeq ($(USE_CRUN),false)
	SBT = sbt
	SBT_INTERACTIVE = sbt
else
	SBT = crun sbt -- sbt
	SBT_INTERACTIVE = crun -i sbt -- sbt
endif

default: precheckin

run:
	docker-compose up --force-recreate -d
	crun sbt -i --docker-options="-p5000:5000 --link=strangler_zk --link=strangler_memcached --env-file=config/development" -- sbt run

precheckin:
	make unit-test
	make package
	make end-to-end-test

end-to-end-test:
	echo "This assumes you've run make package before"
	PUBLIC_API_STRANGLER_VERSION=$(shell artifact-manager package-version) docker-compose -f docker-compose-e2e-tests.yml up --force-recreate -d publicapistrangler
	bin/wait-for-http $(DOCKER_IP):4567/-/health # wait for publicapistub
	bin/wait-for-http $(DOCKER_IP):5000/-/health # wait for publicapistrangler
	crun sbt --docker-options="--link=strangler_api:strangler --link=strangler_zk:zookeeper" -- sbt endToEnd/test

unit-test:
	$(SBT) test

contract-upload:
	contract-upload

contract-promote:
	contract-promote

interactive:
	$(SBT_INTERACTIVE)

clean:
	rm -rf target
	rm -rf project/project
	rm -rf project/target
	rm -rf jdk/target

package: prepare-package-layout
	docker pull docker.dev.s-cloud.net/$(RUNTIME_STACK):latest
	artifact-manager package --runtime=$(RUNTIME_STACK)
	artifact-manager bazooka build --zones=$(BAZOOKA_ZONES) --runtime=$(RUNTIME_STACK) \
				 --proc="$(API_ENTRYPOINT) --config=$(API_CONFIG)" \
				 --proc="$(APITRACKUPLOAD_ENTRYPOINT) --config=$(APITRACKUPLOAD_CONFIG)"

prepare-package-layout: patched-jdk
	crun sbt -- sbt scDebianLayout:packageBin
	gen-wrapper-script --target="bin/$(APP_NAME)" --wrapper=$(API_ENTRYPOINT)
	gen-wrapper-script --target="bin/$(APP_NAME)" --wrapper=$(APITRACKUPLOAD_ENTRYPOINT)
	gen-postinst-script
	add-config config/development
	add-config config/e2e
	add-config config/$(API_CONFIG)
	add-config config/$(APITRACKUPLOAD_CONFIG)
	rm -rf target/deb/srv/public-api-strangler/jdk/target
	mkdir -p target/deb/srv/public-api-strangler/jdk/target
	cp -r jdk/target target/deb/srv/public-api-strangler/jdk

.PHONY: patched-jdk
patched-jdk: jdk/target/sun/nio/ch/Util.class

jdk/target/sun/nio/ch/Util.class: jdk/src/share/classes/sun/nio/ch/Util.java
	mkdir -p jdk/target
	crun jdk-8 -- javac -d jdk/target $<

publish:
	artifact-manager publish
	artifact-manager bazooka publish --runtime=$(RUNTIME_STACK) --zones=$(BAZOOKA_ZONES)

publish-deploy:
	artifact-manager deploy publish \
		--cluster=$(CLUSTER) \
		--component="$(API_ENTRYPOINT)" \
		--command "./$(API_ENTRYPOINT) --config=$(API_CONFIG)" \
		--public \
		--ingress http://$(APP_NAME).$(CLUSTER).lb.s-cloud.net:http \
		--ingress http://$(APP_NAME).int.s-cloud.net:http \
		--ingress http://public-api.int.s-cloud.net:http \
		--ingress http://api.soundcloud.com:http \
		--glimpse http.strangler.prod.public-api \
		--slack-channel '#core-services' \
		--prometheus.port telemetry \
		--strategy.rolling-update.max-surge.percent 10
	artifact-manager deploy publish \
		--cluster=$(CLUSTER) \
		--component="$(APITRACKUPLOAD_ENTRYPOINT)" \
		--command "./$(APITRACKUPLOAD_ENTRYPOINT) --config=$(APITRACKUPLOAD_CONFIG)" \
		--public \
		--ingress http://$(APP_NAME)-trackupload.$(CLUSTER).lb.s-cloud.net:http \
		--glimpse http.strangler-trackupload.test.public-api \
		--slack-channel '#core-services' \
		--prometheus.port telemetry \
		--strategy.rolling-update.max-surge.percent 10

promote-to-stable:
	artifact-manager promote stable
	artifact-manager bazooka promote stable --zones=$(BAZOOKA_ZONES)

promote-to-release:
	artifact-manager promote release
	artifact-manager bazooka promote release --zones=$(BAZOOKA_ZONES)

.PHONY: deploy-db-api
deploy-db-api:
	crun bazooka-cli "./$(DEPLOY_SCRIPT) bazooka --clean-revs \
		--boot-timeout=60s --health-port=app --health-path=/tracks/116540862?client_id=hannes_test \
		--bazooka-zone=db --scale-step=10 --instance-cnt=$(API_INSTANCE_COUNT) \
		--proc=$(API_ENTRYPOINT) \
		--slack-channels=#core-services"

.PHONY: deploy-db-apitrackupload
deploy-db-apitrackupload:
	crun bazooka-cli "./$(DEPLOY_SCRIPT) bazooka --clean-revs \
		--boot-timeout=60s --health-port=app --health-path=/tracks/116540862?client_id=hannes_test \
		--bazooka-zone=db --scale-step=10 --instance-cnt=$(APITRACKUPLOAD_INSTANCE_COUNT) \
		--proc=$(APITRACKUPLOAD_ENTRYPOINT) \
		--slack-channels=#core-services"

.PHONY: deploy-k8s-api
deploy-k8s-api:
	artifact-manager deploy run --cluster="$(CLUSTER)" --component="$(API_ENTRYPOINT)"

.PHONY: deploy-k8s-apitrackupload
deploy-k8s-apitrackupload:
	artifact-manager deploy run --cluster="$(CLUSTER)" --component="$(APITRACKUPLOAD_ENTRYPOINT)"
