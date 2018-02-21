APP_NAME := $(shell sc manifest name)

API_COMPONENT := api
API_CONFIG    := production_api.sh.enc

APITRACKUPLOAD_COMPONENT := apitrackupload
APITRACKUPLOAD_CONFIG    := production_apitrackupload.sh.enc

RUNTIME_STACK := jdk-8

DOCKER_IP ?= $(shell sc docker-ip)

CLUSTER ?= k2

ifeq ($(USE_CRUN),false)
	SBT = sbt
	SBT_INTERACTIVE = sbt
else
	SBT = sc crun sbt -- sbt
	SBT_INTERACTIVE = sc crun -i sbt -- sbt
endif

default: precheckin

run:
	docker-compose up --force-recreate -d
	sc crun sbt -i --docker-options="-p 5000:5000 --link=strangler_zk --link=strangler_memcached --env-file=config/development" -- sbt run

run-no-docker:
	set -o allexport; source config/production_api.sh; set +o allexport; sbt run

precheckin:
	make unit-test
	make package
	make end-to-end-test

end-to-end-test:
	echo "This assumes you've run make package before"
	env PUBLIC_API_STRANGLER_VERSION=$(shell sc artifact-manager package-version) \
		docker-compose -f docker-compose-e2e-tests.yml up --force-recreate -d publicapistrangler
	sc wait http $(DOCKER_IP):4567/-/health # wait for publicapistub
	sc wait http $(DOCKER_IP):5000/-/health # wait for publicapistrangler
	sc crun sbt --docker-options="--link=strangler_api:strangler --link=strangler_zk:zookeeper" -- sbt endToEnd/test

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

package: prepare-package-layout
	sc artifact-manager package --runtime=$(RUNTIME_STACK)

prepare-package-layout:
	sc crun sbt -- sbt scDebianLayout:packageBin
	sc gen-wrapper-script --target="bin/$(APP_NAME)" --wrapper=api
	sc gen-postinst-script
	sc add-config config/development
	sc add-config config/e2e
	sc add-config config/$(API_CONFIG)
	sc add-config config/$(APITRACKUPLOAD_CONFIG)

publish:
	sc artifact-manager publish

publish-deploy:
	sc artifact-manager deploy publish \
		--cluster=$(CLUSTER) \
		--component="$(API_COMPONENT)" \
		--command "./api --config=$(API_CONFIG)" \
		--ingress http://$(APP_NAME).$(CLUSTER).lb.s-cloud.net:http \
		--ingress http://$(APP_NAME).int.s-cloud.net:http \
		--ingress http://public-api.int.s-cloud.net:http \
		--public-ingress http://api.soundcloud.com:http \
		--glimpse http.strangler.prod.public-api \
		--slack-channel '#backend-productivity' \
		--prometheus.port telemetry \
		--strategy.rolling-update.max-surge.percent 20
		--set MEMORY_REQUEST=3072Mi
		--set MEMORY_LIMIT=4096Mi
	sc artifact-manager deploy publish \
		--cluster=$(CLUSTER) \
		--component="$(APITRACKUPLOAD_COMPONENT)" \
		--command "./api --config=$(APITRACKUPLOAD_CONFIG)" \
		--ingress http://$(APP_NAME)-trackupload.$(CLUSTER).lb.s-cloud.net:http \
		--public-ingress http://api.soundcloud.com:http \
		--glimpse http.strangler-trackupload.prod.public-api \
		--slack-channel '#backend-productivity' \
		--prometheus.port telemetry \
		--strategy.rolling-update.max-surge.percent 20
		--set MEMORY_REQUEST=4096Mi
		--set MEMORY_LIMIT=4096Mi

promote-to-stable:
	sc artifact-manager promote stable

promote-to-release:
	sc artifact-manager promote release

.PHONY: deploy-k8s-api
deploy-k8s-api:
	sc artifact-manager deploy run --cluster="$(CLUSTER)" --component="$(API_COMPONENT)"

.PHONY: deploy-k8s-apitrackupload
deploy-k8s-apitrackupload:
	sc artifact-manager deploy run --cluster="$(CLUSTER)" --component="$(APITRACKUPLOAD_COMPONENT)"
