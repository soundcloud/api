SBT = SBT_OPTS="-Xms512M -Xmx2G -Xss1M" ./sbt

all:
	$(SBT) clean test startScript

# bazooka target
build:
	$(SBT) clean startScript

ci:
	$(SBT) -no-colors clean test startScript

clean:
	$(SBT) clean

