#!/bin/bash
ENV=$(cat .env.example)
echo $ENV
eval $ENV vendor/sbt/bin/sbt $@
