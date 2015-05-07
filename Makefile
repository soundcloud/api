SBT := vendor/sbt/bin/sbt -Duser.home=$(shell echo "$$HOME") -Dsbt.boot.properties=project/sbt.boot.properties -J-Xmx3G -J-Xms512m
LOAD_ENV := $(shell echo "$$(./development.properties.sh development.properties) $$1" | tr '\n' ' ')
LOAD_IT_ENV := $(shell echo "MEMCACHED_TEST_HOST=`bin/docker-host-ip` MEMCACHED_TEST_PORT=11211")

VENDOR_DIR=$(PWD)/vendor
BIN_DIR=$(PWD)/vendor/bin

PLATFORM := $(shell sh -c 'uname -s 2>/dev/null')

JRE=jre-8u31
JRE_TARBALL=http://files.int.s-cloud.net/java/jre/$(JRE)-linux-x64.tar.gz
JRE_DIR=$(VENDOR_DIR)/$(JRE)

export PATH:=$(JRE_DIR)/bin:$(PATH)

.install.jre:
ifeq ($(PLATFORM), Linux)
	mkdir -p $(JRE_DIR)
	curl -L $(JRE_TARBALL) | tar xz -C $(JRE_DIR) --strip-components 1
endif
	touch $@

remove.ivy.lock:
	rm -f .ivy2/.sbt.ivy.lock

all: remove.ivy.lock .install.jre
	$(SBT) clean test startScript

compile:
	$(SBT) compile

# bazooka target
build: remove.ivy.lock .install.jre
	$(SBT) clean startScript

run: _dev_docker_compose
	$(LOAD_ENV) $(SBT) run

precheckin: test it-test

ci: remove.ivy.lock remove.install.jre .install.jre
	$(SBT) -no-colors clean test startScript

it-test: _dev_docker_compose
	$(LOAD_ENV) $(LOAD_IT_ENV) $(SBT) it:test

test:
	$(SBT) test

clean: remove.ivy.lock .install.jre
	$(SBT) clean

remove.install.jre:
		rm -f .install.jre

_dev_docker_compose:
	docker-compose up -d
