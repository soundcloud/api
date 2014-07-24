#!/bin/bash
ENV=$(cat development.properties)
echo $ENV
eval $ENV vendor/sbt/bin/sbt $@
