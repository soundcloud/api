ZONES ?= db
SYSTEM := $(shell sc manifest name)
API_SYSTEM := $(shell sc manifest name --manifest=../manifest.json)
COMPONENT := doc

CRUN = sc crun nodejs-lts

.PHONY: run clean package publish promote publish-deploy deploy

resolve_api: node_modules
	./node_modules/.bin/multi-file-swagger -o yaml api.yaml > compiled_api.yaml

node_modules:
	npm i

run: resolve_api
	$(CRUN) -i -o'-p3000:3000' -- node app.js
	$(MAKE) clear

test: resolve_api
	./node_modules/.bin/dredd
	$(MAKE) clear

contract-test: test

clean:
	rm -rf node_modules

clear:
	rm compiled_api.yaml

package:
	sc add-config config/*
	sc add-files api.yaml
	sc add-files app.js
	sc add-files package.json
	sc add-files package-lock.json
	sc add-files run-swagger-ui.sh
	$(CRUN) --  npm install
	sc add-files node_modules
	sc add-files assets
	sc add-files components
	sc gen-wrapper-script --target="./run-swagger-ui.sh" --wrapper=$(COMPONENT)
	sc artifact-manager package --runtime=nodejs-lts

publish:
	sc artifact-manager publish

promote:
	sc artifact-manager promote stable
	sc artifact-manager promote release

publish-deploy:
	sc artifact-manager deploy publish \
		--zones=$(ZONES) \
		--system=$(API_SYSTEM) \
		--component=$(COMPONENT) \
		--glimpse=http.$(COMPONENT).prod.$(SYSTEM) \
		--env=production \
		--slack '#deploys' \
		--ingress=http://$(SYSTEM):http \
		--ingress=http://$(SYSTEM).int.s-cloud.net:http \
		--ingress=http://$(SYSTEM):http \
		--ingress=https://$(SYSTEM).soundcloud.org:http \
		--public-ingress=https://developers.soundcloud.com:http/docs/api/explorer
		
deploy:
	sc artifact-manager deploy run \
		--zones=$(ZONES) \
		--system=$(API_SYSTEM) \
		--component=$(COMPONENT) \
		--env=production
