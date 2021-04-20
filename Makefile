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
	sc crun sbt -l --config=development --expose-port 5000:5000,5001:5001,5005:5005 -- sbt run

run-no-docker:
	set -o allexport; source config/development; set +o allexport; sbt run

precheckin:
	make lint
	make unit-test
	make package
	make end-to-end-test

lint:
	$(SBT) scalafmtCheckAll

format: auto-apply-lint
auto-apply-lint:
	$(SBT) scalafmtAll

fetch-token:
	curl -X POST "https://api.soundcloud.com/oauth2/token" \
		-d "grant_type=password" \
		-d "username=$(USER)" -d "password=$(PASSWORD)" \
		-d "client_id=$(CLIENT_ID)" -d "client_secret=$(CLIENT_SECRET)"

generate-token:
	sc crun base-dev --config=e2e.secrets --enable-proxy 'make -s fetch-token | jq .access_token'

check-prometheus:
	sc prometheus promtool -- check rules config/prometheus.yml

docker-up-%:
	echo "This assumes you've run make package & make package-assets before"
	CONFIG=$* VERSION=$(PUBLIC_API_STRANGLER_VERSION) docker-compose -f docker-compose-e2e-tests.yml up -d
	sc crun -l base-dev -- sc wait http publicapistrangler:5000/-/health

end-to-end-test: remove-containers
	echo "This assumes you've run make package before"
	CONFIG=e2e VERSION=$(PUBLIC_API_STRANGLER_VERSION) docker-compose -f docker-compose-e2e-tests.yml up -d publicapistrangler
	sc crun -l base-dev -- sc wait http publicapistrangler:5000/-/health
	sc crun -l sbt -e ACCESS_TOKEN=$(shell make -s generate-token) --config=e2e.secrets -- sbt endToEnd/test
	make docker-down

local-contract-test: remove-containers docker-up-development
	cd doc && make test

package-assets:
	make --directory=asset-uploads package

contract-test: package package-assets remove-containers docker-up-development
	sc crun -l nodejs-12-dev -- make --directory=doc contract-test
	make docker-down

docker-down:
	CONFIG= VERSION=$(PUBLIC_API_STRANGLER_VERSION) docker-compose down --remove-orphans

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
	sc crun sbt -- sbt packageSC
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
		--slack-channel '#deploys' \
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

CPU_REQUEST_db = 2
CPU_REQUEST_et = 300m
CPU_REQUEST = $(if $(CPU_REQUEST_$(ZONE)),$(CPU_REQUEST_$(ZONE)),$(error CPU_REQUEST is not set for ZONE $(ZONE)))

MEMORY_REQUEST_db = 15Gi
MEMORY_REQUEST_et = 2Gi
MEMORY_REQUEST = $(if $(MEMORY_REQUEST_$(ZONE)),$(MEMORY_REQUEST_$(ZONE)),$(error MEMORY_REQUEST is not set for ZONE $(ZONE)))

deploy-prometheus:
	sc prometheus deploy -z $(ZONE) -s public-api-strangler -e production \
		--cpu.request=$(CPU_REQUEST) \
		--memory.request=$(MEMORY_REQUEST) \
		--volume-size=150Gi \
		--rule=config/prometheus.yml \
		--rule=https://ent.int.s-cloud.net/prometheus/rules/prometheus_base.yml \
		--rule=https://ent.int.s-cloud.net/prometheus/rules/jvmkit.yml \
		--rule=https://ent.int.s-cloud.net/prometheus/rules/canary_vs_release.yml \
		--rule=https://ent.int.s-cloud.net/prometheus/rules/slo.yml \
		--rule=https://ent.int.s-cloud.net/prometheus/rules/memcached.yml 
