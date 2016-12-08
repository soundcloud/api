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
	make -f Makefile.pipeline package
	make end-to-end-test

end-to-end-test:
	echo "This assumes you've run make -f Makefile.pipeline package before"
	PUBLIC_API_STRANGLER_VERSION=$(shell artifact-manager package-version) docker-compose -f docker-compose-e2e-tests.yml up --force-recreate -d publicapistrangler
	bin/wait-for-http localhost:4567/-/health # wait for publicapistub
	bin/wait-for-http localhost:5000/-/health # wait for publicapistrangler
	crun sbt --docker-options="--link=strangler_api:strangler --link=strangler_zk:zookeeper" -- sbt endToEnd/test

test:
	$(SBT) test

interactive:
	$(SBT_INTERACTIVE)

clean:
	rm -rf target
	rm -rf project/project
	rm -rf project/target
	rm -rf jdk/target
