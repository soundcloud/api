ZONE ?= db
SYSTEM := $(shell sc manifest name)
COMPONENT := doc

CRUN = sc crun nodejs-12

.PHONY: run clean package publish promote publish-deploy deploy

run:
	$(CRUN) -i -o'-p3000:3000' -- node app.js

clean:
	rm -rf node_modules

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

	sc gen-wrapper-script --target="./run-swagger-ui.sh" --wrapper=$(COMPONENT)
	sc artifact-manager package --runtime=nodejs-12

publish:
	sc artifact-manager publish

promote:
	sc artifact-manager promote stable
	sc artifact-manager promote release

publish-deploy:
	sc artifact-manager deploy publish \
		--zone=$(ZONE) \
		--system=$(SYSTEM) \
		--component=$(COMPONENT) \
		--glimpse=http.$(COMPONENT).prod.$(SYSTEM) \
		--env=production \
		--ingress=http://$(SYSTEM).lb.$(ZONE).s-cloud.net:http \
		--ingress=http://$(SYSTEM).int.s-cloud.net:http \
		--ingress=http://$(SYSTEM):http \
		--ingress=https://$(SYSTEM).soundcloud.org:http
		
deploy:
	sc artifact-manager deploy run \
		--zone=$(ZONE) \
		--system=$(SYSTEM) \
		--component=$(COMPONENT) \
		--env=production
