#!/bin/bash +x
eval vendor/sbt/bin/sbt -Duser.home=$HOME -J-Xmx3G -J-Xms512m -Dsbt.log.noformat=true -Dsbt.boot.properties=project/sbt.boot.properties "$@"
