#!/bin/bash

sed "s/zookeeper/$(bin/docker-host-ip)/g" $1 | sed "s/memcached/$(bin/docker-host-ip)/g"