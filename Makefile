SBT = cd api && SBT_OPTS="-Xms512M -Xmx2G -Xss1M" ./sbt

all:
	$(SBT) clean test startScript

# bazooka target
build: all

ci:
	$(SBT) -no-colors clean test startScript

clean:
	$(SBT) clean

