#!/usr/bin/env bash
echo PUBLIC_API_STRANGLER_SERVER=$(bin/docker-host-ip):5000
echo MEMCACHED_TEST_HOST=`bin/docker-host-ip`
echo MEMCACHED_TEST_PORT=11211
echo ZOOKEEPER_SERVERS=$(bin/docker-host-ip):2181
