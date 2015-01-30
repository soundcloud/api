SBT = SBT_OPTS="-Xms512M -Xmx2G -Xss1M" ./sbt

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

# bazooka target
build: remove.ivy.lock .install.jre
	$(SBT) clean startScript

ci: remove.ivy.lock .install.jre remove.install.jre
	$(SBT) -no-colors clean test startScript

clean: remove.ivy.lock .install.jre
	$(SBT) clean

remove.install.jre:
		rm -f .install.jre
