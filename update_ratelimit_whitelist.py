#!/usr/bin/env python

import re
import subprocess

def production_properties():
    with open('production.properties', 'r') as f:
        return f.readlines()

def zookeeper_host():
    for line in production_properties():
        result = re.match( r'ZOOKEEPER_SERVERS="(.*?),.*"', line)
        if result:
            return result.group(1) 

def app_name():
    for line in production_properties():
        result = re.match( r'APP_NAME="(.*)"', line)
        if result:
            return result.group(1)
    
whitelist_path = "/" + app_name() + "/ratelimits/whitelist"
zookeeper_server = zookeeper_host()
   
def create_command(node):
    return "create " + node + " '' false false true"  

def delete_command(node):
    return "rmr " + node
  
def create_node_recursively(node, zookeeper_host):
    subprocess.call(["zk-shell", "--run-once", create_command(node), zookeeper_host])

def delete_node_recursively(node, zookeeper_host):
    subprocess.call(["zk-shell", "--run-once", delete_command(node), zookeeper_host])

def delete_whitelist(zookeeper_host):
    delete_node_recursively(whitelist_path, zookeeper_host)

def create_whitelist(zookeeper_host):
    create_node_recursively(whitelist_path, zookeeper_host)

def whitelisted_clients():
    with open('ratelimiting_whitelist.txt', 'r') as f:
        return [urn.rstrip() for urn in f.readlines()]

def whitelisted_client_path(urn):
    return whitelist_path + '/' + urn

def populate_whitelist(zookeeper_host):
    for urn in whitelisted_clients():
        create_node_recursively(whitelisted_client_path(urn), zookeeper_host)
        print "added to whitelist: " + urn
    
def run():        
    print "Updating whitelist on ZooKeeper server " + zookeeper_server + "..." 
    print "Removing previous whitelist..."  
    delete_whitelist(zookeeper_server)
    print "Creating new empty whitelist..."    
    create_whitelist(zookeeper_server)
    populate_whitelist(zookeeper_server)
    print "Done!"
    
run()
