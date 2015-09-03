#!/usr/bin/env bash
ip=$(docker-ip)
endpoint="http://$ip:5000/-/health"

until $(curl --output /dev/null --silent --fail $endpoint); do
    echo "Waiting for $endpoint"
    sleep 5
done