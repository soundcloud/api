DEV_STACK := sbt-jdk-8

APP_NAME := $(shell sc manifest name)
API_PUBLIC_VERSION := $(shell sc artifact-manager package-version)

API_COMPONENT := api
API_CONFIG    := production

RUNTIME_STACK := jdk-8

DOCKER_IP ?= $(shell sc docker-ip)

ZONES ?= $(error please specify the ZONES environment variable)

ifeq ($(USE_CRUN),false)
	SBT = sbt
	SBT_INTERACTIVE = sbt
else
	SBT = sc crun $(DEV_STACK) -- sbt
	SBT_INTERACTIVE = sc crun -i $(DEV_STACK) -- sbt
endif

default: precheckin

dependencies:
	docker-compose up --force-recreate -d

run: dependencies
	sc crun $(DEV_STACK) -l --config=development --expose-port 5000:5000,5001:5001,5005:5005 -- sbt run

run-no-docker:
	set -o allexport; source config/development; set +o allexport; sbt run

precheckin:
	make lint
	make unit-test
	make package
	make end-to-end-test

lint: validate-manifest
	$(SBT) scalafmtCheckAll

format: auto-apply-lint
auto-apply-lint:
	$(SBT) scalafmtAll

check-prometheus:
	sc prometheus promtool -- check rules config/prometheus.yml

docker-up-%:
	echo "This assumes you've run make package & make package-assets before"
	CONFIG=$* VERSION=$(API_PUBLIC_VERSION) docker-compose -f docker-compose-e2e-tests.yml up -d
	sc crun -l base-dev -- sc wait http apipublic:5000/-/health
	sc crun -l base-dev -- sc wait http asset-uploads:5005/-/health


end-to-end-test: stop-containers
	echo "This assumes you've run make package before"
	CONFIG=e2e VERSION=$(API_PUBLIC_VERSION) docker-compose -f docker-compose-e2e-tests.yml up -d apipublic
	sc crun -l base-dev -- sc wait http apipublic:5000/-/health
	sc crun -l $(DEV_STACK) --config=e2e.secrets -- sbt endToEnd/test
	make docker-down

local-contract-test: stop-containers docker-up-development
	cd doc && make test

package-assets:
	make --directory=asset-uploads package

contract-test: package package-assets stop-containers docker-up-development
	sc crun -l nodejs-12-dev --enable-proxy --config=e2e.secrets -- make --directory=doc contract-test
	make docker-down

docker-down:
	CONFIG= VERSION=$(API_PUBLIC_VERSION) docker-compose down --remove-orphans

stop-containers:
	CONFIG= VERSION=$(API_PUBLIC_VERSION) docker-compose rm -s -f

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
	sc crun $(DEV_STACK) -- sbt packageSC
	sc gen-wrapper-script --target="bin/$(APP_NAME)" --wrapper=api
	sc add-config config/development
	sc add-config config/e2e
	sc add-config config/$(API_CONFIG)

publish:
	sc artifact-manager publish

publish-deploy:
	sc artifact-manager deploy publish \
		--zones=$(ZONES) \
		--component="$(API_COMPONENT)" \
		--command "./api --config=$(API_CONFIG)" \
		--ingress http://$(APP_NAME).k2.lb.s-cloud.net:http \
		--ingress http://$(APP_NAME):http \
		--ingress http://$(APP_NAME).int.s-cloud.net:http \
		--ingress http://api-public.int.s-cloud.net:http \
		--public-ingress http://api.soundcloud.com:http \
		--slack '#deploys' \
		--glimpse http.api.prod.api-public \
		--prometheus.port telemetry

promote-to-stable:
	sc artifact-manager promote stable

promote-to-release:
	sc artifact-manager promote release

canary-api:
	sc k8s canary \
		--zones=$(ZONES) \
		--system=api-public \
		--env=production \
		--component="$(API_COMPONENT)" \
		--slack '#deploys' \
		--replicas=2

.PHONY: deploy-api
deploy-api:
	sc artifact-manager deploy run --zones=$(ZONES) --component="$(API_COMPONENT)"

check-autoscale:
	sc k8s --zones $(ZONES) --system $(APP_NAME) kubectl describe hpa $(APP_NAME)-$(API_COMPONENT)-autoscale

autoscale:
	sc k8s scale --zones $(ZONES) --system $(APP_NAME) --component $(API_COMPONENT) \
		--autoscale.replicas.max=150 --autoscale.replicas.min=25 \
		--autoscale.metric.name=namespace_system_env_component_track_version_pod_container:cpu_usage:rate --autoscale.metric.target-value=0.5

CPU_REQUEST_db = 2
CPU_REQUEST_replicas = 300m
CPU_REQUEST = $(if $(CPU_REQUEST_$(ZONES)),$(CPU_REQUEST_$(ZONES)),$(error CPU_REQUEST is not set for ZONES $(ZONES)))

MEMORY_REQUEST_db = 15Gi
MEMORY_REQUEST_replicas = 2Gi
MEMORY_REQUEST = $(if $(MEMORY_REQUEST_$(ZONES)),$(MEMORY_REQUEST_$(ZONES)),$(error MEMORY_REQUEST is not set for ZONES $(ZONES)))

deploy-prometheus:
	sc prometheus deploy --zones $(ZONES) -s api-public -e production \
		--cpu.request=$(CPU_REQUEST) \
		--memory.request=$(MEMORY_REQUEST) \
		--volume-size=150Gi \
		--rule=config/prometheus.yml \
		--rule=https://ent.int.s-cloud.net/prometheus/rules/prometheus_base.yml \
		--rule=https://ent.int.s-cloud.net/prometheus/rules/jvmkit.yml \
		--rule=https://ent.int.s-cloud.net/prometheus/rules/canary_vs_release.yml \
		--rule=https://ent.int.s-cloud.net/prometheus/rules/memcached.yml

GO_REVISION_GIT ?= HEAD
publish-changelog: 
	git diff-index --exit-code $(GO_REVISION_GIT)~1 RELEASE_NOTES.md && echo 'No release notes to publish...' && exit 0; \
		./scripts/release

.PHONY: validate-manifest
validate-manifest:
	sc manifest validate -m manifest.json
