APP_NAME := $(shell sc manifest name)
PUBLIC_API_STRANGLER_VERSION := $(shell sc artifact-manager package-version)

API_COMPONENT := api
API_CONFIG    := production

RUNTIME_STACK := jdk-8

DOCKER_IP ?= $(shell sc docker-ip)

ZONE ?= $(error please specify the ZONE environment variable)

ifeq ($(USE_CRUN),false)
	SBT = sbt
	SBT_INTERACTIVE = sbt
else
	SBT = sc crun sbt -- sbt
	SBT_INTERACTIVE = sc crun -i sbt -- sbt
endif

default: precheckin

dependencies:
	docker-compose up --force-recreate -d

run: dependencies
	sc crun sbt -i --docker-options="-p 5000:5000 -p 5001:5001 --link=strangler_zk --link=strangler_memcached --env-file=config/development" -- sbt run

run-debug: dependencies
	sc crun sbt -i --docker-options="-p 5000:5000 -p 5001:5001 -p 5005:5005 --link=strangler_zk --link=strangler_memcached --env-file=config/development" -- sbt run

run-no-docker:
	set -o allexport; source config/development; set +o allexport; sbt run

precheckin:
	make lint
	make unit-test
	make package
	make end-to-end-test

lint:
	$(SBT) scalafmtCheckAll

auto-apply-lint:
	$(SBT) scalafmtAll

docker-up-%:
	echo "This assumes you've run make package before"
	CONFIG=$* VERSION=$(PUBLIC_API_STRANGLER_VERSION) \
	docker-compose -f docker-compose-e2e-tests.yml up --force-recreate -d publicapistrangler

wait:
	sc wait http $(DOCKER_IP):4567/-/health # wait for publicapistub
	sc wait http $(DOCKER_IP):5000/-/health # wait for publicapistrangler

end-to-end-test: remove-containers docker-up-e2e wait
	sc crun sbt --docker-options="--link=strangler_api:strangler --link=strangler_zk:zookeeper" -- sbt endToEnd/test
	make docker-down

local-contract-test: remove-containers docker-up-development wait
	cd doc && make test

contract-test: package remove-containers docker-up-development wait
	sc crun -l nodejs-12-dev -- make --directory=doc contract-test
	make docker-down

docker-down:
	CONFIG= VERSION=$(PUBLIC_API_STRANGLER_VERSION) docker-compose down

remove-containers:
	CONFIG= VERSION=$(PUBLIC_API_STRANGLER_VERSION) docker-compose rm -s -f

unit-test:
	$(SBT) test

interactive:
	$(SBT_INTERACTIVE)

clean:
	rm -rf target
	rm -rf project/project
	rm -rf project/target
	rm -rf endToEndTests/target

package: prepare-package-layout
	sc artifact-manager package --runtime=$(RUNTIME_STACK)

prepare-package-layout:
	sc crun sbt -- sbt scDebianLayout:packageBin
	sc gen-wrapper-script --target="bin/$(APP_NAME)" --wrapper=api
	sc add-config config/development
	sc add-config config/e2e
	sc add-config config/$(API_CONFIG)

publish:
	sc artifact-manager publish

publish-deploy:
	sc artifact-manager deploy publish \
		--zone=$(ZONE) \
		--component="$(API_COMPONENT)" \
		--command "./api --config=$(API_CONFIG)" \
		--ingress http://$(APP_NAME).k2.lb.s-cloud.net:http \
		--ingress http://$(APP_NAME).$(ZONE).lb.s-cloud.net:http \
		--ingress http://$(APP_NAME).int.s-cloud.net:http \
		--ingress http://public-api.int.s-cloud.net:http \
		--public-ingress http://api.soundcloud.com:http \
		--slack-channel '#integrations-alerts' \
		--glimpse http.strangler.prod.public-api \
		--prometheus.port telemetry

promote-to-stable:
	sc artifact-manager promote stable

promote-to-release:
	sc artifact-manager promote release

canary-api:
	sc k8s canary \
		--zone=$(ZONE) \
		--system=public-api-strangler \
		--env=production \
		--component="$(API_COMPONENT)" \
		--replicas=2

.PHONY: deploy-api
deploy-api:
	sc artifact-manager deploy run --zone="$(ZONE)" --component="$(API_COMPONENT)"

